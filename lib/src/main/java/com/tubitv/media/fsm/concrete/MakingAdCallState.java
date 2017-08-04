package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.RetrieveAdCallback;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.MediaModel;

/**
 * Created by allensun on 7/31/17.
 */
public class MakingAdCallState extends BaseState {

    private static final String TAG = MakingAdCallState.class.toString();

    private AdInterface adInterface;

    private String videoId;

    private String videoPublisherId;

    private long cuePointPos = -1;

    public void injectDependency(AdInterface adInterface, String videoId, String videoPublisherId, long cuePointPos) {
        this.adInterface = adInterface;
        this.videoId = videoId;
        this.videoPublisherId = videoPublisherId;
        this.cuePointPos = cuePointPos;
    }

    @Override
    public State transformToState(Input input, StateFactory factory) {
        switch (input) {
            case AD_RECEIVED:
                return factory.createState(ReceiveAdState.class);

            case EMPTY_AD:
                return factory.createState(MoviePlayingState.class);

            case MAKE_AD_CALL:
                return factory.createState(MakingAdCallState.class);

            /***************below is the error handling******************************************/
            case SHOW_ADS:
                // ad server hasn't return any ad, can not show ad, this round of ad showing opportunity is over.
                return factory.createState(MoviePlayingState.class);

        }
        return null;
    }

    @Override
    public void updatePlayerUI(@NonNull PlayerUIController controller, @NonNull MediaModel movieMedia, @Nullable MediaModel adMedia) {

    }

    public void fetchAd(AdInterface adInterface, AdRetriever retriever, RetrieveAdCallback callback) {
        if (adInterface != null && retriever != null) {
            adInterface.fetchAd(retriever,callback);
        } else {
            Log.e(TAG, "fetchAd fail, adInterface or retreiever is empty");
        }
    }
}
