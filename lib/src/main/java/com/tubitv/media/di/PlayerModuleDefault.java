package com.tubitv.media.di;

import android.support.annotation.Nullable;
import com.tubitv.media.controller.PlayerAdLogicController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.di.annotation.ActicityScope;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.callback.CuePointCallBack;
import com.tubitv.media.fsm.callback.RetrieveAdCallback;
import com.tubitv.media.fsm.concrete.FetchCuePointState;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.listener.AdPlayingMonitor;
import com.tubitv.media.fsm.listener.CuePointMonitor;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.fsm.state_machine.FsmPlayerImperial;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.CuePointsRetriever;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.models.VpaidClient;
import dagger.Module;
import dagger.Provides;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by allensun on 8/7/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
@Module
public class PlayerModuleDefault {

    public PlayerModuleDefault() {
    }

    @ActicityScope
    @Provides
    StateFactory provideStateFactory() {
        return new StateFactory();
    }

    @ActicityScope
    @Provides
    FsmPlayer provideFsmPlayer(StateFactory factory) {
        return new FsmPlayerImperial(factory) {
            @Override
            public Class initializeState() {
                return FetchCuePointState.class;
            }
        };
    }

    @ActicityScope
    @Provides
    PlayerUIController provideController() {
        return new PlayerUIController();
    }

    @ActicityScope
    @Provides
    PlayerAdLogicController provideComponentController() {
        return new PlayerAdLogicController();
    }

    @ActicityScope
    @Provides
    AdRetriever provideAdRetriever() {
        return new AdRetriever();
    }

    @ActicityScope
    @Provides
    CuePointsRetriever provideCuePointsRetriever() {
        return new CuePointsRetriever();
    }

    @ActicityScope
    @Provides
    AdPlayingMonitor provideAdPlayingMonitor(FsmPlayer player) {
        return new AdPlayingMonitor(player);
    }

    @ActicityScope
    @Provides
    CuePointMonitor provideCuePointMonitor(FsmPlayer fsmPlayer) {

        CuePointMonitor cuePointMonitor = new CuePointMonitor(fsmPlayer) {
            @Override
            public int networkingAhead() {
                return 5000;
            }
        };
        return cuePointMonitor;
    }

    @ActicityScope
    @Provides
    AdMediaModel provideAdMediaModel() {
        MediaModel ad_1 = MediaModel
                .ad("http://devimages.apple.com/samplecode/adDemo/ad.m3u8",
                        "https://tubitv.com/", false);

        final List<MediaModel> list = new ArrayList<>();
        list.add(ad_1);

        AdMediaModel adMediaModel = new AdMediaModel(list) {
            @Nullable
            @Override
            public MediaModel nextAD() {
                return list != null && list.size() > 0 ? list.get(0) : null;
            }
        };

        return adMediaModel;
    }

    @ActicityScope
    @Provides
    AdInterface provideAdInterfaceNoPreroll() {

        // using the fake generated AdMediaModel to do has the returned data.
        return new AdInterface() {
            @Override
            public void fetchAd(AdRetriever retriever, RetrieveAdCallback callback) {
                callback.onReceiveAd(provideAdMediaModel());
            }

            @Override
            public void fetchQuePoint(CuePointsRetriever retriever, CuePointCallBack callBack) {

                callBack.onCuePointReceived(null);
                //"AdBreak point at 0s, 1min, 15min, 30min, 60min. With each adbreak showing one ads"
                //new long[] { 60000, 900000, 1800000, 3600000 }
            }
        };
    }

    @ActicityScope
    @Provides
    VpaidClient provideVpaidClient() {
        return new VpaidClient() {
            @Override
            public void init(MediaModel ad) {

            }

            @Override
            public void notifyAdError(int code, String error) {

            }

            @Override
            public void notifyVideoEnd() {

            }

            @Override
            public String getVastXml() {
                return null;
            }
        };
    }
}
