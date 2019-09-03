package com.droi.aiui.adapter;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cuixiaojun on 17-12-28.
 */

public class OpenQAParseAdapter extends BaseParseAdapter {

    private final String TAG = "OpenQAParseAdapter";
    @Override
    public String getSemanticResultText(String json) {
        String answerText = null;
        Log.d(TAG,"OpenQAParseAdapter--->getSemanticResultText-->json =  "+json);
        try {
            JSONObject resultJson = new JSONObject(json);
            JSONObject answerJson = new JSONObject(resultJson.optString("answer"));
            answerText = answerJson.optString("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return answerText;
    }
}