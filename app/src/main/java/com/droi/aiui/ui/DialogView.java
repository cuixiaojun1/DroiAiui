package com.droi.aiui.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.droi.aiui.R;

/**
 * Created by cuixiaojun on 17-12-26.
 */

class DialogView extends Dialog {

    public ImageView mVolume;
    public DialogView(@NonNull Context context) {
        super(context, R.style.SpeechDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.speech_dialog,null);
        mVolume = (ImageView)view.findViewById(R.id.id_recorder_dialog_volume);
        setContentView(view);
        getWindow().setGravity(Gravity.CENTER);
    }

}