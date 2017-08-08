package com.tubitv.media.fsm.listener;

import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 8/7/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class AdVideoEventListener implements ExoPlayer.EventListener {

    private static final String TAG = AdVideoEventListener.class.getSimpleName();

    private FsmPlayer fsm;

    private AdMediaModel adMediaModel;

    public AdVideoEventListener(FsmPlayer fsmPlayer) {
        fsm = fsmPlayer;
    }

    public void setAdMediaModel(AdMediaModel adMediaModel) {
        this.adMediaModel = adMediaModel;
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

        if (playbackState == ExoPlayer.STATE_ENDED && fsm != null && playWhenReady == true) {

            MediaModel nextAd = adMediaModel.nextMedaiModel();

            if (nextAd == null) { // when all ads are finish playing
                fsm.transit(Input.AD_FINISH);

            } else if (nextAd.isAd() && !nextAd.isVpaid()){ // the next add is vast ads
                fsm.transit(Input.NEXT_AD);

            }else if (nextAd.isAd() && nextAd.isAd()){ // the next add is vpaid ads
                fsm.transit(Input.VPAID_MANIFEST);

            }else{
                Log.w(TAG, "unexpected ad mediaModel format");
            }

        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
