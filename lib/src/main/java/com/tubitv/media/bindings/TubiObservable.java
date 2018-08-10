package com.tubitv.media.bindings;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.tubitv.media.BR;
import com.tubitv.media.interfaces.PlayerConsumer;
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;
import com.tubitv.media.interfaces.TubiPlaybackInterface;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.StateImageButton;

import static com.google.android.exoplayer2.ExoPlayer.STATE_ENDED;
import static com.google.android.exoplayer2.ExoPlayer.STATE_READY;

/**
 * The observable class for the {@link com.tubitv.media.views.TubiPlayerControlView}
 * Created by stoyan on 5/12/17.
 */
public class TubiObservable extends BaseObservable
        implements ExoPlayer.EventListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener,
        View.OnClickListener {
    /**
     * Toggle tag for {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerPlayToggleIb}
     */
    public static final String TUBI_PLAY_TOGGLE_TAG = "TUBI_PLAY_TOGGLE_TAG";
    /**
     * Subtitle tag for {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerSubtitlesIb}
     */
    public static final String TUBI_SUBTITLES_TAG = "TUBI_SUBTITLES_TAG";
    /**
     * Subtitle tag for {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerQualityIb}
     */
    public static final String TUBI_QUALITY_TAG = "TUBI_QUALITY_TAG";
    /**
     * Subtitle tag for {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerAdDescription}
     */
    public static final String TUBI_AD_LEARN_MORE_TAG = "TUBI_AD_LEARN_MORE_TAG";
    /**
     * Tag for logging
     */
    private static final String TAG = TubiObservable.class.getSimpleName();
    /**
     * The max progress bar value
     */
    private static final int DEFAULT_PROGRESS_MAX = 1000;
    /**
     * The default time to skip in {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerForwardIb}
     * and {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerRewindIb}
     */
    public static final int DEFAULT_FAST_FORWARD_MS = 15000;
    public final ObservableField<String> numberOfAdLeft = new ObservableField<>("");
    /**
     * The interface from {@link com.tubitv.media.views.TubiPlayerControlView}
     */
    @NonNull
    private final TubiPlaybackControlInterface playbackControlInterface;
    /**
     * The interface from the calling activity for hooking general media playback state
     */
    @NonNull
    private final TubiPlaybackInterface playbackInterface;
    /**
     * The title of the movie being played
     */
    @NonNull
    public String title;
    /**
     * The time in milliseconds that we skip by in {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerRewindIb}
     * and {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerForwardIb}
     */
    public int skipBy = DEFAULT_FAST_FORWARD_MS;
    /**
     * The remaining time of the movie in hh:mm:ss
     */
    @NonNull
    private String remainingTime;
    /**
     * The elapsed time of the movie in hh:mm:ss
     */
    @NonNull
    private String elapsedTime;
    /**
     * The play state of the video
     */
    private boolean isPlaying;
    /**
     * The state of the subtitles, true if they have been enabled in any language
     */
    private boolean subtitlesEnabled;
    /**
     * The state of the media source tracks, if there are no subtitle tracks, hide the view
     */
    private boolean subtitlesExist;
    /**
     * The state of the quality settings, true if user has defined a quality;
     * false if the quality is on auto
     */
    private boolean qualityEnabled;
    /**
     * The max for the progress bar
     */
    private int progressBarMax = DEFAULT_PROGRESS_MAX;
    /**
     * The dragging state of the {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerSeekBar},
     * true when the user is dragging the thumbnail through the video duration
     */
    private boolean draggingSeekBar = false;
    /**
     * The value of the progress bar {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerSeekBar}
     */
    private long progressBarValue;
    /**
     * The secondary value of the progress bar {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerSeekBar}
     */
    private long secondaryProgressBarValue;
    /**
     * The playback state of the {@link #player}
     */
    private int playbackState = ExoPlayer.STATE_IDLE;
    /**
     * The current ad index
     */
    private int adIndex = 0;
    /**
     * The total number of ads for this break
     */
    private int adTotal = 0;
    /**
     * Whether the player is currently playing an ad
     */
    private boolean adPlaying;
    /**
     * The currently playing media model
     */
    @Nullable
    private MediaModel mediaModel;
    /**
     * The exo player that this controller is for
     */
    private SimpleExoPlayer player;
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    public TubiObservable(@NonNull SimpleExoPlayer player,
            @NonNull final TubiPlaybackControlInterface playbackControlInterface,
            @NonNull final TubiPlaybackInterface playbackInterface) {
        this.playbackControlInterface = playbackControlInterface;
        this.playbackInterface = playbackInterface;
        setPlayer(player);
        setAdPlaying(false);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        setPlaybackState(playbackState);
        setIsPlaying();
        updateProgress();
    }

    @Override public void onRepeatModeChanged(final int repeatMode) {
        
    }

    @Override public void onShuffleModeEnabledChanged(final boolean shuffleModeEnabled) {

    }

    @Override
    public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
        setPlaybackState();
        updateProgress();
        //        updateMedia();
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override public void onSeekProcessed() {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, @Player.TimelineChangeReason int reason) {
        setPlaybackState();
        updateProgress();
        //        updateMedia();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        setDraggingSeekBar(true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            long position = Utils.progressToMilli(player.getDuration(), seekBar);
            long duration = player == null ? 0 : player.getDuration();
            setProgressSeekTime(position, duration);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (player != null) {
            seekTo(Utils.progressToMilli(player.getDuration(), seekBar));
        }
        setDraggingSeekBar(true);
        playbackControlInterface.hideAfterTimeout();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //override on touch of the seek bar for ads
        return true;
    }

    @Override
    public void onClick(View view) {
        switch ((String) view.getTag()) {
            case TUBI_PLAY_TOGGLE_TAG:
                if (isDuringCustomSeek()) {
                    confirmCustomSeek();
                    if (!isPlaying) {
                        togglePlay();
                        playbackControlInterface.hideAfterTimeout();
                    }
                } else {
                    togglePlay();
                    playbackControlInterface.hideAfterTimeout();
                }

                break;
            case TUBI_SUBTITLES_TAG:
                boolean enabled = ((StateImageButton) view).isChecked();
                playbackControlInterface.onSubtitlesToggle(((StateImageButton) view).isChecked());
                if (mediaModel != null) {
                    playbackInterface.onSubtitles(mediaModel, enabled);
                }
                break;
            case TUBI_QUALITY_TAG:
                playbackControlInterface.onQualityTrackToggle(((StateImageButton) view).isChecked());
                if (mediaModel != null) {
                    playbackInterface.onQuality(mediaModel);
                }
                break;
            case TUBI_AD_LEARN_MORE_TAG:
                if (mediaModel != null) {
                    playbackInterface.onLearnMoreClick(mediaModel);
                }
                break;
        }

    }

    public void setPlayer(SimpleExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(this);
        }
        this.player = player;
        if (player != null) {
            player.addListener(this);

            boolean playing = player.getPlayWhenReady();
            setIsPlaying(playing);
        }

        updateAll();
    }

    private void seekTo(long positionMs) {
        if (player != null) {
            seekTo(player.getCurrentWindowIndex(), positionMs);
        }
    }

    private void seekTo(int windowIndex, long positionMs) {
        if (player != null) {
            playbackInterface.onSeek(mediaModel,
                    player.getCurrentPosition(), positionMs);

            player.seekTo(windowIndex, positionMs);
        }
    }

    public void togglePlay() {
        if (player != null) {
            boolean playing = player.getPlayWhenReady();
            player.setPlayWhenReady(!playing);
            if (mediaModel != null) {
                playbackInterface.onPlayToggle(mediaModel, !playing);
            }
        }
        setIsPlaying();
    }

    public void seekBy(long timeMillis) {
        if (player != null) {
            long position = player.getCurrentPosition();
            long place = position + timeMillis;
            //lower bound
            place = place < 0 ? 0 : place;
            //upper bound
            place = place > player.getDuration() ? player.getDuration() : place;
            seekTo(place);
            setDraggingSeekBar(true);

            //Set the progress bar and text to the new position
            long duration = player == null ? 0 : player.getDuration();
            setProgressBarMax((int) duration);
            setProgressSeekTime(place, duration);
            setProgressBarValue(position);
        }
    }

    public static final int NORMAL_CONTROL_STATE = 1;
    public static final int CUSTOM_SEEK_CONTROL_STATE = 2; // Every time long press left/right will enter this state
    public static final int EDIT_CUSTOM_SEEK_CONTROL_STATE = 3; // After long press left/right will enter this state
    public static final int OPTIONS_CONTROL_STATE = 4; // Every time focus on caption button will enter this state

    private Long mCustomSeekPosition = null;
    private int mControlState = NORMAL_CONTROL_STATE;
    private Runnable mOnEnterCustomSeek;
    private Runnable mOnBackFromCustomSeek;
    private Runnable mOnControlStateChange;
    private PlayerConsumer<Long> mOnCustomSeek;

    /**
     * Set callback for enter custom seek
     *
     * @param onEnterCustomSeek Callback to run when enter custom seek
     */
    public void setOnEnterCustomSeek(final Runnable onEnterCustomSeek) {
        mOnEnterCustomSeek = onEnterCustomSeek;
    }

    /**
     * Set callback for back from custom seek
     *
     * @param onBackFromCustomSeek Callback to run when back from custom seek
     */
    public void setOnBackFromCustomSeek(final Runnable onBackFromCustomSeek) {
        mOnBackFromCustomSeek = onBackFromCustomSeek;
    }

    /**
     * Set callback for control state change
     *
     * @param onControlStateChange Callback to run when control state change
     */
    public void setOnControlStateChange(final Runnable onControlStateChange) {
        mOnControlStateChange = onControlStateChange;
    }

    /**
     * Set callback for custom seek
     *
     * @param onCustomSeek Callback to run when seekbar move
     */
    public void setOnCustomSeek(final PlayerConsumer<Long> onCustomSeek) {
        mOnCustomSeek = onCustomSeek;
    }

    /**
     * Update player control progress based on time
     *
     * @param seekDelta seek time delta (positive or negative)
     */
    public void updateUIForCustomSeek(final long seekDelta) {
        updateUIForCustomSeek(seekDelta, false);
    }

    /**
     * Update player control progress based on time
     *
     * @param seekDelta     seek time delta (positive or negative)
     * @param fromLongPress True if this update UI come from long press left/right button
     */
    public void updateUIForCustomSeek(final long seekDelta, final boolean fromLongPress) {
        if (seekDelta == 0) {
            return;
        }

        if (player != null) {

            if (mCustomSeekPosition == null) {
                mCustomSeekPosition = player.getCurrentPosition();

                if (mOnEnterCustomSeek != null) {
                    mOnEnterCustomSeek.run();
                }
            }

            if (fromLongPress) {
                setState(CUSTOM_SEEK_CONTROL_STATE);
            }

            mCustomSeekPosition += seekDelta;

            if (mOnCustomSeek != null) {
                mOnCustomSeek.accept(seekDelta);
            }

            mCustomSeekPosition = Math.max(0, mCustomSeekPosition);
            mCustomSeekPosition = Math.min(player.getDuration(), mCustomSeekPosition);

            setProgressBarMax((int) player.getDuration());
            setProgressSeekTime(mCustomSeekPosition, player.getDuration());
            setProgressBarValue(mCustomSeekPosition);
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
     * Confirm current custom seek progress
     */
    public void confirmCustomSeek() {
        seekTo(mCustomSeekPosition);
        setDraggingSeekBar(true);
        mCustomSeekPosition = null;
        setState(NORMAL_CONTROL_STATE);
        if (mOnBackFromCustomSeek != null) {
            mOnBackFromCustomSeek.run();
        }
    }

    /**
     * Cancel custom seek, resume from previous position
     */
    public void cancelCustomSeek() {
        mCustomSeekPosition = null;
        setProgressBarMax((int) player.getDuration());
        setProgressSeekTime(player.getCurrentPosition(), player.getDuration());
        setProgressBarValue(player.getCurrentPosition());
        setState(NORMAL_CONTROL_STATE);
        if (mOnBackFromCustomSeek != null) {
            mOnBackFromCustomSeek.run();
        }
    }

    /**
     * Check if player is playing video
     *
     * @return True if video is playing
     */
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    private void updateProgress() {
        long position = player == null ? 0 : player.getCurrentPosition();
        long duration = player == null ? 0 : player.getDuration();
        setProgressBarMax((int) duration);
        if (!draggingSeekBar && !isDuringCustomSeek()) {
            setProgressSeekTime(position, duration);
            setProgressBarValue(position);
        }
        long bufferedPosition = player == null ? 0 : player.getBufferedPosition();
        setSecondaryProgressBarValue(bufferedPosition);

        playbackControlInterface.cancelRunnable(updateProgressAction);
        // Schedule an update if necessary.
        int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            if (playbackInterface.isActive()) {
                playbackControlInterface.postRunnable(updateProgressAction, delayMs);
            }
        }
    }

    private void setProgressSeekTime(long position, long duration) {
        setElapsedTime(Utils.getProgressTime(position, false));
        setRemainingTime(Utils.getProgressTime(duration - position, true));
    }

    //    private void updateMedia() {
    ////        setAdPlaying(player.getCurrentWindowIndex() < 2);
    //        setMediaModel(MediaHelper.getMediaByIndex(player.getCurrentWindowIndex()));
    //    }

    private void updateAll() {
        setIsPlaying();
        setPlaybackState();
        updateProgress();
        //        updateMedia();
    }

    public boolean userInteracting() {
        return draggingSeekBar;
    }

    private void setIsPlaying() {
        if (player != null) {
            boolean playing = player.getPlayWhenReady();
            setIsPlaying(playing);
        }
    }

    @Bindable
    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    @NonNull
    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(@NonNull String remainingTime) {
        this.remainingTime = remainingTime;
        notifyPropertyChanged(BR.remainingTime);
    }

    @Bindable
    @NonNull
    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(@NonNull String elapsedTime) {
        this.elapsedTime = elapsedTime;
        notifyPropertyChanged(BR.elapsedTime);
    }

    @Bindable
    public boolean isIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
        notifyPropertyChanged(BR.isPlaying);
    }

    @Bindable
    public boolean isSubtitlesEnabled() {
        return subtitlesEnabled;
    }

    public void setSubtitlesEnabled(boolean subtitlesEnabled) {
        this.subtitlesEnabled = subtitlesEnabled;
        notifyPropertyChanged(BR.subtitlesEnabled);
    }

    @Bindable
    public boolean isQualityEnabled() {
        return qualityEnabled;
    }

    public void setQualityEnabled(boolean qualityEnabled) {
        this.qualityEnabled = qualityEnabled;
        notifyPropertyChanged(BR.qualityEnabled);
    }

    @Bindable
    public int getProgressBarMax() {
        return progressBarMax;
    }

    public void setProgressBarMax(int progressBarMax) {
        this.progressBarMax = progressBarMax;
        notifyPropertyChanged(BR.progressBarMax);
    }

    @Bindable
    public int getPlaybackState() {
        return playbackState;
    }

    public void setPlaybackState(int playbackState) {
        this.playbackState = playbackState;
        switch (playbackState) {
            case STATE_READY:
                setDraggingSeekBar(false);
                break;
            case ExoPlayer.STATE_BUFFERING:
            case ExoPlayer.STATE_IDLE:  //nothing to play
            case STATE_ENDED: //stream ended
                break;
        }

        notifyPropertyChanged(BR.playbackState);
    }

    private void setPlaybackState() {
        setPlaybackState(player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState());
    }

    @Bindable
    public boolean isDraggingSeekBar() {
        return draggingSeekBar;
    }

    public void setDraggingSeekBar(boolean draggingSeekBar) {
        this.draggingSeekBar = draggingSeekBar;
        notifyPropertyChanged(BR.draggingSeekBar);
    }

    @Bindable
    public long getProgressBarValue() {
        return progressBarValue;
    }

    public void setProgressBarValue(long progressBarValue) {
        this.progressBarValue = progressBarValue;
        notifyPropertyChanged(BR.progressBarValue);
    }

    @Bindable
    public long getSecondaryProgressBarValue() {
        return secondaryProgressBarValue;
    }

    public void setSecondaryProgressBarValue(long secondaryProgressBarValue) {
        this.secondaryProgressBarValue = secondaryProgressBarValue;
        notifyPropertyChanged(BR.secondaryProgressBarValue);
    }

    @Bindable
    public boolean isSubtitlesExist() {
        return subtitlesExist;
    }

    public void setSubtitlesExist(boolean subtitlesExist) {
        this.subtitlesExist = subtitlesExist;
        notifyPropertyChanged(BR.subtitlesExist);
    }

    @Bindable
    public int getAdIndex() {
        return adIndex;
    }

    public void setAdIndex(int adIndex) {
        this.adIndex = adIndex;
        notifyPropertyChanged(BR.adIndex);
    }

    @Bindable
    public int getAdTotal() {
        return adTotal;
    }

    public void setAdTotal(int adTotal) {
        this.adTotal = adTotal;
        notifyPropertyChanged(BR.adTotal);
    }

    @Bindable
    public MediaModel getMediaModel() {
        return mediaModel;
    }

    public void setMediaModel(MediaModel mediaModel) {
        this.mediaModel = mediaModel;
        notifyPropertyChanged(BR.mediaModel);

        if (mediaModel != null) {
            setAdPlaying(mediaModel.isAd());
        }
    }

    @Bindable
    public boolean isAdPlaying() {
        return adPlaying;
    }

    public void setAdPlaying(boolean adPlaying) {
        this.adPlaying = adPlaying;
        setAdIndex(player.getCurrentWindowIndex() + 1);
        if (player.getCurrentManifest() != null && player.getCurrentManifest().getClass().isArray()) {
            Object[] manifestContainer = ((Object[]) player.getCurrentManifest());
            if (manifestContainer.length > 0 && manifestContainer[0].getClass().isArray()) {
                Object[] array = (Object[]) manifestContainer[0];
                setAdTotal(array.length - 1);
            }
        }
        notifyPropertyChanged(BR.adPlaying);
    }
}

