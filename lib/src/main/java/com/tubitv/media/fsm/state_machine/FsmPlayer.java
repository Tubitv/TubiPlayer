package com.tubitv.media.fsm.state_machine;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.controller.PlayerComponentController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.RetrieveAdCallback;
import com.tubitv.media.fsm.concrete.MoviePlayingState;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 7/27/17.
 */
public abstract class FsmPlayer implements Fsm, RetrieveAdCallback {

    /**
     * a wrapper class for player UI related objects
     */
    private PlayerUIController controller;

    /**
     * a wrapper class for player logic related component objects.
     */
    protected PlayerComponentController playerComponentController;

    /**
     * a generic call ad network class
     */
    private AdInterface adServerInterface;

    /**
     * information to use when retrieve ad from server
     */
    private AdRetriever retriever;

    /**
     * the main content media
     */
    private MediaModel movieMedia;

    /**
     * the content of ad being playing
     */
    private AdMediaModel adMedia;

    /**
     * the central state representing {@link com.google.android.exoplayer2.ExoPlayer} state at any given time.
     */
    private State currentState = null;

    /**
     * a factory class to create different state when fsm change to a different state.
     */
    private StateFactory factory;

    /**
     * only initialize the fsmPlay onc
     */
    private boolean isInitialized = false;

    public FsmPlayer(StateFactory factory) {
        this.factory = factory;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setMovieMedia(MediaModel movieMedia) {
        this.movieMedia = movieMedia;
    }

    public void setAdMedia(AdMediaModel adMedia) {
        this.adMedia = adMedia;
    }

    public AdInterface getAdServerInterface() {
        return adServerInterface;
    }

    public AdRetriever getRetriever() {
        return retriever;
    }

    public boolean hasAdToPlay() {
        return adMedia != null && adMedia.getListOfAds() != null && adMedia.getListOfAds().size() > 0;
    }

    /**
     * delete the add at the first of the itme in list, which have been played already.
     */
    public void popPlayedAd() {
        if (adMedia != null) {
            adMedia.popFirstAd();
        }
    }

    public MediaModel getNextAdd() {
        return adMedia.nextAD();
    }

    public void setController(@NonNull PlayerUIController controller) {
        this.controller = controller;
    }

    public void setAdServerInterface(@NonNull AdInterface adServerInterface) {
        this.adServerInterface = adServerInterface;
    }

    public void setAdRetriever(@NonNull AdRetriever retriever) {
        this.retriever = retriever;
    }

    public void setPlayerComponentController(PlayerComponentController playerComponentController) {
        this.playerComponentController = playerComponentController;
    }

    public void updateCuePointForRetriever(long cuepoint) {
        retriever.setCubPoint(cuepoint);
    }

    @Override
    public State getCurrentState() {
        return currentState;
    }

    @Override
    public void transit(Input input) {

        State transitToState;

        if (currentState != null) {
            transitToState = currentState.transformToState(input, factory);
        } else {

            isInitialized = true;
            transitToState = factory.createState(initializeState());
        }

        if (transitToState != null) {
            /**
             * when transition is not null, state change is successful, and transit to a new state
             */
            currentState = transitToState;

        } else {

            Log.e(Constants.FSMPLAYER_TESTING, "Error happed"+ " jump to MoviePlayingState");

            updateMovieResumePostion(controller);
            /**
             * when transition is null, state change is error, transit to default {@link MoviePlayingState}
             */
            if (currentState instanceof MoviePlayingState) { // if player is current in moviePlayingstate when transition error happen, doesn't nothing.
                return;
            }

            currentState = factory.createState(MoviePlayingState.class);
        }

        currentState.performWorkAndupdatePlayerUI(this, controller, playerComponentController, movieMedia, adMedia);
    }


    @Override
    public void updateSelf() {
        if (currentState != null) {
            currentState.performWorkAndupdatePlayerUI(this, controller, playerComponentController, movieMedia, adMedia);
        }
    }

    @Override
    public void onReceiveAd(AdMediaModel mediaModels) {
        adMedia = mediaModels;
        // prepare and build the adMediaModel
        playerComponentController.getDoublePlayerInterface().onPrepareAds(adMedia);

        transit(Input.AD_RECEIVED);
    }

    @Override
    public void onError() {
        transit(Input.ERROR);
    }

    @Override
    public void onEmptyAdReceived() {
        transit(Input.EMPTY_AD);
    }

    /**
     * update the resume position of the main movice
     * @param controller
     */
    public static void updateMovieResumePostion(PlayerUIController controller) {

        if (controller == null) {
            return;
        }

        SimpleExoPlayer moviePlayer = controller.getContentPlayer();

        if (moviePlayer != null) {
            if (moviePlayer != null && moviePlayer.getPlaybackState() != ExoPlayer.STATE_IDLE) {
                int resumeWindow = moviePlayer.getCurrentWindowIndex();
                long resumePosition = moviePlayer.isCurrentWindowSeekable() ? Math.max(0, moviePlayer.getCurrentPosition())
                        : C.TIME_UNSET;
                controller.setMovieResumeInfo(resumeWindow, resumePosition);
            }
        }
    }
}
