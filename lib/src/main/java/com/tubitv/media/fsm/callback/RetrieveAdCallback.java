package com.tubitv.media.fsm.callback;

import com.tubitv.media.models.MediaModel;

import java.util.List;

/**
 * Created by allensun on 8/2/17.
 */
public interface RetrieveAdCallback {

    void onReceiveAd(List<MediaModel> mediaModels);

    void onError();

    void onEmptyAdReceived();
}
