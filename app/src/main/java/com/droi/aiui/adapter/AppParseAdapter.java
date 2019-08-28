package com.droi.aiui.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.droi.aiui.R;
import com.droi.aiui.bean.AppBean;
import com.droi.aiui.bean.AppInfo;
import com.droi.aiui.controler.DataControler;
import com.droi.aiui.util.FunctionUtil;
import com.droi.aiui.util.JsonParserUtil;

import java.util.List;


/**
 * Created by cuixiaojun on 18-1-10.
 */

public class AppParseAdapter extends BaseParseAdapter {

    private final String TAG = "AppParseAdapter";
    private AppBean mAppBean;
    private Context mContext;
    private List<AppInfo> allApps;
    private CameraManager mCameraManager;

    public AppParseAdapter(Context context) {
        this.mContext = context;
        allApps = DataControler.getInstance(mContext).loadAllApps();
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public String getSemanticResultText(String json) {
        mAppBean = JsonParserUtil.parseJsonObject(json,AppBean.class);
        String result = handleApplicationIntent(getApplicationIntent());
        if(TextUtils.isEmpty(result)){
            result = mContext.getString(R.string.text_no_result);
        }
        return result;
    }

    /**
     * ��ȡintent
     */
    private String getApplicationIntent(){
        String intent = null;
        List<AppBean.SemanticBean> semantic = mAppBean.getSemantic();
        for (int i = 0; i < semantic.size(); i++) {
            AppBean.SemanticBean semanticBean = semantic.get(i);
            intent = semanticBean.getIntent();
        }
        return intent;
    }

    /**
     * ����intent
     */
    private String handleApplicationIntent(String intent){
        String result = null;
        if (intent != null){
            switch (intent){
                case "open_common_app_intent":
                case "open_custom_app_intent":
                case "open_special_app_intent":
                    result = handleAppResult("");
                    break;
                case "view_photo_intent":
                    result = handleAppResult("���");
                    break;
                case "take_photo_intent":
                    result = handleAppResult("���");
                    break;
                    default:
                        result = "�Բ�����û���������˵����ʲô��˼��";
                        break;
            }
        }else{
            result = "�Բ���û���������ֻ�����ҵ���Ӧ�ã�";
        }
        return result;
    }

    /**
     *
     * @return ���ض�Ӧ����ʾ��
     */
    private String handleAppResult(String name){
        String result;
        String appName;
        if(!TextUtils.isEmpty(name)){
            appName = name;
        }else{
            appName = parseAppName();
            if(appName.equals("�ſ���Ƶ")){
                appName = "�ſ�";
            }else if(appName.equals("qq�����")){
                appName = "QQ�����";
            }else if(appName.equals("����Ϣ") || appName.equals("����")){
                appName = "��Ϣ";
            }else if(appName.equals("��Ƶ������")){
                appName = "��Ƶ";
            } else if(appName.equals("�����")){
                appName = "���";
            } else if(appName.equals("���ֲ�����")){
                appName = "������̳";
            } else if(appName.equals("fm")){
                appName = "������";
            } else if(appName.equals("����")){
                appName = "ʱ��";
            } else if(appName.equals("�Ƕ�") || appName.equals("�㳡��")){
                appName = "�Ƕ��㳡��";
            } else if(appName.equals("sim��Ӧ��") ){
                appName = "SIM��Ӧ��";
            }
        }
        final ComponentName componentName = getComponentByAppName(appName);
        if(!TextUtils.isEmpty(appName)){
            if( componentName != null){
                if(appName.equals("��������")){
                    result = "��Ŀǰ�Ѿ���������������";
                }else{
                    if(FunctionUtil.checkApkInstall(mContext,componentName.getPackageName())){
                        result = "����Ϊ����"+appName;
                        if(appName.equals("������")){
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.setComponent(componentName);
                                        intent.putExtra("EXTRA_SHOW_TAB", 1);
                                        mContext.startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            },3000);

                        }else if(appName.equals("�绰")){
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.setComponent(componentName);
                                        intent.putExtra("EXTRA_SHOW_TAB", 0);
                                        mContext.startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            },3000);
                        }else{
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    FunctionUtil.startApp(componentName,mContext);
                                }
                            },3000);
                        }
                    }else{
                        result = mContext.getString(R.string.text_no_app_found_result);
                    }
                }
            }else if(appName.equals("�ƿ���")){
                //���ƿ��� zhouhua@20181115 up ��������� packagename
                if(FunctionUtil.checkApkInstall(mContext,"com.android.browser")){
                    result = "����Ϊ����"+appName;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(FunctionUtil.isNetworkAvailable(mContext)){
                                openCloudClassRoomHasNet();
                            }else{
                                openCloudClassRoomNoNet();
                            }
                        }
                    },3000);
                }else{
                    result = "����δ��װ��ص���������밲װ֮�����ԣ�";
                }
            }else if(appName.equals("�ֵ�Ͳ")){
                result = handleTorchAction(mCameraManager);
            }else{
                result = mContext.getString(R.string.text_no_app_found_result);
            }
        } else {
            result = mContext.getString(R.string.text_no_app_found_result);
        }
        return result;
    }

    /**
     * ����Ӧ������
     * @return
     */
    private String parseAppName(){
        String result = null;
        List<AppBean.SemanticBean> semantic = mAppBean.getSemantic();
        for (int i = 0; i < semantic.size(); i++) {
            List<AppBean.SemanticBean.SlotsBean> slots = semantic.get(i).getSlots();
            for (int j = 0; j < slots.size(); j++) {
                result = slots.get(i).getValue();
            }
        }
        return result;
    }

    /**
     * ͨ��Ӧ�����ƻ�ȡӦ�ö�Ӧ�İ���
     */
    private ComponentName getComponentByAppName(String appName){
        ComponentName component = null;
        for (int i = 0; i < allApps.size(); i++) {
            AppInfo info = allApps.get(i);
            String pckName = info.getPackageName();
            String className = info.getClassName();
            String name = info.getAppName();
            if (name.equals(appName)) {
                component = new ComponentName(pckName,className);
            }
            if(appName.equals("������")){
                component = new ComponentName("com.android.dialer","com.android.dialer.app.DialtactsActivity");
            }else if(appName.equals("����") || appName.equals("��׿����")){
                component = new ComponentName("com.android.settings","com.android.settings.Settings");
            }else if(appName.equals("��Ϸ")){
                component = new ComponentName("com.cappu.launcherwin","com.cappu.launcherwin.applicationList.activity.PlayCenterActivity");
            }else if(appName.equals("���Ѳ�ѯ")){
                component = new ComponentName("com.cappu.launcherwin","com.cappu.launcherwin.phoneutils.PhoneUtilActivity");
            }else if(appName.equals("�绰")){
                component = new ComponentName("com.android.dialer","com.android.dialer.app.DialtactsActivity");
            }
        }
        return component;
    }

    /**
     * �������������´��ƿ���
     */
    private void openCloudClassRoomNoNet(){
        String webPath = "http://yun.cappu.com/index.php/page/index/yinpin";
        try{
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName("com.cappu.launcherwin","com.cappu.launcherwin.NoNetActivity");
            intent.setComponent(componentName);
            intent.putExtra("address_uri", webPath);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.getApplicationContext().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * �������������´��ƿ���
     */
    private void openCloudClassRoomHasNet(){
        String webPath = "http://yun.cappu.com/index.php/page/index/yinpin";
        try{
            Uri uri = Uri.parse(webPath);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.android.browser"); //zhouhua@20181115 up ��������� packagename 
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.getApplicationContext().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String handleTorchAction(CameraManager cameraManager){
        if(FunctionUtil.isHasFlashLight(cameraManager)){
            if(FunctionUtil.getTorchState(mContext) == 0){
                if(FunctionUtil.handelFlashLight(mContext,cameraManager,false)){
                    return "����Ϊ���ر��ֵ�Ͳ��";
                }else{
                    return "�ر��ֵ�Ͳʧ�ܣ������³��ԣ�";
                }
            }else{
                if(FunctionUtil.handelFlashLight(mContext,cameraManager,true)){
                    return "����Ϊ�����ֵ�Ͳ��";
                }else{
                    return "���ֵ�Ͳʧ�ܣ������³��ԣ�";
                }
            }
        }else{
            return "�Բ���δ�������ֻ��м�⵽��ص��豸��";
        }
    }
}