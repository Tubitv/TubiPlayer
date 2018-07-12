package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.CuePointCallBack;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.fsm.state_machine.FsmPlayerImperial;
import com.tubitv.media.models.CuePointsRetriever;
import com.tubitv.media.utilities.ExoPlayerLogger;

import static com.tubitv.media.helpers.Constants.FSMPLAYER_TESTING;

/**
 * Created by allensun on 8/17/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class FetchCuePointState extends BaseState {

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
    public void performWorkAndUpdatePlayerUI(@NonNull FsmPlayer fsmPlayer) {
        super.performWorkAndUpdatePlayerUI(fsmPlayer);

        if (isNull(fsmPlayer)) {
            return;
        }
        //does nothing with the UI.
        fetchCuePointCall(fsmPlayer.getAdServerInterface(), fsmPlayer.getCuePointsRetriever(),
                (FsmPlayerImperial) fsmPlayer);
    }

    private void fetchCuePointCall(AdInterface adInterface, CuePointsRetriever retriever, CuePointCallBack callBack) {
        if (adInterface != null && retriever != null && callBack != null) {
            adInterface.fetchQuePoint(retriever, callBack);
        } else {
            ExoPlayerLogger.e(FSMPLAYER_TESTING, "fetch Ad fail, adInterface or CuePointsRetriever is empty");
        }
    }

}
