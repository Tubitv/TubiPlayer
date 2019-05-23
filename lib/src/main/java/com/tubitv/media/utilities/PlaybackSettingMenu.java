package com.tubitv.media.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.models.PlaybackSpeed;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class PlaybackSettingMenu {

    private SimpleExoPlayer contentPlayer;

    private Context context;

    private static String[] settingOptions = {"Playback Speed"};    // hard coded for now

    public PlaybackSettingMenu() {
    }

    public PlaybackSettingMenu(@NonNull SimpleExoPlayer contentPlayer, @NonNull View exoPlayerView) {
        this.contentPlayer = contentPlayer;
        this.context = exoPlayerView.getContext();
    }

    public void setContentPlayer(@NonNull SimpleExoPlayer contentPlayer) {
        this.contentPlayer = contentPlayer;
    }

    public void setContext(@NonNull Context context) {
        this.context = context;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Setting");

        builder.setItems(settingOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case 0:
                        showPlaybackSpeedMenu();
                        break;
                }
                dialog.dismiss();   // always close after click
            }
        });

        AlertDialog dialog = builder.create();
        setAlertDialogGravityBottomCenter(dialog);
        dialog.show();
    }

    private void showPlaybackSpeedMenu() {
        ArrayList<PlaybackSpeed> playbackSpeeds = new ArrayList<>(Arrays.asList(PlaybackSpeed.class.getEnumConstants()));

        LinkedList<String> playbackSpeedTexts = new LinkedList<>();
        LinkedList<Float> playbackSpeedValues = new LinkedList<>();

        for (PlaybackSpeed playbackSpeed : playbackSpeeds) {
            playbackSpeedTexts.add(playbackSpeed.getCopy(context));
            playbackSpeedValues.add(playbackSpeed.getSpeed());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(playbackSpeedTexts.toArray(new String[playbackSpeedTexts.size()]),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(context, playbackSpeedTexts.get(i), Toast.LENGTH_SHORT).show();

                PlaybackParameters originParameters = contentPlayer.getPlaybackParameters();
                PlaybackParameters updatedSpeedParameters = new PlaybackParameters(
                        playbackSpeedValues.get(i),
                        originParameters.pitch, // Keeping old values
                        originParameters.skipSilence
                );

                contentPlayer.setPlaybackParameters(updatedSpeedParameters);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        setAlertDialogGravityBottomCenter(dialog);
        dialog.show();
    }

    private void setAlertDialogGravityBottomCenter(AlertDialog alertDialog) {
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        if (layoutParams != null) {
            layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        }
    }
}
