package com.tubitv.media.bindings;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.tubitv.media.BR;
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.StateImageButton;

import static com.google.android.exoplayer2.ExoPlayer.STATE_ENDED;
import static com.google.android.exoplayer2.ExoPlayer.STATE_READY;


/**
 * The observable class for the {@link com.tubitv.media.views.TubiPlayerControlViewOld}
 * <p>
 * Created by stoyan on 5/12/17.
 */
public class TubiObservable extends BaseObservable implements ExoPlayer.EventListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {
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
    private static final int DEFAULT_FAST_FORWARD_MS = 15000;

    /**
     * Toggle tag for {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerPlayToggleIb}
     */
    public static final String TUBI_PLAY_TOGGLE_TAG = "TUBI_PLAY_TOGGLE_TAG";

    /**
     * Subtitle tag for {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerSubtitlesIb}
     */
    public static final String TUBI_SUBTITLES_TAG = "TUBI_SUBTITLES_TAG";

    /**
     * The interface from {@link com.tubitv.media.views.TubiPlayerControlView}
     */
    @NonNull
    private final TubiPlaybackControlInterface playbackControlInterface;

    /**
     * The title of the movie being played
     */
    @NonNull
    public String title = "Stoyan the Great";

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
     * The state of the quality settings, true if user has defined a quality;
     * false if the quality is on auto
     */
    private boolean qualityEnabled;

    /**
     * The max for the progress bar
     */
    private int progressBarMax = DEFAULT_PROGRESS_MAX;

    /**
     * The time in milliseconds that we skip by in {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerRewindIb}
     * and {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerForwardIb}
     */
    public int skipBy = DEFAULT_FAST_FORWARD_MS;

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
     * The exo player that this controller is for
     */
    private SimpleExoPlayer player;

    public TubiObservable(@NonNull final TubiPlaybackControlInterface playbackControlInterface, @NonNull SimpleExoPlayer player) {
        this.playbackControlInterface = playbackControlInterface;
        setPlayer(player);

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

    @Override
    public void onPositionDiscontinuity() {
//        updateNavigation();
        setPlaybackState();
        updateProgress();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
//        updateNavigation();
        setPlaybackState();
        updateProgress();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStartTrackingTouch");
//        removeCallbacks(hideAction);
        setDraggingSeekBar(true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged");
        if (fromUser) {
            long position = Utils.progressToMilli(player.getDuration(), seekBar);
            long duration = player == null ? 0 : player.getDuration();
            setProgressSeekTime(position, duration);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStopTrackingTouch");
        if (player != null) {
            seekTo(Utils.progressToMilli(player.getDuration(), seekBar));
        }
        setDraggingSeekBar(true);
        playbackControlInterface.hideAfterTimeout();
    }

    @Override
    public void onClick(View view) {
        switch((String)view.getTag()){
            case TUBI_PLAY_TOGGLE_TAG:
                Log.d(TAG, "Play toggle pressed");
                if (player != null) {
                    boolean playing = player.getPlayWhenReady();
                    player.setPlayWhenReady(!playing);
                }
                setIsPlaying();
                playbackControlInterface.hideAfterTimeout();
                break;
            case TUBI_SUBTITLES_TAG:
                Log.d(TAG, "Subtitles toggle pressed");
                playbackControlInterface.onSubtitlesToggle(((StateImageButton) view).isChecked()) ;
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
            player.seekTo(windowIndex, positionMs);
        }
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

    private void updateProgress() {
        long position = player == null ? 0 : player.getCurrentPosition();
        long duration = player == null ? 0 : player.getDuration();
        setProgressBarMax((int) duration);
        if (!draggingSeekBar) {
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
            playbackControlInterface.postRunnable(updateProgressAction, delayMs);
        }
    }

    private void setProgressSeekTime(long position, long duration) {
        setElapsedTime(Utils.getProgressTime(position, false));
        setRemainingTime(Utils.getProgressTime(duration - position, true));
    }

    private void updateAll(){
        setIsPlaying();
        setPlaybackState();
        updateProgress();
    }

    public boolean userInteracting() {
        return draggingSeekBar;
    }

    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

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
        //(playMedia.playbackState == ExoPlayer.STATE_READY || (playMedia.playbackState == ExoPlayer.STATE_BUFFERING &amp;&amp; playMedia.draggingSeekBar)) ? View.VISIBLE : View.INVISIBLE
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

}
