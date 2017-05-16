package com.tubitv.media.bindings;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.SeekBar;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.BR;
import com.tubitv.media.R;

/**
 * The observable class for the {@link com.tubitv.media.views.TubiPlayerControlViewOld}
 * <p>
 * Created by stoyan on 5/12/17.
 */
public class TubiObservable extends BaseObservable implements SeekBar.OnSeekBarChangeListener{
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
     * The exo player that this controller is for
     */
    private SimpleExoPlayer player;

    public TubiObservable(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStartTrackingTouch");
//        removeCallbacks(hideAction);
//        dragging = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged");
//        if (fromUser) {
//            long position = positionValue(progress);
//            long duration = player == null ? 0 : player.getDuration();
//            setProgressTime(position, duration);
//            if (player != null && !dragging) {
//                seekTo(position);
//            }
//        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStopTrackingTouch");
        //Reset thumb, or smaller unpressed drawable will get blown up
        seekBar.setThumb(context.getResources().getDrawable(R.drawable.tubi_tv_drawable_scrubber_selector));
//        dragging = false;
        if (player != null) {
//            seekTo(positionValue(seekBar.getProgress()));
        }
//        hideAfterTimeout();
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

}
