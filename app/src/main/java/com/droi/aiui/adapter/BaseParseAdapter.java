package com.droi.aiui.adapter;


/**
 * Created by cuixiaojun on 17-12-18.
 */

public abstract class BaseParseAdapter{
    //返回语义理解结果文本
    public abstract String getSemanticResultText(String json);
}