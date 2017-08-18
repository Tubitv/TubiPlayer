package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tubitv.media.controller.PlayerComponentController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.RetrieveAdCallback;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 8/18/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class MakingPrerollAdCallState extends BaseState {

    private static final String TAG = MakingPrerollAdCallState.class.getSimpleName();

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
        Log.d(Constants.FSMPLAYER_TESTING, "update stat to: " + TAG);

        if (isNull(fsmPlayer, controller, componentController, movieMedia, adMedia)) {
            return;
        }

        //update the AdRetriever for pre_roll cue point, which is 0.
        fsmPlayer.updateCuePointForRetriever(0);
        fetchAd(fsmPlayer.getAdServerInterface(), fsmPlayer.getRetriever(), fsmPlayer);
    }


    private void fetchAd(AdInterface adInterface, AdRetriever retriever, RetrieveAdCallback callback) {
        if (adInterface != null && retriever != null && callback != null) {
            adInterface.fetchAd(retriever, callback);
        } else {
            Log.e(TAG, "fetchAd fail, adInterface or retreiever is empty");
        }
    }
}
