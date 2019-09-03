package com.droi.aiui.controler;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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
 * 单例模式解析json结果，并且返回相对应的对象
 */

public class ParseControler {

    private final String TAG = "ParseControler";

    private OnParseResultTextListener onParseResultTextListener;
    private Context mContext;
    private BaseParseAdapter mBaseParseAdapter;
    public ArrayList<Message> messageList = new ArrayList<Message>();

    /** 听写用句队列 */
    private List<Sentence> mSentences = new ArrayList<Sentence>();

    public ParseControler(Context context) {
        this.mContext = context;
    }

    public void setOnParseResultTextListener(OnParseResultTextListener onParseResultTextListener) {
        this.onParseResultTextListener = onParseResultTextListener;
    }

    /**
     * 根据返回的json字串解析语义理解结果
     *
     * @param json
     */
    public String handleNlpResult(String json) {
        String serviceType = getServiceType(json);
        Log.d(TAG, "[ParseControler][handleNlpResult]serviceType = " + serviceType + ",rc = " + getCloudRc(json));
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
                case "weather"://默认解析，自由问答库
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
                case "cmd"://命令解析器
                    mBaseParseAdapter = new CmdParseAdapter();
                    break;
                case "CAPPU.system_settings"://系统设置技能，包括字体大小，屏幕亮度,声音大小
                    mBaseParseAdapter = new SettingParseAdapter(mContext);
                    break;
                case "CAPPU.applacition"://应用技能
                    mBaseParseAdapter = new AppParseAdapter(mContext);
                    break;
                case "CAPPU.cappu_mms"://短信技能
                    mBaseParseAdapter = new MmsParseAdapter(mContext);
                    break;
                case "scheduleX"://提醒技能
                    mBaseParseAdapter = new RemindParseAdapter(mContext);
                    break;
                case "telephone"://电话技能，默认使用的是讯飞的开放技能
                    mBaseParseAdapter = new PhoneParaseAdapter(mContext);
                    break;
                case "CAPPU.music_demo"://音乐技能，目前只支持播放本地播放
                    mBaseParseAdapter = new MusicParseAdapter(mContext);
                    break;
                default://默认
                    mBaseParseAdapter = new DefaultParseAdapter(mContext);
                    break;
            }
        } else {//如果获取不到服务的话，默认提示不识别
            mBaseParseAdapter = new DefaultParseAdapter(mContext);
        }
        return returnResult(mBaseParseAdapter,json);
    }

    /**
     * 获取在线听写结果
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
     * 获取技能类型
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
     * 解析json字串的返回码，用于对应的逻辑处理
     *
     * "rc"(respond code)
     *      0 	操作成功
     *      1 	输入异常
     *      2 	系统内部异常
     *      3 	业务操作失败，错误信息在error字段描述
     *      4 	文本没有匹配的技能场景，技能不理解或不能处理该文本
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
     * 处理错误事件返回
     * @param event
     */
    public void handleErrorEvent(AIUIEvent event){
        messageList.clear();
        if(event.arg1 == 10118){
            setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT,"您好像没有说话哦！");
        }else if(event.arg1 == -1){
            setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT,"我没有听清楚您说的话，请再说一次！");
        }else if(event.arg1 == 10120){
            setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT,"对不起，当前网络不好，请重试！");
        }
    }

    /**
     * 处理AIUI返回的结果
     * @param event
     */
    public void handleEventResult(Map<String,String> result,AIUIEvent event){
        // 若AIUIConstant.KEY_INTENT_ENGINE_TYPE设为AIUIConstant.ENGINE_TYPE_MIXED
        // ，云端和本地结果都会给出或报错，由客户端自己控制取舍。
        Log.d(TAG,"handleEventResult");
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
                    // 解析得到听写结果
                    String strResult = cntJson.optString("text");
                    //result.put(AIUIControler.KEY_IAT_RESULT,strResult);
                    //Log.d("cuixiaojun", "在线听写========== "+strResult);
                } else if ("nlp".equals(sub)) {
                    // 解析得到语义结果
                    String strResult = cntJson.optString("intent");
                    //cloudReturnResult = handleNlpResult(strResult);
                    result.put(AIUIControler.KEY_CLOUD_RESULT,strResult);
                    Log.d(TAG,"在线结果============ ： "+strResult);
                } else if ("asr".equals(sub)) {
                    // 解析得到本地命令词识别结果
                    String strResult = cntJson.optString("intent");
                    Log.d(TAG,"本地结果============ ： "+strResult);
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
            result = "对不起，我没有听清楚，请再说一遍！";
        }
        return result;
    }

    /**
     * 设置返回消息
     */
    public void setMessage(Message.MsgType msgType,Message.FromType fromType,String message){
        Log.d(TAG,"setMessage---->message = "+message);
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
     * 处理听写结果
     */
    public String handleIatResult(String json) {
        try {
            JSONObject joText = new JSONObject(json);

            // 解析文本内容
            String txt = "";
            JSONArray txtJsonArray = joText.getJSONArray("ws");
            for (int i = 0; i < txtJsonArray.length(); i++) {
                JSONObject txtJson = txtJsonArray.getJSONObject(i);
                JSONArray cw = txtJson.getJSONArray("cw");
                txt += cw.getJSONObject(0).getString("w");
            }

            // 结果的序号
            int sn = joText.getInt("sn");
            /* 听写动态修正 begin */
            String pgs = joText.optString("pgs");
            // 如果非实时上屏动态修正，就没有pgs字段。
            if ("apd".equals(pgs) || TextUtils.isEmpty(pgs)) {
                /* 听写动态修正 end */
                // 追加
                Sentence sentence = new Sentence();
                sentence.mText = txt;
                sentence.mSns.add(sn);
                mSentences.add(sentence);
                /* 听写动态修正 begin */
            } else if ("rpl".equals(pgs)) {
                // 替换
                // 结果替换区间，sn为单位
                JSONArray rgArray = joText.getJSONArray("rg");
                int rgBegin = rgArray.getInt(0);
                int rgEnd = rgArray.getInt(1);
                // 查找在区间范围内的句子
                boolean isFirstFound = false;
                for (Sentence tmpSentence : mSentences) {
                    for (int tmpSn : tmpSentence.mSns) {
                        if (rgBegin <= tmpSn && rgEnd >= tmpSn) {
                            // 把找到的第一句替换，其余的清空
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
            /* 听写动态修正 end */

            // 展示结果
            StringBuilder totalTextBuilder = new StringBuilder();
            for (Sentence tmpSentence : mSentences) {
                totalTextBuilder.append(tmpSentence.mText);
            }
            // 是否最后一个
            boolean ls = joText.getBoolean("ls");
            if (ls) {
                // 存到稳定的文本中，清空句队列
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