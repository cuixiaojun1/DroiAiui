package com.droi.aiui;

import android.app.Application;
import android.util.Log;

import com.droi.aiui.controler.CrashHandler;
import com.droi.aiui.controler.DataControler;
import com.umeng.commonsdk.UMConfigure;


/**
 * Created by cuixiaojun on 17-12-6.
 */

public class SpeechApp extends Application {
    private final String TAG = "SpeechApp";
    private DataControler mDataControler;
    private static SpeechApp speechApp;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"[SpeechApp][onCreate]start");
        speechApp = this;
        mDataControler = DataControler.getInstance(this);
        mDataControler.startLoadContact();
        mDataControler.startLoadApps();
        mDataControler.startLoadMusic();
        mDataControler.registerContentObservers(this);
        //捕获所有的错误信息，如果有致命错误发生，直接退出应用
        CrashHandler.create(this);
        //设置友盟报错信息抓取
        UMConfigure.init(this,  UMConfigure.DEVICE_TYPE_PHONE, null);
        Log.d(TAG,"[SpeechApp][onCreate]end");
    }

    public static SpeechApp getSpeechApp(){
        return speechApp;
    }

}