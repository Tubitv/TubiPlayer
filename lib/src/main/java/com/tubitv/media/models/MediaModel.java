package com.tubitv.media.models;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by stoyan on 6/5/17.
 */
public class MediaModel implements Serializable{

    /**
     * The url of the media
     */
    @NonNull
    private final String videoUrl;

    /**
     * The title of the media to display
     */
    @NonNull
    private final String mediaName;

    /**
     * The url of the artwork to display while loading
     */
    @NonNull
    private final String artworkUrl;

    /**
     * The nullable subtitles that we sideload for the main media source
     */
    @Nullable
    private final String subtitlesUrl;

    public MediaModel(@NonNull String mediaName, @NonNull String videoUrl, @NonNull String artworkUrl, @Nullable String subtitlesUrl) {
        this.mediaName = mediaName;
        this.videoUrl = videoUrl;
        this.artworkUrl = artworkUrl;
        this.subtitlesUrl = subtitlesUrl;
    }

    @NonNull
    public String getMediaName() {
        return mediaName;
    }

    @NonNull
    public Uri getVideoUrl() {
        return Uri.parse(videoUrl);
    }

    @NonNull
    public Uri getArtworkUrl() {
        return Uri.parse(artworkUrl);
    }

    @Nullable
    public Uri getSubtitlesUrl() {
        return subtitlesUrl != null ? Uri.parse(subtitlesUrl) : null;
    }
}
