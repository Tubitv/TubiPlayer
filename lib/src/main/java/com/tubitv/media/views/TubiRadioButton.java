package com.tubitv.media.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.tubitv.media.R;

import com.tubitv.media.databinding.ViewTubiRadioButtonBinding;

/**
 * Created by stoyan on 6/2/17.
 */
public class TubiRadioButton extends LinearLayout {
    /**
     * Attached state of this view
     */
    private boolean isAttachedToWindow;

    private ViewTubiRadioButtonBinding mBinding;

    public TubiRadioButton(Context context) {
        this(context, null);
    }

    public TubiRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TubiRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Skipping...
        if (isInEditMode()) {
            return;
        }

        initLayout();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
    }

    private void initLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.view_tubi_radio_button, this, true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }

    /**
     * Sets the text on the {@link ViewTubiRadioButtonBinding#tubiRadioButtonText}
     *
     * @param textResId The resource id of the string to set
     */
    public void setText(@StringRes int textResId) {
        setText(getResources().getString(textResId));
    }

    /**
     * Sets the text on the {@link ViewTubiRadioButtonBinding#tubiRadioButtonText}
     *
     * @param text The string to set
     */
    public void setText(@NonNull String text) {
        mBinding.tubiRadioButtonText.setText(text);
    }
}
