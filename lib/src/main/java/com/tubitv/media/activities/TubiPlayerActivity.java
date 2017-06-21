package com.tubitv.media.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
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
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
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
import com.tubitv.media.MediaHelper;
import com.tubitv.media.R;
import com.tubitv.media.helpers.TrackSelectionHelper;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.EventLogger;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiExoPlayerView;
import com.tubitv.media.views.TubiPlayerControlView;

public class TubiPlayerActivity extends Activity implements TubiPlayerControlView.VisibilityListener {
    public static String TUBI_MEDIA_KEY = "tubi_media_key";

    private SimpleExoPlayer mTubiExoPlayer;
    private Handler mMainHandler;
    private TubiExoPlayerView mTubiPlayerView;
    private DataSource.Factory mMediaDataSourceFactory;
    private DefaultTrackSelector mTrackSelector;
    private EventLogger mEventLogger;
    private TrackSelectionHelper mTrackSelectionHelper;

    private int resumeWindow;

    private long resumePosition;

    @NonNull
    private MediaModel mediaModel;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private boolean shouldAutoPlay;

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

    @SuppressWarnings("ConstantConditions")
    private void parseIntent() {
        String errorNoMediaMessage = getResources().getString(R.string.activity_tubi_player_no_media_error_message);
        Assertions.checkState(getIntent() != null && getIntent().getExtras() != null,
                errorNoMediaMessage);
        mediaModel = (MediaModel) getIntent().getExtras().getSerializable(TUBI_MEDIA_KEY);
        Assertions.checkState(mediaModel != null,
                errorNoMediaMessage);
    }

    private void initLayout() {
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

        MediaSource mediaSource = createMediaSource();

        playMedia(mediaSource);
    }

    private void initPlayer() {
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

        mTubiPlayerView.setPlayer(mTubiExoPlayer);
        mTubiPlayerView.setMediaModel(mediaModel);
        mTubiPlayerView.setTrackSelectionHelper(mTrackSelectionHelper);
        mTubiExoPlayer.setPlayWhenReady(shouldAutoPlay);
    }

    private MediaSource createMediaSource() {
        //fake media
        Uri uri;
        uri = mediaModel.getVideoUrl();
        String extension = "m3u8";
        MediaSource mediaSource;
        mediaSource = buildMediaSource(uri, extension);

        if (mediaModel.getSubtitlesUrl() != null) {
            MediaSource subtitleSource = new SingleSampleMediaSource(
                    mediaModel.getSubtitlesUrl(),
                    buildDataSourceFactory(false),
                    Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE, C.SELECTION_FLAG_DEFAULT, "en", null, 0),
                    0);
            // Plays the video with the sideloaded subtitle.
            mediaSource =
                    new MergingMediaSource(mediaSource, subtitleSource);
        }

        //ad
        Uri adUri = Uri.parse("http://c11.adrise.tv/ads/transcodes/003572/940826/v0329081907-1280x720-HD-,740,1285,1622,2138,3632,k.mp4.m3u8");
        MediaSource adOne = buildMediaSource(adUri, extension);
        MediaSource adTwo = buildMediaSource(adUri, extension);
        ConcatenatingMediaSource concatenatedSource =
//                new ConcatenatingMediaSource(mediaSource);
//                new ConcatenatingMediaSource(adOne, adTwo, mediaSource);
                new ConcatenatingMediaSource(
                        adOne,
//                        new ClippingMediaSource(mediaSource, 0, 30 * 1000000),
//                        adTwo
                        new ClippingMediaSource(mediaSource, 800 * 1000000, C.TIME_END_OF_SOURCE)
        );
        return concatenatedSource;
    }

    private void playMedia(MediaSource mediaSource) {
        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            mTubiExoPlayer.seekTo(resumeWindow, resumePosition);
        }
        mTubiExoPlayer.prepare(mediaSource, !haveResumePosition, false);
        Utils.hideSystemUI(this, true);
    }

    private void releasePlayer() {
        if (mTubiExoPlayer != null) {
            shouldAutoPlay = mTubiExoPlayer.getPlayWhenReady();
            updateResumePosition();
            mTubiExoPlayer.release();
            mTubiExoPlayer = null;
            mTrackSelector = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mMediaDataSourceFactory), mMainHandler, mEventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mMediaDataSourceFactory), mMainHandler, mEventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mMediaDataSourceFactory, mMainHandler, mEventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mMediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mMainHandler, mEventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     * Returns a new DataSource factory.MainActivity
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return MediaHelper.buildDataSourceFactory(this, useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private void updateResumePosition() {
        if (mTubiExoPlayer != null) {
            resumeWindow = mTubiExoPlayer.getCurrentWindowIndex();
            resumePosition = mTubiExoPlayer.isCurrentWindowSeekable() ? Math.max(0, mTubiExoPlayer.getCurrentPosition())
                    : C.TIME_UNSET;
        }
    }

    private void clearResumePosition() {
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
