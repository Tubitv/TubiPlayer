package com.tubitv.demo;

import android.support.test.runner.AndroidJUnit4;

import com.tubitv.media.fsm.state_machine.Fsm;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.concrete.AdPlayingState;
import com.tubitv.media.fsm.concrete.FinishState;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.fsm.concrete.MakingAdCallState;
import com.tubitv.media.fsm.concrete.MoviePlayingState;
import com.tubitv.media.fsm.concrete.ReceiveAdState;
import com.tubitv.media.fsm.concrete.VastAdInteractionSandBoxState;
import com.tubitv.media.fsm.concrete.VpaidState;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

/**
 * Created by allensun on 8/1/17.
 */
@RunWith(AndroidJUnit4.class)
public class ExoPlayerFSMTest {

    Fsm playerFsm;


    @Test
    public void testFSMFlowWithVpaid() {

        playerFsm = new FsmPlayer();

        assertTrue(playerFsm.getCurrentState() instanceof MakingAdCallState);

        playerFsm.transit(Input.AD_RECEIVED);

        assertTrue(playerFsm.getCurrentState() instanceof ReceiveAdState);

        playerFsm.transit(Input.SHOW_ADS);

        assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

        playerFsm.transit(Input.NEXT_AD);

        assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

        playerFsm.transit(Input.AD_CLICK);

        assertTrue(playerFsm.getCurrentState() instanceof VastAdInteractionSandBoxState);

        playerFsm.transit(Input.BACK_TO_PLAYER_FROM_VAST_AD);

        assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

        playerFsm.transit(Input.VPAID_MANIFEST);

        assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

        playerFsm.transit(Input.BACK_TO_PLAYER_FROM_VPAID_AD);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        playerFsm.transit(Input.MAKE_AD_CALL);

        assertTrue(playerFsm.getCurrentState() instanceof MakingAdCallState);

        playerFsm.transit(Input.EMPTY_AD);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        playerFsm.transit(Input.MOVIE_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof FinishState);
    }

    @Test
    public void testFSMFlowWithNoVpaid() {

        playerFsm = new FsmPlayer();

        for (int i = 0; i < 10; i++) {

            assertTrue(playerFsm.getCurrentState() instanceof MakingAdCallState);

            playerFsm.transit(Input.AD_RECEIVED);

            assertTrue(playerFsm.getCurrentState() instanceof ReceiveAdState);

            playerFsm.transit(Input.SHOW_ADS);

            assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

            playerFsm.transit(Input.NEXT_AD);

            assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

            playerFsm.transit(Input.AD_CLICK);

            assertTrue(playerFsm.getCurrentState() instanceof VastAdInteractionSandBoxState);

            playerFsm.transit(Input.BACK_TO_PLAYER_FROM_VAST_AD);

            assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

            playerFsm.transit(Input.AD_CLICK);

            assertTrue(playerFsm.getCurrentState() instanceof VastAdInteractionSandBoxState);

            playerFsm.transit(Input.BACK_TO_PLAYER_FROM_VAST_AD);

            assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

            playerFsm.transit(Input.NEXT_AD);

            assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

            playerFsm.transit(Input.VPAID_MANIFEST);

            assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

            playerFsm.transit(Input.BACK_TO_PLAYER_FROM_VPAID_AD);

            assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

            playerFsm.transit(Input.MAKE_AD_CALL);

        }

        playerFsm.transit(Input.EMPTY_AD);

        playerFsm.transit(Input.MOVIE_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof FinishState);
    }

    @Test
    public void testErrorFlow(){
        testFSMFlowWithNoVpaid();

        playerFsm.transit(Input.AD_CLICK);
        playerFsm.transit(Input.BACK_TO_PLAYER_FROM_VAST_AD);
        playerFsm.transit(Input.AD_FINISH);
        playerFsm.transit(Input.AD_CLICK);


        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);
    }


}
