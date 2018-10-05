package com.tubitv.media.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.tubitv.media.bindings.UserController;
import com.tubitv.media.controller.PlayerAdLogicController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.di.FSMModuleTesting;
import com.tubitv.media.di.component.DaggerFsmComonent;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.concrete.VpaidState;
import com.tubitv.media.fsm.listener.AdPlayingMonitor;
import com.tubitv.media.fsm.listener.CuePointMonitor;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.interfaces.AutoPlay;
import com.tubitv.media.interfaces.DoublePlayerInterface;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.CuePointsRetriever;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.models.VpaidClient;
import com.tubitv.media.player.PlayerContainer;
import com.tubitv.media.utilities.PlayerDeviceUtils;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.UIControllerView;
import javax.inject.Inject;

/**
 * Created by allensun on 7/24/17.
 */
public class DoubleViewTubiPlayerActivity extends TubiPlayerActivity implements DoublePlayerInterface, AutoPlay {

    private static final String TAG = DoubleViewTubiPlayerActivity.class.getSimpleName();
    @Inject
    FsmPlayer fsmPlayer;
    @Inject
    PlayerUIController playerUIController;
    @Inject
    AdPlayingMonitor adPlayingMonitor;
    @Inject
    CuePointMonitor cuePointMonitor;
    @Inject
    AdRetriever adRetriever;
    @Inject
    CuePointsRetriever cuePointsRetriever;
    @Inject
    AdInterface adInterface;
    @Inject
    PlayerAdLogicController playerComponentController;
    @Inject
    VpaidClient vpaidClient;

    protected AdRetriever getAdRetriever() {
        return adRetriever;
    }

    protected CuePointsRetriever getCuePointsRetriever() {
        return cuePointsRetriever;
    }

    protected PlayerUIController getPlayerUIController() {
        return playerUIController;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        injectDependency();
        dependencyPrepare();
    }

    @Override
    public View addUserInteractionView() {
        return new UIControllerView(getBaseContext())
                .setUserController((UserController) getPlayerController());
    }

    /**
     * this should be the first method the subclass of {@link DoubleViewTubiPlayerActivity} call, before accessing all the different variables.
     */
    protected void injectDependency() {
        //FSMModuleTesting requirement object such as ExoPlayer haven't been initialized yet
        DaggerFsmComonent.builder().fSMModuleTesting(new FSMModuleTesting(null, null, null, null)).build().inject(this);
    }

    /**
     * this method is called immediately after {@link DoubleViewTubiPlayerActivity#injectDependency()} for all injected dependencies preparation.
     */
    protected void dependencyPrepare() {
    }

    protected FsmPlayer getFsmPlayer() {
        return fsmPlayer;
    }

    @Override
    protected void initPlayer() {
        super.initPlayer();
        PlayerContainer.setFsmPlayer(fsmPlayer);
    }

    @Override
    protected void onPlayerReady() {
        prepareFSM();
    }

    @Override
    protected void cleanUpPlayer() {
        super.cleanUpPlayer();
        if (vpaidWebView != null) {
            vpaidWebView.loadUrl(VpaidClient.EMPTY_URL);
            vpaidWebView.clearHistory();
        }
    }

    /**
     * update the movie and ad playing position when players are released
     */
    @Override
    protected void updateResumePosition() {
        if (PlayerContainer.getPlayer() != null
                && playerUIController != null
                && PlayerContainer.getPlayer().getPlaybackState() != Player.STATE_IDLE
                && !playerUIController.isPlayingAds) {

            int resumeWindow = PlayerContainer.getPlayer().getCurrentWindowIndex();
            long resumePosition = PlayerContainer.getPlayer().isCurrentWindowSeekable() ?
                    Math.max(0, PlayerContainer.getPlayer().getCurrentPosition()) : C.TIME_UNSET;

            playerUIController.setMovieResumeInfo(resumeWindow, resumePosition);
        }
    }

    @Override
    protected boolean isCaptionPreferenceEnable() {
        return true;
    }

    /**
     * prepare / set up FSM and inject all the elements into the FSM
     */
    @Override
    public void prepareFSM() {
        //update the playerUIController view, need to update the view everything when two ExoPlayer being recreated in activity lifecycle.
        playerUIController.setExoPlayerView(mTubiPlayerView);
        playerUIController.setVpaidWebView(vpaidWebView);

        //update the MediaModel
        fsmPlayer.setController(playerUIController);
        fsmPlayer.setMovieMedia(mediaModel);
        fsmPlayer.setAdRetriever(adRetriever);
        fsmPlayer.setCuePointsRetriever(cuePointsRetriever);
        fsmPlayer.setAdServerInterface(adInterface);

        //set the PlayerComponentController.
        playerComponentController.setAdPlayingMonitor(adPlayingMonitor);
        playerComponentController.setTubiPlaybackInterface(this);
        playerComponentController.setDoublePlayerInterface(this);
        playerComponentController.setCuePointMonitor(cuePointMonitor);
        playerComponentController.setVpaidClient(vpaidClient);
        fsmPlayer.setPlayerComponentController(playerComponentController);
        fsmPlayer.setLifecycle(getLifecycle());

        if (fsmPlayer.isInitialized()) {
            fsmPlayer.updateSelf();
            Utils.hideSystemUI(this, true);
        } else {
            fsmPlayer.transit(Input.INITIALIZE);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (playerUIController != null) {
            playerUIController.clearMovieResumeInfo();
        }
    }

    @Override
    public void onBackPressed() {

        if (fsmPlayer != null && fsmPlayer.getCurrentState() instanceof VpaidState && vpaidWebView != null
                && vpaidWebView.canGoBack()) {

            //if the last page is empty url, then, it should
            if (ingoreWebViewBackNavigation(vpaidWebView)) {
                super.onBackPressed();
                return;
            }

            vpaidWebView.goBack();

        } else {
            super.onBackPressed();
        }
    }

    //when the last item is "about:blank", ingore the back navigation for webview.
    private boolean ingoreWebViewBackNavigation(WebView vpaidWebView) {

        if (vpaidWebView != null) {
            WebBackForwardList mWebBackForwardList = vpaidWebView.copyBackForwardList();

            if (mWebBackForwardList == null) {
                return false;
            }

            WebHistoryItem historyItem = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex() - 1);

            if (historyItem == null) {
                return false;
            }

            String historyUrl = historyItem.getUrl();

            if (historyUrl != null && historyUrl.equalsIgnoreCase(VpaidClient.EMPTY_URL)) {

                return true;
            }
        }

        return false;
    }

    @Override
    public void onProgress(@Nullable MediaModel mediaModel, long milliseconds, long durationMillis) {
        //        ExoPlayerLogger.v(TAG, mediaModel.getMediaName() + ": " + mediaModel.toString() + " onProgress: " + "milliseconds: " + milliseconds + " durationMillis: " + durationMillis);

        // monitor the movie progress.
        cuePointMonitor.onMovieProgress(milliseconds, durationMillis);
    }

    @Override
    public void onSeek(@Nullable MediaModel mediaModel, long oldPositionMillis, long newPositionMillis) {
        //        ExoPlayerLogger.v(TAG, mediaModel.getMediaName() + ": " + mediaModel.toString() + " onSeek : " + "oldPositionMillis: " + oldPositionMillis + " newPositionMillis: " + newPositionMillis);
    }

    @Override
    public void onPlayToggle(@Nullable MediaModel mediaModel, boolean playing) {
        //        ExoPlayerLogger.v(TAG, mediaModel.getMediaName() + ": " + mediaModel.toString() + " onPlayToggle :");
    }

    @Override
    public void onLearnMoreClick(@NonNull MediaModel mediaModel) {
        //        ExoPlayerLogger.v(TAG, mediaModel.getMediaName() + ": " + mediaModel.toString() + " onLearnMoreClick :" + mediaModel.getClickThroughUrl());

        if (!PlayerDeviceUtils.isTVDevice(this)
                && mediaModel != null
                && !TextUtils.isEmpty(mediaModel.getClickThroughUrl())) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mediaModel.getClickThroughUrl()));
            startActivity(browserIntent);
        }
    }

    @Override
    public void onSubtitles(@Nullable MediaModel mediaModel, boolean enabled) {
        //        ExoPlayerLogger.v(TAG, mediaModel.getMediaName() + ": " + mediaModel.toString() + " onSubtitles :" + mediaModel.getMediaName());
    }

    @Override
    public void onQuality(@Nullable MediaModel mediaModel) {
        //        ExoPlayerLogger.v(TAG, mediaModel.getMediaName() + ": " + mediaModel.toString() + " onQuality :" + mediaModel.getMediaName());
    }

    @Override
    public void onCuePointReceived(long[] cuePoints) {

        cuePointIndictor.setText(printCuePoints(cuePoints));
    }

    private String printCuePoints(long[] cuePoints) {
        StringBuilder builder = new StringBuilder();
        builder.append("Adbreak will be in : ");

        for (int i = 0; i < cuePoints.length; i++) {

            double minutes = (double) cuePoints[i] / 1000 / 60;

            double second = (minutes - Math.floor(minutes)) * 60;

            builder.append((int) minutes + "min" + (int) second + "sec, ");
        }

        return builder.toString();
    }

    @Override
    public void playNext(MediaModel nextVideo) {
        PlayerContainer.preparePlayer(nextVideo);
        fsmPlayer.setMovieMedia(nextVideo);
        fsmPlayer.restart();
    }
}
