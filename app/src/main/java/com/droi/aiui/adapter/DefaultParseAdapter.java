package com.droi.aiui.adapter;

import android.content.Context;

import com.droi.aiui.util.FunctionUtil;

/**
 * Created by cuixiaojun on 17-12-19.
 */

public class DefaultParseAdapter extends BaseParseAdapter {

    private final String TAG = "DefaultParseAdapter";
    private Context mContext;
    public DefaultParseAdapter(Context context) {
        mContext = context;
    }

    @Override
    public String getSemanticResultText(String json) {
        return FunctionUtil.getAnswer(mContext);
    }

}