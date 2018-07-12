package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;

/**
 * Created by allensun on 7/31/17.
 */
public class ReceiveAdState extends BaseState {

    @Override
    public State transformToState(Input input, StateFactory factory) {

        switch (input) {
            case SHOW_ADS:
                return factory.createState(AdPlayingState.class);
        }

        return null;
    }

    @Override
    public void performWorkAndUpdatePlayerUI(@NonNull FsmPlayer fsmPlayer) {
        super.performWorkAndUpdatePlayerUI(fsmPlayer);

        // doesn't need to do any UI work.
        if (isNull(fsmPlayer)) {
            return;
        }

        SimpleExoPlayer moviePlayer = controller.getContentPlayer();

        // this mean, user jump out of the activity lifecycle in ReceivedAdState.
        if (moviePlayer != null && moviePlayer.getPlaybackState() == ExoPlayer.STATE_IDLE) {
            fsmPlayer.transit(Input.ERROR);
        }

    }
}
