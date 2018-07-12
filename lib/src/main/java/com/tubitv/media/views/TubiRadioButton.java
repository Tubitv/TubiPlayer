package com.tubitv.media.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
        mBinding.tubiRadioButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.tubiRadioButton.setChecked(!mBinding.tubiRadioButton.isChecked());
                TubiRadioButton.this.callOnClick();
            }
        });
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

    /**
     * Checks the checked state of the radio button in this view {@link ViewTubiRadioButtonBinding#tubiRadioButton}
     *
     * @return True if it is checked
     */
    public boolean isChecked() {
        return mBinding.tubiRadioButton.isChecked();
    }

    /**
     * Sets the checked state of radio button in this view {@link ViewTubiRadioButtonBinding#tubiRadioButton}
     *
     * @param checked True if it should be checked, false otherwise
     */
    public void setChecked(boolean checked) {
        mBinding.tubiRadioButton.setChecked(checked);
    }
}
