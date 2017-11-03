package com.example.weisc.alarm.util;

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

}
