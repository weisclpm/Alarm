package com.example.weisc.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.weisc.alarm.Alarm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmService extends Service {
    private static final String INTENT_ALARM_ACTION = "com.weisc.alarm";
    private static final String INTENT_ALARM_DATA = "alarm_data";
    private static final String INTENT_ALARM_OPT = "alarm_opt";
    private static final int INTENT_OPT_SET_ALARM = 0;
    private static final int INTENT_OPT_STOP_NOTIFY = 1;
    private static final long DAY_INTERVAL = 3600 * 24 * 1000;
    private static final long WEEK_INTERVAL = 7 * DAY_INTERVAL;
    //    private Map<String, PendingIntent> alarms = new HashMap<>();
    private AlarmManager alarmManager;

    private AlarmServiceBinder binder = new AlarmServiceBinder();
    private ActivityCallBack callback;

    private Ringtone ringtone;

    public interface ActivityCallBack {
        void setAlarmSwitchOff(Alarm alarm);
    }

    public class AlarmServiceBinder extends Binder {
        public void setAlarm(Alarm alarm, boolean flag) {
            if (flag) {
                setAlarmInService(alarm);
            } else {
                cancelAlarm(alarm);
            }
        }

        public void cancelAlarm(Alarm alarm) {
            PendingIntent operator = PendingIntent.getBroadcast(AlarmService.this, alarm.alarm_id,
                    new Intent(INTENT_ALARM_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d("ALARM", "cancelAlarm: 关闭闹钟");
            alarmManager.cancel(operator);
        }

        public void setActivityCallBack(ActivityCallBack ck) {
            callback = ck;
        }
    }

    @Override
    public void onCreate() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ALARM_ACTION);
        registerReceiver(new AlarmBroadcastReceiver(), intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private void setAlarmInService(Alarm alarm) {
        long time = calculate(alarm.getHour(), alarm.getMinute(), alarm.getRepeatDate());

//        PendingIntent operation = alarms.get(alarm.getAlarmName());
        sendNotification();
        Intent intent = new Intent(INTENT_ALARM_ACTION);
        intent.putExtra(INTENT_ALARM_DATA, alarm);
        intent.putExtra(INTENT_ALARM_OPT, INTENT_OPT_SET_ALARM);
        Toast.makeText(this, "alarm_id:" + alarm.alarm_id, Toast.LENGTH_SHORT).show();
        PendingIntent operation = PendingIntent.
                getBroadcast(this, alarm.alarm_id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//            alarms.put(alarm.getAlarmName(), operation);
        Toast.makeText(this, "闹钟将在" + timeToText(time) + "之后响起", Toast.LENGTH_LONG).show();
        AlarmManager.AlarmClockInfo info =
                new AlarmManager.AlarmClockInfo(time, null);
        alarmManager.setAlarmClock(info, operation);
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, operation);
        Log.d("ALARM", "setAlarmInService: 设定闹钟" + intent.getIntExtra(INTENT_ALARM_OPT, -1));
    }

    private String timeToText(long time) {
        Calendar current = Calendar.getInstance();
        Calendar future = Calendar.getInstance();
        future.setTimeInMillis(time);

        int day = future.get(Calendar.DAY_OF_WEEK) - current.get(Calendar.DAY_OF_WEEK);
        int hour = future.get(Calendar.HOUR) - current.get(Calendar.HOUR);
        int minute = future.get(Calendar.MINUTE) - current.get(Calendar.MINUTE);
        int second = future.get(Calendar.SECOND) - current.get(Calendar.SECOND);
        if (second < 0) {
            second += 60;
            minute--;
        }
        if (minute < 0) {
            minute += 60;
            hour--;
        }
        if (hour < 0) {
            hour += 24;
            day--;
        }
        if (day < 0) {
            day += 7;
        }

        StringBuilder resultText = new StringBuilder();
        if (day > 0) resultText.append(Integer.toString(day) + "天");
        if (hour > 0) resultText.append(Integer.toString(hour) + "小时");
        if (minute > 0) resultText.append(Integer.toString(minute) + "分");
        if (second > 0) resultText.append(Integer.toString(second) + "秒");

        return resultText.toString();


    }

//    private void setExistingAlarm(Alarm alarm) {
//        PendingIntent operator = alarms.get(alarm.getAlarmName());
//        long time = calculate(alarm.getHour(), alarm.getMinute(), alarm.getRepeatDate());
//        alarmManager.set(AlarmManager.RTC_WAKEUP, time, operator);
//    }

    private long calculate(int hour, int minute, int repeatDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
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

    private void handleAlarm(Alarm alarm) {

        Uri ringtoneUri = Uri.parse(alarm.getRingtone());
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
//        MediaPlayer mediaPlayer=new MediaPlayer();
//        mediaPlayer.setDataSource(this,);
//        ringtone.play();
        Log.d("ALARM", "handleAlarm: isPlaying? " + ringtone.isPlaying());

        sendNotification();
        if (!alarm.isOnetime()) {
            setAlarmInService(alarm);
        } else {
            callback.setAlarmSwitchOff(alarm);
        }


    }


    private void sendNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(INTENT_ALARM_ACTION);
        intent.putExtra(INTENT_ALARM_OPT, INTENT_OPT_STOP_NOTIFY);
        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("闹钟")
                .setContentText("时间到了！")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setAutoCancel(false)
                .setFullScreenIntent(pendingIntent, true);

        Notification.Action.Builder dismiss =
                new Notification.Action.Builder(android.R.drawable.ic_lock_idle_alarm, "停止"
                        , pendingIntent);
        builder.addAction(dismiss.build());


        Notification notification = builder.build();

        notificationManager.notify(0, notification);

    }

    private void stopNotify() {
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(0);
        Log.d("ALARM", "stopNotify: cancel");
    }


    private class AlarmBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int opt = intent.getIntExtra(INTENT_ALARM_OPT, -1);

            Log.d("ALARM", "onReceive: broadcast receive " + opt + " context Type: " + context.toString());
            switch (opt) {
                case INTENT_OPT_SET_ALARM: {
                    Alarm alarm = (Alarm) intent.getSerializableExtra(INTENT_ALARM_DATA);
                    Toast.makeText(context, "alarm=" + (alarm == null), Toast.LENGTH_LONG).show();
                    if (alarm != null) {
                        handleAlarm(alarm);
                    }
                    break;
                }
                case INTENT_OPT_STOP_NOTIFY: {
                    stopNotify();
                    break;
                }
                case -1:
                    Toast.makeText(context, "获取不到值，未知广播", Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }


}
