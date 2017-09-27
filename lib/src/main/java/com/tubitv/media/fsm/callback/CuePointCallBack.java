package com.tubitv.media.fsm.callback;

/**
 * Created by allensun on 8/17/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public interface CuePointCallBack {

    void onCuePointReceived(long[] quePoints);

    void onCuePointError();
}
