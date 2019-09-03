package com.droi.aiui.apkupdate;

import android.content.Context;
import android.util.Log;

public class UpdateChecker {



    /**
     * dialog更新
     * @param context
     */
    public static void checkForDialog(Context context) {
        if (context != null) {
            new CheckUpdateTask(context).execute();
        } else {
            Log.e("UpdateChecker", "[checkForDialog]The arg context is null");
        }
    }

}