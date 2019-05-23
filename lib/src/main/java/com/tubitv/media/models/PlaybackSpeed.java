package com.tubitv.media.models;

import android.content.Context;

import com.tubitv.media.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Playback Speed in percent.
 * e.g.
 * 1.0f == normal speed
 * 0.75f == three-quarter of normal speed
 * 2.0f == double normal speed
 */
public enum PlaybackSpeed {

    A_QUARTER(R.string.playback_speed_a_quarter, 0.25f),
    A_HALF(R.string.playback_speed_a_half, 0.5f),
    THREE_QUARTER(R.string.playback_speed_three_quarter, 0.75f),
    NORMAL(R.string.playback_speed_normal, 1f),
    ONE_AND_A_QUARTER(R.string.playback_speed_one_and_a_quarter, 1.25f),
    ONE_AND_A_HALF(R.string.playback_speed_one_and_a_half, 1.5f),
    ONE_AND_THREE_QUARTER(R.string.playback_speed_one_and_three_quarter, 1.75f),
    TWO(R.string.playback_speed_two, 2f);

    private final int stringResourceId;
    private final float speed;

    PlaybackSpeed(int stringResourceId, float speed) {
        this.stringResourceId = stringResourceId;
        this.speed = speed;
    }

    public int getStringResourceId() {
        return stringResourceId;
    }

    public String getText(Context context) {
        return context.getResources().getString(stringResourceId);
    }

    public float getSpeed() {
        return speed;
    }

    public static ArrayList<PlaybackSpeed> getAllPlaybackSpeedEnums() {
        return new ArrayList<>(Arrays.asList(PlaybackSpeed.class.getEnumConstants()));
    }
}
