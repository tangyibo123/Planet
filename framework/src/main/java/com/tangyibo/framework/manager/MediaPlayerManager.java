package com.tangyibo.framework.manager;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;

import com.tangyibo.framework.utils.LogUtils;

import java.io.IOException;

public class MediaPlayerManager {

    private MediaPlayer mMediaPlayer;

    //表示状态的静态常量
    //播放
    public static final int MEDIA_STATUS_PLAY = 0;
    //暂停
    public static final int MEDIA_STATUS_PAUSE =1;
    //停止
    public static final int MEDIA_STATUS_STOP = 2;
    //当前状态
    public static int MEDIA_STATUS = MEDIA_STATUS_STOP;

    //构造方法，实例化一个MediaPlayer对象
    public MediaPlayerManager(){
        mMediaPlayer = new MediaPlayer();
    }

    /**
     * 是否在播放
     * @return true/false
     */
    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    /**
     * 设置是否循环播放
     */
    public void setLooping(boolean isLooping){
        mMediaPlayer.setLooping(isLooping);
    }

    /**
     * 播放
     * @param path
     */
    public void startPlay(AssetFileDescriptor path){
        mMediaPlayer.reset();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaPlayer.setDataSource(path.getFileDescriptor(),
                        path.getStartOffset(), path.getLength());
            }
        } catch (IOException e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            LogUtils.e(e.toString());
            e.printStackTrace();
        }
        mMediaPlayer.start();
        MEDIA_STATUS = MEDIA_STATUS_PLAY;
    }

    /**
     * 暂停播放
     */
    public void pausePlay(){
        if(isPlaying()){
            mMediaPlayer.pause();
            MEDIA_STATUS = MEDIA_STATUS_PAUSE;
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay(){
        mMediaPlayer.start();
        MEDIA_STATUS = MEDIA_STATUS_PLAY;
    }

    /**
     * 跳转播放
     * @param s
     */
    public void seekToPlay(int s){
        mMediaPlayer.seekTo(s);
    }

    /**
     * 停止播放
     */
    public void stopPlay(){
        mMediaPlayer.stop();
        MEDIA_STATUS = MEDIA_STATUS_STOP;
    }

    /**
     * 获取当前位置
     */
    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 获取总时长
     */
    public int getDuration(){
        return mMediaPlayer.getDuration();
    }

    /**
     * 设置播放结束监听器
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
        mMediaPlayer.setOnCompletionListener(listener);
    }

    /**
     *设置播放错误监听器
     */
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener){
        mMediaPlayer.setOnErrorListener(listener);
    }



}
