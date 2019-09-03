package com.droi.aiui.apkupdate;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";
    public static final String FILE_CAPPU_AIUI = "cappuaiui";
    private static final int NOTIFICATION_ID = 0;

    private DownloadManager downloadManager;
    private long downloadId;

    private SharedPreferences mSharedPreferences;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        mSharedPreferences = this.getSharedPreferences(FILE_CAPPU_AIUI, MODE_PRIVATE);
        String appName = getString(getApplicationInfo().labelRes);
        String urlStr = intent.getStringExtra(Constants.APK_DOWNLOAD_URL);
        String apkNames = intent.getStringExtra("apkName");
        final String apkName = apkNames.substring(apkNames.lastIndexOf("/") + 1, apkNames.length());
        Log.d(TAG, "[DownloadService][onHandleIntent]appName = " + appName + ",urlStr = " + urlStr + ",apkName = " + apkName);
        downloadId = mSharedPreferences.getLong("downloadId", 0);
        if (downloadId != 0) {
            clearCurrentTask(downloadId);
        }
        downloadId = downloadApk(DownloadService.this, urlStr, appName);
        Log.d(TAG,"[onHandleIntent]downloadId = "+downloadId);
        if (downloadId != 0) {
            mSharedPreferences.edit().putLong("downloadId", downloadId).commit();
        }
    }

    private long downloadApk(Context context,String url, String title) {
        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        String apkName = title + ".apk";
        File file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + apkName);
        if (file != null && file.exists()) {
            file.delete();
        }
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir() ;
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, apkName);
        mSharedPreferences.edit().putString("apkName", apkName).commit();
        //下载中和下载完后都显示通知栏
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);
        //设置WIFI下进行更新
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //设置类型为.apk
        request.setMimeType("application/vnd.android.package-archive");
        //通知栏标题
        request.setTitle(title);
        //通知栏描述信息
        request.setDescription("下载完成后，点击安装");
        request.setAllowedOverRoaming(false);
        //获取下载任务ID
        return downloadManager.enqueue(request);
    }

    /**
     * 下载前先移除前一个任务，防止重复下载
     *
     * @param downloadId
     */
    public void clearCurrentTask(long downloadId) {
        try {
            downloadManager.remove(downloadId);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

}