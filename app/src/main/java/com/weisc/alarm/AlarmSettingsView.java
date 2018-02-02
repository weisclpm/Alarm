package com.weisc.alarm;

import android.net.Uri;
import android.widget.ListView;
import android.widget.TimePicker;

/**
 * File Description:
 * Author:weisc
 * Create Date:18-2-2
 * Change List:
 */

public class AlarmSettings {

    private TimePicker timePicker;
    private ListView listView;
    private SetAlarmActivity.AlarmSettingsAdapter adapter;
    private int repeatDate;
    private Uri ringtone;
}
