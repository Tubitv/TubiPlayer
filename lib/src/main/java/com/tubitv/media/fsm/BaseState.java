package com.tubitv.media.fsm;

import android.support.annotation.NonNull;
import com.tubitv.media.controller.PlayerAdLogicController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.ExoPlayerLogger;

/**
 * Created by allensun on 7/31/17.
 * Base class for {@link State}, preparation method to get the state ready for UI and business rule manipulation.
 */
public abstract class BaseState implements State {

    protected PlayerUIController controller;

    protected PlayerAdLogicController componentController;

    protected MediaModel movieMedia;

    protected AdMediaModel adMedia;

    /**
     * for testing purpose,
     *
     * @param fsmPlayer
     * @return
     */
    protected boolean isNull(@NonNull FsmPlayer fsmPlayer) {
        if (fsmPlayer == null) {
            throw new IllegalStateException("FsmPlayer can not be null");
        }

        if (controller == null || componentController == null || movieMedia == null) {
            ExoPlayerLogger.e(Constants.FSMPLAYER_TESTING, "components are null");
            return true;
        }

        return false;
    }

    @Override
    public void performWorkAndUpdatePlayerUI(@NonNull FsmPlayer fsmPlayer) {
        /**
         * need to get the reference of the UI and Business logic components first.
         */
        controller = fsmPlayer.getController();
        componentController = fsmPlayer.getPlayerComponentController();
        movieMedia = fsmPlayer.getMovieMedia();
        adMedia = fsmPlayer.getAdMedia();
    }
}
