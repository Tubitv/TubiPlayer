package com.tubitv.media.fsm.state_machine;

import android.arch.lifecycle.Lifecycle;
import android.support.annotation.NonNull;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.controller.PlayerAdLogicController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.RetrieveAdCallback;
import com.tubitv.media.fsm.concrete.MakingAdCallState;
import com.tubitv.media.fsm.concrete.MakingPrerollAdCallState;
import com.tubitv.media.fsm.concrete.MoviePlayingState;
import com.tubitv.media.fsm.concrete.VpaidState;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.CuePointsRetriever;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.ExoPlayerLogger;
import com.tubitv.media.utilities.PlayerDeviceUtils;

/**
 * Created by allensun on 7/27/17.
 */
public abstract class FsmPlayer implements Fsm, RetrieveAdCallback, FsmAdController {

    /**
     * a wrapper class for player logic related component objects.
     */
    protected PlayerAdLogicController playerComponentController;
    /**
     * a wrapper class for player UI related objects
     */
    private PlayerUIController controller;
    /**
     * a generic call ad network class
     */
    private AdInterface adServerInterface;

    /**
     * information to use when retrieve ad from server
     */
    private AdRetriever adRetriever;

    /**
     * information to use when retrieve cuePoint from server.
     */
    private CuePointsRetriever cuePointsRetriever;

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

    //this is a example url, you should use your own vpaid url instead.
    private String VPAID_END_POINT = "http://tubitv.com/";

    /**
     * only initialize the fsmPlay onc
     */
    private boolean isInitialized = false;

    private Lifecycle mLifecycle;

    public FsmPlayer(StateFactory factory) {
        this.factory = factory;
    }

    /**
     * update the resume position of the main video
     *
     * @param controller
     */
    public static void updateMovieResumePosition(PlayerUIController controller) {

        if (controller == null) {
            return;
        }

        SimpleExoPlayer moviePlayer = controller.getContentPlayer();

        if (moviePlayer != null && moviePlayer.getPlaybackState() != Player.STATE_IDLE) {
            int resumeWindow = moviePlayer.getCurrentWindowIndex();
            long resumePosition = moviePlayer.isCurrentWindowSeekable() ? Math.max(0, moviePlayer.getCurrentPosition())
                    : C.TIME_UNSET;
            controller.setMovieResumeInfo(resumeWindow, resumePosition);

            ExoPlayerLogger.i(Constants.FSMPLAYER_TESTING, resumePosition + "");
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public MediaModel getMovieMedia() {
        return movieMedia;
    }

    public void setMovieMedia(MediaModel movieMedia) {
        this.movieMedia = movieMedia;
    }

    public AdMediaModel getAdMedia() {
        return adMedia;
    }

    public void setAdMedia(AdMediaModel adMedia) {
        this.adMedia = adMedia;
    }

    public AdInterface getAdServerInterface() {
        return adServerInterface;
    }

    public void setAdServerInterface(@NonNull AdInterface adServerInterface) {
        this.adServerInterface = adServerInterface;
    }

    public AdRetriever getAdRetriever() {
        return adRetriever;
    }

    public void setAdRetriever(@NonNull AdRetriever adRetriever) {
        this.adRetriever = adRetriever;
    }

    public Lifecycle getLifecycle() {
        return mLifecycle;
    }

    public void setLifecycle(Lifecycle mLifecycle) {
        this.mLifecycle = mLifecycle;
    }

    public boolean hasAdToPlay() {
        return adMedia != null && adMedia.getListOfAds() != null && adMedia.getListOfAds().size() > 0;
    }

    public String getVPAID_END_POINT() {
        return VPAID_END_POINT;
    }

    public void setVPAID_END_POINT(String VPAID_END_POINT) {
        this.VPAID_END_POINT = VPAID_END_POINT;
    }

    /**
     * delete the add at the first of the itme in list, which have been played already.
     */
    private void popPlayedAd() {
        if (adMedia != null) {
            adMedia.popFirstAd();
        }
    }

    public MediaModel getNextAdd() {
        return adMedia.nextAD();
    }

    public PlayerUIController getController() {
        return controller;
    }

    public void setController(@NonNull PlayerUIController controller) {
        this.controller = controller;
    }

    public PlayerAdLogicController getPlayerComponentController() {
        return playerComponentController;
    }

    public void setPlayerComponentController(PlayerAdLogicController playerComponentController) {
        this.playerComponentController = playerComponentController;
    }

    public CuePointsRetriever getCuePointsRetriever() {
        return cuePointsRetriever;
    }

    public void setCuePointsRetriever(CuePointsRetriever cuePointsRetriever) {
        this.cuePointsRetriever = cuePointsRetriever;
    }

    public void updateCuePointForRetriever(long cuepoint) {
        if (adRetriever != null) {
            adRetriever.setCubPoint(cuepoint);
        }
    }

    @Override
    public State getCurrentState() {
        return currentState;
    }

    @Override
    public void restart() {
        getController().getContentPlayer().stop();
        getController().getContentPlayer().setPlayWhenReady(false);
        currentState = null;
        getController().clearMovieResumeInfo();

        getController().getContentPlayer().prepare(movieMedia.getMediaSource(), true, true);
        transit(Input.INITIALIZE);
    }

    @Override
    public void transit(Input input) {

        // if the current lifecycle of activity is after on_stop, omit the transition
        if (getLifecycle() != null) {
            if (!getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                ExoPlayerLogger.e(Constants.FSMPLAYER_TESTING, "Activity out of lifecycle");
                return;
            }
        }

        State transitToState;

        /**
         * make state transition.
         */
        if (currentState != null) {
            transitToState = currentState.transformToState(input, factory);
        } else {

            isInitialized = true;
            transitToState = factory.createState(initializeState());

            ExoPlayerLogger.i(Constants.FSMPLAYER_TESTING, "initialize fsmPlayer");
        }

        /**
         * check if the transition flow is correct, if not then handle the error case.
         */
        if (transitToState != null) {
            /**
             * when transition is not null, state change is successful, and transit to a new state
             */
            currentState = transitToState;

        } else {

            /**
             * when transition is null, state change is error, transit to default {@link MoviePlayingState}
             */
            if (currentState instanceof MoviePlayingState) { // if player is current in moviePlayingstate when transition error happen, doesn't nothing.
                ExoPlayerLogger.e(Constants.FSMPLAYER_TESTING, "FSM flow error: remain in MoviePlayingState");
                return;
            }

            ExoPlayerLogger
                    .e(Constants.FSMPLAYER_TESTING, "FSM flow error:" + "prepare transition to MoviePlayingState");
            currentState = factory.createState(MoviePlayingState.class);
        }

        if (controller != null) {
            if (!PlayerDeviceUtils.useSinglePlayer() || !controller.isPlayingAds) {
                updateMovieResumePosition(controller);
            }
        }

        ExoPlayerLogger.d(Constants.FSMPLAYER_TESTING, "transit to: " + currentState.getClass().getSimpleName());

        currentState.performWorkAndUpdatePlayerUI(this);

    }

    @Override
    public void removePlayedAdAndTransitToNextState() {

        // need to remove the already played ad first.
        popPlayedAd();

        //then check if there are any ad need to be played.
        if (hasAdToPlay()) {

            if (getNextAdd().isVpaid()) {
                transit(Input.VPAID_MANIFEST);
            } else {
                transit(Input.NEXT_AD);
            }

        } else {
            //depends on current state, to transit to MoviePlayingState.
            if (currentState != null && currentState instanceof VpaidState) {
                transit(Input.VPAID_FINISH);
            } else {
                transit(Input.AD_FINISH);
            }
        }

    }

    @Override
    public void adPlayerError() {
        transit(Input.ERROR);
    }

    @Override
    public void updateSelf() {
        if (currentState != null) {
            ExoPlayerLogger
                    .i(Constants.FSMPLAYER_TESTING,
                            "Fsm updates self : " + currentState.getClass().getSimpleName());
            currentState.performWorkAndUpdatePlayerUI(this);
        }
    }

    @Override
    public void onReceiveAd(AdMediaModel mediaModels) {
        ExoPlayerLogger.i(Constants.FSMPLAYER_TESTING, "AdBreak received");

        adMedia = mediaModels;
        // prepare and build the adMediaModel
        playerComponentController.getDoublePlayerInterface().onPrepareAds(adMedia);

        transitToStateBaseOnCurrentState(currentState);
    }

    @Override
    public void onError() {
        ExoPlayerLogger.w(Constants.FSMPLAYER_TESTING, "Fetch Ad fail");
        transit(Input.ERROR);
    }

    @Override
    public void onEmptyAdReceived() {
        ExoPlayerLogger.w(Constants.FSMPLAYER_TESTING, "Fetch ad succeed, but empty ad");
        transit(Input.EMPTY_AD);
    }

    /**
     * transit to different state, when current state is {@link MakingAdCallState}, then when Ad comes back from server, transit to AD_RECEIVED,which transit current state to {@link com.tubitv.media.fsm.concrete.ReceiveAdState}
     * if current state is {@link MakingPrerollAdCallState}, then when Ad comes back, transit to PRE_ROLL_AD_RECEIVED, which then transit state to {@link com.tubitv.media.fsm.concrete.AdPlayingState}.
     *
     * @param currentState
     */
    private void transitToStateBaseOnCurrentState(State currentState) {

        if (currentState == null) {
            return;
        }

        if (currentState instanceof MakingPrerollAdCallState) {
            transit(Input.PRE_ROLL_AD_RECEIVED);
        } else if (currentState instanceof MakingAdCallState) {
            transit(Input.AD_RECEIVED);
        }
    }
}
