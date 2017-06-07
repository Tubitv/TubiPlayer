package com.tubitv.media.interfaces;

/**
 * Created by stoyan on 6/2/17.
 */
public interface TrackSelectionHelperInterface {

    /**
     * Callback method to inform the caller of {@link com.tubitv.media.helpers.TrackSelectionHelper#showSelectionDialog(int, TrackSelectionHelperInterface)}
     * if the track selector was overridden
     *
     * @param trackSelected True if the track selector was overridden, false if not (ie. its on auto select)
     */
    void onTrackSelected(boolean trackSelected);
}
