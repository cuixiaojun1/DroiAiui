package com.droi.aiui.bean;

/**
 * Created by hejianfeng on 2017/12/14.
 */

public class Message {

    public enum MsgType {
        TEXT, VOICE, IMAGE
    }

    public enum FromType {
        USER, ROBOT
    }
    public FromType fromType;
    public MsgType msgType;
    private String text;

    public Message() {

    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isText(){
        return msgType == MsgType.TEXT;
    }
    public boolean isVoice(){
        return msgType == MsgType.VOICE;
    }
    public boolean isImage(){
        return msgType == MsgType.IMAGE;
    }

    public boolean isFromUser(){
        return fromType == FromType.USER;
    }

    public boolean isFromRobot(){
        return fromType == FromType.ROBOT;
    }
}