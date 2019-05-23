package com.tubitv.media.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.models.PlaybackSpeed;

public class PlaybackSettingMenu {

    private SimpleExoPlayer contentPlayer;

    private Context context;

    private static String[] settingOptions = {"Playback Speed"};

    public PlaybackSettingMenu() {
    }

    public PlaybackSettingMenu(@NonNull SimpleExoPlayer contentPlayer, @NonNull View exoPlayerView) {
        this.contentPlayer = contentPlayer;
        this.context = exoPlayerView.getContext();
    }

    public void setContentPlayer(SimpleExoPlayer contentPlayer) {
        this.contentPlayer = contentPlayer;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Setting");

        builder.setItems(settingOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // TODO
                        Toast.makeText(context, "HaHa Playback Speed", Toast.LENGTH_SHORT).show();
                        break;
                }
                dialog.dismiss();   // always close onClick
            }
        });
        builder.create().show();
    }

    public interface PlaybackSpeedCallback {
        void onSelect(PlaybackSpeed playbackSpeed);
    }
    public PlaybackSettingMenu addPlaybackSpeedOption(@NonNull PlaybackSpeedCallback callback) {

        return this;
    }

}
