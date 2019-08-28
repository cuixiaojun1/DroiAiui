package com.droi.aiui.Interface;

import com.droi.aiui.bean.Message;

import java.util.ArrayList;

/**
 * Created by cuixiaojun on 17-12-18.
 */

public interface OnParseResultTextListener {
    void onParseResult(ArrayList<Message> messages);
}