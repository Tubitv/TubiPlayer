package com.tubitv.media.fsm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tubitv.media.controller.PlayerComponentController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.ExoPlayerLogger;

/**
 * Created by allensun on 7/31/17.
 */
public abstract class BaseState implements State {

    /**
     * in every state for the Exoplayer can have out of network fail.
     */
    public void networkFail() {

    }

    public boolean isNull(@Nullable FsmPlayer fsmPlayer, @NonNull PlayerUIController controller, @NonNull PlayerComponentController componentController, @NonNull MediaModel movieMedia, @Nullable AdMediaModel adMedia) {
        if (fsmPlayer == null || controller == null || componentController == null || movieMedia == null) {
            ExoPlayerLogger.e(Constants.FSMPLAYER_TESTING, "component is null");
            return true;
        }
        return false;
    }
}
