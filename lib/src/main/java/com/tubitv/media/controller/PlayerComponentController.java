package com.tubitv.media.controller;

import android.support.annotation.Nullable;

import com.tubitv.media.fsm.listener.AdPlayingMonitor;
import com.tubitv.media.interfaces.DoublePlayerInterface;
import com.tubitv.media.interfaces.TubiPlaybackInterface;

/**
 * Created by allensun on 8/11/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class PlayerComponentController {

    private AdPlayingMonitor adPlayingMonitor;

    private TubiPlaybackInterface tubiPlaybackInterface;

    private DoublePlayerInterface doublePlayerInterface;

    public PlayerComponentController(@Nullable AdPlayingMonitor adPlayingMonitor,@Nullable TubiPlaybackInterface tubiPlaybackInterface,@Nullable DoublePlayerInterface doublePlayerInterface) {
        this.adPlayingMonitor = adPlayingMonitor;
        this.tubiPlaybackInterface = tubiPlaybackInterface;
        this.doublePlayerInterface = doublePlayerInterface;
    }

    public DoublePlayerInterface getDoublePlayerInterface() {
        return doublePlayerInterface;
    }

    public void setDoublePlayerInterface(DoublePlayerInterface doublePlayerInterface) {
        this.doublePlayerInterface = doublePlayerInterface;
    }

    public void setAdPlayingMonitor(AdPlayingMonitor adPlayingMonitor) {
        this.adPlayingMonitor = adPlayingMonitor;
    }

    public void setTubiPlaybackInterface(TubiPlaybackInterface tubiPlaybackInterface) {
        this.tubiPlaybackInterface = tubiPlaybackInterface;
    }

    public AdPlayingMonitor getAdPlayingMonitor() {
        return adPlayingMonitor;
    }

    public TubiPlaybackInterface getTubiPlaybackInterface() {
        return tubiPlaybackInterface;
    }
}
