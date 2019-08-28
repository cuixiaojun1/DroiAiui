package com.droi.aiui.adapter;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.droi.aiui.AiuiManager;
import com.droi.aiui.bean.AppInfo;
import com.droi.aiui.bean.Contact;
import com.droi.aiui.bean.Song;
import com.droi.aiui.controler.DataControler;
import com.droi.aiui.controler.SpeechControler;
import com.droi.aiui.util.DialerUtils;
import com.droi.aiui.util.FunctionUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LocalParseAdapter{

    private final String TAG = "LocalParseAdapter";
    private Context mContext;
    private DataControler mDataControler;
    private List<Contact> allContacts;
    private List<AppInfo> allApps;
    private List<Song> allSongs;
    private SpeechControler mSpeechControler;
    private IMediaPlaybackService mMusicService;
    private CameraManager mCameraManager;

    public LocalParseAdapter(Context context) {
        this.mContext = context;
        mDataControler = DataControler.getInstance(context);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    /**
     * ����绰������ͼ
     * @param json
     * @return
     */
    public String handleAsrResult(String json){
        if(getLocalRc(json) != 0){
            return null;
        }else{
            return handleActions(json);
        }
    }

    /**
     * ��������ͼ
     * @param json ���ط��ؽ��
     * @return
     */
    private String handleActions(String json){
        mMusicService = AiuiManager.getInstance(mContext).getMusicService();
        allContacts = mDataControler.loadAllContacts();
        allApps = mDataControler.loadAllApps();
        //��ϵ������
        String contactName = getOperateActionByIntent(json,"<contact>");
        //�绰����
        String callSkill = getOperateActionByIntent(json,"<callCmd>");
        //������ͼ
        String smsSkill = getOperateActionByIntent(json,"<smsCmd>");
        //Ӧ�ü���
        String appSkill = getOperateActionByIntent(json,"<appCmd>");
        String appName = getOperateActionByIntent(json,"<appName>");
        //������ͼ
        String musicSkill = getOperateActionByIntent(json,"<playMusic>");
        String song = getOperateActionByIntent(json,"<songName>");
        String singer = getOperateActionByIntent(json,"<singerName>");
        String songType = getOperateActionByIntent(json,"<musicType>");
        //������ͼ
        String settingCmdIncrease = getOperateActionByIntent(json,"<cmdIncrease>");
        String settingCmdReduce = getOperateActionByIntent(json,"<cmdReduce>");
        String settingCmdOpen = getOperateActionByIntent(json,"<cmdOpen>");
        String settingCmdClose = getOperateActionByIntent(json,"<cmdClose>");
        String settingTakePhoto = getOperateActionByIntent(json,"<takePhoto>");
        String settingViewPhoto = getOperateActionByIntent(json,"<viewPhoto>");
        String settingTypeWifi = getOperateActionByIntent(json,"<typeWifi>");
        String settingTypeData = getOperateActionByIntent(json,"<typeData>");
        String settingTypeBT = getOperateActionByIntent(json,"<typeBT>");
        String settingTypeGPS = getOperateActionByIntent(json,"<typeGPS>");
        String settingTypeVolume = getOperateActionByIntent(json,"<typeVolume>");
        String settingTypeLight = getOperateActionByIntent(json,"<typeLight>");
        String settingTypeFont = getOperateActionByIntent(json,"<typeFont>");
        String settingTypeTorch = getOperateActionByIntent(json,"<typeTorch>");
        //�����л�����
        String speechDefault = getOperateActionByIntent(json,"<pthDefault>");
        String speechWomen = getOperateActionByIntent(json,"<pthWomen>");
        String speechGirl = getOperateActionByIntent(json,"<pthGirl>");
        String speechMan = getOperateActionByIntent(json,"<pthMan>");
        String speechDongbei = getOperateActionByIntent(json,"<pthDongbei>");
        String speechHenan = getOperateActionByIntent(json,"<pthHenan>");
        String speechHunan = getOperateActionByIntent(json,"<pthHunan>");
        String speechSichuan = getOperateActionByIntent(json,"<pthSichuan>");
        String speechTaiwan = getOperateActionByIntent(json,"<pthTaiwan>");
        String speechYueyu = getOperateActionByIntent(json,"<pthYueyu>");
        String speechSet = getOperateActionByIntent(json,"<setSpeech>");
        String speech = getOperateActionByIntent(json,"<speech>");

        if(isTextExist(callSkill)){
            return handleCallAction(json,contactName);
        }else if(isTextExist(smsSkill)){
            return handleSmsAction(json,contactName);
        }else if(isTextExist(appSkill)){
            return handleAppAction(appName);
        }else if(isTextExist(musicSkill)){
            return handleMusicAction(singer,song,songType);
        }else if(isTextExist(settingCmdIncrease) || isTextExist(settingCmdReduce) || isTextExist(settingCmdOpen)
                || isTextExist(settingCmdClose) || isTextExist(settingTakePhoto) || isTextExist(settingViewPhoto)
                || isTextExist(settingTypeWifi) || isTextExist(settingTypeData) || isTextExist(settingTypeBT)
                || isTextExist(settingTypeGPS) || isTextExist(settingTypeVolume) || isTextExist(settingTypeLight)
                || isTextExist(settingTypeFont) || isTextExist(settingTypeTorch)){
            return handleSettingAction(settingCmdIncrease,settingCmdReduce,settingCmdOpen,settingCmdClose,settingTakePhoto,
                    settingViewPhoto,settingTypeWifi,settingTypeData,settingTypeGPS,settingTypeBT,settingTypeVolume,
                    settingTypeLight,settingTypeFont,settingTypeTorch);
        }else if((isTextExist(speechDefault) || isTextExist(speechWomen) || isTextExist(speechGirl)
                || isTextExist(speechMan) || isTextExist(speechDongbei) || isTextExist(speechHenan)
                || isTextExist(speechHunan) || isTextExist(speechSichuan) || isTextExist(speechTaiwan)
                || isTextExist(speechYueyu)) && (isTextExist(speechSet) || isTextExist(speech))){
            return handleDialectAction(speechDefault,speechWomen,speechGirl,speechMan,speechDongbei,speechHenan,
                    speechHunan,speechSichuan,speechTaiwan,speechYueyu);
        }else{
            return null;
        }
    }

    /**
     * ��ȡ���ؼ�������
     * @param json �������巵�ؽ��
     * @param intent ��������
     * @return
     */
    private String getOperateActionByIntent(String json,String intent){
        String type = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("ws");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String tempIntent = jsonObject1.getString("slot");
                JSONArray jsonArray1 = jsonObject1.getJSONArray("cw");
                String word = jsonArray1.getJSONObject(0).getString("w");
                if(tempIntent.equals(intent)){
                    type = word;
                }
            }
        } catch(JSONException e){
            e.printStackTrace();
        }
        return type;
    }

    /**
     * ��ȡ���ؽ����
     * @param json
     * @return
     */
    public int getLocalRc(String json){
        if(!TextUtils.isEmpty(json)){
            try {
                return new JSONObject(json).optInt("rc");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * �ж��ַ����Ƿ����
     * @param text
     * @return
     */
    private boolean isTextExist(String text){
        return !TextUtils.isEmpty(text);
    }

    /**
     * ͨ����ϵ�����ƻ�ȡ��ϵ�˵ĵ绰����
     * @param name
     * @return
     */
    private String getPhoneNumberByName(String name){
        String number = null;
        if(allContacts != null && allContacts.size() != 0){
            for (int i = 0; i < allContacts.size(); i++) {
                if(allContacts.get(i).getName().equals(name)){
                    number = allContacts.get(i).getPhoneNumber();
                }
            }
        }
        return number;
    }

    /**
     * ͨ����ϵ�˵ĵ绰�����ȡ�ú���ĵ绰���ͣ������֣��й��ƶ����й����ţ��й���ͨ
     * @param phoneNumber
     * @return
     */
    private String getPhoneTypeByPhoneBumber(String phoneNumber){
        String phoneType = null;
        if(phoneNumber != null && allContacts != null && allContacts.size() != 0){
            for (int i = 0; i < allContacts.size(); i++) {
                if(allContacts.get(i).getPhoneNumber().equals(phoneNumber)){
                    phoneType = allContacts.get(i).getCarrier();
                }
            }
        }
        return phoneType;
    }

    /**
     * �жϵ绰��������
     */
    private String getPhoneType(String phoneType){
        String type = null;
        if(phoneType.contains("�ƶ�")){
            type = "�ƶ�";
        }else if(phoneType.contains("��ͨ")){
            type = "��ͨ";
        }else if(phoneType.contains("����")){
            type = "����";
        }else{
            type = "δ֪";
        }
        return type;
    }

    /**
     * ��ʶ�����л�ȡ�绰����
     */
    private String getPhoneNumberFromResult(String json){
        StringBuffer phoneNumber = new StringBuffer();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("ws");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                JSONArray jsonArray1 = jsonObject1.getJSONArray("cw");
                String word = jsonArray1.getJSONObject(0).getString("w");
                boolean isNum = word.matches("[0-9]+");
                if(isNum){
                    phoneNumber.append(word);
                }
            }
        } catch(JSONException e){
            e.printStackTrace();
        }
        return phoneNumber.toString();
    }

    /**
     * �����ص绰����
     */
    private String handleCallAction(String jsonResult, String contactName){
        Log.d(TAG,"handleCallAction---->contactName = "+contactName);
        String phoneNumber = getPhoneNumberByName(contactName);
        if(!isTextExist(phoneNumber)){
            phoneNumber = getPhoneNumberFromResult(jsonResult);
        }
        return doCall(contactName,phoneNumber);
    }

    /**
     * �����ص绰����
     */
    private String handleSmsAction(String jsonResult, String contactName){
        Log.d(TAG,"handleSmsAction---->contactName = "+contactName);
        String phoneNumber = getPhoneNumberByName(contactName);
        if(!isTextExist(phoneNumber)){
            phoneNumber = getPhoneNumberFromResult(jsonResult);
        }
        return doSms(contactName,phoneNumber);
    }

    /**
     * ����绰
     */
    private String doCall(String name, final String phoneNumber) {
        Log.d(TAG,"doCall---->name = "+name+",phoneNumber = "+phoneNumber);
        String result;
        if (isTextExist(name)) {
            if(FunctionUtil.hasSimCard(mContext)){
                result = "����Ϊ������" + name;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                        DialerUtils.doCall(mContext, phoneNumber);
                        AiuiManager.getInstance(mContext).cancelVoiceNlp();
                    }
                }, 3000);
            }else{
                result = "û���������ֻ��м�⵽SIM��������SIM���Ƿ���ȷ���룡";
            }
        } else if(isTextExist(phoneNumber)){
            if(FunctionUtil.hasSimCard(mContext)){
                result = "����Ϊ������" + phoneNumber;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                        DialerUtils.doCall(mContext, phoneNumber);
                        AiuiManager.getInstance(mContext).cancelVoiceNlp();
                    }
                }, 3000);
            }else{
                result = "û���������ֻ��м�⵽SIM��������SIM���Ƿ���ȷ���룡";
            }
        } else {
            final ComponentName component = new ComponentName("com.android.dialer","com.android.dialer.app.DialtactsActivity");
            if(openApp(component)){
                if(FunctionUtil.isScreenLocked(mContext)){
                    result = "����Ϊ���򿪵绰�����̣������֮����в鿴��";
                }else{
                    result = "����Ϊ���򿪵绰�����̣�";
                }
            }else{
                result = "û��Ϊ���ҵ���صĵ绰�����̣�";
            }
        }
        return result;
    }

    /**
     * ������Ӧ�ü���
     */
    private String handleAppAction(String appName){
        Log.d(TAG,"handleAppAction---->appName = "+appName);
        if(appName.equals("��������")){
            return "����ǰ���������������棡";
        }
        ComponentName componentName = FunctionUtil.getComponentByAppName(allApps,appName);
        FunctionUtil.isScreenLocked(mContext);
        if(openApp(componentName)){
            if(FunctionUtil.isScreenLocked(mContext)){
                return "����Ϊ����"+appName+",�����֮����в鿴��";
            }else{
                return "����Ϊ����"+appName;
            }
        }else{
            return "û��Ϊ���ҵ���Ӧ�ã�";
        }
    }

    /**
     * ����绰
     */
    private String doSms(String name, final String phoneNumber) {
        Log.d(TAG,"doSms---->name = "+name+",phoneNumber = "+phoneNumber);
        String result;
        if (isTextExist(name)) {
            if(FunctionUtil.hasSimCard(mContext)){
                result = "���ڷ����Ÿ�" + name;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DialerUtils.sendMms(mContext,phoneNumber);
                    }
                },3000);
            }else{
                result = "û���������ֻ��м�⵽SIM��������SIM���Ƿ���ȷ���룡";
            }
        } else if(isTextExist(phoneNumber)){
            if(FunctionUtil.hasSimCard(mContext)){
                result = "���ڷ����Ÿ�" + phoneNumber;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DialerUtils.sendMms(mContext,phoneNumber);
                    }
                },3000);
            }else{
                result = "û���������ֻ��м�⵽SIM��������SIM���Ƿ���ȷ���룡";
            }
        } else {
            final ComponentName component = new ComponentName("com.android.mms","com.android.mms.ui.ConversationList");
            if(openApp(component)){
                if(FunctionUtil.isScreenLocked(mContext)){
                    result = "����Ϊ���򿪶���Ӧ�ã������֮����в鿴��";
                }else{
                    result = "����Ϊ���򿪶���Ӧ�ã�";
                }
            }else{
                result = "û��Ϊ���ҵ���صĶ���Ӧ�ã�";
            }
        }
        return result;
    }

    /**
     * ���������ּ���
     * @param singer
     * @param song
     * @param songType
     * @return
     */
    private String handleMusicAction(String singer, String song, String songType) {
        allSongs = mDataControler.loadAllSongs();
        if(isTextExist(singer) && isTextExist(song)){
            return playSongBySingerAndSong(singer,song);
        }else if(isTextExist(singer)){
            return playSongBySingerWithRandom(singer);
        }else if(isTextExist(song)){
            return playSongByName(song);
        }else if(isTextExist(songType)){
            return playMusicBySongType(songType);
        }else {
            return playSongByRandom(allSongs);
        }
    }

    /**
     * ��Ӧ��
     */
    private boolean openApp(final ComponentName component){
        Log.d(TAG,"[LocaleParseAdapter][openApp]component = "+component);
        if(component == null){
            return false;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FunctionUtil.startApp(component,mContext);
            }
        },3000);
        return true;
    }

    /**
     * �������ĳ���͸����������и�������ĳ�����ֵ����и���
     * ���ܰ���ĳ�����͵ĸ�����ȫ��������ĳ���ֵĸ���
     */
    private String playSongByRandom(List<Song> songs){
        Log.d(TAG,"[LocalParseAdapter][playSongByRandom]allSongsSize = "+allSongs.size());
        String result;
        if(songs != null && songs.size() > 0){
            long[] playList = FunctionUtil.getPlayList(songs);
            Random random = new Random();
            int position = random.nextInt(playList.length);
            String name = songs.get(position).getTitle();
            if(mMusicService != null){
                FunctionUtil.playMusicByList(mMusicService,playList,position);
                result = "����Ϊ������"+name;
            }else{
                result = "���ַ����ʼ��ʧ�ܣ������³��ԣ�";
            }
        }else{
            result = "�Բ���û���������ֻ�����ҵ������������֮�����ԣ�";
        }
        return result;
    }

    /**
     * �������
     */
    private String playSongByName(String songName){
        Log.d(TAG,"[LocalParseAdapter][playSongByName]songName = "+songName);
        String result;
        String songPath;
        if(allSongs != null && allSongs.size() > 0){
            if(isSongNameExisted(allSongs,songName)){//�жϸ����Ƿ����
                if(mMusicService != null){
                    result = "����Ϊ������"+songName+"!";
                    playMusicByName(mMusicService,songName);
                }else{
                    result = "���ַ����ʼ��ʧ�ܣ������³��ԣ�";
                }
            }else{
                result = "û���ҵ�����"+songName+"��������������ĸ�����";
            }
        }else{
            result = "û���������ֻ����ҵ������������֮�����³��ԣ�";
        }
        return result;
    }

    /**
     * ͨ���������ƻ�ȡ���ֵĲ���·��
     */
    private String getSongPathByName(List<Song> allSongs,String songName){
        if(allSongs != null && allSongs.size() > 0){
            for (int i = 0; i <allSongs.size(); i++) {
                if(allSongs.get(i).getTitle().contains(songName)){
                    return allSongs.get(i).getFileUrl();
                }
            }
        }
        return null;
    }

    /**
     * �жϸ����Ƿ����
     */
    private boolean isSongNameExisted(List<Song> allSongs,String songName){
        Log.d(TAG,"[MusicParseAdapter][isExisted]songName = "+songName);
        for (int i = 0; i <allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if(song.getTitle().contains(songName)){
                return true;
            }
        }
        return false;
    }

    /**
     * �������ĳ������
     */
    private String playMusicBySongType(String songType){
        ArrayList<Song> songs = getSongsByType(songType);
        Log.d(TAG,"playMusicBySongType--->songType = "+songType+",size = "+songs.size());
        if(songs != null && songs.size() != 0){
            return playSongByRandom(songs);
        }else{
          return "û��Ϊ���ҵ�"+songType+"��صĸ�����";
        }
    }

    /**
     * ����ĳ�����ֵ�ĳ�׸���
     */
    private String playSongBySingerAndSong(String singer,String song){
        Log.d(TAG,"playSongBySingerAndSong--->singer = "+singer+",song = "+song);
        if(isSingerOrSongExisted(singer,song)){
            return playSongByName(song);
        }else{
            return "û��Ϊ���ҵ�"+singer+"��"+song+"!";
        }
    }

    /**
     * �������ĳ�����ֵĸ���
     */
    private String playSongBySingerWithRandom(String singer){
        Log.d(TAG,"playSongBySingerWithRandom--->singer = "+singer);
        ArrayList<Song> songs = getSongsBySinger(singer);
        if(songs != null && songs.size() != 0){
            return playSongByRandom(songs);
        }else{
            return "�Բ���û��Ϊ���ҵ�"+singer+"��صĸ�����";
        }
    }

    /**
     * ���Ÿ���
     */
    private void playMusicByName(IMediaPlaybackService mMusicService,String songName){
        Log.d(TAG,"[MusicParseAdapter][playMusicByName]mMusicService = "+mMusicService+",songPath = "+songName);
        long[] playList = FunctionUtil.getPlayList(allSongs);
        long songId = 0;
        int position = 0;
        for (int i = 0; i < allSongs.size(); i++) {
            if(allSongs.get(i).getTitle().equals(songName)){
                songId = allSongs.get(i).getId();
            }
        }
        for (int i = 0; i < playList.length; i++) {
            if(playList[i] == songId){
                position = i;
            }
        }
        FunctionUtil.playMusicByList(mMusicService,playList,position);
    }

    /**
     * ͨ���������ֻ�ȡ���иø��ֵĸ���
     */
    private ArrayList<Song> getSongsBySinger(String singer){
        ArrayList<Song> songs = new ArrayList<Song>();
        for (int i = 0; i < allSongs.size(); i++) {
            String singer1 = allSongs.get(i).getSinger();
            if(singer1.contains(singer)){
                songs.add(allSongs.get(i));
            }
        }
        return songs;
    }

    /**
     * ͨ���������͵����Ƶõ���صĸ����б�
     */
    private ArrayList<Song> getSongsByType(String songType){
        ArrayList<Song> songs = new ArrayList<Song>();
        if(allSongs != null && allSongs.size() != 0){
            for (int i = 0; i < allSongs.size(); i++) {
                Song song = allSongs.get(i);
                String fileUrl = song.getFileUrl();
                if(fileUrl.indexOf(songType) != -1){
                    songs.add(song);
                }
            }
        }
        return songs;
    }

    /**
     * �жϸ����Ƿ���ڻ��߸����Ƿ����
     */
    private boolean isSingerOrSongExisted(String singer,String song){
        if(allSongs != null && allSongs.size() != 0){
            for (int i = 0; i < allSongs.size(); i++) {
                String songName = allSongs.get(i).getTitle();
                String singerName = allSongs.get(i).getSinger();
                if(singerName.contains(singer) && songName.contains(song)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ����������ͼ
     */
    private String handleSettingAction(String increase, String reduce, String open,String close,
                                       String takePhoto,String viewPhoto,String wifi,String data,String gps,
                                       String bt,String volume,String light,String font,String torch){
        if((isTextExist(increase) || isTextExist(reduce)) && (isTextExist(volume) || isTextExist(light) || isTextExist(font))){
            return handleVolumeLightFontAction(increase,reduce,volume,light,font);
        } else if((isTextExist(open) || isTextExist(close)) && (isTextExist(wifi) || isTextExist(data) || isTextExist(gps) || isTextExist(bt) || isTextExist(torch))){
            return handleWifiDataGpsBtTorchAction(open,close,wifi,data,gps,bt,torch);
        } else if(isTextExist(takePhoto) || isTextExist(viewPhoto)){
            return handlePhotoAction(takePhoto,viewPhoto);
        } else {
            return null;
        }
    }

    /**
     * ����������Ļ�������弼��
     */
    private String handleVolumeLightFontAction(String increase, String reduce,String volume,String light,String font){
        if(isTextExist(volume)){
            return handleVolumeAction(increase,reduce);
        }else if(isTextExist(light)){
            return handleLightAction(increase,reduce);
        }else if(isTextExist(font)){
            return handleFontAction(increase,reduce);
        }else{
            return null;
        }
    }

    /**
     * ����wifi,�������ӣ�GPS���������ֵ�Ͳ
     */
    private String handleWifiDataGpsBtTorchAction(String open, String close,String wifi,String data,String gps,String bt,String torch){
        if(isTextExist(wifi)){
            return handleWifiAction(wifi,open,close);
        }else if(isTextExist(data)){
            return handleDataAction(data,open,close);
        }else if(isTextExist(gps)){
            return handleGpsAction(gps,open,close);
        }else if(isTextExist(bt)){
            return handleBtAction(open,close);
        }else if(isTextExist(torch)){
            return handleTorchAction(open,close);
        }else {
            return null;
        }
    }

    /**
     * �����Լ���
     */
    private String handleDialectAction(String speechDefault,String speechWomen,String speechGirl,String speechMan,String speechDongbei,String speechHenan,
                                    String speechHunan,String speechSichuan,String speechTaiwan,String speechYueyu){
        if(isTextExist(speechDefault) || isTextExist(speechWomen) || isTextExist(speechGirl)
                || isTextExist(speechMan) || isTextExist(speechDongbei) || isTextExist(speechHenan)
                || isTextExist(speechHunan) || isTextExist(speechSichuan) || isTextExist(speechTaiwan)
                || isTextExist(speechYueyu)){
            return setDialect(speechDefault,speechWomen,speechGirl,speechMan,speechDongbei,speechHenan,
                    speechHunan,speechSichuan,speechTaiwan,speechYueyu);
        }else{
            return null;
        }
    }

    /**
     * ���÷���
     */
    private String setDialect(String speechDefault,String speechWomen,String speechGirl,String speechMan,String speechDongbei,String speechHenan,
                               String speechHunan,String speechSichuan,String speechTaiwan,String speechYueyu){
        mSpeechControler = AiuiManager.getInstance(mContext).getSpeechControler();
        if(isTextExist(speechDefault)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_DEFAULT)){
                    return "��ǰ�Ѿ�Ϊ"+speechDefault+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_DEFAULT);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechDefault+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechWomen)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_JIAJIA)){
                    return "��ǰ�Ѿ�Ϊ"+speechWomen+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_JIAJIA);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechWomen+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechGirl)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_NANNAN)){
                    return "��ǰ�Ѿ�Ϊ"+speechGirl+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_NANNAN);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechGirl+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechMan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_XIAOFENG)){
                    return "��ǰ�Ѿ�Ϊ"+speechMan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_XIAOFENG);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechMan+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechDongbei)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_DONG_BEI)){
                    return "��ǰ�Ѿ�Ϊ"+speechDongbei+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_DONG_BEI);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechDongbei+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechHenan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_HE_NAN)){
                    return "��ǰ�Ѿ�Ϊ"+speechHenan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_HE_NAN);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechHenan+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechHunan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_HU_NAN)){
                    return "��ǰ�Ѿ�Ϊ"+speechHunan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_HU_NAN);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechHunan+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechSichuan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_SI_CHUAN)){
                    return "��ǰ�Ѿ�Ϊ"+speechSichuan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_SI_CHUAN);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechSichuan+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechTaiwan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_TAIWAN)){
                    return "��ǰ�Ѿ�Ϊ"+speechTaiwan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_TAIWAN);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechTaiwan+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else if(isTextExist(speechYueyu)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_YUE_YU)){
                    return "��ǰ�Ѿ�Ϊ"+speechYueyu+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_YUE_YU);
                    return "�Ѿ�Ϊ���л�Ϊ"+speechYueyu+"!";
                }
            }else{
                return "�����л�ʧ��!";
            }
        }else{
            return null;
        }
    }

    /**
     * ������������
     */
    private String handleVolumeAction(String increase,String reduce){
        Log.d(TAG,"handleVolumeAction--->volume = "+FunctionUtil.getVolume(mContext));
        if(isTextExist(increase)){
            if(FunctionUtil.getVolume(mContext) >= 15){
                return "��ǰ�����Ѿ���Ϊ���";
            }else{
                FunctionUtil.upVoice(mContext);
                return "�Ѿ�Ϊ������������";
            }
        } else if(isTextExist(reduce)){
            if(FunctionUtil.getVolume(mContext) <= 0){
                return "��ǰ�����Ѿ���Ϊ��С��";
            }else{
                FunctionUtil.downVoice(mContext);
                return "�Ѿ�Ϊ������������";
            }
        } else {
            return null;
        }
    }

    /**
     * ������Ļ���ȼ���
     */
    private String handleLightAction(String increase,String reduce){
        Log.d(TAG,"handleLightAction:��ǰ��Ļ����: = "+FunctionUtil.getBrightness(mContext));
        if(isTextExist(increase)){
            if(FunctionUtil.getBrightness(mContext) >= 255){
                return "��ǰ��Ļ�����Ѿ�����������";
            }else{
                FunctionUtil.upBrightness(mContext);
                return "�Ѿ�Ϊ��������Ļ���ȣ�";
            }
        }else if(isTextExist(reduce)){
            if(FunctionUtil.getBrightness(mContext) <= 0){
                return "��ǰ��Ļ�����Ѿ���Ϊ���";
            }else{
                FunctionUtil.downBrightness(mContext);
                return "�Ѿ�Ϊ��������Ļ���ȣ�";
            }
        }else{
            return null;
        }
    }

    /**
     * �������弼��
     */
    private String handleFontAction(String increase,String reduce){
        float newFontSize = mContext.getResources().getConfiguration().fontScale;
        if(newFontSize == 1.01f){
            newFontSize = 1.0f;
        }
        Log.d(TAG,"handleFontAction---->newFontSize = "+newFontSize);
        Configuration configuration = new Configuration();
        float small_fontSize = 0.85f;
        float default_fontSize = 1.0f;
        float bigger_fontSize = 1.15f;
        float biggest_fontSize = 1.3f;
        if(isTextExist(increase)){
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
        }else if(isTextExist(reduce)){
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
        }else{
            return null;
        }
    }

    /**
     * ����WIFI����
     */
    private String handleWifiAction(String wifi,String open, String close){
        if(isTextExist(open)){
            if(FunctionUtil.isWifiEnabled(mContext)){
                return "��ǰ"+wifi+"�Ѿ��򿪣�";
            }else{
                if(FunctionUtil.setWifiEnable(mContext,true)){
                    return "����Ϊ����"+wifi+"��";
                }else{
                    return wifi+"��ʧ�ܣ�";
                }
            }
        }else if(isTextExist(close)){
            if(FunctionUtil.isWifiEnabled(mContext)){
                if(FunctionUtil.setWifiEnable(mContext,false)){
                    return "����Ϊ���ر�"+wifi+"!";
                }else{
                    return wifi+"�ر�ʧ�ܣ�";
                }
            }else{
                return "��ǰ"+wifi+"�Ѿ��رգ�";
            }
        }else{
            return null;
        }
    }

    /**
     * ����������������
     */
    private String handleDataAction(String data,String open, String close){
        Log.d(TAG,"handleDataAction--->isDataEnabled = "+FunctionUtil.getDataEnabled(mContext));
        if(FunctionUtil.hasSimCard(mContext)){
            if(isTextExist(open)){
                if(FunctionUtil.getDataEnabled(mContext)){
                    return "��ǰ"+data+"�Ѿ��򿪣�";
                }else{
                    FunctionUtil.setDataEnabled(mContext,true);
                    return "����Ϊ����"+data+"!";
                }
            }else if(isTextExist(close)){
                if(FunctionUtil.getDataEnabled(mContext)){
                    FunctionUtil.setDataEnabled(mContext,false);
                    return "����Ϊ���ر�"+data+"!";
                }else{
                    return "��ǰ"+data+"�Ѿ��رգ�";
                }
            }else{
                return null;
            }
        }else{
            return "��ǰδ��⵽SIM������ȷ���Ƿ���ȷ������SIM����";
        }
    }

    /**
     * ����GPS����
     */
    private String handleGpsAction(String gps,String open, String close){
        Log.d(TAG,"handleGpsAction---->isGPSEnabled = "+FunctionUtil.isLocationEnabled(mContext));
        if(isTextExist(open)){
            if(FunctionUtil.isLocationEnabled(mContext)){
                return "��ǰ"+gps+"�Ѿ��򿪣�";
            }else{
                if(FunctionUtil.setLocationEnabled(mContext,true)){
                    return "����Ϊ����"+gps+"!";
                }else{
                    return gps+"��ʧ�ܣ�";
                }
            }
        }else if(isTextExist(close)){
            if(FunctionUtil.isLocationEnabled(mContext)){
                if(FunctionUtil.setLocationEnabled(mContext,false)){
                    return "����Ϊ���ر�"+gps+"!";
                }else{
                    return gps+"�ر�ʧ�ܣ�";
                }
            }else{
                return "��ǰ"+gps+"�Ѿ��رգ�";
            }
        }else{
            return null;
        }
    }

    /**
     * ������������
     */
    private String handleBtAction(String open, String close){
        Log.d(TAG,"handleBtAction---->isBtEnabled = "+FunctionUtil.isBluetoothEnabled());
        if(isTextExist(open)){
            if(FunctionUtil.isBluetoothEnabled()){
                return "��ǰ�����Ѵ򿪣�";
            }else{
                if(FunctionUtil.setBluetoothEnabled(true)){
                    return "����Ϊ����������";
                }else{
                    return "������ʧ�ܣ�";
                }
            }
        }else if(isTextExist(close)){
            if(FunctionUtil.isBluetoothEnabled()){
                if(FunctionUtil.setBluetoothEnabled(false)){
                    return "����Ϊ���ر�������";
                }else{
                    return "�����ر�ʧ�ܣ�";
                }
            }else{
                return "��ǰ�����ѹرգ�";
            }
        }else{
            return null;
        }
    }

    /**
     * �����ֵ�Ͳ����
     */
    private String handleTorchAction(String open, String close){
        Log.d(TAG,"[LocalParseAdapter][handleTorchAction]torchState = "+FunctionUtil.getTorchState(mContext));
        if(FunctionUtil.isHasFlashLight(mCameraManager)){
            if(isTextExist(open)){
                if(FunctionUtil.getTorchState(mContext) == 0){
                    return "��ǰ�ֵ�Ͳ�Ѿ��򿪣�";
                }else{
                    if(FunctionUtil.handelFlashLight(mContext,mCameraManager,true)){
                        return "����Ϊ�����ֵ�Ͳ��";
                    }else{
                        return "�ֵ�Ͳ��ʧ�ܣ������³��ԣ�";
                    }
                }
            }else if(isTextExist(close)){
                if(FunctionUtil.getTorchState(mContext) == 1){
                    return "��ǰ�ֵ�Ͳ�Ѿ��رգ�";
                }else {
                    if(FunctionUtil.handelFlashLight(mContext,mCameraManager,false)){
                        return "����Ϊ���ر��ֵ�Ͳ��";
                    }else{
                        return "�ֵ�Ͳ�ر�ʧ�ܣ������³��ԣ�";
                    }
                }
            }else{
                return null;
            }
        }else{
            return "�Բ���δ�������ֻ��м�⵽��ص��豸��";
        }
    }

    /**
     * ������Ƭ��صļ��ܣ���������Ƭ�Ͳ鿴��Ƭ
     */
    private String handlePhotoAction(String takePhoto,String viewPhoto){
        Log.d(TAG,"[LocalParseAdapter][handlePhotoAction]takePhoto = "+takePhoto+",viewPhoto = "+viewPhoto);
        if(isTextExist(takePhoto)){
            ComponentName componentName = new ComponentName("com.mediatek.camera","com.mediatek.camera.CameraLauncher");
            if(openApp(componentName)){
                return "����Ϊ���������";
            }else{
                return "û��Ϊ���ҵ������ص�Ӧ�ã�";
            }
        }else if(isTextExist(viewPhoto)){
            ComponentName componentName = new ComponentName("com.android.gallery3d","com.android.gallery3d.app.GalleryActivity");
            if(openApp(componentName)){
                return "����Ϊ������ᣡ";
            }else{
                return "û��Ϊ���ҵ������ص�Ӧ�ã�";
            }
        }else{
            return null;
        }
    }
}