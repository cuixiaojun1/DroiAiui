package com.droi.aiui.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuixiaojun on 18-1-8.
 * 句，用于存解析的结果
 */

public class Sentence {
    /** 文本 */
    public String mText = "";
    /** sn的队列 */
    public List<Integer> mSns = new ArrayList<Integer>();
}