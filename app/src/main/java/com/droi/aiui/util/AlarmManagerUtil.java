package com.droi.aiui.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.droi.aiui.bean.RemindInfo;
import com.droi.aiui.reciver.RemindReciver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cuixiaojun on 18-3-24.
 */

public class AlarmManagerUtil {

    private static final String TAG = "AlarmManagerUtil";
    public static final int REPEAT_TYPE_ONETIME = 1;
    public static final int REPEAT_TYPE_EVERYDAY = 2;
    public static final int REPEAT_TYPE_EVERYWEEK = 3;
    public static final int REPEAT_TYPE_MOREWEEKS = 4;
    public static final int REPEAT_TYPE_WORKDAY = 5;
    public static final int REPEAT_TYPE_WEEKEND = 6;

    /**
     * 取消对应的闹钟
     * @param context
     * @param action
     */
    public static void cancelAlarm(Context context, String action) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.d(TAG,"cancelAlarm----->action = "+action);
        Intent intent = new Intent(context, RemindReciver.class);
        intent.setAction(action);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(pi);
    }

    /**
     * @param weekType 传入的是整型的星期几
     * @param dateTime 传入的是时间戳（设置当天的年月日+从选择框拿来的时分秒）
     * @return 返回起始闹钟时间的时间戳
     * Calculate the alarm time
     */
    public static long calculateAlarmTime(int weekType, long dateTime) {
        long time = 0;
        //weekflag == 0表示是按天为周期性的时间间隔或者是一次行的，weekfalg非0时表示每周几的闹钟并以周为时间间隔
        if (weekType != 0) {
            Calendar c = Calendar.getInstance();
            int week = c.get(Calendar.DAY_OF_WEEK)-1;
            if (weekType == week) {
                if(FunctionUtil.getRemindDateTime(dateTime).getDay().equals(FunctionUtil.getRemindDateTime(System.currentTimeMillis()).getDay())){
                    time = dateTime;
                }else{
                    time = dateTime + 6 * 24 * 3600 * 1000;
                }
            } else if (weekType > week) {
                if(FunctionUtil.getRemindDateTime(dateTime).getDay().equals(FunctionUtil.getRemindDateTime(System.currentTimeMillis()).getDay())){
                    time = dateTime + (weekType - week) * 24 * 3600 * 1000;
                }else{
                    time = dateTime + (weekType - week - 1) * 24 * 3600 * 1000;
                }
            } else if (weekType < week) {
                if(FunctionUtil.getRemindDateTime(dateTime).getDay().equals(FunctionUtil.getRemindDateTime(System.currentTimeMillis()).getDay())){
                    time = dateTime + (weekType - week + 7) * 24 * 3600 * 1000;
                }else{
                    time = dateTime + (weekType - week + 6) * 24 * 3600 * 1000;
                }
            }
        } else {
            time = dateTime;
        }
        return time;
    }
    /**
     * 获取提醒的重复周期
     * @param begin
     * @param end
     * @return
     */
    public static List<Integer> getRepeatWeek(int begin,int end){
        List<Integer> repeatWeek = new ArrayList<Integer>();
        if(begin == end){
            repeatWeek.add(begin);
        }else if(begin < end){
            for (int i = begin; i <= end; i++) {
                repeatWeek.add(i);
            }
        }else if(begin > end){
            for (int i = begin; i <= 7; i++) {
                repeatWeek.add(i);
            }
            for (int i = 1; i <= end; i++) {
                repeatWeek.add(i);
            }
        }
        return repeatWeek;
    }

    /**
     * 设置提醒
     * @param remindInfo
     */
    public static void setRealAlarm(Context context, RemindInfo remindInfo, long intervalMillis, int id) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //得到日历实例，主要是为了下面的获取时间
        Calendar calendar = Calendar.getInstance();
        //是设置日历的时间，主要是让日历的年月日和当前同步
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Intent intent = new Intent(context, RemindReciver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FunctionUtil.KEY_REMINDINFO,remindInfo);
        intent.putExtra(FunctionUtil.KEY_REMINDINFO_DATA,bundle);
        intent.setAction(remindInfo.getTime()+remindInfo.getRepeatDate());
        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, 0);
        if(remindInfo.getRepeatDate().equals("ONETIME")){
            Log.d("TAG","设置单次闹钟--->remindInfo = "+remindInfo.toString()+printTime(remindInfo.getTime()));
            am.set(AlarmManager.RTC_WAKEUP, remindInfo.getTime(), sender);
        }else{
            Log.d(TAG,"设置重复闹钟--->remindInfo = "+remindInfo.toString()+printTime(remindInfo.getTime())+",间隔时间 = "+intervalMillis/3600000+"小时");
            am.setRepeating(AlarmManager.RTC_WAKEUP, remindInfo.getTime(), intervalMillis, sender);
        }
    }

    /**
     * 获取字符串
     * @param repeatDate
     * @return
     */
    public static int getWeekStart(String repeatDate,String type){
        Log.d(TAG,"getWeekStart--->repeatDate = "+repeatDate);
        Pattern p = Pattern.compile("\\d+");
        Matcher matcher;
        String[] temp;
        if(repeatDate.indexOf("-") != -1){
            temp = repeatDate.split("-");
            if(type.equals("begin")){
                matcher = p.matcher(temp[0]);
            }else{
                matcher = p.matcher(temp[1]);
            }
        }else{
            matcher = p.matcher(repeatDate);
        }
        matcher.find();
        if(matcher.group() != null){
            return Integer.parseInt(matcher.group());
        }
        return -1;
    }

    /**
     * 获取闹钟重复的类型
     */
    public static int getRepeatType(String repeatDate){
        if(repeatDate.equals("ONETIME")){
            return REPEAT_TYPE_ONETIME;
        }else if(repeatDate.equals("EVERYDAY")){
            return REPEAT_TYPE_EVERYDAY;
        }else if(repeatDate.equals("WEEKEND")){
            return REPEAT_TYPE_WEEKEND;
        }else if(repeatDate.equals("WORKDAY")){
            return REPEAT_TYPE_WORKDAY;
        }else if(repeatDate.indexOf("-") != -1){
            return REPEAT_TYPE_MOREWEEKS;
        }else{
            return REPEAT_TYPE_EVERYWEEK;
        }
    }

    /**
     * 打印提醒时间
     */
    public static String printTime(long time){
        return FunctionUtil.longToString(time,FunctionUtil.TIME_FORMATE1);
    }

}