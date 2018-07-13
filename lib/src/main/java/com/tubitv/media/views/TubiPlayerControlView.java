package com.tubitv.media.views;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.tubitv.media.R;
import com.tubitv.media.bindings.TubiObservable;
import com.tubitv.media.databinding.ViewTubiPlayerControlBinding;
import com.tubitv.media.interfaces.TrackSelectionHelperInterface;
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;
import com.tubitv.media.interfaces.PlaybackActionCallback;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.Utils;

/**
 * Created by stoyan on 5/15/17.
 */
public class TubiPlayerControlView extends ConstraintLayout implements TrackSelectionHelperInterface {
    /**
     * The default time to hide the this view if the user is not interacting with it
     */
    private static final int DEFAULT_HIDE_TIMEOUT_MS = 5000;

    private ViewTubiPlayerControlBinding mBinding;
    private TubiPlayerControlView.VisibilityListener visibilityListener;
    /**
     * Attached state of this view
     */
    private boolean isAttachedToWindow;
    /**
     * The time out time for the view to be hidden if the user is not interacting with it
     */
    private int showTimeoutMs = DEFAULT_HIDE_TIMEOUT_MS;
    /**
     * The time out time for the view to be hidden if the user is not interacting with it
     */
    private long hideAtMs;
    /**
     * The binding observable for the control views
     */
    private TubiObservable tubiObservable;
    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * The media model the player is initialized with
     */
    @NonNull
    private MediaModel mediaModel;

    /**
     * The exo player instance for this view
     */
    private SimpleExoPlayer mPlayer;

    private float seekBarUpperBound = 0f;

    public TubiPlayerControlView(Context context) {
        this(context, null);
    }

    public TubiPlayerControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TubiPlayerControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Skipping...
        if (isInEditMode()) {
            return;
        }

        initLayout();
    }

    @BindingAdapter("bind:tubi_hide_timeout_ms")
    public static void setCustomTypeface(TubiPlayerControlView tubiController, int milliseconds) {
        tubiController.setHideTimeoutMs(milliseconds);
    }

    /**
     * get the seekBar upper bound relative to bottom of the screen, for Auto play animation purposes.
     *
     * @return if return 0f, it mean the view hasn't been created, therefore, should only called this method when view has created on the screen already.
     */
    public float getSeekBarUpperBound() {
        return seekBarUpperBound;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        if (hideAtMs != C.TIME_UNSET) {
            long delayMs = hideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hide();
            } else {
                postDelayed(hideAction, delayMs);
            }
        }
        //        updateAll();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        seekBarUpperBound = calculateSeekBarUpperBound();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        //        removeCallbacks(updateProgressAction);
        removeCallbacks(hideAction);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
        if (handled) {
            show();
        }
        return handled;
    }

    @Override
    public void onTrackSelected(boolean trackSelected) {
        tubiObservable.setQualityEnabled(trackSelected);
    }

    private void initLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.view_tubi_player_control, this, true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        mBinding.setController(this);
    }

    public void setHideTimeoutMs(int hideTimeoutMs) {
        this.hideAtMs = hideTimeoutMs;
    }

    public void setPlayer(@NonNull SimpleExoPlayer player,
            @NonNull final TubiPlaybackControlInterface playbackControlInterface,
            @NonNull final PlaybackActionCallback playbackActionCallback) {
        if (this.mPlayer == null || this.mPlayer != player) {
            tubiObservable = new TubiObservable(player, playbackControlInterface, playbackActionCallback);
            //Controller doesn't get re-initialized TODO fix instance call
            mBinding.viewTubiControllerSubtitlesIb.clearClickListeners();
            mBinding.viewTubiControllerQualityIb.clearClickListeners();
            mBinding.viewTubiControllerChromecastIb.clearClickListeners();
            mBinding.viewTubiControllerPlayToggleIb.clearClickListeners();
            mBinding.setPlayMedia(tubiObservable);
        }
    }

    /**
     * manually set the subtitle indicator icon to on, use this for global preference setting.
     *
     * @param isON
     */
    public void checkSubtitleIcon(boolean isON) {
        if (mBinding.viewTubiControllerSubtitlesIb == null) {
            return;
        }
        mBinding.viewTubiControllerSubtitlesIb.setChecked(isON);
    }

    /**
     * Shows the playback controls. If {@link #getShowTimeoutMs()} is positive then the controls will
     * be automatically hidden after this duration of time has elapsed without user input.
     */
    public void show() {
        if (!isVisible()) {
            findViewById(R.id.controller_panel).setVisibility(VISIBLE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(VISIBLE);
            }
            alignSubtitlesView(true);
            //            updateAll();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    public void hide() {
        if (isVisible()) {
            if (tubiObservable == null || !tubiObservable.userInteracting()) {
                findViewById(R.id.controller_panel).setVisibility(GONE);
                if (visibilityListener != null) {
                    visibilityListener.onVisibilityChange(GONE);
                }
                alignSubtitlesView(false);
                //            removeCallbacks(updateProgressAction);
                removeCallbacks(hideAction);
                hideAtMs = C.TIME_UNSET;
            } else {
                hideAfterTimeout();
            }
        }
    }

    public void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (showTimeoutMs > 0) {
            hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
            if (isAttachedToWindow) {
                postDelayed(hideAction, showTimeoutMs);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }

    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return false;
    }

    public void setVisibilityListener(VisibilityListener visibilityListener) {
        this.visibilityListener = visibilityListener;
    }

    /**
     * Aligns the bottom of the subtitle view of the parent from {@link #getSubtitlesView()} with the top
     * of {@link ViewTubiPlayerControlBinding#viewTubiControllerSeekBar} when this controller
     * is visible to prevent overlap
     *
     * @param visible True if this controller is visible
     */
    private void alignSubtitlesView(boolean visible) {
        View subtitles = getSubtitlesView(); //TODO show to design 5/31/17
        if (subtitles != null) {
            int seekBarTop = mBinding.viewTubiControllerSeekBar.getTop();

            subtitles.setPadding(0, 0, 0, visible ? getHeight() - seekBarTop : 0);
            //            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //            lp.setMargins(0,0,0, visible ? getHeight() - seekBarTop : 0);
            //            subtitles.setLayoutParams(lp);
        }
    }

    /**
     * Searches the parent views for the subtitle view {@link com.google.android.exoplayer2.ui.SubtitleView}
     *
     * @return The subtitle view or null if not found
     */
    @Nullable
    private View getSubtitlesView() {
        View subtitles = null;
        if (getParent() != null && getParent().getParent() != null) {
            View layout = (View) getParent().getParent();
            subtitles = layout.findViewById(R.id.exo_subtitles);
        }
        return subtitles;
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return findViewById(R.id.controller_panel).getVisibility() == VISIBLE;
    }

    public int getShowTimeoutMs() {
        return showTimeoutMs;
    }

    public void setShowTimeoutMs(int i) {

    }

    public void setMediaModel(@NonNull MediaModel mediaModel) {
        this.mediaModel = mediaModel;
        if (mediaModel.isAd()) {
            this.tubiObservable.setTitle("");
        } else {
            this.tubiObservable.setTitle(mediaModel.getMediaName());
        }
        this.tubiObservable.setSubtitlesExist(mediaModel.getSubtitlesUrl() != null);
        this.tubiObservable.setMediaModel(mediaModel);
    }

    public void setAvailableAdLeft(int count) {
        Resources res = getResources();
        String numberofAdLeftInString = res
                .getQuantityString(R.plurals.view_tubi_ad_learn_more_ads_resume_shortly_text, count, count);
        this.tubiObservable.numberOfAdLeft.set(numberofAdLeftInString);
    }

    /**
     * @return the upper bound of seek bar relative to the bottom of the screen in px.
     */
    public float calculateSeekBarUpperBound() {

        //if the seekBarUpperBound has already been calculated, do not need to calculated the position again,
        //because the operation is relatively expensive.
        if (seekBarUpperBound != 0f) {
            return seekBarUpperBound;
        }

        float positionFromBottom = 0;

        if (mBinding.viewTubiControllerSeekBar != null) {
            //because the seekbar visibility is "GONE" when first enter the playback activity, we need a special way to get
            //control view's height.
            int widthSpec = MeasureSpec
                    .makeMeasureSpec(mBinding.viewTubiControllerSeekBar.getWidth(), MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            mBinding.viewTubiControllerSeekBar.measure(widthSpec, heightSpec);
            int height = mBinding.viewTubiControllerSeekBar.getMeasuredHeight();

            // the control's view's height + 20dp which is the bottom guideline space.
            positionFromBottom = height + Utils.pxFromDp(getContext(), 20);
        }

        return positionFromBottom;
    }

    /**
     * Listener to be notified about changes of the visibility of the UI control.
     */
    public interface VisibilityListener {

        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either {@link View#VISIBLE} or {@link View#GONE}.
         */
        void onVisibilityChange(int visibility);

    }
}
