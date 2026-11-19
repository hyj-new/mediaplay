package com.example.mediaplay;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private List<String> songList;
    private int currentIndex = 0;
    private int currentVolume = 50;
    private boolean isMute = false;
    private AudioManager audioManager;

    @Override
    public void onCreate() {
        super.onCreate();
        songList = new ArrayList<>();
        songList.add("歌曲 1：中控媒体测试");
        songList.add("歌曲 2：双屏同步播放");
        songList.add("歌曲 3：车载大屏演示");

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.test1); // 先放一首测试音乐

        // 接收控制广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("PLAY_PAUSE");
        filter.addAction("PREV");
        filter.addAction("NEXT");
        filter.addAction("MUTE");
        filter.addAction("VOLUME_CHANGE");
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                .registerReceiver(controlReceiver, filter);
    }

    private final BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("播放", "Service 收到了：" + action);
            switch (action) {
                case "PLAY_PAUSE":
                    togglePlay();
                    break;
                case "PREV":
                    prevSong();
                    break;
                case "NEXT":
                    nextSong();
                    break;
                case "MUTE":
                    muteSong();
                    break;
                case "VOLUME_CHANGE":
                    currentVolume = intent.getIntExtra("volume", 50);
                    setMediaVolume(currentVolume);
                    break;
            }
            postUpdate(); // 所有操作后同步双屏
        }
    };

    private void togglePlay() {
        Log.d("播放",String.valueOf(mediaPlayer.isPlaying()));
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        else mediaPlayer.start();
    }

    private void nextSong() {
        currentIndex = (currentIndex + 1) % songList.size();
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(this, R.raw.test1);
        mediaPlayer.start();
    }

    private void prevSong() {
        currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(this, R.raw.test1);
        mediaPlayer.start();
    }

    private void muteSong() {
        isMute = !isMute;
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, isMute);
    }

    private void setMediaVolume(int vol) {
        float volume = vol / 100f;
        mediaPlayer.setVolume(volume, volume);
    }

    // 同步状态给双屏
    private void postUpdate() {
        Intent intent = new Intent("MEDIA_STATUS_UPDATE");
        intent.putExtra("song", songList.get(currentIndex));
        intent.putExtra("isPlaying", mediaPlayer.isPlaying());
        intent.putExtra("volume", currentVolume);
        intent.putExtra("isMute", isMute);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(controlReceiver);
        if(mediaPlayer != null) mediaPlayer.release();
    }
    @Override
    public IBinder onBind(Intent intent) {return null;}
}
