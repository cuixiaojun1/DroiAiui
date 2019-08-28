package com.droi.aiui.apkupdate;


import android.content.Context;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.droi.aiui.util.JsonParserUtil;

/**
 * @author feicien (ithcheng@gmail.com)
 * @since 2016-07-05 19:21
 */
class CheckUpdateTask extends AsyncTask<Void, Void, String> {

    private final String TAG = "CheckUpdateTask";
    private Context mContext;


    CheckUpdateTask(Context context) {
        this.mContext = context;
    }


    protected void onPreExecute() {
    }


    @Override
    protected void onPostExecute(String result) {
        if (!TextUtils.isEmpty(result)) {
            parseJson(result);
        }
    }

    private void parseJson(String result) {
        ApkUpdateBean apkUpdateBean = JsonParserUtil.parseJsonObject(result,ApkUpdateBean.class);
        if (apkUpdateBean.getRt() != 1)
            return;
        int apkCode = apkUpdateBean.getVcode();
        String updateMessage = apkUpdateBean.getDescription();
        boolean isForce = apkUpdateBean.isIsforce();
        String apkUrl = Constants.BASE_URL + apkUpdateBean.getPath() + "&sign="+Constants.SIGN + "&appkey=" + Constants.APPKEY;
        int versionCode = AppUtils.getVersionCode(mContext);
        if (apkCode > versionCode) {
            showDialog(mContext, updateMessage, apkUrl, apkUpdateBean.getPath(), isForce);
        }
    }

    /**
     * Show dialog
     */
    public void showDialog(Context context, String content, String apkUrl, String apkName, boolean isForce) {
        UpdateDialog.show(context, content, apkUrl, apkName, isForce);
    }


    @Override
    protected String doInBackground(Void... args) {
        return HttpUtils.get(mContext);
    }
}