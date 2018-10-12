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
import com.tubitv.media.player.PlayerContainer;
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

        // then setup the player for ad to playe
        MediaModel adMedia = adMediaModel.nextAD();

        //TODO: Handle situation when ad medaia is empty, or invalid urls.
        if (adMedia != null) {

            if (adMedia.isVpaid()) {
                fsmPlayer.transit(Input.VPAID_MANIFEST);
                return;
            }

            hideVpaidNShowPlayer(controller);

            if (PlayerContainer.getPlayer() != null) {
                PlayerContainer.getPlayer().setPlayWhenReady(false);
            }

            PlayerContainer.releasePlayer();

            // TODO as current set up, we always clearAdResumeInfo when enter AdPlaying, so this is always false
            boolean haveResumePosition = controller.getAdResumePosition() != C.TIME_UNSET;

            //prepare the mediaSource to AdPlayer
            PlayerContainer.preparePlayer(adMedia, !haveResumePosition, true, true);

            SimpleExoPlayer player = PlayerContainer.getPlayer();

            if (player != null) {
                if (haveResumePosition) {
                    player.seekTo(player.getCurrentWindowIndex(), controller.getAdResumePosition());
                }

                //update the ExoPlayerView with AdPlayer and AdMedia
                TubiExoPlayerView tubiExoPlayerView = (TubiExoPlayerView) controller.getExoPlayerView();
                tubiExoPlayerView.setPlayer(player, componentController.getTubiPlaybackInterface());
                tubiExoPlayerView.setMediaModel(adMedia);
                //update the numbers of ad left to give user indicator
                tubiExoPlayerView.setAvailableAdLeft(adMediaModel.nubmerOfAd());

                //Player the Ad.
                player.setPlayWhenReady(true);
                //TODO move adds monitor to PlayerContainer
                player.addAnalyticsListener(componentController.getAdPlayingMonitor());
                player.setMetadataOutput(componentController.getAdPlayingMonitor());

                //hide the subtitle view when ad is playing
                tubiExoPlayerView.getSubtitleView().setVisibility(View.INVISIBLE);
            }
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
