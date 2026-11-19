package com.example.mediaplay;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvSongName, tvLyrics;
    private Button btnPlay, btnPrev, btnNext, btnMute;
    private SeekBar seekVolume, seekProgress;
    private MediaPresentation presentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSongName = findViewById(R.id.tv_song_name);
        tvLyrics = findViewById(R.id.tv_lyrics);
        btnPlay = findViewById(R.id.btn_play);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnMute = findViewById(R.id.btn_mute);
        seekVolume = findViewById(R.id.seek_volume);
        seekProgress = findViewById(R.id.seek_progress);

        // 先启动音乐服务
        startService(new Intent(this, MusicService.class));

        // 延迟100ms启动副屏，确保系统加载完
        findViewById(android.R.id.content).postDelayed(this::showPresentation, 100);

        // 按钮控制
        btnPlay.setOnClickListener(v -> sendControl("PLAY_PAUSE"));
        btnPrev.setOnClickListener(v -> sendControl("PREV"));
        btnNext.setOnClickListener(v -> sendControl("NEXT"));
        btnMute.setOnClickListener(v -> sendControl("MUTE"));

        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                {
                    Intent intent = new Intent("VOLUME_CHANGE");
                    intent.putExtra("volume", progress);
                    androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(MainActivity.this)
                            .sendBroadcast(intent);
                }
                //sendBroadcast(new Intent("VOLUME_CHANGE").putExtra("volume", progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter("MEDIA_STATUS_UPDATE"));
        //registerReceiver(receiver, new IntentFilter("MEDIA_STATUS_UPDATE"));
    }

    private void showPresentation() {
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();
        // 必须判断数量=2，再创建
        Log.d("副屏数量",String.valueOf(displays.length));
        if (displays.length >= 2) {
            if (presentation == null) {
                Log.d("副屏启动","成功显示1");
                presentation = new MediaPresentation(this, displays[1]);
                //presentation.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                presentation.show();
                Log.d("副屏启动","成功显示2");
            }
        }
    }
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
//                .unregisterReceiver(receiver);
//    }
    private void sendControl(String action) {
        Log.d("播放","sendcontrol");
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(action));
        //sendBroadcast(new Intent(action));
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("播放","broadcastreceiver");
            String song = intent.getStringExtra("song");
            boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
            int volume = intent.getIntExtra("volume", 50);

            tvSongName.setText(song);

            btnPlay.setText(isPlaying ? "暂停" : "播放");
            seekVolume.setProgress(volume);
        }
    };
}