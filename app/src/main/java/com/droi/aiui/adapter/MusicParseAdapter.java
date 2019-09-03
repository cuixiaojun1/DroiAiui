package com.droi.aiui.adapter;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.droi.aiui.AiuiManager;
import com.droi.aiui.bean.MusicBean;
import com.droi.aiui.bean.Song;
import com.droi.aiui.controler.DataControler;
import com.droi.aiui.util.FunctionUtil;
import com.droi.aiui.util.JsonParserUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by cuixiaojun on 18-2-1.
 */

public class MusicParseAdapter extends BaseParseAdapter {

    private final String TAG = "MusicParseAdapter";

    private static final String TYPE_NAME = "song";
    private static final String TYPE_SINGER = "singer";
    private static final String TYPE_STYLE = "songStyle";

    private static final String PLAY_TYPE_RANDOM = "play_by_random";
    private static final String PLAY_TYPE_SONG = "play_by_song";
    private static final String PLAY_TYPE_SINGER = "play_by_singer";
    private static final String PLAY_TYPE_NEXT = "play_to_next";
    private static final String PLAY_TYPE_PAUSE = "play_to_pause";
    private static final String PLAY_TYPE_PREV = "play_to_prev";
    private static final String PLAY_TYPE_STOP = "play_to_stop";
    private static final String PLAY_TYPE_STYLE = "play_by_style";

    private Context mContext;
    private List<Song> allSongs;
    private MusicBean mMusicBean;

    public MusicParseAdapter(Context context) {
        mContext = context;
        allSongs = DataControler.getInstance(context).loadAllSongs();
    }

    @Override
    public String getSemanticResultText(String json) {
        mMusicBean = JsonParserUtil.parseJsonObject(json,MusicBean.class);
        return handlePlayIntent(getPlayIntent());
    }

    /**
     * 获取音乐播放意图
     */
    private String getPlayIntent(){
        if(mMusicBean != null){
            List<MusicBean.SemanticBean> semantic = mMusicBean.getSemantic();
            if(semantic != null && semantic.size() > 0){
                for (int i = 0; i < semantic.size(); i++) {
                    MusicBean.SemanticBean semanticBean = semantic.get(i);
                    if(semanticBean != null){
                        return semanticBean.getIntent();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取歌曲的具体信息，包括歌手的名称和歌曲的名称
     */
    private String getSongInfoFromServerByType(String type){
        Log.d(TAG,"[MusicParseAdapter][getSongInfoFromServerByType]type = "+type);
        if(mMusicBean != null){
            List<MusicBean.SemanticBean> semantic = mMusicBean.getSemantic();
            if(semantic != null && semantic.size() >0) {
                for (int i = 0; i <semantic.size(); i++) {
                    MusicBean.SemanticBean semanticBean = semantic.get(i);
                    if(semanticBean != null){
                        List<MusicBean.SemanticBean.SlotsBean> slots = semanticBean.getSlots();
                        if(slots != null && slots.size() > 0){
                            for (int j = 0; j < slots.size(); j++) {
                                MusicBean.SemanticBean.SlotsBean slotsBean = slots.get(j);
                                if(slotsBean != null){
                                    if(slotsBean.getName().equals(type)){
                                        return slotsBean.getNormValue();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 处理歌曲意图
     */
    private String handlePlayIntent(String intent){
        Log.d(TAG,"[MusicParseAdapter][handlePlayIntent]intent = "+intent);
        String result = null;
        if(intent != null){
            switch (intent){
                case PLAY_TYPE_RANDOM:
                    Log.d(TAG,"[MusicParseAdapter][handlePlayIntent]PLAY_TYPE_RANDOM--->size = "+allSongs.size());
                    result = playSongByRandom(allSongs);
                    break;
                case PLAY_TYPE_STYLE:
                    String style = getSongInfoFromServerByType(TYPE_STYLE);
                    List<Song> songs = getSongsByType(allSongs,style);
                    result = playSongByRandom(songs);
                    Log.d(TAG,"[MusicParseAdapter][handlePlayIntent]PLAY_TYPE_STYLE--->style = "+style+",size = "+songs.size());
                    break;
                case PLAY_TYPE_SONG:
                    String name = getSongInfoFromServerByType(TYPE_NAME);
                    String singer = getSongInfoFromServerByType(TYPE_SINGER);
                    result = playSongByName(name,singer);
                    Log.d(TAG,"[MusicParseAdapter][handlePlayIntent]PLAY_TYPE_SONG--->name = "+name+",singer = "+singer);
                    break;
                case PLAY_TYPE_SINGER:
                    String singer1 = getSongInfoFromServerByType(TYPE_SINGER);
                    List<Song> songs1 = getSongsBySinger(allSongs,singer1);
                    Log.d(TAG,"[MusicParseAdapter][handlePlayIntent]PLAY_TYPE_SINGER--->singer1 = "+singer1+",size = "+songs1.size());
                    result = playSongByRandom(songs1);
                    break;
                case PLAY_TYPE_PREV:
                    //result = playSongToNext(false);
                    Log.d(TAG,"[MusicParseAdapter][handlePlayIntent]PLAY_TYPE_PREV");
                    break;
                case PLAY_TYPE_NEXT:
                    //result = playSongToNext(true);
                    Log.d(TAG,"[MusicParseAdapter][handlePlayIntent]PLAY_TYPE_NEXT");
                    break;
                case PLAY_TYPE_PAUSE:
                case PLAY_TYPE_STOP:
                    //result = stopPlaySong();
                    Log.d(TAG,"[MusicParseAdapter][handlePlayIntent]PLAY_TYPE_STOP");
                    break;
                    default:
                        break;
            }
        }
        return result;
    }

    /**
     * 随机播放某类型歌曲或者所有歌曲或者某个歌手的所有歌曲
     * 可能包含某种类型的歌曲，全部歌曲，某歌手的歌曲
     */
    private String playSongByRandom(List<Song> songs){
        Log.d(TAG,"[MusicParseAdapter][playSongByRandom]allSongsSize = "+allSongs.size());
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
    private String playSongByName(String songName,String singer){
        Log.d(TAG,"[playSongByName]songName = "+songName+",singer = "+singer);
        String result;
        if(allSongs != null && allSongs.size() > 0){
            if(!TextUtils.isEmpty(singer)){
                if(isSingerExisted(allSongs,singer)){//判断歌手是否存在
                    if(isSongNameExisted(allSongs,songName)){//判断歌曲是否存在
                        result = "音乐服务初始化失败，请重新尝试！";
                    }else{
                        result = "没有找到"+singer+"的"+songName+"，请听听别的歌手的歌曲！";
                    }
                }else {
                    result = "没有找到"+singer+"相关的歌曲，请听听别的歌手的歌曲！";
                }
            }else{
                if(isSongNameExisted(allSongs,songName)){//判断歌曲是否存在
                    result = "音乐服务初始化失败，请重新尝试！";
                }else{
                    result = "没有找到歌曲"+songName+"，您可以听听别的歌曲！";
                }
            }
        }else{
            result = "没有在您的手机中找到歌曲，请添加之后重新尝试！";
        }
        return result;
    }

    /**
     * 播放歌曲
     */
    /*private void playMusicByName(String songName){
        Log.d(TAG,"[MusicParseAdapter][playMusicByName]mMusicService = "+mMusicService+",songPath = "+songName);
        long[] playList = FunctionUtil.getPlayList(allSongs);
        long songId = 0;
        int position = 0;
        for (int i = 0; i < allSongs.size(); i++) {
            if(allSongs.get(i).getTitle().contains(songName)){
                songId = allSongs.get(i).getId();
            }
        }
        for (int i = 0; i < playList.length; i++) {
            if(playList[i] == songId){
                position = i;
            }
        }
        FunctionUtil.playMusicByList(playList,position);
    }*/


    /**
     * 随机歌曲
     */
    /*private String playSongToNext(boolean isNext){
        String result = null;
        if(mMusicService != null) try {
            long[] queue = mMusicService.getQueue();
            int queuePosition = mMusicService.getQueuePosition();
            int repeatMode = mMusicService.getRepeatMode();//1  单曲循环 //0随机播放 //2顺序播放�
            Log.d(TAG, "[MusicParseAdapter][playSongToNext]queue = " + queue
                    + ",queuePosition = " + queuePosition+",repeatMode = "+repeatMode);
            if (queue != null && queue.length > 0) {
                Log.d(TAG, "[MusicParseAdapter][playSongToNext]queue.size = "+queue.length);
                if (isNext) {
                    if(repeatMode == 0){
                        Random random = new Random();
                        queuePosition = random.nextInt(queue.length);
                    }else{
                        if (queuePosition == queue.length - 1) {
                            queuePosition = 0;
                        }
                    }
                } else {
                    if(repeatMode == 0){
                        Random random = new Random();
                        queuePosition = random.nextInt(queue.length);
                    }else{
                        if (queuePosition == 0) {
                            queuePosition = queue.length - 1;
                        }
                    }
                }
                if (allSongs != null && allSongs.size() > 0) {
                    String songName;
                    if (isNext) {
                        songName = getSongName(queue[queuePosition + 1]);
                    } else {
                        songName = getSongName(queue[queuePosition - 1]);
                    }
                    if (!TextUtils.isEmpty(songName)) {
                        result = "即将为您播放" + songName + "!";
                        if (isNext) {
                            FunctionUtil.playMusicByList(mMusicService, queue, queuePosition + 1);
                        } else {
                            FunctionUtil.playMusicByList(mMusicService, queue, queuePosition - 1);
                        }
                    } else {
                        result = "对不起，没有找到该歌曲，您可以听听别的歌曲！";
                    }
                } else {
                    result = "没有在您的手机中找到歌曲，请添加之后重新尝试！";
                }
            } else if (allSongs != null && allSongs.size() > 0) {
                result = playSongByRandom(allSongs);
            } else {
                result = "没有在您的手机中找到歌曲，请添加之后重新尝试！";
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        else{
            result = "音乐服务初始化失败，请稍后再试！";
        }
        return result;
    }*/

    /**
     * 通过歌曲id获取歌曲名称
     */
    private String getSongName(long songId){
        for (int i = 0; i <allSongs.size(); i++) {
            if(allSongs.get(i).getId() == songId){
                return allSongs.get(i).getTitle();
            }
        }
        return null;
    }

    /**
     * 暂停播放歌曲
     */
    /*private String stopPlaySong(){
        String result;
        if(mMusicService != null){
            result = "好的，那我就先停止播放了，等您想听歌的时候再告诉我吧！";
            FunctionUtil.stopPlaySong(mMusicService);
        }else{
            result = "音乐服务初始化失败，请稍后再试！";
        }

        return result;
    }*/

    /**
     * 通过歌曲类型获取相关的所有歌曲
     * @return
     */
    private List<Song> getSongsByType(List<Song> allSongs,String type){
        Log.d(TAG,"[MusicParseAdapter][getSongsByType]allSize = "+allSongs.size()+",type = "+type);
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if(song.getFileUrl().contains(type)){//获取所有类型歌曲
                songs.add(song);
            }
        }
        return songs;
    }

    /**
     * 通过歌曲类型获取相关的所有歌曲
     * @return
     */
    private List<Song> getSongsBySinger(List<Song> allSongs,String singer){
        Log.d(TAG,"[MusicParseAdapter][getSongsByType]allSize = "+allSongs.size()+",singer = "+singer);
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if(song.getSinger().contains(singer)){//获取所有类型歌曲
                songs.add(song);
            }
        }
        return songs;
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
     * 通过音乐名称和歌手名称获取音乐的播放路径
     */
    private String getSongPathByNameAndSinger(List<Song> allSongs,String songName,String singer){
        for (int i = 0; i <allSongs.size(); i++) {
            if(allSongs.get(i).getTitle().contains(songName) && allSongs.get(i).getSinger().contains(singer)){
                return allSongs.get(i).getFileUrl();
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
     * 判断歌手是否存在
     */
    private boolean isSingerExisted(List<Song> allSongs,String singer){
        Log.d(TAG,"[MusicParseAdapter][isExisted]singer = "+singer);
        for (int i = 0; i <allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if(song.getSinger().contains(singer)){
                return true;
            }
        }
        return false;
    }
}