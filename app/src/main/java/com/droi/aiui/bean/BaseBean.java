package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 17-12-18.
 *  ���࣬��Ҫ���ڴ�ż�������
 */

public class BaseBean {

    //��������
    private String service;
    //������
    private int rc;
    //uuid
    private String uuid;
    //sid
    private String sid;
    //��д�ı�
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