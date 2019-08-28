package com.droi.aiui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droi.aiui.R;
import com.droi.aiui.bean.AnimationState;
import com.droi.aiui.bean.CappuHelpListItemData;

import java.util.List;

public class CappuHelpListAdapter extends BaseAdapter implements View.OnClickListener {

    Context context;
    List<CappuHelpListItemData> getHelpListItemData;
    public InnerItemOnclickListener mListener;
    private List<AnimationState> animationStates;

    public CappuHelpListAdapter(Context context, List<CappuHelpListItemData> getHelpListItemData,List<AnimationState> animationStates) {
        this.context = context;
        this.getHelpListItemData = getHelpListItemData;
        this.animationStates = animationStates;
    }

    @Override
    public int getCount() {
        return getHelpListItemData.size();
    }

    @Override
    public Object getItem(int position) {
        return getHelpListItemData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        HelpListViewHolder helpListViewHolder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_help, null);
            helpListViewHolder = new HelpListViewHolder();
            helpListViewHolder.title = (TextView) view.findViewById(R.id.help_listView_title);
            helpListViewHolder.content_1 = (TextView)view.findViewById(R.id.help_listView_item_one);
            helpListViewHolder.contnet_2 = (TextView)view.findViewById(R.id.help_listView_item_two);
            helpListViewHolder.imageView1 = (ImageView) view.findViewById(R.id.item_speaker_one);
            helpListViewHolder.imageView2 = (ImageView) view.findViewById(R.id.item_speaker_two);
            helpListViewHolder.linearLayout1 = (LinearLayout) view.findViewById(R.id.linear_one);
            helpListViewHolder.linearLayout2 = (LinearLayout)view.findViewById(R.id.linear_two);
            helpListViewHolder.linearLayout1.setOnClickListener(this);
            helpListViewHolder.linearLayout2.setOnClickListener(this);
            view.setTag(helpListViewHolder);
        } else {
            helpListViewHolder = (HelpListViewHolder) view.getTag();
        }

        helpListViewHolder.title.setText(getHelpListItemData.get(position).getTitle());
        helpListViewHolder.content_1.setText(getHelpListItemData.get(position).getContent1());
        helpListViewHolder.contnet_2.setText(getHelpListItemData.get(position).getContent2());

        helpListViewHolder.linearLayout1.setTag(position);
        helpListViewHolder.linearLayout2.setTag(position);

        if (animationStates.get(position).isOne_state()) {
            helpListViewHolder.imageView1.setImageResource(R.drawable.animation);
            AnimationDrawable drawable= (AnimationDrawable) helpListViewHolder.imageView1.getDrawable();
            drawable.start();
        }else {
            helpListViewHolder.imageView1.setImageResource(R.mipmap.speaker);
        }
        if (animationStates.get(position).isTwo_state()) {
            helpListViewHolder.imageView2.setImageResource(R.drawable.animation);
            AnimationDrawable drawable= (AnimationDrawable) helpListViewHolder.imageView2.getDrawable();
            drawable.start();
        }else {
            helpListViewHolder.imageView2.setImageResource(R.mipmap.speaker);
        }
        return view;
    }

    class HelpListViewHolder {
        TextView title;
        TextView content_1;
        TextView contnet_2;
        ImageView imageView1;
        ImageView imageView2;
        LinearLayout linearLayout1;
        LinearLayout linearLayout2;
    }

    public interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        if(mListener != null){
            mListener.itemClick(v);
        }
    }
}