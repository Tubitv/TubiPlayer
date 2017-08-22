package com.tubitv.media.fsm.listener;

import android.support.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.state_machine.FsmPlayer;

/**
 * Created by allensun on 8/9/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class AdPlayingMonitor implements ExoPlayer.EventListener {

    public FsmPlayer fsmPlayer;

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
            // need to remove the already played ad first.
            fsmPlayer.popPlayedAd();

            //then check if there are any ad need to be played.
            if (fsmPlayer.hasAdToPlay()) {

                if (fsmPlayer.getNextAdd().isVpaid()) {
                    fsmPlayer.transit(Input.VPAID_MANIFEST);
                } else {
                    fsmPlayer.transit(Input.NEXT_AD);
                }

            } else {
                fsmPlayer.updateCuePointAfterAdbeenPlayed();
                fsmPlayer.transit(Input.AD_FINISH);
            }

        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        fsmPlayer.transit(Input.ERROR);
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
