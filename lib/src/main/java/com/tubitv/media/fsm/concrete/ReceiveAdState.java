package com.tubitv.media.fsm.concrete;

import com.google.android.exoplayer2.ExoPlayer;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;

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
    public void updatePlayer(ExoPlayer contentPlayer, ExoPlayer adPlayer) {

    }
}
