package com.tubitv.media.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.tubitv.media.R;
import com.tubitv.media.bindings.UserController;
import com.tubitv.media.utilities.ExoPlayerLogger;

public class UIControllerView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = UIControllerView.class.getSimpleName();

    private UserController userController;

    private boolean playOrPause = true;

    public UIControllerView(final Context context) {
        this(context, null);
    }

    public UIControllerView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UIControllerView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    public UIControllerView setUserController(UserController userController) {
        this.userController = userController;
        return this;
    }

    @Override public void onClick(final View v) {
        switch ((String) v.getTag()) {
            case "rewind":

                userController.seekBy(-15 * 1000);
                ExoPlayerLogger.i(TAG, "rewind click");
                printVideoDetail();
                break;

            case "play_pause":

                playOrPause = !playOrPause;
                userController.triggerPlayOrPause(playOrPause);

                ExoPlayerLogger.i(TAG, "play_pause click");
                printVideoDetail();
                break;

            case "fastford":

                userController.seekBy(15 * 1000);
                ExoPlayerLogger.i(TAG, "fastford click");
                printVideoDetail();
                break;

            default:
                return;
        }

    }

    private void initLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.ui_controller_view, this);
        findViewById(R.id.rewind).setOnClickListener(this);
        findViewById(R.id.play_pause).setOnClickListener(this);
        findViewById(R.id.fastford).setOnClickListener(this);
    }

    private void printVideoDetail() {
        StringBuilder builder = new StringBuilder();

        builder.append(userController.getCurrentVideoName());
        builder.append("--");
        builder.append("current_duration-->");
        builder.append(userController.currentDuration());
        builder.append(" current_progress-->");
        builder.append(userController.currentProgressPosition());

        ExoPlayerLogger.i(TAG, builder.toString());
    }

}
