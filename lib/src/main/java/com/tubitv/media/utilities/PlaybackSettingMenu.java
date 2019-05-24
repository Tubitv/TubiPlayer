package com.tubitv.media.utilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.R;
import com.tubitv.media.models.PlaybackSpeed;

import java.util.ArrayList;

/**
 * A nested playback setting menu using AlertDialog.
 */
public class PlaybackSettingMenu {

    private SimpleExoPlayer contentPlayer;

    private Activity activity;

    private AlertDialog mainDialog;

    private ArrayList<MenuOption> menuOptions;

    public PlaybackSettingMenu() {
    }

    public PlaybackSettingMenu(@NonNull SimpleExoPlayer contentPlayer, @NonNull Activity playerActivity) {
        this.contentPlayer = contentPlayer;
        this.activity = playerActivity;
    }

    public void setContentPlayer(@NonNull SimpleExoPlayer contentPlayer) {
        this.contentPlayer = contentPlayer;
    }

    public void setActivity(@NonNull Activity activity) {
        this.activity = activity;
    }

    public void buildSettingMenuOptions() {
        menuOptions = new ArrayList<>();

        // Option can be separately injected from root activity if needed.
        // It requires dependencies: activityContext & contentSimpleExoPlayer.
        MenuOption playbackSpeedOption = new MenuOption(activity.getString(
                R.string.playback_setting_speed_title), new MenuOptionCallback() {
            @Override
            public void onClick() {
                ArrayList<String> playbackSpeedTexts = new ArrayList<>();
                ArrayList<Float> playbackSpeedValues = new ArrayList<>();

                for (PlaybackSpeed playbackSpeed : PlaybackSpeed.getAllPlaybackSpeedEnums()) {
                    playbackSpeedTexts.add(playbackSpeed.getText(activity));
                    playbackSpeedValues.add(playbackSpeed.getSpeedValue());
                }

                String[] speedOptionTextArray = playbackSpeedTexts.toArray(new String[0]);
                int currentSpeedPosition = PlaybackSpeed.getPlaybackSpeedPositionBySpeedValue(
                        contentPlayer.getPlaybackParameters().speed);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setSingleChoiceItems(
                        speedOptionTextArray,
                        currentSpeedPosition,   // When is -1, none will be selected.
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

                AlertDialog chooseSpeedDialog = builder.create();
                setAlertDialogGravityBottomCenter(chooseSpeedDialog);
                alertDialogImmersiveShow(chooseSpeedDialog);
            }

            @Override
            public String getTitle(String defaultTitle) {
                // Dynamic title
                Float currentSpeedValue = contentPlayer.getPlaybackParameters().speed;
                PlaybackSpeed currentPlaybackSpeed = PlaybackSpeed.getPlaybackSpeedBySpeedValue(currentSpeedValue);
                if (currentPlaybackSpeed != null) {
                    defaultTitle = defaultTitle + " - " + currentPlaybackSpeed.getText(activity);
                }
                return defaultTitle;
            }
        });

        menuOptions.add(playbackSpeedOption);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.playback_setting_title));

        String[] settingOptionTitles = new String[menuOptions.size()];
        for (int i = 0; i < menuOptions.size(); i++) {
            settingOptionTitles[i] = menuOptions.get(i).getTitle();
        }

        builder.setItems(settingOptionTitles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                menuOptions.get(i).onClick();
                dialog.dismiss();
            }
        });

        mainDialog = builder.create();
        setAlertDialogGravityBottomCenter(mainDialog);
        alertDialogImmersiveShow(mainDialog);
    }

    public void dismiss() {
        if (mainDialog != null) {
            mainDialog.dismiss();
        }
    }

    private void setAlertDialogGravityBottomCenter(AlertDialog alertDialog) {
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        if (layoutParams != null) {
            layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        }
    }

    /*
     * This method is to keep the system navigation bar not appearing after dialog calling .show().
     *
     * More on this problem:
     * https://stackoverflow.com/questions/22794049/how-do-i-maintain-the-immersive-mode-in-dialogs
     */
    private void alertDialogImmersiveShow(AlertDialog alertDialog) {
        // Set dialog not-focusable, to avoid popping the nav bar.
        alertDialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        alertDialog.show();

        // Copy the hosted activity systemUI setting.
        alertDialog.getWindow().getDecorView().setSystemUiVisibility(
                activity.getWindow().getDecorView().getSystemUiVisibility());
        // Clear the not-focusable, which let the dialog re-gain focus.
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    interface MenuOptionCallback {
        void onClick();
        String getTitle(String defaultTitle);
    }

    class MenuOption {
        private String title;
        private MenuOptionCallback callback;

        MenuOption(String title, MenuOptionCallback callback) {
            this.title = title;
            this.callback = callback;
        }

        void onClick() {
            callback.onClick();
        }

        String getTitle() {
            return callback.getTitle(title);
        }
    }
}
