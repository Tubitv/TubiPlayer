package com.tubitv.media.utilities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.tubitv.media.interfaces.DoublePlayerInterface;

/**
 * Created by allensun on 7/26/17.
 */
public class AdVideoEventListener implements ExoPlayer.EventListener {

    private static final String TAG = AdVideoEventListener.class.getSimpleName();

    private DoublePlayerInterface doublePlayerActivity;

    public AdVideoEventListener(@NonNull DoublePlayerInterface doublePlayer) {
        doublePlayerActivity = doublePlayer;
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        Log.e(TAG,"onTimelineChanged");
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.e(TAG,"onTracksChanged");
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.e(TAG,"onLoadingChanged");
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.e(TAG,"onPlayerStateChanged");
        /**
         * only when three conditionals are met, then finish the ads, and continue with the main content viedo
         *
         * 1. the video ads has ended
         * 2. the activity is not empty
         * 3. user did not pause the ad video when ads video are finished
         */
        if (playbackState == ExoPlayer.STATE_ENDED && doublePlayerActivity != null && playWhenReady == true) {
            doublePlayerActivity.adShowFinish();
        }

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(TAG,"onPlayerError");
    }

    @Override
    public void onPositionDiscontinuity() {
        Log.e(TAG,"onPositionDiscontinuity");
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.e(TAG,"onPlaybackParametersChanged");
    }
}
