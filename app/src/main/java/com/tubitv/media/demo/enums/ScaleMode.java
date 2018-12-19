package com.tubitv.media.demo.enums;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.*;

public enum ScaleMode {
    MODE_DEFAULT(RESIZE_MODE_FIT, -1, "default"),
    MODE_4_3(RESIZE_MODE_FIT, -1, "4:3"),
    MODE_16_9(RESIZE_MODE_FIT, -1, "16:9"),
    MODE_FULL_SCREEN(RESIZE_MODE_FILL, -1, "full screen"),;

    private int mMode;
    private int mResId;
    private String mDescription;

    ScaleMode(int mode, int restId, String description) {
        mMode = mode;
        mResId = restId;
        mDescription = description;
    }

    public int getMode() {
        return mMode;
    }

    public int getRestId() {
        return mResId;
    }

    public String getDescription() {
        return mDescription;
    }

    public ScaleMode nextMode() {
        ScaleMode[] modes = ScaleMode.values();
        for (int i = 0; i < modes.length; i++) {
            if (this == modes[i]) {
                return modes[(++i) % modes.length];
            }
        }
        return MODE_DEFAULT;
    }
}
