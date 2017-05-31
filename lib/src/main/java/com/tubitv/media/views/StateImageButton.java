package com.tubitv.media.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.tubitv.media.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A image button that can transition between multiple states
 *
 * Created by stoyan tubi_tv_quality_on 5/1/17.
 */
@SuppressLint("AppCompatCustomView")
public class StateImageButton extends ImageButton implements View.OnClickListener {
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

    /**
     * A list of {@link android.view.View.OnClickListener} to call when this view is clicked
     */
    private List<OnClickListener> mOnClickListeners = new ArrayList<>();

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
        for(OnClickListener listener : mOnClickListeners){
            listener.onClick(v);
        }
    }

    /**
     * Binding adapter to add a {@link android.view.View.OnClickListener} to this views
     * {@link #mOnClickListeners}
     *
     * @param imageButton The view
     * @param listener The listener to add
     */
    @BindingAdapter("bind:onClickStateImage")
    public static void onClickStateImage(StateImageButton imageButton, OnClickListener listener){
        imageButton.addClickListener(listener);
    }

    @BindingAdapter("bind:tubi_state_set_checked")
    public static void onStateChanged(StateImageButton imageButton, boolean checked){
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
                mStateCheckedDrawableId = a.getResourceId(R.styleable.StateImageButton_state_checked, R.drawable.tubi_tv_drawable_subtitles_on_selector);
                mStateNotCheckedDrawableId = a.getResourceId(R.styleable.StateImageButton_state_not_checked, R.drawable.tubi_tv_drawable_subtitles_off_selector);
            } finally {
                a.recycle();
            }
        }

        setDrawableSelector();
        setOnClickListener(this);
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
        if(isChecked){
            setBackgroundResource(mStateCheckedDrawableId);
        }else{
            setBackgroundResource(mStateNotCheckedDrawableId);
        }
        invalidate();
    }

    /**
     * Set the checked status
     *
     * @param checked
     */
    public void setChecked(boolean checked){
        isChecked = checked;
        setDrawableSelector();
    }

    /**
     * Add listeners to this view, instead of using the {@link #setOnClickListener(OnClickListener)},
     * since we already set a listener
     *
     * @param listener Another listener to be added to the list
     */
    public void addClickListener(@NonNull OnClickListener listener){
        mOnClickListeners.add(listener);
    }

    /**
     * Clears all the added listeners in {@link #mOnClickListeners}
     */
    public void clearClickListeners(){
        mOnClickListeners.clear();
    }

}
