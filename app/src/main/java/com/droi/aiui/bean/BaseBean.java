package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 17-12-18.
 *  基类，主要用于存放技能类型
 */

public class BaseBean {

    //服务类型
    private String service;
    //返回码
    private int rc;
    //uuid
    private String uuid;
    //sid
    private String sid;
    //听写文本
    private String text;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRc() {
        return rc;
    }

    public void setRc(int rc) {
        this.rc = rc;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return "BaseBean{" +
                "service='" + service + '\'' +
                ", rc=" + rc +
                ", uuid='" + uuid + '\'' +
                ", sid='" + sid + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}