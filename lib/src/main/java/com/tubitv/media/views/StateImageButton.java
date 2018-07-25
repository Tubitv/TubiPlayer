package com.tubitv.media.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.tubitv.media.R;

@SuppressLint("AppCompatCustomView")
public class StateImageButton extends ImageButton {

    public interface OnAction {
        void onAction();
    }

    /**
     * Remember the checked state of the button
     */
    private boolean isChecked = false;

    /**
     * The checked image state
     */
    @DrawableRes
    private int mStateCheckedDrawableId;

    /**
     * The un-checked image state
     */
    @DrawableRes
    private int mStateNotCheckedDrawableId;

    public StateImageButton(Context context) {
        super(context);
        init(null);
    }

    public StateImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public StateImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @BindingAdapter("bind:setCheckedState")
    public static void onStateChanged(StateImageButton imageButton, boolean checked) {
        imageButton.setChecked(checked);
    }



    /**
     * Initialize all of the drawables and animations as well as apply attributes if set
     *
     * @param attrs
     */
    private void init(@Nullable AttributeSet attrs) {
        mStateCheckedDrawableId = R.drawable.tubi_tv_subtitles_on;
        mStateNotCheckedDrawableId = R.drawable.tubi_tv_subtitles_off;

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
                    R.styleable.StateImageButton, 0, 0);
            try {
                mStateCheckedDrawableId = a.getResourceId(R.styleable.StateImageButton_state_checked,
                        R.drawable.tubi_tv_drawable_subtitles_on_selector);
                mStateNotCheckedDrawableId = a.getResourceId(R.styleable.StateImageButton_state_not_checked,
                        R.drawable.tubi_tv_drawable_subtitles_off_selector);
            } finally {
                a.recycle();
            }
        }

        setDrawableSelector();
    }

    /**
     * Toggle the checked state of this view
     */
    private void toggleCheckState() {
        isChecked = !isChecked;
        setDrawableSelector();
    }

    /**
     * Sets the background drawable assets based on the checked status
     */
    private void setDrawableSelector() {
        if (isChecked) {
            setBackgroundResource(mStateCheckedDrawableId);
        } else {
            setBackgroundResource(mStateNotCheckedDrawableId);
        }
        invalidate();
    }

    /**
     * Returns the checked state of the button
     *
     * @return True if checked, false otherwise
     */
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * Set the checked status
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        isChecked = checked;
        setDrawableSelector();
    }

}
