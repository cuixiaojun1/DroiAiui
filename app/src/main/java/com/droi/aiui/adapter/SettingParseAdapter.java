package com.droi.aiui.adapter;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.camera2.CameraManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.droi.aiui.bean.SettingBean;
import com.droi.aiui.util.FunctionUtil;
import com.droi.aiui.util.JsonParserUtil;

import java.util.List;


/**
 * Created by hejianfeng on 2018/01/09.
 */

public class SettingParseAdapter extends BaseParseAdapter {

    private static final String TAG = "SettingParseAdapter";
    private SettingBean settingBean;
    private Context mContext;
    private CameraManager mCameraManager;

    public SettingParseAdapter(Context context) {
        this.mContext = context;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public String getSemanticResultText(String json) {
        settingBean = JsonParserUtil.parseJsonObject(json, SettingBean.class);
        String operationType = getType("operation");
        String settingType = getType("type");
        Log.d(TAG, "[SettingParseAdapter][getSemanticResultText]operationType = " + operationType + ",settingType = " + settingType);
        return handleSettingType(settingType, operationType);
    }

    private String handleSettingType(String settingType, String operationType) {
        Log.d(TAG, "[SettingParseAdapter][handleSettingType]settingType = " + settingType + ",operationType = " + operationType);
        if (settingType != null) {
            switch (settingType) {
                case "手电筒":
                    return handleTorchSkill(settingType, operationType);
                case "蓝牙":
                    return handleBluetoothSkill(settingType, operationType);
                case "流量":
                    return handleGPRSSkill(settingType, operationType);
                case "无线":
                    return handleWIFISkill(settingType, operationType);
                case "定位":
                    return handleGPSSkill(settingType, operationType);
                case "字体":
                    return handleFontSkill(settingType, operationType);
                case "亮度":
                    return handleBrightnessSkill(settingType, operationType);
                case "音量":
                    return handleVolumeSkill(settingType, operationType);
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 处理手电筒技能
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleTorchSkill(String settingType, String operationType) {
        Log.d("cuixiaojun", "[SettingParseAdapter][handleTorchSkill]settingType = " + settingType + ",operationType = " + operationType
                                            +",isHasFlashLight = "+FunctionUtil.isHasFlashLight(mCameraManager)+",getTorchState = "+FunctionUtil.getTorchState(mContext));
        if (operationType != null) {
            switch (operationType) {
                case "关闭":
                    if (FunctionUtil.isHasFlashLight(mCameraManager)) {
                        if (FunctionUtil.getTorchState(mContext) == 0) {
                            if(FunctionUtil.handelFlashLight(mContext,mCameraManager,false)){
                                return "正在为您关闭" + settingType + "!";
                            }else{
                                return settingType + "关闭失败，请重新尝试！";
                            }
                        } else {
                            return "当前" + settingType + "已经关闭！";
                        }
                    } else {
                        return "对不起，未在您的手机中检测到相关的设备！";
                    }
                case "打开":
                    if (FunctionUtil.isHasFlashLight(mCameraManager)) {
                        if (FunctionUtil.getTorchState(mContext) == 1) {
                            if(FunctionUtil.handelFlashLight(mContext,mCameraManager,true)){
                                return "正在为您打开"+ settingType + "!";
                            }else{
                                return settingType + "打开失败，请重新尝试!";
                            }
                        } else {
                            return "当前" + settingType + "已经打开！";
                        }
                    } else {
                        return "对不起，未在您的手机中检测到相关的设备！";
                    }
                case "增大":
                case "减小":
                    return "对不起，我不是很明白您说的意思，请换种说法试试！";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 处理蓝牙技能
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleBluetoothSkill(String settingType, String operationType) {
        Log.d(TAG, "[SettingParseAdapter][handleBluetoothSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "关闭":
                    if (FunctionUtil.isBluetoothEnabled()) {
                        if (FunctionUtil.setBluetoothEnabled(false)) {
                            return "正在为您关闭" + settingType + "！";
                        } else {
                            return "关闭" + settingType + "失败，请重新尝试！";
                        }
                    } else {
                        return "当前"+settingType+"已关闭！";
                    }
                case "打开":
                    if (FunctionUtil.isBluetoothEnabled()) {
                        return "当前"+settingType+"已打开！";
                    } else {
                        if (FunctionUtil.setBluetoothEnabled(true)) {
                            return "正在为您打开"+settingType+"！";
                        } else {
                            return "打开"+settingType+"失败,请重新尝试！";
                        }
                    }
                case "增大":
                case "减小":
                    return "对不起，我不是很明白您说的意思，请换种说法试试！";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 处理流量技能
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleGPRSSkill(String settingType, String operationType) {
        Log.d(TAG, "[SettingParseAdapter][handleGPRSSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "关闭":
                    if (FunctionUtil.hasSimCard(mContext)) {
                        if (FunctionUtil.getDataEnabled(mContext)) {
                            FunctionUtil.setDataEnabled(mContext, false);
                            return "正在为您关闭" + settingType + "!";
                        } else {
                            return "当前" + settingType + "已经关闭！";
                        }
                    } else {
                        return "当前未检测到SIM卡，请确认是否正确插入了SIM卡！";
                    }
                case "打开":
                    if (FunctionUtil.hasSimCard(mContext)) {
                        if(FunctionUtil.getDataEnabled(mContext)){
                            return "当前"+settingType+"已经打开！";
                        }else{
                            FunctionUtil.setDataEnabled(mContext,true);
                            return "正在为您打开"+settingType+"!";
                        }
                    } else {
                        return "当前未检测到SIM卡，请确认是否正确插入了SIM卡！";
                    }
                case "增大":
                case "减小":
                    return "对不起，我不是很明白您说的意思，请换种说法试试！";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 处理wifi技能
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleWIFISkill(String settingType, String operationType) {
        Log.d(TAG, "[SettingParseAdapter][handleWIFISkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "关闭":
                    if(FunctionUtil.isWifiEnabled(mContext)){
                        if(FunctionUtil.setWifiEnable(mContext,false)){
                            return "正在为您关闭"+settingType+"!";
                        }else{
                            return "关闭"+settingType+"失败,请重新尝试！";
                        }
                    }else{
                        return "当前"+settingType+"已经关闭！";
                    }
                case "打开":
                    if(FunctionUtil.isWifiEnabled(mContext)){
                        return "当前"+settingType+"已经打开！";
                    }else{
                        if(FunctionUtil.setWifiEnable(mContext,true)){
                            return "正在为您打开"+settingType+"！";
                        }else{
                            return "打开"+settingType+"失败,请重新尝试！";
                        }
                    }
                case "增大":
                case "减小":
                    return "对不起，我不是很明白您说的意思，请换种说法试试！";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 处理定位技能
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleGPSSkill(String settingType, String operationType) {
        Log.d(TAG, "[SettingParseAdapter][handleGPSSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "关闭":
                    if(FunctionUtil.isLocationEnabled(mContext)){
                        if(FunctionUtil.setLocationEnabled(mContext,false)){
                            return "正在为您关闭"+settingType+"!";
                        }else{
                            return "关闭"+settingType+"失败，请重新尝试！";
                        }
                    }else{
                        return "当前"+settingType+"已经关闭！";
                    }
                case "打开":
                    if(FunctionUtil.isLocationEnabled(mContext)){
                        return "当前"+settingType+"已经打开！";
                    }else{
                        if(FunctionUtil.setLocationEnabled(mContext,true)){
                            return "正在为您打开"+settingType+"!";
                        }else{
                            return "打开"+settingType+"失败，请重新尝试！";
                        }
                    }
                case "增大":
                case "减小":
                    return "对不起，我不是很明白您说的意思，请换种说法试试！";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 处理字体技能
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleFontSkill(String settingType, String operationType) {
        Log.d(TAG, "[SettingParseAdapter][handleFontSkill]settingType = " + settingType + ",operationType = " + operationType);
        float newFontSize = mContext.getResources().getConfiguration().fontScale;
        Log.d(TAG,"[SettingParseAdapter][handleFontSkill]newFontSize = "+newFontSize);
        if(newFontSize == 1.01f){
            newFontSize = 1.0f;
        }
        Configuration configuration = new Configuration();
        float small_fontSize = 0.85f;
        float default_fontSize = 1.0f;
        float bigger_fontSize = 1.15f;
        float biggest_fontSize = 1.3f;
        if (operationType != null) {
            switch (operationType) {
                case "关闭":
                case "打开":
                    return "对不起，我不是很明白您说的意思，请换种说法试试！";
                case "增大":
                    if (newFontSize == small_fontSize) {
                        configuration.fontScale = default_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "已经为您调为默认字体！";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "字体调节失败！";
                        }
                    } else if (newFontSize == default_fontSize) {
                        configuration.fontScale = bigger_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "已经为您调为更大字体！";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "字体调节失败！";
                        }
                    } else if (newFontSize == bigger_fontSize) {
                        configuration.fontScale = biggest_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "已经为您调为最大字体！";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "字体调节失败！";
                        }
                    } else if(newFontSize == biggest_fontSize){
                        return "当前字体已经为最大字体！";
                    } else{
                        return "未识别当前字体大小！";
                    }
                case "减小":
                    if (newFontSize == small_fontSize) {
                        return "当前字体已经为最小字体！";
                    } else if (newFontSize == default_fontSize) {
                        configuration.fontScale = small_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "已经为您调为最小字体！";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "字体调节失败！";
                        }
                    } else if (newFontSize == bigger_fontSize) {
                        configuration.fontScale = default_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "已经为您调为默认字体！";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "字体调节失败！";
                        }
                    } else if(newFontSize == biggest_fontSize){
                        configuration.fontScale = bigger_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "已经为您调为更大字体！";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "字体调节失败！";
                        }
                    } else {
                        return "未识别当前字体大小！";
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 处理屏幕亮度技能
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleBrightnessSkill(String settingType, String operationType) {
        Log.d(TAG, "[SettingParseAdapter][handleBrightnessSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "关闭":
                case "打开":
                    return "对不起，我不是很明白您说的意思，请换种说法试试！";
                case "增大":
                    if(FunctionUtil.getBrightness(mContext) >= 255){
                        return "当前屏幕亮度已经调到最亮！";
                    }else{
                        FunctionUtil.upBrightness(mContext);
                        return "已经为您调亮屏幕亮度！";
                    }
                case "减小":
                    if(FunctionUtil.getBrightness(mContext) <= 0){
                        return "当前屏幕亮度已经调为最暗！";
                    }else{
                        FunctionUtil.downBrightness(mContext);
                        return "已经为您调暗屏幕亮度！";
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 处理音量技能
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleVolumeSkill(String settingType, String operationType) {
        Log.d(TAG, "[SettingParseAdapter][handleVolumeSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "关闭":
                case "打开":
                    return "对不起，我不是很明白您说的意思，请换种说法试试！";
                case "增大":
                    if(FunctionUtil.getVolume(mContext) >= 15){
                        return "当前音量已经调为最大！";
                    }else{
                        FunctionUtil.upVoice(mContext);
                        return "已经为您调高音量！";
                    }
                case "减小":
                    if(FunctionUtil.getVolume(mContext) <= 0){
                        return "当前音量已经调为最小！";
                    }else{
                        FunctionUtil.downVoice(mContext);
                        return "已经为您调低音量！";
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 根据字段获取操作类型和设置类型
     * @param type 可选值为：操作类型字段：operation，设置类型字段：type
     * @return
     */
    private String getType(String type) {
        List<SettingBean.SemanticBean> semantic = settingBean.getSemantic();
        if (semantic != null && semantic.size() > 0) {
            for (int i = 0; i < semantic.size(); i++) {
                SettingBean.SemanticBean semanticBean = semantic.get(i);
                if (semanticBean != null) {
                    List<SettingBean.SemanticBean.SlotsBean> slots = semanticBean.getSlots();
                    if (slots != null && slots.size() > 0) {
                        for (int j = 0; j < slots.size(); j++) {
                            SettingBean.SemanticBean.SlotsBean slotsBean = slots.get(j);
                            if (slotsBean != null) {
                                String name = slotsBean.getName();
                                if (name != null && name.equals(type)) {
                                    String types = slotsBean.getNormValue();
                                    if (!TextUtils.isEmpty(types)) {
                                        return types;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}