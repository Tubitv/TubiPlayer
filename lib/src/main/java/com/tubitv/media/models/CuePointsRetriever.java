package com.tubitv.media.models;

/**
 * Created by allensun on 8/17/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class CuePointsRetriever {

    private String videoId;

    private String publisherId;

    public CuePointsRetriever() {
    }

    public CuePointsRetriever(String videoId, String publisherId) {
        this.videoId = videoId;
        this.publisherId = publisherId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }
}
