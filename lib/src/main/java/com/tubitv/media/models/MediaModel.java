package com.tubitv.media.models;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.exoplayer2.source.MediaSource;
import java.io.Serializable;

/**
 * Created by stoyan on 6/5/17.
 */
public class MediaModel implements Serializable {

    /**
     * The url of the media
     */
    @NonNull
    private final String videoUrl;

    /**
     * The title of the media to display
     */
    @Nullable
    private final String mediaName;

    /**
     * The url of the artwork to display while loading
     */
    @Nullable
    private final String artworkUrl;

    /**
     * The nullable subtitles that we sideload for the main media source
     */
    @Nullable
    private final String subtitlesUrl;

    /**
     * The nullable click through url for this media if its an ad
     *
     * @see #isAd
     */
    @Nullable
    private final String clickThroughUrl;

    /**
     * The media source representation of this model
     */
    private MediaSource mediaSource;

    /**
     * Whether this media is an ad or not
     */
    private boolean isAd;

    /**
     * Whether this media is an ad or not
     */
    private boolean isVpaid;

    public MediaModel(@Nullable String mediaName, @NonNull String videoUrl, @Nullable String artworkUrl,
            @Nullable String subtitlesUrl, @Nullable String clickThroughUrl, boolean isAd, boolean isVpaid) {
        this.mediaName = mediaName;
        this.videoUrl = videoUrl;
        this.artworkUrl = artworkUrl;
        this.subtitlesUrl = subtitlesUrl;
        this.clickThroughUrl = clickThroughUrl;
        this.isAd = isAd;
        this.isVpaid = isVpaid;
    }

    public static MediaModel video(@NonNull String mediaName, @NonNull String videoUrl, @NonNull String artworkUrl,
            @Nullable String subtitlesUrl) {
        return new MediaModel(mediaName, videoUrl, artworkUrl, subtitlesUrl, null, false, false);
    }

    public static MediaModel ad(@NonNull String videoUrl, @Nullable String clickThroughUrl, boolean isVpaid) {
        return new MediaModel(null, videoUrl, null, null, clickThroughUrl, true, isVpaid);
    }

    @Nullable
    public String getMediaName() {
        return mediaName;
    }

    @NonNull
    public Uri getVideoUrl() {
        return Uri.parse(videoUrl);
    }

    @Nullable
    public Uri getArtworkUrl() {
        return Uri.parse(artworkUrl);
    }

    @Nullable
    public Uri getSubtitlesUrl() {
        return subtitlesUrl != null ? Uri.parse(subtitlesUrl) : null;
    }

    @Nullable
    public String getClickThroughUrl() {
        return clickThroughUrl;
    }

    public boolean isAd() {
        return isAd;
    }

    public String getMediaExtension() {
        return "m3u8";
    }

    public MediaSource getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(MediaSource mediaSource) {
        this.mediaSource = mediaSource;
    }

    public boolean isVpaid() {
        return isVpaid;
    }
}
