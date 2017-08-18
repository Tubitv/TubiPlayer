package com.tubitv.media.fsm.state_machine;

import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.callback.CuePointCallBack;
import com.tubitv.media.fsm.concrete.factory.StateFactory;

/**
 * Created by allensun on 8/17/17.
 * on Tubitv.com, allengotstuff@gmail.com
 * <p>
 * This fsmPlayer implements the {@link CuePointCallBack} logic.
 */
public abstract class FsmPlayerImperial extends FsmPlayer implements CuePointCallBack {

    public FsmPlayerImperial(StateFactory factory) {
        super(factory);
    }

    @Override
    public void onCuePointReceived(int[] quePoints) {

        //update the cuePointMonitor
        if (playerComponentController != null && playerComponentController.getCuePointMonitor() != null) {
            playerComponentController.getCuePointMonitor().setQuePoints(quePoints);
        }

        if (hasPrerollAd(quePoints)) {
            transit(Input.PRE_ROLL_AD_RECEIVED);
        } else {
            transit(Input.NO_PREROLL_AD);
        }
    }

    @Override
    public void onCuePointError() {
    }

    private boolean hasPrerollAd(int[] cuePoints) {
        if (cuePoints != null && cuePoints.length > 0 && cuePoints[0] == 0) {
            return true;
        }
        return false;
    }
}
