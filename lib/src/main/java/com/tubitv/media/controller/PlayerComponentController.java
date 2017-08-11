package com.tubitv.media.controller;

import android.support.annotation.Nullable;

import com.tubitv.media.fsm.listener.AdPlayingMonitor;
import com.tubitv.media.interfaces.TubiPlaybackInterface;

/**
 * Created by allensun on 8/11/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class PlayerComponentController {

    private AdPlayingMonitor adPlayingMonitor;

    private TubiPlaybackInterface tubiPlaybackInterface;

    public PlayerComponentController(@Nullable AdPlayingMonitor adPlayingMonitor, @Nullable TubiPlaybackInterface tubiPlaybackInterface) {
        this.adPlayingMonitor = adPlayingMonitor;
        this.tubiPlaybackInterface = tubiPlaybackInterface;
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
