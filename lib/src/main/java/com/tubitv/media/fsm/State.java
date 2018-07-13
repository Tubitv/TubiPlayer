package com.tubitv.media.fsm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;

/**
 * Created by allensun on 7/27/17.
 * This is the state to describe ExoPlayer's state
 * The state should not hold any other reference, because the state can change a lot of time during video playing experiences,
 * so you can create the state class using {@link StateFactory}
 */
public interface State {

    /**
     * let the state to examine itself in a constant time line to detects any input that can change the state.
     */
    @Nullable
    State transformToState(@NonNull Input input, @NonNull StateFactory factory);

    /**
     * once the fsm changes states, update player's UI components.
     *
     * @param fsmPlayer the state machine itself that contains the UI and Business logic parts.
     */
    void performWorkAndUpdatePlayerUI(@NonNull FsmPlayer fsmPlayer);

}
