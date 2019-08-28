package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 18-1-10.
 */

public class AppName {
    String appName;

    public AppName() {
    }

    public AppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String toString() {
        return "AppName{" +
                "appName='" + appName + '\'' +
                '}';
    }
}