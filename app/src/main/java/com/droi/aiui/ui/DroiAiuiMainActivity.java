package com.droi.aiui.ui;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cuixiaojun on 17-12-28.
 * AIUI������
 */

public class DroiAiuiMainActivity extends Activity implements View.OnTouchListener, View.OnClickListener, FragmentManageImp.IManageFragment, OnParseResultTextListener {

    private final String TAG = "CappuAiuiActivity";

    private TextView mainHelpBtn, mainRemindBtn, mainHelpBtnText, mainRemindBtnText;
    private ImageView mSpeechButton;
    /*����ʶ�������*/
    public AiuiManager mAiuiManager;
    /*fragment������*/
    private FragmentManageImp fragmentManageImp;
    /*������д�Ի���*/
    private SpeechDialogView mSpeechDialogView;
    /*����ɾ���ص��ӿ�*/
    private IOnRemindCancelClickListener onRemindCancelClickListener;
    /*������ջص��ӿ�*/
    private IOnParseListener onParseListener;

    private SpeechControler mSpeechControler;

    /* ��ȡ�ٶ�λ����Ϣ*/
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener;
    private LocationClientOption locationClientOption;
    private RemindFragment remindFragment;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "[CappuAiuiActivity][onCreate]");
        setContentView(R.layout.cappu_main_activity);

        UpdateChecker.checkForDialog(this);
        //�����˱���log
        UMConfigure.setLogEnabled(true);

        /*����Activityȫ����ʾ*/
        StatusBarUtils.fullScreen(this);
        //������Ƶ������Ϊ��ý������
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        /* ��ʼ��fragment������*/
        fragmentManageImp = new FragmentManageImp(this, this);

        /* ��ʼ��AIUI������ */
        mAiuiManager = AiuiManager.getInstance(getApplicationContext());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechControler = mAiuiManager.getSpeechControler();
        /* �ٶȶ�λ */
        mLocationClient = new LocationClient(getApplicationContext());
        //����LocationClient��
        locationClientOption = new LocationClientOption();
        //���ðٶȶ�λ����
        setLocationParams();
        myListener = new MyLocationListener();
        //ע���������
        mLocationClient.registerLocationListener(myListener);

        /* ע�������仯�ص��ӿ�*/
        mAiuiManager.getAIUIControler().setOnVolumeChangedListener(myOnVolumeChangedListener);
        /* ע���������ص��ӿ�*/
        mAiuiManager.getAIUIControler().getParseControler().setOnParseResultTextListener(this);

        /* Ĭ����ʾfragment*/
        fragmentManageImp.showFragment(1);

        /* ��ʼ���Ի�view*/
        mSpeechDialogView = new SpeechDialogView(this);
        /*��ʼ����ͼ*/
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //�������˱�����Ϣץȡ
        MobclickAgent.onResume(this);
        //��ʼ�ٶȶ�λ
        if (mLocationClient != null) {
            mLocationClient.start();
        }
        if (onRemindCancelClickListener != null) {
            onRemindCancelClickListener.onRemindCancel();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        //�������˱�����Ϣץȡ
        MobclickAgent.onPause(this);
        if (mSpeechControler != null && mSpeechControler.isSpeaking()) {
            mSpeechControler.stopSpeech();
        }
        if (mSpeechDialogView != null) {
            mSpeechDialogView.dimissDialog();
        }
    }

    /**
     * ��ͼ��ʼ��
     */
    private void initView() {
        mainHelpBtn = (TextView) findViewById(R.id.mainHelpBtn);
        mainRemindBtn = (TextView) findViewById(R.id.mainRemindBtn);
        mSpeechButton = (ImageView) findViewById(R.id.recording);
        mainHelpBtnText = (TextView) findViewById(R.id.tv_help);
        mainRemindBtnText = (TextView) findViewById(R.id.tv_remind);

        mSpeechButton.setBackgroundResource(R.mipmap.cappu_record_selected);

        mSpeechButton.setOnTouchListener(this);
        mainHelpBtn.setOnClickListener(this);
        mainRemindBtn.setOnClickListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (view.getId()) {
            case R.id.recording:
                mainHelpBtn.setBackgroundResource(R.mipmap.cappu_help);
                mainHelpBtnText.setBackgroundResource(R.drawable.button_bg_corner_normal);
                mainRemindBtn.setBackgroundResource(R.mipmap.cappu_remind);
                mainRemindBtnText.setBackgroundResource(R.drawable.button_bg_corner_normal);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                        mSpeechButton.setBackgroundResource(R.mipmap.cappu_record_pressed);
                        mAiuiManager.startVoiceNlp();
                        if (mSpeechControler != null && mSpeechControler.isSpeaking()) {
                            mSpeechControler.stopSpeech();
                        }
                        if (mSpeechDialogView != null) {
                            mSpeechDialogView.showRecordingDialog();
                        }
                        fragmentManageImp.showFragment(1);
                        break;
                    case MotionEvent.ACTION_UP:
                        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
                        mSpeechButton.setBackgroundResource(R.mipmap.cappu_record_unpressed);
                        mAiuiManager.stopVoiceNlp();
                        if (mSpeechDialogView != null) {
                            mSpeechDialogView.dimissDialog();
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mainHelpBtn:
                if (!FunctionUtil.isFastClick()) {
                    if (!fragmentManageImp.getCurrentFragment().getTag().equals("help")) {
                        fragmentManageImp.showFragment(0);
                        mainHelpBtn.setBackgroundResource(R.mipmap.cappu_help_press);
                        mainHelpBtnText.setBackgroundResource(R.drawable.button_bg_corner_pressed);
                        mainRemindBtn.setBackgroundResource(R.mipmap.cappu_remind);
                        mainRemindBtnText.setBackgroundResource(R.drawable.button_bg_corner_normal);
                        mSpeechButton.setBackgroundResource(R.mipmap.cappu_record_unselected);
                    } else {
                        fragmentManageImp.showFragment(1);
                        mainHelpBtn.setBackgroundResource(R.mipmap.cappu_help);
                        mainHelpBtnText.setBackgroundResource(R.drawable.button_bg_corner_normal);
                        mainRemindBtn.setBackgroundResource(R.mipmap.cappu_remind);
                        mainRemindBtnText.setBackgroundResource(R.drawable.button_bg_corner_normal);
                        mSpeechButton.setBackgroundResource(R.mipmap.cappu_record_selected);
                    }
                }
                break;
            case R.id.mainRemindBtn:
                if (!FunctionUtil.isFastClick()) {
                    if (!fragmentManageImp.getCurrentFragment().getTag().equals("remind")) {
                        mainRemindBtn.setBackgroundResource(R.mipmap.cappu_remind_press);
                        mainRemindBtnText.setBackgroundResource(R.drawable.button_bg_corner_pressed);
                        mainHelpBtn.setBackgroundResource(R.mipmap.cappu_help);
                        mainHelpBtnText.setBackgroundResource(R.drawable.button_bg_corner_normal);
                        mSpeechButton.setBackgroundResource(R.mipmap.cappu_record_unselected);
                        fragmentManageImp.showFragment(2);
                    } else {
                        mainHelpBtn.setBackgroundResource(R.mipmap.cappu_help);
                        mainHelpBtnText.setBackgroundResource(R.drawable.button_bg_corner_normal);
                        mainRemindBtn.setBackgroundResource(R.mipmap.cappu_remind);
                        mainRemindBtnText.setBackgroundResource(R.drawable.button_bg_corner_normal);
                        mSpeechButton.setBackgroundResource(R.mipmap.cappu_record_selected);
                        fragmentManageImp.showFragment(1);
                    }
                    break;
                }
        }
    }

    @Override
    public int getFragmentContainer() {
        return R.id.container;
    }

    @Override
    public List<String> initFragmentTags() {
        return Arrays.asList("help", "list", "remind");
    }

    @Override
    public BaseFragment instantFragment(int currIndex) {
        switch (currIndex) {
            case 0:
                return new HelpFragment();
            case 1:
                return new ChatFragment();

            case 2:
                remindFragment = new RemindFragment();
                return remindFragment;

        }
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        fragmentManageImp.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fragmentManageImp.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * ������Ϣ�ص�
     *
     * @param messages
     */
    @Override
    public void onParseResult(ArrayList<Message> messages) {
        if (onParseListener != null) {
            onParseListener.onParseListener(messages);
        }
    }

    public void setOnRemindCancelClickListener(IOnRemindCancelClickListener onRemindCancelClickListener) {
        this.onRemindCancelClickListener = onRemindCancelClickListener;
    }

    public void setOnParseListener(IOnParseListener onParseListener) {
        this.onParseListener = onParseListener;
    }

    /**
     * �ٶ�λ�ñ仯�ص��ӿ�
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //�˴���BDLocationΪ��λ�����Ϣ�࣬ͨ�����ĸ���get�����ɻ�ȡ��λ��ص�ȫ�����
            //����ֻ�оٲ��ֻ�ȡ��γ����أ����ã��Ľ����Ϣ
            //��������Ϣ��ȡ˵�����������ο���BDLocation���е�˵��
            double latitude = location.getLatitude();    //��ȡγ����Ϣ
            double longitude = location.getLongitude();    //��ȡ������Ϣ
            float radius = location.getRadius();    //��ȡ��λ���ȣ�Ĭ��ֵΪ0.0f
            //��ȡ��ǰ����
            String city = location.getCity();
            //��ȡ��γ���������ͣ���LocationClientOption�����ù�����������Ϊ׼
            String coorType = location.getCoorType();
            //��ȡ��λʧ����
            int errorCode = location.getLocType();
            //��ȡ��λ���͡���λ���󷵻��룬������Ϣ�ɲ�����ο���BDLocation���е�˵��

            if (latitude != 0 && longitude != 0) {
                Log.d(TAG, "onReceiveLocation--->��ǰ���У�" + city + ",���� = " + longitude + ",γ�� = " + latitude);
                //��λ�ɹ�����ȡ������γ�������¸�AIUI���ò���
                if (mAiuiManager.getAIUIControler() != null) {
                    mAiuiManager.getAIUIControler().setAIUIParams(longitude, latitude);
                }
                //ֹͣ�ٶȶ�λ
                mLocationClient.stop();
            }
        }
    }

    /**
     * ���ðٶȶ�λ����
     */
    private void setLocationParams() {
        locationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //��ѡ�����ö�λģʽ��Ĭ�ϸ߾���
        //LocationMode.Hight_Accuracy���߾��ȣ�
        //LocationMode. Battery_Saving���͹��ģ�
        //LocationMode. Device_Sensors����ʹ���豸��
        locationClientOption.setCoorType("gcj02");
        //��ѡ�����÷��ؾ�γ���������ͣ�Ĭ��gcj02
        //gcj02����������ꣻ
        //bd09ll���ٶȾ�γ�����ꣻ
        //bd09���ٶ�ī�������ꣻ
        //���������λ�����������������ͣ�ͳһ����wgs84��������
        locationClientOption.setScanSpan(1000);
        //��ѡ�����÷���λ����ļ����int���ͣ���λms
        //�������Ϊ0��������ζ�λ��������λһ�Σ�Ĭ��Ϊ0
        //������÷�0��������1000ms���ϲ���Ч
        locationClientOption.setOpenGps(true);
        //��ѡ�������Ƿ�ʹ��gps��Ĭ��false
        //ʹ�ø߾��Ⱥͽ����豸���ֶ�λģʽ�ģ�������������Ϊtrue
        locationClientOption.setLocationNotify(true);
        //��ѡ�������Ƿ�GPS��Чʱ����1S/1��Ƶ�����GPS�����Ĭ��false
        locationClientOption.setIgnoreKillProcess(false);
        //��ѡ����λSDK�ڲ���һ��service�����ŵ��˶������̡�
        //�����Ƿ���stop��ʱ��ɱ��������̣�Ĭ�ϣ����飩��ɱ������setIgnoreKillProcess(true)
        locationClientOption.SetIgnoreCacheException(false);
        //��ѡ�������Ƿ��ռ�Crash��Ϣ��Ĭ���ռ���������Ϊfalse
        //locationClientOption.setWifiCacheTimeOut(5 * 60 * 1000);
        //��ѡ��7.2�汾��������
        //��������˸ýӿڣ��״�������λʱ�������жϵ�ǰWiFi�Ƿ񳬳���Ч�ڣ���������Ч�ڣ���������ɨ��WiFi��Ȼ��λ
        locationClientOption.setEnableSimulateGps(false);
        locationClientOption.setIsNeedAddress(true);
        //��ѡ�������Ƿ���Ҫ����GPS��������Ĭ����Ҫ��������Ϊfalse
        mLocationClient.setLocOption(locationClientOption);
        //mLocationClientΪ�ڶ�����ʼ������LocationClient����
        //�轫���úõ�LocationClientOption����ͨ��setLocOption�������ݸ�LocationClient����ʹ��
        //����LocationClientOption�����ã��������ο���LocationClientOption�����ϸ˵��
    }

    private OnVolumeChangedListener myOnVolumeChangedListener = new OnVolumeChangedListener() {

        @Override
        public void onVolumeChanged(final int volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSpeechDialogView != null) {
                        mSpeechDialogView.updateVoiceLevel(volume);
                    }
                }
            });
        }
    };

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mAiuiManager != null) {
            mAiuiManager.destory();
        }
    }
}