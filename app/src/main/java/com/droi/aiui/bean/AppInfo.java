package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 18-1-10.
 */

public class AppInfo {
    private String appName;
    private String packageName;
    private String className;

    public AppInfo() {
    }

    public AppInfo(String appName, String packageName, String className) {
        this.appName = appName;
        this.packageName = packageName;
        this.className = className;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }

}