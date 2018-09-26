package com.tubitv.media.controller;

import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.utilities.PlayerDeviceUtils;

/**
 * Created by allensun on 8/3/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class PlayerUIController {

    public boolean isPlayingAds = false;

    private SimpleExoPlayer contentPlayer;

    private SimpleExoPlayer adPlayer;

    private WebView vpaidWebView;

    private View exoPlayerView;

    private int adResumeWindow = C.INDEX_UNSET;

    private long adResumePosition = C.TIME_UNSET;

    private int movieResumeWindow = C.INDEX_UNSET;

    private long movieResumePosition = C.TIME_UNSET;

    private boolean hasHistory = false;

    private long historyPosition = C.TIME_UNSET;

    public PlayerUIController() {
    }

    public PlayerUIController(@Nullable SimpleExoPlayer contentPlayer, @Nullable SimpleExoPlayer adPlayer,
            @Nullable WebView vpaidWebView, @Nullable View exoPlayerView) {
        this.contentPlayer = contentPlayer;
        this.adPlayer = adPlayer;
        this.vpaidWebView = vpaidWebView;
        this.exoPlayerView = exoPlayerView;
    }

    public SimpleExoPlayer getContentPlayer() {
        return contentPlayer;
    }

    public void setContentPlayer(SimpleExoPlayer contentPlayer) {
        this.contentPlayer = contentPlayer;
    }

    public SimpleExoPlayer getAdPlayer() {
        // We'll reuse content player to play ads for single player instance case
        if (PlayerDeviceUtils.useSinglePlayer()) {
            return contentPlayer;
        }
        return adPlayer;
    }

    public void setAdPlayer(SimpleExoPlayer adPlayer) {
        this.adPlayer = adPlayer;
    }

    public WebView getVpaidWebView() {
        return vpaidWebView;
    }

    public void setVpaidWebView(WebView vpaidWebView) {
        this.vpaidWebView = vpaidWebView;
    }

    public View getExoPlayerView() {
        return exoPlayerView;
    }

    public void setExoPlayerView(View exoPlayerView) {
        this.exoPlayerView = exoPlayerView;
    }

    /**
     * This is set when user want to begin the movie from current position
     *
     * @param pos
     */
    public void setPlayFromHistory(long pos) {
        hasHistory = true;
        historyPosition = pos;
    }

    public boolean hasHistory() {
        return hasHistory;
    }

    public long getHistoryPosition() {
        return historyPosition;
    }

    public void clearHistoryRecord() {
        hasHistory = false;
        historyPosition = C.TIME_UNSET;
    }

    public void setAdResumeInfo(int window, long position) {
        adResumeWindow = window;
        adResumePosition = position;
    }

    public void clearAdResumeInfo() {
        setAdResumeInfo(C.INDEX_UNSET, C.TIME_UNSET);
    }

    public void setMovieResumeInfo(int window, long position) {
        movieResumeWindow = window;
        movieResumePosition = position;
    }

    public void clearMovieResumeInfo() {
        setMovieResumeInfo(C.INDEX_UNSET, C.TIME_UNSET);
    }

    public int getAdResumeWindow() {
        return adResumeWindow;
    }

    public long getAdResumePosition() {
        return adResumePosition;
    }

    public int getMovieResumeWindow() {
        return movieResumeWindow;
    }

    public long getMovieResumePosition() {
        return movieResumePosition;
    }

    public static class Builder {

        private SimpleExoPlayer contentPlayer = null;

        private SimpleExoPlayer adPlayer = null;

        private WebView vpaidWebView = null;

        private View exoPlayerView = null;

        public Builder() {
        }

        public Builder setContentPlayer(SimpleExoPlayer contentPlayer) {
            this.contentPlayer = contentPlayer;
            return this;
        }

        public Builder setAdPlayer(SimpleExoPlayer adPlayer) {
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
