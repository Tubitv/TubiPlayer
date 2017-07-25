package com.tubitv.media.interfaces;

import com.google.android.exoplayer2.source.MediaSource;

/**
 * Created by allensun on 7/24/17.
 * This is a strategy to use two ExoPlayer in layer, one is to show main content, the other one is to show video ad.
 */
public interface DoublePlayerInterface {

    /**
     *  to prepare resource for showing video ads in the secondary ExoPlayer.
     */
    void onPrepareAds(MediaSource ads);

    /**
     * this is the method to call when application want to show a video.
     */
    void showAds();

    /**
     * when the video ads is finished, show release secondary video resource, and pop back to the primary ExoPlayer and its contentView.
     */
    void adShowFinish();

    //TODO: also considering situation when user exist the activity when playing video ads, also should release player resource.
}
