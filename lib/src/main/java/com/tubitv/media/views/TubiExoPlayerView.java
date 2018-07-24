package com.tubitv.media.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.ApicFrame;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.tubitv.media.R;
import com.tubitv.media.bindings.TubiObservable;
import com.tubitv.media.helpers.TrackSelectionHelper;
import com.tubitv.media.interfaces.TubiPlaybackControlInterface;
import com.tubitv.media.interfaces.TubiPlaybackInterface;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.ExoPlayerLogger;
import com.tubitv.media.utilities.SeekCalculator;
import com.tubitv.ui.VaudTextView;
import com.tubitv.ui.VaudType;
import java.util.List;

/**
 * Created by stoyan tubi_tv_quality_on 3/22/17.
 */
@TargetApi(16)
public class TubiExoPlayerView extends FrameLayout implements TubiPlaybackControlInterface {

    private static final String TAG = TubiExoPlayerView.class.getSimpleName();

    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

    private final AspectRatioFrameLayout contentFrame;
    private final View shutterView;
    private final View surfaceView;
    private final ImageView artworkView;
    private final SubtitleView subtitleView;
    private final TubiPlayerControlView controller;
    private final ComponentListener componentListener;
    private final FrameLayout overlayFrameLayout;

    private SimpleExoPlayer player;
    private boolean useController;
    private boolean useArtwork;
    private Bitmap defaultArtwork;
    private int controllerShowTimeoutMs;
    private boolean controllerHideOnTouch;

    private TrackSelectionHelper mTrackSelectionHelper;
    private Activity mActivity;

    @NonNull
    private MediaModel mediaModel;

    /**
     * The interface from the calling activity for hooking general media playback state
     */
    @Nullable
    private TubiPlaybackInterface playbackInterface;

    public TubiExoPlayerView(Context context) {
        this(context, null);
    }

    public TubiExoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TubiExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            contentFrame = null;
            shutterView = null;
            surfaceView = null;
            artworkView = null;
            subtitleView = null;
            controller = null;
            componentListener = null;
            overlayFrameLayout = null;
            ImageView logo = new ImageView(context, attrs);
            if (Util.SDK_INT >= 23) {
                configureEditModeLogoV23(getResources(), logo);
            } else {
                configureEditModeLogo(getResources(), logo);
            }
            addView(logo);
            return;
        }

        int playerLayoutId = R.layout.tubi_player_view;
        boolean useArtwork = true;
        int defaultArtworkId = 0;
        boolean useController = true;
        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        int controllerShowTimeoutMs = PlaybackControlView.DEFAULT_SHOW_TIMEOUT_MS;
        boolean controllerHideOnTouch = true;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.SimpleExoPlayerView, 0, 0);
            try {
                playerLayoutId = a.getResourceId(R.styleable.SimpleExoPlayerView_player_layout_id,
                        playerLayoutId);
                useArtwork = a.getBoolean(R.styleable.SimpleExoPlayerView_use_artwork, useArtwork);
                defaultArtworkId = a.getResourceId(R.styleable.SimpleExoPlayerView_default_artwork,
                        defaultArtworkId);
                useController = a.getBoolean(R.styleable.SimpleExoPlayerView_use_controller, useController);
                surfaceType = a.getInt(R.styleable.SimpleExoPlayerView_surface_type, surfaceType);
                resizeMode = a.getInt(R.styleable.SimpleExoPlayerView_resize_mode, resizeMode);
                controllerShowTimeoutMs = a.getInt(R.styleable.SimpleExoPlayerView_show_timeout,
                        controllerShowTimeoutMs);
                controllerHideOnTouch = a.getBoolean(R.styleable.SimpleExoPlayerView_hide_on_touch,
                        controllerHideOnTouch);
            } finally {
                a.recycle();
            }
        }

        LayoutInflater.from(context).inflate(playerLayoutId, this);
        componentListener = new ComponentListener();
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        // Content frame.
        contentFrame = (AspectRatioFrameLayout) findViewById(R.id.exo_content_frame);
        if (contentFrame != null) {
            setResizeModeRaw(contentFrame, resizeMode);
        }

        // Shutter view.
        shutterView = findViewById(R.id.exo_shutter);

        // Create a surface view and insert it into the content frame, if there is one.
        if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView = surfaceType == SURFACE_TYPE_TEXTURE_VIEW ? new TextureView(context)
                    : new SurfaceView(context);
            surfaceView.setLayoutParams(params);
            contentFrame.addView(surfaceView, 0);
        } else {
            surfaceView = null;
        }

        // Overlay frame layout.
        overlayFrameLayout = (FrameLayout) findViewById(R.id.exo_overlay);

        // Artwork view.
        artworkView = (ImageView) findViewById(R.id.exo_artwork);
        this.useArtwork = useArtwork && artworkView != null;
        if (defaultArtworkId != 0) {
            defaultArtwork = BitmapFactory.decodeResource(context.getResources(), defaultArtworkId);
        }

        // Subtitle view.
        subtitleView = (SubtitleView) findViewById(R.id.exo_subtitles);
        if (subtitleView != null) {
            subtitleView.setStyle(new CaptionStyleCompat(
                    Color.WHITE,
                    getResources().getColor(R.color.tubi_tv_player_controls_subtitles_background),
                    Color.TRANSPARENT,
                    CaptionStyleCompat.EDGE_TYPE_NONE,
                    Color.WHITE,
                    VaudTextView.getFont(context, VaudType.VAUD_REGULAR.getAssetFileName())));
            subtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.view_tubi_exo_player_subtitle_text_size));
            subtitleView.setApplyEmbeddedStyles(false);
            subtitleView.setVisibility(View.INVISIBLE);
        }

        // Playback control view.
        View controllerPlaceholder = findViewById(R.id.exo_controller_placeholder);
        if (controllerPlaceholder != null) {
            // Note: rewindMs and fastForwardMs are passed via attrs, so we don't need to make explicit
            // calls to set them.
            this.controller = new TubiPlayerControlView(context, attrs);
            controller.setLayoutParams(controllerPlaceholder.getLayoutParams());
            ViewGroup parent = ((ViewGroup) controllerPlaceholder.getParent());
            int controllerIndex = parent.indexOfChild(controllerPlaceholder);
            parent.removeView(controllerPlaceholder);
            parent.addView(controller, controllerIndex);
        } else {
            this.controller = null;
        }
        this.controllerShowTimeoutMs = controller != null ? controllerShowTimeoutMs : 0;
        this.controllerHideOnTouch = controllerHideOnTouch;
        this.useController = useController && controller != null;
        hideController();
    }

    //    /**
    //     * Switches the view targeted by a given {@link SimpleExoPlayer}.
    //     *
    //     * @param player The player whose target view is being switched.
    //     * @param oldPlayerView The old view to detach from the player.
    //     * @param newPlayerView The new view to attach to the player.
    //     */
    //    public  void switchTargetView(@NonNull SimpleExoPlayer player,
    //                                        @Nullable SimpleExoPlayerView oldPlayerView, @Nullable SimpleExoPlayerView newPlayerView) {
    //        if (oldPlayerView == newPlayerView) {
    //            return;
    //        }
    //        // We attach the new view before detaching the old one because this ordering allows the player
    //        // to swap directly from one surface to another, without transitioning through a state where no
    //        // surface is attached. This is significantly more efficient and achieves a more seamless
    //        // transition when using platform provided video decoders.
    //        if (newPlayerView != null) {
    //            newPlayerView.setPlayer(player, this);
    //        }
    //        if (oldPlayerView != null) {
    //            oldPlayerView.setPlayer(null, this);
    //        }
    //    }

    @TargetApi(23)
    private static void configureEditModeLogoV23(Resources resources, ImageView logo) {
        logo.setImageDrawable(resources.getDrawable(R.drawable.exo_edit_mode_logo, null));
        logo.setBackgroundColor(resources.getColor(R.color.exo_edit_mode_background_color, null));
    }

    @SuppressWarnings("deprecation")
    private static void configureEditModeLogo(Resources resources, ImageView logo) {
        logo.setImageDrawable(resources.getDrawable(R.drawable.exo_edit_mode_logo));
        logo.setBackgroundColor(resources.getColor(R.color.exo_edit_mode_background_color));
    }

    @SuppressWarnings("ResourceType")
    private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
        aspectRatioFrame.setResizeMode(resizeMode);
    }

    public TubiPlayerControlView getControlView() {
        return controller;
    }

    /**
     * Returns the player currently set on this view, or null if no player is set.
     */
    public SimpleExoPlayer getPlayer() {
        return player;
    }

    /**
     * Set the {@link SimpleExoPlayer} to use. The {@link SimpleExoPlayer#setTextOutput} and
     * {@link SimpleExoPlayer#setVideoListener} method of the player will be called and previous
     * assignments are overridden.
     *
     * @param player The {@link SimpleExoPlayer} to use.
     */
    public void setPlayer(SimpleExoPlayer player, @NonNull TubiPlaybackInterface playbackInterface) {
        setPlaybackInterface(playbackInterface);
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
            this.player.clearTextOutput(componentListener);
            this.player.clearVideoListener(componentListener);
            if (surfaceView instanceof TextureView) {
                this.player.clearVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                this.player.clearVideoSurfaceView((SurfaceView) surfaceView);
            }
        }
        this.player = player;
        if (useController) {
            controller.setPlayer(player, this, playbackInterface);
        }
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        if (player != null) {
            if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            }
            player.setVideoListener(componentListener);
            player.setTextOutput(componentListener);
            player.addListener(componentListener);
            //            maybeShowController(false);
            updateForCurrentTrackSelections();
        } else {
            hideController();
            hideArtwork();
        }
    }

    /**
     * Sets the resize mode.
     *
     * @param resizeMode The resize mode.
     */
    public void setResizeMode(@AspectRatioFrameLayout.ResizeMode int resizeMode) {
        Assertions.checkState(contentFrame != null);
        contentFrame.setResizeMode(resizeMode);
    }

    /**
     * Returns whether artwork is displayed if present in the media.
     */
    public boolean getUseArtwork() {
        return useArtwork;
    }

    /**
     * Sets whether artwork is displayed if present in the media.
     *
     * @param useArtwork Whether artwork is displayed.
     */
    public void setUseArtwork(boolean useArtwork) {
        Assertions.checkState(!useArtwork || artworkView != null);
        if (this.useArtwork != useArtwork) {
            this.useArtwork = useArtwork;
            updateForCurrentTrackSelections();
        }
    }

    /**
     * Returns the default artwork to display.
     */
    public Bitmap getDefaultArtwork() {
        return defaultArtwork;
    }

    /**
     * Sets the default artwork to display if {@code useArtwork} is {@code true} and no artwork is
     * present in the media.
     *
     * @param defaultArtwork the default artwork to display.
     */
    public void setDefaultArtwork(Bitmap defaultArtwork) {
        if (this.defaultArtwork != defaultArtwork) {
            this.defaultArtwork = defaultArtwork;
            updateForCurrentTrackSelections();
        }
    }

    /**
     * Returns whether the playback controls can be shown.
     */
    public boolean getUseController() {
        return useController;
    }

    /**
     * Sets whether the playback controls can be shown. If set to {@code false} the playback controls
     * are never visible and are disconnected from the player.
     *
     * @param useController Whether the playback controls can be shown.
     */
    public void setUseController(boolean useController) {
        Assertions.checkState(!useController || controller != null);
        if (this.useController == useController) {
            return;
        }
        this.useController = useController;
        if (useController) {
            controller.setPlayer(player, this, playbackInterface);
        } else if (controller != null) {
            controller.hide();
            controller.setPlayer(null, this, playbackInterface);
        }
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return useController && controller.dispatchMediaKeyEvent(event);
    }

    /**
     * Shows the playback controls. Does nothing if playback controls are disabled.
     */
    public void showController() {
        if (useController) {
            maybeShowController(true);
        }
    }

    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    public void hideController() {
        if (controller != null) {
            controller.hide();
        }
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input and with playback or buffering in
     * progress.
     *
     * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
     * visible indefinitely.
     */
    public int getControllerShowTimeoutMs() {
        return controllerShowTimeoutMs;
    }

    //    /**
    //     * Set the {@link PlaybackControlView.VisibilityListener}.
    //     *
    //     * @param listener The listener to be notified about visibility changes.
    //     */
    //    public void setControllerVisibilityListener(PlaybackControlView.VisibilityListener listener) {
    //        Assertions.checkState(controller != null);
    //        controller.setVisibilityListener(listener);
    //    }

    //    /**
    //     * Sets the {@link com.google.android.exoplayer2.ui.PlaybackControlView.ControlDispatcher}.
    //     *
    //     * @param controlDispatcher The {@link com.google.android.exoplayer2.ui.PlaybackControlView.ControlDispatcher}, or null to use
    //     *     {@link PlaybackControlView#DEFAULT_CONTROL_DISPATCHER}.
    //     */
    //    public void setControlDispatcher(PlaybackControlView.ControlDispatcher controlDispatcher) {
    //        Assertions.checkState(controller != null);
    //        controller.setControlDispatcher(controlDispatcher);
    //    }
    //
    //    /**
    //     * Sets the rewind increment in milliseconds.
    //     *
    //     * @param rewindMs The rewind increment in milliseconds.
    //     */
    //    public void setRewindIncrementMs(int rewindMs) {
    //        Assertions.checkState(controller != null);
    //        controller.setRewindIncrementMs(rewindMs);
    //    }
    //
    //    /**
    //     * Sets the fast forward increment in milliseconds.
    //     *
    //     * @param fastForwardMs The fast forward increment in milliseconds.
    //     */
    //    public void setFastForwardIncrementMs(int fastForwardMs) {
    //        Assertions.checkState(controller != null);
    //        controller.setFastForwardIncrementMs(fastForwardMs);
    //    }
    //
    //    /**
    //     * Sets whether the time bar should show all windows, as opposed to just the current one.
    //     *
    //     * @param showMultiWindowTimeBar Whether to show all windows.
    //     */
    //    public void setShowMultiWindowTimeBar(boolean showMultiWindowTimeBar) {
    //        Assertions.checkState(controller != null);
    //        controller.setShowMultiWindowTimeBar(showMultiWindowTimeBar);
    //    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause
     *                                the controller to remain visible indefinitely.
     */
    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        Assertions.checkState(controller != null);
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;
    }

    /**
     * Returns whether the playback controls are hidden by touch events.
     */
    public boolean getControllerHideOnTouch() {
        return controllerHideOnTouch;
    }

    /**
     * Sets whether the playback controls are hidden by touch events.
     *
     * @param controllerHideOnTouch Whether the playback controls are hidden by touch events.
     */
    public void setControllerHideOnTouch(boolean controllerHideOnTouch) {
        Assertions.checkState(controller != null);
        this.controllerHideOnTouch = controllerHideOnTouch;
    }

    /**
     * Gets the view onto which video is rendered. This is either a {@link SurfaceView} (default)
     * or a {@link TextureView} if the {@code use_texture_view} view attribute has been set to true.
     *
     * @return Either a {@link SurfaceView} or a {@link TextureView}.
     */
    public View getVideoSurfaceView() {
        return surfaceView;
    }

    /**
     * Gets the overlay {@link FrameLayout}, which can be populated with UI elements to show on top of
     * the player.
     *
     * @return The overlay {@link FrameLayout}, or {@code null} if the layout has been customized and
     * the overlay is not present.
     */
    public FrameLayout getOverlayFrameLayout() {
        return overlayFrameLayout;
    }

    /**
     * Gets the {@link SubtitleView}.
     *
     * @return The {@link SubtitleView}, or {@code null} if the layout has been customized and the
     * subtitle view is not present.
     */
    public SubtitleView getSubtitleView() {
        return subtitleView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!useController || player == null || ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        toggleControllerVisiblity();
        return true;
    }

    private Long mHoldKeyStartTime = null; // Track how long the key has been holden

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {

        if (mHoldKeyStartTime == null) {
            mHoldKeyStartTime = SystemClock.elapsedRealtime();
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handleSeekKeyHold(mHoldKeyStartTime, SeekCalculator.FORWARD_DIRECTION);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handleSeekKeyHold(mHoldKeyStartTime, SeekCalculator.REWIND_DIRECTION);
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        mHoldKeyStartTime = null;

        if (!useController || player == null) {
            return false;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP: // Up should always show the control and focus on caption
                if (controller.isDuringCustomSeek()) {
                    handleCancelCustomSeek();
                    return false;
                }

                if (!controller.isVisible()) {
                    toggleControllerVisiblity();
                }

                if (!controller.getCaptionButton().isFocused()) {
                    controller.setState(TubiObservable.OPTIONS_CONTROL_STATE);
                    controller.getCaptionButton().setFocusable(true);
                    controller.getCaptionButton().requestFocus();
                    controller.getPlayButton().setFocusable(false);
                }

                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (controller.isVisible()) {

                    switch (controller.getState()) {
                        case TubiObservable.OPTIONS_CONTROL_STATE:
                            controller.getCaptionButton().setFocusable(false);
                            controller.setState(TubiObservable.NORMAL_CONTROL_STATE);
                            controller.getPlayButton().setFocusable(true);
                            controller.getPlayButton().requestFocus();
                            break;
                        case TubiObservable.CUSTOM_SEEK_CONTROL_STATE:
                        case TubiObservable.EDIT_CUSTOM_SEEK_CONTROL_STATE:
                            handleCancelCustomSeek();
                            break;
                        default:
                            toggleControllerVisiblity();
                            break;
                    }

                } else {
                    toggleControllerVisiblity();
                    controller.getPlayButton().setFocusable(true);
                    controller.getPlayButton().requestFocus();
                }

                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handleSeekKeyUp(SeekCalculator.FORWARD_DIRECTION);

                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handleSeekKeyUp(SeekCalculator.REWIND_DIRECTION);

                return true;
            case KeyEvent.KEYCODE_ENTER:
                if (controller.isDuringCustomSeek()) {
                    controller.confirmCustomSeek();
                    if (!controller.isPlayerPlaying()) {
                        controller.togglePlay();
                    }
                } else {
                    controller.togglePlay();
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (controller.isDuringCustomSeek()) {
                    controller.cancelCustomSeek();
                    if (!controller.isPlayerPlaying()) {
                        controller.togglePlay();
                    }
                    return true;
                } else {
                    return false;
                }

            default:
                return false;
        }
    }

    private void handleCancelCustomSeek() {
        controller.cancelCustomSeek();
        if (!controller.isPlayerPlaying()) {
            controller.togglePlay();
        }
        toggleControllerVisiblity();
    }

    private void handleSeekKeyUp(final int direction) {

        switch (controller.getState()) {
            case TubiObservable.NORMAL_CONTROL_STATE:
                if (direction == SeekCalculator.FORWARD_DIRECTION) {
                    controller.forward();
                } else {
                    controller.rewind();
                }
                break;
            case TubiObservable.CUSTOM_SEEK_CONTROL_STATE:
                controller.setState(TubiObservable.EDIT_CUSTOM_SEEK_CONTROL_STATE);
                break;

            case TubiObservable.EDIT_CUSTOM_SEEK_CONTROL_STATE:
                controller.updateUIForCustomSeek(
                        direction * TubiObservable.DEFAULT_FAST_FORWARD_MS);
                break;
            case TubiObservable.OPTIONS_CONTROL_STATE:
                // Do nothing
                break;
            default:
                ExoPlayerLogger.d(TAG, "unhandled player control state = " + controller.getState());
                break;

        }
    }

    private void handleSeekKeyHold(final long startTime, final int direction) {
        if (controller.getCaptionButton().isFocused()) { // Don't handle custom seek if caption is focused
            return;
        }

        final long currentTime = SystemClock.elapsedRealtime();

        final long seekDelta = direction * SeekCalculator.getSeekRate(startTime, currentTime);

        controller.updateUIForCustomSeek(seekDelta, true);

        if (seekDelta != 0) {
            if (!controller.isVisible()) {
                maybeShowController(true);
            }

            if (controller.isPlayerPlaying()) {
                controller.togglePlay();
            }
        }
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController || player == null) {
            return false;
        }
        maybeShowController(true);
        return true;
    }

    private void toggleControllerVisiblity() {
        if (!controller.isVisible()) {
            maybeShowController(true);
        } else if (controllerHideOnTouch) {
            controller.hide();
        }
    }

    private void maybeShowController(boolean isForced) {
        if (!useController || player == null) {
            return;
        }
        int playbackState = player.getPlaybackState();
        boolean showIndefinitely = playbackState == ExoPlayer.STATE_IDLE
                || playbackState == ExoPlayer.STATE_ENDED
                || !player.getPlayWhenReady();
        boolean wasShowingIndefinitely = controller.isVisible() && controller.getShowTimeoutMs() <= 0;
        controller.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
        if (isForced || showIndefinitely || wasShowingIndefinitely) {
            controller.show();
        }
    }

    private void updateForCurrentTrackSelections() {
        if (player == null) {
            return;
        }
        TrackSelectionArray selections = player.getCurrentTrackSelections();
        for (int i = 0; i < selections.length; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_VIDEO && selections.get(i) != null) {
                // Video enabled so artwork must be hidden. If the shutter is closed, it will be opened in
                // onRenderedFirstFrame().
                hideArtwork();
                return;
            }
        }
        // Video disabled so the shutter must be closed.
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        // Display artwork if enabled and available, else hide it.
        if (useArtwork) {
            for (int i = 0; i < selections.length; i++) {
                TrackSelection selection = selections.get(i);
                if (selection != null) {
                    for (int j = 0; j < selection.length(); j++) {
                        Metadata metadata = selection.getFormat(j).metadata;
                        if (metadata != null && setArtworkFromMetadata(metadata)) {
                            return;
                        }
                    }
                }
            }
            if (setArtworkFromBitmap(defaultArtwork)) {
                return;
            }
        }
        // Artwork disabled or unavailable.
        hideArtwork();
    }

    public void setTrackSelectionHelper(@Nullable TrackSelectionHelper trackSelectionHelper) {
        mTrackSelectionHelper = trackSelectionHelper;
    }

    public void setActivity(@NonNull Activity activity) {
        this.mActivity = activity;
    }

    private boolean setArtworkFromMetadata(Metadata metadata) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry metadataEntry = metadata.get(i);
            if (metadataEntry instanceof ApicFrame) {
                byte[] bitmapData = ((ApicFrame) metadataEntry).pictureData;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                return setArtworkFromBitmap(bitmap);
            }
        }
        return false;
    }

    private boolean setArtworkFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if (bitmapWidth > 0 && bitmapHeight > 0) {
                if (contentFrame != null) {
                    contentFrame.setAspectRatio((float) bitmapWidth / bitmapHeight);
                }
                artworkView.setImageBitmap(bitmap);
                artworkView.setVisibility(VISIBLE);
                return true;
            }
        }
        return false;
    }

    private void hideArtwork() {
        if (artworkView != null) {
            artworkView.setImageResource(android.R.color.transparent); // Clears any bitmap reference.
            artworkView.setVisibility(INVISIBLE);
        }
    }

    public void setPlaybackInterface(@Nullable TubiPlaybackInterface playbackInterface) {
        this.playbackInterface = playbackInterface;
    }

    public void setMediaModel(@NonNull MediaModel mediaModel, boolean forceShowArtView) {
        this.mediaModel = mediaModel;
        if (!mediaModel.isAd() && forceShowArtView) {
            artworkView.setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(mediaModel.getArtworkUrl()).into(artworkView);
        }
        controller.setMediaModel(mediaModel);
    }

    public void setAvailableAdLeft(int count) {
        controller.setAvailableAdLeft(count);
    }

    @Override
    public void onSubtitlesToggle(boolean enabled) {
        View subtitles = getSubtitleView();
        if (subtitles != null) {
            subtitles.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onQualityTrackToggle(boolean enabled) {
        if (mTrackSelectionHelper != null && mActivity != null) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = mTrackSelectionHelper.getSelector()
                    .getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                mTrackSelectionHelper.showSelectionDialog(0, controller);
            }
        }
    }

    @Override
    public void cancelRunnable(@NonNull Runnable runnable) {
        removeCallbacks(runnable);
    }

    @Override
    public void postRunnable(@NonNull Runnable runnable, long millisDelay) {
        postDelayed(runnable, millisDelay);
        if (playbackInterface != null) {

            if (player != null && player.getPlayWhenReady() == true) {
                playbackInterface.onProgress(mediaModel, player.getCurrentPosition(), player.getDuration());
            }
        }

    }

    @Override
    public void hideAfterTimeout() {

    }

    private final class ComponentListener implements SimpleExoPlayer.VideoListener,
            TextRenderer.Output, ExoPlayer.EventListener {

        // TextRenderer.Output implementation

        @Override
        public void onCues(List<Cue> cues) {
            if (subtitleView != null) {
                subtitleView.onCues(cues);
            }
        }

        // SimpleExoPlayer.VideoListener implementation

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                float pixelWidthHeightRatio) {
            if (contentFrame != null) {
                float aspectRatio = height == 0 ? 1 : (width * pixelWidthHeightRatio) / height;
                contentFrame.setAspectRatio(aspectRatio);
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            if (shutterView != null) {
                shutterView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
            updateForCurrentTrackSelections();
        }

        // ExoPlayer.EventListener implementation

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            //            maybeShowController(false);
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // Do nothing.
        }

        @Override
        public void onPositionDiscontinuity() {
            // Do nothing.
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            // Do nothing.
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            // Do nothing.
        }

    }

}
