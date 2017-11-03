package com.example.weisc.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.weisc.alarm.util.Constant;


public class AlarmBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Handler serviceHandler = AlarmService.getHandler();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            //处理开机
        }
        if (action.equals(Constant.INTENT_ALARM_BROADCAST_ACTION)) {
            int opt = intent.getIntExtra(Constant.INTENT_ALARM_OPT, -1);
            Log.d("ALARM", "onReceive: broadcast receive opt " + opt);
            switch (opt) {
                case Constant.INTENT_OPT_SET_ALARM: {
                    int alarmId = intent.getIntExtra(Constant.INTENT_ALARM_ID, -1);
                    Message msg = Message.obtain();
                    msg.what = Constant.MSG_WHAT_HANDLE_ALARM;
                    msg.arg1 = alarmId;
                    serviceHandler.sendMessage(msg);
                    break;
                }
                case Constant.INTENT_OPT_STOP_NOTIFY: {
                    serviceHandler.sendEmptyMessage(Constant.MSG_WHAT_STOP_NOTIFY);
                    break;
                }
                case -1:
                    Log.d("ALARM", "onReceive: 获取不到值，未知广播 ");
                    break;
            }
        }

    }
}
