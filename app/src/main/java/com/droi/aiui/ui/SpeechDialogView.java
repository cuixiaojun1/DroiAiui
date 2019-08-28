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
     * ���췽�� ����������
     */
    public SpeechDialogView(Context context) {
        this.mContext = context;
    }

    // ��ʾ¼���ĶԻ���
    public void showRecordingDialog() {
        if (mDialog == null){
            mDialog = new DialogView(mContext);
        }
        mDialog.getWindow().setWindowAnimations(0);
        mDialog.show();
    }

    // ��ʾȡ���ĶԻ���
    public void dimissDialog() {
        if(mDialog != null && mDialog.isShowing()){ //��ʾ״̬
            mDialog.dismiss();
        }
    }

    // ��ʾ������������ĶԻ���
    public void updateVoiceLevel(int level) {
        if(mDialog != null && mDialog.isShowing()){ //��ʾ״̬
            mDialog.mVolume.setImageResource(R.drawable.volume);
            mDialog.mVolume.setImageLevel(level);
        }
    }

}