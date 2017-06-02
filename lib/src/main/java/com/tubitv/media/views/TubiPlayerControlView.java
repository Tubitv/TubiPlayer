package com.tubitv.media.views;

import android.content.Context;
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
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;

/**
 * Created by stoyan on 5/15/17.
 */
public class TubiPlayerControlView extends ConstraintLayout {
    /**
     * The default time to hide the this view if the user is not interacting with it
     */
    private static final int DEFAULT_HIDE_TIMEOUT_MS = 5000;


    private ViewTubiPlayerControlBinding mBinding;
    private TubiPlayerControlViewOld.VisibilityListener visibilityListener;

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
     * The interface for playback control of the media
     */
    private TubiPlaybackControlInterface playbackInterface;

    /**
     * The binding observable for the control views
     */
    private TubiObservable media;

    /**
     * The exo player instance for this view
     */
    private SimpleExoPlayer mPlayer;

    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

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

    private void initLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.view_tubi_player_control, this, true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        mBinding.setController(this);
    }

    @BindingAdapter("bind:tubi_hide_timeout_ms")
    public static void setCustomTypeface(TubiPlayerControlView tubiController, int milliseconds) {
        tubiController.setHideTimeoutMs(milliseconds);
    }

    public void setHideTimeoutMs(int hideTimeoutMs) {
        this.hideAtMs = hideTimeoutMs;
    }


    public void setPlayer(@NonNull SimpleExoPlayer player, @NonNull final TubiPlaybackControlInterface playbackControlInterface) {
        if (this.mPlayer == null || this.mPlayer != player) {
            media = new TubiObservable(player, playbackControlInterface);
            setPlaybackInterface(playbackControlInterface);
            //Controller doesn't get re-initialized TODO fix instance call
            mBinding.viewTubiControllerSubtitlesIb.clearClickListeners();
            mBinding.viewTubiControllerQualityIb.clearClickListeners();
            mBinding.viewTubiControllerChromecastIb.clearClickListeners();
            mBinding.viewTubiControllerPlayToggleIb.clearClickListeners();
            mBinding.setPlayMedia(media);
        }
    }

    /**
     * Shows the playback controls. If {@link #getShowTimeoutMs()} is positive then the controls will
     * be automatically hidden after this duration of time has elapsed without user input.
     */
    public void show() {
        if (!isVisible()) {
            setVisibility(VISIBLE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            alignSubtitlesView(true);
//            updateAll();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    public void hide() {
        if (isVisible()) {
            if (media == null || !media.userInteracting()) {
                setVisibility(GONE);
                if (visibilityListener != null) {
                    visibilityListener.onVisibilityChange(getVisibility());
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

    public void setVisibilityListener(TubiPlayerControlViewOld.VisibilityListener visibilityListener) {
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

            subtitles.setPadding(0,0,0, visible ? getHeight() - seekBarTop : 0);
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
        return getVisibility() == VISIBLE;
    }

    public int getShowTimeoutMs() {
        return showTimeoutMs;
    }

    public void setShowTimeoutMs(int i) {

    }

    public void setPlaybackInterface(TubiPlaybackControlInterface playbackInterface) {
        this.playbackInterface = playbackInterface;
        if(media != null){
            media.setPlaybackInterface(playbackInterface);
        }
    }
}
