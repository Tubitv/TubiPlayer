package com.tubitv.media.fsm.listener;

import android.support.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.tubitv.media.fsm.state_machine.FsmAdController;
import com.tubitv.media.fsm.state_machine.FsmPlayer;

/**
 * Created by allensun on 8/9/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class AdPlayingMonitor implements ExoPlayer.EventListener {

    public FsmAdController fsmPlayer;

    public AdPlayingMonitor(@NonNull FsmPlayer fsmPlayer) {
        this.fsmPlayer = fsmPlayer;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        //the last ad has finish playing.
        if (playbackState == ExoPlayer.STATE_ENDED && playWhenReady == true) {
            fsmPlayer.removePlayedAdAndTransitToNextState();
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        fsmPlayer.removePlayedAdAndTransitToNextState();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
