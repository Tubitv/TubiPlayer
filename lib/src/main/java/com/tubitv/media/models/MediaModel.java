package com.tubitv.media.models;

import android.support.annotation.NonNull;

/**
 * Created by stoyan on 6/5/17.
 */
public class MediaModel {

    @NonNull
    private final String videoUrl;

    @NonNull
    private final String artworkUrl;

    public MediaModel(@NonNull String videoUrl, @NonNull String artworkUrl) {

        this.videoUrl = videoUrl;
        this.artworkUrl = artworkUrl;
    }

    @NonNull
    public String getVideoUrl() {
        return videoUrl;
    }

    @NonNull
    public String getArtworkUrl() {
        return artworkUrl;
    }
}
