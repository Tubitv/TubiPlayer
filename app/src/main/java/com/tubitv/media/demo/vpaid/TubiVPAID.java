package com.tubitv.media.demo.vpaid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import com.tubitv.demo.BuildConfig;

import java.util.Locale;


/**
 * Helper class that contains all the different details we need to play VPAID ads. VPAID ads are
 * designed for mobile-web, so we have to switch into a webview and load a tubitv.com URL that
 * contains the necessary javascript to play vpaid ads.
 * <p>
 * Includes the JS Interface added to a WebView that is meant to play a VPAID ad which helps the
 * JS code communicate with the android code and vice-versa
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
public class TubiVPAID {
    private static final String TAG = "VPAIDAD";
    //    private TubiPlayerEvents mPlayerEvents;
    private WebView mVPAIDWebView;
    private Handler mHandler;
    private String vastXml = Vastxml.getAdXmlBody();
//    private Ad mAd;

    /**
     * @param webView the webview that will be playing the VPAID ad
     */
    public TubiVPAID(@NonNull WebView webView, @NonNull Handler handler) {
        mVPAIDWebView = webView;
        mHandler = handler;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(21)
    public void init() {
        // setup the webview we need if we want to load vpaid
        mVPAIDWebView.setWebViewClient(new VPAidWebViewClient());
        mVPAIDWebView.setWebChromeClient(new VPAIDWebChromeClient());
        mVPAIDWebView.getSettings().setJavaScriptEnabled(true);
        mVPAIDWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        mVPAIDWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    public void reset() {
        mVPAIDWebView.clearHistory();
        mVPAIDWebView.loadUrl("about:blank");
        mVPAIDWebView.setVisibility(View.GONE);
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public void notifyVideoEnd() {
        Log.e(TAG, "VPAID Ad Completed");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                reset();
            }
        });

        //TODO: Ask the Androids if the below needs to be done async on another thread
//        if (mPlayerEvents != null) {
//            mPlayerEvents.onComplete();
//        }
    }

    /**
     * Called by the WebView JS video player whenever an ad error event is raised on the Javascript
     * side. We log these errors to track down bad creatives.
     *
     * @param code  usually an HTTP error code or -1 if a code was not available
     * @param error Error message from the player
     */
    @JavascriptInterface
    @SuppressWarnings("unused")
    public void notifyAdError(int code, String error) {
        // make the error comma separated for easy parsing
        String errorMsg = String.format(Locale.US, "AdId:%s,SiId:%s,Code:%d,Message:\"%s\"",
                code,
                TextUtils.isEmpty(error) ? "No msg" : error);

        Log.e(TAG, "Error playing VPAID Ad: " + errorMsg);

//        try {
//            TubiTvService.UnifiedApiWithoutAuthorization service
//                    = new TubiTvService(TubiApplication.getInstance().getApplicationContext()).getUnifiedApiWithoutAuthorization();
//            JsonObject content = new JsonObject();
//            content.addProperty("platform", TubiTvService.API_ANDROID_PLATFORM);
//            content.addProperty("device_id", TubiApplication.getInstance().getAppUUID());
//            content.addProperty("user_id", UserAuthHelper.getUserId());
//            content.addProperty("type", "AD:ERROR");
//            content.addProperty("level", "error");
//            content.addProperty("subtype", "VPAID");
//            content.addProperty("message", errorMsg);
//            content.addProperty("version", BuildConfig.VERSION_NAME);
//            service.createLog(content, new ResponseCallback() {
//                @Override
//                public void success(Response response) {
//                }
//
//                @Override
//                public void failure(RetrofitError error) {
//                    TubiLog.e(error, "Failed to log VPAID ad error issue.");
//                }
//            });
//        } catch (Exception ex) {
//            TubiLog.e(ex);
//        }
    }

    @JavascriptInterface
    @SuppressWarnings("unused")
    public String getVastXml() {
        return vastXml;
    }

    private class VPAidWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // force links to be loaded inside the webview
            view.loadUrl(url);
            return true;
        }

        /**
         * Fires in cases where the webview cannot even load (e.g. no network)
         */
        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            if (failingUrl.equalsIgnoreCase("about:blank")) // not a real error
                return;

            String errorMsg = String.format(Locale.US,
                    "Error loading webview to play VPAID ad. Code=%d, Msg=%s",
                    errorCode,
                    description);
            Log.e(TAG, errorMsg);
            reset();
//            if (mPlayerEvents != null)
//                mPlayerEvents.onComplete();
        }

        /**
         * Fires in cases where the webview cannot even load (e.g. no network)
         */
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            onReceivedError(view,
                    error.getErrorCode(),
                    error.getDescription().toString(),
                    request.getUrl().toString());
        }
    }


    /**
     * If we ever want to support real full screen playback in a webview, this is where we need
     * to do it
     */
    private class VPAIDWebChromeClient extends WebChromeClient {
        private View mVideoProgressView;

        public VPAIDWebChromeClient() {
        }

        /**
         * @param progressView the view to show while a video is loading
         */
        public VPAIDWebChromeClient(WebView view, View progressView) {
            mVideoProgressView = progressView;
        }

        @Override
        public View getVideoLoadingProgressView() {
            return mVideoProgressView;
        }

        /**
         * We override this here to keep taps on VPAID ads that abuse their power
         *
         * @param request details around the permission request, see {@link PermissionRequest}
         */
        @Override
        public void onPermissionRequest(PermissionRequest request) {
            if (Build.VERSION.SDK_INT >= 21)
                request.deny();
        }
    }
}
