package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 18-1-18.
 */

public class RemindDataTime {

    //年份
    private String year;
    //日期
    private String day;
    //时间
    private String time;
    //星期
    private String week;
    //上午还是下午
    private String ampm;

    public RemindDataTime() {
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getAmpm() {
        return ampm;
    }

    public void setAmpm(String ampm) {
        this.ampm = ampm;
    }

    @Override
    public String toString() {
        return "RemindDataTime{" +
                "year='" + year + '\'' +
                ", day='" + day + '\'' +
                ", time='" + time + '\'' +
                ", week='" + week + '\'' +
                ", ampm='" + ampm + '\'' +
                '}';
    }
}