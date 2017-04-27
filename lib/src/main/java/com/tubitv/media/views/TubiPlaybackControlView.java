package com.tubitv.media.views;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.tubitv.media.R;
import com.tubitv.ui.TubiLoadingView;

import static com.google.android.exoplayer2.ExoPlayer.STATE_BUFFERING;
import static com.google.android.exoplayer2.ExoPlayer.STATE_ENDED;
import static com.google.android.exoplayer2.ExoPlayer.STATE_IDLE;
import static com.google.android.exoplayer2.ExoPlayer.STATE_READY;

/**
 * The view for the center playback controls that shows the play/pause button,
 * rewind and forward buttons, and the loading overlay
 * <p>
 * Created by stoyan on 4/27/17.
 */
public class TubiPlaybackControlView extends ConstraintLayout {

    /**
     * The view we toggle between play and pause depending on {@link com.google.android.exoplayer2.ExoPlayer}
     * state
     */
    private ImageView mPlayToggleView;

    /**
     * The loading spinner that we toggle when {@link TubiPlayerControlView.ComponentListener#onLoadingChanged(boolean)}.
     * When this view is visible, then the {@link #mPlayToggleView} should be invisible
     */
    private TubiLoadingView mLoadingSpinner;

    /**
     * The rewind button that can be clicked or pressed
     */
    private ImageButton mRewind;

    /**
     * The fast forward button that can be clicked or pressed
     */
    private ImageButton mFastForward;

    private boolean isAttachedToWindow;

    public TubiPlaybackControlView(Context context) {
        super(context);
        initLayout();
    }

    public TubiPlaybackControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    public TubiPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        LayoutInflater.from(getContext()).inflate(R.layout.view_tubi_playback_control, this);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        if(isInEditMode()){
            return;
        }
        mPlayToggleView = (ImageView) findViewById(R.id.view_tubi_controller_play_toggle_ib);
        mLoadingSpinner = (TubiLoadingView) findViewById(R.id.view_tubi_controller_loading);
        mRewind = (ImageButton) findViewById(R.id.view_tubi_controller_rewind_ib);
        mFastForward = (ImageButton) findViewById(R.id.view_tubi_controller_forward_ib);
    }

    public void onPlaybackState(boolean playing, int playbackState) {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }

//        boolean playing = player != null && player.getPlayWhenReady();
//        int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();
        switch (playbackState) {
            case STATE_READY:
                showLoading(true, playing);
                break;
            case STATE_BUFFERING:
                showLoading(false, false);
                break;
            case STATE_IDLE:  //nothing to play
            case STATE_ENDED: //stream ended
                break;
        }
    }

    /**
     * Toggles the views in this control when the player is
     * @param isLoaded
     * @param isPlaying
     */
    private void showLoading(boolean isLoaded, boolean isPlaying) {
        int vis = isLoaded ? View.VISIBLE : View.INVISIBLE;
        mPlayToggleView.setVisibility(vis);
        mFastForward.setVisibility(vis);
        mRewind.setVisibility(vis);

        if (isLoaded) {
            mLoadingSpinner.stop();
        } else {
            mLoadingSpinner.start();
        }

        if (isPlaying) {
            mPlayToggleView.setBackgroundResource(R.drawable.tubi_tv_pause_large);
        } else {
            mPlayToggleView.setBackgroundResource(R.drawable.tubi_tv_play_large);
        }
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }
}
