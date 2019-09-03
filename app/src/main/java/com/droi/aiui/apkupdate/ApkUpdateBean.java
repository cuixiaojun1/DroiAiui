package com.droi.aiui.apkupdate;

/**
 * Created by pc on 2018/2/9.
 */

public class ApkUpdateBean {


    /**
     * icon : /v1/getFile?path=/upload/clientfile/20180209093543105.png
     * isforce : false
     * description : 才艺大道二期
     * name : 才艺大道线下版本
     * md5 : 8e91fe7de0c85c1de184d1b25d35007e
     * path : /v1/getFile?path=/upload/clientfile/20180209093543105.apk
     * rt : 1
     * version : 1.0.1
     * vcode : 2
     */

    private String icon;
    private boolean isforce;
    private String description;
    private String name;
    private String md5;
    private String path;
    private int rt;
    private String version;
    private int vcode;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isIsforce() {
        return isforce;
    }

    public void setIsforce(boolean isforce) {
        this.isforce = isforce;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRt() {
        return rt;
    }

    public void setRt(int rt) {
        this.rt = rt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVcode() {
        return vcode;
    }

    public void setVcode(int vcode) {
        this.vcode = vcode;
    }
}