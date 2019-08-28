package com.droi.aiui.ui;


import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.droi.aiui.R;
import com.droi.aiui.adapter.CappuHelpListAdapter;
import com.droi.aiui.bean.AnimationState;
import com.droi.aiui.bean.CappuHelpListItemData;
import com.droi.aiui.controler.SpeechControler;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HelpFragment extends BaseFragment implements CappuHelpListAdapter.InnerItemOnclickListener {

    private final String TAG = "HelpFragment";
    private DroiAiuiMainActivity mActivity;
    private String[] helpListItemTitles = {"������", "��绰", "��Ӧ��", "������", "���ֺ�", "������", "������", "������" ,"������"};
    private String[] helpListItemContents1 = {"�����������ô��?", "��绰������",  "������Ϣ", "��������3�������ҳ�ҩ", "���æ��С����", "������Ļ����",  "��������", "����������죿" ,"�������׸�"};
    private String[] helpListItemContents2 = {"���������������ô��?", "����10086",  "��΢��", "��������9�����������", "���æ��������", "������Ļ����",  "��������", "���괺���Ǽ��ţ�","�������������"};

    private List<CappuHelpListItemData> helpListItemGetData;
    private final int MSG_PLAY_ANIMATION = 0;
    private final int MSG_STOP_ANIMATION = 1;

    private List<AnimationState> animationStates ;

    private CappuHelpListAdapter listViewAdapter;
    private SpeechControler mSpeechControler;

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_PLAY_ANIMATION:
                    startAnimation(msg.arg1,msg.arg2);
                    break;
                case MSG_STOP_ANIMATION:
                    stopAnimation();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    public HelpFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.cappu_help_activity;
    }

    @Override
    public void initView() {
        ListView listview = (ListView) view.findViewById(R.id.help_listView);
         listViewAdapter = new CappuHelpListAdapter(mActivity, helpListItemGetData,animationStates);
        listViewAdapter.setOnInnerItemOnClickListener(this);
        listview.setAdapter(listViewAdapter);
    }

    @Override
    public void initData() {
        mActivity = (DroiAiuiMainActivity) getActivity();
        if(mActivity != null  && mActivity.mAiuiManager != null){
            mSpeechControler = mActivity.mAiuiManager.getSpeechControler();
        }
        helpListItemGetData = new ArrayList<CappuHelpListItemData>();
        animationStates = new ArrayList<AnimationState>();

        for (int i = 0; i < helpListItemTitles.length; i++) {
            animationStates.add(new AnimationState());
            CappuHelpListItemData data = new CappuHelpListItemData();
            data.setTitle(helpListItemTitles[i]);
            data.setContent1(helpListItemContents1[i]);
            data.setContent2(helpListItemContents2[i]);
            helpListItemGetData.add(data);
        }
    }

    @Override
    public void itemClick(View v) {
        switch (v.getId()) {
            case R.id.linear_one:
                handler.removeMessages(MSG_PLAY_ANIMATION);
                final Integer positon1 = (Integer) v.getTag();
                Message message1 = new Message();
                message1.what = MSG_PLAY_ANIMATION;
                message1.arg1 = positon1;
                message1.arg2 = R.id.linear_one;
                handler.sendMessageDelayed(message1,200);
                handler.removeMessages(MSG_STOP_ANIMATION);
                handler.sendEmptyMessageDelayed(MSG_STOP_ANIMATION,3000);
                break;
            case R.id.linear_two:
                handler.removeMessages(MSG_PLAY_ANIMATION);
                handler.removeMessages(MSG_STOP_ANIMATION);
                final Integer positon2= (Integer) v.getTag();
                Message message2 = new Message();
                message2.what = MSG_PLAY_ANIMATION;
                message2.arg1 = positon2;
                message2.arg2 = R.id.linear_two;
                handler.sendMessageDelayed(message2,200);
                handler.sendEmptyMessageDelayed(MSG_STOP_ANIMATION,3000);
                break;
            default:
                break;
        }
    }

    //��ʼ���Ŷ���
    private void startAnimation(final int position, int id){
        for (int i = 0; i < animationStates.size(); i++) {
            animationStates.get(i).setOne_state(false);
            animationStates.get(i).setTwo_state(false);
        }
        if(id == R.id.linear_one){
            animationStates.get(position).setOne_state(true);
            String speech = helpListItemGetData.get(position).getContent1();
            if(mSpeechControler != null){
                mSpeechControler.setSpeechContent(speech);
                mSpeechControler.startSpeechByType("HelpFragment");
            }
        }else if(id == R.id.linear_two){
            animationStates.get(position).setTwo_state(true);
            String speech = helpListItemGetData.get(position).getContent2();
            if(mSpeechControler != null){
                mSpeechControler.setSpeechContent(speech);
                mSpeechControler.startSpeechByType("HelpFragment");
            }
        }
        listViewAdapter.notifyDataSetChanged();
    }

    //ֹͣ���Ŷ���
    private void stopAnimation() {
        for (int i = 0; i < animationStates.size(); i++) {
            animationStates.get(i).setOne_state(false);
            animationStates.get(i).setTwo_state(false);
        }
        listViewAdapter.notifyDataSetChanged();
    }
}