package com.droi.aiui.controler;

import android.content.Context;
import android.text.TextUtils;

import com.droi.aiui.Interface.OnParseResultTextListener;
import com.droi.aiui.adapter.AppParseAdapter;
import com.droi.aiui.adapter.BaseParseAdapter;
import com.droi.aiui.adapter.CmdParseAdapter;
import com.droi.aiui.adapter.DefaultParseAdapter;
import com.droi.aiui.adapter.MmsParseAdapter;
import com.droi.aiui.adapter.MusicParseAdapter;
import com.droi.aiui.adapter.OpenQAParseAdapter;
import com.droi.aiui.adapter.PhoneParaseAdapter;
import com.droi.aiui.adapter.RemindParseAdapter;
import com.droi.aiui.adapter.SettingParseAdapter;
import com.droi.aiui.bean.BaseBean;
import com.droi.aiui.bean.Message;
import com.droi.aiui.bean.Sentence;
import com.droi.aiui.util.JsonParserUtil;
import com.iflytek.business.speech.AIUIConstant;
import com.iflytek.business.speech.AIUIEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cuixiaojun on 17-12-18.
 * ����ģʽ����json��������ҷ������Ӧ�Ķ���
 */

public class ParseControler {

    private final String TAG = "ParseControler";

    private OnParseResultTextListener onParseResultTextListener;
    private Context mContext;
    private BaseParseAdapter mBaseParseAdapter;
    public ArrayList<Message> messageList = new ArrayList<Message>();

    /** ��д�þ���� */
    private List<Sentence> mSentences = new ArrayList<Sentence>();

    public ParseControler(Context context) {
        this.mContext = context;
    }

    public void setOnParseResultTextListener(OnParseResultTextListener onParseResultTextListener) {
        this.onParseResultTextListener = onParseResultTextListener;
    }

    /**
     * ���ݷ��ص�json�ִ��������������
     *
     * @param json
     */
    public String handleNlpResult(String json) {
        String serviceType = getServiceType(json);
        if (serviceType != null) {
            switch (getServiceType(json)) {
                case "openQA":
                case "datetime":
                case "train":
                case "flight":
                case "poetry":
                case "baike":
                case "news":
                case "story":
                case "joke":
                case "AIUI.brainTeaser":
                case "animalCries":
                case "englishEveryday":
                case "websearch":
                case "translation":
                case "holiday":
                case "sentenceMaking":
                case "calc":
                case "cookbook":
                case "weather"://Ĭ�Ͻ����������ʴ��
                case "LEIQIAO.cyclopedia":
                case "ZUOMX.queryCapital":
                case "IFLYTEK.OpenQa":
                case "IFLYTEK.constellation":
                case "IFLYTEK.dream":
                case "IFLYTEK.chineseZodiac":
                case "IFLYTEK.app":
                case "IFLYTEK.radio":
                    mBaseParseAdapter = new OpenQAParseAdapter();
                    break;
                case "cmd"://���������
                    mBaseParseAdapter = new CmdParseAdapter();
                    break;
                case "CAPPU.system_settings"://ϵͳ���ü��ܣ����������С����Ļ����,������С
                    mBaseParseAdapter = new SettingParseAdapter(mContext);
                    break;
                case "CAPPU.applacition"://Ӧ�ü���
                    mBaseParseAdapter = new AppParseAdapter(mContext);
                    break;
                case "CAPPU.cappu_mms"://���ż���
                    mBaseParseAdapter = new MmsParseAdapter(mContext);
                    break;
                case "scheduleX"://���Ѽ���
                    mBaseParseAdapter = new RemindParseAdapter(mContext);
                    break;
                case "telephone"://�绰���ܣ�Ĭ��ʹ�õ���Ѷ�ɵĿ��ż���
                    mBaseParseAdapter = new PhoneParaseAdapter(mContext);
                    break;
                case "CAPPU.music_demo"://���ּ��ܣ�Ŀǰֻ֧�ֲ��ű��ز���
                    mBaseParseAdapter = new MusicParseAdapter(mContext);
                    break;
                default://Ĭ��
                    mBaseParseAdapter = new DefaultParseAdapter(mContext);
                    break;
            }
        } else {//�����ȡ��������Ļ���Ĭ����ʾ��ʶ��
            mBaseParseAdapter = new DefaultParseAdapter(mContext);
        }
        return returnResult(mBaseParseAdapter,json);
    }

    /**
     * ��ȡ������д���
     */
    public String getCloudResult(String json){
        String result = null;
        BaseBean mBaseBean = JsonParserUtil.parseJsonObject(json, BaseBean.class);
        if(mBaseBean != null){
            result = mBaseBean.getText();
        }
        return result;
    }

    /**
     * ��ȡ��������
     */
    public String getServiceType(String json){
        BaseBean mBaseBean;
        mBaseBean = JsonParserUtil.parseJsonObject(json, BaseBean.class);
        if (mBaseBean != null) {
            return mBaseBean.getService();
        }
        return null;
    }

    /**
     * ����json�ִ��ķ����룬���ڶ�Ӧ���߼�����
     *
     * "rc"(respond code)
     *      0 	�����ɹ�
     *      1 	�����쳣
     *      2 	ϵͳ�ڲ��쳣
     *      3 	ҵ�����ʧ�ܣ�������Ϣ��error�ֶ�����
     *      4 	�ı�û��ƥ��ļ��ܳ��������ܲ������ܴ�����ı�
     */
    public int getCloudRc(String json){
        int code = -1;
        BaseBean mBaseBean = JsonParserUtil.parseJsonObject(json, BaseBean.class);
        if(mBaseBean != null){
            code = mBaseBean.getRc();
        }
        return code;
    }

    /**
     * ��������¼�����
     * @param event
     */
    public void handleErrorEvent(AIUIEvent event){
        messageList.clear();
        if(event.arg1 == 10118){
            setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT,"������û��˵��Ŷ��");
        }else if(event.arg1 == -1){
            setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT,"��û���������˵�Ļ�������˵һ�Σ�");
        }else if(event.arg1 == 10120){
            setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT,"�Բ��𣬵�ǰ���粻�ã������ԣ�");
        }
    }

    /**
     * ����AIUI���صĽ��
     * @param event
     */
    public void handleEventResult(Map<String,String> result,AIUIEvent event){
        // ��AIUIConstant.KEY_INTENT_ENGINE_TYPE��ΪAIUIConstant.ENGINE_TYPE_MIXED
        // ���ƶ˺ͱ��ؽ����������򱨴��ɿͻ����Լ�����ȡ�ᡣ
        String bizParams = event.bundle.getString(AIUIConstant.KEY_INFO);
        try {
            JSONObject bizParamsJson = new JSONObject(bizParams);
            JSONObject data = bizParamsJson.getJSONArray("data").getJSONObject(0);
            JSONObject params = data.getJSONObject("params");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);
            if (content.has("cnt_id")) {
                String cnt_id = content.getString("cnt_id");
                JSONObject cntJson = new JSONObject(
                        new String(event.bundle.getByteArray(cnt_id), "utf-8"));
                String sub = params.optString("sub");
                if ("iat".equals(sub)) {
                    // �����õ���д���
                    String strResult = cntJson.optString("text");
                    //result.put(AIUIControler.KEY_IAT_RESULT,strResult);
                    //JeffLog.d("cuixiaojun", "������д========== "+strResult);
                } else if ("nlp".equals(sub)) {
                    // �����õ�������
                    String strResult = cntJson.optString("intent");
                    //cloudReturnResult = handleNlpResult(strResult);
                    result.put(AIUIControler.KEY_CLOUD_RESULT,strResult);
                } else if ("asr".equals(sub)) {
                    // �����õ����������ʶ����
                    String strResult = cntJson.optString("intent");
                    result.put(AIUIControler.KEY_LOCAL_RESULT,strResult);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private String returnResult(BaseParseAdapter baseParaseAdapter,String json) {
        String result = null;
        if(baseParaseAdapter != null){
            result = baseParaseAdapter.getSemanticResultText(json);
        }
        if(TextUtils.isEmpty(result)){
            result = "�Բ�����û�������������˵һ�飡";
        }
        return result;
    }

    /**
     * ���÷�����Ϣ
     */
    public void setMessage(Message.MsgType msgType, Message.FromType fromType, String message){
        messageList.clear();
        Message temp = new Message();
        temp.msgType = msgType;
        temp.fromType = fromType;
        temp.setText(message);
        messageList.add(temp);
        if (onParseResultTextListener != null){
            onParseResultTextListener.onParseResult(messageList);
        }
    }

    /**
     * ������д���
     */
    public String handleIatResult(String json) {
        try {
            JSONObject joText = new JSONObject(json);

            // �����ı�����
            String txt = "";
            JSONArray txtJsonArray = joText.getJSONArray("ws");
            for (int i = 0; i < txtJsonArray.length(); i++) {
                JSONObject txtJson = txtJsonArray.getJSONObject(i);
                JSONArray cw = txtJson.getJSONArray("cw");
                txt += cw.getJSONObject(0).getString("w");
            }

            // ��������
            int sn = joText.getInt("sn");
            /* ��д��̬���� begin */
            String pgs = joText.optString("pgs");
            // �����ʵʱ������̬��������û��pgs�ֶΡ�
            if ("apd".equals(pgs) || TextUtils.isEmpty(pgs)) {
                /* ��д��̬���� end */
                // ׷��
                Sentence sentence = new Sentence();
                sentence.mText = txt;
                sentence.mSns.add(sn);
                mSentences.add(sentence);
                /* ��д��̬���� begin */
            } else if ("rpl".equals(pgs)) {
                // �滻
                // ����滻���䣬snΪ��λ
                JSONArray rgArray = joText.getJSONArray("rg");
                int rgBegin = rgArray.getInt(0);
                int rgEnd = rgArray.getInt(1);
                // ���������䷶Χ�ڵľ���
                boolean isFirstFound = false;
                for (Sentence tmpSentence : mSentences) {
                    for (int tmpSn : tmpSentence.mSns) {
                        if (rgBegin <= tmpSn && rgEnd >= tmpSn) {
                            // ���ҵ��ĵ�һ���滻����������
                            if (!isFirstFound) {
                                isFirstFound = true;
                                tmpSentence.mText = txt;
                                tmpSentence.mSns.add(sn);
                            } else {
                                tmpSentence.mText = "";
                            }
                            break;
                        }
                    }
                }
            }
            /* ��д��̬���� end */

            // չʾ���
            StringBuilder totalTextBuilder = new StringBuilder();
            for (Sentence tmpSentence : mSentences) {
                totalTextBuilder.append(tmpSentence.mText);
            }
            // �Ƿ����һ��
            boolean ls = joText.getBoolean("ls");
            if (ls) {
                // �浽�ȶ����ı��У���վ����
                mSentences.clear();
            }
            String totalText = totalTextBuilder.toString();
            if(!TextUtils.isEmpty(totalText)){
                return totalText;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}