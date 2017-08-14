package com.tubitv.media.interfaces;

import com.tubitv.media.models.AdMediaModel;

/**
 * Created by allensun on 7/24/17.
 * This is a strategy to use two ExoPlayer in layer, one is to show main content, the other one is to show video ad.
 */
public interface DoublePlayerInterface {

    /**
     * prepare the {@link AdMediaModel} to have the {@link com.google.android.exoplayer2.source.MediaSource} insert into it.
     *
     * @param ads the adMediaModel.
     */
    void onPrepareAds(AdMediaModel ads);

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
