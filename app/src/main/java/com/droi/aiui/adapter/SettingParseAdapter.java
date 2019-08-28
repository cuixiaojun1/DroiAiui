package com.droi.aiui.adapter;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.camera2.CameraManager;
import android.os.RemoteException;
import android.text.TextUtils;

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
        CappuLog.d(TAG, "[SettingParseAdapter][getSemanticResultText]operationType = " + operationType + ",settingType = " + settingType);
        return handleSettingType(settingType, operationType);
    }

    private String handleSettingType(String settingType, String operationType) {
        CappuLog.d(TAG, "[SettingParseAdapter][handleSettingType]settingType = " + settingType + ",operationType = " + operationType);
        if (settingType != null) {
            switch (settingType) {
                case "�ֵ�Ͳ":
                    return handleTorchSkill(settingType, operationType);
                case "����":
                    return handleBluetoothSkill(settingType, operationType);
                case "����":
                    return handleGPRSSkill(settingType, operationType);
                case "����":
                    return handleWIFISkill(settingType, operationType);
                case "��λ":
                    return handleGPSSkill(settingType, operationType);
                case "����":
                    return handleFontSkill(settingType, operationType);
                case "����":
                    return handleBrightnessSkill(settingType, operationType);
                case "����":
                    return handleVolumeSkill(settingType, operationType);
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * �����ֵ�Ͳ����
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleTorchSkill(String settingType, String operationType) {
        CappuLog.d("cuixiaojun", "[SettingParseAdapter][handleTorchSkill]settingType = " + settingType + ",operationType = " + operationType
                                            +",isHasFlashLight = "+FunctionUtil.isHasFlashLight(mCameraManager)+",getTorchState = "+FunctionUtil.getTorchState(mContext));
        if (operationType != null) {
            switch (operationType) {
                case "�ر�":
                    if (FunctionUtil.isHasFlashLight(mCameraManager)) {
                        if (FunctionUtil.getTorchState(mContext) == 0) {
                            if(FunctionUtil.handelFlashLight(mContext,mCameraManager,false)){
                                return "����Ϊ���ر�" + settingType + "!";
                            }else{
                                return settingType + "�ر�ʧ�ܣ������³��ԣ�";
                            }
                        } else {
                            return "��ǰ" + settingType + "�Ѿ��رգ�";
                        }
                    } else {
                        return "�Բ���δ�������ֻ��м�⵽��ص��豸��";
                    }
                case "��":
                    if (FunctionUtil.isHasFlashLight(mCameraManager)) {
                        if (FunctionUtil.getTorchState(mContext) == 1) {
                            if(FunctionUtil.handelFlashLight(mContext,mCameraManager,true)){
                                return "����Ϊ����"+ settingType + "!";
                            }else{
                                return settingType + "��ʧ�ܣ������³���!";
                            }
                        } else {
                            return "��ǰ" + settingType + "�Ѿ��򿪣�";
                        }
                    } else {
                        return "�Բ���δ�������ֻ��м�⵽��ص��豸��";
                    }
                case "����":
                case "��С":
                    return "�Բ����Ҳ��Ǻ�������˵����˼���뻻��˵�����ԣ�";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * ������������
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleBluetoothSkill(String settingType, String operationType) {
        CappuLog.d(TAG, "[SettingParseAdapter][handleBluetoothSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "�ر�":
                    if (FunctionUtil.isBluetoothEnabled()) {
                        if (FunctionUtil.setBluetoothEnabled(false)) {
                            return "����Ϊ���ر�" + settingType + "��";
                        } else {
                            return "�ر�" + settingType + "ʧ�ܣ������³��ԣ�";
                        }
                    } else {
                        return "��ǰ"+settingType+"�ѹرգ�";
                    }
                case "��":
                    if (FunctionUtil.isBluetoothEnabled()) {
                        return "��ǰ"+settingType+"�Ѵ򿪣�";
                    } else {
                        if (FunctionUtil.setBluetoothEnabled(true)) {
                            return "����Ϊ����"+settingType+"��";
                        } else {
                            return "��"+settingType+"ʧ��,�����³��ԣ�";
                        }
                    }
                case "����":
                case "��С":
                    return "�Բ����Ҳ��Ǻ�������˵����˼���뻻��˵�����ԣ�";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * ������������
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleGPRSSkill(String settingType, String operationType) {
        CappuLog.d(TAG, "[SettingParseAdapter][handleGPRSSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "�ر�":
                    if (FunctionUtil.hasSimCard(mContext)) {
                        if (FunctionUtil.getDataEnabled(mContext)) {
                            FunctionUtil.setDataEnabled(mContext, false);
                            return "����Ϊ���ر�" + settingType + "!";
                        } else {
                            return "��ǰ" + settingType + "�Ѿ��رգ�";
                        }
                    } else {
                        return "��ǰδ��⵽SIM������ȷ���Ƿ���ȷ������SIM����";
                    }
                case "��":
                    if (FunctionUtil.hasSimCard(mContext)) {
                        if(FunctionUtil.getDataEnabled(mContext)){
                            return "��ǰ"+settingType+"�Ѿ��򿪣�";
                        }else{
                            FunctionUtil.setDataEnabled(mContext,true);
                            return "����Ϊ����"+settingType+"!";
                        }
                    } else {
                        return "��ǰδ��⵽SIM������ȷ���Ƿ���ȷ������SIM����";
                    }
                case "����":
                case "��С":
                    return "�Բ����Ҳ��Ǻ�������˵����˼���뻻��˵�����ԣ�";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * ����wifi����
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleWIFISkill(String settingType, String operationType) {
        CappuLog.d(TAG, "[SettingParseAdapter][handleWIFISkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "�ر�":
                    if(FunctionUtil.isWifiEnabled(mContext)){
                        if(FunctionUtil.setWifiEnable(mContext,false)){
                            return "����Ϊ���ر�"+settingType+"!";
                        }else{
                            return "�ر�"+settingType+"ʧ��,�����³��ԣ�";
                        }
                    }else{
                        return "��ǰ"+settingType+"�Ѿ��رգ�";
                    }
                case "��":
                    if(FunctionUtil.isWifiEnabled(mContext)){
                        return "��ǰ"+settingType+"�Ѿ��򿪣�";
                    }else{
                        if(FunctionUtil.setWifiEnable(mContext,true)){
                            return "����Ϊ����"+settingType+"��";
                        }else{
                            return "��"+settingType+"ʧ��,�����³��ԣ�";
                        }
                    }
                case "����":
                case "��С":
                    return "�Բ����Ҳ��Ǻ�������˵����˼���뻻��˵�����ԣ�";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * ����λ����
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleGPSSkill(String settingType, String operationType) {
        CappuLog.d(TAG, "[SettingParseAdapter][handleGPSSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "�ر�":
                    if(FunctionUtil.isLocationEnabled(mContext)){
                        if(FunctionUtil.setLocationEnabled(mContext,false)){
                            return "����Ϊ���ر�"+settingType+"!";
                        }else{
                            return "�ر�"+settingType+"ʧ�ܣ������³��ԣ�";
                        }
                    }else{
                        return "��ǰ"+settingType+"�Ѿ��رգ�";
                    }
                case "��":
                    if(FunctionUtil.isLocationEnabled(mContext)){
                        return "��ǰ"+settingType+"�Ѿ��򿪣�";
                    }else{
                        if(FunctionUtil.setLocationEnabled(mContext,true)){
                            return "����Ϊ����"+settingType+"!";
                        }else{
                            return "��"+settingType+"ʧ�ܣ������³��ԣ�";
                        }
                    }
                case "����":
                case "��С":
                    return "�Բ����Ҳ��Ǻ�������˵����˼���뻻��˵�����ԣ�";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * �������弼��
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleFontSkill(String settingType, String operationType) {
        CappuLog.d(TAG, "[SettingParseAdapter][handleFontSkill]settingType = " + settingType + ",operationType = " + operationType);
        float newFontSize = mContext.getResources().getConfiguration().fontScale;
        CappuLog.d(TAG,"[SettingParseAdapter][handleFontSkill]newFontSize = "+newFontSize);
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
                case "�ر�":
                case "��":
                    return "�Բ����Ҳ��Ǻ�������˵����˼���뻻��˵�����ԣ�";
                case "����":
                    if (newFontSize == small_fontSize) {
                        configuration.fontScale = default_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "�Ѿ�Ϊ����ΪĬ�����壡";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "�������ʧ�ܣ�";
                        }
                    } else if (newFontSize == default_fontSize) {
                        configuration.fontScale = bigger_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "�Ѿ�Ϊ����Ϊ�������壡";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "�������ʧ�ܣ�";
                        }
                    } else if (newFontSize == bigger_fontSize) {
                        configuration.fontScale = biggest_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "�Ѿ�Ϊ����Ϊ������壡";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "�������ʧ�ܣ�";
                        }
                    } else if(newFontSize == biggest_fontSize){
                        return "��ǰ�����Ѿ�Ϊ������壡";
                    } else{
                        return "δʶ��ǰ�����С��";
                    }
                case "��С":
                    if (newFontSize == small_fontSize) {
                        return "��ǰ�����Ѿ�Ϊ��С���壡";
                    } else if (newFontSize == default_fontSize) {
                        configuration.fontScale = small_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "�Ѿ�Ϊ����Ϊ��С���壡";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "�������ʧ�ܣ�";
                        }
                    } else if (newFontSize == bigger_fontSize) {
                        configuration.fontScale = default_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "�Ѿ�Ϊ����ΪĬ�����壡";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "�������ʧ�ܣ�";
                        }
                    } else if(newFontSize == biggest_fontSize){
                        configuration.fontScale = bigger_fontSize;
                        try {
                            ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                            return "�Ѿ�Ϊ����Ϊ�������壡";
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return "�������ʧ�ܣ�";
                        }
                    } else {
                        return "δʶ��ǰ�����С��";
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * ������Ļ���ȼ���
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleBrightnessSkill(String settingType, String operationType) {
        CappuLog.d(TAG, "[SettingParseAdapter][handleBrightnessSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "�ر�":
                case "��":
                    return "�Բ����Ҳ��Ǻ�������˵����˼���뻻��˵�����ԣ�";
                case "����":
                    if(FunctionUtil.getBrightness(mContext) >= 255){
                        return "��ǰ��Ļ�����Ѿ�����������";
                    }else{
                        FunctionUtil.upBrightness(mContext);
                        return "�Ѿ�Ϊ��������Ļ���ȣ�";
                    }
                case "��С":
                    if(FunctionUtil.getBrightness(mContext) <= 0){
                        return "��ǰ��Ļ�����Ѿ���Ϊ���";
                    }else{
                        FunctionUtil.downBrightness(mContext);
                        return "�Ѿ�Ϊ��������Ļ���ȣ�";
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * ������������
     * @param settingType
     * @param operationType
     * @return
     */
    private String handleVolumeSkill(String settingType, String operationType) {
        CappuLog.d(TAG, "[SettingParseAdapter][handleVolumeSkill]settingType = " + settingType + ",operationType = " + operationType);
        if (operationType != null) {
            switch (operationType) {
                case "�ر�":
                case "��":
                    return "�Բ����Ҳ��Ǻ�������˵����˼���뻻��˵�����ԣ�";
                case "����":
                    if(FunctionUtil.getVolume(mContext) >= 15){
                        return "��ǰ�����Ѿ���Ϊ���";
                    }else{
                        FunctionUtil.upVoice(mContext);
                        return "�Ѿ�Ϊ������������";
                    }
                case "��С":
                    if(FunctionUtil.getVolume(mContext) <= 0){
                        return "��ǰ�����Ѿ���Ϊ��С��";
                    }else{
                        FunctionUtil.downVoice(mContext);
                        return "�Ѿ�Ϊ������������";
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * �����ֶλ�ȡ�������ͺ���������
     * @param type ��ѡֵΪ�����������ֶΣ�operation�����������ֶΣ�type
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