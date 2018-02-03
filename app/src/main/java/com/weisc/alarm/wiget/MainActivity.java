package com.weisc.alarm.wiget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.weisc.alarm.R;
import com.weisc.alarm.data.Alarm;
import com.weisc.alarm.data.AlarmDao;
import com.weisc.alarm.util.AlarmUtil;
import com.weisc.alarm.wiget.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final int CREATE_ALARM = 1;

    private boolean isExit;
    private List<Alarm> alarmList = new ArrayList<>();
    private ListView alarmListView;
    private AlarmAdapter adapter;

    private FloatingActionButton addAlarm;

    private AlarmService.AlarmServiceBinder alarmServiceBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initAlarmService();
        initAlarmList();
        alarmListView = (ListView) findViewById(R.id.alarmList);
        adapter = new AlarmAdapter(this, R.layout.alarm_item, alarmList);
        alarmListView.setAdapter(adapter);
        alarmListView.setOnItemClickListener(mItemListener);
        registerForContextMenu(alarmListView);
        addAlarm = (FloatingActionButton) findViewById(R.id.addAlarm);
        addAlarm.setOnClickListener(this);

        super.onCreate(savedInstanceState);
    }

    private void initAlarmService() {
        Intent service = new Intent(this, AlarmService.class);
//        startService(service);
        ServiceConnection conn = new AlarmServiceConnection();
        bindService(service, conn, BIND_AUTO_CREATE);
    }

    private void initAlarmList() {
        Log.d("ALARM", "initAlarmList");
        alarmList = AlarmDao.loadAlarms(this);
    }

    private void initAlarm() {
        for (Alarm alarm : alarmList) {
            if (alarm.isStatus())
                alarmServiceBinder.setAlarm(alarm, true);
        }
    }

    private void deleteAlarm(Alarm alarm) {
        if (alarm.isStatus()) {
            alarmServiceBinder.cancelAlarm(alarm);
        }
        adapter.remove(alarm);
        adapter.notifyDataSetChanged();
        AlarmDao.deleteAlarm(this, alarm);
    }

    @Override
    public void onBackPressed() {
        Timer tExit;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            //退出
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSettings:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_menu, menu);
        menu.setHeaderTitle("操作");
        menu.setHeaderIcon(android.support.v7.appcompat.R.drawable.abc_ic_menu_cut_mtrl_alpha);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        switch (item.getItemId()) {
            case R.id.menuDelete: {
                Alarm alarm = adapter.getItem(position);
                deleteAlarm(alarm);
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.addAlarm: {
                startActivityForResult(new Intent(this, SetAlarmActivity.class), CREATE_ALARM);
                break;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CREATE_ALARM:
                if (resultCode == RESULT_OK) {
                    int hour = data.getIntExtra(SetAlarmActivity.INTENT_ALARM_DATA_HOUR, -1);
                    int minute = data.getIntExtra(SetAlarmActivity.INTENT_ALARM_DATA_MINUTE, -1);
                    int repeatDate = data.getIntExtra(SetAlarmActivity.INTENT_ALARM_DATA_REPEAT, -1);
                    if (hour == -1 || minute == -1 || repeatDate == -1) {
                        Log.d("ALARM", "Alarm 创建失败");
                        return;
                    }
                    String ringtone = data.getStringExtra(SetAlarmActivity.INTENT_ALARM_DATA_RINGTONE);
                    Alarm alarm = new Alarm(-1, hour, minute, repeatDate, true, ringtone);
                    AlarmDao.saveAlarm(this, alarm);
                    adapter.add(alarm);
                    alarmServiceBinder.setAlarm(alarm, alarm.isStatus());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "闹钟将在" + AlarmUtil.timeToText(AlarmUtil.nextTimeInMills(alarm)) +
                            "之后响起", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    private class AlarmServiceConnection implements ServiceConnection, AlarmService.ActivityCallback {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            alarmServiceBinder = (AlarmService.AlarmServiceBinder) service;
            initAlarm();
            alarmServiceBinder.setActivityCallBack(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void setAlarmSwitchOff(Alarm alarm) {
            Log.d("ALARM", "setAlarmSwitchOff: ");
            adapter.changeAlarmStatus(alarm, false);
        }
    }


    private class AlarmAdapter extends ArrayAdapter<Alarm> {
        private int resource;
        private Context context;

        public AlarmAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Alarm> objects) {
            super(context, resource, objects);
            this.resource = resource;
            this.context = context;
        }

        public void changeAlarmStatus(@Nullable Alarm alarm, boolean status) {
            int size = getCount();
            for (int i = 0; i < size; i++) {
                Alarm item = getItem(i);
                if (item.getId() == alarm.getId()) {
                    item.setStatus(status);
                    AlarmDao.saveAlarm(context, item);
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Alarm alarm = getItem(position);
            View view;
            ViewHolder viewHolder;

            if (convertView == null) {
                view = getLayoutInflater().from(getContext()).inflate(resource, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.alarmTimeText = view.findViewById(R.id.alarmTimeText);
                viewHolder.aSwitch = view.findViewById(R.id.alarmSwitch);
                viewHolder.repeatDate = view.findViewById(R.id.alarmRepeat);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.alarmTimeText.setText(alarm.getTimeText());
            viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (alarm.isStatus() != isChecked) {
                        changeAlarmStatus(alarm, isChecked);
                        alarmServiceBinder.setAlarm(alarm, isChecked);
                    }
                }
            });

            viewHolder.aSwitch.setChecked(alarm.isStatus());
            viewHolder.repeatDate.setText(alarm.getRepeatText());
            return view;
        }

        class ViewHolder {
            TextView alarmTimeText;
            Switch aSwitch;
            TextView repeatDate;
        }
    }

    private final AdapterView.OnItemClickListener mItemListener = new AdapterView.OnItemClickListener() {
        SparseBooleanArray mShowItems = new SparseBooleanArray();
        private View settingsView;

        /**
         *  点击列表项时，显示或者隐藏设置项。
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            showOrHide(view, position);
        }

        private void showOrHide(View view, int position) {
            ViewGroup itemView = (ViewGroup) view;
            if (mShowItems.get(position)) {
                itemView.removeView(getSettingsView());
                mShowItems.put(position, false);
            } else {
                itemView.addView(getSettingsView());
                mShowItems.put(position, true);
            }
        }

        private View getSettingsView() {
            if (settingsView == null) {
                settingsView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_create_alarm, null);
            }
            return settingsView;
        }
    };


}
