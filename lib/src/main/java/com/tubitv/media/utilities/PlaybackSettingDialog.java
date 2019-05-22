package com.tubitv.media.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

public abstract class PlaybackSettingDialog {

    private static String[] settingOptions = {"Playback Speed"};

    public static void show(@NonNull Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Setting");

        builder.setItems(settingOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // TODO
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


}
