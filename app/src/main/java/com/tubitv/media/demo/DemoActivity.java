package com.tubitv.media.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tubitv.media.activities.TubiPlayerActivity;
import com.tubitv.media.models.MediaModel;

import static android.content.ContentValues.TAG;

public class DemoActivity extends TubiPlayerActivity {

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
    }
}
