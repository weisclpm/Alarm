package com.example.weisc.alarm.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weisc.alarm.Alarm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by weisc on 17-11-3.
 */

public class AlarmDao {
    private static final String PREFS = "sp_alarm";
    private static final String SP_NEXT_ID = "next_id";
    private static final String SP_ALARMS_IDS = "ALARM_IDS";
    private static final String SP_HOUR = "HOUR";
    private static final String SP_MINUTE = "MINUTE";
    private static final String SP_REPEAT_DATE = "REPEAT_DATE";
    private static final String SP_STATUS = "STATUS";
    private static final String SP_RINGTONE_URI = "RINGTONE_URI";

    private static SharedPreferences sp;


    public static void saveAlarm(Context context, Alarm alarm) {
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        int id = alarm.getId();
        if (id == -1) {
            //generate a new id
            id = sp.getInt(SP_NEXT_ID, 0);
            editor.putInt(SP_NEXT_ID, id + 1);
            alarm.setId(id);

            Set<String> ids = getAlarmIds(context);
            ids.add(String.valueOf(id));
            editor.putStringSet(SP_ALARMS_IDS, ids);
        }

        editor.putInt(SP_HOUR + id, alarm.getHour());
        editor.putInt(SP_MINUTE + id, alarm.getMinute());
        editor.putInt(SP_REPEAT_DATE + id, alarm.getRepeatDate());
        editor.putBoolean(SP_STATUS + id, alarm.isStatus());
        String ringtoneStr = alarm.getRingtone();
        if (ringtoneStr != null)
            editor.putString(SP_RINGTONE_URI + id, ringtoneStr);
        editor.apply();

    }


    public static void deleteAlarm(Context context, Alarm alarm) {
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        int id = alarm.getId();

        Set<String> ids = getAlarmIds(context);
        ids.remove(String.valueOf(id));
        if (ids.isEmpty()) {
            editor.remove(SP_NEXT_ID);
            editor.remove(SP_ALARMS_IDS);
        } else {
            editor.putStringSet(SP_ALARMS_IDS, ids);
        }
        editor.remove(SP_HOUR + id);
        editor.remove(SP_MINUTE + id);
        editor.remove(SP_REPEAT_DATE + id);
        editor.remove(SP_STATUS + id);
        editor.remove(SP_RINGTONE_URI + id);
        editor.apply();
    }

    public static Alarm loadAlarm(Context context, int id) {
        SharedPreferences sp = getSharedPreferences(context);
        int hour = sp.getInt(SP_HOUR + id, -1);
        int minute = sp.getInt(SP_MINUTE + id, -1);
        int repeatDate = sp.getInt(SP_REPEAT_DATE + id, -1);
        boolean status = sp.getBoolean(SP_STATUS + id, false);
        String ringtone = sp.getString(SP_RINGTONE_URI + id, null);

        if (hour == -1 || minute == -1 || repeatDate == -1) {
            sp.edit().clear().apply();
            return null;
        }

        return new Alarm(id, hour, minute, repeatDate, status, ringtone);
    }

    public static List<Alarm> loadAlarms(Context context) {
        List<Alarm> alarms = new ArrayList();
        Set<String> ids = getAlarmIds(context);
        for (String id : ids) {
            Alarm alarm = loadAlarm(context, Integer.parseInt(id));
            if (alarm != null)
                alarms.add(alarm);
        }
        return alarms;
    }

    private static Set<String> getAlarmIds(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getStringSet(SP_ALARMS_IDS, new HashSet<String>());
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        if (sp == null) {
            sp = context.getSharedPreferences(PREFS, context.MODE_PRIVATE);
        }
        return sp;
    }
}
