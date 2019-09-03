package com.droi.aiui.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.droi.aiui.AiuiManager;
import com.droi.aiui.R;
import com.droi.aiui.bean.RemindDataTime;
import com.droi.aiui.bean.RemindInfo;
import com.droi.aiui.controler.SpeechControler;
import com.droi.aiui.dao.RemindDBHelp;
import com.droi.aiui.util.AlarmManagerUtil;
import com.droi.aiui.util.FunctionUtil;
import com.droi.aiui.util.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuixiaojun on 2018/01/13.
 */

public class RemindActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "RemindActivity";
    private TextView reminds_date,reminds_content,reminds_cancel;
    private MediaPlayer mMediaPlayer;
    private Vibrator vibrator;
    private RemindDBHelp remindDBHelp;
    private AiuiManager mAiuiManager;
    private SpeechControler mSpeechControler;
    private List<RemindInfo> allRemindInfos = new ArrayList<RemindInfo>();
    private RemindInfo currentRemindInfo;
    private RemindDataTime remindDataTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cappu_remind_ui);
        Log.d(TAG,"onCreate");
        StatusBarUtils.fullScreen(this);
        mMediaPlayer = new MediaPlayer();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        remindDBHelp = RemindDBHelp.getInstance(getApplicationContext());
        mAiuiManager = AiuiManager.getInstance(getApplicationContext());
        mSpeechControler = mAiuiManager.getSpeechControler();

        reminds_date = (TextView)findViewById(R.id.tv_reminds_date);
        reminds_content = (TextView)findViewById(R.id.tv_reminds_content);
        reminds_cancel = (TextView)findViewById(R.id.tv_reminds_cancel);
        reminds_cancel.setOnClickListener(this);

        currentRemindInfo = (RemindInfo) getIntent().getBundleExtra(FunctionUtil.KEY_REMINDINFO_DATA).getSerializable(FunctionUtil.KEY_REMINDINFO);
        Log.d(TAG,"[RemindActivity][onCreate]currentRemindInfo = "+currentRemindInfo);
        if(!allRemindInfos.contains(currentRemindInfo)){
            allRemindInfos.add(currentRemindInfo);
        }
        remindDataTime = FunctionUtil.getRemindDateTime(currentRemindInfo.getTime());
        Log.d(TAG,"[RemindActivity][onCreate]--->当前提醒："+currentRemindInfo+",remindDataTime = "+ AlarmManagerUtil.printTime(currentRemindInfo.getTime()));

        if(remindDataTime != null){
            reminds_date.setText(remindDataTime.getTime());
        }
        if(currentRemindInfo != null){
            reminds_content.setText(currentRemindInfo.getContent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        currentRemindInfo = (RemindInfo) intent.getBundleExtra(FunctionUtil.KEY_REMINDINFO_DATA).getSerializable(FunctionUtil.KEY_REMINDINFO);
        Log.d(TAG,"[RemindActivity][onNewIntent]currentRemindInfo = "+currentRemindInfo);
        if(!allRemindInfos.contains(currentRemindInfo)){
            allRemindInfos.add(currentRemindInfo);
        }
        remindDataTime = FunctionUtil.getRemindDateTime(currentRemindInfo.getTime());
        Log.d(TAG,"onNewIntent--->当前提醒："+currentRemindInfo+",remindDataTime = "+currentRemindInfo);

        if(remindDataTime != null){
            reminds_date.setText(remindDataTime.getTime());
        }
        if(currentRemindInfo != null){
            reminds_content.setText(currentRemindInfo.getContent());
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.tv_reminds_cancel){
            if(allRemindInfos != null){
                for (int i = 0; i < allRemindInfos.size(); i++) {
                    RemindInfo remindInfo = allRemindInfos.get(i);
                    if(remindDBHelp != null){
                        if(allRemindInfos.get(i).getRepeatDate().equals("ONETIME")){
                            remindDBHelp.delete(remindInfo);
                        }
                    }
                }
            }
            FunctionUtil.cancelNotification(this,currentRemindInfo);
            if(mSpeechControler != null){
                mSpeechControler.stopSpeech();
            }
            stopVibrator();
            stopMedia();
            mHandler.removeMessages(0);
            finish();
        }
    }
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            startVibrator();
            startMedia();
            if(AiuiManager.getInstance() != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String speech = "主人，您该"+reminds_content.getText()+"了！";
                        mSpeechControler.setSpeechContent(speech);
                        if(!mSpeechControler.isSpeaking()){
                            Log.d(TAG,"开始播报！"+speech);
                            mSpeechControler.startSpeechByType("RemindActivity");
                        }
                    }
                });
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(allRemindInfos != null ){
            for (int i = 0; i < allRemindInfos.size(); i++) {
                RemindInfo remindInfo = allRemindInfos.get(i);
                if(remindDBHelp != null){
                    if(allRemindInfos.get(i).getRepeatDate().equals("ONETIME")){
                        remindDBHelp.delete(remindInfo);
                    }
                }
            }
        }
        FunctionUtil.cancelNotification(this,currentRemindInfo);
        if(mSpeechControler != null){
            mSpeechControler.stopSpeech();
        }
        stopVibrator();
        stopMedia();
        mHandler.removeMessages(0);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        mHandler.sendEmptyMessageDelayed(0,2000);
    }
    /**
     * 开始播放铃声
     */
    private void startMedia() {
        try {
            mMediaPlayer.setDataSource(this,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)); //铃声类型为默认闹钟铃声
            mMediaPlayer.prepare();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭铃声播放
     */
    private void stopMedia(){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
    }

    /**
     * 开始震动
     */
    private void startVibrator() {
        Log.d(TAG,"开始震动");
        /**
         * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
         */
        long[] pattern = { 500, 1000, 500, 1000 }; // 停止 开启 停止 开启
        vibrator.vibrate(pattern, 0);
    }

    /**
     * 取消震动
     */
    private void stopVibrator(){
        Log.d(TAG,"停止震动");
        if(vibrator.hasVibrator()){
            vibrator.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSpeechControler != null ){
            mSpeechControler.stopSpeech();
        }
        stopVibrator();
        stopMedia();
        mHandler.removeMessages(0);
    }
}