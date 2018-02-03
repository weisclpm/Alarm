package com.weisc.alarm.wiget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.weisc.alarm.R;
import com.weisc.alarm.data.AlarmSettings;
import com.weisc.alarm.util.AlarmUtil;
import com.weisc.alarm.wiget.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SetAlarmActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    public static final String INTENT_ALARM_DATA_HOUR = "INTENT_ALARM_DATA_HOUR";
    public static final String INTENT_ALARM_DATA_REPEAT = "INTENT_ALARM_DATA_REPEAT";
    public static final String INTENT_ALARM_DATA_MINUTE = "INTENT_ALARM_DATA_MINUTE";
    public static final String INTENT_ALARM_DATA_RINGTONE = "INTENT_ALARM_DATA_RINGTONE";

    private static final int REPEAT_SETTINGS_RES = 1001;
    private static final int RING_SETTINGS_RES = 1002;

    private TimePicker timePicker;
    private ListView listView;
    private AlarmSettingsAdapter adapter;
    private int repeatDate;
    private Uri ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);


        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        listView = (ListView) findViewById(R.id.timeSettingList);
        adapter = new AlarmSettingsAdapter(this, R.layout.alarm_settings_item,
                initAlarmSettings());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    private List<AlarmSettings> initAlarmSettings() {
        List<AlarmSettings> list = new ArrayList<>();
        list.add(new AlarmSettings("重复", AlarmUtil.generateDateText(AlarmUtil.ONETIME)));
        ringtone = RingtoneManager.
                getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        list.add(new AlarmSettings("铃声", updateRingtoneName(this, ringtone).toString()));
        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        switch (position) {
            case 0: {
                Intent intent = new Intent(this, RepeatSettingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(RepeatSettingsActivity.REPEAT_DATE, repeatDate);
                intent.putExtras(bundle);
                startActivityForResult(intent, REPEAT_SETTINGS_RES);
                break;
            }
            case 1: {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtone);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置通闹钟铃声");
                startActivityForResult(intent, RING_SETTINGS_RES);
                break;
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuConfirm: {
                Intent intent = new Intent();
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String ringtoneStr = ringtone == null ? null : ringtone.toString();
                intent.putExtra(INTENT_ALARM_DATA_HOUR, hour);
                intent.putExtra(INTENT_ALARM_DATA_MINUTE, minute);
                intent.putExtra(INTENT_ALARM_DATA_REPEAT, repeatDate);
                intent.putExtra(INTENT_ALARM_DATA_RINGTONE, ringtoneStr);
                setResult(RESULT_OK, intent);
                finish();

                break;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REPEAT_SETTINGS_RES:
                if (resultCode == RESULT_OK) {
                    repeatDate = data.getIntExtra(RepeatSettingsActivity.REPEAT_DATE, 0);
                    String repeatDateText = AlarmUtil.generateDateText(repeatDate);
                    AlarmSettings settings = adapter.getItem(0);
                    settings.setSettingsType(repeatDateText);
                    adapter.notifyDataSetChanged();
                }
                break;
            case RING_SETTINGS_RES:
                if (resultCode == RESULT_OK) {
                    ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    AlarmSettings settings = adapter.getItem(1);
                    settings.setSettingsType(updateRingtoneName(this, ringtone).toString());
                    adapter.notifyDataSetChanged();
                }
        }
    }

    private class AlarmSettingsAdapter extends ArrayAdapter<AlarmSettings> {
        int mResource;

        public AlarmSettingsAdapter(@NonNull Context context, @LayoutRes int resource,
                                    @NonNull List<AlarmSettings> objects) {
            super(context, resource, objects);
            this.mResource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            AlarmSettings alarmSettings = getItem(position);
            AlarmSettingsViewHolder viewHolder;
            View view;
            if (convertView == null) {
                view = getLayoutInflater().from(getContext()).inflate(mResource, parent, false);
                viewHolder = new AlarmSettingsViewHolder();
                viewHolder.alarmSettingsName = view.findViewById(R.id.alarmSettingsName);
                viewHolder.alarmSettingsType = view.findViewById(R.id.alarmSettingsType);
                view.setTag(viewHolder);

            } else {
                view = convertView;
                viewHolder = (AlarmSettingsViewHolder) view.getTag();
            }
            viewHolder.alarmSettingsName.setText(alarmSettings.getSettingsName());
            viewHolder.alarmSettingsType.setText(alarmSettings.getSettingsType());

            return view;
        }


        class AlarmSettingsViewHolder {
            TextView alarmSettingsName;
            TextView alarmSettingsType;
        }
    }

    //获取铃声的名称
    private static CharSequence updateRingtoneName(Context context, Uri ringtoneUri) {
        if (context == null) {
            return null;
        }
        CharSequence summary = "";
        // Is it a silent ringtone?
        if (ringtoneUri == null) {
            summary = "静音";
        } else {
            Cursor cursor = null;
            try {
                if (MediaStore.AUTHORITY.equals(ringtoneUri.getAuthority())) {
                    // Fetch the ringtone title from the media provider
                    cursor = context.getContentResolver().query(ringtoneUri,
                            new String[]{MediaStore.Audio.Media.TITLE}, null, null, null);
                } else if (ContentResolver.SCHEME_CONTENT.equals(ringtoneUri.getScheme())) {
                    cursor = context.getContentResolver().query(ringtoneUri,
                            new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        summary = cursor.getString(0);
                    }
                }
            } catch (IllegalArgumentException iae) {
                // Some other error retrieving the column from the provider
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return summary;
    }

}
