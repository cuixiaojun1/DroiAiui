package com.droi.aiui.apkupdate;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author feicien (ithcheng@gmail.com)
 * @since 2016-07-05 19:25
 */
public class HttpUtils {


    public static String get(Context context) {

        String postUrl = new StringBuffer("http://upgrade.cappu.cn/v1/upgrade?")
                .append("appkey="+Constants.APPKEY)
                .append("&channel="+Constants.CHANNEL)
                .append("&sign="+Constants.SIGN)
                .append("&vcode=")
                .append(AppUtils.getVersionCode(context))
                .append("&pack=")
                .append("com.cappu.aiui")
                .toString();
        Log.d("HttpUtils","[HttpUtils][get]postUrl = "+postUrl);
        HttpURLConnection uRLConnection = null;
        InputStream is = null;
        BufferedReader buffer = null;
        String result = null;
        try {
            URL url = new URL(postUrl);
            uRLConnection = (HttpURLConnection) url.openConnection();
            uRLConnection.setRequestMethod("GET");

            is = uRLConnection.getInputStream();
            buffer = new BufferedReader(new InputStreamReader(is));
            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = buffer.readLine()) != null) {
                strBuilder.append(line);
            }
            result = strBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException ignored) {

                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {

                }
            }
            if (uRLConnection != null) {
                uRLConnection.disconnect();
            }
        }
        return result;
    }
}