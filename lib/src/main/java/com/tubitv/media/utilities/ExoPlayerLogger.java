package com.tubitv.media.utilities;

import android.util.Log;
import com.tubitv.media.BuildConfig;

/**
 * Created by allensun on 9/12/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class ExoPlayerLogger {

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            // do something for a debug build
            Log.d(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG) {
            // do something for a debug build
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG) {
            // do something for a debug build
            Log.w(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG) {
            // do something for a debug build
            Log.e(tag, message);
        }
    }

    public static void v(String tag, String message) {
        if (BuildConfig.DEBUG) {
            // do something for a debug build
            Log.v(tag, message);
        }
    }
}
