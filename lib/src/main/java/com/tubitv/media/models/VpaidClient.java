package com.tubitv.media.models;

import android.webkit.JavascriptInterface;

/**
 * Created by allensun on 9/14/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public interface VpaidClient {

    void init(MediaModel adMediaModel);

    @JavascriptInterface
    void notifyAdError(int code, String error);

    @JavascriptInterface
    void notifyVideoEnd();

    @JavascriptInterface
    String getVastXml();
}
