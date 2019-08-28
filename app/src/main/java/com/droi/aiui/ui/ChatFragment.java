package com.droi.aiui.ui;


import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
 * ����������
 */
public class ChatFragment extends BaseFragment implements IOnParseListener {

    private final String TAG = "ChatFragment";
    private DroiAiuiMainActivity mActivity;
    //��Ϣ�б�
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
                    String speech = "����,���ã�����С�����飬��ʲô���԰��������أ�";
                    if(mSpeechControler != null){
                        mSpeechControler.setSpeechContent(speech);
                        mSpeechControler.startSpeechByType("ChatFragment");
                    }
                }
            }, 1000);
        }
    }

    /**
     * ��ȡ��Ϣ����
     * @param messages
     */
    public void getData(ArrayList<Message> messages) {
        for(int i=0;i<messages.size();i++){
            Message message = new Message();
            message.fromType = messages.get(i).fromType;
            message.msgType = messages.get(i).msgType;
            //�ж���Ϣ�����Ի����ˣ�ֻ�ʶ�������˵�Ļ�
            if(messages.get(i).fromType.equals(Message.FromType.ROBOT)){
                //��ʼ�ʶ���Ϣ
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
     * Ϊ��������������Դ�ĸı�
     */
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            //���Ự��Ϣ�����ƶ�
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
                    //ÿ���յ���Ϣ֮����Ҫˢ������
                    mAdapter.notifyDataSetChanged();
                }
                getData(messages);
            }
        });
    }
}