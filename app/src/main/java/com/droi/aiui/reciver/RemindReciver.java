package com.droi.aiui.reciver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.droi.aiui.SpeechApp;
import com.droi.aiui.apkupdate.DownloadService;
import com.droi.aiui.bean.RemindInfo;
import com.droi.aiui.controler.DataControler;
import com.droi.aiui.util.FunctionUtil;

import java.io.File;
import java.util.Calendar;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cuixiaojun on 18-1-15.
 */

public class RemindReciver extends BroadcastReceiver {

    private final String TAG = "RemindReciver";
    private static final String NETWORK_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private DataControler mDataControler;
    private SharedPreferences mSharedPreferences;
    private DownloadManager mManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(NETWORK_ACTION)) {
            mDataControler = DataControler.getInstance(context);
            if (mDataControler != null) {
                mDataControler.startLoadContact();
                mDataControler.startLoadApps();
                mDataControler.startLoadMusic();
            }
        } else if (action.equals("com.cappu.aiui.contact_changed")) {
            mDataControler = DataControler.getInstance(context);
            mDataControler.startLoadContact();
        } else if (action.equals("android.intent.action.PACKAGE_ADDED") || intent.getAction().equals("android.intent.action.PACKAGE_REPLACED") || intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {     // app
            mDataControler = DataControler.getInstance(context);
            mDataControler.startLoadApps();
        } else if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
            mSharedPreferences = context.getSharedPreferences(DownloadService.FILE_CAPPU_AIUI, MODE_PRIVATE);
            long saveApkId = mSharedPreferences.getLong("downloadId", -1L);
            if (downloadApkId == saveApkId) {
                checkDownloadStatus(context, downloadApkId);
            }
        } else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            // DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            //��ȡ������������Ids��
            //long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            ////���֪ͨ��ȡ����������
            //manager.remove(ids);
            //Toast.makeText(context, "����������ȡ��", Toast.LENGTH_SHORT).show();
            //���� �����δ������أ��û����Notification ����ת����������
            Intent viewDownloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            viewDownloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(viewDownloadIntent);
        } else {
            Bundle bundle = intent.getBundleExtra(FunctionUtil.KEY_REMINDINFO_DATA);
            RemindInfo remindInfo = (RemindInfo) bundle.getSerializable(FunctionUtil.KEY_REMINDINFO);
            Calendar mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            Intent intentRemind = new Intent();
            intentRemind.putExtra(FunctionUtil.KEY_REMINDINFO_DATA,bundle);

            ComponentName componentName = new ComponentName("com.cappu.aiui", "com.cappu.aiui.ui.RemindActivity");
            intentRemind.setComponent(componentName);
            intentRemind.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentRemind);
            //����֪ͨ
            FunctionUtil.sendNotification(SpeechApp.getSpeechApp(), remindInfo);
        }
    }

    private void checkDownloadStatus(Context context, long downloadId) {
        mManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = mManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    installApk(context);
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.d("DownApkReceiver", "����ʧ��.....");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    Log.d("DownApkReceiver", "��������.....");
                    break;
                default:
                    break;
            }
        }
    }

    private void installApk(Context context) {
        String apkName = mSharedPreferences.getString("apkName", null);
        if (apkName != null) {
            File file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + apkName);
            if (file != null) {
                Intent install = new Intent("android.intent.action.VIEW");
                Uri downloadFileUri = Uri.fromFile(file);
                install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(install);
            } else {
            }
        } else {
        }
    }


}