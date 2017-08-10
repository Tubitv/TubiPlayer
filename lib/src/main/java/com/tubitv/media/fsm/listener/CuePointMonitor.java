package com.tubitv.media.fsm.listener;

import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.state_machine.FsmPlayer;

/**
 * Created by allensun on 8/7/17.
 * on Tubitv.com, allengotstuff@gmail.com
 * <p>
 * This class monitors the ExoPlayer frame by frame playing progress, and handles {@link com.tubitv.media.fsm.state_machine.FsmPlayer} and its two {@link com.tubitv.media.fsm.Input}
 * <p>
 * 1. somme abstract two start making the network call to the server relative to cue point
 * <p>
 * 2. when at the cue point, show ads.
 */
public abstract class CuePointMonitor {

    private FsmPlayer fsmPlayer;

    /**
     * this is the safe check to only call ad work one time given that the range check can make multiple add call for one cuepoint.
     */
    private boolean safeCheckForAdcall = true;

    private boolean safeCheckForCue = true;

    private int[] cuePoints;

    private int[] adCallPoints;

    /**
     * keep track of current relevant cue point this time,
     */
    private int currentQueuePointPos = -1;

    public abstract int networkingAhead();

    public CuePointMonitor(FsmPlayer fsmPlayer) {
        this.fsmPlayer = fsmPlayer;
    }

    public void setQuePoints(int[] cuePoints) {
        this.cuePoints = cuePoints;
        adCallPoints = getAddCallPoints(cuePoints);
    }

    /**
     * this method will update frame by frame on movie millisecond to check if any action can be triggered
     *
     * @param milliseconds
     * @param durationMillis
     */
    public void onMovieProgress(long milliseconds, long durationMillis) {

        //check if need to request ad call, if does, update the fsmPlayer, Request_AD.
        preformAdCallIfNecessary(milliseconds);

        //check if need to show ad, id does, update the fsmPlayer to Show_AD
        preformShowAdIfNecessary(milliseconds);
    }

    private void preformShowAdIfNecessary(long milliseconds) {
        if (isProgressActionable(cuePoints, milliseconds) && safeCheckForCue) {
            safeCheckForCue = false;
            fsmPlayer.transit(Input.SHOW_ADS);
            return;
        } else {
            safeCheckForCue = true;
        }
    }

    private void preformAdCallIfNecessary(long milliseconds) {
        if (isProgressActionable(adCallPoints, milliseconds) && safeCheckForAdcall) {
            safeCheckForAdcall = false;

            //update the cuepoint;
            if (currentQueuePointPos >= 0) {
                long currentQueuePoint = cuePoints[currentQueuePointPos];

                // update the cue point infor to AdRetriever and FsmPlayer status.
                fsmPlayer.updateCuePointForRetriever(currentQueuePoint);
                fsmPlayer.transit(Input.MAKE_AD_CALL);
                return;
            }

        } else {
            safeCheckForAdcall = true;
        }
    }

    /**
     * check if current progress millisecond is capable of trigger action
     *
     * @param array           array of relevant check points.
     * @param currentProgress current milliSecond of movie
     * @return is current millisecond need to trigger action.
     */
    private boolean isProgressActionable(int[] array, long currentProgress) {
        if (array == null || array.length <= 0) {
            currentQueuePointPos = -1;
            safeCheckForAdcall = true;
            return false;
        }

        int resultPos = binarySerchWithRange(array, (int) currentProgress);

        if (resultPos < 0) {
            currentQueuePointPos = -1;
            safeCheckForAdcall = true;
            return false;
        }

        currentQueuePointPos = resultPos;
        return true;
    }


    private static final int RANGE_FACTOR = 0 ;

    public static int binarySerchWithRange(int[] a, int key) {
        return binarySearchWithRange(a, 0, a.length, key, RANGE_FACTOR);
    }

    // Like public version, but without range checks.
    private static int binarySearchWithRange(int[] a, int fromIndex, int toIndex,
                                             int key, int range_factor) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal + range_factor < key)
                low = mid + 1;
            else if (midVal - range_factor > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * create add call points array base on queuePoints
     *
     * @param cuePoints the cuePoints of the movie
     * @return ad call points
     */
    private int[] getAddCallPoints(int[] cuePoints) {
        int array[] = new int[cuePoints.length];
        for (int i = 0; i < cuePoints.length; i++) {
            array[i] = cuePoints[i] * networkingAhead();
        }//end for
        return array;
    }


}
