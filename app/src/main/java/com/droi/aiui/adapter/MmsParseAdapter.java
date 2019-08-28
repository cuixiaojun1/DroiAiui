package com.droi.aiui.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.droi.aiui.bean.Contact;
import com.droi.aiui.bean.MmsBean;
import com.droi.aiui.controler.DataControler;
import com.droi.aiui.util.DialerUtils;
import com.droi.aiui.util.JsonParserUtil;

import java.util.List;

/**
 * Created by hejianfeng on 2018/01/10.
 */

public class MmsParseAdapter extends BaseParseAdapter {
    private static final String TAG = "MmsParseAdapter";
    private MmsBean mmsBean;
    private Context mContext;
    private List<Contact> allContacts;

    public MmsParseAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public String getSemanticResultText(String json) {
        mmsBean = JsonParserUtil.parseJsonObject(json, MmsBean.class);
        allContacts = DataControler.getInstance(mContext).loadAllContacts();
        String result = handleResult();
        if(TextUtils.isEmpty(result)){
            result = "�����ڻ�������������ʱ�������������˼��";
        }
        return result;
    }

    /**
     * �������ҵ��
     */
    private String handleResult(){
        String result;
        String name = getContactNameByMmsBean();
        if(!TextUtils.isEmpty(name)){
            final String phoneNumber = getPhoneNumberByName(allContacts,name);
            if(!TextUtils.isEmpty(phoneNumber)){
                result = "���ڷ����Ÿ�"+name;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DialerUtils.sendMms(mContext,phoneNumber);
                    }
                },3000);
            }else{
                result = "��Ҫ�����ŵ���ϵ���ֻ�����Ϊ�գ���ȷ��֮�������³��ԣ�";
            }
        }else{
            result = "��Ҫ�����ŵ���ϵ�˲����ڣ���ȷ��֮�������³��ԣ�";
        }
        return result;
    }

    /**
     * ͨ�������õ���ϵ�˵�����
     */
    private String getContactNameByMmsBean(){
        String name = null;
        List<MmsBean.SemanticBean> semantic = mmsBean.getSemantic();
        for (int i = 0; i < semantic.size(); i++) {
            List<MmsBean.SemanticBean.SlotsBean> slots = semantic.get(i).getSlots();
            for (int j = 0; j < slots.size(); j++) {
                if(slots.get(j).getName().equals("contact")){
                    name = slots.get(j).getValue();
                }
            }
        }
        return name;
    }

    /**
     * ͨ����ϵ�����ƻ�ȡ��ϵ�˵绰����
     */
    public String getPhoneNumberByName(List<Contact> contacts, String name) {
        String phoneNumber = null;
        if(contacts != null && contacts.size() != 0){
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).getName().equals(name)) {
                    phoneNumber = contacts.get(i).getPhoneNumber();
                }
            }
        }
        return phoneNumber;
    }
}