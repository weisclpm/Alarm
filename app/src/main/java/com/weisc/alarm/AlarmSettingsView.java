package com.weisc.alarm;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.weisc.alarm.data.AlarmSettings;
import com.weisc.alarm.util.AlarmUtil;
import com.weisc.alarm.util.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * File Description:
 * Author:weisc
 * Create Date:18-2-2
 * Change List:
 */

public class AlarmSettingsView implements AdapterView.OnItemClickListener {

    private View mView;
    private Context mContext;
    private TimePicker timePicker;
    private ListView listView;
    private AlarmSettingsAdapter adapter;
    private int repeatDate;
    private Uri ringtone;


    public AlarmSettingsView(Context context) {
        mView = LayoutInflater.from(context).inflate(R.layout.activity_create_alarm, null);
        mContext = context;
        timePicker = mView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        listView = mView.findViewById(R.id.timeSettingList);
        adapter = new AlarmSettingsAdapter(mContext, R.layout.alarm_settings_item,
                initAlarmSettings());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public View getView() {
        return mView;
    }

    private List<AlarmSettings> initAlarmSettings() {
        List<AlarmSettings> list = new ArrayList<>();
        list.add(new AlarmSettings("重复", AlarmUtil.generateDateText(AlarmUtil.ONETIME)));
        ringtone = RingtoneManager.
                getActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_ALARM);
        list.add(new AlarmSettings("铃声", AlarmUtil.updateRingtoneName(mContext, ringtone).toString()));
        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0: {
                Intent intent = new Intent(mContext, RepeatSettingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(RepeatSettingsActivity.REPEAT_DATE, repeatDate);
                intent.putExtras(bundle);
                ((Activity) mContext).startActivityForResult(intent, Constant.REPEAT_SETTINGS_RES);
                break;
            }
            case 1: {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtone);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置通闹钟铃声");
                ((Activity) mContext).startActivityForResult(intent, Constant.RING_SETTINGS_RES);
                break;
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
            com.weisc.alarm.data.AlarmSettings alarmSettings = getItem(position);
            AlarmSettingsAdapter.AlarmSettingsViewHolder viewHolder;
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
                viewHolder = new AlarmSettingsAdapter.AlarmSettingsViewHolder();
                viewHolder.alarmSettingsName = view.findViewById(R.id.alarmSettingsName);
                viewHolder.alarmSettingsType = view.findViewById(R.id.alarmSettingsType);
                view.setTag(viewHolder);

            } else {
                view = convertView;
                viewHolder = (AlarmSettingsAdapter.AlarmSettingsViewHolder) view.getTag();
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

}
