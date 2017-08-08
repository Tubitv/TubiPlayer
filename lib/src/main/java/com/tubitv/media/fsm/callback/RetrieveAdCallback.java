package com.tubitv.media.fsm.callback;

import com.tubitv.media.models.AdMediaModel;

/**
 * Created by allensun on 8/2/17.
 */
public interface RetrieveAdCallback {

    void onReceiveAd(AdMediaModel mediaModels);

    void onError();

    void onEmptyAdReceived();
}
