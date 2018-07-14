package com.tubitv.media.fsm.state_machine;

import android.support.annotation.NonNull;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;

/**
 * Created by allensun on 7/27/17.
 */
public interface Fsm {

    State getCurrentState();

    void transit(Input input);

    void updateSelf();

    /**
     * this is the beginning state of the fsm
     *
     * @return
     */
    @NonNull
    Class initializeState();

    void restart();

}
