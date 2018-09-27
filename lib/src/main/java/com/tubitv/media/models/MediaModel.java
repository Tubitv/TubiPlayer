package com.tubitv.media.models;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import java.io.Serializable;

/**
 * Created by stoyan on 6/5/17.
 */
public class MediaModel implements Serializable {
    private static final String TAG = MediaModel.class.getSimpleName();
    private static final String LANGUAGE = "en";

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

    public boolean isVpaid() {
        return isVpaid;
    }

    public boolean isDRM() {
        return false;
    }

    public void buildMediaSourceIfNeeded(
            final android.os.Handler handler,
            final DataSource.Factory videoFactory,
            final DataSource.Factory nonVideoFactory,
            final com.tubitv.media.utilities.EventLogger eventLogger) {

        if (this.mediaSource != null) { // Prevent regenerating MediaSource
            return;
        }

        MediaSource mediaSource;
        int type = TextUtils.isEmpty(getMediaExtension()) ? Util.inferContentType(getVideoUrl())
                : Util.inferContentType("." + getMediaExtension());

        //        if (!isAds) { // TODO parse video format correctly
        //            type = C.TYPE_DASH;
        //        }

        // TODO: Replace deprecated constructors with proper factory
        switch (type) {
            case C.TYPE_SS:
                mediaSource = new SsMediaSource(getVideoUrl(), nonVideoFactory,
                        new DefaultSsChunkSource.Factory(videoFactory), handler, eventLogger);
                break;
            case C.TYPE_DASH:
                mediaSource = new DashMediaSource(getVideoUrl(), nonVideoFactory,
                        new DefaultDashChunkSource.Factory(videoFactory), handler, eventLogger);
                break;
            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource(getVideoUrl(), videoFactory, handler, eventLogger);
                break;
            case C.TYPE_OTHER:
                mediaSource = new ExtractorMediaSource(getVideoUrl(), videoFactory, new DefaultExtractorsFactory(),
                        handler, eventLogger);
                break;
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }

        if (getSubtitlesUrl() != null) {
            MediaSource subtitleSource = new SingleSampleMediaSource(
                    getSubtitlesUrl(),
                    nonVideoFactory,
                    Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE,
                            C.SELECTION_FLAG_DEFAULT, LANGUAGE, null, 0),
                    0);
            // Plays the video with the sideloaded subtitle.
            mediaSource =
                    new MergingMediaSource(mediaSource, subtitleSource);
        }

        this.mediaSource = mediaSource;
    }
}
