package com.tubitv.media.bindings;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.BR;

/**
 * The observable class for the {@link com.tubitv.media.views.TubiPlayerControlViewOld}
 * <p>
 * Created by stoyan on 5/12/17.
 */
public class TubiObservable extends BaseObservable {

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
     * The exo player that this controller is for
     */
    private SimpleExoPlayer player;

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

}
