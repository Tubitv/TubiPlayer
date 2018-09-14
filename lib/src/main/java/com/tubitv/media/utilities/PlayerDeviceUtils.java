package com.tubitv.media.utilities;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import com.google.android.exoplayer2.util.Util;

import static android.content.Context.UI_MODE_SERVICE;

public class PlayerDeviceUtils {
    private static final String TAG = PlayerDeviceUtils.class.getSimpleName();
    private static final String XIAOMI_MANUFACTURER = "Xiaomi";
    private static final String MI_BOX_DEVICE = "once";
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

    /**
     * Check if we should use one player instance instead of two to handle video and ads playback
     * Single player instance will only use content player without initializing ads player
     * Single player instance will re-buffer every time when we switch between MediaSource
     */
    public static boolean useSinglePlayer() {
        // Turn it on for all TV devices
        if (sIsTVDevice) {
            return true;
        }

        // Use single player instance for Mi Box, since for 2.8.3 exoplayer it doesn't support more than one player instance
        // When we start second player instance and prepare, we got insufficiant resource error and player stuck
        if (XIAOMI_MANUFACTURER.equals(Util.MANUFACTURER) && MI_BOX_DEVICE.equals(Util.DEVICE)) {
            return true;
        }
        return false;
    }
}
