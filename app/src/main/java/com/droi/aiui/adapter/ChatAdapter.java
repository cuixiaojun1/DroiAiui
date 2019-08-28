package com.droi.aiui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.droi.aiui.R;
import com.droi.aiui.bean.Message;

import java.util.ArrayList;


/**
 * Created by hejianfeng on 2017/12/14.
 */

public class ChatAdapter extends BaseAdapter {
    private ArrayList<Message> messageArrayList;
    private LayoutInflater mInflater;

    final int MSG_ROBOT = 0;
    final int MSG_USER = 1;

    public ChatAdapter(Context context, ArrayList<Message> messages) {
        messageArrayList = messages;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return messageArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return messageArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageArrayList.get(position).isFromRobot()) {
            return MSG_ROBOT;
        } else {
            return MSG_USER;
        }
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        BaseChatHolder baseChatHolder = null;
        Message message = messageArrayList.get(i);
        switch (getItemViewType(i)) {
            case MSG_ROBOT:
                convertView = mInflater.inflate(R.layout.chartting_item_cappu_robot_text, null);
                baseChatHolder = new RobotHolder(convertView);
                break;
            case MSG_USER:
                convertView = mInflater.inflate(R.layout.chartting_item_user, null);
                baseChatHolder = new UserHolder(convertView);
                break;
        }
        baseChatHolder.tvChatMsgRobot.setText(message.getText());
        return convertView;
    }

}