package com.tubitv.media.utilities;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

import static android.content.Context.UI_MODE_SERVICE;

public class PlayerDeviceUtils {
    private static final String TAG = PlayerDeviceUtils.class.getSimpleName();
    private static final String AMAZON_FEATURE_FIRE_TV = "amazon.hardware.fire_tv";
    private static Boolean sIsTVDevice = null;

    public static boolean isTVDevice(final Context context) {
        if (sIsTVDevice == null) {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
            sIsTVDevice = uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;

            if (!sIsTVDevice) { // We also check fire tv
                sIsTVDevice = context.getPackageManager().hasSystemFeature(AMAZON_FEATURE_FIRE_TV);
            }
        }
        return sIsTVDevice;
    }
}
