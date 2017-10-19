package com.example.weisc.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by weisc on 17-10-11.
 */

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra("ringtone_uri");
        Ringtone ringtone = RingtoneManager.getRingtone(context, Uri.parse(msg));
        ringtone.play();
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        Log.d("TEST", "onReceive " + ringtone.getTitle(context));

    }
}
