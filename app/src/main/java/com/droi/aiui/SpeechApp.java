package com.droi.aiui;

import android.app.Application;
import android.util.Log;

import com.droi.aiui.controler.CrashHandler;
import com.droi.aiui.controler.DataControler;


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
        //CrashHandler.create(this);
        //UMConfigure.init(this,  UMConfigure.DEVICE_TYPE_PHONE, null);
    }

    public static SpeechApp getSpeechApp(){
        return speechApp;
    }

}