package com.droi.aiui.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.droi.aiui.bean.RemindBean;
import com.droi.aiui.bean.RemindInfo;
import com.droi.aiui.dao.RemindDBHelp;
import com.droi.aiui.util.AlarmManagerUtil;
import com.droi.aiui.util.FunctionUtil;
import com.droi.aiui.util.JsonParserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by cuixiaojun on 18-1-13.
 */

public class RemindParseAdapter extends BaseParseAdapter {

    private final String TAG  = "RemindParseAdapter";
    private Context mContext;
    private RemindBean mRemindBean;
    private RemindDBHelp mRemindDBHelp;

    public RemindParseAdapter(Context context) {
        mContext = context;
        mRemindDBHelp = RemindDBHelp.getInstance(context.getApplicationContext());
    }

    @Override
    public String getSemanticResultText(String json) {
        mRemindBean = JsonParserUtil.parseJsonObject(json, RemindBean.class);
        String result = returnResult();
        if(TextUtils.isEmpty(result)){
            result = "�Բ�����û���������˵�Ļ�������˵һ�Σ�";
        }
        return result;
    }

    /**
     * ���ؽ�������ִ�
     */
    private String returnResult(){
        String result;
        if(mRemindBean.getRc() == 0 || mRemindBean.getRc() == 3){
            result = handleRemindIntent();
        }else {
            result = getAnswer();
        }
        if(TextUtils.isEmpty(result)){
            result = "�Ҳ��Ǻ�����������˼�������Ի���˵�����ԣ�";
        }
        return result;
    }

    /**
     * ��ȡ������ͼ��������������ͼ��
     *   CREATE     �½�����
     *   VIEW       �鿴����
     *   CHANGE     ��������
     *   CANCEL     ȡ������
     */
    private String getRemindIntent(){
        List<RemindBean.SemanticBean> semantic = mRemindBean.getSemantic();
        if(semantic != null && semantic.size() != 0){
            for (int i = 0; i < semantic.size(); i++) {
                return semantic.get(i).getIntent();
            }
        }
        return null;
    }

    /**
     *  ����������ͼ
     */
    private String handleRemindIntent(){
        String result = null;
        switch (getRemindIntent()){
            case "CREATE":
                result = handleRemind();
                break;
            case "VIEW":
                result =  "��ʱ����֧�ֲ鿴���ѻ����ӣ�";
                break;
            case "CHANGE":
                result =  "��ʱ����֧���޸����ѻ����ӣ�";
                break;
            case "CANCEL":
                result =  "��ʱ����֧��ȡ�����ѻ����ӣ�";
                break;
                default:
                    break;
        }
        return result;
    }

    /**
     * ��������
     */
    private String handleRemind(){
        String result = null;
        String remindContent = getRemindTypeOrContentOrRepeat("content");
        long remindTime = formateRemindTime(getRemindTimeJson());
        String repeatDate = getRemindTypeOrContentOrRepeat("repeat");
        if (!TextUtils.isEmpty(remindContent)) {
            RemindInfo remindInfo = new RemindInfo();
            if (repeatDate != null) {
                if (repeatDate.indexOf("M") != -1) {//�����·ݵ��ظ�,ȫ������Ϊ��������
                    remindInfo.setRepeatDate("ONETIME");
                } else {
                    remindInfo.setRepeatDate(repeatDate);
                }
            } else {
                remindInfo.setRepeatDate("ONETIME");
            }
            remindInfo.setContent(remindContent);
            remindInfo.setTime(remindTime);
            if(remindTime > System.currentTimeMillis() && getRemindState() != null && (getRemindState().equals("reminderFinished") || getRemindState().equals("clockFinished"))){
                result = setAlarm(remindInfo);
            }else{
                result = getAnswer();
            }
        } else {
            result = "�Բ���,����û���������ѵ�����,����������!";
        }
        return result;
    }

    /**
     * ��ȡ���ѵ�ʱ���ִ�
     */
    private String getRemindTimeJson(){
        List<RemindBean.SemanticBean> semantic = mRemindBean.getSemantic();
        List<RemindBean.SemanticBean.SlotsBean> slots;
        if(semantic != null && semantic.size() != 0){
            for (int i = 0; i < semantic.size(); i++) {
                slots = semantic.get(i).getSlots();
                for (int j = 0; j < slots.size(); j++) {
                    String name = slots.get(j).getName();
                    String normValue = slots.get(j).getNormValue();
                    if(name.equals("datetime")){
                        return normValue;
                    }
                }
            }
        }
        return null;
    }

    /**
     * ��ȡ������ص����ݣ��������ѵ����ݣ����ѵ����ͣ����ѵ��ظ�ʱ��
     * ��ȡ���ѵ����ͣ��������֣�һ��Ϊ��reminder��������һ��Ϊ��clock����
     */
    private String getRemindTypeOrContentOrRepeat(String type){
        String temp = null;
        List<RemindBean.SemanticBean> semantic = null;
        if(mRemindBean != null){
            semantic = mRemindBean.getSemantic();
        }
        if(semantic != null && semantic.size() != 0){
            for (int i = 0; i < semantic.size(); i++) {
                RemindBean.SemanticBean semanticBean = semantic.get(i);
                List<RemindBean.SemanticBean.SlotsBean> slots = semanticBean.getSlots();
                for (int j = 0; j <slots.size(); j++) {
                    RemindBean.SemanticBean.SlotsBean slotsBean = slots.get(j);
                    if(type.equals(slotsBean.getName())){
                        temp = slotsBean.getValue();
                    }
                }
            }
        }
        return temp;
    }

    /**
     * ��ȡ���ѵĵ�ǰ״̬
     */
    private String getRemindState(){
        String state = null;
        RemindBean.UsedStateBean used_state = mRemindBean.getUsed_state();
        if(used_state != null){
            state = used_state.getState();
        }
        return state;
    }
    /**
     * ��ȡ���ѵľ���ʱ��
     */
    private long formateRemindTime(String jsonTime){
        long longTime = 0L;
        if(jsonTime != null){
            try {
                JSONObject jsonObject = new JSONObject(jsonTime);
                String timeString = jsonObject.optString("suggestDatetime");
                String beginTime;
                if(timeString != null && timeString.indexOf("/") != -1){
                    beginTime = timeString.split("/")[0];
                    if(beginTime != null){
                        longTime = formateTime(beginTime);
                    }
                }else{
                    longTime = formateTime(timeString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
        }
        return longTime;
    }

    /**
     * ��ʽ��ʱ�䣬��jsontimeת��Ϊlongtime
     */
    private long formateTime(String jsonTime){
        long longTime = 0L;
        String[] stringTime;
        if(jsonTime != null && jsonTime.indexOf("T") != -1){
            stringTime = jsonTime.split("T");
            if(stringTime != null && stringTime.length == 2){
                if(stringTime[0] == null){
                    stringTime[0] = getCurrentDate();
                }
                if(stringTime[1] == null){
                    stringTime[1] = getCurrentTime();
                }
                longTime = FunctionUtil.stringToLong((stringTime[0]+" "+stringTime[1]), FunctionUtil.TIME_FORMATE);
            }else{
            }
        }else{
        }
        return longTime;
    }


    /**
     * ��ȡ��������
     */
    private String getCurrentDate(){
        String data = null;
        String time1;
        long time = System.currentTimeMillis();
        time1 = FunctionUtil.longToString(time,FunctionUtil.TIME_FORMATE);
        String[] time2 = time1.split(" ");
        if(time2[0] != null){
            data = time2[0];
        }
        return data;
    }

    /**
     * ��ȡ��ǰʱ��
     */
    private String getCurrentTime(){
        String time = null;
        String time1;
        long longTime = System.currentTimeMillis();
        time1 = FunctionUtil.longToString(longTime,FunctionUtil.TIME_FORMATE);
        String[] time2 = time1.split(" ");
        if(time2[1] != null){
            time = time2[1];
        }
        return time;
    }

    /**
     * ��ȡ���������ص��ı����
     */
    private String getAnswer(){
        String answer1 = null;
        RemindBean answer;
        if(mRemindBean != null){
            answer = mRemindBean.getAnswer();
            if(answer != null){
                answer1 = answer.getText();
            }else{
                answer1 = "�Ҳ��Ǻ�����������˼�������Ի���˵�����ԣ�";
            }
        }
        return answer1;
    }

    /**
     * @param remindInfo      ������Ϣ���������ѵ����ݣ����ѵ�ʱ�䣬���ѵ��ظ�����
     */
    public String setAlarm(RemindInfo remindInfo) {
        String result = null;
        String repeatDate = remindInfo.getRepeatDate();
        int repeatType = AlarmManagerUtil.getRepeatType(repeatDate);
        long tempTime = remindInfo.getTime();
        switch(repeatType){
            case AlarmManagerUtil.REPEAT_TYPE_ONETIME:
                long intervalMillis = 0;
                long remindTime = AlarmManagerUtil.calculateAlarmTime(0,tempTime);
                remindInfo.setTime(remindTime);
                if(mRemindDBHelp.insert(remindInfo)){
                    result = "��Ϊ�����¸�����,������ȥ���ѽ���鿴������Ϣ��";
                }else{
                    result = getAnswer()+",������ȥ���ѽ���鿴�����õ����ѣ�";
                }
                AlarmManagerUtil.setRealAlarm(mContext,remindInfo,intervalMillis,0);
                break;
            case AlarmManagerUtil.REPEAT_TYPE_EVERYDAY:
                long intervalMillis1 = 24 * 3600 * 1000;
                long remindTime1 = AlarmManagerUtil.calculateAlarmTime(0,tempTime);
                remindInfo.setTime(remindTime1);
                if(mRemindDBHelp.insert(remindInfo)){
                    result = "�Ѿ�Ϊ�����¸�����,������ȥ���ѽ���鿴������Ϣ��";
                }else{
                    result = getAnswer()+",������ȥ���ѽ���鿴�����õ����ѣ�";
                }
                AlarmManagerUtil.setRealAlarm(mContext,remindInfo,intervalMillis1,0);
                break;
            case AlarmManagerUtil.REPEAT_TYPE_EVERYWEEK:
                long intervalMillis2 = 24 * 3600 * 1000 * 7;
                long remindTime2 = AlarmManagerUtil.calculateAlarmTime(0,tempTime);
                remindInfo.setTime(remindTime2);
                if(mRemindDBHelp.insert(remindInfo)){
                    result = "�Ѿ�Ϊ�����¸�����,������ȥ���ѽ���鿴������Ϣ��";
                }else{
                    result = getAnswer()+",������ȥ���ѽ���鿴�����õ����ѣ�";
                }
                AlarmManagerUtil.setRealAlarm(mContext,remindInfo,intervalMillis2,0);
                break;
            case AlarmManagerUtil.REPEAT_TYPE_WEEKEND:
                long intervalMillis3 = 24 * 3600 * 1000 * 7;
                List<Integer> week = AlarmManagerUtil.getRepeatWeek(6,7);
                for (int i = 0; i < week.size(); i++) {
                    long remindTime3 = AlarmManagerUtil.calculateAlarmTime(week.get(i),tempTime);
                    remindInfo.setTime(remindTime3);
                    if(mRemindDBHelp.insert(remindInfo)){
                        result = "�Ѿ�Ϊ�����¸�����,������ȥ���ѽ���鿴������Ϣ��";
                    }else{
                        result = getAnswer()+",������ȥ���ѽ���鿴�����õ����ѣ�";
                    }
                    AlarmManagerUtil.setRealAlarm(mContext,remindInfo,intervalMillis3,i);
                }
                break;
            case AlarmManagerUtil.REPEAT_TYPE_WORKDAY:
                long intervalMillis4 = 24 * 3600 * 1000 * 7;
                List<Integer> week1 = AlarmManagerUtil.getRepeatWeek(1,5);
                for (int i = 0; i < week1.size(); i++) {
                    long remindTime4 = AlarmManagerUtil.calculateAlarmTime(week1.get(i),tempTime);
                    remindInfo.setTime(remindTime4);
                    if(mRemindDBHelp.insert(remindInfo)){
                        result = "�Ѿ�Ϊ�����¸�����,������ȥ���ѽ���鿴������Ϣ��";
                    }else{
                        result = getAnswer()+",������ȥ���ѽ���鿴�����õ����ѣ�";
                    }
                    AlarmManagerUtil.setRealAlarm(mContext,remindInfo,intervalMillis4,i);
                }
                break;
            case AlarmManagerUtil.REPEAT_TYPE_MOREWEEKS:
                long intervalMillis5 = 24 * 3600 * 1000 * 7;
                int begin = AlarmManagerUtil.getWeekStart(remindInfo.getRepeatDate(),"begin");
                int end   = AlarmManagerUtil.getWeekStart(remindInfo.getRepeatDate(),"end");
                List<Integer> week2 = AlarmManagerUtil.getRepeatWeek(begin,end);
                for (int i = 0; i < week2.size(); i++) {
                    long remindTime5 = AlarmManagerUtil.calculateAlarmTime(week2.get(i),tempTime);
                    remindInfo.setTime(remindTime5);
                    if(mRemindDBHelp.insert(remindInfo)){
                        result = "�Ѿ�Ϊ�����¸�����,������ȥ���ѽ���鿴������Ϣ��";
                    }else{
                        result = getAnswer()+",������ȥ���ѽ���鿴�����õ����ѣ�";
                    }
                    AlarmManagerUtil.setRealAlarm(mContext,remindInfo,intervalMillis5,i);
                }
                break;
            default:
                break;
        }
        return result;
    }
}