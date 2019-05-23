package com.tubitv.media.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.tubitv.media.models.PlaybackSpeed;

public class PlaybackSettingMenu {

    private Context context;
    private AlertDialog dialog;
    private static String[] settingOptions = {"Playback Speed"};

    public PlaybackSettingMenu build(@NonNull Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Setting");

        builder.setSingleChoiceItems(settingOptions, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // TODO
                        break;
                }
                dialog.dismiss();   // always close onClick
            }
        });
        dialog = builder.create();
        return this;
    }

    public void show() {
        if (dialog == null) {
            throw new NullPointerException("Menu must be built first");
        } else {
            dialog.show();
        }
    }

    public interface PlaybackSpeedCallback {
        void onSelect(PlaybackSpeed playbackSpeed);
    }
    public PlaybackSettingMenu addPlaybackSpeedOption(@NonNull PlaybackSpeedCallback callback) {

        return this;
    }

}
