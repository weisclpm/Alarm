package com.example.weisc.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.weisc.alarm.util.Constant;


public class AlarmBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            //处理开机
            Log.d("Alarm", "onReceive: 开机");
        }
        if (Constant.INTENT_ALARM_BROADCAST_ACTION.equals(action)) {
            Intent service=new Intent(intent);
            service.setAction(null);
            service.setClass(context,AlarmService.class);
            context.startService(service);
        }

    }
}
