package com.tubitv.media.fsm;

/**
 * Created by allensun on 7/27/17.
 */
public enum Input {


    /**
     *  Only expect inputs of {@link com.tubitv.media.fsm.concrete.MakingAdCallState}
     */
    AD_RECEIVED,
    EMPTY_AD,

    /**
     *  Only expect inputs of {@link com.tubitv.media.fsm.concrete.ReceiveAdState}
     */
    SHOW_ADS,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.AdPlayingState}
     */
    AD_CLICK,
    AD_FINISH,
    VPAID_MANIFEST,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.WebviewAdSandBoxState}
     */
    BACK_TO_PLAYER,

    /**
     * Only expect inputs of {@link com.tubitv.media.fsm.concrete.MoviePlayingState}
     */
    MAKE_AD_CALL,
    MOVIE_FINISH,

}
