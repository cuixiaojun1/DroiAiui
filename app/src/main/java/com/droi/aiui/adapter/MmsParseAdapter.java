package com.droi.aiui.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

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
        Log.d(TAG,"allContacts.size = "+allContacts.size());
        String result = handleResult();
        if(TextUtils.isEmpty(result)){
            result = "我现在还不够聪明，暂时不能理解您的意思！";
        }
        return result;
    }

    /**
     * 处理短信业务
     */
    private String handleResult(){
        String result;
        String name = getContactNameByMmsBean();
        if(!TextUtils.isEmpty(name)){
            final String phoneNumber = getPhoneNumberByName(allContacts,name);
            if(!TextUtils.isEmpty(phoneNumber)){
                result = "正在发短信给"+name;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DialerUtils.sendMms(mContext,phoneNumber);
                    }
                },3000);
            }else{
                result = "您要发短信的联系人手机号码为空，请确认之后再重新尝试！";
            }
        }else{
            result = "你要发短信的联系人不存在，请确认之后再重新尝试！";
        }
        return result;
    }

    /**
     * 通过解析得到联系人的名称
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
     * 通过联系人名称获取联系人电话号码
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