package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 7/31/17.
 */
public class VastAdInteractionSandBoxState extends BaseState {

    @Override
    public State transformToState(Input input, StateFactory factory) {

        switch (input){
            case BACK_TO_PLAYER_FROM_VAST_AD:
                return factory.createState(AdPlayingState.class);

        }
        return null;
    }

    @Override
    public void updatePlayerUI(@NonNull PlayerUIController controller, @NonNull MediaModel movieMedia, @Nullable MediaModel adMedia) {

    }
}
