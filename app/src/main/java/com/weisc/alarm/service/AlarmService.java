package com.weisc.alarm.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.weisc.alarm.data.Alarm;
import com.weisc.alarm.data.AlarmDao;
import com.weisc.alarm.util.AlarmUtil;
import com.weisc.alarm.util.Constant;

public class AlarmService extends Service {
    private AlarmManager alarmManager;

    private AlarmServiceBinder binder = new AlarmServiceBinder();

    private Ringtone ringtone;

    private ActivityCallback callback;

    public interface ActivityCallback {
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
            PendingIntent operator = PendingIntent.getBroadcast(AlarmService.this, alarm.getId(),
                    new Intent(Constant.INTENT_ALARM_BROADCAST_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
            Log.d("ALARM", "cancelAlarm: 关闭闹钟");
            alarmManager.cancel(operator);
        }

        public void setActivityCallBack(ActivityCallback ck) {
            callback = ck;
        }
    }

    @Override
    public void onCreate() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int opt = intent.getIntExtra(Constant.INTENT_ALARM_OPT, -1);
        Log.d("ALARM", "onStartCommand: service opt " + opt);
        switch (opt) {
            case Constant.INTENT_OPT_SET_ALARM: {
                int alarmId = intent.getIntExtra(Constant.INTENT_ALARM_ID, -1);
                handleAlarm(alarmId);
                break;
            }
            case Constant.INTENT_OPT_STOP_NOTIFY: {
                stopNotify();
                break;
            }
            case -1:
                Log.d("ALARM", "onStartCommand: undefined service operation ");
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void setAlarmInService(Alarm alarm) {
        long time = AlarmUtil.nextTimeInMills(alarm);
        Intent intent = new Intent(Constant.INTENT_ALARM_BROADCAST_ACTION);
        intent.putExtra(Constant.INTENT_ALARM_ID, alarm.getId());
        intent.putExtra(Constant.INTENT_ALARM_OPT, Constant.INTENT_OPT_SET_ALARM);
        PendingIntent operation = PendingIntent.
                getBroadcast(this, alarm.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager.AlarmClockInfo info =
                new AlarmManager.AlarmClockInfo(time, null);
        alarmManager.setAlarmClock(info, operation);
        Log.d("ALARM", "setAlarmInService: 设定闹钟 " + AlarmUtil.timeToText(time));
    }

    private void handleAlarm(int alarmId) {

        Alarm alarm = AlarmDao.loadAlarm(this, alarmId);
        String ringtoneStr = alarm.getRingtone();
        Uri ringtoneUri = ringtoneStr == null ? null : Uri.parse(ringtoneStr);
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        ringtone.play();

        sendNotification();
        if (!AlarmUtil.isOnetime(alarm)) {
            setAlarmInService(alarm);
        } else {
            callback.setAlarmSwitchOff(alarm);
        }


    }

    private void sendNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(this, 1, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("闹钟")
                .setContentText("时间到了！")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setAutoCancel(false).setFullScreenIntent(pendingIntent, true);

        //停止动作
        Intent intent = new Intent(Constant.INTENT_ALARM_BROADCAST_ACTION);
        intent.putExtra(Constant.INTENT_ALARM_OPT, Constant.INTENT_OPT_STOP_NOTIFY);
        PendingIntent dismissOperation = PendingIntent.getBroadcast
                (this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        Notification.Action.Builder dismiss =
                new Notification.Action.Builder(android.R.drawable.ic_lock_idle_alarm, "停止"
                        , dismissOperation);
        builder.addAction(dismiss.build());


        Notification notification = builder.build();

        notificationManager.notify(0, notification);

    }

    private void stopNotify() {
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(0);
    }

//    private class AlarmServiceHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            int what = msg.what;
//            switch (what) {
//                case Constant.MSG_WHAT_HANDLE_ALARM:
//                    int alarmId = msg.arg1;
//                    handleAlarm(alarmId);
//                    break;
//
//                case Constant.MSG_WHAT_STOP_NOTIFY:
//                    stopNotify();
//                    break;
//            }
//        }
//    }

}
