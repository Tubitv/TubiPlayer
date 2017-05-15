package com.tubitv.media.views;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.tubitv.media.R;
import com.tubitv.media.databinding.ViewTubiPlayerControlBinding;

/**
 * Created by stoyan on 5/15/17.
 */
public class TubiPlayerControlView extends ConstraintLayout {
    /**
     * The default time to hide the this view if the user is not interacting with it
     */
    private static final int DEFAULT_HIDE_TIMEOUT_MS = 5000;

    /**
     * The time out time for the view to be hidden if the user is not interacting with it
     */
    private int mHideTimeoutMs = DEFAULT_HIDE_TIMEOUT_MS;

    private ViewTubiPlayerControlBinding mBinding;

    public TubiPlayerControlView(Context context) {
        this(context, null);
    }

    public TubiPlayerControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TubiPlayerControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Skipping...
        if (isInEditMode() || attrs == null) {
            return;
        }

        initLayout();
    }

    private void initLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.view_tubi_player_control, this, true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        mBinding.setController(this);
//        mBinding.viewTubiControllerSeekBar.setOnSeekBarChangeListener(componentListener);
//        mBinding.viewTubiControllerSeekBar.setMax(PROGRESS_BAR_MAX);
//        mBinding.viewTubiControllerPlayToggleIb.addClickListener(componentListener);
    }

    @BindingAdapter("bind:tubi_hide_timeout_ms")
    public static void setCustomTypeface(TubiPlayerControlView tubiController, int milliseconds) {
        tubiController.setHideTimeoutMs(milliseconds);
    }

    public void setHideTimeoutMs(int hideTimeoutMs) {
        this.mHideTimeoutMs = hideTimeoutMs;
    }
}
