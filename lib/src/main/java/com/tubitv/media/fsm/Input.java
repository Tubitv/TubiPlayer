package com.tubitv.media.fsm;

import com.tubitv.media.fsm.concrete.VastAdInteractionSandBoxState;

/**
 * Created by allensun on 7/27/17.
 */
public enum Input {

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.FetchCuePointState}
     */
    HAS_PREROLL_AD,
    NO_PREROLL_AD,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.MakingPrerollAdCallState}
     */
    PRE_ROLL_AD_RECEIVED,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.MakingAdCallState}
     */
    AD_RECEIVED,
    EMPTY_AD,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.ReceiveAdState}
     */
    SHOW_ADS,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.AdPlayingState}
     */
    NEXT_AD,
    AD_CLICK,
    AD_FINISH,
    VPAID_MANIFEST,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.VpaidState}
     */
    VPAID_FINISH,

    /**
     * Only expect inputs of {@link VastAdInteractionSandBoxState}
     */
    BACK_TO_PLAYER_FROM_VAST_AD,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.MoviePlayingState}
     */
    MAKE_AD_CALL,
    MOVIE_FINISH,

    /**
     * ERROR INPUT
     */
    ERROR,

    INITIALIZE,
}
