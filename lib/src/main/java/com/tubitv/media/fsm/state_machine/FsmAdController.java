package com.tubitv.media.fsm.state_machine;

/**
 * Created by allensun on 9/15/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public interface FsmAdController {

    void removePlayedAdAndTransitToNextState();

    void adPlayerError();
}
