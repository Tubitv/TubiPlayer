package com.tubitv.media.fsm;

/**
 * Created by allensun on 7/27/17.
 */
public interface Fsm {

    State getCurrentState();

    void transit(Input input);

    void mainfestToState();

    void playerFinalize();
}
