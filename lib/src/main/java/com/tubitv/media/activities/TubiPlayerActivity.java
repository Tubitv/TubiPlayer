package com.tubitv.media.activities;

import android.app.Activity;
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

public abstract class TubiPlayerActivity extends Activity implements TubiPlayerControlView.VisibilityListener, TubiPlaybackInterface {
    public static String TUBI_MEDIA_KEY = "tubi_media_key";

    protected SimpleExoPlayer mTubiExoPlayer;
    private Handler mMainHandler;
    protected TubiExoPlayerView mTubiPlayerView;
    private DataSource.Factory mMediaDataSourceFactory;
    protected DefaultTrackSelector mTrackSelector;
    private EventLogger mEventLogger;
    private TrackSelectionHelper mTrackSelectionHelper;

    protected int resumeWindow;

    protected long resumePosition;

    protected boolean isActive = false;

    @NonNull
    protected MediaModel mediaModel;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    protected boolean shouldAutoPlay;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utils.hideSystemUI(this, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearResumePosition();
        parseIntent();
        Utils.hideSystemUI(this, true);
        shouldAutoPlay = true;
        mMediaDataSourceFactory = buildDataSourceFactory(true);
        initLayout();
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        clearResumePosition();
        shouldAutoPlay = true;
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
        if ((Util.SDK_INT <= 23 || mTubiExoPlayer == null)) {
            setupExo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
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
        mTubiPlayerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });
    }

    private void setupExo() {
        initPlayer();
        isActive = true;
        onPlayerReady();
    }

    protected abstract void onPlayerReady();

    protected void initPlayer() {
        // 1. Create a default TrackSelector
        mMainHandler = new Handler();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        mTrackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        mTrackSelectionHelper = new TrackSelectionHelper(this, mTrackSelector);


        // 3. Create the mTubiExoPlayer
        mTubiExoPlayer = ExoPlayerFactory.newSimpleInstance(this, mTrackSelector);

        mEventLogger = new EventLogger(mTrackSelector);
        mTubiExoPlayer.addListener(mEventLogger);
        mTubiExoPlayer.setAudioDebugListener(mEventLogger);
        mTubiExoPlayer.setVideoDebugListener(mEventLogger);
        mTubiExoPlayer.setMetadataOutput(mEventLogger);

        mTubiPlayerView.setPlayer(mTubiExoPlayer, this);
        mTubiPlayerView.setMediaModel(mediaModel,true);
        mTubiPlayerView.setTrackSelectionHelper(mTrackSelectionHelper);
    }

    protected void playMedia(MediaSource mediaSource) {
        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            mTubiExoPlayer.seekTo(resumeWindow, resumePosition);
        }
        mTubiExoPlayer.setPlayWhenReady(shouldAutoPlay);
        mTubiExoPlayer.prepare(mediaSource, !haveResumePosition, false);
        Utils.hideSystemUI(this, true);
    }

    protected void releasePlayer() {
        if (mTubiExoPlayer != null) {
            shouldAutoPlay = mTubiExoPlayer.getPlayWhenReady();
            updateResumePosition();
            mTubiExoPlayer.release();
            mTubiExoPlayer = null;
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

    protected void updateResumePosition() {
        if (mTubiExoPlayer != null) {
            resumeWindow = mTubiExoPlayer.getCurrentWindowIndex();
            resumePosition = mTubiExoPlayer.isCurrentWindowSeekable() ? Math.max(0, mTubiExoPlayer.getCurrentPosition())
                    : C.TIME_UNSET;
        }
    }

    protected void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "TubiPlayerActivity"), bandwidthMeter);
    }


}
