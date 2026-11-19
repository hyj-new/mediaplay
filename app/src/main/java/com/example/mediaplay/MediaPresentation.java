package com.example.mediaplay;
import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MediaPresentation extends Presentation {

    private TextView tvSongName, tvVolume;
    private Button btnPlay, btnPrev, btnNext, btnMute;
    private SeekBar seekVolume;
    private BroadcastReceiver receiver;

    public MediaPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.presentation_media);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.x = -200;  // 向左偏移200像素，完整露出
        lp.y = 0;
        Log.d("偏移","200");
        getWindow().setAttributes(lp);
        tvSongName = findViewById(R.id.tv_song_name);
        tvVolume = findViewById(R.id.tv_volume);
        btnPlay = findViewById(R.id.btn_play);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnMute = findViewById(R.id.btn_mute);
        seekVolume = findViewById(R.id.seek_volume);

        btnPlay.setOnClickListener(v -> sendControl("PLAY_PAUSE"));
        btnPrev.setOnClickListener(v -> sendControl("PREV"));
        btnNext.setOnClickListener(v -> sendControl("NEXT"));
        btnMute.setOnClickListener(v -> sendControl("MUTE"));

        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    Intent intent = new Intent("VOLUME_CHANGE");
                    intent.putExtra("volume", progress);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }
                //getContext().sendBroadcast(new Intent("VOLUME_CHANGE").putExtra("volume", progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("副屏接收",intent.getAction());
                String song = intent.getStringExtra("song");
                boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
                int volume = intent.getIntExtra("volume", 50);

                tvSongName.setText(song);
                tvVolume.setText("音量：" + volume);
                seekVolume.setProgress(volume);
                btnPlay.setText(isPlaying ? "暂停" : "播放");
            }
        };
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(receiver, new IntentFilter("MEDIA_STATUS_UPDATE"));
        //getContext().registerReceiver(receiver, new IntentFilter("MEDIA_STATUS_UPDATE"));
    }
    @Override
    public void dismiss() {
        super.dismiss();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }
    private void sendControl(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent); //
        //getContext().sendBroadcast(new Intent(action));
    }

    // 销毁时解绑广播，防止内存泄漏

}