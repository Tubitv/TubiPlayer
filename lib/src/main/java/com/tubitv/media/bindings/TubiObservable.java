package com.tubitv.media.bindings;

import android.content.Context;
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
import com.tubitv.media.utilities.Utils;


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
     * The context from {@link com.tubitv.media.views.TubiPlayerControlView}
     */
    @NonNull
    private final Context context;

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
     * The dragging state of the {@link com.tubitv.media.databinding.ViewTubiPlayerControlBinding#viewTubiControllerSeekBar},
     * true when the user is dragging the thumbnail through the video duration
     */
    private boolean draggingSeekBar = false;

    /**
     * The playback state of the {@link #player}
     */
    private int playbackState = ExoPlayer.STATE_IDLE;

    /**
     * The exo player that this controller is for
     */
    private SimpleExoPlayer player;

    public TubiObservable(@NonNull final Context context, @NonNull SimpleExoPlayer player) {
        this.context = context;
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
        if (player != null) {
            boolean playing = player.getPlayWhenReady();
            setIsPlaying(playing);
        }
//        updateProgress();
    }

    @Override
    public void onPositionDiscontinuity() {
//        updateNavigation();
        setPlaybackState();
//        updateProgress();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
//        updateNavigation();
        setPlaybackState();
//        updateProgress();
    }

    /**
     * Updates the playback controlls when the player state changes.
     * ie. The play/pause and loading spinner
     */
    public void onPlaybackState() {
//        boolean playing = player != null && player.getPlayWhenReady();
//        int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();
//        switch (playbackState) {
//            case ExoPlayer.STATE_READY:
//
//                break;
//            case ExoPlayer.STATE_BUFFERING:
//                showLoading(false, false);
//                break;
//            case ExoPlayer.STATE_IDLE:  //nothing to play
//            case ExoPlayer.STATE_ENDED: //stream ended
//                break;
//        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStartTrackingTouch");
//        removeCallbacks(hideAction);
        draggingSeekBar = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged");
        if (fromUser) {
            long position = Utils.progressToMilli(player.getDuration(), seekBar);
            long duration = player == null ? 0 : player.getDuration();
            setElapsedTime(Utils.getProgressTime(position, false));
            setRemainingTime(Utils.getProgressTime(duration - position, true));
            if (player != null && !draggingSeekBar) {
                seekTo(position);
            }
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStopTrackingTouch");
        draggingSeekBar = false;
        if (player != null) {
            seekTo(Utils.progressToMilli(player.getDuration(), seekBar));
        }
//        hideAfterTimeout();
    }

    @Override
    public void onClick(View v) {
        if (player != null) {
            boolean playing = player.getPlayWhenReady();
            player.setPlayWhenReady(!playing);
            setIsPlaying(!playing);
        }
//        hideAfterTimeout();
    }

    private void seekTo(long positionMs) {
        seekTo(player.getCurrentWindowIndex(), positionMs);
    }

    private void seekTo(int windowIndex, long positionMs) {
        player.seekTo(windowIndex, positionMs);
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

//        updateAll();
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
        notifyPropertyChanged(BR.playbackState);
    }

    private void setPlaybackState() {
        setPlaybackState(player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState());
    }
}
