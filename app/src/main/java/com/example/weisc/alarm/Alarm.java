package com.example.weisc.alarm;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by weisc on 17-10-16.
 */

public class Alarm implements Serializable {

    public static final int ONETIME = 0;
    public static final int WORKDAY = 31;
    public static final int WEEKEND = 96;
    public static final int EVERYDAY = 127;

    private static final String HOUR_SP = "HOUR";
    private static final String MINUTE_SP = "MINUTE";
    private static final String REPEAT_DATE_SP = "REPEAT_DATE";
    private static final String STATUS_SP = "STATUS";
    private static final String RINGTONE_URI_SP = "RINGTONE_URI";

    private boolean status;
    private int hour;
    private int minute;
    private int repeatDate;
    private String timeText;
    private String repeatText;
    private String alarmName;
    private String ringtone;

    public Alarm(int hour, int minute, int repeatDate, boolean status, String ringtone, String alarmName) {
        this.alarmName = alarmName == null ? UUID.randomUUID().toString() : alarmName;
        setTimeText(hour, minute);
        setRepeat(repeatDate);
        this.status = status;
        this.ringtone = ringtone;
    }


    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getTimeText() {
        return timeText;
    }

    private void setTimeText(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        String hourStr = hour < 10 ? "0" + Integer.toString(hour) : Integer.toString(hour);
        String minuteStr = minute < 10 ? "0" + Integer.toString(minute) : Integer.toString(minute);
        this.timeText = hourStr + ":" + minuteStr;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getRepeatText() {
        return repeatText;
    }

    private static final String[] date = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    public void setRepeat(int repeatDate) {
        this.repeatDate = repeatDate;
        this.repeatText = generateDateText(repeatDate);
    }

    public int getRepeatDate() {
        return repeatDate;
    }

    public static String generateDateText(int repeatDate) {
        StringBuilder sb = new StringBuilder();
        switch (repeatDate) {
            case ONETIME:
                sb.append("一次");
                break;
            case EVERYDAY:
                sb.append("每天");
                break;
            case WORKDAY:
                sb.append("工作日");
                break;
            case WEEKEND:
                sb.append("周末");
                break;
            default:
                int[] repeatDay = parseRepeatDate(repeatDate);
                for (int n : repeatDay) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(date[n]);
                }
                break;
        }
        return sb.toString();
    }

    public boolean isOnetime() {
        return repeatDate == ONETIME;
    }

    public static int[] parseRepeatDate(int repeatDate) {
        int repeat_copy = repeatDate;
        int n = 0, index = 0;
        int[] repeatDay = new int[7];
        while (repeat_copy != 0 && n < 7) {
            if ((repeat_copy & 0x01) != 0) {//说明这一bit被选中
                repeatDay[index++] = n;
            }
            repeat_copy >>= 1;
            n++;
        }
        return Arrays.copyOf(repeatDay, index);
    }

    public static void saveToSP(Context context, Alarm alarm) {
        SharedPreferences sp = context.getSharedPreferences(alarm.alarmName, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(HOUR_SP, alarm.hour);
        editor.putInt(MINUTE_SP, alarm.minute);
        editor.putInt(REPEAT_DATE_SP, alarm.repeatDate);
        editor.putBoolean(STATUS_SP, alarm.status);
        if (alarm.ringtone != null)
            editor.putString(RINGTONE_URI_SP, alarm.ringtone.toString());
        editor.commit();
    }

    public static void deleteFromSP(Context context, Alarm alarm) {
        SharedPreferences sp = context.getSharedPreferences(alarm.alarmName, context.MODE_PRIVATE);
        sp.edit().clear().commit();

        SharedPreferences sp1 = context.getSharedPreferences("sp_alarm", context.MODE_PRIVATE);
        sp1.edit().remove(alarm.getAlarmName()).commit();
    }

    public static Alarm loadFromSP(Context context, String alarmName) {
        SharedPreferences sp = context.getSharedPreferences(alarmName, context.MODE_PRIVATE);
        int hour = sp.getInt(HOUR_SP, -1);
        int minute = sp.getInt(MINUTE_SP, -1);
        int repeatDate = sp.getInt(REPEAT_DATE_SP, -1);
        boolean status = sp.getBoolean(STATUS_SP, false);
        String ringtone = sp.getString(RINGTONE_URI_SP, null);

        if (hour == -1 || minute == -1 || repeatDate == -1) {
            sp.edit().clear().commit();
            return null;
        }

        return new Alarm(hour, minute, repeatDate, status, ringtone, alarmName);

    }
}
