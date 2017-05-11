package com.tubitv.media;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.trackselection.TrackSelector;

import static com.google.android.exoplayer2.ExoPlayerFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS;

/**
 * Created by stoyan tubi_tv_quality_on 3/22/17.
 */
public class TubiExoPlayer extends SimpleExoPlayer {
    protected TubiExoPlayer(Context context, TrackSelector trackSelector, LoadControl loadControl, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode, long allowedVideoJoiningTimeMs) {
        super(context, trackSelector, loadControl, drmSessionManager, extensionRendererMode, allowedVideoJoiningTimeMs);
    }

    public static TubiExoPlayer newInstance(@NonNull Context context, TrackSelector trackSelector, LoadControl loadControl) {
        return new TubiExoPlayer(context, trackSelector, loadControl, null,
                SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }
}
