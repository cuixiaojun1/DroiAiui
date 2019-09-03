package com.droi.aiui.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.droi.aiui.AiuiManager;
import com.droi.aiui.R;
import com.droi.aiui.bean.Contact;
import com.droi.aiui.bean.PhoneBean;
import com.droi.aiui.controler.DataControler;
import com.droi.aiui.util.DialerUtils;
import com.droi.aiui.util.JsonParserUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuixiaojun on 17-12-18.
 * 针对电话技能进行解析,主要负责处理电话解析结果和处理电话相关的动作。
 */

public class PhoneParaseAdapter extends BaseParseAdapter {

    private final String TAG = "PhoneParaseAdapter";
    //电话解析对象
    private PhoneBean mPhoneBean;
    private Context mContext;
    private static Contact mContact;
    private static List<Contact> contacts = new ArrayList<>();
    private DataControler mDataControler;

    public PhoneParaseAdapter(Context context) {
        this.mContext = context;
        mDataControler = DataControler.getInstance(context);
    }

    @Override
    public String getSemanticResultText(String json) {
        Log.d(TAG,"getSemanticResultText---->json = "+json);
        mPhoneBean = JsonParserUtil.parseJsonObject(json,PhoneBean.class);
        String answer = parseResult();
        if(TextUtils.isEmpty(answer)){
            answer = mContext.getString(R.string.text_no_result);
        }
        return answer;
    }

    /**
     * 根据不同的返回码返回不同的提示语
     */
    private String parseResult(){
        Log.d(TAG,"parseResult--->getRc = "+mPhoneBean.getRc());
        String result = null;
        switch (mPhoneBean.getRc()){
            case 0:
            case 3:
                result = handleDialerIntent();
                break;
            case 1:
                result = "输入异常!";
                break;
            case 2:
                result = "系统内部异常";
                break;
            case 4:
                result = "对不起，不明白您说的是什么意思，请换一种说法试试！";
                break;
                default:
                    break;
        }
        return result;
    }

    /**
     * 处理联系人的状态
     */
    private String handleState(){
        contacts.clear();
        String contactName = getContactNameFromPhoneBean();
        final String contactNumber = getPhoneNumberFromPhoneBean();
        List<String> contactNames = mDataControler.getAllContactNames();
        Log.d(TAG,"[PhoneParseAdapter][handleState]contactName = "+contactName+",contactNumber = "+contactNumber+",contactNames = "+contactNames);
        if(!TextUtils.isEmpty(contactName) && !TextUtils.isEmpty(contactNumber) && contactNames != null){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                    DialerUtils.doCall(mContext,contactNumber);
                    AiuiManager.getInstance(mContext).cancelVoiceNlp();
                }
            },3000);
            return "正在为您呼叫"+contactName;
        }else if(!TextUtils.isEmpty(contactName)){
            contacts = getContactsInfoByName(mDataControler.loadAllContacts(),contactName);
        }else if(!TextUtils.isEmpty(contactNumber)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                    DialerUtils.doCall(mContext,contactNumber);
                    AiuiManager.getInstance(mContext).cancelVoiceNlp();
                }
            },3000);
            return "正在为您呼叫"+contactNumber;
        }else{
            return "我不是很明白您说的是什么意思！";
        }
        Log.d(TAG,"[handleState]getContactState = "+getContactState()+",contactName = "+contactName+",size = "+contacts.size());
        String result = null;
        switch (getContactState()){
            case "moreNumber":
                StringBuffer phoneNumber = new StringBuffer();
                for (int i = 0; i <contacts.size(); i++) {
                    phoneNumber.append("第"+(i+1)+"个号码：\n"+contacts.get(i).getPhoneNumber());
                    phoneNumber.append("\n");
                }
                result = getAnswerText()+"\n"+phoneNumber.toString();
                break;
            case "moreContact":
                StringBuffer contact = new StringBuffer();
                for (int i = 0; i <contacts.size(); i++) {
                    contact.append("第"+(i+1)+"个:"+contacts.get(i).getName()+"\n"+contacts.get(i).getPhoneNumber());
                    contact.append("\n");
                }
                result = getAnswerText()+"\n"+contact.toString();
                break;
            case "oneNumber":
            case "default":
                if(contacts != null && contacts.size() != 0){
                    mContact = contacts.get(0);
                }
                result = handleCallAction();
                break;
                default:
                    break;
        }
        return result;
    }

    /**
     * 根据不同的电话意图处理不同的逻辑
     */
    private String handleDialerIntent(){
        Log.d(TAG,"[handleDialerIntent]getDialerIntent = "+getDialerIntent());
        String returnString = null;
        switch (getDialerIntent()){
            case "DIAL":
            case "RECTIFY":
            case "QUERY":
            case "CANCEL":
                returnString = handleState();
                break;
            case "INSTRUCTION":
                returnString = handleInstruction();
                break;
            default:
                break;
        }
        return returnString;
    }

    /**
     * 处理拨打电话意图
     */
    private String handleCallAction(){
        String returnString;
        if(mContact != null){
            if(!TextUtils.isEmpty(mContact.getName())){
                if(isContactExisted(mDataControler.getAllContactNames(),mContact.getName())){
                    if(!TextUtils.isEmpty(mContact.getPhoneNumber())){
                        returnString = "正在为您呼叫"+mContact.getName();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                                DialerUtils.doCall(mContext,mContact.getPhoneNumber());
                                AiuiManager.getInstance(mContext).cancelVoiceNlp();
                                mContact = null;
                            }
                        },3000);
                    }else{
                        returnString = "您拨打的电话号码为空号，请确认之后重新尝试！";
                    }
                }else{
                    returnString = "对不起，没有在您的手机中找到联系人"+mContact.getName()+"!";
                }
            }else if(!TextUtils.isEmpty(mContact.getPhoneNumber())){
                if(!TextUtils.isEmpty(mContact.getPhoneNumber())){
                    returnString = "正在为您呼叫"+mContact.getPhoneNumber();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                            DialerUtils.doCall(mContext,mContact.getPhoneNumber());
                            AiuiManager.getInstance(mContext).cancelVoiceNlp();
                            mContact = null;
                        }
                    },3000);
                }else{
                    returnString = "您拨打的电话号码为空号，请确认之后重新尝试！";
                }
            }else{
                returnString = "对不起，您要拨打的联系人不存在！";
            }
        }else{
            returnString = "对不起，没有在您的手机中找到该联系人！";
        }
        return returnString;
    }

    /**
     * 处理请求命令
     * @param
     */
    private String handleInstruction(){
        Log.d(TAG,"processInstruction---->getConfirmType = "+getConfirmType());
        String returnString = null;
        switch (getConfirmType()){
            case "CONFIRM":
                returnString = "对不起，我没有听懂你说的是什么意思，您可以换种说法试试！";
                break;
            case "QUIT":
                returnString = mContext.getString(R.string.text_cancel_call);
                AiuiManager.getInstance(mContext).cancelVoiceNlp();
                break;
            case "SEQUENCE":
                int index = Integer.parseInt(getPhoneNumberIndex());
                if(contacts != null && contacts.size() != 0){
                    Log.d(TAG,"SEQUENCE---->index = "+index+",number = "+contacts.size());
                    for (int i = 0; i < contacts.size(); i++) {
                        if(index < 0 || index > contacts.size()){
                            returnString = "对不起，您选择的序号有误，请重新选择！";
                        }else{
                            final Contact contact = contacts.get(index-1);
                            if(!TextUtils.isEmpty(contact.getName()) && !TextUtils.isEmpty(contact.getPhoneNumber())){
                                returnString = "正在为您呼叫"+contact.getName();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                                        DialerUtils.doCall(mContext,contact.getPhoneNumber());
                                        AiuiManager.getInstance(mContext).cancelVoiceNlp();
                                    }
                                }, 3000);
                            }else{
                                returnString = "您拨打的联系人不存在，请确认之后重新尝试！";
                            }
                        }
                    }
                    contacts.clear();
                }else{
                    returnString = "您拨打的联系人不存在，请确认之后重新尝试！";
                }
                break;
            default:
                returnString = mContext.getString(R.string.text_no_result);
                break;
        }
        return returnString;
    }

    /**
     * 获取电话操作相关指令
     * @param
     * @return
     */
    private String getConfirmType(){
        String type = null;
        List<PhoneBean.SemanticBean> semantic = null;
        List<PhoneBean.SemanticBean.SlotsBean> slots = null;
        if(mPhoneBean != null){
            semantic = mPhoneBean.getSemantic();
        }
        if(semantic != null){
            for (int i = 0; i < semantic.size(); i++) {
                slots = semantic.get(i).getSlots();
                for (int j = 0; j < slots.size(); j++) {
                    if(slots.get(j).getName().equals("insType")){
                        type = slots.get(j).getValue();
                    }
                }
            }
        }
        return type;
    }

    /**
     * 获取服务器返回的文本字串
     * @param
     * @return
     */
    private String getAnswerText(){
        return mPhoneBean.getAnswer().getText();
    }

    /**
     *  获取电话意图，总共有四种，分别为：
     *      DIAL        //拨打电话
     *      RECTIFY     //修改联系人、号码
     *      CANCEL      //纠正联系人、号码
     *      INSTRUCTION //操作指令
     */
    private String getDialerIntent(){
        String dialerIntent=null;
        List<PhoneBean.SemanticBean> semantic = null;
        if(mPhoneBean != null){
            semantic = mPhoneBean.getSemantic();
        }
        if(semantic != null){
            for (int i = 0; i < semantic.size(); i++) {
                dialerIntent = semantic.get(i).getIntent();
            }
        }
        return dialerIntent;
    }

    /**
     * 获取特定联系人电话号码的位置
     */
    private String getPhoneNumberIndex(){
        String index = null;
        List<PhoneBean.SemanticBean> semantic = mPhoneBean.getSemantic();
        List<PhoneBean.SemanticBean.SlotsBean> slots;
        for (int i = 0; i < semantic.size(); i++) {
            slots = semantic.get(i).getSlots();
            for (int j = 0; j < slots.size(); j++) {
                String name = slots.get(j).getName();
                if(name.equals("posRank.offset")){
                    index = slots.get(j).getValue();
                }
            }
        }
        return index;
    }

    /**
     * 获取联系人的状态
     */
    private String getContactState(){
        String state = null;
        PhoneBean.UsedStateBean used_state = mPhoneBean.getUsed_state();
        if(used_state != null){
            state = used_state.getState();
        }
        return state;
    }

    /**
     * 通过联系人名称获取联系人电话号码
     */
    private List<Contact> getContactsInfoByName(List<Contact> allContacts,String name){
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < allContacts.size(); i++) {
            Contact contact = allContacts.get(i);
            if(contact.getName().contains(name)){
                contacts.add(contact);
            }
        }
        return contacts;
    }

    /**
     * 获取要拨打的联系人号码
     */
    private String getPhoneNumberFromPhoneBean(){
        String phoneNumber = null;
        List<PhoneBean.SemanticBean> semantic = null;
        List<PhoneBean.SemanticBean.SlotsBean> slots;
        if(mPhoneBean != null){
            semantic = mPhoneBean.getSemantic();
        }
        if(semantic != null){
            for (int i = 0; i < semantic.size(); i++) {
                slots = semantic.get(i).getSlots();
                for (int j = 0; j < slots.size(); j++) {
                    if(slots.get(j).getName().equals("code")){
                        phoneNumber =  slots.get(j).getValue();
                    }
                }
            }
        }
        return phoneNumber;
    }

    /**
     *  获取要拨打的联系人名称
     */
    private String getContactNameFromPhoneBean(){
        String contantName = null;
        List<PhoneBean.SemanticBean> semantic = null;
        List<PhoneBean.SemanticBean.SlotsBean> slots;
        if(mPhoneBean != null){
            semantic = mPhoneBean.getSemantic();
        }
        if(semantic != null){
            for (int i = 0; i < semantic.size(); i++) {
                slots = semantic.get(i).getSlots();
                for (int j = 0; j < slots.size(); j++) {
                    if(slots.get(j).getName().equals("name")){
                        contantName = slots.get(j).getValue();
                    }
                }
            }
        }
        return contantName;
    }

    /**
     * 获取联系人的所有信息
     */
    private ArrayList<Contact> getContactInfoFromPhoneBean(){
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        PhoneBean.DataBean data = null;
        List<PhoneBean.DataBean.ResultBean> result;
        if(mPhoneBean != null){
            data = mPhoneBean.getData();
        }
        if(data != null){
            result = data.getResult();
            if(result != null && result.size() != 0){
                for (int i = 0; i <result.size(); i++) {
                    Contact contact = new Contact();
                    String name = result.get(i).getName();
                    String phoneNumber = result.get(i).getPhoneNumber();
                    String carrier = null;
                    String location = null;
                    contact.setName(name);
                    contact.setPhoneNumber(phoneNumber);
                    if(!TextUtils.isEmpty(phoneNumber)){
                        carrier = DialerUtils.getCarrier(mContext,phoneNumber);
                        location = DialerUtils.getLocation(mContext,phoneNumber);
                    }
                    if(!TextUtils.isEmpty(carrier)){
                        contact.setCarrier(carrier);
                    }else{
                        contact.setCarrier("未知");
                    }
                    if(!TextUtils.isEmpty(location)){
                        contact.setLocation(location);
                    }else{
                        contact.setLocation("未知");
                    }
                    contacts.add(contact);
                }
            }else{
                Contact contact = new Contact();
                String name = getContactNameFromPhoneBean();
                String phoneNumber = getPhoneNumberFromPhoneBean();
                contact.setName(name);
                contact.setPhoneNumber(phoneNumber);
                String carrier = null;
                String location = null;
                if(!TextUtils.isEmpty(phoneNumber)){
                    carrier = DialerUtils.getCarrier(mContext,phoneNumber);
                    location = DialerUtils.getLocation(mContext,phoneNumber);
                }
                if(!TextUtils.isEmpty(carrier)){
                    contact.setCarrier(carrier);
                }else{
                    contact.setCarrier("未知");
                }
                if(!TextUtils.isEmpty(location)){
                    contact.setLocation(location);
                }else{
                    contact.setLocation("未知");
                }
                contacts.add(contact);
            }
        }else{
            Contact contact = new Contact();
            String name = getContactNameFromPhoneBean();
            String phoneNumber = getPhoneNumberFromPhoneBean();
            contact.setName(name);
            contact.setPhoneNumber(phoneNumber);
            String carrier = null;
            String location = null;
            if(!TextUtils.isEmpty(phoneNumber)){
                carrier = DialerUtils.getCarrier(mContext,phoneNumber);
                location = DialerUtils.getLocation(mContext,phoneNumber);
            }
            if(!TextUtils.isEmpty(carrier)){
                contact.setCarrier(carrier);
            }else{
                contact.setCarrier("未知");
            }
            if(!TextUtils.isEmpty(location)){
                contact.setLocation(location);
            }else{
                contact.setLocation("未知");
            }
            contacts.add(contact);
        }
        return contacts;
    }

    private boolean isContactExisted(List<String> allContacts,String contactName){
        for (int i = 0; i < allContacts.size(); i++) {
            if(allContacts.get(i).equals(contactName)){
                return true;
            }
        }
        return false;
    }
}