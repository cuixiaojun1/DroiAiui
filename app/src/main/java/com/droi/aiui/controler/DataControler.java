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
        // ʵ����
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
     * ��ȡ���е���ϵ��
     */
    public List<Contact> loadAllContacts() {
        Log.d(TAG,"[DataControler][loadAllContacts]size = "+allContacts.size());
        return allContacts;
    }

    /**
     * ��ȡ���е�Ӧ����Ϣ
     */
    public List<AppInfo> loadAllApps(){
        Log.d(TAG,"[DataControler][loadAllApps]size = "+allApps.size());
        return allApps;
    }

    /**
     * ��ȡ���еĸ�����Ϣ
     */
    public List<Song> loadAllSongs(){
        Log.d(TAG,"[DataControler][loadAllSongs]size = "+allSongs.size());
        return allSongs;
    }

    public void startLoadApps(){
        new LoadAppTask().execute();
    }

    /**
     * �첽����������е�Ӧ����Ϣ
     */
    private class LoadAppTask extends AsyncTask<Void, Integer, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            allApps.clear();
            Log.d(TAG,"��ʼ��ѯӦ����Ϣ��");
            allApps = getAllAppsInfo(mContext,loadApps(mContext));
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.d(TAG,"��ѯӦ����Ϣ��ɣ����鵽"+allApps.size()+"��Ӧ�ã�");
            } else {
                Log.d(TAG,"���ݻ�δ������ɣ�");
            }
        }
    }

    /**
     * ��ȡ�ֻ��������Ӧ�õ���Ϣ���������ƣ�����������
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
     * �����ֻ�������app
     */
    private List<ResolveInfo> loadApps(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return context.getPackageManager().queryIntentActivities(intent, 0);
    }

    /**
     * ����ȡ������ϵ����Ϣת��Ϊjson��ʽ
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
     * ����ȡ������ϵ����Ϣת��Ϊjson��ʽ
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
     * ��ѯ���е������ļ���Ϣ
     */
    public void startLoadMusic(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; // ����ý�壻
        // ��ѯ���ֶ�
        String[] projection = {
                MediaStore.Audio.Media._ID,           //����ID
                MediaStore.Audio.Media.DISPLAY_NAME,  //�ļ�����
                MediaStore.Audio.Media.TITLE,         //��������
                MediaStore.Audio.Media.DURATION,      //����ʱ��
                MediaStore.Audio.Media.ARTIST,        //����������
                MediaStore.Audio.Media.ALBUM,         //����ר��
                MediaStore.Audio.Media.YEAR,          //�������
                MediaStore.Audio.Media.MIME_TYPE,     //��������
                MediaStore.Audio.Media.SIZE,          //�ļ���С
                MediaStore.Audio.Media.DATA };        //�ļ�·��
        // ����sort_key�����ԃ
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
     * �����ļ�������
     */
    private class MusicAsyncQueryHandler extends AsyncQueryHandler {

        public MusicAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.d(TAG,"��ʼ��ѯ������");
            allSongs.clear();
            int count = cursor.getCount();
            if (cursor != null && count > 0) {
                cursor.moveToFirst(); // �α��ƶ�����һ��
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
                        song.setTitle("δ֪����");
                    }
                    int duration = cursor.getInt(3);
                    String artist = FunctionUtil.formateString(cursor.getString(4));
                    if(!TextUtils.isEmpty(artist)){
                        song.setSinger(artist);
                    }else{
                        song.setSinger("δ֪����");
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
            Log.d(TAG,"������ѯ���,����"+count+"�׸�����");

            super.onQueryComplete(token, cookie, cursor);
        }
    }

    /**
     * �첽��ѯ���е���ϵ����Ϣ
     */
    public void startLoadContact(){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // ��ϵ��Uri��
        // ��ѯ���ֶ�
        String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1 };
        // ����sort_key�����ԃ
        mContactAsyncQueryHandler.startQuery(0, null, uri, projection, null, null,
                "sort_key COLLATE LOCALIZED asc");
    }

    /**
     * ��ϵ�˼�����
     */
    private class ContactAsyncQueryHandler extends AsyncQueryHandler {

        public ContactAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.d(TAG,"��ʼ��ѯ��ϵ�ˣ�");
            allContacts.clear();
            int count = cursor.getCount();
            if (cursor != null && count > 0) {
                cursor.moveToFirst(); // �α��ƶ�����һ��
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);
                    String name = cursor.getString(1);
                    String number = cursor.getString(2);
                    // ������ϵ�˶���
                    Contact contact = new Contact();
                    contact.setName(FunctionUtil.formateString(name));
                    contact.setPhoneNumber(number);
                    allContacts.add(contact);
                }
                cursor.close();
            }
            Log.d(TAG,"��ϵ�˲�ѯ���,����"+count+"����ϵ�ˣ�");
            super.onQueryComplete(token, cookie, cursor);
        }
    }

    /**
     * ��ȡ���е���ϵ�˵�����
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
     * ��ȡ���е�Ӧ�õ�����
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
     * ��ȡ���е���������
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