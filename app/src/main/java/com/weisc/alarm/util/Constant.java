package com.weisc.alarm.util;

/**
 * Created by weisc on 17-11-3.
 */

public interface Constant {
    int INTENT_OPT_SET_ALARM = 0;
    int INTENT_OPT_STOP_NOTIFY = 1;
    String INTENT_ALARM_BROADCAST_ACTION = "com.weisc.alarm";
    String INTENT_ALARM_ID = "alarm_data";
    String INTENT_ALARM_OPT = "alarm_opt";

    int MSG_WHAT_HANDLE_ALARM = 11;
    int MSG_WHAT_STOP_NOTIFY = 12;


    String INTENT_ALARM_DATA_HOUR = "INTENT_ALARM_DATA_HOUR";
    String INTENT_ALARM_DATA_REPEAT = "INTENT_ALARM_DATA_REPEAT";
    String INTENT_ALARM_DATA_MINUTE = "INTENT_ALARM_DATA_MINUTE";
    String INTENT_ALARM_DATA_RINGTONE = "INTENT_ALARM_DATA_RINGTONE";
    int REPEAT_SETTINGS_RES = 1001;
    int RING_SETTINGS_RES = 1002;

}
