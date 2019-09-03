package com.droi.aiui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.droi.aiui.R;
import com.droi.aiui.bean.RemindInfo;
import com.droi.aiui.util.FunctionUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by cuixiaojun on 18-1-17.
 */

public class CappuRemindAdapter extends BaseAdapter{

    private Context mContext;
    private LayoutInflater inflater;
    private Map<String,List<RemindInfo>> map;
    private List<String> keys;
    public CappuRemindAdapter(Context context,List<String> keys,Map<String,List<RemindInfo>> map) {
        inflater=LayoutInflater.from(context);
        this.mContext = context;
        this.keys=keys;
        this.map = map;
    }

    @Override
    public int getCount() {
        return keys.size();
    }

    @Override
    public Object getItem(int position) {
        return keys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null) {
            holder = new ViewHolder();
            convertView =inflater.inflate(R.layout.item_remind,null);
            holder.container = (LinearLayout)convertView.findViewById(R.id.linear_remind_details);
            holder.remindInfo_date = (TextView)convertView.findViewById(R.id.remindInfo_date);
            holder.remindInfo_data_tips = (TextView)convertView.findViewById(R.id.remindInfo_data_tips);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        //所有的提醒
        final List<RemindInfo> remindDetails = map.get(keys.get(position));
        //将提醒时间格式化，取出具体的提醒日期
        String day = FunctionUtil.getRemindDateTime(remindDetails.get(0).getTime()).getDay();
        holder.remindInfo_date.setText(day);
        //将当前系统时间格式化，取出具体的系统时间戳
        //今天的日期
        String todayDate = FunctionUtil.getRemindDateTime(System.currentTimeMillis()).getDay();
        //明天的日期
        String tomorrowDate = FunctionUtil.getRemindDateTime(System.currentTimeMillis()+ 24 * 3600 * 1000).getDay();
        //后天的日期
        String acquiredDate = FunctionUtil.getRemindDateTime(System.currentTimeMillis()+ 24 * 3600 * 1000 * 2).getDay();
        //提醒的星期
        String week = FunctionUtil.getRemindDateTime(remindDetails.get(0).getTime()).getWeek();
        //判断提醒日期和当前系统日期是否一致，如果一致则将当前的日期文本颜色设置为红色，否则还是显示当前的颜色�
        if(day.equals(todayDate)) {
            holder.remindInfo_data_tips.setText(R.string.reminds_data_tips_today);
            holder.remindInfo_date.setTextColor(ContextCompat.getColor(mContext,R.color.remind_detail_delete_text_color));
            holder.remindInfo_data_tips.setTextColor(ContextCompat.getColor(mContext,R.color.remind_detail_delete_text_color));
        }else if(day.equals(tomorrowDate)){
            holder.remindInfo_data_tips.setText(R.string.reminds_data_tips_tomorrow);
            holder.remindInfo_date.setTextColor(ContextCompat.getColor(mContext,R.color.background_gray));
            holder.remindInfo_data_tips.setTextColor(ContextCompat.getColor(mContext,R.color.background_gray));
        }else if(day.equals(acquiredDate)){
            holder.remindInfo_data_tips.setText(R.string.reminds_data_tips_acquired);
            holder.remindInfo_date.setTextColor(ContextCompat.getColor(mContext,R.color.background_gray));
            holder.remindInfo_data_tips.setTextColor(ContextCompat.getColor(mContext,R.color.background_gray));
        }else{
            holder.remindInfo_data_tips.setText(week);
            holder.remindInfo_date.setTextColor(ContextCompat.getColor(mContext,R.color.background_gray));
            holder.remindInfo_data_tips.setTextColor(ContextCompat.getColor(mContext,R.color.background_gray));
        }
        holder.container.removeAllViews();
        for (int i = 0; i < remindDetails.size(); i++) {
            //提醒的详细view，主要包含提醒的具体时间和提醒的内容
            View childView=inflater.inflate(R.layout.item_remind_details,null);
            //将提醒的详细信息临时保存起来，以便后边点击查看具体的提醒详情
            childView.setTag(remindDetails.get(i));
            //为每条提醒设置点击监听
            childView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //将点击监听事件回调到RemindFragment
                    if (ionItemClickListener!=null) {
                        RemindInfo cinfo= (RemindInfo) view.getTag();
                        ionItemClickListener.onItemClickListen(cinfo);
                    }
                }
            });

            TextView remind_content = (TextView)childView.findViewById(R.id.remind_content);
            TextView remind_time = (TextView)childView.findViewById(R.id.remind_time);

            remind_content.setText(remindDetails.get(i).getContent());
            remind_time.setText(FunctionUtil.getRemindDateTime(remindDetails.get(i).getTime()).getTime());

            holder.container.addView(childView);
            if (i != remindDetails.size()-1) {
                View view=new View(mContext);
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1));
                view.setBackgroundColor(ContextCompat.getColor(mContext,R.color.background_black_5));
                holder.container.addView(view);
            }
        }
        return convertView;
    }

    class ViewHolder{
        LinearLayout container;
        TextView remindInfo_date;
        TextView remindInfo_data_tips;
    }

    IonItemClickListener ionItemClickListener;

    public void setIonItemClickListener(IonItemClickListener ionItemClickListener) {
        this.ionItemClickListener = ionItemClickListener;
    }

    public interface IonItemClickListener
    {
        void onItemClickListen(RemindInfo info);
    }
}