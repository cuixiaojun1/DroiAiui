package com.droi.aiui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.droi.aiui.controler.AIUIControler;
import com.droi.aiui.controler.SpeechControler;
import com.iflytek.business.speech.SpeechIntent;
import com.iflytek.business.speech.SpeechServiceUtil;


/**
 * Created by cuixiaojun on 2017/12/13.
 * 管理类，主要负责管理语义相关的动作。
 */

public class AiuiManager {

    private final String TAG = "AiuiManager";
    private static AiuiManager mAiuiManager;
    private Intent logIntent;
    //第三方讯飞语音服务，只能初始化一次
    private SpeechServiceUtil mService;
    //语音识别控制器
    private AIUIControler mAIUIControler;
    //语音播报控制器
    private SpeechControler mSpeechControler;
    private IMediaPlaybackServiceConnection mIMediaPlaybackServiceConnection = new IMediaPlaybackServiceConnection();
    private Context mContext;

    private AiuiManager(Context context){
        logIntent = getLogIntent();
        mService = new SpeechServiceUtil(context, mSpeechInitListener, logIntent);
        mAIUIControler = new AIUIControler(context,mService);
        mSpeechControler = new SpeechControler(context,mService);
        mContext = context;
        bindMusicService(context);
    }

    public static AiuiManager getInstance(Context context){
        if(mAiuiManager == null){
            mAiuiManager = new AiuiManager(context);
        }
        return mAiuiManager;
    }
    public static AiuiManager getInstance(){
        return mAiuiManager;
    }
    /**
     * 初期化监听。
     */
    private SpeechServiceUtil.ISpeechInitListener mSpeechInitListener = new SpeechServiceUtil.ISpeechInitListener() {

        @Override
        public void onSpeechInit(int errorCode) {
            // TODO Auto-generated method stub
            Log.d(TAG, "SpeechInitListener---->onSpeechInit | errorCode = " + errorCode);
            if (0 == errorCode) {
                Log.d(TAG, "服务初始化成功");
                String localRecogEngineVersion = mService.getParam(SpeechIntent.EXT_RECOG_LOCAL_ENGINE_VERSION, null);
                if (null != mService) {
                    // 初始化识别引擎
                    // 初始化本地识别，如果不需要本地识别就不用传入SpeechIntent.ENGINE_LOCAL_DEC参数。
                    // 这里演示仅初始化本地识别，构建语法更新词典另做。�
                    //Intent intentInitAsr = new Intent();
                    //intentInitAsr.putExtra(SpeechIntent.ENGINE_LOCAL_DEC, new Bundle());
                    mService.initRecognitionEngine(mAIUIControler.getRecognitionListener(), mAIUIControler.getRecognitionInitParams());
                    mService.initSynthesizerEngine(mSpeechControler.getSynthesizerListener(), mSpeechControler.getTtsInitParams());
                }
            } else {
                Log.d(TAG, "服务初始化失败 | errorCode = " + errorCode);
            }
        }

        @Override
        public void onSpeechUninit() {
            // TODO Auto-generated method stub
            Log.d("TAG", "onSpeechUninit");
        }
    };

    public boolean bindMusicService(Context context) {
        Log.d(TAG, "[AiuiManager][bindMusicService]");
        try{
            Intent service = new Intent("com.droi.music.MediaPlaybackService");
            service.setPackage("com.cappu.music");
            return context.getApplicationContext().bindService(service,
                    mIMediaPlaybackServiceConnection, Context.BIND_AUTO_CREATE);
        }catch(Exception e){
            Log.d(TAG, "[AiuiManager][bindMusicService]音乐服务绑定失败：error = "+e.toString());
            return false;
        }
    }

    private final class IMediaPlaybackServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "[AiuiManager][onServiceConnected]音乐服务连接成功！");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "[AiuiManager][onServiceDisconnected]音乐服务连接失败！");
        }

    }

    /**
     * 开始语音语义理解
     */
    public void startVoiceNlp(){
        Log.d(TAG,"startVoiceNlp");
        if(mAIUIControler != null){
            mAIUIControler.startVoiceNlp();
        }
    }

    /**
     * 结束语音语义理解
     */
    public void stopVoiceNlp(){
        Log.d(TAG,"stopVoiceNlp");
        if(mAIUIControler != null){
            mAIUIControler.stopVoiceNlp();
        }
    }

    /**
     * 暂停语义理解,每次结束一轮对话需要调用次方法结束当前对话
     */
    public void cancelVoiceNlp(){
        if(mAIUIControler != null){
            mAIUIControler.cancelVoiceNlp();
        }
    }

    /**
     * 设置log参数
     */
    private Intent getLogIntent(){
        Intent serviceIntent = new Intent();
        serviceIntent.putExtra(SpeechIntent.SERVICE_LOG_ENABLE, true);
        return serviceIntent;
    }

    /**
     * 获取AIUI控制器
     * @return
     */
    public AIUIControler getAIUIControler(){
        if(mAIUIControler != null){
            return mAIUIControler;
        }
        return null;
    }

    /**
     * 获取语音播报控制器
     * @return
     */
    public SpeechControler getSpeechControler(){
        if(mSpeechControler != null){
            return mSpeechControler;
        }
        return null;
    }

    /**
     * 销毁对象，释放内存
     */
    public void destory(){
        if(mAiuiManager != null){
            mAiuiManager = null;
        }
        if(mAIUIControler != null){
            mAIUIControler.onDestroy();
            mAIUIControler = null;
        }
        if(mSpeechControler != null){
            mSpeechControler.destory();
            mSpeechControler = null;
        }
    }

}