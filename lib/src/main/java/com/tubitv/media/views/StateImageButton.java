package com.tubitv.media.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.tubitv.media.R;

/**
 * Created by stoyan tubi_tv_quality_on 5/1/17.
 */
@SuppressLint("AppCompatCustomView")
public class StateImageButton extends ImageButton implements View.OnClickListener {
    /**
     * Remember the checked state of the button
     */
    private boolean isChecked = false;

    @DrawableRes
    private int mStateCheckedDrawableId;

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

    @Override
    public void onClick(View v) {
        toggleCheckState();
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
                mStateCheckedDrawableId = a.getResourceId(R.styleable.StateImageButton_state_checked, R.drawable.tubi_tv_drawable_subtitles_on_selector);
                mStateNotCheckedDrawableId = a.getResourceId(R.styleable.StateImageButton_state_not_checked, R.drawable.tubi_tv_drawable_subtitles_off_selector);
            } finally {
                a.recycle();
            }
        }

        setDrawableSelector();
        setOnClickListener(this);

//        if (isInEditMode()) {
//            return;
//        }


    }

    private void toggleCheckState() {
        isChecked = !isChecked;
        setDrawableSelector();
    }

    /**
     *
     */
    private void setDrawableSelector() {
        if(isChecked){
            setBackgroundResource(mStateCheckedDrawableId);
        }else{
            setBackgroundResource(mStateNotCheckedDrawableId);
        }
        invalidate();
    }

    private void setChecked(boolean checked){
        isChecked = checked;
        setDrawableSelector();
    }

}
