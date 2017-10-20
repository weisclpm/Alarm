package com.example.weisc.alarm;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AlarmAlertDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_alert_dialog);
        showAlertDialog();

    }

    private void showAlertDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("闹钟响了").setPositiveButton("确定", null).create();

        dialog.show();
    }
}

