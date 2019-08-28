package com.droi.aiui.bean;

/**
 * Created by cuixiaojun on 17-12-28.
 */

public class DynamicEntityData {
    public String resName;
    public String idName;
    public String idValue;
    public String syncData;

    public DynamicEntityData(String resName, String idName, String idValue, String syncData) {
        this.resName = resName;
        this.idName = idName;
        this.idValue = idValue;
        this.syncData = syncData;
    }
}