package com.tubitv.media.bindings;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.tubitv.media.helpers.TrackSelectionHelper;
import com.tubitv.media.interfaces.PlaybackActionCallback;
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.ExoPlayerLogger;
import com.tubitv.media.views.TubiExoPlayerView;

import static com.google.android.exoplayer2.ExoPlayer.STATE_ENDED;

/**
 * This class contains business logic of user interaction between user and player action. This class will be serving
 * as interface between Player UI and Business logic, such as seek, pause, UI logic for displaying ads vs movie.
 */
public class UserController extends BaseObservable implements TubiPlaybackControlInterface, ExoPlayer.EventListener {

    private static final String TAG = UserController.class.getSimpleName();

    /**
     * video action states
     */
    public final ObservableInt playerPlaybackState = new ObservableInt(0);

    public final ObservableBoolean isVideoPlayWhenReady = new ObservableBoolean(false);

    /**
     * video information states
     */
    public final ObservableField<String> videoName = new ObservableField("");

    public final ObservableField<Uri> videoPoster = new ObservableField(null);

    public final ObservableField<String> videoMetaData = new ObservableField("");

    public final ObservableLong videoDuration = new ObservableLong(0);

    public final ObservableLong videoCurrentTime = new ObservableLong(0);

    public final ObservableLong videoBufferedPosition = new ObservableLong(0);

    /**
     * Ad information
     */
    public final ObservableField<String> adClickUrl = new ObservableField("");

    public final ObservableField<String> adMetaData = new ObservableField("");

    public final ObservableInt numberOfAdsLeft = new ObservableInt(0);

    public final ObservableBoolean isCurrentVideoAd = new ObservableBoolean(false);

    private Handler mProgressUpdateHandler = new Handler();

    private Runnable updateProgressAction = new Runnable() {
        @Override public void run() {
            updateProgress();
        }
    };

    private static final long DEFAULT_FREQUENCY = 1000;

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

    private TrackSelectionHelper mTrackSelectionHelper;

    /**
     * Every time the {@link com.tubitv.media.fsm.state_machine.FsmPlayer} change states between
     * {@link com.tubitv.media.fsm.concrete.AdPlayingState} and {@link com.tubitv.media.fsm.concrete.MoviePlayingState},
     * current controller instance need to update the video instance.
     *
     * @param mediaModel the current video that will be played by the {@link UserController#mPlayer} instance.
     */
    public void setMediaModel(@NonNull MediaModel mediaModel) {
        if (mediaModel == null) {
            ExoPlayerLogger.e(TAG, "setMediaModel is null");
            return;
        }

        this.mMediaModel = mediaModel;

        if (mMediaModel.isAd()) {

            isCurrentVideoAd.set(true);

            if (!TextUtils.isEmpty(mMediaModel.getClickThroughUrl())) {
                adClickUrl.set(mMediaModel.getClickThroughUrl());
            }

        } else {

            if (!TextUtils.isEmpty(mMediaModel.getMediaName())) {
                videoName.set(mMediaModel.getMediaName());
            }

            if (mMediaModel.getArtworkUrl() != null) {
                videoPoster.set(mMediaModel.getArtworkUrl());
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
    public void setPlayer(@NonNull SimpleExoPlayer player, @NonNull PlaybackActionCallback playbackActionCallback) {
        if (player == null) {
            ExoPlayerLogger.e(TAG, "setPlayer is null");
            return;
        }

        if (this.mPlayer == player) {
            return;
        }

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

    public void setTrackSelectionHelper(@Nullable TrackSelectionHelper trackSelectionHelper) {
        this.mTrackSelectionHelper = trackSelectionHelper;
    }

    @Override public void triggerSubtitlesToggle(final boolean enabled) {

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
    }

    @Override public void triggerQualityTrackToggle() {

        if (mTrackSelectionHelper != null) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = mTrackSelectionHelper.getSelector()
                    .getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                mTrackSelectionHelper.showSelectionDialog(0, null);
            }
        }

        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {
            mPlaybackActionCallback.onQuality(mMediaModel);
        }
    }

    @Override public void seekBy(final long millisecond) {
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

    @Override public void seekTo(final long millisecond) {
        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {
            long currentProgress = mPlayer != null ? mPlayer.getCurrentPosition() : 0;
            mPlaybackActionCallback.onSeek(mMediaModel, currentProgress, millisecond);
        }

        seekToPosition(millisecond);
    }

    @Override public void triggerPlayOrPause(final boolean setPlay) {

        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(setPlay);
            isVideoPlayWhenReady.set(setPlay);
        }

        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {
            mPlaybackActionCallback.onPlayToggle(mMediaModel, setPlay);
        }
    }

    @Override public String getCurrentVideoName() {
        return videoName.get();
    }

    @Override public boolean playReadyToPlay() {
        return isVideoPlayWhenReady.get();
    }

    @Override public boolean isCurrentVideoAd() {
        return isCurrentVideoAd.get();
    }

    @Override public long currentDuration() {
        return videoDuration.get();
    }

    @Override public long currentProgressPosition() {
        return videoCurrentTime.get();
    }

    @Override public long currentBufferPosition() {
        return videoBufferedPosition.get();
    }

    //------------------------------player playback listener-------------------------------------------//

    @Override public void onTimelineChanged(final Timeline timeline, final Object manifest) {
        setPlaybackState();
        updateProgress();
    }

    @Override public void onPositionDiscontinuity() {
        setPlaybackState();
        updateProgress();
    }

    @Override public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
        playerPlaybackState.set(playbackState);
        isVideoPlayWhenReady.set(playWhenReady);
        updateProgress();
    }

    @Override
    public void onTracksChanged(final TrackGroupArray trackGroups, final TrackSelectionArray trackSelections) {

    }

    @Override public void onLoadingChanged(final boolean isLoading) {

    }

    @Override public void onPlayerError(final ExoPlaybackException error) {

    }

    @Override public void onPlaybackParametersChanged(final PlaybackParameters playbackParameters) {
    }

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

        videoCurrentTime.set(position);
        videoDuration.set(duration);
        videoBufferedPosition.set(bufferedPosition);

        ExoPlayerLogger.i(TAG, "updateProgress:----->" + videoCurrentTime.get());

        if (mPlaybackActionCallback != null && mPlaybackActionCallback.isActive()) {
            mPlaybackActionCallback.onProgress(mMediaModel, position, duration);
        }

        mProgressUpdateHandler.removeCallbacks(updateProgressAction);

        // Schedule an update if necessary.
        int playbackState = mPlayer == null ? ExoPlayer.STATE_IDLE : mPlayer.getPlaybackState();
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != STATE_ENDED && mPlaybackActionCallback
                .isActive()) {

            //don't post the updateProgress event when user pause the video
            if (mPlayer != null && !mPlayer.getPlayWhenReady()) {
                return;
            }

            long delayMs = DEFAULT_FREQUENCY;
            mProgressUpdateHandler.postDelayed(updateProgressAction, delayMs);
        }
    }
}
