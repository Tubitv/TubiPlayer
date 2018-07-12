package com.tubitv.media.utilities;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.SeekBar;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.util.Util;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by stoyan on 5/23/17.
 */
public class Utils {
    private static String FORMAT_HOURS = "%d:%02d:%02d";
    private static String FORMAT_MINUTES = "%02d:%02d";
    private static StringBuilder formatBuilder = new StringBuilder();
    private static Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

    public static String getProgressTime(long timeMs, boolean remaining) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        String time = hours > 0 ? formatter.format(FORMAT_HOURS, hours, minutes, seconds).toString()
                : formatter.format(FORMAT_MINUTES, minutes, seconds).toString();
        return remaining && timeMs != 0 ? "-" + time : time;
    }

    public static long progressToMilli(long playerDurationMs, SeekBar seekBar) {
        long duration = playerDurationMs < 1 ? C.TIME_UNSET : playerDurationMs;
        return duration == C.TIME_UNSET ? 0 : ((duration * seekBar.getProgress()) / seekBar.getMax());
    }

    // This snippet hides the system bars.
    public static void hideSystemUI(@NonNull final Activity activity, final boolean immediate) {
        hideSystemUI(activity, immediate, 5000);
    }

    // This snippet hides the system bars.
    public static void hideSystemUI(@NonNull final Activity activity, final boolean immediate, final int delayMs) {
        View decorView = activity.getWindow().getDecorView();
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        int uiState = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        if (Util.SDK_INT > 18) {
            uiState |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        } else {
            final Handler handler = new Handler();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility == View.VISIBLE) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                hideSystemUI(activity, false);
                            }
                        };
                        if (immediate) {
                            handler.post(runnable);
                        } else {
                            handler.postDelayed(runnable, delayMs);
                        }
                    }
                }
            });
        }
        decorView.setSystemUiVisibility(uiState);
    }

    /**
     * Checks if a string is empty
     *
     * @param text String to check
     * @return True if string is null or ""
     */
    public static boolean isEmpty(@Nullable String text) {
        return text == null || text.equalsIgnoreCase("");
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
