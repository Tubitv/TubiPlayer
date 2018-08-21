package com.tubitv.media.fsm.state_machine;

import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.callback.CuePointCallBack;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.utilities.ExoPlayerLogger;
import java.util.Arrays;

/**
 * Created by allensun on 8/17/17.
 * on Tubitv.com, allengotstuff@gmail.com
 * This fsmPlayer implements the {@link CuePointCallBack} logic.
 */
public abstract class FsmPlayerImperial extends FsmPlayer implements CuePointCallBack {

    private static final String TAG = FsmPlayerImperial.class.getSimpleName();

    public FsmPlayerImperial(StateFactory factory) {
        super(factory);
    }

    @Override
    public void onCuePointReceived(long[] cuePoints) {

        ExoPlayerLogger.i(Constants.FSMPLAYER_TESTING, "CuePoint received");

        //if it has pre-roll in the list of cue points, remove the pre-roll cue point,
        // because the pre-roll should not managed by the CuePointMonitor
        if (hasPrerollAd(cuePoints)) {

            updateCuePointsWithRemoveFirstCue(cuePoints, true);
            transit(Input.HAS_PREROLL_AD);
        } else {

            updateCuePointsWithRemoveFirstCue(cuePoints, false);
            transit(Input.NO_PREROLL_AD);
        }
        /**
         *  update the cuepoint receive event to Activity {@link com.tubitv.media.activities.DoubleViewTubiPlayerActivity}
         *
         */

        playerComponentController.getTubiPlaybackInterface().onCuePointReceived(cuePoints);
    }

    @Override
    public void onCuePointError() {

        ExoPlayerLogger.e(Constants.FSMPLAYER_TESTING, "CuePoint fetch fail");
        //TODO: need to handle situation when cuepoint not able to retrieve.
        transit(Input.ERROR);
    }

    private void updateCuePointsWithRemoveFirstCue(long[] array, boolean yes) {

        if (playerComponentController == null || playerComponentController.getCuePointMonitor() == null) {

            ExoPlayerLogger.e(TAG, " playerComponentController || playerComponentController is empty");
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
        if (cuePoints != null && cuePoints.length > 0 && cuePoints[0] == 0l) {
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
