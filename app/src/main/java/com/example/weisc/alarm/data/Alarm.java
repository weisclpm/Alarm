package com.example.weisc.alarm.data;


import com.example.weisc.alarm.util.AlarmUtil;

import java.io.Serializable;

/**
 * Created by weisc on 17-10-16.
 */

public class Alarm implements Serializable {
    private boolean status;
    private int hour;
    private int minute;
    private int repeatDate;
    private String timeText;
    private String repeatText;
    private int id;
    private String ringtone;

    public int alarm_id;

    public Alarm(int id, int hour, int minute, int repeatDate, boolean status, String ringtone) {
        this.id = id;
        setTimeText(hour, minute);
        setRepeat(repeatDate);
        this.status = status;
        this.ringtone = ringtone;
        this.alarm_id = hashCode();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setRepeat(int repeatDate) {
        this.repeatDate = repeatDate;
        this.repeatText = AlarmUtil.generateDateText(repeatDate);
    }

    public int getRepeatDate() {
        return repeatDate;
    }

}
