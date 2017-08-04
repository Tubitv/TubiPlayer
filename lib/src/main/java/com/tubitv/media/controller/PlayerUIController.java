package com.tubitv.media.controller;

import android.view.View;
import android.webkit.WebView;

import com.google.android.exoplayer2.ExoPlayer;

/**
 * Created by allensun on 8/3/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class PlayerUIController {

    private ExoPlayer contentPlayer;

    private ExoPlayer adPlayer;

    private WebView vpaidWebView;

    private View exoPlayerView;

    public PlayerUIController(ExoPlayer contentPlayer, ExoPlayer adPlayer, WebView vpaidWebView, View exoPlayerView) {
        this.contentPlayer = contentPlayer;
        this.adPlayer = adPlayer;
        this.vpaidWebView = vpaidWebView;
        this.exoPlayerView = exoPlayerView;
    }

    public ExoPlayer getContentPlayer() {
        return contentPlayer;
    }

    public ExoPlayer getAdPlayer() {
        return adPlayer;
    }

    public WebView getVpaidWebView() {
        return vpaidWebView;
    }

    public View getExoPlayerView() {
        return exoPlayerView;
    }

    public void setContentPlayer(ExoPlayer contentPlayer) {
        this.contentPlayer = contentPlayer;
    }

    public void setAdPlayer(ExoPlayer adPlayer) {
        this.adPlayer = adPlayer;
    }

    public void setVpaidWebView(WebView vpaidWebView) {
        this.vpaidWebView = vpaidWebView;
    }

    public void setExoPlayerView(View exoPlayerView) {
        this.exoPlayerView = exoPlayerView;
    }

    public static class Builder {

        private ExoPlayer contentPlayer = null;

        private ExoPlayer adPlayer = null;

        private WebView vpaidWebView = null;

        private View exoPlayerView = null;

        public Builder() {
        }

        public Builder setContentPlayer(ExoPlayer contentPlayer) {
            this.contentPlayer = contentPlayer;
            return this;
        }

        public Builder setAdPlayer(ExoPlayer adPlayer) {
            this.adPlayer = adPlayer;
            return this;
        }

        public Builder setVpaidWebView(WebView vpaidWebView) {
            this.vpaidWebView = vpaidWebView;
            return this;
        }

        public Builder setExoPlayerView(View exoPlayerView) {
            this.exoPlayerView = exoPlayerView;
            return this;
        }

        public PlayerUIController build() {

            return new PlayerUIController(contentPlayer, adPlayer, vpaidWebView, exoPlayerView);
        }
    }

}
