package com.tubitv.media.fsm;

/**
 * Created by allensun on 7/31/17.
 */
public abstract class BaseState implements State {

    /**
     * in every state for the Exoplayer can have out of network fail.
     */
   public void networkFail(){

   }

}
