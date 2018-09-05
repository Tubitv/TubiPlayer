package com.tubitv.media.fsm.concrete;

import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;
import com.google.android.exoplayer2.ExoPlayer;
import com.tubitv.media.controller.PlayerAdLogicController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.fsm.BaseState;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.helpers.Constants;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.models.VpaidClient;
import com.tubitv.media.utilities.ExoPlayerLogger;
import com.tubitv.media.views.TubiExoPlayerView;

/**
 * Created by allensun on 8/1/17.
 */
public class VpaidState extends BaseState {

    @Override
    public State transformToState(Input input, StateFactory factory) {

        switch (input) {
            case VPAID_FINISH:
                return factory.createState(MoviePlayingState.class);

            case VPAID_MANIFEST:
                return factory.createState(VpaidState.class);

            case NEXT_AD:
                return factory.createState(AdPlayingState.class);
        }
        return null;
    }

    //TODO: API level lower that certain, will disable vpaid.
    @Override
    public void performWorkAndUpdatePlayerUI(@NonNull FsmPlayer fsmPlayer) {
        super.performWorkAndUpdatePlayerUI(fsmPlayer);

        if (isNull(fsmPlayer)) {
            return;
        }

        pausePlayerAndSHowVpaid(controller, componentController, fsmPlayer, adMedia);
    }

    private void pausePlayerAndSHowVpaid(PlayerUIController controller, PlayerAdLogicController componentController,
            FsmPlayer fsmPlayer, AdMediaModel adMedia) {

        ExoPlayer moviePlayer = controller.getContentPlayer();

        if (moviePlayer != null && moviePlayer.getPlayWhenReady()) {
            moviePlayer.setPlayWhenReady(false);
        }

        ExoPlayer adPlayer = controller.getAdPlayer();
        if (adPlayer != null && adPlayer.getPlayWhenReady()) {
            adPlayer.setPlayWhenReady(false);
        }

        VpaidClient client = componentController.getVpaidClient();

        if (client != null) {

            MediaModel ad = adMedia.nextAD();

            if (ad == null) {
                ExoPlayerLogger.w(Constants.FSMPLAYER_TESTING, "Vpaid ad is null");
                return;
            }

            client.init(ad);

            controller.getExoPlayerView().setVisibility(View.INVISIBLE);

            WebView vpaidWebView = controller.getVpaidWebView();

            vpaidWebView.setVisibility(View.VISIBLE);
            vpaidWebView.bringToFront();
            vpaidWebView.invalidate();

            vpaidWebView.addJavascriptInterface(client, "TubiNativeJSInterface");
            vpaidWebView.loadUrl(fsmPlayer.getVPAID_END_POINT());

            //hide the subtitle view when vpaid is playing
            ((TubiExoPlayerView) controller.getExoPlayerView()).getSubtitleView().setVisibility(View.INVISIBLE);
        } else {
            ExoPlayerLogger.w(Constants.FSMPLAYER_TESTING, "VpaidClient is null");
        }

    }

}
