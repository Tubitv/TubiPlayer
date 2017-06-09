package com.tubitv.media.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.tubitv.media.R;
import com.tubitv.media.databinding.ViewTubiAdLearnMoreBinding;

/**
 * Created by stoyan on 6/9/17.
 */
public class AdLearnMoreView extends ConstraintLayout {

    ViewTubiAdLearnMoreBinding mBinding;

    public AdLearnMoreView(Context context) {
        this(context, null);
    }

    public AdLearnMoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdLearnMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Skipping...
        if (isInEditMode()) {
            return;
        }

        init(attrs);
    }

    /**
     * Initialize all of the drawables and animations as well as apply attributes if set
     *
     * @param attrs
     */
    private void init(@Nullable AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.view_tubi_ad_learn_more, this, true);
        String text =
                getResources().getString(R.string.view_tubi_ad_learn_more_ads_remaining_text, 1, 4);
        mBinding.viewTubiAdLearnMoreAdsRemaining.setText(text);
    }
}
