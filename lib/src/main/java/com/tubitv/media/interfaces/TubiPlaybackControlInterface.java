package com.tubitv.media.interfaces;

import android.support.annotation.NonNull;

/**
 * Created by stoyan tubi_tv_quality_on 4/27/17.
 */
public interface TubiPlaybackControlInterface {

    void onSubtitlesToggle(boolean enabled);

    void onQualityTrackToggle();

    void cancelRunnable(@NonNull Runnable runnable);

    void postRunnable(@NonNull Runnable runnable, long millisDelay);

    void hideAfterTimeout();
}
