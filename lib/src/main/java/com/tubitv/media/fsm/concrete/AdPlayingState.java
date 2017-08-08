package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 7/31/17.
 */
public class AdPlayingState extends BaseState {


    @Override
    public State transformToState(Input input, StateFactory factory) {

        switch (input) {

            case NEXT_AD:
                return  factory.createState(AdPlayingState.class);

            case AD_CLICK:
                return factory.createState(VastAdInteractionSandBoxState.class);

            case AD_FINISH:
                return factory.createState(MoviePlayingState.class);

            case VPAID_MANIFEST:
                return factory.createState(VpaidState.class);

        }
        return null;
    }

    @Override
    public void updatePlayerUI(@NonNull PlayerUIController controller, @NonNull MediaModel movieMedia, @Nullable AdMediaModel adMedia) {

    }


}
