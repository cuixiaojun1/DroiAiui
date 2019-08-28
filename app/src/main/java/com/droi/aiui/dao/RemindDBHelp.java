package com.droi.aiui.dao;

import android.content.ContentValues;
import android.content.Context;

import com.droi.aiui.bean.RemindInfo;

import java.util.List;

/**
 * Created by cuixiaojun on 18-1-17.
 */

public class RemindDBHelp {

    private final String TAG = "RemindDBHelp";
    private static RemindDBHelp dbHelp;
    private DBManager dbManager;
    private static final String DB_NAME="reminds.db";
    private static final int DB_VERSION = 8;

    private RemindDBHelp(Context context) {
        dbManager = new DBManager(context, DB_NAME, DB_VERSION, RemindInfo.class);
    }

    public static RemindDBHelp getInstance(Context context)
    {
        if (dbHelp == null)
            dbHelp = new RemindDBHelp(context);
        return dbHelp;
    }

    /**
     * ��������
     * @param info
     */
    public boolean insert(RemindInfo info){
        boolean isExist = false;

        List<RemindInfo> remindInfos = queryRemindAll();
        if (remindInfos != null && remindInfos.size() > 0) {
            for (int i = 0; i < remindInfos.size(); i++) {
                RemindInfo remindInfo = remindInfos.get(i);
                if (remindInfo.getTime() == info.getTime() && remindInfo.getRepeatDate().equals(info.getRepeatDate())) {
                    remindInfo.setContent(remindInfo.getContent()+","+info.getContent());
                    update(remindInfo);
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                long insert = dbManager.insert(info);
                info.setId(insert);
            }
        } else {
            long insert = dbManager.insert(info);
            info.setId(insert);
        }
        return isExist;
    }

    /**
     * ɾ��
     * @param
     */
    public void delete(RemindInfo info) {
        dbManager.deleteById(RemindInfo.class, info.getId());
    }

    /**
     * ��ѯ����
     */
    public List<RemindInfo> queryRemindAll() {
        List<RemindInfo> remindInfos = dbManager.findAll(RemindInfo.class);
        return remindInfos;
    }

    private void update(RemindInfo info) {
        ContentValues values = new ContentValues();
        values.put("content", info.getContent());
        dbManager.updateById(RemindInfo.class, values, info.getId());
    }

}