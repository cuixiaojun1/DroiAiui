package com.droi.aiui.ui;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droi.aiui.Interface.IOnRemindCancelClickListener;
import com.droi.aiui.Interface.IonDialogRemindInfoClickListen;
import com.droi.aiui.R;
import com.droi.aiui.adapter.CappuRemindAdapter;
import com.droi.aiui.apkupdate.DownloadService;
import com.droi.aiui.bean.RemindInfo;
import com.droi.aiui.dao.RemindDBHelp;
import com.droi.aiui.util.AlarmManagerUtil;
import com.droi.aiui.widget.DialogRemindInfo;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cuixiaojun on 17-12-28.
 * 提醒设置界面
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
    private DroiAiuiMainActivity cappuAiuiActivity;
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
        cappuAiuiActivity = (DroiAiuiMainActivity) getActivity();
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
        //从数据库读取所有的提醒数据
        List<RemindInfo> remindInfos = mRemindDBHelp.queryRemindAll();
        Log.d(TAG,"getData--->remindInfos = "+remindInfos.size());
        if (remindInfos != null && remindInfos.size() > 0) {
            for (int i = 0; i < remindInfos.size(); i++) {
                //刷新数据的时候需要删除单次闹钟，并且该提醒的时间在当前时间之前的
                if(remindInfos.get(i).getTime() <= System.currentTimeMillis() && remindInfos.get(i).getRepeatDate().equals("ONETIME")){
                    if(mRemindDBHelp != null){
                        mRemindDBHelp.delete(remindInfos.get(i));
                    }
                }
                String day = getDay(remindInfos.get(i).getTime());
                //将所有的单次提醒数据添加到单次提醒数据里边
                if(remindInfos.get(i).getRepeatDate().equals("ONETIME")){
                    if (singleMap.size()>0 && singleMap.get(day) != null ) {
                        singleMap.get(day).add(remindInfos.get(i));
                    } else {
                        List<RemindInfo> list=new ArrayList<RemindInfo>();
                        singleKeys.add(day);
                        list.add(remindInfos.get(i));
                        singleMap.put(day,list);
                    }
                }else{//将所有的重复提醒添加到重复提醒数据里边
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
            //对单次提醒数据进行排序
            Iterator<String> iter = singleMap.keySet().iterator();
            while (iter.hasNext()) {
                List<RemindInfo> list =  singleMap.get(iter.next());
                //对时间进行排序（提醒的具体时间）
                Collections.sort(list,timeCompare);
            }
            //对日期进行排序(提醒的日期)
            Collections.sort(singleKeys,dateCompare);
            //对重复提醒数据进行排序
            Iterator<String> iter1 = repeatMap.keySet().iterator();
            while (iter1.hasNext()) {
                List<RemindInfo> list =  repeatMap.get(iter1.next());
                //对时间进行排序（提醒的具体时间）
                Collections.sort(list,timeCompare);
            }
            //对日期进行排序(提醒的日期)
            Collections.sort(repeatKeys,dateCompare);
        }
    }

    private String getDay(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM" + "月" + "dd" +"日");
        return format.format(new Date(time));
    }

    /**
     * 根据时间对提醒进行排序
     */
    private Comparator timeCompare=new Comparator<RemindInfo>(){
        public int compare(RemindInfo arg0, RemindInfo arg1) {
            return (int) (arg0.getTime()-arg1.getTime());
        }
    };

    /**
     * 根据日期对提醒进行排序
     */
    private Comparator dateCompare=new Comparator<String>(){
        public int compare(String arg0, String arg1) {
            return arg0.compareTo(arg1);
        }
    };

    /**
     * item点击
     * @param info
     */
    @Override
    public void onItemClickListen(RemindInfo info) {
        currentRemindInfo = info;
        Log.d(TAG,"onItemClickListen--->remindInfo = "+info.toString());
        if (remindinfoDialig == null) {
            remindinfoDialig = new DialogRemindInfo(getActivity());
            remindinfoDialig.setDialogRemindInfoClickListen(this);
        }
        //显示提醒详细的信息
        remindinfoDialig.showCurrentRemindInfo(currentRemindInfo);
    }

    /**
     * 删除当前的remindinfo
     */
    @Override
    public void onRemindInfoDel() {
        Log.d(TAG,"onRemindInfoDel");
        //删除数据库
        mRemindDBHelp.delete(currentRemindInfo);
        //取消提醒
        Log.d(TAG,"取消闹钟：currentRemindInfo = "+currentRemindInfo.toString());
        AlarmManagerUtil.cancelAlarm(cappuAiuiActivity,currentRemindInfo.getTime()+currentRemindInfo.getRepeatDate());
        //刷新界面
        refreshView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //提醒界面进行切换的时候数显界面
        if(!hidden){
            refreshView();
        }
    }

    @Override
    public void onRemindCancel() {
        //当闹钟时间到，删除闹钟，返回到提醒界面的时候也需要刷新界面
        refreshView();
    }

    /**
     * 刷新界面
     */
    private void refreshView(){
        getData();
        Log.d(TAG,"refreshView---->单次提醒：size = "+singleMap.size()+",重复提醒：size = "+repeatMap.size());
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