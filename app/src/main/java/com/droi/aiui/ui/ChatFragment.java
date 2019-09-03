package com.droi.aiui.ui;


import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.droi.aiui.AiuiManager;
import com.droi.aiui.Interface.IOnParseListener;
import com.droi.aiui.R;
import com.droi.aiui.adapter.ChatAdapter;
import com.droi.aiui.bean.Message;
import com.droi.aiui.controler.SpeechControler;

import java.util.ArrayList;


/**
 * Created by cuixiaojun on 17-12-28.
 * 聊天主界面
 */
public class ChatFragment extends BaseFragment implements IOnParseListener{

    private final String TAG = "ChatFragment";
    private DroiAiuiMainActivity mActivity;
    //消息列表
    private ArrayList<Message> arrayListChat=new ArrayList<Message>();
    private ListView mListView;
    private LinearLayout mMessagePoint;
    private ChatAdapter mAdapter;
    private SpeechControler mSpeechControler;

    public ChatFragment() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_list;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void initView() {
        Log.d(TAG,"initView");
        mActivity.setOnParseListener(this);
        mListView = (ListView) view.findViewById(R.id.listview_chat);
        mMessagePoint = (LinearLayout) view.findViewById(R.id.message_point);
        if (arrayListChat.size() != 0) {
            mMessagePoint.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mMessagePoint.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        mAdapter = new ChatAdapter(mActivity, arrayListChat);
        mAdapter.registerDataSetObserver(mDataSetObserver);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void initData() {
        Log.d(TAG,"initData");
        mActivity = (DroiAiuiMainActivity) getActivity();
        mSpeechControler = mActivity.mAiuiManager.getSpeechControler();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(AiuiManager.getInstance() != null && mMessagePoint != null && mMessagePoint.getVisibility() == View.VISIBLE){
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    String speech = "主人,您好，我是小布精灵，有什么可以帮助您的呢？";
                    if(mSpeechControler != null){
                        mSpeechControler.setSpeechContent(speech);
                        mSpeechControler.startSpeechByType("ChatFragment");
                    }
                }
            }, 1000);
        }
    }

    /**
     * 获取消息数据
     * @param messages
     */
    public void getData(ArrayList<Message> messages) {
        Log.d(TAG,"getData--->messages.size = "+messages.size());
        for(int i=0;i<messages.size();i++){
            Message message = new Message();
            message.fromType = messages.get(i).fromType;
            message.msgType = messages.get(i).msgType;
            //判断消息是来自机器人，只朗读机器人说的话
            if(messages.get(i).fromType.equals(Message.FromType.ROBOT)){
                //开始朗读消息
                String speech = messages.get(i).getText();
                mSpeechControler.setSpeechContent(speech);
                if(!mSpeechControler.isSpeaking()){
                    mSpeechControler.startSpeechByType("ChatFragment");
                }
            }
            message.setText(messages.get(i).getText());
            arrayListChat.add(message);
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 为适配器设置数据源的改变
     */
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            //将会话消息向下移动
            mListView.smoothScrollToPosition(arrayListChat.size());
        }
    };

    @Override
    public void onParseListener(final ArrayList<Message> messages) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(messages.size() != 0){
                    mMessagePoint.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    //每次收到消息之后都需要刷新数据
                    mAdapter.notifyDataSetChanged();
                }
                getData(messages);
            }
        });
    }
}