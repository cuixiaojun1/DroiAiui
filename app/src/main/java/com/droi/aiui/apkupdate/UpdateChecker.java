package com.droi.aiui.apkupdate;

import android.content.Context;

public class UpdateChecker {



    /**
     * dialog����
     * @param context
     */
    public static void checkForDialog(Context context) {
        if (context != null) {
            new CheckUpdateTask(context).execute();
        } else {
        }
    }

}