package com.tubitv.media.fsm.concrete;

import com.google.android.exoplayer2.ExoPlayer;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;

/**
 * Created by allensun on 7/31/17.
 */
public class MakingAdCallState extends BaseState {

    @Override
    public State transformToState(Input input, StateFactory factory) {
        switch (input){
            case AD_RECEIVED:
                return factory.createState(ReceiveAdState.class);

            case EMPTY_AD:
                return factory.createState(MoviePlayingState.class);

            case MAKE_AD_CALL:
                return factory.createState(MakingAdCallState.class);

            /***************below is the error handling******************************************/
            case SHOW_ADS:
                // ad server hasn't return any ad, can not show ad, this round of ad showing opportunity is over.
                return factory.createState(MoviePlayingState.class);

        }
        return null;
    }

    @Override
    public void updatePlayer(ExoPlayer contentPlayer, ExoPlayer adPlayer) {



    }
}
