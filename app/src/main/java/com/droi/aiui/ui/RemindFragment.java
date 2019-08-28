package com.droi.aiui.ui;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cuixiaojun on 17-12-28.
 * �������ý���
 */

public class RemindFragment extends BaseFragment implements CappuRemindAdapter.IonItemClickListener,
        IonDialogRemindInfoClickListen, IOnRemindCancelClickListener {

    private final String TAG = "RemindFragment";
    private RemindListView mSingleListView,mRepeatListView;
    private LinearLayout mMessage_point_remind;
    private TextView tv_singleRemind,tv_repeatRemind;

    private CappuRemindAdapter mSingleRemindAdapter,mRepeatRemindAdapter;
    private RemindInfo currentRemindInfo;
    private DialogRemindInfo remindinfoDialig;
    private RemindDBHelp mRemindDBHelp;
    private CappuAiuiActivity cappuAiuiActivity;
    private SharedPreferences mSharedPreferences;

    private Map<String,List<RemindInfo>> singleMap = new HashMap<String, List<RemindInfo>>();
    private List<String> singleKeys = new ArrayList<String>();

    private Map<String,List<RemindInfo>> repeatMap = new HashMap<String, List<RemindInfo>>();
    private List<String> repeatKeys= new ArrayList<String>();



    @Override
    public int getLayoutId() {
        return R.layout.fragment_remind;
    }

    @Override
    public void initView() {
        cappuAiuiActivity = (CappuAiuiActivity) getActivity();
        mSingleListView = (RemindListView) view.findViewById(R.id.single_listview);
        mRepeatListView = (RemindListView) view.findViewById(R.id.repeat_listview);
        mMessage_point_remind = (LinearLayout) view.findViewById(R.id.message_point_remind);
        tv_singleRemind = (TextView) view.findViewById(R.id.single_reminder);
        tv_repeatRemind = (TextView) view.findViewById(R.id.repeat_reminder);

        mSingleRemindAdapter = new CappuRemindAdapter(getActivity(),singleKeys, singleMap);
        mSingleRemindAdapter.setIonItemClickListener(this);

        mRepeatRemindAdapter = new CappuRemindAdapter(getActivity(),repeatKeys, repeatMap);
        mRepeatRemindAdapter.setIonItemClickListener(this);

        cappuAiuiActivity.setOnRemindCancelClickListener(this);

        mSingleListView.setAdapter(mSingleRemindAdapter);
        mRepeatListView.setAdapter(mRepeatRemindAdapter);

        refreshView();
    }

    @Override
    public void initData(){
        mRemindDBHelp = RemindDBHelp.getInstance(getActivity().getApplicationContext());
        getData();
    }
    public void getData() {
        mSharedPreferences = getActivity().getSharedPreferences(DownloadService.FILE_CAPPU_AIUI, MODE_PRIVATE);
        singleMap.clear();
        singleKeys.clear();
        repeatMap.clear();
        repeatKeys.clear();
        //�����ݿ��ȡ���е���������
        List<RemindInfo> remindInfos = mRemindDBHelp.queryRemindAll();
        JeffLog.d(TAG,"getData--->remindInfos = "+remindInfos.size());
        if (remindInfos != null && remindInfos.size() > 0) {
            for (int i = 0; i < remindInfos.size(); i++) {
                //ˢ�����ݵ�ʱ����Ҫɾ���������ӣ����Ҹ����ѵ�ʱ���ڵ�ǰʱ��֮ǰ��
                if(remindInfos.get(i).getTime() <= System.currentTimeMillis() && remindInfos.get(i).getRepeatDate().equals("ONETIME")){
                    if(mRemindDBHelp != null){
                        mRemindDBHelp.delete(remindInfos.get(i));
                    }
                }
                String day = getDay(remindInfos.get(i).getTime());
                //�����еĵ�������������ӵ����������������
                if(remindInfos.get(i).getRepeatDate().equals("ONETIME")){
                    if (singleMap.size()>0 && singleMap.get(day) != null ) {
                        singleMap.get(day).add(remindInfos.get(i));
                    } else {
                        List<RemindInfo> list=new ArrayList<RemindInfo>();
                        singleKeys.add(day);
                        list.add(remindInfos.get(i));
                        singleMap.put(day,list);
                    }
                }else{//�����е��ظ�������ӵ��ظ������������
                    if (repeatMap.size()>0 && repeatMap.get(day) != null ) {
                        repeatMap.get(day).add(remindInfos.get(i));
                    } else {
                        List<RemindInfo> list=new ArrayList<RemindInfo>();
                        repeatKeys.add(day);
                        list.add(remindInfos.get(i));
                        repeatMap.put(day,list);
                    }
                }
            }
            //�Ե����������ݽ�������
            Iterator<String> iter = singleMap.keySet().iterator();
            while (iter.hasNext()) {
                List<RemindInfo> list =  singleMap.get(iter.next());
                //��ʱ������������ѵľ���ʱ�䣩
                Collections.sort(list,timeCompare);
            }
            //�����ڽ�������(���ѵ�����)
            Collections.sort(singleKeys,dateCompare);
            //���ظ��������ݽ�������
            Iterator<String> iter1 = repeatMap.keySet().iterator();
            while (iter1.hasNext()) {
                List<RemindInfo> list =  repeatMap.get(iter1.next());
                //��ʱ������������ѵľ���ʱ�䣩
                Collections.sort(list,timeCompare);
            }
            //�����ڽ�������(���ѵ�����)
            Collections.sort(repeatKeys,dateCompare);
        }
    }

    private String getDay(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM" + "��" + "dd" +"��");
        return format.format(new Date(time));
    }

    /**
     * ����ʱ������ѽ�������
     */
    private Comparator timeCompare=new Comparator<RemindInfo>(){
        public int compare(RemindInfo arg0, RemindInfo arg1) {
            return (int) (arg0.getTime()-arg1.getTime());
        }
    };

    /**
     * �������ڶ����ѽ�������
     */
    private Comparator dateCompare=new Comparator<String>(){
        public int compare(String arg0, String arg1) {
            return arg0.compareTo(arg1);
        }
    };

    /**
     * item���
     * @param info
     */
    @Override
    public void onItemClickListen(RemindInfo info) {
        currentRemindInfo = info;
        JeffLog.d(TAG,"onItemClickListen--->remindInfo = "+info.toString());
        if (remindinfoDialig == null) {
            remindinfoDialig = new DialogRemindInfo(getActivity());
            remindinfoDialig.setDialogRemindInfoClickListen(this);
        }
        //��ʾ������ϸ����Ϣ
        remindinfoDialig.showCurrentRemindInfo(currentRemindInfo);
    }

    /**
     * ɾ����ǰ��remindinfo
     */
    @Override
    public void onRemindInfoDel() {
        JeffLog.d(TAG,"onRemindInfoDel");
        //ɾ�����ݿ�
        mRemindDBHelp.delete(currentRemindInfo);
        //ȡ������
        JeffLog.d(TAG,"ȡ�����ӣ�currentRemindInfo = "+currentRemindInfo.toString());
        AlarmManagerUtil.cancelAlarm(cappuAiuiActivity,currentRemindInfo.getTime()+currentRemindInfo.getRepeatDate());
        //ˢ�½���
        refreshView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //���ѽ�������л���ʱ�����Խ���
        if(!hidden){
            refreshView();
        }
    }

    @Override
    public void onRemindCancel() {
        //������ʱ�䵽��ɾ�����ӣ����ص����ѽ����ʱ��Ҳ��Ҫˢ�½���
        refreshView();
    }

    /**
     * ˢ�½���
     */
    private void refreshView(){
        getData();
        JeffLog.d(TAG,"refreshView---->�������ѣ�size = "+singleMap.size()+",�ظ����ѣ�size = "+repeatMap.size());
        mSingleRemindAdapter.notifyDataSetChanged();
        mRepeatRemindAdapter.notifyDataSetChanged();
        if(singleKeys.size() != 0){
            mSingleListView.setVisibility(View.VISIBLE);
            tv_singleRemind.setVisibility(View.VISIBLE);
        }else{
            mSingleListView.setVisibility(View.GONE);
            tv_singleRemind.setVisibility(View.GONE);
        }
        if(repeatKeys.size() != 0){
            mRepeatListView.setVisibility(View.VISIBLE);
            tv_repeatRemind.setVisibility(View.VISIBLE);
        }else{
            mRepeatListView.setVisibility(View.GONE);
            tv_repeatRemind.setVisibility(View.GONE);
        }
        if((singleKeys.size()+repeatKeys.size()) != 0){
            mMessage_point_remind.setVisibility(View.GONE);
        }else{
            mMessage_point_remind.setVisibility(View.VISIBLE);
        }
    }

}