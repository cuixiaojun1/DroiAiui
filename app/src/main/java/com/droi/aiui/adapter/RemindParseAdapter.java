package com.droi.aiui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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
        Log.d(TAG,"RemindParseAdapter========================>json = "+json);
        mRemindBean = JsonParserUtil.parseJsonObject(json,RemindBean.class);
        String result = returnResult();
        if(TextUtils.isEmpty(result)){
            result = "对不起，我没有听清楚您说的话，请再说一次！";
        }
        return result;
    }

    /**
     * 返回解析结果字串
     */
    private String returnResult(){
        String result;
        if(mRemindBean.getRc() == 0 || mRemindBean.getRc() == 3){
            result = handleRemindIntent();
        }else {
            result = getAnswer();
        }
        if(TextUtils.isEmpty(result)){
            result = "我不是很明白您的意思，您可以换种说法试试！";
        }
        return result;
    }

    /**
     * 获取提醒意图包含如下四种意图：
     *   CREATE     新建提醒
     *   VIEW       查看提醒
     *   CHANGE     更改提醒
     *   CANCEL     取消提醒
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
     *  处理提醒意图
     */
    private String handleRemindIntent(){
        String result = null;
        Log.d(TAG,"handleRemindIntent---->getRemindIntent = "+getRemindIntent());
        switch (getRemindIntent()){
            case "CREATE":
                result = handleRemind();
                break;
            case "VIEW":
                result =  "暂时还不支持查看提醒或闹钟！";
                break;
            case "CHANGE":
                result =  "暂时还不支持修改提醒或闹钟！";
                break;
            case "CANCEL":
                result =  "暂时还不支持取消提醒或闹钟！";
                break;
                default:
                    break;
        }
        return result;
    }

    /**
     * 设置闹钟
     */
    private String handleRemind(){
        String result = null;
        String remindContent = getRemindTypeOrContentOrRepeat("content");
        long remindTime = formateRemindTime(getRemindTimeJson());
        String repeatDate = getRemindTypeOrContentOrRepeat("repeat");
        Log.d(TAG,"answer = "+getAnswer()+",remindContent = "+remindContent+",remindTime = "+FunctionUtil.longToString(remindTime,FunctionUtil.TIME_FORMATE1)+",repeatDate = "+repeatDate);
        if (!TextUtils.isEmpty(remindContent)) {
            RemindInfo remindInfo = new RemindInfo();
            if (repeatDate != null) {
                if (repeatDate.indexOf("M") != -1) {//包含月份的重复,全部设置为单次提醒
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
            result = "对不起,您还没有设置提醒的内容,请重新设置!";
        }
        return result;
    }

    /**
     * 获取提醒的时间字串
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
     * 获取提醒相关的内容，包括提醒的内容，提醒的类型，提醒的重复时间
     * 获取提醒的类型，包括两种，一种为“reminder”，另外一种为“clock”，
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
     * 获取提醒的当前状态
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
     * 获取提醒的具体时间
     */
    private long formateRemindTime(String jsonTime){
        Log.d(TAG,"formateRemindTime---->jsonTime = "+jsonTime);
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
            Log.d(TAG,"没有具体的日期和提醒时间！");
        }
        return longTime;
    }

    /**
     * 格式化时间，将jsontime转换为longtime
     */
    private long formateTime(String jsonTime){
        long longTime = 0L;
        String[] stringTime;
        if(jsonTime != null && jsonTime.indexOf("T") != -1){
            stringTime = jsonTime.split("T");
            if(stringTime != null && stringTime.length == 2){
                if(stringTime[0] == null){
                    stringTime[0] = getCurrentDate();
                    Log.d(TAG,"对不起时间格式有误！没有具体的日期！");
                }
                if(stringTime[1] == null){
                    stringTime[1] = getCurrentTime();
                    Log.d(TAG,"对不起时间格式有误！没有具体的时间！");
                }
                longTime = FunctionUtil.stringToLong((stringTime[0]+" "+stringTime[1]),FunctionUtil.TIME_FORMATE);
            }else{
                Log.d(TAG,"对不起时间格式有误！没有具体的时间！");
            }
        }else{
            Log.d(TAG,"对不起时间格式有误！没有分隔符！");
        }
        return longTime;
    }


    /**
     * 获取当天日期
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
     * 获取当前时间
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
     * 获取服务器返回的文本结果
     */
    private String getAnswer(){
        String answer1 = null;
        RemindBean answer;
        if(mRemindBean != null){
            answer = mRemindBean.getAnswer();
            if(answer != null){
                answer1 = answer.getText();
            }else{
                answer1 = "我不是很明白您的意思，您可以换种说法试试！";
            }
        }
        return answer1;
    }

    /**
     * @param remindInfo      提醒信息，包括提醒的内容，提醒的时间，提醒的重复周期
     */
    public String setAlarm(RemindInfo remindInfo) {
        String result = null;
        String repeatDate = remindInfo.getRepeatDate();
        int repeatType = AlarmManagerUtil.getRepeatType(repeatDate);
        long tempTime = remindInfo.getTime();
        Log.d(TAG,"setAlarm--->remindInfo = "+remindInfo.toString()+",repeatType = "+repeatType);
        switch(repeatType){
            case AlarmManagerUtil.REPEAT_TYPE_ONETIME:
                long intervalMillis = 0;
                long remindTime = AlarmManagerUtil.calculateAlarmTime(0,tempTime);
                remindInfo.setTime(remindTime);
                if(mRemindDBHelp.insert(remindInfo)){
                    result = "已为您更新该提醒,您可以去提醒界面查看具体信息！";
                }else{
                    result = getAnswer()+",您可以去提醒界面查看已设置的提醒！";
                }
                AlarmManagerUtil.setRealAlarm(mContext,remindInfo,intervalMillis,0);
                break;
            case AlarmManagerUtil.REPEAT_TYPE_EVERYDAY:
                long intervalMillis1 = 24 * 3600 * 1000;
                long remindTime1 = AlarmManagerUtil.calculateAlarmTime(0,tempTime);
                remindInfo.setTime(remindTime1);
                if(mRemindDBHelp.insert(remindInfo)){
                    result = "已经为您更新该提醒,您可以去提醒界面查看具体信息！";
                }else{
                    result = getAnswer()+",您可以去提醒界面查看已设置的提醒！";
                }
                AlarmManagerUtil.setRealAlarm(mContext,remindInfo,intervalMillis1,0);
                break;
            case AlarmManagerUtil.REPEAT_TYPE_EVERYWEEK:
                long intervalMillis2 = 24 * 3600 * 1000 * 7;
                long remindTime2 = AlarmManagerUtil.calculateAlarmTime(0,tempTime);
                remindInfo.setTime(remindTime2);
                if(mRemindDBHelp.insert(remindInfo)){
                    result = "已经为您更新该提醒,您可以去提醒界面查看具体信息！";
                }else{
                    result = getAnswer()+",您可以去提醒界面查看已设置的提醒！";
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
                        result = "已经为您更新该提醒,您可以去提醒界面查看具体信息！";
                    }else{
                        result = getAnswer()+",您可以去提醒界面查看已设置的提醒！";
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
                        result = "已经为您更新该提醒,您可以去提醒界面查看具体信息！";
                    }else{
                        result = getAnswer()+",您可以去提醒界面查看已设置的提醒！";
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
                        result = "已经为您更新该提醒,您可以去提醒界面查看具体信息！";
                    }else{
                        result = getAnswer()+",您可以去提醒界面查看已设置的提醒！";
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