package com.droi.aiui.controler;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.droi.aiui.Interface.OnVolumeChangedListener;
import com.droi.aiui.adapter.LocalParseAdapter;
import com.droi.aiui.util.FunctionUtil;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuixiaojun on 2017-12-21.
 */

public class AIUIControler{

    private final String TAG = "AIUIControler";
    public static final String KEY_LOCAL_RESULT = "local_result";
    public static final String KEY_CLOUD_RESULT = "cloud_result";
    public static final String KEY_IAT_RESULT = "iat_result";

    private final int MSG_HANDLE_RESULT = 0;

    private OnVolumeChangedListener onVolumeChangedListener;
    private Context mContext;
    private SpeechServiceUtil mService;
    private AIUIAgent mAIUIAgent;
    private final int SAMPLE_RATE_16K = 16000;
    private final int SAMPLE_RATE_8K = 8000;
    private final String CHARSET_NAME = "utf-8";
    private JSONObject mJoParams;
    private int mSampleRate = SAMPLE_RATE_16K;
    //private String mIntentEngineType = AIUIConstant.ENGINE_TYPE_CLOUD;
    //private String mIntentEngineType = AIUIConstant.ENGINE_TYPE_LOCAL;
    private String mIntentEngineType = AIUIConstant.ENGINE_TYPE_MIXED;
    private LocalParseAdapter mLocalParseAdapter;
    public ParseControler mParseControler;
    private Map<String,String> resultMap = new HashMap<String, String>();
    private int resultCount = 1;
    private boolean isRecognitionInitSuccess;
    private boolean isGrammarUploadSuccess;
    private DataControler mDataControler;

    public AIUIControler(Context context, SpeechServiceUtil service) {
        mService = service;
        mContext = context;
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

    private final AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            // �¼�����ͽ������
            switch (event.eventType) {
                //�����¼�
                case AIUIConstant.EVENT_WAKEUP:
                    break;
                /**
                 * ����״̬�¼�
                 * ����AIUI����CMD_GET_STATE����ʱ�׳����¼�,
                 *arg1�ֶ�ȡֵΪ:
                 *����״̬�¼�,�ܹ�������״̬��
                 *1=>STATE_IDLE������״̬��AIUIδ����,��ʱֻ�ܽ���start���������񣩲�����
                 *2=>STATE_READY������״̬��AIUI�Ѿ���,�ȴ��û�����״̬,�������CMD_WAKEUP��Ϣ���ѷ���
                 *                ����AIUIAgent.createAgent��������֮�󣬷���Ϊ����״̬��
                 *3=>STATE_WORKING�����Ѻ󣬷�����빤��״̬,AIUI�����У��ɽ��н���,
                 *                  ��ʱ���������������ı���AIUI��̨���н�����
                 */
                case AIUIConstant.EVENT_STATE: {
                    if (AIUIConstant.STATE_IDLE == event.arg1) {
                        // ����״̬��AIUIδ����
                        Log.d(TAG, "AIUI״̬������");
                    } else if (AIUIConstant.STATE_READY == event.arg1) {
                        // AIUI�Ѿ������ȴ�����
                        Log.d(TAG, "AIUI״̬������");
                    } else if (AIUIConstant.STATE_WORKING == event.arg1) {
                        // AIUI�����У��ɽ��н���
                        Log.d(TAG, "AIUI״̬������");
                    }
                }
                break;
                /**
                 * ����¼���������д�����壬�����﷨���������ͽ�����ʽ�μ�2.2.2 AIUIEventһ�ڣ�
                 * ȡֵ--->1,data�ֶ�Я��������ݣ�info�ֶ�Ϊ�������ݵ�JSON�ַ�����
                 */
                case AIUIConstant.EVENT_RESULT: {
                    // ��AIUIConstant.KEY_INTENT_ENGINE_TYPE��ΪAIUIConstant.ENGINE_TYPE_MIXED
                    // ���ƶ˺ͱ��ؽ����������򱨴��ɿͻ����Լ�����ȡ�ᡣ
                    Log.d(TAG,"AIUIConstant.EVENT_RESULT");
                    handler.removeMessages(MSG_HANDLE_RESULT);
                    if(mParseControler != null){
                        mParseControler.handleEventResult(resultMap,event);
                    }
                    Log.d(TAG,">>>>>>>>>>>isNetworkAvailable = "+ FunctionUtil.isNetworkAvailable(mContext)+",isGrammarUploadSuccess = "+isGrammarUploadSuccess+",isRecognitionInitSuccess = "+isRecognitionInitSuccess+",resultCount = "+resultCount);
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

                //�����¼�,ȡֵ--->2
                //arg1�ֶ�Ϊ�����룬info�ֶ�Ϊ����������Ϣ��
                case AIUIConstant.EVENT_ERROR: {
                    // ���info�ֶβ�Ϊnull�����п�����[engine_type=cloud]��[engine_type=local]��Ϣ���ֱ��ʾ�ƶ˴���򱾵ش���
                    String info = event.bundle.getString(AIUIConstant.KEY_INFO);
                    resultCount = 1;
                    if(mParseControler != null){
                        mParseControler.handleErrorEvent(event);
                    }
                }
                break;
                /**
                 * CMD_UPLOAD_LEXICON:�ϴ��û�ʶ���ȴ�,ȡֵ-->11,AIUI��ʶ���ȴʣ�
                 *                      ������AIUI��ʶ��ʱ����ʶ����ȴ��е����ݡ�
                 *                  �����û�˵`y��n j��ng p�� ji��`����û���ϴ��ȴʵ������
                 *                  �ܴ���ʻᱻͨ��ģ��ʶ���`�۾�ơ��`�����ǵ��������ϴ���`�ྩơ��`���ȴʣ���ʶ��ɹ��ʻ�����������
                 *                  AIUI�ȴʷ�Ϊ���֣�һ�����û������ȴʣ�ͨ��CMD_UPLOAD_LEXICON�ϴ��� �����ϴ����ȴʵ��豸��Ч��
                 *                  һ����Ӧ���ȴʣ�ͨ��AIUI��̨Ӧ�ù���������ϴ�����ʹ��APPID���õ������豸��Ч��
                 *                  ���������ȴʹ�ͬ��Ч�����಻�Ḳ�ǣ�ͬ�����ȴʵڶ����ϴ��Ḳ����Ч��
                 *                  �����������ȴʣ��ȴʵ���Чʱ��Ϊ10~60���ӡ�
                 *CMD_RESULT_VALIDATION_ACK:���ȷ��,ȡֵ-->20
                 *                      �ڽ��յ����塢��д������Ľ����5s�ڿɷ��͸�ָ��Խ������ȷ�ϣ�
                 *                      AIUI����Ϊ���������Ч�������¿�ʼAIUI������ʱ�ļ�ʱ
                 *                      ���ڽ�����ʱ�Ļ��Ʋο�AIUI������interact_timeout�Ľ���
                 *CMD_CLEAN_DIALOG_HISTORY:��ս�����ʷ,ȡֵ-->21
                 */
                case AIUIConstant.EVENT_CMD_RETURN: {
                    Log.d(TAG,"AIUIConstant.EVENT_CMD_RETURN--->event.arg1 = "+event.arg1+",event.arg2 = "+event.arg2+",dtype = "+event.bundle.getInt("sync_dtype")+",tag = "+event.bundle.getString(AIUIConstant.KEY_TAG));
                    // ͬ����������
                    if (AIUIConstant.CMD_UPLOAD_LEXICON == event.arg1) {
                        //�ȴ��ϴ�
                        Log.d(TAG, "�ȴ��ϴ�:" + (0 == event.arg2 ? "�ɹ�" : "ʧ��"));
                    } else if (AIUIConstant.CMD_SYNC == event.arg1) {//����ͬ������̬ʵ���ϴ�
                        int dtype = event.bundle.getInt("sync_dtype");
                        // arg2��ʾ������
                        if (AIUIConstant.SYNC_DATA_SCHEMA == dtype) {
                            //��������ͬ������ʱ���õı�ǩ��������������
                            String tag = event.bundle.getString(AIUIConstant.KEY_TAG);
                            // arg2�Ǵ�����
                            if (0 == event.arg2) { // ͬ���ɹ�
                                // ע���ϴ��ɹ�������ʾ���ݴ���ɹ�������ɹ����Ӧ��ͬ��״̬��ѯ���Ϊ׼
                                // ������ֻ�д���ɹ����������ʹ��
                                String mSyncSid = event.bundle.getString("sid");
                                queryDataStatus(mSyncSid);
                                Log.d(TAG,"schema����ͬ���ɹ�, sid = " + mSyncSid + ", tag = " + tag);
                            } else {
                                Log.d(TAG,"schema����ͬ������: " + event.arg2 + ", tag = " + tag);
                            }
                        }
                    } else if (AIUIConstant.CMD_QUERY_SYNC_STATUS == event.arg1) {//��ѯ��̬ʵ����״̬
                        int syncType = event.bundle.getInt("sync_dtype");
                        if (AIUIConstant.SYNC_DATA_QUERY == syncType) {
                            String result = event.bundle.getString("result");
                            if (0 == event.arg2) {
                                Log.d(TAG, "��ѯ�����" + result);
                            } else {
                                Log.d(TAG, "��̬ʵ������״̬��ѯ����" + event.arg2 + ", result:" + result);
                            }
                        }
                    }
                }
                break;
                /**
                 * VAD�¼�
                 * ����⵽������Ƶ��ǰ�˵�󣬻��׳����¼���
                 *��arg1��ʶǰ��˵����������Ϣ:0(ǰ�˵�)��1(����)��2(��˵�)��
                 *��arg1ȡֵΪ1ʱ��arg2Ϊ������С��
                 */
                case AIUIConstant.EVENT_VAD: {
                    switch (event.arg1) {
                        case AIUIConstant.VAD_VOL: {
                            //���������ı仯
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
                //��ʼ¼���¼�,ȡֵ��11--->�׳����¼�֪ͨ�ⲿ¼����ʼ���û����Կ�ʼ˵��
                case AIUIConstant.EVENT_START_RECORD: {
                }
                break;
                //ֹͣ¼���¼���ȡֵ��12--->֪ͨ�ⲿ¼������
                case AIUIConstant.EVENT_STOP_RECORD: {
                }
                break;
                case AIUIConstant.EVENT_SLEEP: {
                }
                break;
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:{
                    Log.d(TAG, "AIUIConstant.EVENT_CONNECTED_TO_SERVER");
                    //��ʼ�ϴ�Ӧ����Ϣ
                    checkDataAppUpdate(mDataControler);
                    //��ʼ�ϴ���ϵ��
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
     * ��ȡ����ʶ��ص��ӿ�
     */
    public RecognitionListener getRecognitionListener() {
        return mRecognitionListener;
    }

    /**
     * ��ȡ����������
     */
    public ParseControler getParseControler() {
        return mParseControler;
    }

    /**
     * ����ʶ��ص��ӿ�
     */
    private final RecognitionListener mRecognitionListener = new RecognitionListener.Stub() {
        @Override
        public void onInit(int errorCode) throws RemoteException {
            Log.d(TAG, "RecognitionListener--->onInit--->errorCode = " + errorCode);
            if (SpeechError.SUCCESS == errorCode) {
                isRecognitionInitSuccess = true;
                Log.d(TAG, "��ʼ��ʶ������ɹ�");
            } else if (SpeechError.ERROR_GRAMMAR_NO_NEED_REBUILD == errorCode) {
                Log.d(TAG, "��ʼ��ʶ������ɹ�,�﷨����Ҫ���¹���");
            } else {
                Log.d(TAG, "��ʼ��ʶ������ʧ�ܣ������룺" + errorCode);
            }
            if (null != mService) {
                //��ȡ������
                Bundle bundle = getAIUIParams();
                if (null == bundle) {
                    return;
                }
                // ����AIUIAgent
                mAIUIAgent = mService.createAIUIAgent(bundle, mAIUIListener);
                if (null == mAIUIAgent) {
                    return;
                }
                // ����AIUI
                AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null);
                mAIUIAgent.sendMessage(aiuiMsg);
                Log.d(TAG,"mAIUIAgent��ʼ����ɣ�");
                //��ʼͬ��������������������������ʶ��ʹ��
                //ͬ����ϵ�����ݣ��������ص绰���ܣ����ż��ܺ���ϵ�˼��ܡ�
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
     * ���������仯�ӿڻص�
     *
     * @param onVolumeChangedListener
     */
    public void setOnVolumeChangedListener(OnVolumeChangedListener onVolumeChangedListener) {
        this.onVolumeChangedListener = onVolumeChangedListener;
    }

    /**
     *�����ؽ��
     */
    private void handleResurnResult(String localResult,String cloudResult){
        Log.d(TAG,"[AIUIControler][handleResurnResult]���߽�� = "+cloudResult+",localResult = "+localResult);
        if(mLocalParseAdapter == null || mParseControler == null){
            return;
        }
        Log.d(TAG,"[AIUIControler][handleResurnResult]--->isNetworkAvailable = "+FunctionUtil.isNetworkAvailable(mContext)
                +",getNetWorkStatus = "+FunctionUtil.getNetWorkStatus(mContext)+",isWifiEnable = "+FunctionUtil.isWifiEnable(mContext));
        //�жϵ�ǰ�����Ƿ������Ӳ��ҿ���,����ʹ������ʶ��
        if(FunctionUtil.isNetworkAvailable(mContext)){
            //�жϵ�ǰ�����Ƿ�Ϊwifi����
            if(FunctionUtil.getNetWorkStatus(mContext) == FunctionUtil.NETWORK_WIFI){
                //�жϵ�ǰwifi�źŵĺû������wifi����ʹ�ã���ʹ������ʶ�𣬷���ʹ������ʶ��
                if(FunctionUtil.isWifiEnable(mContext)){
                    //�жϵ�ǰ����ʶ��ѷ��ܹ�����ʶ����������ܹ�������ʹ������ʶ�𣬷���ʹ������ʶ��
                    if(mParseControler.getCloudRc(cloudResult) == 4){
                        //����ʶ��
                        handelLocalSkill(localResult);
                    }else{
                        //����ʶ��
                        handelCloudSkill(cloudResult);
                    }
                }else{
                    //����ʶ��
                    handelLocalSkill(localResult);
                }
                //�жϵ�ǰ���������Ƿ�Ϊ��������,������4G���ӣ������4Gʹ������ʶ�𣬷���ʹ������ʶ��
            }else if(FunctionUtil.getNetWorkStatus(mContext) == FunctionUtil.NETWORK_CLASS_4_G){
                //�жϵ�ǰ����ʶ��ѷ��ܹ�����ʶ����������ܹ�������ʹ������ʶ�𣬷���ʹ������ʶ��
                if(mParseControler.getCloudRc(cloudResult) == 4){
                    handelLocalSkill(localResult);
                }else{
                    handelCloudSkill(cloudResult);
                }
            }else{
                //����ʶ��
                handelLocalSkill(localResult);
            }
        } else {
            //����ʶ��
            handelLocalSkill(localResult);
        }
    }

    /**
     * �������߼���
     */
    private void handelLocalSkill(String localResult){
        Log.d(TAG,"[AIUIControler][handelLocalSkill][�����ؼ���]localResult = "+localResult);
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
                mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, "������˼����û���������˵�Ļ���");
            }
        }else {
            mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, "������˼����û���������˵�Ļ���");
        }
    }

    /**
     * �������߼���
     * @param cloudResult
     */
    private void handelCloudSkill(String cloudResult){
        Log.d(TAG,"[AIUIControler][handelCloudSkill][�������߼���]cloudResult = "+cloudResult);
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
                mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, "������˼����û���������˵�Ļ���");
            }
        }else{
            mParseControler.setMessage(Message.MsgType.TEXT, Message.FromType.ROBOT, "������˼����û���������˵�Ļ���");
        }
    }

    /**
     * ��ȡAIUI���������
     */
    private Bundle getAIUIParams() {
        Log.d(TAG,"[AIUIControler][getAIUIParams]");
        if (null == mJoParams) {
            try {
                JSONObject joRoot = new JSONObject();
                {
                    // ��������
                    JSONObject joInteract = new JSONObject();
                    // ������ʱ����λ��ms���������Ѻ�һ��ʱ������Ч��������ߡ�
                    // ȡֵ��[10000,180000]��Ĭ��ֵ��10000��
                    joInteract.put(AIUIConstant.KEY_INTERACT_TIMEOUT, "1000 * 10");
                    // �����ʱʱ�䣨��λ��ms��������⵽��Ƶ��˵��һ��ʱ�����޽����ʱ��
                    // Ĭ��ֵ��5000��
                    joInteract.put(AIUIConstant.KEY_RESULT_TIMEOUT, "5000");
                    joRoot.put("interact", joInteract);
                }
                {
                    // ȫ������
                    JSONObject joGlobal = new JSONObject();
                    // ҵ�񳡾�
                    joGlobal.put(AIUIConstant.KEY_SCENE, "main");
                    joRoot.put("global", joGlobal);
                }
                {
                    // ����VAD����
                    JSONObject joVad = new JSONObject();
                    // VAD���أ�ȡֵ��0���رգ���1���򿪣���
                    joVad.put(AIUIConstant.KEY_VAD_ENABLE, "1");
                    // VADǰ�˵㳬ʱ����λ��ms����ȡֵ��Χ��[1000, 10000]��Ĭ��ֵ��5000��
                    joVad.put(AIUIConstant.KEY_VAD_BOS, "5000");
                    // VAD��˵㳬ʱ����λ��ms����ȡֵ��Χ��[650, 10000]��Ĭ��ֵ��1500.
                    joVad.put(AIUIConstant.KEY_VAD_EOS, "1000");
                    joRoot.put("vad", joVad);
                }
                {
                    // ����ʶ�����
                    JSONObject joAsr = new JSONObject();
                    // ҵ�񳡾�
                    joAsr.put(AIUIConstant.KEY_SCENE, "call;sms;app;setting;music");
                    // ���ޡ�Ĭ��ֵ��0��
                    joAsr.put(AIUIConstant.KEY_THRESHOLD, "0");
                    joRoot.put("asr", joAsr);
                }
                {
                    // ����ҵ�����̿���
                    JSONObject joSpeech = new JSONObject();
                    // ��Ƶ�����ʣ���λ��Hz����ȡֵ��8000��16000��Ĭ��ֵ��16000��
                    joSpeech.put(AIUIConstant.KEY_SAMPLE_RATE, "" + mSampleRate);
                    // ������ͼ���������ͣ�ȡֵ��local�����ߴ����������﷨ʶ�𣩡�cloud���ƶ˴��������壩��mixed����Ϸ�ʽ����Ĭ��ֵ��cloud��
                    joSpeech.put(AIUIConstant.KEY_INTENT_ENGINE_TYPE, mIntentEngineType);
                    joRoot.put("speech", joSpeech);
                }
                {
                    // ��������
                    JSONObject joAudioParams = new JSONObject();
                    // ��Ч�û�����̬ʵ��
                    joAudioParams.put("pers_param", "{\"uid\":\"\"}");
                    joRoot.put("audioparams", joAudioParams);
                }
                {
                    // ��־����
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
     * ��������AIUI������
     * ��Ҫ�ǽ���ǰλ�õľ��Ⱥ�γ���ϴ���Ѷ�ɷ�����
     */
    public void setAIUIParams(double longitude, double latitude) {
        Log.d(TAG, "[AIUIControler][setAIUIParams]���� = "+longitude+",ά�� = "+latitude);
        if (null == mJoParams) {
            try {
                JSONObject joRoot = new JSONObject();
                {
                    // ��������
                    JSONObject joInteract = new JSONObject();
                    // ������ʱ����λ��ms���������Ѻ�һ��ʱ������Ч��������ߡ�
                    // ȡֵ��[10000,180000]��Ĭ��ֵ��10000��
                    joInteract.put(AIUIConstant.KEY_INTERACT_TIMEOUT, "1000 * 10");
                    // �����ʱʱ�䣨��λ��ms��������⵽��Ƶ��˵��һ��ʱ�����޽����ʱ��
                    // Ĭ��ֵ��5000��
                    joInteract.put(AIUIConstant.KEY_RESULT_TIMEOUT, "5000");
                    joRoot.put("interact", joInteract);
                }
                {
                    // ȫ������
                    JSONObject joGlobal = new JSONObject();
                    // ҵ�񳡾�
                    joGlobal.put(AIUIConstant.KEY_SCENE, "main");
                    joRoot.put("global", joGlobal);
                }
                {
                    // ����VAD����
                    JSONObject joVad = new JSONObject();
                    // VAD���أ�ȡֵ��0���رգ���1���򿪣���
                    joVad.put(AIUIConstant.KEY_VAD_ENABLE, "1");
                    // VADǰ�˵㳬ʱ����λ��ms����ȡֵ��Χ��[1000, 10000]��Ĭ��ֵ��5000��
                    joVad.put(AIUIConstant.KEY_VAD_BOS, "5000");
                    // VAD��˵㳬ʱ����λ��ms����ȡֵ��Χ��[650, 10000]��Ĭ��ֵ��1500.
                    joVad.put(AIUIConstant.KEY_VAD_EOS, "1000");
                    joRoot.put("vad", joVad);
                }
                {
                    // ����ʶ�����
                    JSONObject joAsr = new JSONObject();
                    // ҵ�񳡾�,����ж���﷨���������÷ֺŸ���
                    joAsr.put(AIUIConstant.KEY_SCENE, "call;sms;app;setting;music");
                    // ���ޡ�Ĭ��ֵ��0��
                    joAsr.put(AIUIConstant.KEY_THRESHOLD, "40");
                    joRoot.put("asr", joAsr);
                }
                {
                    // ����ҵ�����̿���
                    JSONObject joSpeech = new JSONObject();
                    // ��Ƶ�����ʣ���λ��Hz����ȡֵ��8000��16000��Ĭ��ֵ��16000��
                    joSpeech.put(AIUIConstant.KEY_SAMPLE_RATE, "" + mSampleRate);
                    // ������ͼ���������ͣ�ȡֵ��local�����ߴ����������﷨ʶ�𣩡�cloud���ƶ˴��������壩��mixed����Ϸ�ʽ����Ĭ��ֵ��cloud��
                    joSpeech.put(AIUIConstant.KEY_INTENT_ENGINE_TYPE, mIntentEngineType);
                    joRoot.put("speech", joSpeech);
                }
                {
                    // ��������
                    JSONObject joAudioParams = new JSONObject();
                    // ��Ч�û�����̬ʵ��
                    joAudioParams.put("pers_param", "{\"uid\":\"\"}");
                    // ��γ�ȣ�gcj02����ϵ
                    // ����
                    joAudioParams.put("msc.lng", longitude);
                    // γ��
                    joAudioParams.put("msc.lat", latitude);
                    joRoot.put("audioparams", joAudioParams);
                }
                {
                    // ��־����
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
     * ��������������
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

            // ������AIUI����á����޸Ĵ���AIUIAgentʱ�Ĳ�����
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, params);
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, bundle);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * ������ͼ����������
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

            // ������AIUI����á����޸Ĵ���AIUIAgentʱ�Ĳ�����
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, params);
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, bundle);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * ��ȡʶ�����
     *
     * @return
     */
    public Intent getRecognitionInitParams() {
        Log.d(TAG,"[AIUIControler][getRecognitionInitParams]");
        Intent recIntent = new Intent();
        // ����ʶ���������
        Bundle offlineBundle = new Bundle();
        offlineBundle.putBoolean(SpeechIntent.EXT_GRAMMARS_FLUSH, true);
        offlineBundle.putInt(SpeechIntent.EXT_SAMPLERATE, mSampleRate);
        offlineBundle.putInt(SpeechIntent.EXT_GRAMMARS_PREBUILD, 0);
        // ����Ƕ���﷨�ļ���ʹ�ö��ŷָ���{"grammar_call.mp3", "commons.mp3"}
        String[] grammar_files = {"grammar_call.mp3","grammar_sms.mp3","grammar_app.mp3","grammar_settings.mp3","grammar_music.mp3"};
        offlineBundle.putInt(SpeechIntent.ARG_RES_TYPE, SpeechIntent.RES_FROM_CLIENT);
        offlineBundle.putStringArray(SpeechIntent.EXT_GRAMMARS_FILES, grammar_files);
        recIntent.putExtra(SpeechIntent.ENGINE_LOCAL_DEC, offlineBundle);
        return recIntent;
    }

    /**
     * ��ʼ����ʶ��
     */
    public void startVoiceNlp() {
        Log.d(TAG,"[AIUIControler][��ʼ¼��ʶ��]");
        if (null != mAIUIAgent) {
            // ����AIUI
            // �ȷ��ͻ�����Ϣ���ı�AIUI�ڲ�״̬��ֻ�л���״̬���ܽ�����������
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, null);
            mAIUIAgent.sendMessage(aiuiMsg);

            // ��AIUI�ڲ�¼��������ʼ¼����
            String params = "sample_rate=16000,data_type=audio";
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, params);
            aiuiMsg = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, bundle);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * ֹͣ����ʶ��
     */
    public void stopVoiceNlp() {
        Log.d(TAG,"[AIUIControler][ֹͣ¼��ʶ��]");
        if (null != mAIUIAgent) {
            // ֹͣ¼��
            // �ֶ�ֹͣ¼�����ȴ������
            // ע��Ի��������Զ�ֹͣ¼����
            String params = "sample_rate=16000,data_type=audio";
            Bundle bundle = new Bundle();
            bundle.putString(AIUIConstant.KEY_PARAMS, params);
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, bundle);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * ȡ�����ζԻ�
     */
    public void cancelVoiceNlp() {
        Log.d(TAG,"[AIUIControler][ȡ��ʶ��]");
        // ���û���AIUI
        // �ֶ�ȡ���Ի�
        // ע��Ի��������Զ����û��ѡ�
        if (null != mAIUIAgent) {
            AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_CLEAN_DIALOG_HISTORY, 0, 0, null);
            mAIUIAgent.sendMessage(aiuiMsg);
        }
    }

    /**
     * ͬ����̬ʵ��
     * ������AIUI����á�
     */
    private synchronized void syncDynamicEntity(String data,String resName,String dataType) {
        //���ϴ�������
        try {
            JSONObject syncSchemaJson = new JSONObject();
            JSONObject paramJson = new JSONObject();
            // �������ñ�ǩ������ص�ʱ����ϣ�������������
            paramJson.put(AIUIConstant.KEY_TAG, dataType);
            paramJson.put("id_name", "uid");
            paramJson.put("res_name", resName);
            syncSchemaJson.put("param", paramJson);
            syncSchemaJson.put("data", Base64.encodeToString(data.getBytes(), Base64.DEFAULT | Base64.NO_WRAP));

            // ���������һ��ҪΪutf-8����
            byte[] syncData = syncSchemaJson.toString().getBytes(CHARSET_NAME);

            Bundle bundle = new Bundle();
            bundle.putByteArray(AIUIConstant.KEY_DATA, syncData);
            AIUIMessage syncAthenaMessage = new AIUIMessage(AIUIConstant.CMD_SYNC,
                    AIUIConstant.SYNC_DATA_SCHEMA, 0, bundle);
            if(mAIUIAgent != null){
                Log.d(TAG,"��ʼ�ϴ���̬ʵ��--->"+dataType);
                mAIUIAgent.sendMessage(syncAthenaMessage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * �ڵ�����SYNC_DATA_SCHEMA�ϴ���̬ʵ��֮�󣬷������ᷢ����ϢEVENT_CMD_RETURN���ͻ��ˣ�
     * �ͻ����յ���Ϣ֮�����ͨ��������ϢCMD_QUERY_SYNC_STATUS������������ѯ���ݵĴ��״̬��
     * ֻ�д���ɹ��˶�̬ʵ��ſ�������ʹ�á�
     * @param syncSid ��Ҫ��ѯ�Ļػ�ID�����������ĸ���̬ʵ�塣
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
     * �ϴ���ϵ�˶�̬ʵ��
     * ˵����
     * 1���ϴ�ʵ�壨IFLYTEK.telephone_contact��--->���ʵ����Ѷ�ɿ��ŵĹ�����̬ʵ�壬��Ҫ����Ѷ�����ߴ�绰���ܡ�
     * 2���ϴ�ʵ�壨CAPPU.contacts��--->���ʵ���Զ���Ķ�̬ʵ�壬��Ҫ�������߷����ż��ܡ���
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
     * �ϴ�Ӧ����Ϣ��̬ʵ��,
     * ˵����ʵ�壨CAPPU.applacitions��-->���ʵ��Ҳ���Զ���Ķ�̬ʵ�壬��Ҫ�������ߴ�Ӧ�ü��ܡ�
     *
     */
    public void checkDataAppUpdate(final DataControler mDataControler) {
        if (mDataControler != null) {
            syncDynamicEntity(mDataControler.appsToJson(mDataControler.loadAllApps()),
                    "CAPPU.applacitions","CAPPU.applacitions");
        }
    }

    /**
     * ͬ������ʶ����Դ
     * @param grammar_type Ҫͬ�����﷨�ļ��������ǵ�����Ҳ�����Ƕ���������Ҫ�á�����������
     * @param lexiconName Ҫͬ���������
     * @param data Ҫͬ��������
     *
     * ˵�����˷�����Ҫ����ͬ�����������ļ�������ۣ�����Ӧ�����ƣ���ϵ�����ƣ��������ƣ���������
     *             ֻ��ͬ������Щ����ʵ�壬����ʶ�����ʶ�𵽣���Ҫע����������Ҫ���������ϴ��ɹ���
     *             �����ʧ�ܣ��������߼����޷�ʹ�á�
     */
    private synchronized void uploadLocalData(String[] grammar_type, String lexiconName, List<String> data){
        Log.d(TAG,"[AIUIControler][uploadLocalData]�ϴ�����ʵ��---> lexiconName = "+lexiconName+",data = "+data.size());
        Intent intent = new Intent();
        // ��ͬʱ���¶���﷨��ͬһ���ۣ��������﷨������{ "call", "smsgr"}
        intent.putExtra(SpeechIntent.EXT_GRAMMARS_NAMES, grammar_type);// Ҫд�����е��﷨��ûд���﷨����û��build��
        intent.putExtra(SpeechIntent.EXT_ENGINE_TYPE, mIntentEngineType);//��������
        intent.putExtra(SpeechIntent.EXT_LEXICON_NAME, lexiconName); //Ҫͬ����ʵ������
        String[] dataResult = null == data ? null : data.toArray(new String[1]);
        intent.putExtra(SpeechIntent.EXT_LEXICON_ITEM, dataResult);
        intent.putExtra(SpeechIntent.EXT_SAMPLERATE, mSampleRate);
        mService.updateLexicon(intent);

    }

    /**
     * �˳����ٶ�Ӧ�Ķ����ͷ��ڴ�
     */
    public void onDestroy() {
        // �˳�ʱ�ͷ�����
        if (mService != null) {
            if (null != mAIUIAgent) {
                cancelVoiceNlp();
                // ֹͣAIUI
                AIUIMessage aiuiMsg = new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, null);
                mAIUIAgent.sendMessage(aiuiMsg);
                // ����AIUIAgent����
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