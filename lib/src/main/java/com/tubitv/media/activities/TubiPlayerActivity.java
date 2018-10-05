package com.tubitv.media.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import com.tubitv.media.R;
import com.tubitv.media.interfaces.PlaybackActionCallback;
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.player.PlayerContainer;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiExoPlayerView;

/**
 * This is the base activity that prepare one instance of {@link SimpleExoPlayer} mMoviePlayer, this player is mean to serve as the main player to player content.
 * Along with some abstract methods to be implemented by subclass for extra functions.
 * You can use this class as it is and implement the abstract methods to be a standalone player to player video with customized UI controls and different forms of adaptive streaming.
 */
public abstract class TubiPlayerActivity extends LifeCycleActivity
        implements PlaybackActionCallback {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    public static String TUBI_MEDIA_KEY = "tubi_media_key";
    protected TubiExoPlayerView mTubiPlayerView;
    protected WebView vpaidWebView;
    protected TextView cuePointIndictor;
    protected boolean isActive = false;
    /**
     * ideally, only one instance of {@link MediaModel} and its arrtibute {@link MediaSource} for movie should be created throughout the whole movie playing experiences.
     */
    @NonNull
    protected MediaModel mediaModel;
    protected Handler mMainHandler = new Handler();

    public abstract View addUserInteractionView();

    protected abstract void onPlayerReady();

    protected abstract void updateResumePosition();

    protected abstract boolean isCaptionPreferenceEnable();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utils.hideSystemUI(this, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        Utils.hideSystemUI(this, true);
        initLayout();
    }

    @Override
    public void onNewIntent(Intent intent) {
        cleanUpPlayer();
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            setupExo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) {
            setupExo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            cleanUpPlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            cleanUpPlayer();
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @SuppressWarnings("ConstantConditions")
    protected void parseIntent() {
        String errorNoMediaMessage = getResources().getString(R.string.activity_tubi_player_no_media_error_message);
        Assertions.checkState(getIntent() != null && getIntent().getExtras() != null,
                errorNoMediaMessage);
        mediaModel = (MediaModel) getIntent().getExtras().getSerializable(TUBI_MEDIA_KEY);
        Assertions.checkState(mediaModel != null,
                errorNoMediaMessage);
    }

    protected void initLayout() {
        setContentView(R.layout.activity_tubi_player);
        mTubiPlayerView = (TubiExoPlayerView) findViewById(R.id.tubitv_player);
        mTubiPlayerView.requestFocus();
        vpaidWebView = (WebView) findViewById(R.id.vpaid_webview);
        vpaidWebView.setBackgroundColor(Color.BLACK);

        cuePointIndictor = (TextView) findViewById(R.id.cuepoint_indictor);
        mTubiPlayerView.addUserInteractionView(addUserInteractionView());
    }

    private void setCaption(boolean isOn) {
        if (mediaModel != null && mediaModel.getSubtitlesUrl() != null && mTubiPlayerView != null
                && mTubiPlayerView.getControlView() != null) {
            mTubiPlayerView.getPlayerController().triggerSubtitlesToggle(isOn);
        }
    }

    private void setupExo() {
        initPlayer();
        setCaption(isCaptionPreferenceEnable());
        isActive = true;
        onPlayerReady();
    }

    protected void initPlayer() {
        PlayerContainer.initialize(this, mMainHandler, mediaModel);
    }

    protected void cleanUpPlayer() {
        updateResumePosition();
        PlayerContainer.releasePlayer();
        PlayerContainer.cleanUp();
        isActive = false;
    }

    protected TubiPlaybackControlInterface getPlayerController() {
        if (mTubiPlayerView != null && mTubiPlayerView.getPlayerController() != null) {
            return mTubiPlayerView.getPlayerController();
        }
        return null;
    }

}
