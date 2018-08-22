package com.tubitv.media.bindings;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.tubitv.media.R;
import com.tubitv.media.interfaces.PlaybackActionCallback;
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.ExoPlayerLogger;
import com.tubitv.media.utilities.PlayerDeviceUtils;
import com.tubitv.media.utilities.SeekCalculator;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiExoPlayerView;

import static com.google.android.exoplayer2.ExoPlayer.STATE_ENDED;

/**
 * This class contains business logic of user interaction between user and player action. This class will be serving
 * as interface between Player UI and Business logic, such as seek, pause, UI logic for displaying ads vs movie.
 */
public class UserController extends BaseObservable
        implements TubiPlaybackControlInterface, ExoPlayer.EventListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = UserController.class.getSimpleName();

    public static final int NORMAL_CONTROL_STATE = 1;
    public static final int CUSTOM_SEEK_CONTROL_STATE = 2; // Every time long press left/right will enter this state
    public static final int EDIT_CUSTOM_SEEK_CONTROL_STATE = 3; // After long press left/right will enter this state
    public static final int OPTIONS_CONTROL_STATE = 4; // Every time focus on caption button will enter this state

    private static final long DEFAULT_FREQUENCY = 1000;

    /**
     * video action states
     */
    public final ObservableInt playerPlaybackState = new ObservableInt(ExoPlayer.STATE_IDLE);

    public final ObservableBoolean isVideoPlayWhenReady = new ObservableBoolean(false);

    /**
     * video information states
     */
    public final ObservableField<String> videoName = new ObservableField("");

    public final ObservableField<Uri> videoPoster = new ObservableField(null);

    public final ObservableField<String> videoMetaData = new ObservableField("");

    public final ObservableField<Long> videoDuration = new ObservableField<>(0l);

    public final ObservableField<Long> videoCurrentTime = new ObservableField<>(0l);

    public final ObservableField<Long> videoBufferedPosition = new ObservableField<>(0l);

    public final ObservableField<String> videoRemainInString = new ObservableField<>("");

    public final ObservableField<String> videoPositionInString = new ObservableField<>("");

    public final ObservableField<Boolean> videoHasSubtitle = new ObservableField<>(false);

    /**
     * Ad information
     */
    public final ObservableField<String> adClickUrl = new ObservableField("");

    public final ObservableField<String> adMetaData = new ObservableField("");

    public final ObservableInt numberOfAdsLeft = new ObservableInt(0);

    public final ObservableField<Boolean> isCurrentAd = new ObservableField<>(false);

    /**
     * user interaction attribute
     */
    public final ObservableField<Boolean> isSubtitleEnabled = new ObservableField<>(false);

    public final ObservableField<Boolean> isDraggingSeekBar = new ObservableField<>(false);

    private Handler mProgressUpdateHandler = new Handler();

    private Runnable updateProgressAction = () -> updateProgress();
    private Runnable mOnControlStateChange;

    /**
     * the Exoplayer instance which this {@link UserController} is controlling.
     */
    private SimpleExoPlayer mPlayer;

    /**
     * this is the current mediaModel being played, it could be a ad or actually video
     */
    private MediaModel mMediaModel;

    private PlaybackActionCallback mPlaybackActionCallback;

    private TubiExoPlayerView mTubiExoPlayerView;

    private int mControlState = NORMAL_CONTROL_STATE;

    /**
     * Every time the {@link com.tubitv.media.fsm.state_machine.FsmPlayer} change states between
     * {@link com.tubitv.media.fsm.concrete.AdPlayingState} and {@link com.tubitv.media.fsm.concrete.MoviePlayingState},
     * current controller instance need to update the video instance.
     *
     * @param mediaModel the current video that will be played by the {@link UserController#mPlayer} instance.
     */
    public void setMediaModel(@NonNull MediaModel mediaModel, Context context) {
        if (mediaModel == null) {
            ExoPlayerLogger.e(TAG, "setMediaModel is null");
            return;
        }

        this.mMediaModel = mediaModel;

        //mark flag for ads to movie
        isCurrentAd.set(mMediaModel.isAd());

        if (mMediaModel.isAd()) {

            if (!PlayerDeviceUtils.isTVDevice(context)
                    && !TextUtils.isEmpty(mMediaModel.getClickThroughUrl())) {
                adClickUrl.set(mMediaModel.getClickThroughUrl());
            }

            videoName.set(context.getString(R.string.commercial));

            videoHasSubtitle.set(false);

        } else {

            if (!TextUtils.isEmpty(mMediaModel.getMediaName())) {
                videoName.set(mMediaModel.getMediaName());
            }

            if (!PlayerDeviceUtils.isTVDevice(context) // Disable artwork for TV
                    && mMediaModel.getArtworkUrl() != null) {
                videoPoster.set(mMediaModel.getArtworkUrl());
            }

            if (mMediaModel.getSubtitlesUrl() != null) {
                videoHasSubtitle.set(true);
            }

        }

    }

    /**
     * Every time the {@link com.tubitv.media.fsm.state_machine.FsmPlayer} change states between
     * {@link com.tubitv.media.fsm.concrete.AdPlayingState} and {@link com.tubitv.media.fsm.concrete.MoviePlayingState},
     * {@link UserController#mPlayer} instance need to update .
     *
     * @param player the current player that is playing the video
     */
    public void setPlayer(@NonNull SimpleExoPlayer player, @NonNull PlaybackActionCallback playbackActionCallback,
            @NonNull TubiExoPlayerView tubiExoPlayerView) {
        if (player == null || tubiExoPlayerView == null) {
            ExoPlayerLogger.e(TAG, "setPlayer is null");
            return;
        }

        if (this.mPlayer == player) {
            return;
        }

        mTubiExoPlayerView = tubiExoPlayerView;

        //remove the old listener
        if (mPlayer != null) {
            this.mPlayer.removeListener(this);
        }

        this.mPlayer = player;

        mPlayer.addListener(this);
        playerPlaybackState.set(mPlayer.getPlaybackState());
        mPlaybackActionCallback = playbackActionCallback;
        updateProgress();

    }

    public void setAvailableAdLeft(int count) {
        numberOfAdsLeft.set(count);
    }

    public void updateTimeTextViews(long position, long duration) {
        //translate the movie remaining time number into display string, and update the UI
        videoRemainInString.set(Utils.getProgressTime((duration - position), true));
        videoPositionInString.set(Utils.getProgressTime(position, false));
    }

    public void togglePlayPause() {
        triggerPlayOrPause(!isVideoPlayWhenReady.get());
    }

    public void fastForward() {
        seekBy(SeekCalculator.FAST_SEEK_INTERVAL);
    }

    public void fastRewind() {
        seekBy(SeekCalculator.REWIND_DIRECTION * SeekCalculator.FAST_SEEK_INTERVAL);
    }

    /**
     * Get current player control state
     *
     * @return Current control state
     */
    public int getState() {
        return mControlState;
    }

    /**
     * Set current player state
     */
    public void setState(final int state) {
        mControlState = state;

        if (mOnControlStateChange != null) {
            mOnControlStateChange.run();
        }
    }

    /**
     * Check if it is during custom seek
     *
     * @return True if custom seek is performing
     */
    public boolean isDuringCustomSeek() {
        return mControlState == CUSTOM_SEEK_CONTROL_STATE || mControlState == EDIT_CUSTOM_SEEK_CONTROL_STATE;
    }

    /**
     * Set callback for control state change
     *
     * @param onControlStateChange Callback to run when control state change
     */
    public void setOnControlStateChange(final Runnable onControlStateChange) {
        mOnControlStateChange = onControlStateChange;
    }

    @Override
    public void triggerSubtitlesToggle(final boolean enabled) {

        if (mTubiExoPlayerView == null) {
            ExoPlayerLogger.e(TAG, "triggerSubtitlesToggle() --> tubiExoPlayerView is null");
            return;
        }

        //trigger the hide or show subtitles.
        View subtitles = mTubiExoPlayerView.getSubtitleView();
        if (subtitles != null) {
            subtitles.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }

        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {
            mPlaybackActionCallback.onSubtitles(mMediaModel, enabled);
        }

        isSubtitleEnabled.set(enabled);
    }

    @Override
    public void seekBy(final long millisecond) {
        if (mPlayer == null) {
            ExoPlayerLogger.e(TAG, "seekBy() ---> player is empty");
            return;
        }

        long currentPosition = mPlayer.getCurrentPosition();
        long seekPosition = currentPosition + millisecond;

        //lower bound
        seekPosition = seekPosition < 0 ? 0 : seekPosition;
        //upper bound
        seekPosition = seekPosition > mPlayer.getDuration() ? mPlayer.getDuration() : seekPosition;

        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {

            mPlaybackActionCallback.onSeek(mMediaModel, currentPosition, seekPosition);
        }

        seekToPosition(seekPosition);
    }

    @Override
    public void seekTo(final long millisecond) {
        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {
            long currentProgress = mPlayer != null ? mPlayer.getCurrentPosition() : 0;
            mPlaybackActionCallback.onSeek(mMediaModel, currentProgress, millisecond);
        }

        seekToPosition(millisecond);
    }

    @Override
    public void triggerPlayOrPause(final boolean setPlay) {

        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(setPlay);
            isVideoPlayWhenReady.set(setPlay);
        }

        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {
            mPlaybackActionCallback.onPlayToggle(mMediaModel, setPlay);
        }
    }

    @Override
    public void clickCurrentAd() {
        if (mPlaybackActionCallback == null) {
            ExoPlayerLogger.w(TAG, "clickCurrentAd params is null");
            return;
        }

        mPlaybackActionCallback.onLearnMoreClick(mMediaModel);
    }

    @Override
    public String getCurrentVideoName() {
        return videoName.get();
    }

    @Override
    public boolean isPlayWhenReady() {
        return isVideoPlayWhenReady.get();
    }

    @Override
    public boolean isCurrentVideoAd() {
        return isCurrentAd.get();
    }

    @Override
    public long currentDuration() {
        return videoDuration.get();
    }

    @Override
    public long currentProgressPosition() {
        return videoCurrentTime.get();
    }

    @Override
    public long currentBufferPosition() {
        return videoBufferedPosition.get();
    }

    //------------------------------player playback listener-------------------------------------------//

    @Override
    public void onTimelineChanged(final Timeline timeline, final Object manifest, final int reason) {
        setPlaybackState();
        updateProgress();
    }

    @Override
    public void onPositionDiscontinuity(final int reason) {
        setPlaybackState();
        updateProgress();
    }

    @Override
    public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
        playerPlaybackState.set(playbackState);
        isVideoPlayWhenReady.set(playWhenReady);
        updateProgress();
    }

    @Override
    public void onRepeatModeChanged(final int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(final boolean shuffleModeEnabled) {

    }

    @Override
    public void onTracksChanged(final TrackGroupArray trackGroups, final TrackSelectionArray trackSelections) {
        ExoPlayerLogger.i(TAG, "onTracksChanged");
    }

    @Override
    public void onLoadingChanged(final boolean isLoading) {
        ExoPlayerLogger.i(TAG, "onLoadingChanged");
    }

    @Override
    public void onPlayerError(final ExoPlaybackException error) {
        ExoPlayerLogger.i(TAG, "onPlayerError");
    }

    @Override
    public void onPlaybackParametersChanged(final PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {

    }

    //-----------------------------------------SeekBar listener--------------------------------------------------------------//
    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

        if (fromUser) {
            long position = Utils.progressToMilli(mPlayer.getDuration(), seekBar);
            long duration = mPlayer == null ? 0 : mPlayer.getDuration();
            updateTimeTextViews(position, duration);
        }
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
        isDraggingSeekBar.set(true);
        ExoPlayerLogger.i(TAG, "onStartTrackingTouch");
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {

        if (mPlayer != null) {
            seekTo(Utils.progressToMilli(mPlayer.getDuration(), seekBar));
        }

        isDraggingSeekBar.set(false);
        ExoPlayerLogger.i(TAG, "onStopTrackingTouch");
    }

    //---------------------------------------private method---------------------------------------------------------------------------//

    private void setPlaybackState() {
        int playBackState = mPlayer == null ? ExoPlayer.STATE_IDLE : mPlayer.getPlaybackState();
        playerPlaybackState.set(playBackState);
    }

    private void seekToPosition(long positionMs) {
        if (mPlayer != null) {
            mPlayer.seekTo(mPlayer.getCurrentWindowIndex(), positionMs);
        }
    }

    private void updateProgress() {

        long position = mPlayer == null ? 0 : mPlayer.getCurrentPosition();
        long duration = mPlayer == null ? 0 : mPlayer.getDuration();
        long bufferedPosition = mPlayer == null ? 0 : mPlayer.getBufferedPosition();

        //only update the seekBar UI when user are not interacting, to prevent UI interference
        if (!isDraggingSeekBar.get() && !isDuringCustomSeek()) {
            updateSeekBar(position, duration, bufferedPosition);
            updateTimeTextViews(position, duration);
        }

        ExoPlayerLogger.i(TAG, "updateProgress:----->" + videoCurrentTime.get());

        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {
            mPlaybackActionCallback.onProgress(mMediaModel, position, duration);
        }

        mProgressUpdateHandler.removeCallbacks(updateProgressAction);

        // Schedule an update if necessary.
        if (playerPlaybackState.get() != ExoPlayer.STATE_IDLE && playerPlaybackState.get() != STATE_ENDED
                && mPlaybackActionCallback
                .isActive()) {

            //don't post the updateProgress event when user pause the video
            if (mPlayer != null && !mPlayer.getPlayWhenReady()) {
                return;
            }

            long delayMs = DEFAULT_FREQUENCY;
            mProgressUpdateHandler.postDelayed(updateProgressAction, delayMs);
        }
    }

    private void updateSeekBar(long position, long duration, long bufferedPosition) {
        //update progressBar.
        videoCurrentTime.set(position);
        videoDuration.set(duration);
        videoBufferedPosition.set(bufferedPosition);
    }

}
