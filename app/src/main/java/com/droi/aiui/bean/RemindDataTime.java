package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 18-1-18.
 */

public class RemindDataTime {

    //���
    private String year;
    //����
    private String day;
    //ʱ��
    private String time;
    //����
    private String week;
    //���绹������
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