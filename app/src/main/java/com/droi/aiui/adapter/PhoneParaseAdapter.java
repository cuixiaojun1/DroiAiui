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
 * ��Ե绰���ܽ��н���,��Ҫ������绰��������ʹ���绰��صĶ�����
 */

public class PhoneParaseAdapter extends BaseParseAdapter {

    private final String TAG = "PhoneParaseAdapter";
    //�绰��������
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
        mPhoneBean = JsonParserUtil.parseJsonObject(json, PhoneBean.class);
        String answer = parseResult();
        if(TextUtils.isEmpty(answer)){
            answer = mContext.getString(R.string.text_no_result);
        }
        return answer;
    }

    /**
     * ���ݲ�ͬ�ķ����뷵�ز�ͬ����ʾ��
     */
    private String parseResult(){
        String result = null;
        switch (mPhoneBean.getRc()){
            case 0:
            case 3:
                result = handleDialerIntent();
                break;
            case 1:
                result = "�����쳣!";
                break;
            case 2:
                result = "ϵͳ�ڲ��쳣";
                break;
            case 4:
                result = "�Բ��𣬲�������˵����ʲô��˼���뻻һ��˵�����ԣ�";
                break;
                default:
                    break;
        }
        return result;
    }

    /**
     * ������ϵ�˵�״̬
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
            return "����Ϊ������"+contactName;
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
            return "����Ϊ������"+contactNumber;
        }else{
            return "�Ҳ��Ǻ�������˵����ʲô��˼��";
        }
        String result = null;
        switch (getContactState()){
            case "moreNumber":
                StringBuffer phoneNumber = new StringBuffer();
                for (int i = 0; i <contacts.size(); i++) {
                    phoneNumber.append("��"+(i+1)+"�����룺\n"+contacts.get(i).getPhoneNumber());
                    phoneNumber.append("\n");
                }
                result = getAnswerText()+"\n"+phoneNumber.toString();
                break;
            case "moreContact":
                StringBuffer contact = new StringBuffer();
                for (int i = 0; i <contacts.size(); i++) {
                    contact.append("��"+(i+1)+"��:"+contacts.get(i).getName()+"\n"+contacts.get(i).getPhoneNumber());
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
     * ���ݲ�ͬ�ĵ绰��ͼ����ͬ���߼�
     */
    private String handleDialerIntent(){
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
     * ������绰��ͼ
     */
    private String handleCallAction(){
        String returnString;
        if(mContact != null){
            if(!TextUtils.isEmpty(mContact.getName())){
                if(isContactExisted(mDataControler.getAllContactNames(),mContact.getName())){
                    if(!TextUtils.isEmpty(mContact.getPhoneNumber())){
                        returnString = "����Ϊ������"+mContact.getName();
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
                        returnString = "������ĵ绰����Ϊ�պţ���ȷ��֮�����³��ԣ�";
                    }
                }else{
                    returnString = "�Բ���û���������ֻ����ҵ���ϵ��"+mContact.getName()+"!";
                }
            }else if(!TextUtils.isEmpty(mContact.getPhoneNumber())){
                if(!TextUtils.isEmpty(mContact.getPhoneNumber())){
                    returnString = "����Ϊ������"+mContact.getPhoneNumber();
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
                    returnString = "������ĵ绰����Ϊ�պţ���ȷ��֮�����³��ԣ�";
                }
            }else{
                returnString = "�Բ�����Ҫ�������ϵ�˲����ڣ�";
            }
        }else{
            returnString = "�Բ���û���������ֻ����ҵ�����ϵ�ˣ�";
        }
        return returnString;
    }

    /**
     * ������������
     * @param
     */
    private String handleInstruction(){
        String returnString = null;
        switch (getConfirmType()){
            case "CONFIRM":
                returnString = "�Բ�����û��������˵����ʲô��˼�������Ի���˵�����ԣ�";
                break;
            case "QUIT":
                returnString = mContext.getString(R.string.text_cancel_call);
                AiuiManager.getInstance(mContext).cancelVoiceNlp();
                break;
            case "SEQUENCE":
                int index = Integer.parseInt(getPhoneNumberIndex());
                if(contacts != null && contacts.size() != 0){
                    for (int i = 0; i < contacts.size(); i++) {
                        if(index < 0 || index > contacts.size()){
                            returnString = "�Բ�����ѡ����������������ѡ��";
                        }else{
                            final Contact contact = contacts.get(index-1);
                            if(!TextUtils.isEmpty(contact.getName()) && !TextUtils.isEmpty(contact.getPhoneNumber())){
                                returnString = "����Ϊ������"+contact.getName();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                                        DialerUtils.doCall(mContext,contact.getPhoneNumber());
                                        AiuiManager.getInstance(mContext).cancelVoiceNlp();
                                    }
                                }, 3000);
                            }else{
                                returnString = "���������ϵ�˲����ڣ���ȷ��֮�����³��ԣ�";
                            }
                        }
                    }
                    contacts.clear();
                }else{
                    returnString = "���������ϵ�˲����ڣ���ȷ��֮�����³��ԣ�";
                }
                break;
            default:
                returnString = mContext.getString(R.string.text_no_result);
                break;
        }
        return returnString;
    }

    /**
     * ��ȡ�绰�������ָ��
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
     * ��ȡ���������ص��ı��ִ�
     * @param
     * @return
     */
    private String getAnswerText(){
        return mPhoneBean.getAnswer().getText();
    }

    /**
     *  ��ȡ�绰��ͼ���ܹ������֣��ֱ�Ϊ��
     *      DIAL        //����绰
     *      RECTIFY     //�޸���ϵ�ˡ�����
     *      CANCEL      //������ϵ�ˡ�����
     *      INSTRUCTION //����ָ��
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
     * ��ȡ�ض���ϵ�˵绰�����λ��
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
     * ��ȡ��ϵ�˵�״̬
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
     * ͨ����ϵ�����ƻ�ȡ��ϵ�˵绰����
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
     * ��ȡҪ�������ϵ�˺���
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
     *  ��ȡҪ�������ϵ������
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
     * ��ȡ��ϵ�˵�������Ϣ
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
                        contact.setCarrier("δ֪");
                    }
                    if(!TextUtils.isEmpty(location)){
                        contact.setLocation(location);
                    }else{
                        contact.setLocation("δ֪");
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
                    contact.setCarrier("δ֪");
                }
                if(!TextUtils.isEmpty(location)){
                    contact.setLocation(location);
                }else{
                    contact.setLocation("δ֪");
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
                contact.setCarrier("δ֪");
            }
            if(!TextUtils.isEmpty(location)){
                contact.setLocation(location);
            }else{
                contact.setLocation("δ֪");
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