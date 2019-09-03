package com.droi.aiui.controler;


import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.droi.aiui.bean.AppInfo;
import com.droi.aiui.bean.AppName;
import com.droi.aiui.bean.Contact;
import com.droi.aiui.bean.Song;
import com.droi.aiui.util.FunctionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuixiaojun on 18-1-3.
 */

public class DataControler{

    private final String TAG = "DataControler";
    private Context mContext;
    public static final String TYPE_SONG_NAME = "song_of_name";
    public static final String TYPE_SONG_SINGER = "song_of_singer";

    private static DataControler mDataControler;
    private ContactAsyncQueryHandler mContactAsyncQueryHandler;
    private MusicAsyncQueryHandler mMusicAsyncQueryHandler;

    private List<AppInfo> allApps = new ArrayList<>();
    private List<Contact> allContacts = new ArrayList<>();
    private List<Song> allSongs = new ArrayList<>();

    public DataControler(Context context){
        this.mContext = context;
        // 实例化
        mContactAsyncQueryHandler = new ContactAsyncQueryHandler(context.getContentResolver());
        mMusicAsyncQueryHandler = new MusicAsyncQueryHandler(context.getContentResolver());
    }

    public static DataControler getInstance(Context context){
        if(mDataControler == null){
            mDataControler = new DataControler(context);
        }
        return mDataControler;
    }

    /**
     * 获取所有的联系人
     */
    public List<Contact> loadAllContacts() {
        Log.d(TAG,"[DataControler][loadAllContacts]size = "+allContacts.size());
        return allContacts;
    }

    /**
     * 获取所有的应用信息
     */
    public List<AppInfo> loadAllApps(){
        Log.d(TAG,"[DataControler][loadAllApps]size = "+allApps.size());
        return allApps;
    }

    /**
     * 获取所有的歌曲信息
     */
    public List<Song> loadAllSongs(){
        Log.d(TAG,"[DataControler][loadAllSongs]size = "+allSongs.size());
        return allSongs;
    }

    public void startLoadApps(){
        new LoadAppTask().execute();
    }

    /**
     * 异步任务加载所有的应用信息
     */
    private class LoadAppTask extends AsyncTask<Void, Integer, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            allApps.clear();
            Log.d(TAG,"开始查询应用信息！");
            allApps = getAllAppsInfo(mContext,loadApps(mContext));
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.d(TAG,"查询应用信息完成，共查到"+allApps.size()+"个应用！");
            } else {
                Log.d(TAG,"数据还未加载完成！");
            }
        }
    }

    /**
     * 获取手机里边所有应用的信息，包括名称，包名和类名
     */
    private List<AppInfo> getAllAppsInfo(Context context, List<ResolveInfo> allApps){
        ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
        for (int i = 0; i < allApps.size(); i++) {
            ResolveInfo resolveInfo = allApps.get(i);
            String appName = resolveInfo.loadLabel(context.getPackageManager()).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            String className = resolveInfo.activityInfo.name;
            AppInfo appInfo = new AppInfo(appName,packageName,className);
            apps.add(appInfo);
        }
        return apps;
    }

    /**
     * 加载手机里所有app
     */
    private List<ResolveInfo> loadApps(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return context.getPackageManager().queryIntentActivities(intent, 0);
    }

    /**
     * 将获取到的联系人信息转化为json格式
     * @param allApps
     * @return
     */
    public String appsToJson(List<AppInfo> allApps){
        StringBuffer res=new StringBuffer();
        for (int i = 0; i < allApps.size(); i++) {
            String name = allApps.get(i).getAppName();
            AppName appName = new AppName(name);
            String json = com.alibaba.fastjson.JSON.toJSONString(appName);
            res.append(json);
            res.append("\n");
        }
        return res.toString();
    }

    /**
     * 将获取到的联系人信息转化为json格式
     * @param contacts
     * @return
     */
    public String contactsToJson(List<Contact> contacts){
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = new Contact(contacts.get(i).getName(),contacts.get(i).getPhoneNumber());
            String json = com.alibaba.fastjson.JSON.toJSONString(contact);
            res.append(json);
            res.append("\n");
        }
        return res.toString();
    }

    public void registerContentObservers(Context context) {
        Uri uri = Uri.parse("content://media/external/audio/media");
        if(mMediaObserver != null){
            context.getContentResolver().registerContentObserver(uri, true,mMediaObserver);
        }
    }

    public void unRegisterContentObservers(Context context) {
        if(mMediaObserver != null){
            context.getContentResolver().unregisterContentObserver(mMediaObserver);
        }
    }

    /**
     * 查询所有的音乐文件信息
     */
    public void startLoadMusic(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; // 音乐媒体；
        // 查询的字段
        String[] projection = {
                MediaStore.Audio.Media._ID,           //歌曲ID
                MediaStore.Audio.Media.DISPLAY_NAME,  //文件名称
                MediaStore.Audio.Media.TITLE,         //歌曲名称
                MediaStore.Audio.Media.DURATION,      //歌曲时长
                MediaStore.Audio.Media.ARTIST,        //歌曲艺术家
                MediaStore.Audio.Media.ALBUM,         //歌曲专辑
                MediaStore.Audio.Media.YEAR,          //歌曲年份
                MediaStore.Audio.Media.MIME_TYPE,     //歌曲类型
                MediaStore.Audio.Media.SIZE,          //文件大小
                MediaStore.Audio.Media.DATA };        //文件路径
        // 按照sort_key升序查詢
        mMusicAsyncQueryHandler.startQuery(0, null, uri, projection, null, null, null);
    }

    ContentObserver mMediaObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG,"[DataControler][onChange]");
            startLoadMusic();
        }
    };

    /**
     * 音乐文件加载器
     */
    private class MusicAsyncQueryHandler extends AsyncQueryHandler {

        public MusicAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.d(TAG,"开始查询歌曲！");
            allSongs.clear();
            int count = cursor.getCount();
            if (cursor != null && count > 0) {
                cursor.moveToFirst(); // 游标移动到第一项
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);
                    Song song = new Song();
                    long id = cursor.getLong(0);
                    song.setId(id);
                    String name = cursor.getString(1);
                    String title = FunctionUtil.formateString(cursor.getString(2));
                    if(!TextUtils.isEmpty(title)){
                        song.setTitle(title);
                    }else{
                        song.setTitle("未知歌曲");
                    }
                    int duration = cursor.getInt(3);
                    String artist = FunctionUtil.formateString(cursor.getString(4));
                    if(!TextUtils.isEmpty(artist)){
                        song.setSinger(artist);
                    }else{
                        song.setSinger("未知歌手");
                    }
                    String album = cursor.getString(5);
                    String year = cursor.getString(6);
                    String mineType = cursor.getString(7);
                    String size = cursor.getString(8);
                    String path = cursor.getString(9);
                    if(path != null){
                        song.setFileUrl(path);
                    }
                    allSongs.add(song);
                }
                cursor.close();
            }
            Log.d(TAG,"歌曲查询完成,共有"+count+"首歌曲！");

            super.onQueryComplete(token, cookie, cursor);
        }
    }

    /**
     * 异步查询所有的联系人信息
     */
    public void startLoadContact(){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人Uri；
        // 查询的字段
        String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1 };
        // 按照sort_key升序查詢
        mContactAsyncQueryHandler.startQuery(0, null, uri, projection, null, null,
                "sort_key COLLATE LOCALIZED asc");
    }

    /**
     * 联系人加载器
     */
    private class ContactAsyncQueryHandler extends AsyncQueryHandler {

        public ContactAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.d(TAG,"开始查询联系人！");
            allContacts.clear();
            int count = cursor.getCount();
            if (cursor != null && count > 0) {
                cursor.moveToFirst(); // 游标移动到第一项
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);
                    String name = cursor.getString(1);
                    String number = cursor.getString(2);
                    // 创建联系人对象
                    Contact contact = new Contact();
                    contact.setName(FunctionUtil.formateString(name));
                    contact.setPhoneNumber(number);
                    allContacts.add(contact);
                }
                cursor.close();
            }
            Log.d(TAG,"联系人查询完成,共有"+count+"个联系人！");
            super.onQueryComplete(token, cookie, cursor);
        }
    }

    /**
     * 获取所有的联系人的名称
     */
    public List<String> getAllContactNames(){
        List<String> allContactNames = new ArrayList<>();
        if(allContacts != null && allContacts.size() != 0){
            for (int i = 0; i < allContacts.size(); i++) {
                allContactNames.add(allContacts.get(i).getName());
            }
        }
        return allContactNames;
    }

    /**
     * 获取所有的应用的名称
     */
    public List<String> getAllAppNames(){
        List<String> allAppNames = new ArrayList<>();
        if(allApps != null && allApps.size() != 0){
            for (int i = 0; i < allApps.size(); i++) {
                allAppNames.add(allApps.get(i).getAppName());
            }
        }
        return allAppNames;
    }

    /**
     * 获取所有的音乐名称
     */
    public List<String> getAllSongNamesOrSingerNames(String type){
        List<String> allSongNames = new ArrayList<>();
        if(allSongs != null && allSongs.size() != 0){
            for (int i = 0; i < allSongs.size(); i++) {
                if(type.equals(TYPE_SONG_NAME)){
                    allSongNames.add(allSongs.get(i).getTitle());
                }else if(type.equals(TYPE_SONG_SINGER)){
                    allSongNames.add(allSongs.get(i).getSinger());
                }
            }
        }
        return allSongNames;
    }
}