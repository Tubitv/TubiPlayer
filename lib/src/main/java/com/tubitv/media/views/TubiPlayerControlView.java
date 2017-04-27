package com.tubitv.media.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.util.Util;
import com.tubitv.media.R;
import com.tubitv.ui.TubiLoadingView;

import java.util.Formatter;
import java.util.Locale;

import static com.google.android.exoplayer2.ExoPlayer.STATE_BUFFERING;
import static com.google.android.exoplayer2.ExoPlayer.STATE_ENDED;
import static com.google.android.exoplayer2.ExoPlayer.STATE_IDLE;
import static com.google.android.exoplayer2.ExoPlayer.STATE_READY;

/**
 * Created by stoyan on 3/23/17.
 */
public class TubiPlayerControlView extends FrameLayout {
    /**
     * Simple tag for logging
     */
    private static final String TAG = TubiPlayerControlView.class.getSimpleName();

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

    /**
     * Dispatches seek operations to the player.
     */
    public interface SeekDispatcher {

        /**
         * @param player      The player to seek.
         * @param windowIndex The index of the window.
         * @param positionMs  The seek position in the specified window, or {@link C#TIME_UNSET} to seek
         *                    to the window's default position.
         * @return True if the seek was dispatched. False otherwise.
         */
        boolean dispatchSeek(ExoPlayer player, int windowIndex, long positionMs);

    }

    /**
     * Default {@link SeekDispatcher} that dispatches seeks to the player without modification.
     */
    public static final SeekDispatcher DEFAULT_SEEK_DISPATCHER = new SeekDispatcher() {

        @Override
        public boolean dispatchSeek(ExoPlayer player, int windowIndex, long positionMs) {
            player.seekTo(windowIndex, positionMs);
            return true;
        }

    };
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;
    private static final int PROGRESS_BAR_MAX = 1000;
    private static final int DEFAULT_FAST_FORWARD_MS = 15000;
    private static final int DEFAULT_UPDATE_FAST_FORWARD_MS = 333;

    private TextView mElapsedTime;
    private TextView mRemainingTime;
    private final ComponentListener componentListener;

    private SeekBar mProgressBar;
    private final StringBuilder formatBuilder;
    private final Formatter formatter;
    private final Timeline.Window currentWindow;

    private ExoPlayer player;
    private SeekDispatcher seekDispatcher;
    private VisibilityListener visibilityListener;

    private boolean isAttachedToWindow;
    private boolean dragging;
    private int showTimeoutMs;
    private long hideAtMs;
    boolean ffPressed = false;
    boolean rwPressed = false;
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

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

        int controllerLayoutId = R.layout.view_tubi_player_control;
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.TubiPlayerControlView, 0, 0);
            try {
                showTimeoutMs = a.getInt(R.styleable.TubiPlayerControlView_show_timeout_ms, showTimeoutMs);
            } finally {
                a.recycle();
            }
        }
        currentWindow = new Timeline.Window();
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        componentListener = new ComponentListener();
        seekDispatcher = DEFAULT_SEEK_DISPATCHER;


        initLayout(controllerLayoutId);
    }

    private void initLayout(int controllerLayoutId) {
        LayoutInflater.from(getContext()).inflate(controllerLayoutId, this);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        mProgressBar = (SeekBar) findViewById(R.id.view_tubi_controller_seek_bar);
        if (mProgressBar != null) {
            mProgressBar.setOnSeekBarChangeListener(componentListener);
            mProgressBar.setMax(PROGRESS_BAR_MAX);
        }
        mPlayToggleView = (ImageView) findViewById(R.id.view_tubi_controller_play_toggle_ib);
        if (mPlayToggleView != null) {
            mPlayToggleView.setOnClickListener(componentListener);
        }
        mFastForward = (ImageButton) findViewById(R.id.view_tubi_controller_forward_ib);


        if (mFastForward != null) {
            mFastForward.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // PRESSED
                            ffPressed = true;
                            removeCallbacks(hideAction);
                            seekBy(DEFAULT_FAST_FORWARD_MS);
                            pressedSeekBy();
                            return true;
                        case MotionEvent.ACTION_UP:
                            // RELEASED
                            ffPressed = false;
                            getHandler().removeCallbacks(mFastForwardRunnable);
                            hideAfterTimeout();
                            return true;
                    }
                    return false;
                }
            });
        }
        mRewind = (ImageButton) findViewById(R.id.view_tubi_controller_rewind_ib);
        if (mRewind != null) {
            mRewind.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // PRESSED
                            rwPressed = true;
                            removeCallbacks(hideAction);
                            seekBy(-DEFAULT_FAST_FORWARD_MS);
                            pressedSeekBy();
                            return true;
                        case MotionEvent.ACTION_UP:
                            // RELEASED
                            rwPressed = false;
                            getHandler().removeCallbacks(mFastForwardRunnable);
                            hideAfterTimeout();
                            return true;
                    }
                    return false;
                }
            });
        }

        mLoadingSpinner = (TubiLoadingView) findViewById(R.id.view_tubi_controller_loading);

        mElapsedTime = (TextView) findViewById(R.id.view_tubi_controller_elapsed_time);
        mRemainingTime = (TextView) findViewById(R.id.view_tubi_controller_remaining_time);
    }

    Runnable mFastForwardRunnable = new Runnable() {
        @Override
        public void run() {
            if (ffPressed == rwPressed) {
                return;
            }
            seekBy(ffPressed ? DEFAULT_FAST_FORWARD_MS : -DEFAULT_FAST_FORWARD_MS);
            pressedSeekBy();
        }
    };

    private void pressedSeekBy() {
        if (ffPressed != rwPressed) {
            postDelayed(mFastForwardRunnable, DEFAULT_UPDATE_FAST_FORWARD_MS);
        }
    }

    /**
     * Returns the player currently being controlled by this view, or null if no player is set.
     */
    public ExoPlayer getPlayer() {
        return player;
    }

    /**
     * Sets the {@link ExoPlayer} to control.
     *
     * @param player the {@code ExoPlayer} to control.
     */
    public void setPlayer(ExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
        }
        this.player = player;
        if (player != null) {
            player.addListener(componentListener);
        }
        updateAll();
    }

    /**
     * Sets the {@link VisibilityListener}.
     *
     * @param listener The listener to be notified about visibility changes.
     */
    public void setVisibilityListener(VisibilityListener listener) {
        this.visibilityListener = listener;
    }

    /**
     * Sets the {@link SeekDispatcher}.
     *
     * @param seekDispatcher The {@link SeekDispatcher}, or null to use
     *                       {@link #DEFAULT_SEEK_DISPATCHER}.
     */
    public void setSeekDispatcher(SeekDispatcher seekDispatcher) {
        this.seekDispatcher = seekDispatcher == null ? DEFAULT_SEEK_DISPATCHER : seekDispatcher;
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input.
     *
     * @return The duration in milliseconds. A non-positive value indicates that the controls will
     * remain visible indefinitely.
     */
    public int getShowTimeoutMs() {
        return showTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input.
     *
     * @param showTimeoutMs The duration in milliseconds. A non-positive value will cause the controls
     *                      to remain visible indefinitely.
     */
    public void setShowTimeoutMs(int showTimeoutMs) {
        this.showTimeoutMs = showTimeoutMs;
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
            updateAll();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    /**
     * Hides the controller.
     */
    public void hide() {
        if (isVisible()) {
            setVisibility(GONE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            removeCallbacks(updateProgressAction);
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;
        }
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    private void hideAfterTimeout() {
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

    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
//        boolean requestPlayPauseFocus = false;
        boolean playing = player != null && player.getPlayWhenReady();
        int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();
        switch (playbackState){
            case STATE_READY:
                if (mPlayToggleView != null) {
                    mPlayToggleView.setVisibility(View.VISIBLE);
                    if (playing) {
                        mPlayToggleView.setBackgroundResource(R.drawable.tubi_tv_pause_large);
                    } else {
                        mPlayToggleView.setBackgroundResource(R.drawable.tubi_tv_play_large);
                    }
                }
                break;
            case STATE_BUFFERING:
                break;
            case STATE_IDLE:  //nothing to play
            case STATE_ENDED: //stream ended
                break;
        }
    }

    private void updateNavigation() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        Timeline currentTimeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = currentTimeline != null && !currentTimeline.isEmpty();
        boolean isSeekable = false;
        if (haveNonEmptyTimeline) {
            int currentWindowIndex = player.getCurrentWindowIndex();
            currentTimeline.getWindow(currentWindowIndex, currentWindow);
            isSeekable = currentWindow.isSeekable;
        }
        if (mProgressBar != null) {
            mProgressBar.setEnabled(isSeekable);
        }
    }

    private void updateProgress() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        long position = player == null ? 0 : player.getCurrentPosition();
        long duration = player == null ? 0 : player.getDuration();
        if (!dragging) {
            setProgressTime(position, duration);
        }

        if (mProgressBar != null) {
            if (!dragging) {
                mProgressBar.setProgress(progressBarValue(position));
            }
            long bufferedPosition = player == null ? 0 : player.getBufferedPosition();
            mProgressBar.setSecondaryProgress(progressBarValue(bufferedPosition));
        }
        removeCallbacks(updateProgressAction);
        // Schedule an update if necessary.
        int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    private void setButtonEnabled(boolean enabled, View view) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        if (Util.SDK_INT >= 11) {
            setViewAlphaV11(view, enabled ? 1f : 0.3f);
            view.setVisibility(VISIBLE);
        } else {
            view.setVisibility(enabled ? VISIBLE : INVISIBLE);
        }
    }

    @TargetApi(11)
    private void setViewAlphaV11(View view, float alpha) {
        view.setAlpha(alpha);
    }

    private void setProgressTime(long position, long duration) {
        if (mElapsedTime != null) {
            mElapsedTime.setText(toProgressTime(position, false));
        }

        if (mRemainingTime != null) {
            mRemainingTime.setText(toProgressTime(duration - position, true));
        }
    }

    private String toProgressTime(long timeMs, boolean remaining) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        String time = hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
        return remaining && timeMs != 0 ? "-" + time : time;
    }

    private int progressBarValue(long position) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET || duration == 0 ? 0
                : (int) ((position * PROGRESS_BAR_MAX) / duration);
    }

    private long positionValue(int progress) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET ? 0 : ((duration * progress) / PROGRESS_BAR_MAX);
    }

    private void seekBy(long timeMillis) {
        long position = player.getCurrentPosition();
        long place = position + timeMillis;
        //lower bound
        place = place < 0 ? 0 : place;
        //upper bound
        place = place > player.getDuration() ? player.getDuration() : place;
        seekTo(place);
    }

    private void seekTo(long positionMs) {
        seekTo(player.getCurrentWindowIndex(), positionMs);
    }

    private void seekTo(int windowIndex, long positionMs) {
        boolean dispatched = seekDispatcher.dispatchSeek(player, windowIndex, positionMs);
        if (!dispatched) {
            // The seek wasn't dispatched. If the progress bar was dragged by the user to perform the
            // seek then it'll now be in the wrong position. Trigger a progress update to snap it back.
            updateProgress();
        }
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
        updateAll();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        removeCallbacks(updateProgressAction);
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

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (player == null || !isHandledMediaKey(keyCode)) {
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    player.setPlayWhenReady(!player.getPlayWhenReady());
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    player.setPlayWhenReady(true);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    player.setPlayWhenReady(false);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    break;
                default:
                    break;
            }
        }
        show();
        return true;
    }

    private static boolean isHandledMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    }

    private final class ComponentListener implements ExoPlayer.EventListener,
            SeekBar.OnSeekBarChangeListener, View.OnClickListener {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            removeCallbacks(hideAction);
            dragging = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                long position = positionValue(progress);
                long duration = player == null ? 0 : player.getDuration();
                setProgressTime(position, duration);
                if (player != null && !dragging) {
                    seekTo(position);
                }
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //Reset thumb, or smaller unpressed drawable will get blown up
            seekBar.setThumb(getResources().getDrawable(R.drawable.tubi_tv_drawable_scrubber_selector));
            dragging = false;
            if (player != null) {
                seekTo(positionValue(seekBar.getProgress()));
            }
            hideAfterTimeout();
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
            updateProgress();
        }

        @Override
        public void onPositionDiscontinuity() {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
            // Do nothing.
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Do nothing.
        }

        @Override
        public void onClick(View view) {
            if (player != null) {
                boolean playing = player.getPlayWhenReady();
                player.setPlayWhenReady(!playing);
            }
            hideAfterTimeout();
        }

    }

}