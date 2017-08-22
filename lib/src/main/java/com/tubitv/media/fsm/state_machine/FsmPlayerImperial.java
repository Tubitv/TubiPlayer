package com.tubitv.media.fsm.state_machine;

import android.util.Log;

import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.callback.CuePointCallBack;
import com.tubitv.media.fsm.concrete.factory.StateFactory;

import java.util.Arrays;

/**
 * Created by allensun on 8/17/17.
 * on Tubitv.com, allengotstuff@gmail.com
 * <p>
 * This fsmPlayer implements the {@link CuePointCallBack} logic.
 */
public abstract class FsmPlayerImperial extends FsmPlayer implements CuePointCallBack {

    private static final String TAG = FsmPlayerImperial.class.getSimpleName();

    public FsmPlayerImperial(StateFactory factory) {
        super(factory);
    }

    @Override
    public void onCuePointReceived(long[] quePoints) {


        //if it has pre-roll in the list of cue points, remove the pre-roll cue point,
        // because the pre-roll should not managed by the CuePointMonitor
        if (hasPrerollAd(quePoints)) {

            updateCuePointsWithRemoveFirstCue(quePoints, true);
            transit(Input.HAS_PREROLL_AD);
        } else {

            updateCuePointsWithRemoveFirstCue(quePoints, false);
            transit(Input.NO_PREROLL_AD);
        }
    }


    @Override
    public void onCuePointError() {
    }


    private void updateCuePointsWithRemoveFirstCue(long[] array, boolean yes) {

        if (playerComponentController == null || playerComponentController.getCuePointMonitor() == null) {

            Log.e(TAG, " playerComponentController || playerComponentController is empty");
            return;
        }

        if (yes) {
            // update the cuePointMonitor with the first cue remove, because it is a pre-roll ad
            playerComponentController.getCuePointMonitor().setQuePoints(removePreroll(array));

        } else {
            // update the cuePointMonitor
            playerComponentController.getCuePointMonitor().setQuePoints(array);
        }
    }

    private boolean hasPrerollAd(long[] cuePoints) {
        if (cuePoints != null && cuePoints.length > 0 && cuePoints[0] == 0) {
            //TODO: need to remove the time 0 cuePoint indicator.
            return true;
        }
        return false;
    }

    private long[] removePreroll(long[] array) {
        if (array.length <= 1) {
            return array;
        }

        return Arrays.copyOfRange(array, 1, array.length);
    }
}
