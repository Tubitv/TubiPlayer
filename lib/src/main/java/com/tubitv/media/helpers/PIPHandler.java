package com.tubitv.media.helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.util.Rational;
import com.tubitv.media.activities.TubiPlayerActivity;
import com.tubitv.media.interfaces.PIPActionCallback;
import java.util.ArrayList;

/**
 * Handle picture in picture actions
 */
public class PIPHandler {
    /**
     * Intent action for media controls from Picture-in-Picture mode.
     */
    public static final String ACTION_MEDIA_CONTROL = "media_control";

    /**
     * Intent extra for media controls from Picture-in-Picture mode.
     */
    public static final String EXTRA_CONTROL_TYPE = "control_type";

    /**
     * The request code for play action PendingIntent.
     */
    public static final int REQUEST_PLAY = 1;

    /**
     * The request code for pause action PendingIntent.
     */
    public static final int REQUEST_PAUSE = 2;

    /**
     * The intent extra value for play action.
     */
    public static final int CONTROL_TYPE_PLAY = 1;

    /**
     * The intent extra value for pause action.
     */
    public static final int CONTROL_TYPE_PAUSE = 2;

    private BroadcastReceiver mReceiver;
    private boolean mPIPEnable;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder = new PictureInPictureParams.Builder();

    private PIPHandler() {
    }

    public static PIPHandler getInstance() {
        return PIPHandlerHolder.instance;
    }

    private static class PIPHandlerHolder {
        private static final PIPHandler instance = new PIPHandler();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setUpPIPActionReceiver(PIPActionCallback actionCallback) {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                    return;
                }

                final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
                switch (controlType) {
                    case CONTROL_TYPE_PLAY:
                        actionCallback.triggerPlayOrPause(true);
                        break;
                    case CONTROL_TYPE_PAUSE:
                        actionCallback.triggerPlayOrPause(false);
                        break;
                }
            }
        };
    }

    public void registerReceiver(final TubiPlayerActivity activity) {
        activity.registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
    }

    public void unregisterReceiver(final TubiPlayerActivity activity) {
        activity.unregisterReceiver(mReceiver);
    }

    public boolean isPIPEnable() {
        return mPIPEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public void setPIPEnable(final boolean pIPEnable) {
        this.mPIPEnable = pIPEnable;
    }

    public void enterPIP(Activity tubiPlayerActivity, int numerator, int denominator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Rational rational = new Rational(numerator, denominator);
            mPictureInPictureParamsBuilder.setAspectRatio(rational).build();
            tubiPlayerActivity.enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updatePictureInPictureActions(Activity tubiPlayerActivity, @DrawableRes int iconId, String title,
            int controlType, int requestCode) {

        final ArrayList<RemoteAction> actions = new ArrayList<>();

        final PendingIntent intent =
                PendingIntent.getBroadcast(
                        tubiPlayerActivity,
                        requestCode,
                        new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
                        0);
        final Icon icon = Icon.createWithResource(tubiPlayerActivity, iconId);
        actions.add(new RemoteAction(icon, title, title, intent));

        mPictureInPictureParamsBuilder.setActions(actions);
        tubiPlayerActivity.setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
    }

}
