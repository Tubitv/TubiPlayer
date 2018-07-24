package com.tubitv.media.demo.UI;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.tubitv.demo.R;
import com.tubitv.demo.databinding.ExampleUiControlBinding;
import com.tubitv.media.bindings.UserController;
import com.tubitv.media.utilities.ExoPlayerLogger;

public class PlayerControllerUI extends FrameLayout implements View.OnClickListener {

    private static final String TAG = PlayerControllerUI.class.getSimpleName();

    private UserController userController;

    private boolean playOrPause = true;

    ExampleUiControlBinding binding;

    public PlayerControllerUI(final Context context) {
        this(context, null);
    }

    public PlayerControllerUI(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerControllerUI(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    public View setController(UserController controller) {
        this.userController = controller;
        binding.setController(userController);
        return this;
    }

    @Override public void onClick(final View v) {
        if (userController == null) {
            return;
        }
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

        binding = DataBindingUtil
                .inflate(LayoutInflater.from(context), R.layout.example_ui_control, this, true);
        binding.getRoot().findViewById(R.id.rewind).setOnClickListener(this);
        binding.getRoot().findViewById(R.id.play_pause).setOnClickListener(this);
        binding.getRoot().findViewById(R.id.fastford).setOnClickListener(this);

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
