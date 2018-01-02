package com.weisc.alarm.data;

/**
 * Created by weisc on 17-10-17.
 */

public class AlarmSettings{
    private String settingsName;
    private String settingsType;



    public AlarmSettings(String settingsName, String settingsType) {
        this.settingsName = settingsName;
        this.setSettingsType(settingsType);
    }

    public String getSettingsName() {
        return settingsName;
    }

//    public void setSettingsName(String settingsName) {
//        this.settingsName = settingsName;
//    }

    public String getSettingsType() {
        return settingsType;
    }

    public void setSettingsType(String settingsType) {
        this.settingsType = settingsType;
    }

}
