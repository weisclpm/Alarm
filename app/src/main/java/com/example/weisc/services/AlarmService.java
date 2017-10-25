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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.weisc.alarm.Alarm;

public class AlarmService extends Service {
    private static final String INTENT_ALARM_ACTION = "com.weisc.alarm";
    private static final String INTENT_ALARM_DATA = "alarm_data";
    private static final String INTENT_ALARM_OPT = "alarm_opt";
    private static final int INTENT_OPT_SET_ALARM = 0;
    private static final int INTENT_OPT_STOP_NOTIFY = 1;
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
//        long time = calculate(alarm.getHour(), alarm.getMinute(), alarm.getRepeatDate());
        long time = alarm.nextTimeInMills();
        Intent intent = new Intent(INTENT_ALARM_ACTION);
        intent.putExtra(INTENT_ALARM_DATA, alarm);
        intent.putExtra(INTENT_ALARM_OPT, INTENT_OPT_SET_ALARM);
        PendingIntent operation = PendingIntent.
                getBroadcast(this, alarm.alarm_id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager.AlarmClockInfo info =
                new AlarmManager.AlarmClockInfo(time, null);
        alarmManager.setAlarmClock(info, operation);
        Log.d("ALARM", "setAlarmInService: 设定闹钟 " + Alarm.timeToText(time));
    }

    private void handleAlarm(Alarm alarm) {

        Uri ringtoneUri = Uri.parse(alarm.getRingtone());
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        ringtone.play();

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
    }


    private class AlarmBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int opt = intent.getIntExtra(INTENT_ALARM_OPT, -1);
            Log.d("ALARM", "onReceive: broadcast receive opt " + opt);
            switch (opt) {
                case INTENT_OPT_SET_ALARM: {
                    Alarm alarm = (Alarm) intent.getSerializableExtra(INTENT_ALARM_DATA);
                    handleAlarm(alarm);
                    if (alarm != null) {
                    }
                    break;
                }
                case INTENT_OPT_STOP_NOTIFY: {
                    stopNotify();
                    break;
                }
                case -1:
                    Log.d("ALARM", "onReceive: 获取不到值，未知广播 ");
                    break;
            }

        }
    }


}
