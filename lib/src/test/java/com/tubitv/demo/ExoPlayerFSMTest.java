package com.tubitv.demo;

import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.di.PlayerModuleDefault;
import com.tubitv.media.di.component.DaggerFsmComonent;
import com.tubitv.media.di.component.FsmComonent;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.concrete.AdPlayingState;
import com.tubitv.media.fsm.concrete.FetchCuePointState;
import com.tubitv.media.fsm.concrete.FinishState;
import com.tubitv.media.fsm.concrete.MakingAdCallState;
import com.tubitv.media.fsm.concrete.MakingPrerollAdCallState;
import com.tubitv.media.fsm.concrete.MoviePlayingState;
import com.tubitv.media.fsm.concrete.ReceiveAdState;
import com.tubitv.media.fsm.concrete.VastAdInteractionSandBoxState;
import com.tubitv.media.fsm.concrete.VpaidState;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.fsm.state_machine.FsmPlayerImperial;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.MediaModel;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import static junit.framework.Assert.assertTrue;

/**
 * Created by allensun on 8/1/17.
 */
@RunWith(JUnit4.class)
public class ExoPlayerFSMTest {

    FsmPlayer playerFsm;

    @Mock
    MediaModel movieMedia;

    @Mock
    MediaModel adMedia;

    @Mock
    AdRetriever retriever;

    @Mock
    AdInterface adServerInterface;

    @Mock
    PlayerUIController controller;

    @Inject
    StateFactory factory;

    FsmComonent comonent;

    @Before
    public void setup() {
        comonent = DaggerFsmComonent.builder().playerModuleDefault(new PlayerModuleDefault())
                .build();
    }

    @Test
    public void testFSMFlowWithVpaid_No_PreRollAd() {

        factory = comonent.getStateFactory();

        playerFsm = new FsmPlayerImperial(factory) {
            @Override
            public Class initializeState() {
                return FetchCuePointState.class;
            }
        };

        playerFsm.transit(Input.INITIALIZE);

        assertTrue(playerFsm.getCurrentState() instanceof FetchCuePointState);

        playerFsm.transit(Input.NO_PREROLL_AD);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        playerFsm.transit(Input.MAKE_AD_CALL);

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

        playerFsm.transit(Input.VPAID_MANIFEST);

        assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

        playerFsm.transit(Input.VPAID_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        playerFsm.transit(Input.MAKE_AD_CALL);

        assertTrue(playerFsm.getCurrentState() instanceof MakingAdCallState);

        playerFsm.transit(Input.EMPTY_AD);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        playerFsm.transit(Input.MOVIE_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof FinishState);
    }

    @Test
    public void testFSMFlowWithVpaid_With_PreRollAd() {

        factory = comonent.getStateFactory();

        playerFsm = new FsmPlayerImperial(factory) {
            @Override
            public Class initializeState() {
                return FetchCuePointState.class;
            }
        };

        playerFsm.transit(Input.INITIALIZE);

        assertTrue(playerFsm.getCurrentState() instanceof FetchCuePointState);

        playerFsm.transit(Input.HAS_PREROLL_AD);

        assertTrue(playerFsm.getCurrentState() instanceof MakingPrerollAdCallState);

        playerFsm.transit(Input.PRE_ROLL_AD_RECEIVED);

        assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

        playerFsm.transit(Input.NEXT_AD);

        assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

        playerFsm.transit(Input.VPAID_MANIFEST);

        assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

        playerFsm.transit(Input.VPAID_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        playerFsm.transit(Input.MAKE_AD_CALL);

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

        playerFsm.transit(Input.VPAID_MANIFEST);

        assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

        playerFsm.transit(Input.VPAID_FINISH);

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
        factory = comonent.getStateFactory();

        playerFsm = new FsmPlayerImperial(factory) {
            @Override
            public Class initializeState() {
                return MoviePlayingState.class;
            }
        };

        playerFsm.transit(Input.INITIALIZE);

        for (int i = 0; i < 10; i++) {

            playerFsm.transit(Input.MAKE_AD_CALL);

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

            playerFsm.transit(Input.VPAID_FINISH);

            assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

            playerFsm.transit(Input.MAKE_AD_CALL);

        }

        playerFsm.transit(Input.EMPTY_AD);

        playerFsm.transit(Input.MOVIE_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof FinishState);
    }

    @Test
    public void testErrorFlow() {
        testFSMFlowWithNoVpaid();

        playerFsm.transit(Input.AD_CLICK);
        playerFsm.transit(Input.BACK_TO_PLAYER_FROM_VAST_AD);
        playerFsm.transit(Input.AD_FINISH);
        playerFsm.transit(Input.AD_CLICK);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);
    }

    @Test
    public void testNewFSMFlowWithPreroll() {

        factory = comonent.getStateFactory();

        playerFsm = new FsmPlayerImperial(factory) {
            @Override
            public Class initializeState() {
                return FetchCuePointState.class;
            }
        };

        playerFsm.transit(Input.INITIALIZE);

        assertTrue(playerFsm.getCurrentState() instanceof FetchCuePointState);

        playerFsm.transit(Input.HAS_PREROLL_AD);

        assertTrue(playerFsm.getCurrentState() instanceof MakingPrerollAdCallState);

        playerFsm.transit(Input.PRE_ROLL_AD_RECEIVED);

        assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

        playerFsm.transit(Input.NEXT_AD);

        assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

        playerFsm.transit(Input.AD_CLICK);

        assertTrue(playerFsm.getCurrentState() instanceof VastAdInteractionSandBoxState);

        playerFsm.transit(Input.BACK_TO_PLAYER_FROM_VAST_AD);

        assertTrue(playerFsm.getCurrentState() instanceof AdPlayingState);

        playerFsm.transit(Input.VPAID_MANIFEST);

        assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

        playerFsm.transit(Input.VPAID_MANIFEST);

        assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

        playerFsm.transit(Input.VPAID_MANIFEST);

        assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

        playerFsm.transit(Input.VPAID_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        playerFsm.transit(Input.MAKE_AD_CALL);

        assertTrue(playerFsm.getCurrentState() instanceof MakingAdCallState);

        playerFsm.transit(Input.EMPTY_AD);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        playerFsm.transit(Input.MOVIE_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof FinishState);

    }

    @Test
    public void testNewFSMFlowNoPreroll() {
        factory = comonent.getStateFactory();

        playerFsm = new FsmPlayerImperial(factory) {
            @Override
            public Class initializeState() {
                return FetchCuePointState.class;
            }
        };

        playerFsm.transit(Input.INITIALIZE);

        assertTrue(playerFsm.getCurrentState() instanceof FetchCuePointState);

        playerFsm.transit(Input.NO_PREROLL_AD);

        assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        for (int i = 0; i < 10; i++) {

            playerFsm.transit(Input.MAKE_AD_CALL);

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

            playerFsm.transit(Input.VPAID_MANIFEST);

            assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

            playerFsm.transit(Input.VPAID_MANIFEST);

            assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

            playerFsm.transit(Input.VPAID_FINISH);

            assertTrue(playerFsm.getCurrentState() instanceof MoviePlayingState);

        }

        playerFsm.transit(Input.EMPTY_AD);

        playerFsm.transit(Input.MOVIE_FINISH);

        assertTrue(playerFsm.getCurrentState() instanceof FinishState);
    }

}
