package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.controller.PlayerComponentController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.views.TubiExoPlayerView;

/**
 * Created by allensun on 7/27/17.
 */
public class MoviePlayingState extends BaseState {

    @Override
    public State transformToState(Input input, StateFactory factory) {

        switch (input) {
            case MAKE_AD_CALL:
                return factory.createState(MakingAdCallState.class);

            case MOVIE_FINISH:
                return factory.createState(FinishState.class);
        }

        return null;
    }

    @Override
    public void performWorkAndupdatePlayerUI(@Nullable FsmPlayer fsmPlayer, @NonNull PlayerUIController controller, @NonNull PlayerComponentController componentController, @NonNull MediaModel movieMedia, @Nullable AdMediaModel adMedia) {

        if (isNull(fsmPlayer, controller, componentController, movieMedia, adMedia)) {
            return;
        }

        stopAdandPlayerMovie(controller, componentController, movieMedia);
    }

    private void stopAdandPlayerMovie(PlayerUIController controller, PlayerComponentController componentController, MediaModel movieMedia) {

        SimpleExoPlayer adPlayer = controller.getAdPlayer();
        SimpleExoPlayer moviePlayer = controller.getContentPlayer();

        //first remove the AdPlayer's listener and pause the player
        adPlayer.removeListener(componentController.getAdPlayingMonitor());
        adPlayer.setPlayWhenReady(false);

        //then update the playerView with SimpleExoPlayer and Movie MediaModel
        TubiExoPlayerView tubiExoPlayerView = (TubiExoPlayerView) controller.getExoPlayerView();
        tubiExoPlayerView.setPlayer(moviePlayer, componentController.getTubiPlaybackInterface());
        tubiExoPlayerView.setMediaModel(movieMedia, false);

        //prepare the moviePlayer with data source and set it play

        boolean haveResumePosition = controller.getMovieResumePosition() != C.TIME_UNSET;
        if (haveResumePosition) {
            moviePlayer.seekTo(controller.getMovieResumeWindow(), controller.getMovieResumePosition());
        }

        if (moviePlayer.getPlaybackState() == ExoPlayer.STATE_IDLE) {
            moviePlayer.prepare(movieMedia.getMediaSource(), !haveResumePosition, false);
        }

        moviePlayer.setPlayWhenReady(true);

        hideVpaidNShowPlayer(controller);
    }

    private void hideVpaidNShowPlayer(final PlayerUIController controller) {

        controller.getExoPlayerView().setVisibility(View.VISIBLE);

        WebView vpaidEWebView = controller.getVpaidWebView();
        if (vpaidEWebView != null) {
            vpaidEWebView.setVisibility(View.GONE);
            vpaidEWebView.loadUrl("about:blank");
            vpaidEWebView.clearHistory();
        }
    }

}
