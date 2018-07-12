package com.tubitv.demo;

import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by allensun on 8/15/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */

@RunWith(JUnit4.class)
public class ExoPlayerFactoryTest {

    StateFactory stateFactory;

    FsmPlayer playerFsm;

    @Before
    public void setup() {

        stateFactory = new StateFactory();
        stateFactory.overrideStateCreation(TestMoviePlayingState.class);
        stateFactory.overrideStateCreation(TestMakingAdCallState.class);
        stateFactory.overrideStateCreation(TestFinishState.class);
        stateFactory.overrideStateCreation(TestReceivedState.class);
        stateFactory.overrideStateCreation(TestAdPlayingState.class);
        stateFactory.overrideStateCreation(TestVastAdSandBox.class);
        stateFactory.overrideStateCreation(TestVpadState.class);

        playerFsm = new FsmPlayerImperial(stateFactory) {
            @Override
            public Class initializeState() {
                return MoviePlayingState.class;
            }
        };
    }

    @Test
    public void testCustomClass() {

        State state = stateFactory.createState(MoviePlayingState.class);

        assertThat(state instanceof TestMoviePlayingState, is(true));

        // make ad call

        state = stateFactory.createState(MakingAdCallState.class);

        assertThat(state instanceof TestMakingAdCallState, is(true));

        //finish state

        state = stateFactory.createState(FinishState.class);

        assertThat(state instanceof TestFinishState, is(true));

        //receive ad state

        state = stateFactory.createState(ReceiveAdState.class);

        assertThat(state instanceof TestReceivedState, is(true));

        //ad playing state

        state = stateFactory.createState(AdPlayingState.class);

        assertThat(state instanceof TestAdPlayingState, is(true));

        //vast sandbox

        state = stateFactory.createState(VastAdInteractionSandBoxState.class);

        assertThat(state instanceof TestVastAdSandBox, is(true));

        //vpaid

        state = stateFactory.createState(VpaidState.class);

        assertThat(state instanceof TestVpadState, is(true));

        state = stateFactory.createState(MoviePlayingState.class);

        assertThat(state instanceof TestMoviePlayingState, is(true));

        // fetchCuePoint

        state = stateFactory.createState(FetchCuePointState.class);

        assertThat(state instanceof TestFetchCuePointState, is(false));

        stateFactory.overrideStateCreation(TestFetchCuePointState.class);

        state = stateFactory.createState(FetchCuePointState.class);

        assertThat(state instanceof TestFetchCuePointState, is(true));

        // makingPrerollAdcall

        state = stateFactory.createState(MakingPrerollAdCallState.class);

        assertThat(state instanceof TestMakingPrerollAdCallState, is(false));

        stateFactory.overrideStateCreation(TestMakingPrerollAdCallState.class);

        state = stateFactory.createState(MakingPrerollAdCallState.class);

        assertThat(state instanceof TestMakingPrerollAdCallState, is(true));

    }

    @Test
    public void testFSMFlowWithVpaid() {

        playerFsm.transit(Input.INITIALIZE);

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

            playerFsm.transit(Input.VPAID_MANIFEST);

            assertTrue(playerFsm.getCurrentState() instanceof VpaidState);

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

    @Test(expected = IllegalStateException.class)
    public void testWrongStateCreating() {
        StateFactory sf = new StateFactory();
        //not a child class of State
        sf.createState(String.class);
    }

    @Test
    public void testSingleton() {
        // test if the stateFactory creates singleton objects
        State movieState_1 = stateFactory.createState(MoviePlayingState.class);
        State movieState_2 = stateFactory.createState(MoviePlayingState.class);

        assertTrue(movieState_1 == movieState_2);
    }

    /**
     * this is for testing custom class of {@link com.tubitv.media.fsm.BaseState} can be swap into {@link StateFactory}
     */
    public static class TestMoviePlayingState extends MoviePlayingState {
    }

    public static class TestMakingAdCallState extends MakingAdCallState {
    }

    public static class TestFinishState extends FinishState {
    }

    public static class TestReceivedState extends ReceiveAdState {
    }

    public static class TestAdPlayingState extends AdPlayingState {
    }

    public static class TestVastAdSandBox extends VastAdInteractionSandBoxState {
    }

    public static class TestVpadState extends VpaidState {
    }

    public static class TestFetchCuePointState extends FetchCuePointState {
    }

    public static class TestMakingPrerollAdCallState extends MakingPrerollAdCallState {
    }
}
