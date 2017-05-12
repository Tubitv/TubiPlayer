package com.tubitv.media.bindings;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import com.tubitv.media.BR;

/**
 * The observable class for the {@link com.tubitv.media.views.TubiPlayerControlView}
 * <p>
 * Created by stoyan on 5/12/17.
 */
public class TubiObservable extends BaseObservable {

    /**
     * The title of the movie being played
     */
    @NonNull
    private String mTitle;

    /**
     * The remaining time of the movie in hh:mm:ss
     */
    @NonNull
    private String mRemainingTime;

    /**
     * The elapsed time of the movie in hh:mm:ss
     */
    @NonNull
    private String mElapsedTime;

    /**
     * The play state of the video
     */
    private boolean mIsPlaying;

    /**
     * The state of the subtitles, true if they have been enabled in any language
     */
    private boolean mSubtitlesEnabled;

    /**
     * The state of the quality settings, true if user has defined a quality;
     * false if the quality is on auto
     */
    private boolean mQualityEnabled;

    @Bindable
    @NonNull
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(@NonNull String title) {
        this.mTitle = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    @NonNull
    public String getRemainingTime() {
        return mRemainingTime;
    }

    public void setRemainingTime(@NonNull String remainingTime) {
        this.mRemainingTime = remainingTime;
        notifyPropertyChanged(BR.remainingTime);
    }

    @Bindable
    @NonNull
    public String getElapsedTime() {
        return mElapsedTime;
    }

    public void setElapsedTime(@NonNull String elapsedTime) {
        this.mElapsedTime = elapsedTime;
        notifyPropertyChanged(BR.elapsedTime);
    }

    @Bindable
    public boolean isIsPlaying() {
        return mIsPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.mIsPlaying = isPlaying;
        notifyPropertyChanged(BR.isPlaying);
    }

    @Bindable
    public boolean isSubtitlesEnabled() {
        return mSubtitlesEnabled;
    }

    public void setSubtitlesEnabled(boolean subtitlesEnabled) {
        this.mSubtitlesEnabled = subtitlesEnabled;
        notifyPropertyChanged(BR.subtitlesEnabled);
    }
    @Bindable
    public boolean isQualityEnabled() {
        return mQualityEnabled;
    }

    public void setQualityEnabled(boolean qualityEnabled) {
        this.mQualityEnabled = qualityEnabled;
        notifyPropertyChanged(BR.qualityEnabled);
    }

}
