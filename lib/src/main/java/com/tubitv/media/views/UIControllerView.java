package com.tubitv.media.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.Player;
import com.tubitv.media.R;
import com.tubitv.media.bindings.UserController;
import com.tubitv.media.databinding.UiControllerViewBinding;
import com.tubitv.media.utilities.ExoPlayerLogger;

public class UIControllerView extends FrameLayout {

    private static final String TAG = UIControllerView.class.getSimpleName();

    private static final int TIME_TO_HIDE_CONTROL = 3000;
    private UserController userController;

    private UiControllerViewBinding binding;

    private Handler countdownHandler;

    private Runnable hideUIAction = new Runnable() {
        @Override
        public void run() {
            binding.controllerPanel.setVisibility(GONE);
        }
    };

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
        if (userController == null) {
            ExoPlayerLogger.w(TAG, "setUserController()--> param passed in null");
            return null;
        }

        this.userController = userController;

        if (binding != null) {
            binding.setController(userController);
        }
        return this;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ExoPlayerLogger.w(TAG, "onDetachedFromWindow");
        countdownHandler.removeCallbacks(hideUIAction);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        countdownHandler.removeCallbacks(hideUIAction);

        if (binding.controllerPanel.getVisibility() == VISIBLE) {
            binding.controllerPanel.setVisibility(GONE);
        } else {
            if (userController.playerPlaybackState.get() != Player.STATE_IDLE) {
                binding.controllerPanel.setVisibility(VISIBLE);
                hideUiTimeout();
            }
        }

        return super.onTouchEvent(event);
    }

    private void initLayout(Context context) {
        binding = DataBindingUtil
                .inflate(LayoutInflater.from(context), R.layout.ui_controller_view, this, true);
        countdownHandler = new Handler();
    }

    private void hideUiTimeout() {
        countdownHandler.postDelayed(hideUIAction, TIME_TO_HIDE_CONTROL);
    }

}
