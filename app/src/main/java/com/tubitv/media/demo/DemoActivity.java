package com.tubitv.media.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
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
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.tubitv.demo.R;
import com.tubitv.media.MediaHelper;
import com.tubitv.media.TubiExoPlayer;
import com.tubitv.media.views.TubiExoPlayerView;
import com.tubitv.media.views.TubiPlayerControlView;

public class DemoActivity extends Activity implements TubiPlayerControlView.VisibilityListener {
    private TubiExoPlayer mTubiExoPlayer;
    private Handler mMainHandler;
    private TubiExoPlayerView mTubiPlayerView;
    private DataSource.Factory mMediaDataSourceFactory;
    private TrackSelector mTrackSelector;
    private EventLogger mEventLogger;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private boolean shouldAutoPlay;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideSystemUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        shouldAutoPlay = true;
        mMediaDataSourceFactory = buildDataSourceFactory(true);
        initLayout();
    }

    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        shouldAutoPlay = true;
//        clearResumePosition();
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

    private void initLayout() {
        setContentView(R.layout.activity_tubi_player);
        mTubiPlayerView = (TubiExoPlayerView) findViewById(R.id.tubitv_player);
//        mTubiPlayerView.setControllerVisibilityListener(this);
        mTubiPlayerView.requestFocus();
        mTubiPlayerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });
    }

    private void setupExo() {
        // 1. Create a default TrackSelector
        mMainHandler = new Handler();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        mTrackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the mTubiExoPlayer
        mTubiExoPlayer =
                TubiExoPlayer.newInstance(this, mTrackSelector, loadControl);

        mEventLogger = new EventLogger((MappingTrackSelector) mTrackSelector);
        mTubiExoPlayer.addListener(mEventLogger);
        mTubiExoPlayer.setAudioDebugListener(mEventLogger);
        mTubiExoPlayer.setVideoDebugListener(mEventLogger);
        mTubiExoPlayer.setMetadataOutput(mEventLogger);

        mTubiPlayerView.setPlayer(mTubiExoPlayer);
        mTubiPlayerView.setControllerVisibilityListener(this);
        mTubiExoPlayer.setPlayWhenReady(shouldAutoPlay);

        //fake media
        Uri[] uris = new Uri[1];
        String[] extensions = new String[1];
        uris[0] = Uri.parse("http://c11.adrise.tv/v2/sources/content-owners/lionsgate/348932/v201703040118-,225,447,725,1157,1398,k.mp4.m3u8?E5EbI8Bi9XArFsKGvbclx_jKPZ69_sP5XDYOD8UquF3rHsgJgH1kct26QAJGXxmy");
        extensions[0] = "m3u8";
        MediaSource[] mediaSources = new MediaSource[uris.length];
        mediaSources[0] = buildMediaSource(uris[0], extensions[0]);
        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                : new ConcatenatingMediaSource(mediaSources);


        MediaSource subtitleSource = new SingleSampleMediaSource(
                Uri.parse("http://s.adrise.tv/f89566c4-64e9-4f54-8808-717864bcca64.srt"),
                buildDataSourceFactory(false),
                Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE, C.SELECTION_FLAG_DEFAULT, "en", null, 0),
                0);
        // Plays the video with the sideloaded subtitle.
        MergingMediaSource mergedSource =
                new MergingMediaSource(mediaSource, subtitleSource);


        mTubiExoPlayer.prepare(mergedSource, true, false);
    }

    private void releasePlayer() {
        if (mTubiExoPlayer != null) {
            shouldAutoPlay = mTubiExoPlayer.getPlayWhenReady();
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

    // This snippet hides the system bars.
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        int uiState = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        if (Util.SDK_INT > 18) {
            uiState |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        } else {
            final Handler handler = new Handler();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility == View.VISIBLE) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                hideSystemUI();
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    }
                }
            });
        }
        decorView.setSystemUiVisibility(uiState);
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }
}
