package com.droi.aiui.dao;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

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
     * 插入数据
     * @param info
     */
    public boolean insert(RemindInfo info){
        boolean isExist = false;

        List<RemindInfo> remindInfos = queryRemindAll();
        Log.d(TAG,"insert--->remindInfos.size  = "+remindInfos.size());
        if (remindInfos != null && remindInfos.size() > 0) {
            for (int i = 0; i < remindInfos.size(); i++) {
                RemindInfo remindInfo = remindInfos.get(i);
                if (remindInfo.getTime() == info.getTime() && remindInfo.getRepeatDate().equals(info.getRepeatDate())) {
                    remindInfo.setContent(remindInfo.getContent()+","+info.getContent());
                    Log.d(TAG,"更新提醒---->remindInfo = "+remindInfo);
                    update(remindInfo);
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                Log.d(TAG,"添加提醒---->remindInfo = "+info);
                long insert = dbManager.insert(info);
                info.setId(insert);
            }
        } else {
            Log.d(TAG,"添加提醒====>remindInfo = "+info);
            long insert = dbManager.insert(info);
            info.setId(insert);
            Log.d(TAG,"添加提醒>>>>>>>>>>insert = "+insert);
        }
        return isExist;
    }

    /**
     * 删除
     * @param
     */
    public void delete(RemindInfo info) {
        Log.d(TAG,"删除数据库闹钟："+info.toString());
        dbManager.deleteById(RemindInfo.class, info.getId());
    }

    /**
     * 查询所有
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