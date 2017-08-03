package com.tubitv.media.fsm.state_machine;

import com.google.android.exoplayer2.ExoPlayer;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.MakingAdCallState;
import com.tubitv.media.fsm.concrete.MoviePlayingState;
import com.tubitv.media.fsm.concrete.factory.StateFactory;

/**
 * Created by allensun on 7/27/17.
 */
public class FsmPlayer implements Fsm {

    private ExoPlayer contentPlayer;

    private ExoPlayer adPlayer;

    private State currentState;

    private StateFactory factory;

    public FsmPlayer() {
        factory = new StateFactory();

        /**
         *  initializing state is to make a ad call
         */
        currentState = factory.createState(MakingAdCallState.class);
    }

    @Override
    public State getCurrentState() {
        return currentState;
    }

    @Override
    public void transit(Input input) {

        State transitToState = currentState.transformToState(input, factory);

        if (transitToState != null) {
            currentState = transitToState;

        } else {

            //if the current state is not support to handle the input, it means that somewhere, something went wrong, jump back to {@link MoviePlayingState}
            if (currentState instanceof MoviePlayingState) {
                // no need to do any thing.
                return;
            }

            currentState = factory.createState(MoviePlayingState.class);
        }

        // once the state transformation happened, then prepare the player to its current state.
        currentState.prepareWorkLoad();

        currentState.updatePlayer(contentPlayer,adPlayer);

    }

    @Override
    public void mainfestToState() {

    }

    @Override
    public void playerFinalize() {

    }
}
