package com.tubitv.media.interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tubitv.media.models.MediaModel;

/**
 * Created by stoyan on 6/23/17.
 */

public interface TubiPlaybackInterface {

    void onProgress(@Nullable MediaModel mediaModel, long milliseconds, long durationMillis);

    void onSeek(@Nullable MediaModel mediaModel, long oldPositionMillis, long newPositionMillis);

    void onPlayToggle(@Nullable MediaModel mediaModel, boolean playing);

    void onLearnMoreClick(@NonNull MediaModel mediaModel);

    void onSubtitles(@Nullable MediaModel mediaModel, boolean enabled);

    void onQuality(@Nullable MediaModel mediaModel);

    void onCuePointReceived(long[] cuePoints);

    boolean isActive();
}
