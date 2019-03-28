package com.tubitv.media.fsm.listener;

import android.support.annotation.NonNull;
import com.google.android.exoplayer2.Player;
import com.tubitv.media.fsm.state_machine.FsmAdController;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.utilities.EventLogger;

/**
 * Created by allensun on 8/9/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class MoviePlayingMonitor extends EventLogger {

    public FsmAdController fsmPlayer;

    public MoviePlayingMonitor(@NonNull FsmPlayer fsmPlayer) {
        super(null);
        this.fsmPlayer = fsmPlayer;
    }

    @Override
    public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
        super.onPlayerStateChanged(eventTime, playWhenReady, playbackState);

        //the movie has finished playback
        if (playbackState == Player.STATE_ENDED) {
            fsmPlayer.finishMovieAndTransitToNextState();
        }
    }
}
