package com.droi.aiui.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

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
     * 获取intent
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
     * 处理intent
     */
    private String handleApplicationIntent(String intent){
        Log.d(TAG,"handleApplicationIntent---->intent = "+intent);
        String result = null;
        if (intent != null){
            switch (intent){
                case "open_common_app_intent":
                case "open_custom_app_intent":
                case "open_special_app_intent":
                    result = handleAppResult("");
                    break;
                case "view_photo_intent":
                    result = handleAppResult("相册");
                    break;
                case "take_photo_intent":
                    result = handleAppResult("相机");
                    break;
                    default:
                        result = "对不起，我没有听清楚您说的是什么意思！";
                        break;
            }
        }else{
            result = "对不起，没有在您的手机里边找到该应用！";
        }
        return result;
    }

    /**
     *
     * @return 返回对应的提示语
     */
    private String handleAppResult(String name){
        String result;
        String appName;
        if(!TextUtils.isEmpty(name)){
            appName = name;
        }else{
            appName = parseAppName();
            if(appName.equals("优酷视频")){
                appName = "优酷";
            }else if(appName.equals("qq浏览器")){
                appName = "QQ浏览器";
            }else if(appName.equals("短信息") || appName.equals("短信")){
                appName = "信息";
            }else if(appName.equals("视频播放器")){
                appName = "视频";
            } else if(appName.equals("照相机")){
                appName = "相机";
            } else if(appName.equals("音乐播放器")){
                appName = "曲艺杂坛";
            } else if(appName.equals("fm")){
                appName = "收音机";
            } else if(appName.equals("闹钟")){
                appName = "时钟";
            } else if(appName.equals("糖豆") || appName.equals("广场舞")){
                appName = "糖豆广场舞";
            } else if(appName.equals("sim卡应用") ){
                appName = "SIM卡应用";
            }
        }
        final ComponentName componentName = getComponentByAppName(appName);
        Log.d(TAG,"handleAppResult--->appName = "+appName+",componentName = "+componentName);
        if(!TextUtils.isEmpty(appName)){
            if( componentName != null){
                if(appName.equals("智能语音")){
                    result = "您目前已经打开了智能语音！";
                }else{
                    if(FunctionUtil.checkApkInstall(mContext,componentName.getPackageName())){
                        result = "正在为您打开"+appName;
                        if(appName.equals("拨号盘")){
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
                                        Log.d(TAG,"应用打开失败！");
                                        e.printStackTrace();
                                    }
                                }
                            },3000);

                        }else if(appName.equals("电话")){
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
                                        Log.d(TAG,"应用打开失败！");
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
            }else if(appName.equals("云课堂")){
                //打开云课堂 zhouhua@20181115 up 白牌浏览器 packagename
                if(FunctionUtil.checkApkInstall(mContext,"com.android.browser")){
                    result = "正在为您打开"+appName;
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
                    result = "您暂未安装相关的浏览器，请安装之后再试！";
                }
            }else if(appName.equals("手电筒")){
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
     * 解析应用名称
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
     * 通过应用名称获取应用对应的包名
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
            if(appName.equals("拨号盘")){
                component = new ComponentName("com.android.dialer","com.android.dialer.app.DialtactsActivity");
            }else if(appName.equals("设置") || appName.equals("安卓设置")){
                component = new ComponentName("com.android.settings","com.android.settings.Settings");
            }else if(appName.equals("游戏")){
                component = new ComponentName("com.cappu.launcherwin","com.cappu.launcherwin.applicationList.activity.PlayCenterActivity");
            }else if(appName.equals("话费查询")){
                component = new ComponentName("com.cappu.launcherwin","com.cappu.launcherwin.phoneutils.PhoneUtilActivity");
            }else if(appName.equals("电话")){
                component = new ComponentName("com.android.dialer","com.android.dialer.app.DialtactsActivity");
            }
        }
        return component;
    }

    /**
     * 在无网络的情况下打开云课堂
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
            Log.d(TAG,"应用打开错误！");
        }
    }

    /**
     * 在有网络的情况下打开云课堂
     */
    private void openCloudClassRoomHasNet(){
        String webPath = "http://yun.cappu.com/index.php/page/index/yinpin";
        try{
            Uri uri = Uri.parse(webPath);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.android.browser"); //zhouhua@20181115 up 白牌浏览器 packagename 
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.getApplicationContext().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"应用打开错误！");
        }
    }

    private String handleTorchAction(CameraManager cameraManager){
        Log.d("cuixiaojun","[AppParseAdapter][handleTorchAction]isHasFlashLight = "+FunctionUtil.isHasFlashLight(cameraManager)+",getTorchState = "+FunctionUtil.getTorchState(mContext));
        if(FunctionUtil.isHasFlashLight(cameraManager)){
            if(FunctionUtil.getTorchState(mContext) == 0){
                if(FunctionUtil.handelFlashLight(mContext,cameraManager,false)){
                    return "正在为您关闭手电筒！";
                }else{
                    return "关闭手电筒失败，请重新尝试！";
                }
            }else{
                if(FunctionUtil.handelFlashLight(mContext,cameraManager,true)){
                    return "正在为您打开手电筒！";
                }else{
                    return "打开手电筒失败，请重新尝试！";
                }
            }
        }else{
            return "对不起，未在您的手机中检测到相关的设备！";
        }
    }
}