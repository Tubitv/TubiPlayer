package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.controller.PlayerAdLogicController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.models.VpaidClient;
import com.tubitv.media.utilities.PlayerDeviceUtils;
import com.tubitv.media.views.TubiExoPlayerView;

/**
 * Created by allensun on 7/31/17.
 */
public class AdPlayingState extends BaseState {

    @Override
    public State transformToState(Input input, StateFactory factory) {

        switch (input) {

            case NEXT_AD:
                return factory.createState(AdPlayingState.class);

            case AD_CLICK:
                return factory.createState(VastAdInteractionSandBoxState.class);

            case AD_FINISH:
                return factory.createState(MoviePlayingState.class);

            case VPAID_MANIFEST:
                return factory.createState(VpaidState.class);

        }
        return null;
    }

    @Override
    public void performWorkAndUpdatePlayerUI(@NonNull FsmPlayer fsmPlayer) {
        super.performWorkAndUpdatePlayerUI(fsmPlayer);

        if (isNull(fsmPlayer)) {
            return;
        }

        //reset the ad player position everytime when a transition to AdPlaying occur
        controller.clearAdResumeInfo();

        playingAdAndPauseMovie(controller, adMedia, componentController, fsmPlayer);
    }

    private void playingAdAndPauseMovie(PlayerUIController controller, AdMediaModel adMediaModel,
            PlayerAdLogicController componentController, FsmPlayer fsmPlayer) {

        SimpleExoPlayer adPlayer = controller.getAdPlayer();
        SimpleExoPlayer moviePlayer = controller.getContentPlayer();

        // then setup the player for ad to playe
        MediaModel adMedia = adMediaModel.nextAD();

        //TODO: Handle situation when ad medaia is empty, or invalid urls.
        if (adMedia != null) {

            if (adMedia.isVpaid()) {
                fsmPlayer.transit(Input.VPAID_MANIFEST);
                return;
            }

            hideVpaidNShowPlayer(controller);

            moviePlayer.setPlayWhenReady(false);

            // We need save movie play position before play ads for single player instance case
            if (PlayerDeviceUtils.useSinglePlayer() && !controller.isPlayingAds) {
                long resumePosition = Math.max(0, moviePlayer.getCurrentPosition());
                controller.setMovieResumeInfo(moviePlayer.getCurrentWindowIndex(), resumePosition);
            }

            //prepare the moviePlayer with data source and set it play

            boolean haveResumePosition = controller.getAdResumePosition() != C.TIME_UNSET;

            //prepare the mediaSource to AdPlayer
            adPlayer.prepare(adMedia.getMediaSource(), !haveResumePosition, true);
            controller.isPlayingAds = true;

            if (haveResumePosition) {
                adPlayer.seekTo(adPlayer.getCurrentWindowIndex(), controller.getAdResumePosition());
            }

            //update the ExoPlayerView with AdPlayer and AdMedia
            TubiExoPlayerView tubiExoPlayerView = (TubiExoPlayerView) controller.getExoPlayerView();
            tubiExoPlayerView.setPlayer(adPlayer, componentController.getTubiPlaybackInterface());
            tubiExoPlayerView.setMediaModel(adMedia);
            //update the numbers of ad left to give user indicator
            tubiExoPlayerView.setAvailableAdLeft(adMediaModel.nubmerOfAd());

            //Player the Ad.
            adPlayer.setPlayWhenReady(true);
            adPlayer.addAnalyticsListener(componentController.getAdPlayingMonitor());
            adPlayer.setMetadataOutput(componentController.getAdPlayingMonitor());

            //hide the subtitle view when ad is playing
            ((TubiExoPlayerView) controller.getExoPlayerView()).getSubtitleView().setVisibility(View.INVISIBLE);
        }
    }

    private void hideVpaidNShowPlayer(final PlayerUIController imcontroller) {

        imcontroller.getExoPlayerView().setVisibility(View.VISIBLE);

        WebView vpaidEWebView = imcontroller.getVpaidWebView();
        if (vpaidEWebView != null) {
            vpaidEWebView.setVisibility(View.GONE);
            vpaidEWebView.loadUrl(VpaidClient.EMPTY_URL);
            vpaidEWebView.clearHistory();
        }
    }

}
