package com.droi.aiui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.droi.aiui.controler.AIUIControler;
import com.droi.aiui.controler.SpeechControler;


/**
 * Created by cuixiaojun on 2017/12/13.
 */

public class AiuiManager {

    private final String TAG = "AiuiManager";
    private static AiuiManager mAiuiManager;
    private Intent logIntent;

    private SpeechServiceUtil mService;

    private AIUIControler mAIUIControler;

    private SpeechControler mSpeechControler;
    private IMediaPlaybackService mMusicService;
    private IMediaPlaybackServiceConnection mIMediaPlaybackServiceConnection = new IMediaPlaybackServiceConnection();
    private Context mContext;

    private AiuiManager(Context context) {
        logIntent = getLogIntent();
        mService = new SpeechServiceUtil(context, mSpeechInitListener, logIntent);
        mAIUIControler = new AIUIControler(context, mService);
        mSpeechControler = new SpeechControler(context, mService);
        mContext = context;
        bindMusicService(context);
    }

    public static AiuiManager getInstance(Context context) {
        if (mAiuiManager == null) {
            mAiuiManager = new AiuiManager(context);
        }
        return mAiuiManager;
    }

    public static AiuiManager getInstance() {
        return mAiuiManager;
    }

    private SpeechServiceUtil.ISpeechInitListener mSpeechInitListener = new SpeechServiceUtil.ISpeechInitListener() {

        @Override
        public void onSpeechInit(int errorCode) {
            // TODO Auto-generated method stub
            Log.d(TAG, "SpeechInitListener---->onSpeechInit | errorCode = " + errorCode);
            if (0 == errorCode) {
                String localRecogEngineVersion = mService.getParam(SpeechIntent.EXT_RECOG_LOCAL_ENGINE_VERSION, null);
                if (null != mService) {
                    // ��ʼ��ʶ������
                    // ��ʼ������ʶ���������Ҫ����ʶ��Ͳ��ô���SpeechIntent.ENGINE_LOCAL_DEC������
                    // ������ʾ����ʼ������ʶ�𣬹����﷨���´ʵ�������
                    //Intent intentInitAsr = new Intent();
                    //intentInitAsr.putExtra(SpeechIntent.ENGINE_LOCAL_DEC, new Bundle());
                    mService.initRecognitionEngine(mAIUIControler.getRecognitionListener(), mAIUIControler.getRecognitionInitParams());
                    mService.initSynthesizerEngine(mSpeechControler.getSynthesizerListener(), mSpeechControler.getTtsInitParams());
                }
            } else {
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
        try {
            Intent service = new Intent("com.cappu.music.MediaPlaybackService");
            service.setPackage("com.cappu.music");
            return context.getApplicationContext().bindService(service,
                    mIMediaPlaybackServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Log.d(TAG, "[AiuiManager][bindMusicService]���ַ����ʧ�ܣ�error = " + e.toString());
            return false;
        }
    }

    private final class IMediaPlaybackServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "[AiuiManager][onServiceConnected]���ַ������ӳɹ���");
            try {
                mMusicService = IMediaPlaybackService.Stub.asInterface(service);
            } catch (Exception e) {
                mMusicService = null;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "[AiuiManager][onServiceDisconnected]���ַ�������ʧ�ܣ�");
            mMusicService = null;
        }

    }

    public IMediaPlaybackService getMusicService() {
        return mMusicService;
    }

    /**
     * ��ʼ�����������
     */
    public void startVoiceNlp() {
        Log.d(TAG, "startVoiceNlp");
        if (mAIUIControler != null) {
            mAIUIControler.startVoiceNlp();
        }
    }

    /**
     * ���������������
     */
    public void stopVoiceNlp() {
        Log.d(TAG, "stopVoiceNlp");
        if (mAIUIControler != null) {
            mAIUIControler.stopVoiceNlp();
        }
    }

    /**
     * ��ͣ�������,ÿ�ν���һ�ֶԻ���Ҫ���ôη���������ǰ�Ի�
     */
    public void cancelVoiceNlp() {
        if (mAIUIControler != null) {
            mAIUIControler.cancelVoiceNlp();
        }
    }

    /**
     * ����log����
     */
    private Intent getLogIntent() {
        Intent serviceIntent = new Intent();
        serviceIntent.putExtra(SpeechIntent.SERVICE_LOG_ENABLE, true);
        return serviceIntent;
    }

    /**
     * ��ȡAIUI������
     *
     * @return
     */
    public AIUIControler getAIUIControler() {
        if (mAIUIControler != null) {
            return mAIUIControler;
        }
        return null;
    }

    /**
     * ��ȡ��������������
     *
     * @return
     */
    public SpeechControler getSpeechControler() {
        if (mSpeechControler != null) {
            return mSpeechControler;
        }
        return null;
    }

    /**
     * ���ٶ����ͷ��ڴ�
     */
    public void destory() {
        if (mAiuiManager != null) {
            mAiuiManager = null;
        }
        if (mAIUIControler != null) {
            mAIUIControler.onDestroy();
            mAIUIControler = null;
        }
        if (mSpeechControler != null) {
            mSpeechControler.destory();
            mSpeechControler = null;
        }
    }

}