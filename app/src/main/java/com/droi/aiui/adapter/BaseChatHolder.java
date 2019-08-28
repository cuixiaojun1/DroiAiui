package com.droi.aiui.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.droi.aiui.R;

/**
 * Created by cuixiaojun on 17-12-26.
 */

public class BaseChatHolder {

    public TextView tvChatMsgRobot;
    public ImageView ivHead;

    public BaseChatHolder(View view) {
        tvChatMsgRobot =  (TextView)view.findViewById(R.id.tv_chat_msg_cappu_robot);
    }
}