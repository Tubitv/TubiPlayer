package com.tubitv.media.fsm.concrete;

import com.google.android.exoplayer2.ExoPlayer;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;

/**
 * Created by allensun on 7/27/17.
 */
public class MoviePlayingState implements State {


    @Override
    public State transformToState(Input input, StateFactory factory) {

        State currentState = null;

        switch (input){
            case PREPARE_AD:
//                currentState = factory.createState(MovieToAdTransitionState.class);
                break;

            case AD_SHOW:
                currentState = factory.createState(AdPlayingState.class);
                break;

            case MOVIE_FINISH:
                break;
        }

        return currentState;
    }

    @Override
    public void updatePlayer(ExoPlayer contentPlayer, ExoPlayer adPlayer) {

    }





}
