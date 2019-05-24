package com.tubitv.media.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.models.PlaybackSpeed;

import java.util.ArrayList;

/**
 * A nested setting menu using AlertDialog.
 */
public class PlaybackSettingMenu {

    private SimpleExoPlayer contentPlayer;

    private Context context;

    private AlertDialog mainDialog;

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

        mainDialog = builder.create();
        setAlertDialogGravityBottomCenter(mainDialog);
        mainDialog.show();
    }

    public void dismiss() {
        if (mainDialog != null) {
            mainDialog.dismiss();
        }
    }

    private void showPlaybackSpeedMenu() {
        ArrayList<String> playbackSpeedTexts = new ArrayList<>();
        ArrayList<Float> playbackSpeedValues = new ArrayList<>();

        for (PlaybackSpeed playbackSpeed : PlaybackSpeed.getAllPlaybackSpeedEnums()) {
            playbackSpeedTexts.add(playbackSpeed.getText(context));
            playbackSpeedValues.add(playbackSpeed.getSpeed());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(playbackSpeedTexts.toArray(new String[playbackSpeedTexts.size()]),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
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
