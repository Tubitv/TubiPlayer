package com.tubitv.media.fsm.state_machine;

import android.support.annotation.NonNull;

import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.RetrieveAdCallback;
import com.tubitv.media.fsm.concrete.MakingAdCallState;
import com.tubitv.media.fsm.concrete.MoviePlayingState;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 7/27/17.
 */
public class FsmPlayer implements Fsm, RetrieveAdCallback {

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
    StateFactory factory;

    public FsmPlayer(StateFactory factory) {
        this.factory = factory;
    }

    public void setMovieMedia(MediaModel movieMedia) {
        this.movieMedia = movieMedia;
    }

    public void setAdMedia(AdMediaModel adMedia) {
        this.adMedia = adMedia;
    }

    public void setController(@NonNull PlayerUIController controller) {
        this.controller = controller;
    }

    public void setAdServerInterface(@NonNull AdInterface adServerInterface) {
        this.adServerInterface = adServerInterface;
    }

    public void updateAdRetriever(@NonNull AdRetriever retriever) {
        this.retriever = retriever;
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
            transitToState = factory.createState(MakingAdCallState.class);
        }

        if (transitToState != null) {
            /**
             * when transition is not null, state change is successful, and transit to a new state
             */
            currentState = transitToState;
            specialCaseHandle(currentState);
        } else {
            /**
             * when transition is null, state change is error, transit to default {@link MoviePlayingState}
             */
            if (currentState instanceof MoviePlayingState) { // if player is current in moviePlayingstate when transition error happen, doesn't nothing.
                return;
            }

            currentState = factory.createState(MoviePlayingState.class);
        }

        currentState.updatePlayerUI(controller, movieMedia, adMedia);
    }

    /**
     * some special state need to handle special operation when changing state,
     * When change to {@link MakingAdCallState}, the state need to handle fetch to nerwork.
     *
     * @param state the special sate that need to handle special operations.
     */
    public void specialCaseHandle(State state) {
        if (state instanceof MakingAdCallState) {
            ((MakingAdCallState) state).fetchAd(adServerInterface, retriever, this);
        }
    }

    @Override
    public void playerFinalize() {

    }

    @Override
    public void onReceiveAd(AdMediaModel mediaModels) {
        adMedia = mediaModels;
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
}
