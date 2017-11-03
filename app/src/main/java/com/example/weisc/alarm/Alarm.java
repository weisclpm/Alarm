package com.example.weisc.alarm;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by weisc on 17-10-16.
 */

public class Alarm implements Serializable {
    public static final int ONETIME = 0;
    public static final int WORKDAY = 31;
    public static final int WEEKEND = 96;
    public static final int EVERYDAY = 127;

    private static final long DAY_INTERVAL = 3600 * 24 * 1000;
    private static final long WEEK_INTERVAL = 7 * DAY_INTERVAL;

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

    public long nextTimeInMills() {
        return calculate(hour, minute, repeatDate);
    }


    public static String timeToText(long time) {
        Calendar current = Calendar.getInstance();
        Calendar future = Calendar.getInstance();
        future.setTimeInMillis(time);

        int day = future.get(Calendar.DAY_OF_WEEK) - current.get(Calendar.DAY_OF_WEEK);
        int hour = future.get(Calendar.HOUR) - current.get(Calendar.HOUR);
        int minute = future.get(Calendar.MINUTE) - current.get(Calendar.MINUTE);
        int second = future.get(Calendar.SECOND) - current.get(Calendar.SECOND);
        if (second < 0) {
            second += 60;
            minute--;
        }
        if (minute < 0) {
            minute += 60;
            hour--;
        }
        if (hour < 0) {
            hour += 24;
            day--;
        }
        if (day < 0) {
            day += 7;
        }

        StringBuilder resultText = new StringBuilder();
        if (day > 0) resultText.append(Integer.toString(day) + "天");
        if (hour > 0) resultText.append(Integer.toString(hour) + "小时");
        if (minute > 0) resultText.append(Integer.toString(minute) + "分");
        if (second > 0) resultText.append(Integer.toString(second) + "秒");

        return resultText.toString();


    }


    private long calculate(int hour, int minute, int repeatDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long dateTime = calendar.getTimeInMillis();
        int curWeek = calendar.get(Calendar.DAY_OF_WEEK);
        curWeek = ((curWeek - 1) % 7) - 1;
        if (dateTime <= System.currentTimeMillis()) {
            dateTime = dateTime + DAY_INTERVAL;
            curWeek++;
        }
        if (repeatDate == Alarm.EVERYDAY || repeatDate == Alarm.ONETIME) {
            return dateTime;
        } else {
            int[] repeatDay = Alarm.parseRepeatDate(repeatDate);
            int dayOfWeek = findNextDay(repeatDay, curWeek);
            if (dayOfWeek > curWeek) {
                dateTime = dateTime + (dayOfWeek - curWeek) * DAY_INTERVAL;
            } else if (dayOfWeek < curWeek) {
                dateTime = dateTime + (dayOfWeek - curWeek + 7) * DAY_INTERVAL;
            }
            return dateTime;
        }
    }

    private int findNextDay(int[] repeatDay, int curWeek) {
        int high = repeatDay.length - 1;
        if (curWeek > repeatDay[high]) return repeatDay[0];
        else {
            while (--high > -1 && repeatDay[high] > curWeek) ;
            if (high != -1 && repeatDay[high] == curWeek) return curWeek;
            else return repeatDay[(high + 1) % repeatDay.length];
        }
    }
}
