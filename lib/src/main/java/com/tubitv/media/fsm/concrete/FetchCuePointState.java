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
import com.tubitv.media.fsm.callback.CuePointCallBack;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.fsm.state_machine.FsmPlayerImperial;
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.CuePointsRetriever;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 8/17/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class FetchCuePointState extends BaseState {

    private static final String TAG = FetchCuePointState.class.getSimpleName();

    @Nullable
    @Override
    public State transformToState(@NonNull Input input, @NonNull StateFactory factory) {

        switch (input) {
            case HAS_PREROLL_AD:
                return factory.createState(MakingPrerollAdCallState.class);

            case NO_PREROLL_AD:
                return factory.createState(MoviePlayingState.class);
        }

        return null;
    }

    @Override
    public void performWorkAndupdatePlayerUI(@Nullable FsmPlayer fsmPlayer, @NonNull PlayerUIController controller, @NonNull PlayerComponentController componentController, @NonNull MediaModel movieMedia, @Nullable AdMediaModel adMedia) {
        Log.d(Constants.FSMPLAYER_TESTING, "update stat to: " + TAG);
        //does nothing with the UI.

        fetchCuePointCall(fsmPlayer.getAdServerInterface(), fsmPlayer.getCuePointsRetriever(), (FsmPlayerImperial) fsmPlayer);
    }

    private void fetchCuePointCall(AdInterface adInterface, CuePointsRetriever retriever, CuePointCallBack callBack) {
        if (adInterface != null && retriever != null && callBack != null) {
            adInterface.fetchQuePoint(retriever, callBack);
        } else {
            Log.e(TAG, "fetchAd fail, adInterface or retreiever is empty");
        }
    }

}
