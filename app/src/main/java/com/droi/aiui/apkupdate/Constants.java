package com.droi.aiui.apkupdate;

import android.os.Environment;

import java.io.File;

class Constants {

    static final String APK_DOWNLOAD_URL = "url";

    public static final String BASE_URL = "http://upgrade.cappu.cn";
    public static final String SIGN_KEY   = "cappu-g@od";
    public static final String CHANNEL    = "51e66ab897a0764eae2950169cf0fed0";
    public static final String APPKEY     = "af0a54d4ec274245ba43ded3b04c85b8";
    public static final String APP_SECRET = "e5bcf3af334e41638ab73fcd4840fc81";
    public static final String SIGN       = MD5Utils.md5Encode(APP_SECRET + SIGN_KEY);
    /**
     * 工程存储文件夹
     */
    public static String BASE_PROJECT_DIR = Environment.getExternalStorageDirectory() + File.separator + "CappuAiui";
}


