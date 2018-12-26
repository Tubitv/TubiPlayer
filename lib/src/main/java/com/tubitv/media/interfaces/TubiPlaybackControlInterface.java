package com.tubitv.media.interfaces;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

/**
 * Created by stoyan tubi_tv_quality_on 4/27/17.
 */
public interface TubiPlaybackControlInterface {

    //action control
    void triggerSubtitlesToggle(boolean enabled);

    void seekBy(long millisecond);

    void seekTo(long millisecond);

    void triggerPlayOrPause(boolean setPlay);

    void clickCurrentAd();

    //display control
    String getCurrentVideoName();

    boolean isPlayWhenReady();

    boolean isCurrentVideoAd();

    long currentDuration();

    long currentProgressPosition();

    long currentBufferPosition();

    void setVideoAspectRatio(float widthHeightRatio);

    float getInitVideoAspectRatio();

    void setResizeMode(@AspectRatioFrameLayout.ResizeMode int resizeMode);

    void setPlaybackSpeed(float speed);
}
