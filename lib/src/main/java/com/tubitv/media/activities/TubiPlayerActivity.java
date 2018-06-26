package com.tubitv.media.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.tubitv.media.R;
import com.tubitv.media.helpers.MediaHelper;
import com.tubitv.media.helpers.TrackSelectionHelper;
import com.tubitv.media.interfaces.TubiPlaybackInterface;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.EventLogger;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiExoPlayerView;
import com.tubitv.media.views.TubiPlayerControlView;

/**
 * This is the base activity that prepare one instance of {@link SimpleExoPlayer} mMoviePlayer, this player is mean to serve as the main player to player content.
 * Along with some abstract methods to be implemented by subclass for extra functions.
 *
 * You can use this class as it is and implement the abstract methods to be a standalone player to player video with customized UI controls and different forms of adaptive streaming.
 */
public abstract class TubiPlayerActivity extends LifeCycleActivity implements TubiPlayerControlView.VisibilityListener, TubiPlaybackInterface {
    public static String TUBI_MEDIA_KEY = "tubi_media_key";

    protected SimpleExoPlayer mMoviePlayer;
    private Handler mMainHandler;
    protected TubiExoPlayerView mTubiPlayerView;
    private DataSource.Factory mMediaDataSourceFactory;
    protected DefaultTrackSelector mTrackSelector;
    private EventLogger mEventLogger;
    private TrackSelectionHelper mTrackSelectionHelper;

    protected boolean isActive = false;

    /**
     * ideally, only one instance of {@link MediaModel} and its arrtibute {@link MediaSource} for movie should be created throughout the whole movie playing experiences.
     */
    @NonNull
    protected MediaModel mediaModel;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

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
        mMediaDataSourceFactory = buildDataSourceFactory(true);
        initLayout();
    }

    @Override
    public void onNewIntent(Intent intent) {
        releaseMoviePlayer();
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
        if ((Util.SDK_INT <= 23 || mMoviePlayer == null)) {
            setupExo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releaseMoviePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releaseMoviePlayer();
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
        mTubiPlayerView.setActivity(this);
    }

    private void setCaption(boolean isOn) {
        if (mediaModel != null && mediaModel.getSubtitlesUrl() != null && mTubiPlayerView != null && mTubiPlayerView.getControlView() != null) {
            mTubiPlayerView.getControlView().checkSubtitleIcon(isOn);
            mTubiPlayerView.getSubtitleView().setVisibility(isOn ? View.VISIBLE : View.GONE);
        }
    }

    private void setupExo() {
        setCaption(isCaptionPreferenceEnable());
        initMoviePlayer();
        isActive = true;
        onPlayerReady();
    }


    protected void initMoviePlayer() {
        // 1. Create a default TrackSelector
        mMainHandler = new Handler();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        mTrackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        mTrackSelectionHelper = new TrackSelectionHelper(this, mTrackSelector,videoTrackSelectionFactory);


        // 3. Create the mMoviePlayer
        mMoviePlayer = ExoPlayerFactory.newSimpleInstance(this, mTrackSelector);

        mEventLogger = new EventLogger(mTrackSelector);
        mMoviePlayer.addListener(mEventLogger);
        mMoviePlayer.setAudioDebugListener(mEventLogger);
        mMoviePlayer.setVideoDebugListener(mEventLogger);
        mMoviePlayer.setMetadataOutput(mEventLogger);

        mTubiPlayerView.setPlayer(mMoviePlayer, this);
        mTubiPlayerView.setMediaModel(mediaModel,true);
        mTubiPlayerView.setTrackSelectionHelper(mTrackSelectionHelper);
    }

    protected void releaseMoviePlayer() {
        if (mMoviePlayer != null) {
            updateResumePosition();
            mMoviePlayer.release();
            mMoviePlayer = null;
            mTrackSelector = null;
        }
        isActive = false;
    }

    protected MediaSource buildMediaSource(MediaModel model) {
        MediaSource mediaSource;
        int type = TextUtils.isEmpty(model.getMediaExtension()) ? Util.inferContentType(model.getVideoUrl())
                : Util.inferContentType("." + model.getMediaExtension());
        switch (type) {
            case C.TYPE_SS:
                mediaSource = new SsMediaSource(model.getVideoUrl(), buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mMediaDataSourceFactory), mMainHandler, mEventLogger);
                break;
            case C.TYPE_DASH:
                mediaSource = new DashMediaSource(model.getVideoUrl(), buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mMediaDataSourceFactory), mMainHandler, mEventLogger);
                break;
            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource(model.getVideoUrl(), mMediaDataSourceFactory, mMainHandler, mEventLogger);
                break;
            case C.TYPE_OTHER:
                mediaSource = new ExtractorMediaSource(model.getVideoUrl(), mMediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mMainHandler, mEventLogger);
                break;
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }

        if (model.getSubtitlesUrl() != null) {
            MediaSource subtitleSource = new SingleSampleMediaSource(
                    model.getSubtitlesUrl(),
                    buildDataSourceFactory(false),
                    Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE, C.SELECTION_FLAG_DEFAULT, "en", null, 0),
                    0);
            // Plays the video with the sideloaded subtitle.
            mediaSource =
                    new MergingMediaSource(mediaSource, subtitleSource);
        }

        return mediaSource;
    }

    /**
     * Returns a new DataSource factory.MainActivity
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    protected DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return MediaHelper.buildDataSourceFactory(this, useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "TubiPlayerActivity"), bandwidthMeter);
    }


}
