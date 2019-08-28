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
     * ȡ����Ӧ������
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
     * @param weekType ����������͵����ڼ�
     * @param dateTime �������ʱ��������õ����������+��ѡ���������ʱ���룩
     * @return ������ʼ����ʱ���ʱ���
     * Calculate the alarm time
     */
    public static long calculateAlarmTime(int weekType, long dateTime) {
        long time = 0;
        //weekflag == 0��ʾ�ǰ���Ϊ�����Ե�ʱ����������һ���еģ�weekfalg��0ʱ��ʾÿ�ܼ������Ӳ�����Ϊʱ����
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
     * ��ȡ���ѵ��ظ�����
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
     * ��������
     * @param remindInfo
     */
    public static void setRealAlarm(Context context, RemindInfo remindInfo, long intervalMillis, int id) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //�õ�����ʵ������Ҫ��Ϊ������Ļ�ȡʱ��
        Calendar calendar = Calendar.getInstance();
        //������������ʱ�䣬��Ҫ���������������պ͵�ǰͬ��
        calendar.setTimeInMillis(System.currentTimeMillis());
        // ����ʱ����Ҫ����һ�£���Ȼ���ܸ����ֻ�����8��Сʱ��ʱ���
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Intent intent = new Intent(context, RemindReciver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FunctionUtil.KEY_REMINDINFO,remindInfo);
        intent.putExtra(FunctionUtil.KEY_REMINDINFO_DATA,bundle);
        intent.setAction(remindInfo.getTime()+remindInfo.getRepeatDate());
        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, 0);
        if(remindInfo.getRepeatDate().equals("ONETIME")){
            Log.d("TAG","���õ�������--->remindInfo = "+remindInfo.toString()+printTime(remindInfo.getTime()));
            am.set(AlarmManager.RTC_WAKEUP, remindInfo.getTime(), sender);
        }else{
            Log.d(TAG,"�����ظ�����--->remindInfo = "+remindInfo.toString()+printTime(remindInfo.getTime())+",���ʱ�� = "+intervalMillis/3600000+"Сʱ");
            am.setRepeating(AlarmManager.RTC_WAKEUP, remindInfo.getTime(), intervalMillis, sender);
        }
    }

    /**
     * ��ȡ�ַ���
     * @param repeatDate
     * @return
     */
    public static int getWeekStart(String repeatDate,String type){
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
     * ��ȡ�����ظ�������
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
     * ��ӡ����ʱ��
     */
    public static String printTime(long time){
        return FunctionUtil.longToString(time,FunctionUtil.TIME_FORMATE1);
    }

}