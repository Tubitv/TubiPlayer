package com.tubitv.media.utilities;

public class SeekCalculator {
    private static final String TAG = SeekCalculator.class.getSimpleName();

    public static final int FORWARD_DIRECTION = 1;
    public static final int REWIND_DIRECTION = -1;
    public static final long FAST_SEEK_INTERVAL = 15 * 1000; // 15 secs
    public static final long SEEK_INTERVAL_SHORT = 8 * 1000; // 8s
    public static final long SEEK_INTERVAL_RERGUAR = 64 * 1000; // 64s
    public static final long SEEK_INTERVAL_LONG = 256 * 1000; // 256s

    private static final long FIRST_SPEED_INTERVAL = 1 * 1000; // 1s
    private static final long SECOND_SPEED_INTERVAL = 2 * 1000; // 2s
    private static final long THIRD_SPEED_INTERVAL = 6 * 1000; // 6s
    private static final int SEEK_FREQUENCY = 1000 / 4; // 4 seeks per second

    private static Long sLastUpdateTime = null;

    /**
     * Based on how long has passed, generate current seek rate
     * Seek frequency also affect the rate.
     * Positive rate is only returned every once a while according to SEEK_FREQUENCY
     *
     * @param startTime   custom seek start time
     * @param currentTime current local time
     * @return Current seek rate either 0 or positive number
     */
    public static long getSeekRate(final long startTime, final long currentTime) {

        if (sLastUpdateTime == null) {
            sLastUpdateTime = startTime;
        }

        final long intervalSinceLastUpdate = currentTime - sLastUpdateTime;

        if (intervalSinceLastUpdate < SEEK_FREQUENCY) {
            return 0;
        }

        sLastUpdateTime = currentTime;

        return getCurrentSeekRate(startTime, currentTime);
    }

    private static long getCurrentSeekRate(final long startTime, final long currentTime) {
        final long interval = currentTime - startTime;
        if (interval < FIRST_SPEED_INTERVAL) {
            return 0;
        } else if (interval < SECOND_SPEED_INTERVAL) {
            return SEEK_INTERVAL_SHORT;
        } else if (interval < THIRD_SPEED_INTERVAL) {
            return SEEK_INTERVAL_RERGUAR;
        } else {
            return SEEK_INTERVAL_LONG;
        }
    }
}
