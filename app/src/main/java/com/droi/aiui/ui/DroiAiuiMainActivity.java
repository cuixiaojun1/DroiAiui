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

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.droi.aiui.AiuiManager;
import com.droi.aiui.Interface.IOnParseListener;
import com.droi.aiui.Interface.IOnRemindCancelClickListener;
import com.droi.aiui.Interface.OnParseResultTextListener;
import com.droi.aiui.Interface.OnVolumeChangedListener;
import com.droi.aiui.R;
import com.droi.aiui.apkupdate.UpdateChecker;
import com.droi.aiui.bean.Message;
import com.droi.aiui.controler.SpeechControler;
import com.droi.aiui.util.FragmentManageImpl;
import com.droi.aiui.util.FunctionUtil;
import com.droi.aiui.util.StatusBarUtils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cuixiaojun on 17-12-28.
 * AIUI主界面
 */

public class DroiAiuiMainActivity extends Activity implements View.OnTouchListener, View.OnClickListener, FragmentManageImpl.IManageFragment, OnParseResultTextListener {

    private final String TAG = "CappuAiuiActivity";

    private TextView mainHelpBtn, mainRemindBtn, mainHelpBtnText, mainRemindBtnText;
    private ImageView mSpeechButton;
    /*语音识别控制器*/
    public AiuiManager mAiuiManager;
    /*fragment管理器*/
    private FragmentManageImpl fragmentManageImp;
    /*语音听写对话框*/
    private SpeechDialogView mSpeechDialogView;
    /*提醒删除回调接口*/
    private IOnRemindCancelClickListener onRemindCancelClickListener;
    /*结果接收回调接口*/
    private IOnParseListener onParseListener;

    private SpeechControler mSpeechControler;

    /* 获取百度位置信息*/
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
        //检查版本更新
        UpdateChecker.checkForDialog(this);
        //打开友盟报错log
        UMConfigure.setLogEnabled(true);

        /*设置Activity全屏显示*/
        StatusBarUtils.fullScreen(this);
        //设置音频流类型为多媒体类型
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        /* 初始化fragment管理器*/
        fragmentManageImp = new FragmentManageImpl(this, this);

        /* 初始化AIUI管理类 */
        mAiuiManager = AiuiManager.getInstance(getApplicationContext());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechControler = mAiuiManager.getSpeechControler();
        /* 百度定位 */
        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        locationClientOption = new LocationClientOption();
        //设置百度定位参数
        setLocationParams();
        myListener = new MyLocationListener();
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);

        /* 注册音量变化回调接口*/
        mAiuiManager.getAIUIControler().setOnVolumeChangedListener(myOnVolumeChangedListener);
        /* 注册结果解析回调接口*/
        mAiuiManager.getAIUIControler().getParseControler().setOnParseResultTextListener(this);

        /* 默认显示fragment*/
        fragmentManageImp.showFragment(1);

        /* 初始化对话view*/
        mSpeechDialogView = new SpeechDialogView(this);
        /*初始化视图*/
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //设置友盟报错信息抓取
        MobclickAgent.onResume(this);
        //开始百度定位
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
        //设置友盟报错信息抓取
        MobclickAgent.onPause(this);
        if (mSpeechControler != null && mSpeechControler.isSpeaking()) {
            mSpeechControler.stopSpeech();
        }
        if (mSpeechDialogView != null) {
            mSpeechDialogView.dimissDialog();
        }
    }

    /**
     *视图初始化
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
     * 接受消息回调
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
     *百度位置变化回调接口
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            double latitude = location.getLatitude();    //获取纬度信息
            double longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f
            //获取当前城市
            String city = location.getCity();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = location.getCoorType();
            //获取定位失败码
            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

            if (latitude != 0 && longitude != 0) {
                Log.d(TAG, "onReceiveLocation--->当前城市：" + city + ",经度 = " + longitude + ",纬度 = " + latitude);
                //定位成功，获取到经度纬度则重新给AIUI设置参数
                if (mAiuiManager.getAIUIControler() != null) {
                    mAiuiManager.getAIUIControler().setAIUIParams(longitude, latitude);
                }
                //停止百度定位
                mLocationClient.stop();
            }
        }
    }

    /**
     * 设置百度定位参数
     */
    private void setLocationParams() {
        locationClientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；
        locationClientOption.setCoorType("gcj02");
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
        locationClientOption.setScanSpan(1000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        locationClientOption.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        locationClientOption.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        locationClientOption.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        locationClientOption.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false
        //locationClientOption.setWifiCacheTimeOut(5 * 60 * 1000);
        //可选，7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位
        locationClientOption.setEnableSimulateGps(false);
        locationClientOption.setIsNeedAddress(true);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        mLocationClient.setLocOption(locationClientOption);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
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