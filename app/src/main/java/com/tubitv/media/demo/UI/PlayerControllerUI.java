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
import com.tubitv.media.demo.enums.ScaleMode;
import com.tubitv.media.demo.presenters.ScalePresenter;
import com.tubitv.media.utilities.ExoPlayerLogger;

public class PlayerControllerUI extends FrameLayout implements View.OnClickListener {

    private static final String TAG = PlayerControllerUI.class.getSimpleName();

    private UserController mUserController;

    private boolean playOrPause = true;

    ExampleUiControlBinding binding;

    private ScalePresenter mScalePresenter;

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
        this.mUserController = controller;
        binding.setController(mUserController);
        mScalePresenter = new ScalePresenter(getContext(), mUserController);
        binding.videoScaleButton.setText(mScalePresenter.getCurrentScaleMode().getDescription());
        return this;
    }

    @Override public void onClick(final View v) {
        if (mUserController == null) {
            return;
        }
        switch ((String) v.getTag()) {
            case "rewind":

                mUserController.seekBy(-15 * 1000);
                ExoPlayerLogger.i(TAG, "rewind click");
                printVideoDetail();
                break;

            case "play_pause":

                playOrPause = !playOrPause;
                mUserController.triggerPlayOrPause(playOrPause);

                ExoPlayerLogger.i(TAG, "play_pause click");
                printVideoDetail();
                break;

            case "fastford":

                mUserController.seekBy(15 * 1000);
                ExoPlayerLogger.i(TAG, "fastford click");
                printVideoDetail();
                break;

            case "scale":
                mScalePresenter.doScale();
                ScaleMode scaleMode = mScalePresenter.getCurrentScaleMode();
                binding.videoScaleButton.setText(scaleMode.getDescription());
                break;

            case "speedx2":
                mUserController.setPlaybackSpeed(2.0f);
                break;
            case "speedx4":
                mUserController.setPlaybackSpeed(4.0f);
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
        binding.videoScaleButton.setOnClickListener(this);
        binding.videoSpeedX2Button.setOnClickListener(this);
        binding.videoSpeedX4Button.setOnClickListener(this);
    }

    private void printVideoDetail() {
        StringBuilder builder = new StringBuilder();

        builder.append(mUserController.getCurrentVideoName());
        builder.append("--");
        builder.append("current_duration-->");
        builder.append(mUserController.currentDuration());
        builder.append(" current_progress-->");
        builder.append(mUserController.currentProgressPosition());

        ExoPlayerLogger.i(TAG, builder.toString());

    }

}
