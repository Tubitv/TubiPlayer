package com.tubitv.media.activities;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Rational;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
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
import com.tubitv.media.interfaces.PlaybackActionCallback;
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.EventLogger;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiExoPlayerView;
import java.util.ArrayList;

import static com.tubitv.media.helpers.Constants.PIP_ENABLE_KET;
import static com.tubitv.media.helpers.Constants.PIP_ENABLE_VALUE_DEFAULT;

/**
 * This is the base activity that prepare one instance of {@link SimpleExoPlayer} mMoviePlayer, this player is mean to serve as the main player to player content.
 * Along with some abstract methods to be implemented by subclass for extra functions.
 * You can use this class as it is and implement the abstract methods to be a standalone player to player video with customized UI controls and different forms of adaptive streaming.
 */
public abstract class TubiPlayerActivity extends LifeCycleActivity
        implements PlaybackActionCallback {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    public static String TUBI_MEDIA_KEY = "tubi_media_key";
    protected SimpleExoPlayer mMoviePlayer;
    protected TubiExoPlayerView mTubiPlayerView;
    protected WebView vpaidWebView;
    protected TextView cuePointIndictor;
    protected DefaultTrackSelector mTrackSelector;
    protected boolean isActive = false;
    protected boolean mPIPEnable = PIP_ENABLE_VALUE_DEFAULT;
    /**
     * ideally, only one instance of {@link MediaModel} and its arrtibute {@link MediaSource} for movie should be created throughout the whole movie playing experiences.
     */
    @NonNull
    protected MediaModel mediaModel;
    private Handler mMainHandler;
    private DataSource.Factory mMediaDataSourceFactory;
    private EventLogger mEventLogger;
    /**
     * Intent action for media controls from Picture-in-Picture mode.
     */
    protected static final String ACTION_MEDIA_CONTROL = "media_control";

    /**
     * Intent extra for media controls from Picture-in-Picture mode.
     */
    protected static final String EXTRA_CONTROL_TYPE = "control_type";

    /**
     * The request code for play action PendingIntent.
     */
    protected static final int REQUEST_PLAY = 1;

    /**
     * The request code for pause action PendingIntent.
     */
    protected static final int REQUEST_PAUSE = 2;

    /**
     * The intent extra value for play action.
     */
    protected static final int CONTROL_TYPE_PLAY = 1;

    /**
     * The intent extra value for pause action.
     */
    protected static final int CONTROL_TYPE_PAUSE = 2;
    private BroadcastReceiver mReceiver;

    public abstract View addUserInteractionView();

    protected abstract void onPlayerReady();

    protected abstract void updateResumePosition();

    protected abstract boolean isCaptionPreferenceEnable();

    @RequiresApi(api = Build.VERSION_CODES.O)
    private final PictureInPictureParams.Builder mPictureInPictureParamsBuilder = new PictureInPictureParams.Builder();

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setUpPIPActionReceiver();
        }
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

    @Override
    public void onPictureInPictureModeChanged(final boolean isInPictureInPictureMode, final Configuration newConfig) {
        final View controlView = mTubiPlayerView.getControlView();
        if (isInPictureInPictureMode) {
            if (controlView.getVisibility() == View.VISIBLE) {
                controlView.setVisibility(View.GONE);
            }
            registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
        } else {
            if (controlView.getVisibility() == View.GONE) {
                controlView.setVisibility(View.VISIBLE);
            }
            unregisterReceiver(mReceiver);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void updatePictureInPictureActions(@DrawableRes int iconId, String title, int controlType,
            int requestCode) {
        final ArrayList<RemoteAction> actions = new ArrayList<>();

        final PendingIntent intent =
                PendingIntent.getBroadcast(
                        TubiPlayerActivity.this,
                        requestCode,
                        new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
                        0);
        final Icon icon = Icon.createWithResource(TubiPlayerActivity.this, iconId);
        actions.add(new RemoteAction(icon, title, title, intent));

        mPictureInPictureParamsBuilder.setActions(actions);

        setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
    }

    @SuppressWarnings("ConstantConditions")
    protected void parseIntent() {
        String errorNoMediaMessage = getResources().getString(R.string.activity_tubi_player_no_media_error_message);
        Assertions.checkState(getIntent() != null && getIntent().getExtras() != null,
                errorNoMediaMessage);
        mediaModel = (MediaModel) getIntent().getExtras().getSerializable(TUBI_MEDIA_KEY);
        Assertions.checkState(mediaModel != null,
                errorNoMediaMessage);
        setPIPEnable(getIntent().getBooleanExtra(PIP_ENABLE_KET, PIP_ENABLE_VALUE_DEFAULT));
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

    private void setUpPIPActionReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null
                        || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                    return;
                }

                final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
                switch (controlType) {
                    case CONTROL_TYPE_PLAY:
                        mTubiPlayerView.getPlayerController().triggerPlayOrPause(true);
                        break;
                    case CONTROL_TYPE_PAUSE:
                        mTubiPlayerView.getPlayerController().triggerPlayOrPause(false);
                        break;
                }
            }
        };
    }

    private void setCaption(boolean isOn) {
        if (mediaModel != null && mediaModel.getSubtitlesUrl() != null && mTubiPlayerView != null
                && mTubiPlayerView.getControlView() != null) {
            mTubiPlayerView.getPlayerController().triggerSubtitlesToggle(isOn);
        }
    }

    private void setupExo() {
        initMoviePlayer();
        setCaption(isCaptionPreferenceEnable());
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

        // 3. Create the mMoviePlayer
        mMoviePlayer = ExoPlayerFactory.newSimpleInstance(this, mTrackSelector);

        mEventLogger = new EventLogger(mTrackSelector);
        mMoviePlayer.addAnalyticsListener(mEventLogger);
        mMoviePlayer.addMetadataOutput(mEventLogger);

        mTubiPlayerView.setPlayer(mMoviePlayer, this);
        mTubiPlayerView.setMediaModel(mediaModel);
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

        // TODO: Replace deprecated constructors with proper factory
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
                mediaSource = new HlsMediaSource(model.getVideoUrl(), mMediaDataSourceFactory, mMainHandler,
                        mEventLogger);
                break;
            case C.TYPE_OTHER:
                mediaSource = new ExtractorMediaSource(model.getVideoUrl(), mMediaDataSourceFactory,
                        new DefaultExtractorsFactory(),
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
                    Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE,
                            C.SELECTION_FLAG_DEFAULT, "en", null, 0),
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

    //    @Override
    //    public void onVisibilityChange(int visibility) {
    //
    //    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "TubiPlayerActivity"), bandwidthMeter);
    }

    protected TubiPlaybackControlInterface getPlayerController() {
        if (mTubiPlayerView != null && mTubiPlayerView.getPlayerController() != null) {
            return mTubiPlayerView.getPlayerController();
        }
        return null;
    }

    public boolean isPIPEnable() {
        return mPIPEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public void setPIPEnable(final boolean pIPEnable) {
        this.mPIPEnable = pIPEnable;
    }

    protected void enterPIP(int numerator, int denominator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Rational rational = new Rational(numerator, denominator);
            mPictureInPictureParamsBuilder.setAspectRatio(rational).build();
            enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
    }
}
