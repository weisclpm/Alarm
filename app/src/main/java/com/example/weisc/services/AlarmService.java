package com.example.weisc.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.weisc.alarm.Alarm;
import com.example.weisc.alarm.AlarmAlertDialogActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmService extends Service {
    private static int alarmId = 0;
    private static final long DAY_INTERVAL = 3600 * 24 * 1000;
    private static final long WEEK_INTERVAL = 7 * DAY_INTERVAL;
    private Map<String, PendingIntent> alarms = new HashMap<>();
    private AlarmManager alarmManager;

    private AlarmServiceBinder binder = new AlarmServiceBinder();

    public class AlarmServiceBinder extends Binder {
        public void setAlarm(Alarm alarm) {
            String key = alarm.getAlarmName();
            if (alarms.containsKey(key)) {
                Log.d("TEST", "setAlarm: status change");
                setExistingAlarm(alarm);
            } else {
                setNewAlarm(alarm);

            }
        }

        public void cancelAlarm(Alarm alarm) {
            PendingIntent operator = alarms.get(alarm.getAlarmName());
            if (operator != null)
                alarmManager.cancel(operator);
        }
    }


    private void setNewAlarm(Alarm alarm) {
        int repeatDate = alarm.getRepeatDate();
        long time = calculate(alarm.getHour(), alarm.getMinute(), alarm.getRepeatDate());

        Log.d("ALARM", "响铃时间: " + ((time - System.currentTimeMillis()) / (1000 * 60)));
        Intent intent = new Intent("com.weisc.alarm");
        intent.putExtra("alarm_data", alarm);
        PendingIntent operator = PendingIntent.
                getBroadcast(AlarmService.this, alarmId++, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (repeatDate == Alarm.EVERYDAY) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, time, DAY_INTERVAL, operator);

        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, operator);
        }
        alarms.put(alarm.getAlarmName(), operator);
    }

    private void setExistingAlarm(Alarm alarm) {
        PendingIntent operator = alarms.get(alarm.getAlarmName());
        long time = calculate(alarm.getHour(), alarm.getMinute(), alarm.getRepeatDate());
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, operator);
    }

    private long calculate(int hour, int minute, int repeatDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Log.d("ALARM", "calculate: " + calendar.getTime());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Log.d("ALARM", "calculate: " + calendar.getTime());
        long dateTime = calendar.getTimeInMillis();
        int curWeek = calendar.get(Calendar.DAY_OF_WEEK);
        curWeek = ((curWeek - 1) % 7) - 1;
        if (dateTime <= System.currentTimeMillis()) {
            dateTime = dateTime + DAY_INTERVAL;
            curWeek++;
        }
        if (repeatDate == Alarm.EVERYDAY || repeatDate == Alarm.ONETIME) {
            return dateTime;
        } else {
            int[] repeatDay = Alarm.parseRepeatDate(repeatDate);
            int dayOfWeek = findNextDay(repeatDay, curWeek);
            if (dayOfWeek > curWeek) {
                dateTime = dateTime + (dayOfWeek - curWeek) * DAY_INTERVAL;
            } else if (dayOfWeek < curWeek) {
                dateTime = dateTime + (dayOfWeek - curWeek + 7) * DAY_INTERVAL;
            }
            return dateTime;
        }
    }

    private int findNextDay(int[] repeatDay, int curWeek) {
        int high = repeatDay.length - 1;
        if (curWeek > repeatDay[high]) return repeatDay[0];
        else {
            while (--high > -1 && repeatDay[high] > curWeek) ;
            if (high != -1 && repeatDay[high] == curWeek) return curWeek;
            else return repeatDay[(high + 1) % repeatDay.length];
        }
    }


    private class AlarmBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "ring!", Toast.LENGTH_SHORT).show();
            Log.d("ALARM", "onReceive: ");
//            startActivity(new Intent(context, AlarmAlertDialogActivity.class));
//            Alarm alarm = (Alarm) intent.getSerializableExtra("alarm_data");
//            String msg = alarm.getRingtone();
//            Ringtone ringtone = RingtoneManager.getRingtone(context, Uri.parse(msg));
//            ringtone.play();
//            Log.d("TEST", "onReceive " + ringtone.getTitle(context));
        }
    }

    @Override
    public void onCreate() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.weisc.alarm");
        registerReceiver(new AlarmBroadcastReceiver(), intentFilter);

//        Intent intent = new Intent("com.weisc.alarm");
//        PendingIntent operator = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operator);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
