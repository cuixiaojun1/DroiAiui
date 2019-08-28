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
import android.view.View;
import android.widget.TextView;

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
        JeffLog.d(TAG,"onCreate");
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
        CappuLog.d(TAG,"[RemindActivity][onCreate]currentRemindInfo = "+currentRemindInfo);
        if(!allRemindInfos.contains(currentRemindInfo)){
            allRemindInfos.add(currentRemindInfo);
        }
        remindDataTime = FunctionUtil.getRemindDateTime(currentRemindInfo.getTime());
        CappuLog.d(TAG,"[RemindActivity][onCreate]--->��ǰ���ѣ�"+currentRemindInfo+",remindDataTime = "+ AlarmManagerUtil.printTime(currentRemindInfo.getTime()));

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
        CappuLog.d(TAG,"[RemindActivity][onNewIntent]currentRemindInfo = "+currentRemindInfo);
        if(!allRemindInfos.contains(currentRemindInfo)){
            allRemindInfos.add(currentRemindInfo);
        }
        remindDataTime = FunctionUtil.getRemindDateTime(currentRemindInfo.getTime());
        JeffLog.d(TAG,"onNewIntent--->��ǰ���ѣ�"+currentRemindInfo+",remindDataTime = "+currentRemindInfo);

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
                        String speech = "���ˣ�����"+reminds_content.getText()+"�ˣ�";
                        mSpeechControler.setSpeechContent(speech);
                        if(!mSpeechControler.isSpeaking()){
                            JeffLog.d(TAG,"��ʼ������"+speech);
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
        JeffLog.d(TAG,"onResume");
        mHandler.sendEmptyMessageDelayed(0,2000);
    }
    /**
     * ��ʼ��������
     */
    private void startMedia() {
        try {
            mMediaPlayer.setDataSource(this,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)); //��������ΪĬ����������
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
     * �ر���������
     */
    private void stopMedia(){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
    }

    /**
     * ��ʼ��
     */
    private void startVibrator() {
        JeffLog.d(TAG,"��ʼ��");
        /**
         * �������𶯴�С����ͨ���ı�pattern���趨���������ʱ��̫�̣���Ч�����ܸо�����
         */
        long[] pattern = { 500, 1000, 500, 1000 }; // ֹͣ ���� ֹͣ ����
        vibrator.vibrate(pattern, 0);
    }

    /**
     * ȡ����
     */
    private void stopVibrator(){
        JeffLog.d(TAG,"ֹͣ��");
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