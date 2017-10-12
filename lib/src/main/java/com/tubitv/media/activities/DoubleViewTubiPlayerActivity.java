package com.tubitv.media.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.tubitv.media.R;
import com.tubitv.media.controller.PlayerComponentController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.di.FSMModuleTesting;
import com.tubitv.media.di.component.DaggerFsmComonent;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.concrete.AdPlayingState;
import com.tubitv.media.fsm.concrete.VpaidState;
import com.tubitv.media.fsm.listener.AdPlayingMonitor;
import com.tubitv.media.fsm.listener.CuePointMonitor;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.interfaces.DoublePlayerInterface;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.CuePointsRetriever;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.models.VpaidClient;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiExoPlayerView;

import javax.inject.Inject;


/**
 * Created by allensun on 7/24/17.
 */
public class DoubleViewTubiPlayerActivity extends TubiPlayerActivity implements DoublePlayerInterface {

    protected SimpleExoPlayer adPlayer;

    protected WebView vpaidWebView;

    private static final String TAG = "DoubleViewTubiPlayerAct";

    protected TextView cuePointIndictor;

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
    PlayerComponentController playerComponentController;

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

        cuePointIndictor = (TextView) findViewById(R.id.cuepoint_indictor);
        injectDependency();
    }

    protected void injectDependency() {
        //FSMModuleTesting requirement object such as ExoPlayer haven't been initialized yet
        DaggerFsmComonent.builder().fSMModuleTesting(new FSMModuleTesting(null, null, null, null)).build().inject(this);
    }

    protected FsmPlayer getFsmPlayer() {
        return fsmPlayer;
    }

    @Override
    protected void initPlayer() {
        super.initPlayer();
        createMediaSource();
        setupAdPlayer();
    }

    @Override
    protected void onPlayerReady() {
        prepareFSM();
    }

    @Override
    protected void releasePlayer() {
        super.releasePlayer();
        releaseAdPlayer();
    }

    private static final DefaultBandwidthMeter BANDWIDTH_METER_AD = new DefaultBandwidthMeter();
    private DefaultTrackSelector trackSelector_ad;

    private void setupAdPlayer() {
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER_AD);
        trackSelector_ad = new DefaultTrackSelector(adaptiveTrackSelectionFactory);
        adPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector_ad);
    }

    private void releaseAdPlayer() {
        if (adPlayer != null) {
            updateAdResumePosition();
            adPlayer.release();
            adPlayer = null;
            trackSelector_ad = null;
        }

        if(vpaidWebView!=null){
            vpaidWebView.loadUrl(VpaidClient.EMPTY_URL);
            vpaidWebView.clearHistory();
        }
    }

    private void updateAdResumePosition() {
        if (adPlayer != null && playerUIController != null) {
            int adResumeWindow = adPlayer.getCurrentWindowIndex();
            long adResumePosition = adPlayer.isCurrentWindowSeekable() ? Math.max(0, adPlayer.getCurrentPosition())
                    : C.TIME_UNSET;
            playerUIController.setAdResumeInfo(adResumeWindow, adResumePosition);
        }
    }

    /**
     * update the movie and ad playing position when players are released
     */
    @Override
    protected void updateResumePosition() {
        super.updateResumePosition();

        //keep track of movie player's position when activity resume back
        if (mTubiExoPlayer != null && playerUIController != null && mTubiExoPlayer.getPlaybackState() != ExoPlayer.STATE_IDLE) {
            int resumeWindow = mTubiExoPlayer.getCurrentWindowIndex();
            long resumePosition = mTubiExoPlayer.isCurrentWindowSeekable() ? Math.max(0, mTubiExoPlayer.getCurrentPosition())
                    : C.TIME_UNSET;
            playerUIController.setMovieResumeInfo(resumeWindow, resumePosition);
        }

        //keep track of ad player's position when activity resume back, only keep track when current state is in AdPlayingState.
        if (fsmPlayer.getCurrentState() instanceof AdPlayingState && adPlayer != null && playerUIController != null && adPlayer.getPlaybackState() != ExoPlayer.STATE_IDLE) {
            int ad_resumeWindow = adPlayer.getCurrentWindowIndex();
            long ad_resumePosition = adPlayer.isCurrentWindowSeekable() ? Math.max(0, adPlayer.getCurrentPosition())
                    : C.TIME_UNSET;
            playerUIController.setAdResumeInfo(ad_resumeWindow, ad_resumePosition);
        }
    }

    /**
     * prepare / set up FSM and inject all the elements into the FSM
     */
    @Override
    public void prepareFSM() {
        //update the playerUIController view, need to update the view everything when two ExoPlayer being recreated in activity lifecycle.
        playerUIController.setContentPlayer(mTubiExoPlayer);
        playerUIController.setAdPlayer(adPlayer);
        playerUIController.setExoPlayerView(mTubiPlayerView);
        playerUIController.setVpaidWebView(vpaidWebView);

        //update the MediaModel
        fsmPlayer.setController(playerUIController);
        fsmPlayer.setMovieMedia(mediaModel);
//        fsmPlayer.setAdMedia(adMediaModel);
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

        if (fsmPlayer.isInitialized()) {
            fsmPlayer.updateSelf();
            Utils.hideSystemUI(this, true);
        } else {
            fsmPlayer.transit(Input.INITIALIZE);
        }
    }


    @Override
    protected void initLayout() {
        setContentView(R.layout.activity_double_tubi_player);

        mTubiPlayerView = (TubiExoPlayerView) findViewById(R.id.tubitv_player);
        vpaidWebView = (WebView) findViewById(R.id.vpaid_webview);
        vpaidWebView.setBackgroundColor(Color.BLACK);

        mTubiPlayerView.requestFocus();
        mTubiPlayerView.setActivity(this);
        mTubiPlayerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(playerUIController!=null){
            playerUIController.clearMovieResumeInfo();
        }
    }

    @Override
    public void onBackPressed() {


        if (fsmPlayer != null && fsmPlayer.getCurrentState() instanceof VpaidState && vpaidWebView != null && vpaidWebView.canGoBack()) {

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

    /**
     * creating the {@link MediaSource} for the Exoplayer, recreate it everytime when new {@link SimpleExoPlayer} has been initialized
     *
     * @return
     */
    protected void createMediaSource() {

        mediaModel.setMediaSource(buildMediaSource(mediaModel));
    }

    @Override
    public void onPrepareAds(@Nullable AdMediaModel adMediaModel) {

        for (MediaModel singleMedia : adMediaModel.getListOfAds()) {
            MediaSource adMediaSource = buildMediaSource(singleMedia);
            singleMedia.setMediaSource(adMediaSource);
        }
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

        if(mediaModel!=null && ! TextUtils.isEmpty(mediaModel.getClickThroughUrl())) {
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
}
