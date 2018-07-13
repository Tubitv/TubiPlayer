package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.RetrieveAdCallback;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.utilities.ExoPlayerLogger;

import static com.tubitv.media.helpers.Constants.FSMPLAYER_TESTING;

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
    public void performWorkAndUpdatePlayerUI(@NonNull FsmPlayer fsmPlayer) {
        super.performWorkAndUpdatePlayerUI(fsmPlayer);

        // don't do any UI work.
        if (isNull(fsmPlayer)) {
            return;
        }

        //update the AdRetriever for pre_roll cue point, which is 0.
        if (controller.hasHistory()) {
            fsmPlayer.updateCuePointForRetriever(controller.getHistoryPosition());
        } else {
            fsmPlayer.updateCuePointForRetriever(0);
        }
        fetchAd(fsmPlayer.getAdServerInterface(), fsmPlayer.getAdRetriever(), fsmPlayer);
    }

    private void fetchAd(AdInterface adInterface, AdRetriever retriever, RetrieveAdCallback callback) {
        if (adInterface != null && retriever != null && callback != null) {
            adInterface.fetchAd(retriever, callback);
        } else {
            ExoPlayerLogger.e(FSMPLAYER_TESTING, "fetchAd fail, adInterface or AdRetriever is empty");
        }
    }
}
