package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.bindings.UserController;
import com.tubitv.media.controller.PlayerAdLogicController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.models.VpaidClient;
import com.tubitv.media.player.PlayerContainer;
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
    public void performWorkAndUpdatePlayerUI(@NonNull FsmPlayer fsmPlayer) {
        super.performWorkAndUpdatePlayerUI(fsmPlayer);

        if (isNull(fsmPlayer)) {
            return;
        }

        stopAdandPlayerMovie(controller, componentController, movieMedia, fsmPlayer.isComingFromAdsState());
    }

    private void stopAdandPlayerMovie(PlayerUIController controller, PlayerAdLogicController componentController,
            MediaModel movieMedia, boolean wasAdsPlaying) {

        if (wasAdsPlaying) {
            //first pause the player
            PlayerContainer.getPlayer().setPlayWhenReady(false);

            PlayerContainer.releasePlayer();
            PlayerContainer.preparePlayer(movieMedia);
        }

        //then update the playerView with SimpleExoPlayer and Movie MediaModel
        TubiExoPlayerView tubiExoPlayerView = (TubiExoPlayerView) controller.getExoPlayerView();
        tubiExoPlayerView.setPlayer(PlayerContainer.getPlayer(), componentController.getTubiPlaybackInterface());
        tubiExoPlayerView.setMediaModel(movieMedia);

        updatePlayerPosition(PlayerContainer.getPlayer(), controller);

        // Add tracker for end of video
        PlayerContainer.getPlayer().addAnalyticsListener(componentController.getMoviePlayingMonitor());
        // Remove state tracking for movie player
        PlayerContainer.getPlayer().setPlayWhenReady(true);

        hideVpaidNShowPlayer(controller);

        //when return to the movie playing state, show the subtitle if necessary
        if (shouldShowSubtitle()) {
            ((TubiExoPlayerView) controller.getExoPlayerView()).getSubtitleView().setVisibility(View.VISIBLE);
        }
    }

    private void updatePlayerPosition(SimpleExoPlayer moviePlayer, PlayerUIController controller) {

        // if want to play movie from certain position when first open the movie
        if (controller.hasHistory()) {
            moviePlayer.seekTo(moviePlayer.getCurrentWindowIndex(), controller.getHistoryPosition());
            controller.clearHistoryRecord();
            return;
        }

        boolean haveResumePosition = controller.getMovieResumePosition() != C.TIME_UNSET;
        if (haveResumePosition) {
            moviePlayer.seekTo(moviePlayer.getCurrentWindowIndex(), controller.getMovieResumePosition());
        }
    }

    private void hideVpaidNShowPlayer(final PlayerUIController controller) {

        controller.getExoPlayerView().setVisibility(View.VISIBLE);

        WebView vpaidEWebView = controller.getVpaidWebView();
        if (vpaidEWebView != null) {
            vpaidEWebView.setVisibility(View.GONE);
            vpaidEWebView.loadUrl(VpaidClient.EMPTY_URL);
            vpaidEWebView.clearHistory();
        }
    }

    private boolean shouldShowSubtitle() {

        TubiExoPlayerView view = (TubiExoPlayerView) controller.getExoPlayerView();

        UserController controller = (UserController) view.getPlayerController();

        if (controller.videoHasSubtitle.get() && controller.isSubtitleEnabled.get()) {
            return true;
        }

        return false;
    }

}
