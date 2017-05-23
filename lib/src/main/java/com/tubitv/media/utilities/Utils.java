package com.tubitv.media.utilities;

import com.google.android.exoplayer2.C;

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
}
