package com.tubitv.media.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tubitv.media.activities.TubiPlayerActivity;
import com.tubitv.media.models.MediaModel;

public class DemoActivity extends TubiPlayerActivity {
    private final static String TAG = DemoActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onProgress(@Nullable MediaModel mediaModel, long milliseconds) {
        if (mediaModel != null) {
            Log.d(TAG, "playback progress media url: " + mediaModel.getVideoUrl());
        }
        Log.d(TAG, "playback progress millis: " + milliseconds);
//        stop runnable on pause
    }

    @Override
    public void onSeek(@Nullable MediaModel mediaModel, long oldPositionMillis, long newPositionMillis) {
        Log.d(TAG, "playback seek : " + oldPositionMillis + " to " + newPositionMillis);
//        figure out timings
    }
}
