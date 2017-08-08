package com.tubitv.media.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by allensun on 8/3/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public abstract  class AdMediaModel extends MediaModel {

    private List<MediaModel> listOfAds;

    private AdMediaModel(@Nullable String mediaName, @NonNull String videoUrl, @Nullable String artworkUrl, @Nullable String subtitlesUrl, @Nullable String clickThroughUrl, boolean isVpaid) {
        super(mediaName, videoUrl, artworkUrl, subtitlesUrl, clickThroughUrl, true, isVpaid);
    }

    public abstract MediaModel nextMedaiModel();

    public List<MediaModel> getListOfAds() {
        return listOfAds;
    }

    public void setListOfAds(List<MediaModel> listOfAds) {
        this.listOfAds = listOfAds;
    }


}
