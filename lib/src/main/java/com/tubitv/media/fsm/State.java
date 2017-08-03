package com.tubitv.media.fsm;

import com.google.android.exoplayer2.ExoPlayer;
import com.tubitv.media.fsm.concrete.factory.StateFactory;

/**
 * Created by allensun on 7/27/17.
 * This is the state to describe ExoPlayer's state
 */
public interface State {

    /**
     * let the state to examine itself in a constant time line to detects any input that can change the state.
     */
    State transformToState(Input input, StateFactory factory);

    /**
     * let the current state to update two player's status.
     */
    void updatePlayer(ExoPlayer contentPlayer, ExoPlayer adPlayer);

    /**
     * prepare work load of current state.
     */
    void prepareWorkLoad();

}
