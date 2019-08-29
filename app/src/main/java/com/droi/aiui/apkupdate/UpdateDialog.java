package com.droi.aiui.apkupdate;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.widget.Toast;

import com.droi.aiui.R;

import java.util.List;

class UpdateDialog {

    static void show(final Context context, String content, final String downloadUrl, final String apkName,final boolean isForce) {
        if (isContextValid(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.android_auto_update_dialog_title);
            builder.setMessage(Html.fromHtml(content))
                    .setPositiveButton(R.string.android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Toast.makeText(context,"�Ѿ��ں�̨Ϊ�����أ����Ե�֪ͨ���鿴!", Toast.LENGTH_SHORT).show();
                            goToDownload(context, downloadUrl,apkName);
                        }
                    })
                    .setNegativeButton(R.string.android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (isForce)  //�˳�Ӧ��
                            {
                                System.exit(0);
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            //����Ի�������,�Ի�����ʧ
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private static boolean isContextValid(Context context) {
        return context instanceof Activity && !((Activity) context).isFinishing();
    }


    private static void goToDownload(Context context, String downloadUrl,String apkName) {
        if (!isServiceWork(context,"com.cappu.aiui.apkupdate.DownloadService"))
        {
            Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
            intent.putExtra(Constants.APK_DOWNLOAD_URL, downloadUrl);
            intent.putExtra("apkName", apkName);
            context.startService(intent);
        }
    }

    /**
     * �ж�ĳ�������Ƿ��������еķ���
     *
     * @param mContext
     * @param serviceName
     *            �ǰ���+��������������磺net.loonggg.testbackstage.TestService��
     * @return true�����������У�false�������û����������
     */
    private static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}