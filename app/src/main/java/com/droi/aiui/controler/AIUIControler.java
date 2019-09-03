package com.droi.aiui.controler;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.droi.aiui.Interface.OnVolumeChangedListener;
import com.droi.aiui.adapter.LocalParseAdapter;
import com.droi.aiui.bean.Message;
import com.droi.aiui.util.FunctionUtil;
import com.iflytek.business.speech.AIUIAgent;
import com.iflytek.business.speech.AIUIConstant;
import com.iflytek.business.speech.AIUIEvent;
import com.iflytek.business.speech.AIUIListener;
import com.iflytek.business.speech.AIUIMessage;
import com.iflytek.business.speech.RecognitionListener;
import com.iflytek.business.speech.RecognizerResult;
import com.iflytek.business.speech.SpeechError;
import com.iflytek.business.speech.SpeechIntent;
import com.iflytek.business.speech.SpeechServiceUtil;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuixiaojun on 2017-12-21.
 * AIUI控制器，主要用来控制语音的识别和语义理解的返回
 */

public class AIUIControler{

    private final String TAG = "AIUIControler";
    public static final String KEY_LOCAL_RESULT = "local_result";
    public static final String KEY_CLOUD_RESULT = "cloud_result";
    public static final String KEY_IAT_RESULT = "iat_result";
    /*返回结果处理消息*/
    private final int MSG_HANDLE_RESULT = 0;
    /*音量大小回调接口*/
    private OnVolumeChangedListener onVolumeChangedListener;
    /*上下文*/
    private Context mContext;
    /*语音服务*/
    private SpeechServiceUtil mService;
    /*AIUI代理*/
    private AIUIAgent mAIUIAgent;
    /*识别引擎的采样率支持16k和8k*/
    private final int SAMPLE_RATE_16K = 16000;
    private final int SAMPLE_RATE_8K = 8000;
    /*解析格式*/
    private final String CHARSET_NAME = "utf-8";
    /*参数集*/
    private JSONObject mJoParams;
    /*识别引擎的采样率支持16k和8k*/
    private int mSampleRate = SAMPLE_RATE_16K;
    /*识别引擎类型 目前支持三种，在线，离线，混合，默认为混合模式*/
    //private String mIntentEngineType = AIUIConstant.ENGINE_TYPE_CLOUD;
    //private String mIntentEngineType = AIUIConstant.ENGINE_TYPE_LOCAL;
    private String mIntentEngineType = AIUIConstant.ENGINE_TYPE_MIXED;
    /*本地识别解析器*/
    private LocalParseAdapter mLocalParseAdapter;
    /*在线结果解析器*/
    public ParseControler mParseControler;
    /*返回结果集*/
    private Map<String,String> resultMap = new HashMap<String, String>();
    /*返回结果次数*/
    private int resultCount = 1;
    /*语音识别初始化成功标志位*/
    private boolean isRecognitionInitSuccess;
    /*语法文件的数据上传是否成功*/
    private boolean isGrammarUploadSuccess;
    private DataControler mDataControler;

    public AIUIControler(Context context, SpeechServiceUtil service) {
        mService = service;
        mContext = context;
        //解析控制器
        mParseControler = new ParseControler(context);
        mLocalParseAdapter = new LocalParseAdapter(context);
        mDataControler = DataControler.getInstance(context);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_RESULT:
                    String localString = resultMap.get(KEY_LOCAL_RESULT);
                    String cloudString = resultMap.get(KEY_CLOUD_RESULT);
                    handleResurnResult(localString,cloudString);
                    resultCount = 1;
                    resultMap.clear();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * AIUI回调接口
     */
    private final AIUIListener mAIUIListener = new AIUIListener() {
        /**
         * @param event
         * AIUIEvent{
        　      int eventType; //事件类型
        　　    int arg1;      //参数1
        　　    int arg2;      //参数2
        　　    String info;
        　　    Bundle data;
        　}
         */
        @Override
        public void onEvent(AIUIEvent event) {
            // 事件处理和结果解析
            switch (event.eventType) {
                //唤醒事件
                case AIUIConstant.EVENT_WAKEUP:
                    break;
                /**
                 * 服务状态事件
                 * 当向AIUI发送CMD_GET_STATE命令时抛出该事件,
                 *arg1字段取值为:
                 *服务状态事件,总共有三种状态，
                 *1=>STATE_IDLE，闲置状态，AIUI未开启,此时只能进行start（开启服务）操作。
                 *2=>STATE_READY，就绪状态，AIUI已就绪,等待用户唤醒状态,向服务发送CMD_WAKEUP消息唤醒服务。
                 *                调用AIUIAgent.createAgent创建对象之后，服务即为就绪状态。
                 *3=>STATE_WORKING，唤醒后，服务进入工作状态,AIUI工作中，可进行交互,
                 *                  此时可以输入语音、文本与AIUI后台进行交互。
                 */
                case AIUIConstant.EVENT_STATE: {
                    if (AIUIConstant.STATE_IDLE == event.arg1) {
                        // 闲置状态，AIUI未开启
                        Log.d(TAG, "AIUI状态：空闲");
                    } else if (AIUIConstant.STATE_READY == event.arg1) {
                        // AIUI已就绪，等待唤醒
                        Log.d(TAG, "AIUI状态：就绪");
                    } else if (AIUIConstant.STATE_WORKING == event.arg1) {
                        // AIUI工作中，可进行交互
                        Log.d(TAG, "AIUI状态：工作");
                    }
                }
                break;
                /**
                 * 结果事件（包含听写，语义，离线语法结果，定义和解析格式参见2.2.2 AIUIEvent一节）
                 * 取值--->1,data字段携带结果数据，info字段为描述数据的JSON字符串。
                 */
                case AIUIConstant.EVENT_RESULT: {
                    // 若AIUIConstant.KEY_INTENT_ENGINE_TYPE设为AIUIConstant.ENGINE_TYPE_MIXED
                    // ，云端和本地结果都会给出或报错，由客户端自己控制取舍。
                    Log.d(TAG,"AIUIConstant.EVENT_RESULT");
                    handler.removeMessages(MSG_HANDLE_RESULT);
                    if(mParseControler != null){
                        mParseControler.handleEventResult(resultMap,event);
                    }
                    Log.d(TAG,">>>>>>>>>>>isNetworkAvailable = "+FunctionUtil.isNetworkAvailable(mContext)+",isGrammarUploadSuccess = "+isGrammarUploadSuccess+",isRecognitionInitSuccess = "+isRecognitionInitSuccess+",resultCount = "+resultCount);
                    if (!FunctionUtil.isNetworkAvailable(mContext)) {
                        handler.sendEmptyMessage(MSG_HANDLE_RESULT);
                    } else {
                        if (isGrammarUploadSuccess && isRecognitionInitSuccess) {
                            if (resultCount == 3) {
                                handler.sendEmptyMessage(MSG_HANDLE_RESULT);
                            }
                        } else {
                            if (resultCount ==2 ) {
                                handler.sendEmptyMessage(MSG_HANDLE_RESULT);
                            }
                        }
                    }
                    resultCount++;
                }
                break;

                //错误事件,取值--->2
                //arg1字段为错误码，info字段为错误描述信息。
                case AIUIConstant.EVENT_ERROR: {
                    // 如果info字段不为null，其中可能有[engine_type=cloud]或[engine_type=local]信息，分别表示云端错误或本地错误。
                    String info = event.bundle.getString(AIUIConstant.KEY_INFO);
                    Log.v(TAG, "错误事件: " + event.eventType + ",错误码 = " + event.arg1 + ",错误信息 = " + info);
                    resultCount = 1;
                    if(mParseControler != null){
                        mParseControler.handleErrorEvent(event);
                    }
                }
                break;
                /**
                 * CMD_UPLOAD_LEXICON:上传用户识别热词,取值-->11,AIUI的识别热词，
                 *                      可以让AIUI在识别时优先识别成热词中的内容。
                 *                  比如用户说`yàn jīng pí jiǔ`，在没有上传热词的情况下
                 *                  很大概率会被通用模型识别成`眼睛啤酒`，但是当开发者上传了`燕京啤酒`的热词，则识别成功率会显著提升。
                 *                  AIUI热词分为两种，一种是用户级别热词，通过CMD_UPLOAD_LEXICON上传， 仅对上传该热词的设备生效。
                 *                  一种是应用热词，通过AIUI后台应用管理界面中上传，对使用APPID配置的所有设备生效。
                 *                  两种类型热词共同生效，互相不会覆盖，同类型热词第二次上传会覆盖生效。
                 *                  无论是哪种热词，热词的生效时间为10~60分钟。
                 *CMD_RESULT_VALIDATION_ACK:结果确认,取值-->20
                 *                      在接收到语义、听写、后处理的结果后5s内可发送该指令对结果进行确认，
                 *                      AIUI会认为该条结果有效，并重新开始AIUI交互超时的计时
                 *                      关于交互超时的机制参看AIUI配置中interact_timeout的解释
                 *CMD_CLEAN_DIALOG_HISTORY:清空交互历史,取值-->21
                 */
                case AIUIConstant.EVENT_CMD_RETURN: {
                    Log.d(TAG,"AIUIConstant.EVENT_CMD_RETURN--->event.arg1 = "+event.arg1+",event.arg2 = "+event.arg2+",dtype = "+event.bundle.getInt("sync_dtype")+",tag = "+event.bundle.getString(AIUIConstant.KEY_TAG));
                    // 同步数据类型
                    if (AIUIConstant.CMD_UPLOAD_LEXICON == event.arg1) {
                        //热词上传
                        Log.d(TAG, "热词上传:" + (0 == event.arg2 ? "成功" : "失败"));
                    } else if (AIUIConstant.CMD_SYNC == event.arg1) {//数据同步，动态实体上传
                        int dtype = event.bundle.getInt("sync_dtype");
                        // arg2表示错误码
                        if (AIUIConstant.SYNC_DATA_SCHEMA == dtype) {
                            //给出调用同步数据时设置的标签，用于区分请求。
                            String tag = event.bundle.getString(AIUIConstant.KEY_TAG);
                            // arg2是错误码
                            if (0 == event.arg2) { // 同步成功
                                // 注：上传成功并不表示数据打包成功，打包成功与否应以同步状态查询结果为准
                                // ，数据只有打包成功后才能正常使用
                                String mSyncSid = event.bundle.getString("sid");
                                queryDataStatus(mSyncSid);
                                Log.d(TAG,"schema数据同步成功, sid = " + mSyncSid + ", tag = " + tag);
                            } else {
                                Log.d(TAG,"schema数据同步出错: " + event.arg2 + ", tag = " + tag);
                            }
                        }
                    } else if (AIUIConstant.CMD_QUERY_SYNC_STATUS == event.arg1) {//查询动态实体打包状态
                        int syncType = event.bundle.getInt("sync_dtype");
                        if (AIUIConstant.SYNC_DATA_QUERY == syncType) {
                            String result = event.bundle.getString("result");
                            if (0 == event.arg2) {
                                Log.d(TAG, "查询结果：" + result);
                            } else {
                                Log.d(TAG, "动态实体数据状态查询出错：" + event.arg2 + ", result:" + result);
                            }
                        }
                    }
                }
                break;
                /**
                 * VAD事件
                 * 当检测到输入音频的前端点后，会抛出该事件，
                 *用arg1标识前后端点或者音量信息:0(前端点)、1(音量)、2(后端点)。
                 *当arg1取值为1时，arg2为音量大小。
                 */
                case AIUIConstant.EVENT_VAD: {
                    switch (event.arg1) {
                        case AIUIConstant.VAD_VOL: {
                            //监听音量的变化
                            if (onVolumeChangedListener != null) {
                                onVolumeChangedListener.onVolumeChanged(event.arg2);
                            }
                            break;
                        }
                        case AIUIConstant.VAD_BOS: {
                            break;
                        }
                        case AIUIConstant.VAD_EOS: {
                            break;
                        }
                        case AIUIConstant.VAD_BOS_TIMEOUT: {
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
                break;
                //开始录音事件,取值：11--->抛出该事件通知外部录音开始，用户可以开始说话
                case AIUIConstant.EVENT_START_RECORD: {
                }
                break;
                //停止录音事件，取值：12--->通知外部录音结束
                case AIUIConstant.EVENT_STOP_RECORD: {
                }
                break;
                case AIUIConstant.EVENT_SLEEP: {
                }
                break;
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:{
                    Log.d(TAG, "AIUIConstant.EVENT_CONNECTED_TO_SERVER");
                    //开始上传应用信息
                    checkDataAppUpdate(mDataControler);
                    //开始上传联系人
                    checkDataContactsUpdate(mDataControler);
                }
                break;
                case AIUIConstant.EVENT_SERVER_DISCONNECTED:{
                    Log.d(TAG, "AIUIConstant.EVENT_SERVER_DISCONNECTED");
                }
                break;
            }
        }
    };

    /**
     * 获取语义识别回调接口
     */
    public RecognitionListener getRecognitionListener() {
        return mRecognitionListener;
    }

    /**
     * 获取解析控制器
     */
    public ParseControler getParseControler() {
        return mParseControler;
    }

    /**
     * 语义识别回调接口
     */
    private final RecognitionListener mRecognitionListener = new RecognitionListener.Stub() {
        @Override
        public void onInit(int errorCode) throws RemoteException {
            Log.d(TAG, "RecognitionListener--->onInit--->errorCode = " + errorCode);
            if (SpeechError.SUCCESS == errorCode) {
                isRecognitionInitSuccess = true;
                Log.d(TAG, "初始化识别引擎成功");
            } else if (SpeechError.ERROR_GRAMMAR_NO_NEED_REBUILD == errorCode) {
                Log.d(TAG, "初始化识别引擎成功,语法不需要重新构建");
            } else {
                Log.d(TAG, "初始化识别引擎失败，错误码：" + errorCode);
            }
            if (null != mService) {
                //获取参数集
                Bundle bundle = getAIUIParams();
                if (null == bundle) {
                    return;
                }
                // 创建AIUIAgent
                mAIUIAgent = mService.createAIUIAgent(bundle, mAIUIListener);
                if (null == mAIUIAgent) {
                    return;
                }
                // 启动AIUI
                AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null);
                mAIUIAgent.sendMessage(aiuiMsg);
                Log.d(TAG,"mAIUIAgent初始化完成！");
                //开始同步本地数据至服务器，供离线识别使用
                //同步联系人数据，包括本地电话技能，短信技能和联系人技能。
                uploadLocalData(new String[] { "call","sms"},"<contact>",mDataControler.getAllContactNames());
                uploadLocalData(new String[] { "app"},"<appName>",mDataControler.getAllAppNames());
                uploadLocalData(new String[] { "music"},"<singerName>",mDataControler.getAllSongNamesOrSingerNames(DataControler.TYPE_SONG_SINGER));
                uploadLocalData(new String[] { "music"},"<songName>",mDataControler.getAllSongNamesOrSingerNames(DataControler.TYPE_SONG_NAME));
            }
        }

        @Override
        public void onVolumeGet(int vol) throws RemoteException {
        }

        @Override
        public void onSpeechStart() throws RemoteException {
        }

        @Override
        public void onSpeechEnd() throws RemoteException {
        }

        @Override
        public void onResult(RecognizerResult result) throws RemoteException {
        }

        @Override
        public void onRecordStart() throws RemoteException {
        }

        @Override
        public void onRecordEnd() throws RemoteException {
        }

        @Override
        public void onError(int errorCode) throws RemoteException {
        }

        @Override
        public void onPartialResult(RecognizerResult result) throws RemoteException {
        }

        @Override
        public void onEnd(Intent intent) throws RemoteException {
        }

        @Override
        public void onGrammarResult(int engineType, String lexiconName, int errorCode)
                throws RemoteException {
            isGrammarUploadSuccess = true;
            Log.d(TAG,"onGrammarResult---->engineType = "+engineType+",lexiconName = "+lexiconName+",errorCode = "+errorCode);
        }

        @Override
        public void onBuffer(byte[] buffer) throws RemoteException {
        }
    };

    /**
     * 设置音量变化接口回调
     *
     * @param onVolumeChangedListener
     */
    public void setOnVolumeChangedListener(OnVolumeChangedListener onVolumeChangedListener) {
        this.onVolumeChangedListener = onVolumeChangedListener;
    }

    /**
     *处理返回结果
     */
    private void handleResurnResult(String localResult,String cloudResult){
        Log.d(TAG,"[AIUIControler][handleResurnResult]在线结果 = "+cloudResult+",localResult = "+localResult);
        if(mLocalParseAdapter == null || mParseControler == null){
            return;
        }
        Log.d(TAG,"[AIUIControler][handleResurnResult]--->isNetworkAvailable = "+FunctionUtil.isNetworkAvailable(mContext)
                +",getNetWorkStatus = "+FunctionUtil.getNetWorkStatus(mContext)+",isWifiEnable = "+FunctionUtil.isWifiEnable(mContext));
        //判断当前网络是否已连接并且可用,可用使用在线识别
        if(FunctionUtil.isNetworkAvailable(mContext)){
            //判断当前网络是否为wifi网络
            if(FunctionUtil.getNetWorkStatus(mContext) == FunctionUtil.NETWORK_WIFI){
                //判断当前wifi信号的好坏，如果wifi可以使用，则使用在线识别，否则使用离线识别
                if(FunctionUtil.isWifiEnable(mContext)){
                    //判断当前在线识别费否能够返回识别结果，如果能够返回则使用在线识别，否则使用离线识别
                    if(mParseControler.getCloudRc(cloudResult) == 4){
                        //离线识别
                        handelLocalSkill(localResult);
                    }else{
                        //在线识别
                        handelCloudSkill(cloudResult);
                    }
                }else{
                    //离线识别
                    handelLocalSkill(localResult);
                }
                //判断当前网络类型是否为数据连接,并且是4G连接，如果是4G使用在线识别，否则使用离线识别
            }else if(FunctionUtil.getNetWorkStatus(mContext) == FunctionUtil.NETWORK_CLASS_4_G){
                //判断当前在线识别费否能够返回识别结果，如果能够返回则使用在线识别，否则使用离线识别
                if(mParseControler.getCloudRc(cloudResult) == 4){
                    handelLocalSkill(localResult);
                }else{
                    handelCloudSkill(cloudResult);
                }
            }else{
                //离线识别
                handelLocalSkill(localResult);
            }
        } else {
            //离线识别
            handelLocalSkill(localResult);
        }
    }

    /**
     * 处理离线技能
     */
    private void handelLocalSkill(String localResult){
        Log.d(TAG,"[AIUIControler][handelLocalSkill][处理本地技能]localResult = "+localResult);
        if(!TextUtils.isEmpty(localResult)){
            String userSrting = mParseControler.handleIatResult(localResult);
            String robotString = mLocalParseAdapter.handleAsrResult(localResult);
            Log.d(TAG,"[AIUIControler][handelLocalSkill]userSrting = "+userSrting+",robotString = "+robotString);
            if(!TextUtils.isEmpty(userSrting)){
                mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.USER, userSrting);
                if(!TextUtils.isEmpty(robotString)){
                    mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, robotString);
                }else{
                    mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, FunctionUtil.getAnswer(mContext));
                }
            }else{
                mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, "不好意思，我没有听清楚您说的话！");
            }
        }else {
            mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, "不好意思，我没有听清楚您说的话！");
        }
    }

    /**
     * 处理在线技能
     * @param cloudResult
     */
    private void handelCloudSkill(String cloudResult){
        Log.d(TAG,"[AIUIControler][handelCloudSkill][处理在线技能]cloudResult = "+cloudResult);
        if(!TextUtils.isEmpty(cloudResult)){
            String userSrting = mParseControler.getCloudResult(cloudResult);
            String robotString = mParseControler.handleNlpResult(cloudResult);
            Log.d(TAG,"[AIUIControler][handelCloudSkill]userSrting = "+userSrting+",robotString = "+robotString);
            if(!TextUtils.isEmpty(userSrting)){
                mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.USER, userSrting);
                if(!TextUtils.isEmpty(robotString)){
                    mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, robotString);
                }else{
                    mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, FunctionUtil.getAnswer(mContext));
                }
            }else{
                mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, "不好意思，我没有听清楚您说的话！");
            }
        }else{
            mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, "不好意思，我没有听清楚您说的话！");
        }
    }

    /**
     * 获取AIUI捆绑参数集
     */
    private Bundle getAIUIParams() {
        Log.d(TAG,"[AIUIControler][getAIUIParams]");
        if (null == mJoParams) {
            try {
                JSONObject joRoot = new JSONObject();
                {
                    // 交互参数
                    JSONObject joInteract = new JSONObject();
                    // 交互超时（单位：ms），即唤醒后一定时间无有效结果则休眠。
                    // 取值：[10000,180000]；默认值：10000。
                    joInteract.put(AIUIConstant.KEY_INTERACT_TIMEOUT, "1000 * 10");
                    // 结果超时时间（单位：ms），即检测到音频后端点后一段时间内无结果则超时。
                    // 默认值：5000。
                    joInteract.put(AIUIConstant.KEY_RESULT_TIMEOUT, "5000");
                    joRoot.put("interact", joInteract);
                }
                {
                    // 全局设置
                    JSONObject joGlobal = new JSONObject();
                    // 业务场景
                    joGlobal.put(AIUIConstant.KEY_SCENE, "main");
                    joRoot.put("global", joGlobal);
                }
                {
                    // 本地VAD参数
                    JSONObject joVad = new JSONObject();
                    // VAD开关，取值：0（关闭）、1（打开）。
                    joVad.put(AIUIConstant.KEY_VAD_ENABLE, "1");
                    // VAD前端点超时（单位：ms）。取值范围：[1000, 10000]，默认值：5000。
                    joVad.put(AIUIConstant.KEY_VAD_BOS, "5000");
                    // VAD后端点超时（单位：ms）。取值范围：[650, 10000]，默认值：1500.
                    joVad.put(AIUIConstant.KEY_VAD_EOS, "1000");
                    joRoot.put("vad", joVad);
                }
                {
                    // 本地识别参数
                    JSONObject joAsr = new JSONObject();
                    // 业务场景
                    joAsr.put(AIUIConstant.KEY_SCENE, "call;sms;app;setting;music");
                    // 门限。默认值：0。
                    joAsr.put(AIUIConstant.KEY_THRESHOLD, "0");
                    joRoot.put("asr", joAsr);
                }
                {
                    // 语音业务流程控制
                    JSONObject joSpeech = new JSONObject();
                    // 音频采样率（单位：Hz），取值：8000、16000；默认值：16000。
                    joSpeech.put(AIUIConstant.KEY_SAMPLE_RATE, "" + mSampleRate);
                    // 处理意图的引擎类型，取值：local（离线处理，即本地语法识别）、cloud（云端处理，即语义）、mixed（混合方式）；默认值：cloud。
                    joSpeech.put(AIUIConstant.KEY_INTENT_ENGINE_TYPE, mIntentEngineType);
                    joRoot.put("speech", joSpeech);
                }
                {
                    // 其他参数
                    JSONObject joAudioParams = new JSONObject();
                    // 生效用户级动态实体
                    joAudioParams.put("pers_param", "{\"uid\":\"\"}");
                    joRoot.put("audioparams", joAudioParams);
                }
                {
                    // 日志参数
                    JSONObject joLog = new JSONObject();
                    joLog.put("debug_log", "1");
                    joLog.put("save_datalog", "1");
                    joRoot.put("log", joLog);
                }
                mJoParams = joRoot;
            } catch (JSONException e) {
                return null;
            }
        }
        String params = mJoParams.toString();

        Bundle bundle = new Bundle();
        bundle.putString(AIUIConstant.KEY_PARAMS, params);
        return bundle;
    }

    /**
     * 重新设置AIUI参数集
     * 主要是将当前位置的经度和纬度上传到讯飞服务器
     */
    public void setAIUIParams(double longitude, double latitude) {
        Log.d(TAG, "[AIUIControler][setAIUIParams]经度 = "+longitude+",维度 = "+latitude);
        if (null == mJoParams) {
            try {
                JSONObject joRoot = new JSONObject();
                {
                    // 交互参数
                    JSONObject joInteract = new JSONObject();
                    // 交互超时（单位：ms），即唤醒后一定时间无有效结果则休眠。
                    // 取值：[10000,180000]；默认值：10000。
                    joInteract.put(AIUIConstant.KEY_INTERACT_TIMEOUT, "1000 * 10");
                    // 结果超时时间（单位：ms），即检测到音频后端点后一段时间内无结果则超时。
                    // 默认值：5000。
                    joInteract.put(AIUIConstant.KEY_RESULT_TIMEOUT, "5000");
                    joRoot.put("interact", joInteract);
                }
                {
                    // 全局设置
                    JSONObject joGlobal = new JSONObject();
                    // 业务场景
                    joGlobal.put(AIUIConstant.KEY_SCENE, "main");
                    joRoot.put("global", joGlobal);
                }
                {
                    // 本地VAD参数
                    JSONObject joVad = new JSONObject();
                    // VAD开关，取值：0（关闭）、1（打开）。
                    joVad.put(AIUIConstant.KEY_VAD_ENABLE, "1");
                    // VAD前端点超时（单位：ms）。取值范围：[1000, 10000]，默认值：5000。
                    joVad.put(AIUIConstant.KEY_VAD_BOS, "5000");
                    // VAD后端点超时（单位：ms）。取值范围：[650, 10000]，默认值：1500.
                    joVad.put(AIUIConstant.KEY_VAD_EOS, "1000");
                    joRoot.put("vad", joVad);
                }
                {
                    // 本地识别参数
                    JSONObject joAsr = new JSONObject();
                    // 业务场景,如果有多个语法场景，请用分号隔开
                    joAsr.put(AIUIConstant.KEY_SCENE, "call;sms;app;setting;music");
                    // 门限。默认值：0。
                    joAsr.put(AIUIConstant.KEY_THRESHOLD, "40");
                    joRoot.put("asr", joAsr);
                }
                {
                    // 语音业务流程控制
                    JSONObject joSpeech = new JSONObject();
                    // 音频采样率（单位：Hz），取值：8000、16000；默认值：16000。
                    joSpeech.put(AIUIConstant.KEY_SAMPLE_RATE, "" + mSampleRate);
                    // 处理意图的引擎类型，取值：local（离线处理，即本地语法识别）、cloud（云端处理，即语义）、mixed（混合方式）；默认值：cloud。
                    joSpeech.put(AIUIConstant.KEY_INTENT_ENGINE_TYPE, mIntentEngineType);
                    joRoot.put("speech", joSpeech);
                }
                {
                    // 其他参数
                    JSONObject joAudioParams = new JSONObject();
                    // 生效用户级动态实体
                    joAudioParams.put("pers_param", "{\"uid\":\"\"}");
                    // 经纬度，gcj02坐标系
                    // 经度
                    joAudioParams.put("msc.lng", longitude);
                    // 纬度
                    joAudioParams.put("msc.lat", latitude);
                    joRoot.put("audioparams", joAudioParams);
                }
                {
                    // 日志参数
                    JSONObject joLog = new JSONObject();
                    joLog.put("debug_log", "1");
                    joLog.put("save_datalog", "1");
                    joRoot.put("log", joLog);
                }
                mJoParams = joRoot;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String params = mJoParams.toString();
        Bundle bundle = new Bundle();
        bundle.putString(AIUIConstant.KEY_PARAMS, params);
        AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, bundle);
        if (mAIUIAgent != null) {
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * 设置语音采样率
     */
    private void setSampleRate(int sampleRate) {
        if (null != mAIUIAgent) {
            try {
                JSONObject joSpeech = mJoParams.getJSONObject("speech");
                joSpeech.put(AIUIConstant.KEY_SAMPLE_RATE, "" + sampleRate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String params = mJoParams.toString();

            // 在启动AIUI后调用。会修改创建AIUIAgent时的参数。
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, params);
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, bundle);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * 设置意图的引擎类型
     */
    private void setIntentEngineType(String intentEngineType) {
        if (null != mAIUIAgent) {
            try {
                JSONObject joSpeech = mJoParams.getJSONObject("speech");
                joSpeech.put(AIUIConstant.KEY_INTENT_ENGINE_TYPE, intentEngineType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String params = mJoParams.toString();

            // 在启动AIUI后调用。会修改创建AIUIAgent时的参数。
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, params);
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, bundle);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * 获取识别参数
     *
     * @return
     */
    public Intent getRecognitionInitParams() {
        Log.d(TAG,"[AIUIControler][getRecognitionInitParams]");
        Intent recIntent = new Intent();
        // 本地识别引擎参数
        Bundle offlineBundle = new Bundle();
        offlineBundle.putBoolean(SpeechIntent.EXT_GRAMMARS_FLUSH, true);
        offlineBundle.putInt(SpeechIntent.EXT_SAMPLERATE, mSampleRate);
        offlineBundle.putInt(SpeechIntent.EXT_GRAMMARS_PREBUILD, 0);
        // 如果是多个语法文件请使用逗号分隔如{"grammar_call.mp3", "commons.mp3"}
        String[] grammar_files = {"grammar_call.mp3","grammar_sms.mp3","grammar_app.mp3","grammar_settings.mp3","grammar_music.mp3"};
        offlineBundle.putInt(SpeechIntent.ARG_RES_TYPE, SpeechIntent.RES_FROM_CLIENT);
        offlineBundle.putStringArray(SpeechIntent.EXT_GRAMMARS_FILES, grammar_files);
        recIntent.putExtra(SpeechIntent.ENGINE_LOCAL_DEC, offlineBundle);
        return recIntent;
    }

    /**
     * 开始语音识别
     */
    public void startVoiceNlp() {
        Log.d(TAG,"[AIUIControler][开始录音识别]");
        if (null != mAIUIAgent) {
            // 唤醒AIUI
            // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, null);
            mAIUIAgent.sendMessage(aiuiMsg);

            // 打开AIUI内部录音机，开始录音。
            String params = "sample_rate=16000,data_type=audio";
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, params);
            aiuiMsg = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, bundle);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * 停止语音识别
     */
    public void stopVoiceNlp() {
        Log.d(TAG,"[AIUIControler][停止录音识别]");
        if (null != mAIUIAgent) {
            // 停止录音
            // 手动停止录音，等待结果。
            // 注意对话结束会自动停止录音。
            String params = "sample_rate=16000,data_type=audio";
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, params);
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, bundle);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * 取消当次对话
     */
    public void cancelVoiceNlp() {
        Log.d(TAG,"[AIUIControler][取消识别]");
        // 重置唤醒AIUI
        // 手动取消对话
        // 注意对话结束会自动重置唤醒。
        if (null != mAIUIAgent) {
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_CLEAN_DIALOG_HISTORY, 0, 0, null);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * 同步动态实体
     * 在启动AIUI后调用。
     */
    private synchronized void syncDynamicEntity(String data,String resName,String dataType) {
        //待上传的数据
        try {
            JSONObject syncSchemaJson = new JSONObject();
            JSONObject paramJson = new JSONObject();
            // 可以设置标签，结果回调时会带上，用于区分请求。
            paramJson.put(AIUIConstant.KEY_TAG, dataType);
            paramJson.put("id_name", "uid");
            paramJson.put("res_name", resName);
            syncSchemaJson.put("param", paramJson);
            syncSchemaJson.put("data", Base64.encodeToString(data.getBytes(), Base64.DEFAULT | Base64.NO_WRAP));

            // 传入的数据一定要为utf-8编码
            byte[] syncData = syncSchemaJson.toString().getBytes(CHARSET_NAME);

            Bundle bundle = new Bundle();
            bundle.putByteArray(AIUIConstant.KEY_DATA, syncData);
            AIUIMessage syncAthenaMessage = new AIUIMessage(AIUIConstant.CMD_SYNC,
                    AIUIConstant.SYNC_DATA_SCHEMA, 0, bundle);
            if(mAIUIAgent != null){
                Log.d(TAG,"开始上传动态实体--->"+dataType);
                mAIUIAgent.sendMessage(syncAthenaMessage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在调用了SYNC_DATA_SCHEMA上传动态实体之后，服务器会发送消息EVENT_CMD_RETURN给客户端，
     * 客户端收到消息之后可以通过发送消息CMD_QUERY_SYNC_STATUS给服务器，查询数据的打包状态，
     * 只有打包成功了动态实体才可以正常使用。
     * @param syncSid 需要查询的回话ID，用于区分哪个动态实体。
     */
    public void queryDataStatus(String syncSid){
        if (TextUtils.isEmpty(syncSid)) {
            return;
        }
        try {
            JSONObject joParams = new JSONObject();
            joParams.put("sid", syncSid);
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, joParams.toString());
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_QUERY_SYNC_STATUS,
                    AIUIConstant.SYNC_DATA_SCHEMA, 0, bundle);
            if(mAIUIAgent != null){
                mAIUIAgent.sendMessage(aiuiMsg);
            }
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
    }

    /**
     * 上传联系人动态实体
     * 说明：
     * 1，上传实体（IFLYTEK.telephone_contact）--->这个实体是讯飞开放的公共动态实体，主要用于讯飞在线打电话技能。
     * 2，上传实体（CAPPU.contacts）--->这个实体自定义的动态实体，主要用于在线发短信技能。。
     */
    private void checkDataContactsUpdate(final DataControler mDataControler) {
        if (mDataControler != null) {
            syncDynamicEntity(mDataControler.contactsToJson(mDataControler.loadAllContacts()),
                    "IFLYTEK.telephone_contact","IFLYTEK.telephone_contact");
            syncDynamicEntity(mDataControler.contactsToJson(mDataControler.loadAllContacts()),
                    "CAPPU.contacts","CAPPU.contacts");
        }
    }


    /**
     * 上传应用信息动态实体,
     * 说明：实体（CAPPU.applacitions）-->这个实体也是自定义的动态实体，主要用于在线打开应用技能。
     *
     */
    public void checkDataAppUpdate(final DataControler mDataControler) {
        if (mDataControler != null) {
            syncDynamicEntity(mDataControler.appsToJson(mDataControler.loadAllApps()),
                    "CAPPU.applacitions","CAPPU.applacitions");
        }
    }

    /**
     * 同步本地识别资源
     * @param grammar_type 要同步的语法文件，可以是单个，也可以是多个，多个需要用“，”隔开。
     * @param lexiconName 要同步的语义槽
     * @param data 要同步的数据
     *
     * 说明：此方法主要用于同步本地离线文件的语意槽，包括应用名称，联系人名称，歌曲名称，歌手名称
     *             只有同步了这些本地实体，离线识别才能识别到，需要注意的是这个需要联网才能上传成功，
     *             否则会失败，导致离线技能无法使用。
     */
    private synchronized void uploadLocalData(String[] grammar_type, String lexiconName, List<String> data){
        Log.d(TAG,"[AIUIControler][uploadLocalData]上传离线实体---> lexiconName = "+lexiconName+",data = "+data.size());
        Intent intent = new Intent();
        // 如同时更新多个语法中同一个槽，传入多个语法名，如{ "call", "smsgr"}
        intent.putExtra(SpeechIntent.EXT_GRAMMARS_NAMES, grammar_type);// 要写上所有的语法，没写的语法可能没有build。
        intent.putExtra(SpeechIntent.EXT_ENGINE_TYPE, mIntentEngineType);//引擎类型
        intent.putExtra(SpeechIntent.EXT_LEXICON_NAME, lexiconName); //要同步的实体类型
        String[] dataResult = null == data ? null : data.toArray(new String[1]);
        intent.putExtra(SpeechIntent.EXT_LEXICON_ITEM, dataResult);
        intent.putExtra(SpeechIntent.EXT_SAMPLERATE, mSampleRate);
        mService.updateLexicon(intent);

    }

    /**
     * 退出销毁对应的对象，释放内存
     */
    public void onDestroy() {
        // 退出时释放连接
        if (mService != null) {
            if (null != mAIUIAgent) {
                cancelVoiceNlp();
                // 停止AIUI
                AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, null);
                mAIUIAgent.sendMessage(aiuiMsg);
                // 销毁AIUIAgent对象
                mAIUIAgent.destroy();
                mAIUIAgent = null;
            }
            mService.destroy();
            mService = null;
        }
        if (mParseControler != null) {
            mParseControler = null;
        }
        if(mDataControler != null){
            mDataControler.unRegisterContentObservers(mContext);
        }
    }

}