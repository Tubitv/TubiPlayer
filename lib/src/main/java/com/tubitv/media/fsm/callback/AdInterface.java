package com.tubitv.media.fsm.callback;

/**
 * Created by allensun on 8/2/17.
 */
public interface AdInterface {

    /**
     * Call the the ad server to retrieve adbreak
     * @param videoId id of the current video
     * @param videoPublisherId publicsher id of the current video
     * @param positionMs one of the list of cue point returned by the server.
     */
    void fetchAd(String videoId, String videoPublisherId, long positionMs);

}
