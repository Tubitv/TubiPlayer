package com.tubitv.casting;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Helper class encapsulating methods for checking and updating google play services
 * <p>
 * Created by stoyan on 12/14/16.
 */
public class GoogleServicesHelper {
    private static final String TAG = GoogleServicesHelper.class.getSimpleName();

    /**
     * Checks if the google services and their version are available on the user's device. If there
     * is a resolvable reason, ie {@link ConnectionResult#SERVICE_VERSION_UPDATE_REQUIRED}, then
     * this method will show a dialog requesting the user to accept the resolution
     *
     * @param context context to use
     * @return True if they have google services version equal or greater than we require, false otherwise
     */
    public static boolean available(@NonNull Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Api services not available: status code: " + status);

            return false;
        }
        return true;
    }
}
