package com.droi.aiui.bean;

import java.io.Serializable;


/**
 * Created by cuixiaojun on 18-1-13.
 */
public class RemindInfo implements Serializable {

    private long id;
    private long time;
    private String content;
    private String repeatDate;

    public RemindInfo() {
    }

    public RemindInfo(long time, String content, String repeatDate) {
        this.time = time;
        this.content = content;
        this.repeatDate = repeatDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRepeatDate() {
        return repeatDate;
    }

    public void setRepeatDate(String repeatDate) {
        this.repeatDate = repeatDate;
    }

    @Override
    public String toString() {
        return "RemindInfo{" +
                "id=" + id +
                ", time=" + time +
                ", content='" + content + '\'' +
                ", repeatDate='" + repeatDate + '\'' +
                '}';
    }
}