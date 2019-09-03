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
    //private IMediaPlaybackService mMusicService;
    private CameraManager mCameraManager;

    public LocalParseAdapter(Context context) {
        this.mContext = context;
        mDataControler = DataControler.getInstance(context);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    /**
     * 处理电话技能意图
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
     * 处理技能意图
     * @param json 本地返回结果
     * @return
     */
    private String handleActions(String json){
        //mMusicService = AiuiManager.getInstance(mContext).getMusicService();
        allContacts = mDataControler.loadAllContacts();
        allApps = mDataControler.loadAllApps();
        //联系人名称
        String contactName = getOperateActionByIntent(json,"<contact>");
        //电话技能
        String callSkill = getOperateActionByIntent(json,"<callCmd>");
        //短信意图
        String smsSkill = getOperateActionByIntent(json,"<smsCmd>");
        //应用技能
        String appSkill = getOperateActionByIntent(json,"<appCmd>");
        String appName = getOperateActionByIntent(json,"<appName>");
        //音乐意图
        String musicSkill = getOperateActionByIntent(json,"<playMusic>");
        String song = getOperateActionByIntent(json,"<songName>");
        String singer = getOperateActionByIntent(json,"<singerName>");
        String songType = getOperateActionByIntent(json,"<musicType>");
        //设置意图
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
        //方言切换设置
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
     * 获取本地技能类型
     * @param json 本地语义返回结果
     * @param intent 技能类型
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
     * 获取返回结果码
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
     * 判断字符串是否存在
     * @param text
     * @return
     */
    private boolean isTextExist(String text){
        return !TextUtils.isEmpty(text);
    }

    /**
     * 通过联系人名称获取联系人的电话号码
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
     * 通过联系人的电话号码获取该号码的电话类型，即三种，中国移动，中国电信，中国联通
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
     * 判断电话号码类型
     */
    private String getPhoneType(String phoneType){
        String type = null;
        if(phoneType.contains("移动")){
            type = "移动";
        }else if(phoneType.contains("联通")){
            type = "联通";
        }else if(phoneType.contains("电信")){
            type = "电信";
        }else{
            type = "未知";
        }
        return type;
    }

    /**
     * 从识别结果中获取电话号码
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
     * 处理本地电话技能
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
     * 处理本地电话技能
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
     * 拨打电话
     */
    private String doCall(String name, final String phoneNumber) {
        Log.d(TAG,"doCall---->name = "+name+",phoneNumber = "+phoneNumber);
        String result;
        if (isTextExist(name)) {
            if(FunctionUtil.hasSimCard(mContext)){
                result = "正在为您呼叫" + name;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                        DialerUtils.doCall(mContext, phoneNumber);
                        AiuiManager.getInstance(mContext).cancelVoiceNlp();
                    }
                }, 3000);
            }else{
                result = "没有在您的手机中检测到SIM卡，请检查SIM卡是否正确插入！";
            }
        } else if(isTextExist(phoneNumber)){
            if(FunctionUtil.hasSimCard(mContext)){
                result = "正在为您呼叫" + phoneNumber;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AiuiManager.getInstance(mContext).getSpeechControler().stopSpeech();
                        DialerUtils.doCall(mContext, phoneNumber);
                        AiuiManager.getInstance(mContext).cancelVoiceNlp();
                    }
                }, 3000);
            }else{
                result = "没有在您的手机中检测到SIM卡，请检查SIM卡是否正确插入！";
            }
        } else {
            final ComponentName component = new ComponentName("com.android.dialer","com.android.dialer.app.DialtactsActivity");
            if(openApp(component)){
                if(FunctionUtil.isScreenLocked(mContext)){
                    result = "正在为您打开电话拨号盘，请解锁之后进行查看！";
                }else{
                    result = "正在为您打开电话拨号盘！";
                }
            }else{
                result = "没有为您找到相关的电话拨号盘！";
            }
        }
        return result;
    }

    /**
     * 处理本地应用技能
     */
    private String handleAppAction(String appName){
        Log.d(TAG,"handleAppAction---->appName = "+appName);
        if(appName.equals("智能语音")){
            return "您当前已在智能语音界面！";
        }
        ComponentName componentName = FunctionUtil.getComponentByAppName(allApps,appName);
        FunctionUtil.isScreenLocked(mContext);
        if(openApp(componentName)){
            if(FunctionUtil.isScreenLocked(mContext)){
                return "正在为您打开"+appName+",请解锁之后进行查看！";
            }else{
                return "正在为您打开"+appName;
            }
        }else{
            return "没有为您找到该应用！";
        }
    }

    /**
     * 拨打电话
     */
    private String doSms(String name, final String phoneNumber) {
        Log.d(TAG,"doSms---->name = "+name+",phoneNumber = "+phoneNumber);
        String result;
        if (isTextExist(name)) {
            if(FunctionUtil.hasSimCard(mContext)){
                result = "正在发短信给" + name;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DialerUtils.sendMms(mContext,phoneNumber);
                    }
                },3000);
            }else{
                result = "没有在您的手机中检测到SIM卡，请检查SIM卡是否正确插入！";
            }
        } else if(isTextExist(phoneNumber)){
            if(FunctionUtil.hasSimCard(mContext)){
                result = "正在发短信给" + phoneNumber;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DialerUtils.sendMms(mContext,phoneNumber);
                    }
                },3000);
            }else{
                result = "没有在您的手机中检测到SIM卡，请检查SIM卡是否正确插入！";
            }
        } else {
            final ComponentName component = new ComponentName("com.android.mms","com.android.mms.ui.ConversationList");
            if(openApp(component)){
                if(FunctionUtil.isScreenLocked(mContext)){
                    result = "正在为您打开短信应用，请解锁之后进行查看！";
                }else{
                    result = "正在为您打开短信应用！";
                }
            }else{
                result = "没有为您找到相关的短信应用！";
            }
        }
        return result;
    }

    /**
     * 处理本地音乐技能
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
     * 打开应用
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
     * 随机播放某类型歌曲或者所有歌曲或者某个歌手的所有歌曲
     * 可能包含某种类型的歌曲，全部歌曲，某歌手的歌曲
     */
    private String playSongByRandom(List<Song> songs){
        Log.d(TAG,"[LocalParseAdapter][playSongByRandom]allSongsSize = "+allSongs.size());
        String result;
        if(songs != null && songs.size() > 0){
            long[] playList = FunctionUtil.getPlayList(songs);
            Random random = new Random();
            int position = random.nextInt(playList.length);
            String name = songs.get(position).getTitle();
            result = "音乐服务初始化失败，请重新尝试！";
        }else{
            result = "对不起，没有在您的手机里边找到歌曲，请添加之后重试！";
        }
        return result;
    }

    /**
     * 随机歌曲
     */
    private String playSongByName(String songName){
        Log.d(TAG,"[LocalParseAdapter][playSongByName]songName = "+songName);
        String result;
        String songPath;
        if(allSongs != null && allSongs.size() > 0){
            if(isSongNameExisted(allSongs,songName)){//判断歌曲是否存在
                result = "音乐服务初始化失败，请重新尝试！";
            }else{
                result = "没有找到歌曲"+songName+"，您可以听听别的歌曲！";
            }
        }else{
            result = "没有在您的手机中找到歌曲，请添加之后重新尝试！";
        }
        return result;
    }

    /**
     * 通过音乐名称获取音乐的播放路径
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
     * 判断歌手是否存在
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
     * 随机播放某种音乐
     */
    private String playMusicBySongType(String songType){
        ArrayList<Song> songs = getSongsByType(songType);
        Log.d(TAG,"playMusicBySongType--->songType = "+songType+",size = "+songs.size());
        if(songs != null && songs.size() != 0){
            return playSongByRandom(songs);
        }else{
          return "没有为您找到"+songType+"相关的歌曲！";
        }
    }

    /**
     * 播放某个歌手的某首歌曲
     */
    private String playSongBySingerAndSong(String singer,String song){
        Log.d(TAG,"playSongBySingerAndSong--->singer = "+singer+",song = "+song);
        if(isSingerOrSongExisted(singer,song)){
            return playSongByName(song);
        }else{
            return "没有为您找到"+singer+"的"+song+"!";
        }
    }

    /**
     * 随机播放某个歌手的歌曲
     */
    private String playSongBySingerWithRandom(String singer){
        Log.d(TAG,"playSongBySingerWithRandom--->singer = "+singer);
        ArrayList<Song> songs = getSongsBySinger(singer);
        if(songs != null && songs.size() != 0){
            return playSongByRandom(songs);
        }else{
            return "对不起，没有为您找到"+singer+"相关的歌曲！";
        }
    }

    /**
     * 播放歌曲
     */
    /*private void playMusicByName(IMediaPlaybackService mMusicService,String songName){
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
    }*/

    /**
     * 通过歌手名字获取所有该歌手的歌曲
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
     * 通过歌曲类型的名称得到相关的歌曲列表
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
     * 判断歌手是否存在或者歌名是否存在
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
     * 处理设置意图
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
     * 处理音量屏幕亮度字体技能
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
     * 处理wifi,数据连接，GPS，蓝牙，手电筒
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
     * 处理方言技能
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
     * 设置方言
     */
    private String setDialect(String speechDefault,String speechWomen,String speechGirl,String speechMan,String speechDongbei,String speechHenan,
                               String speechHunan,String speechSichuan,String speechTaiwan,String speechYueyu){
        mSpeechControler = AiuiManager.getInstance(mContext).getSpeechControler();
        if(isTextExist(speechDefault)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_DEFAULT)){
                    return "当前已经为"+speechDefault+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_DEFAULT);
                    return "已经为您切换为"+speechDefault+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechWomen)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_JIAJIA)){
                    return "当前已经为"+speechWomen+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_JIAJIA);
                    return "已经为您切换为"+speechWomen+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechGirl)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_NANNAN)){
                    return "当前已经为"+speechGirl+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_NANNAN);
                    return "已经为您切换为"+speechGirl+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechMan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_XIAOFENG)){
                    return "当前已经为"+speechMan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_XIAOFENG);
                    return "已经为您切换为"+speechMan+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechDongbei)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_DONG_BEI)){
                    return "当前已经为"+speechDongbei+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_DONG_BEI);
                    return "已经为您切换为"+speechDongbei+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechHenan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_HE_NAN)){
                    return "当前已经为"+speechHenan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_HE_NAN);
                    return "已经为您切换为"+speechHenan+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechHunan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_HU_NAN)){
                    return "当前已经为"+speechHunan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_HU_NAN);
                    return "已经为您切换为"+speechHunan+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechSichuan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_SI_CHUAN)){
                    return "当前已经为"+speechSichuan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_SI_CHUAN);
                    return "已经为您切换为"+speechSichuan+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechTaiwan)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_TAIWAN)){
                    return "当前已经为"+speechTaiwan+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_TAIWAN);
                    return "已经为您切换为"+speechTaiwan+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else if(isTextExist(speechYueyu)){
            if(mSpeechControler != null){
                if(mSpeechControler.getSpeaker().equals(mSpeechControler.SPEAKER_YUE_YU)){
                    return "当前已经为"+speechYueyu+"!";
                }else{
                    mSpeechControler.setSpeaker(mContext,mSpeechControler.SPEAKER_YUE_YU);
                    return "已经为您切换为"+speechYueyu+"!";
                }
            }else{
                return "方言切换失败!";
            }
        }else{
            return null;
        }
    }

    /**
     * 处理音量技能
     */
    private String handleVolumeAction(String increase,String reduce){
        Log.d(TAG,"handleVolumeAction--->volume = "+FunctionUtil.getVolume(mContext));
        if(isTextExist(increase)){
            if(FunctionUtil.getVolume(mContext) >= 15){
                return "当前音量已经调为最大！";
            }else{
                FunctionUtil.upVoice(mContext);
                return "已经为您调高音量！";
            }
        } else if(isTextExist(reduce)){
            if(FunctionUtil.getVolume(mContext) <= 0){
                return "当前音量已经调为最小！";
            }else{
                FunctionUtil.downVoice(mContext);
                return "已经为您调低音量！";
            }
        } else {
            return null;
        }
    }

    /**
     * 处理屏幕亮度技能
     */
    private String handleLightAction(String increase,String reduce){
        Log.d(TAG,"handleLightAction:当前屏幕亮度: = "+FunctionUtil.getBrightness(mContext));
        if(isTextExist(increase)){
            if(FunctionUtil.getBrightness(mContext) >= 255){
                return "当前屏幕亮度已经调到最亮！";
            }else{
                FunctionUtil.upBrightness(mContext);
                return "已经为您调亮屏幕亮度！";
            }
        }else if(isTextExist(reduce)){
            if(FunctionUtil.getBrightness(mContext) <= 0){
                return "当前屏幕亮度已经调为最暗！";
            }else{
                FunctionUtil.downBrightness(mContext);
                return "已经为您调暗屏幕亮度！";
            }
        }else{
            return null;
        }
    }

    /**
     * 处理字体技能
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
                    return "已经为您调为默认字体！";
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return "字体调节失败！";
                }
            } else if (newFontSize == default_fontSize) {
                configuration.fontScale = bigger_fontSize;
                try {
                    ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                    return "已经为您调为更大字体！";
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return "字体调节失败！";
                }
            } else if (newFontSize == bigger_fontSize) {
                configuration.fontScale = biggest_fontSize;
                try {
                    ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                    return "已经为您调为最大字体！";
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return "字体调节失败！";
                }
            } else if(newFontSize == biggest_fontSize){
                return "当前字体已经为最大字体！";
            } else{
                return "未识别当前字体大小！";
            }
        }else if(isTextExist(reduce)){
            if (newFontSize == small_fontSize) {
                return "当前字体已经为最小字体！";
            } else if (newFontSize == default_fontSize) {
                configuration.fontScale = small_fontSize;
                try {
                    ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                    return "已经为您调为最小字体！";
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return "字体调节失败！";
                }
            } else if (newFontSize == bigger_fontSize) {
                configuration.fontScale = default_fontSize;
                try {
                    ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                    return "已经为您调为默认字体！";
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return "字体调节失败！";
                }
            } else if(newFontSize == biggest_fontSize){
                configuration.fontScale = bigger_fontSize;
                try {
                    ActivityManagerNative.getDefault().updatePersistentConfiguration(configuration);
                    return "已经为您调为更大字体！";
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return "字体调节失败！";
                }
            } else {
                return "未识别当前字体大小！";
            }
        }else{
            return null;
        }
    }

    /**
     * 处理WIFI技能
     */
    private String handleWifiAction(String wifi,String open, String close){
        if(isTextExist(open)){
            if(FunctionUtil.isWifiEnabled(mContext)){
                return "当前"+wifi+"已经打开！";
            }else{
                if(FunctionUtil.setWifiEnable(mContext,true)){
                    return "正在为您打开"+wifi+"！";
                }else{
                    return wifi+"打开失败！";
                }
            }
        }else if(isTextExist(close)){
            if(FunctionUtil.isWifiEnabled(mContext)){
                if(FunctionUtil.setWifiEnable(mContext,false)){
                    return "正在为您关闭"+wifi+"!";
                }else{
                    return wifi+"关闭失败！";
                }
            }else{
                return "当前"+wifi+"已经关闭！";
            }
        }else{
            return null;
        }
    }

    /**
     * 处理数据流量技能
     */
    private String handleDataAction(String data,String open, String close){
        Log.d(TAG,"handleDataAction--->isDataEnabled = "+FunctionUtil.getDataEnabled(mContext));
        if(FunctionUtil.hasSimCard(mContext)){
            if(isTextExist(open)){
                if(FunctionUtil.getDataEnabled(mContext)){
                    return "当前"+data+"已经打开！";
                }else{
                    FunctionUtil.setDataEnabled(mContext,true);
                    return "正在为您打开"+data+"!";
                }
            }else if(isTextExist(close)){
                if(FunctionUtil.getDataEnabled(mContext)){
                    FunctionUtil.setDataEnabled(mContext,false);
                    return "正在为您关闭"+data+"!";
                }else{
                    return "当前"+data+"已经关闭！";
                }
            }else{
                return null;
            }
        }else{
            return "当前未检测到SIM卡，请确认是否正确插入了SIM卡！";
        }
    }

    /**
     * 处理GPS技能
     */
    private String handleGpsAction(String gps,String open, String close){
        Log.d(TAG,"handleGpsAction---->isGPSEnabled = "+FunctionUtil.isLocationEnabled(mContext));
        if(isTextExist(open)){
            if(FunctionUtil.isLocationEnabled(mContext)){
                return "当前"+gps+"已经打开！";
            }else{
                if(FunctionUtil.setLocationEnabled(mContext,true)){
                    return "正在为您打开"+gps+"!";
                }else{
                    return gps+"打开失败！";
                }
            }
        }else if(isTextExist(close)){
            if(FunctionUtil.isLocationEnabled(mContext)){
                if(FunctionUtil.setLocationEnabled(mContext,false)){
                    return "正在为您关闭"+gps+"!";
                }else{
                    return gps+"关闭失败！";
                }
            }else{
                return "当前"+gps+"已经关闭！";
            }
        }else{
            return null;
        }
    }

    /**
     * 处理蓝牙技能
     */
    private String handleBtAction(String open, String close){
        Log.d(TAG,"handleBtAction---->isBtEnabled = "+FunctionUtil.isBluetoothEnabled());
        if(isTextExist(open)){
            if(FunctionUtil.isBluetoothEnabled()){
                return "当前蓝牙已打开！";
            }else{
                if(FunctionUtil.setBluetoothEnabled(true)){
                    return "正在为您打开蓝牙！";
                }else{
                    return "蓝牙打开失败！";
                }
            }
        }else if(isTextExist(close)){
            if(FunctionUtil.isBluetoothEnabled()){
                if(FunctionUtil.setBluetoothEnabled(false)){
                    return "正在为您关闭蓝牙！";
                }else{
                    return "蓝牙关闭失败！";
                }
            }else{
                return "当前蓝牙已关闭！";
            }
        }else{
            return null;
        }
    }

    /**
     * 处理手电筒技能
     */
    private String handleTorchAction(String open, String close){
        Log.d(TAG,"[LocalParseAdapter][handleTorchAction]torchState = "+FunctionUtil.getTorchState(mContext));
        if(FunctionUtil.isHasFlashLight(mCameraManager)){
            if(isTextExist(open)){
                if(FunctionUtil.getTorchState(mContext) == 0){
                    return "当前手电筒已经打开！";
                }else{
                    if(FunctionUtil.handelFlashLight(mContext,mCameraManager,true)){
                        return "正在为您打开手电筒！";
                    }else{
                        return "手电筒打开失败，请重新尝试！";
                    }
                }
            }else if(isTextExist(close)){
                if(FunctionUtil.getTorchState(mContext) == 1){
                    return "当前手电筒已经关闭！";
                }else {
                    if(FunctionUtil.handelFlashLight(mContext,mCameraManager,false)){
                        return "正在为您关闭手电筒！";
                    }else{
                        return "手电筒关闭失败，请重新尝试！";
                    }
                }
            }else{
                return null;
            }
        }else{
            return "对不起，未在您的手机中检测到相关的设备！";
        }
    }

    /**
     * 处理照片相关的技能，包括拍照片和查看照片
     */
    private String handlePhotoAction(String takePhoto,String viewPhoto){
        Log.d(TAG,"[LocalParseAdapter][handlePhotoAction]takePhoto = "+takePhoto+",viewPhoto = "+viewPhoto);
        if(isTextExist(takePhoto)){
            ComponentName componentName = new ComponentName("com.mediatek.camera","com.mediatek.camera.CameraLauncher");
            if(openApp(componentName)){
                return "正在为您打开相机！";
            }else{
                return "没有为您找到相机相关的应用！";
            }
        }else if(isTextExist(viewPhoto)){
            ComponentName componentName = new ComponentName("com.android.gallery3d","com.android.gallery3d.app.GalleryActivity");
            if(openApp(componentName)){
                return "正在为您打开相册！";
            }else{
                return "没有为您找到相册相关的应用！";
            }
        }else{
            return null;
        }
    }
}