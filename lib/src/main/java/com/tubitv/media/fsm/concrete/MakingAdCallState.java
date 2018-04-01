package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tubitv.media.controller.PlayerComponentController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.RetrieveAdCallback;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.ExoPlayerLogger;

/**
 * Created by allensun on 7/31/17.
 */
public class MakingAdCallState extends BaseState {

    @Override
    public State transformToState(Input input, StateFactory factory) {
        switch (input) {
            case AD_RECEIVED:
                return factory.createState(ReceiveAdState.class);

            case EMPTY_AD:
                return null;

            case MAKE_AD_CALL:
                return factory.createState(MakingAdCallState.class);

            case PRE_ROLL_AD_RECEIVED:
                return factory.createState(AdPlayingState.class);
        }

        return null;
    }

    @Override
    public void performWorkAndupdatePlayerUI(@Nullable FsmPlayer fsmPlayer, @NonNull PlayerUIController controller, @NonNull PlayerComponentController componentController, @NonNull MediaModel movieMedia, @Nullable AdMediaModel adMedia) {

        if(isNull(fsmPlayer,controller,componentController,movieMedia,adMedia)){
            return;
        }

        fetchAd(fsmPlayer.getAdServerInterface(),fsmPlayer.getAdRetriever(),fsmPlayer);

        //in the MadingAdCallState, no UI need to be updated.

    }

    private void fetchAd(AdInterface adInterface, AdRetriever retriever, RetrieveAdCallback callback) {
        if (adInterface != null && retriever != null && callback!=null) {
            adInterface.fetchAd(retriever,callback);
        } else {
            ExoPlayerLogger.e("TAG", "fetchAd fail, adInterface or AdRetriever is empty");
        }
    }
}
