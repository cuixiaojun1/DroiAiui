package com.droi.aiui.bean;

import org.json.JSONObject;

/**
 * Created by cuixiaojun on 17-12-20.
 */

public class SemanticResult {
    //������
    private int rc;
    //����
    private String service;
    //���ص��ı�
    private String answerText;
    //���ص�����
    private JSONObject data;
    //���ص�����
    private JSONObject semantic;

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

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String text) {
        this.answerText = text;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONObject getSemantic() {
        return semantic;
    }

    public void setSemantic(JSONObject semantic) {
        this.semantic = semantic;
    }

    @Override
    public String toString() {
        return "SemanticResult{" +
                "rc=" + rc +
                ", service='" + service + '\'' +
                ", answerText='" + answerText + '\'' +
                ", data=" + data +
                ", semantic=" + semantic +
                '}';
    }
}