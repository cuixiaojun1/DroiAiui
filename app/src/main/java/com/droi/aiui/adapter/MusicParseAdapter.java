package com.droi.aiui.adapter;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;

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
    private IMediaPlaybackService mMusicService;

    public MusicParseAdapter(Context context) {
        mContext = context;
        allSongs = DataControler.getInstance(context).loadAllSongs();
        mMusicService = AiuiManager.getInstance(context).getMusicService();
    }

    @Override
    public String getSemanticResultText(String json) {
        mMusicBean = JsonParserUtil.parseJsonObject(json,MusicBean.class);
        return handlePlayIntent(getPlayIntent());
    }

    /**
     * ��ȡ���ֲ�����ͼ
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
     * ��ȡ�����ľ�����Ϣ���������ֵ����ƺ͸���������
     */
    private String getSongInfoFromServerByType(String type){
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
     * ���������ͼ
     */
    private String handlePlayIntent(String intent){
        String result = null;
        if(intent != null){
            switch (intent){
                case PLAY_TYPE_RANDOM:
                    result = playSongByRandom(allSongs);
                    break;
                case PLAY_TYPE_STYLE:
                    String style = getSongInfoFromServerByType(TYPE_STYLE);
                    List<Song> songs = getSongsByType(allSongs,style);
                    result = playSongByRandom(songs);
                    break;
                case PLAY_TYPE_SONG:
                    String name = getSongInfoFromServerByType(TYPE_NAME);
                    String singer = getSongInfoFromServerByType(TYPE_SINGER);
                    result = playSongByName(name,singer);
                    break;
                case PLAY_TYPE_SINGER:
                    String singer1 = getSongInfoFromServerByType(TYPE_SINGER);
                    List<Song> songs1 = getSongsBySinger(allSongs,singer1);
                    result = playSongByRandom(songs1);
                    break;
                case PLAY_TYPE_PREV:
                    result = playSongToNext(false);
                    break;
                case PLAY_TYPE_NEXT:
                    result = playSongToNext(true);
                    break;
                case PLAY_TYPE_PAUSE:
                case PLAY_TYPE_STOP:
                    result = stopPlaySong();
                    break;
                    default:
                        break;
            }
        }
        return result;
    }

    /**
     * �������ĳ���͸����������и�������ĳ�����ֵ����и���
     * ���ܰ���ĳ�����͵ĸ�����ȫ��������ĳ���ֵĸ���
     */
    private String playSongByRandom(List<Song> songs){
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
    private String playSongByName(String songName,String singer){
        String result;
        if(allSongs != null && allSongs.size() > 0){
            if(!TextUtils.isEmpty(singer)){
                if(isSingerExisted(allSongs,singer)){//�жϸ����Ƿ����
                    if(isSongNameExisted(allSongs,songName)){//�жϸ����Ƿ����
                        if(mMusicService != null){
                            result = "����Ϊ������"+singer+"��"+songName+"!";
                            playMusicByName(mMusicService,songName);
                        }else{
                            result = "���ַ����ʼ��ʧ�ܣ������³��ԣ�";
                        }
                    }else{
                        result = "û���ҵ�"+singer+"��"+songName+"����������ĸ��ֵĸ�����";
                    }
                }else {
                    result = "û���ҵ�"+singer+"��صĸ�������������ĸ��ֵĸ�����";
                }
            }else{
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
            }
        }else{
            result = "û���������ֻ����ҵ������������֮�����³��ԣ�";
        }
        return result;
    }

    /**
     * ���Ÿ���
     */
    private void playMusicByName(IMediaPlaybackService mMusicService,String songName){
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
        FunctionUtil.playMusicByList(mMusicService,playList,position);
    }


    /**
     * �������
     */
    private String playSongToNext(boolean isNext){
        String result = null;
        if(mMusicService != null) try {
            long[] queue = mMusicService.getQueue();
            int queuePosition = mMusicService.getQueuePosition();
            int repeatMode = mMusicService.getRepeatMode();//1  ����ѭ�� //0������� //2˳�򲥷�
            if (queue != null && queue.length > 0) {
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
                        result = "����Ϊ������" + songName + "!";
                        if (isNext) {
                            FunctionUtil.playMusicByList(mMusicService, queue, queuePosition + 1);
                        } else {
                            FunctionUtil.playMusicByList(mMusicService, queue, queuePosition - 1);
                        }
                    } else {
                        result = "�Բ���û���ҵ��ø�����������������ĸ�����";
                    }
                } else {
                    result = "û���������ֻ����ҵ������������֮�����³��ԣ�";
                }
            } else if (allSongs != null && allSongs.size() > 0) {
                result = playSongByRandom(allSongs);
            } else {
                result = "û���������ֻ����ҵ������������֮�����³��ԣ�";
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        else{
            result = "���ַ����ʼ��ʧ�ܣ����Ժ����ԣ�";
        }
        return result;
    }

    /**
     * ͨ������id��ȡ��������
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
     * ��ͣ���Ÿ���
     */
    private String stopPlaySong(){
        String result;
        if(mMusicService != null){
            result = "�õģ����Ҿ���ֹͣ�����ˣ������������ʱ���ٸ����Ұɣ�";
            FunctionUtil.stopPlaySong(mMusicService);
        }else{
            result = "���ַ����ʼ��ʧ�ܣ����Ժ����ԣ�";
        }

        return result;
    }

    /**
     * ͨ���������ͻ�ȡ��ص����и���
     * @return
     */
    private List<Song> getSongsByType(List<Song> allSongs,String type){
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if(song.getFileUrl().contains(type)){//��ȡ�������͸���
                songs.add(song);
            }
        }
        return songs;
    }

    /**
     * ͨ���������ͻ�ȡ��ص����и���
     * @return
     */
    private List<Song> getSongsBySinger(List<Song> allSongs,String singer){
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if(song.getSinger().contains(singer)){//��ȡ�������͸���
                songs.add(song);
            }
        }
        return songs;
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
     * ͨ���������ƺ͸������ƻ�ȡ���ֵĲ���·��
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
     * �жϸ����Ƿ����
     */
    private boolean isSongNameExisted(List<Song> allSongs,String songName){
        for (int i = 0; i <allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if(song.getTitle().contains(songName)){
                return true;
            }
        }
        return false;
    }

    /**
     * �жϸ����Ƿ����
     */
    private boolean isSingerExisted(List<Song> allSongs,String singer){
        for (int i = 0; i <allSongs.size(); i++) {
            Song song = allSongs.get(i);
            if(song.getSinger().contains(singer)){
                return true;
            }
        }
        return false;
    }
}