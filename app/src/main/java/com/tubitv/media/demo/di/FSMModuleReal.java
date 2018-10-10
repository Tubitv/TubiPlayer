package com.tubitv.media.demo.di;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;
import com.tubitv.media.controller.PlayerAdLogicController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.demo.vpaid_model.TubiVPAID;
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
 * Created by allensun on 8/29/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
@Module
public class FSMModuleReal {

    private WebView webView;

    private View rootView;

    public FSMModuleReal(@Nullable WebView webView, @Nullable View rootView) {
        this.webView = webView;
        this.rootView = rootView;
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
        return new PlayerUIController(webView, rootView);
    }

    @ActicityScope
    @Provides
    PlayerAdLogicController provideComponentController() {
        return new PlayerAdLogicController(null, null, null, null);
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

        //        cuePointMonitor.setQuePoints(new int[]{0, 60000, 900000, 1800000, 3600000});

        return cuePointMonitor;
    }

    @ActicityScope
    @Provides
    AdMediaModel provideAdMediaModel() {
        // this is the corrupted video source
        MediaModel ad_1 = MediaModel
                .ad("http://c13.adrise.tv/ads/transcodes/003121/1163277/v0809172903-640x360-SD-1047k.mp4.m3u8",
                        "first ad", false);

        MediaModel ad_2 = MediaModel
                .ad("http://c11.adrise.tv/ads/transcodes/003572/940826/v0329081907-1280x720-HD-,740,1285,1622,2138,3632,k.mp4.m3u8",
                        "second ad", false);

        MediaModel ad_3 = MediaModel
                .ad("http://c11.adrise.tv/ads/transcodes/003572/940826/v0329081907-1280x720-HD-,740,1285,1622,2138,3632,k.mp4.m3u8",
                        "third ad", true);

        final List<MediaModel> list = new ArrayList<>();

        //        list.add(ad_3);

        //        list.add(ad_3);
        list.add(ad_2);

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
            public void fetchAd(AdRetriever retriever, final RetrieveAdCallback callback) {

                // post 2000 second delay to emulate networking delay
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onReceiveAd(provideAdMediaModel());
                            }
                        });
                    }
                }).start();

            }

            @Override
            public void fetchQuePoint(CuePointsRetriever retriever, final CuePointCallBack callBack) {

                // post 2000 second delay to emulate networking delay
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onCuePointReceived(new long[] { 0, 60000, 900000, 1800000, 3600000 });
                            }
                        });
                    }
                }).start();

            }
        };
    }

    @ActicityScope
    @Provides
    VpaidClient provideVpaidClient(FsmPlayer player) {
        return new TubiVPAID(webView, new Handler(Looper.getMainLooper()), player);
    }
}
