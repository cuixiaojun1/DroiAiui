package com.droi.aiui.controler;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.droi.aiui.Interface.IOnSynthesizerInitComplete;
import com.iflytek.business.speech.SpeechIntent;
import com.iflytek.business.speech.SpeechServiceUtil;
import com.iflytek.business.speech.SynthesizerListener;
import com.iflytek.business.speech.TextToSpeech;

public class SpeechControler {
    private static final String TAG = "SpeechControler";

    public static final String SPEAKER_KEY = "speech_tools_key";

    public static final String SPEAKER_DEFAULT = "3";//"xiaoyan";
    public static final String SPEAKER_YUE_YU = "15";//"xiaomei";
    public static final String SPEAKER_SI_CHUAN = "14";//"xiaorong";
    public static final String SPEAKER_DONG_BEI = "11";//"xiaoqian";
    public static final String SPEAKER_HE_NAN = "25";//"xiaokun";
    public static final String SPEAKER_HU_NAN = "24";//"xiaoqiang";
    public static final String SPEAKER_TAIWAN = "22";//"xiaolin";
    public static final String SPEAKER_NANNAN = "7";//"nannan";
    public static final String SPEAKER_XIAOFENG = "4";//"xiaofeng";
    public static final String SPEAKER_JIAJIA = "9";//"jiajia";

    //modify by jiangyan@20180205
	//private static final String SPEECH_TTS_FILE = "/sdcard/.Speechcloud/";
	private static final String SPEECH_TTS_FILE="/system/media/speech/tts/";
    private SpeechServiceUtil mService;

    protected Context mContext;
    private String mSpeechStr;
    private boolean isSpeaking = false;
    private boolean isSpeakingByRemind = false;

    //语音合成初始化完成回调，避免引擎还没有初始化完成就语音播报
    private IOnSynthesizerInitComplete iOnSynthesizerInitComplete;

    public static final int START = 0;
    public static final int STOP = 1;
    private int mSpeechState = -1;

    private String mSpeaker;

    private Intent mTtsParamsIntent;

    public SpeechControler(Context context,SpeechServiceUtil service) {
        mService = service;
        mTtsParamsIntent = getTtsIntent();
        init(context);
    }

    public SpeechControler(Context context, int type) {
        init(context);
    }

    protected void init(Context context) {
        mContext = context;
        mSpeaker = getSpeaker();
        Uri uri = Settings.Global.getUriFor(SPEAKER_KEY);
        mContext.getContentResolver().registerContentObserver(uri, true, mSpeakerObserver);
    }

    /**
     * 获取语音合成回调监听
     */
    public SynthesizerListener.Stub getSynthesizerListener(){
        return mSynthesizerListener;
    }

    /**
     * 语音合成回调监听。
     */
    private SynthesizerListener.Stub mSynthesizerListener = new SynthesizerListener.Stub() {

        @Override
        public void onProgressCallBack(int arg0) throws RemoteException {
            Log.d(TAG, "onProgressCallBack");
        }

        @Override
        public void onPlayCompletedCallBack(int arg0) throws RemoteException {
//			mService.stopSpeak();
            Log.d(TAG,"onPlayCompletedCallBack---------->isSpeakingByRemind = "+isSpeakingByRemind);
            if(isSpeakingByRemind){
                startSpeech();
            }
        }

        @Override
        public void onPlayBeginCallBack() throws RemoteException {
            Log.d(TAG, "onPlayBeginCallBack");
        }

        @Override
        public void onInterruptedCallback() throws RemoteException {
            Log.d(TAG, "onInterruptedCallback");
        }

        @Override
        public void onInit(int arg0) throws RemoteException {
            Log.d(TAG, "mSynthesizerListener------>onInit--->iOnSynthesizerInitComplete = "+iOnSynthesizerInitComplete);
            if(iOnSynthesizerInitComplete != null){
                iOnSynthesizerInitComplete.onInitCompleteListener();
            }
        }


        @Override
        public void onSpeakPaused() throws RemoteException {
            // TODO Auto-generated method stub
            Log.d(TAG, "onSpeakPaused");
        }

        @Override
        public void onSpeakResumed() throws RemoteException {
            // TODO Auto-generated method stub
            Log.d(TAG, "onSpeakResumed");
        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3)
                throws RemoteException {
            Log.d(TAG, "onEvent");

        }
    };

    public void startSpeech() {
        if (!TextUtils.isEmpty(mSpeechStr)) {
            startSpeech(mSpeechStr);
        }
    }

    public void startSpeechByType(String type) {
        if (type.equals("RemindActivity")) {
            isSpeakingByRemind = true;
        } else {
            isSpeakingByRemind = false;
        }
        startSpeech();
    }

    public void startSpeech(String speechstr) {
        Log.d(TAG,"startSpeech--->speechstr = "+speechstr);
        synchronized (this) {
            mTtsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN, mSpeaker);
            /*if(!isAudioActive()){
                return;
            }*/
            mService.speak(speechstr, mTtsParamsIntent);
            mSpeechState = START;
            isSpeaking = true;
        }

    }

    public void stopSpeech() {
        synchronized (this) {
            Log.i(TAG, "stopSpeech");
            mSpeechState = STOP;
            isSpeaking = false;
            mService.stopSpeak();
        }
    }

    public int getSpeechState() {
        return mSpeechState;
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void destory() {
        mContext.getContentResolver().unregisterContentObserver(mSpeakerObserver);
        synchronized (this) {
            mService.destroy();
        }
    }

    private Intent getTtsIntent() {
        Intent ttsParamsIntent = new Intent();
        ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL);
        ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, true);
        return ttsParamsIntent;
    }

    public void setSpeechContent(String speech) {
        mSpeechStr = speech;
    }

    /**
     * 获取语音合成参数
     */
    public Intent getTtsInitParams() {
        Intent ttsIntent = new Intent();
        ttsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
        //ttsIntent.putExtra(SpeechIntent.ARG_RES_FILE, SpeechIntent.RES_FROM_ASSETS);
        ttsIntent.putExtra(SpeechIntent.ARG_RES_TYPE, SpeechIntent.RES_SPECIFIED);
        ttsIntent.putExtra(SpeechIntent.ARG_RES_FILE, SPEECH_TTS_FILE);
        ttsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, true);
        return ttsIntent;
    }

    public String getSpeaker() {
        String speaker = Settings.Global.getString(mContext.getContentResolver(), SPEAKER_KEY);
        if (TextUtils.isEmpty(speaker)) {
            return SPEAKER_DEFAULT;
        }
        return speaker;
    }

    public void setSpeaker(String speaker) {
        mSpeaker = speaker;
    }

    public static void setSpeaker(Context context, String speaker) {
        Log.d(TAG,"setSpeaker---->speaker = "+speaker);
        Settings.Global.putString(context.getContentResolver(), SPEAKER_KEY, speaker);
    }


    ContentObserver mSpeakerObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "mSpeakerObserver onChange =" + selfChange);
            Log.i(TAG, "mSpeakerObserver speaker =" + getSpeaker());
            mSpeaker = getSpeaker();
        }
    };

    public void setOnSynthesizerInitComplete(IOnSynthesizerInitComplete iOnSynthesizerInitComplete) {
        this.iOnSynthesizerInitComplete = iOnSynthesizerInitComplete;
    }

    public boolean isAudioActive() {
        final AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        boolean statusFlag;
        if(am != null){
            statusFlag = (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) ? true: false;
        }else{
            return false;
        }
        if(statusFlag == true){
            return false;
        }
        return true;
    }

}