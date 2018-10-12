package com.tubitv.media.interfaces;

/**
 * Created by allensun on 7/24/17.
 * This is a strategy to use two ExoPlayer in layer, one is to show main content, the other one is to show video ad.
 */
public interface DoublePlayerInterface {
    
    void prepareFSM();
}
