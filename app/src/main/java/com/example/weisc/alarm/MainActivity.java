package com.example.weisc.alarm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weisc.services.AlarmService;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CREATE_ALARM = 1;
    private static final String ALARM_DATA = "ALARM_DATA";

    private boolean isExit;
    private List<Alarm> alarmList = new LinkedList<>();
    private ListView alarmListView;
    private AlarmAdapter adapter;

    private ImageButton addAlarm;

    private AlarmService.AlarmServiceBinder alarmServiceBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAlarmService();
        initAlarmList();
        alarmListView = (ListView) findViewById(R.id.alarmList);
        adapter = new AlarmAdapter(this, R.layout.alarm_item, alarmList);
        alarmListView.setAdapter(adapter);
        registerForContextMenu(alarmListView);
        addAlarm = (ImageButton) findViewById(R.id.addAlarm);
        addAlarm.setOnClickListener(this);
    }

    private void initAlarmService() {
        Intent service = new Intent(this, AlarmService.class);
        startService(service);
        ServiceConnection conn = new AlarmServiceConnection();
        bindService(service, conn, BIND_AUTO_CREATE);
    }

    private void initAlarmList() {
        Log.d("ALARM", "initAlarmList");
        SharedPreferences sp = getSharedPreferences("sp_alarm", MODE_PRIVATE);
        Map<String, ?> maps = sp.getAll();
        if (maps.size() == 0) {
            return;
        }
        Set<String> idSet = maps.keySet();
        Iterator<String> i = idSet.iterator();
        while (i.hasNext()) {
            String alarmName = i.next();
            Alarm alarm = Alarm.loadFromSP(this, alarmName);
            if (alarm != null) {
                alarmList.add(alarm);
            }
        }

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
        Alarm.deleteFromSP(this, alarm);
    }

    private void changeAlarmStatus(Alarm alarm, boolean status) {
        alarm.setStatus(status);
        Alarm.saveToSP(this, alarm);
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
                    Alarm alarm = (Alarm) data.getSerializableExtra(ALARM_DATA);
                    adapter.add(alarm);
                    alarmServiceBinder.setAlarm(alarm, alarm.isStatus());
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }


    private class AlarmServiceConnection implements ServiceConnection, AlarmService.ActivityCallBack {
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
        public void setAlarmSwitch(Alarm alarm) {
            Log.d("ALARM", "setAlarmSwitch: ");
            int size = adapter.getCount();
            for (int i = 0; i < size; i++) {
                Alarm item = adapter.getItem(i);
                if (item.getAlarmName().equals(alarm.getAlarmName())) {
                    Log.d("ALARM", "setAlarmSwitch: change");
                    changeAlarmStatus(alarm, false);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }


    private class AlarmAdapter extends ArrayAdapter<Alarm> {
        int resource;
        Context context;

        public AlarmAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Alarm> objects) {
            super(context, resource, objects);
            this.resource = resource;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Alarm alarm = getItem(position);
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = getLayoutInflater().from(getContext()).inflate(resource, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.alarmName = view.findViewById(R.id.alarmName);
                viewHolder.aSwitch = view.findViewById(R.id.alarmSwitch);
                viewHolder.repeatDate = view.findViewById(R.id.alarmRepeat);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.alarmName.setText(alarm.getTimeText());
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
            TextView alarmName;
            Switch aSwitch;
            TextView repeatDate;
        }
    }


}
