package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tubitv.media.controller.PlayerComponentController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 8/18/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class MakingPrerollAdCallState extends BaseState {

    @Nullable
    @Override
    public State transformToState(@NonNull Input input, @NonNull StateFactory factory) {

        switch (input) {
            case PRE_ROLL_AD_RECEIVED:
                return factory.createState(AdPlayingState.class);
        }

        return null;
    }

    @Override
    public void performWorkAndupdatePlayerUI(@Nullable FsmPlayer fsmPlayer, @NonNull PlayerUIController controller, @NonNull PlayerComponentController componentController, @NonNull MediaModel movieMedia, @Nullable AdMediaModel adMedia) {
        // don't do any UI work.
    }
}
