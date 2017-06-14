package com.tubitv.media;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.TrackSelector;

/**
 * Created by stoyan tubi_tv_quality_on 3/22/17.
 */
public class TubiExoPlayer extends SimpleExoPlayer {
//    protected TubiExoPlayer(Context context, TrackSelector trackSelector, LoadControl loadControl, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode, long allowedVideoJoiningTimeMs) {
//        super(context, trackSelector, loadControl, drmSessionManager, extensionRendererMode, allowedVideoJoiningTimeMs);
//    }

    protected TubiExoPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl) {
        super(renderersFactory, trackSelector, loadControl);
    }

    public static TubiExoPlayer newInstance(@NonNull Context context, TrackSelector trackSelector, LoadControl loadControl) {
        return new TubiExoPlayer(new DefaultRenderersFactory(context), trackSelector, loadControl);
    }
}
