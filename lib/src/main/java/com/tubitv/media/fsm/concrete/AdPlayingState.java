package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.views.TubiExoPlayerView;

/**
 * Created by allensun on 7/31/17.
 */
public class AdPlayingState extends BaseState {

    private static final String TAG = AdPlayingState.class.getSimpleName();

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
    public void performWorkAndupdatePlayerUI(@Nullable FsmPlayer fsmPlayer, @NonNull PlayerUIController controller, @NonNull PlayerComponentController componentController, @NonNull MediaModel movieMedia, @Nullable AdMediaModel adMedia) {
        Log.d(Constants.FSMPLAYER_TESTING, "update stat to: " + TAG);

        if(isNull(fsmPlayer,controller,componentController,movieMedia,adMedia)){
            return;
        }

        playingAdAndPauseMovie(controller, adMedia, componentController);
    }

    private void playingAdAndPauseMovie(PlayerUIController controller, AdMediaModel adMediaModel, PlayerComponentController componentController) {

        SimpleExoPlayer adPlayer = controller.getAdPlayer();
        SimpleExoPlayer moviePlayer = controller.getContentPlayer();

        // then setup the player for ad to playe
        MediaModel adMedia = adMediaModel.nextAD();
        if (adMedia != null) {
            // first need to pause the movie player, and also remember main movie playing position.
            updateMovieResumePostion(controller);
            moviePlayer.setPlayWhenReady(false);

            //prepare the mediaSource to AdPlayer
            adPlayer.prepare(adMedia.getMediaSource(), true, true);

            //update the ExoPlayerView with AdPlayer and AdMedia
            TubiExoPlayerView tubiExoPlayerView = (TubiExoPlayerView) controller.getExoPlayerView();
            tubiExoPlayerView.setPlayer(adPlayer, componentController.getTubiPlaybackInterface());
            tubiExoPlayerView.setMediaModel(adMedia, false);

            //Player the Ad.
            adPlayer.setPlayWhenReady(true);
            adPlayer.addListener(componentController.getAdPlayingMonitor());
        }
    }

    private void updateMovieResumePostion(PlayerUIController controller){
        SimpleExoPlayer moviePlayer = controller.getContentPlayer();

        if(moviePlayer!=null){
            if (moviePlayer != null && moviePlayer.getPlaybackState()!= ExoPlayer.STATE_IDLE) {
                int resumeWindow = moviePlayer.getCurrentWindowIndex();
                long resumePosition = moviePlayer.isCurrentWindowSeekable() ? Math.max(0, moviePlayer.getCurrentPosition())
                        : C.TIME_UNSET;
                controller.setMovieResumeInfo(resumeWindow, resumePosition);
            }
        }
    }


}
