package com.droi.aiui.ui;

import android.content.Context;

import com.droi.aiui.R;

/**
 * Created by cuixiaojun on 17-12-20.
 */

public class SpeechDialogView {

    private Context mContext;
    private DialogView mDialog;

    /**
     * 构造方法 传入上下文
     */
    public SpeechDialogView(Context context) {
        this.mContext = context;
    }

    // 显示录音的对话框
    public void showRecordingDialog() {
        if (mDialog == null){
            mDialog = new DialogView(mContext);
        }
        mDialog.getWindow().setWindowAnimations(0);
        mDialog.show();
    }

    // 显示取消的对话框
    public void dimissDialog() {
        if(mDialog != null && mDialog.isShowing()){ //显示状态
            mDialog.dismiss();
        }
    }

    // 显示更新音量级别的对话框
    public void updateVoiceLevel(int level) {
        if(mDialog != null && mDialog.isShowing()){ //显示状态
            mDialog.mVolume.setImageResource(R.drawable.volume);
            mDialog.mVolume.setImageLevel(level);
        }
    }

}