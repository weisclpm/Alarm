package com.weisc.alarm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MyBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }
        super.onCreate(savedInstanceState);
    }
}
