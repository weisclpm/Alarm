package com.example.weisc.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.weisc.alarm.Alarm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmService extends Service {

    private Map<String, PendingIntent> alarms = new HashMap<>();
    private AlarmManager alarmManager;

    private AlarmServiceBinder binder = new AlarmServiceBinder();

    public class AlarmServiceBinder extends Binder {
        public void setAlarm(Alarm alarm) {
            String key = alarm.getAlarmName();
            if (alarms.containsKey(key)) {
//                Intent intent = alarms.get(key);
                Log.d("TEST", "setAlarm: status change");
            } else {
                Intent intent = new Intent("com.weisc.alarm");
                intent.putExtra("ringtone_uri", alarm.getRingtone());
                PendingIntent operator = PendingIntent.
                        getBroadcast(AlarmService.this, 0, intent, 0);
                int interval = calculate(alarm.getHour(), alarm.getMinute(), alarm.getRepeatDate());
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, operator);
                Log.d("TEST", "setAlarm: set alarm");
                alarms.put(key, operator);
            }
        }
    }

    private int calculate(int hour, int minute, int repeatDate) {
        Calendar calendar=Calendar.getInstance();
//        calendar.
        return 0;
    }

    @Override
    public void onCreate() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
