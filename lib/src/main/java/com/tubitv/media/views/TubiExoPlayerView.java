package com.tubitv.media.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
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
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.tubitv.media.R;
import com.tubitv.media.bindings.UserController;
import com.tubitv.media.helpers.TrackSelectionHelper;
import com.tubitv.media.interfaces.PlaybackActionCallback;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.ExoPlayerLogger;
import com.tubitv.ui.VaudTextView;
import com.tubitv.ui.VaudType;
import java.util.List;

/**
 * Created by stoyan tubi_tv_quality_on 3/22/17.
 */
@TargetApi(16)
public class TubiExoPlayerView extends FrameLayout {

    private static final String TAG = TubiExoPlayerView.class.getSimpleName();

    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

    private final AspectRatioFrameLayout contentFrame;
    private final View shutterView;
    private final View surfaceView;
    private final ImageView artworkView;
    private final SubtitleView subtitleView;
    private View mUserInteractionView;
    private final ComponentListener componentListener;

    private SimpleExoPlayer player;
    private boolean useArtwork;
    private Bitmap defaultArtwork;

    private UserController userController;

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
            mUserInteractionView = null;
            componentListener = null;
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

        userController = new UserController();
    }

    public void addUserInteractionView(View controlVidw) {
        if (controlVidw == null) {
            ExoPlayerLogger.e(TAG, "addUserInteractionView()----> adding empty view");
            return;
        }
        // Playback control view.
        View controllerPlaceholder = findViewById(R.id.exo_controller_placeholder);
        if (controllerPlaceholder != null) {
            // Note: rewindMs and fastForwardMs are passed via attrs, so we don't need to make explicit
            // calls to set them.
            mUserInteractionView = controlVidw;

            ViewGroup parent = ((ViewGroup) controllerPlaceholder.getParent());
            int controllerIndex = parent.indexOfChild(controllerPlaceholder);
            parent.removeView(controllerPlaceholder);
            parent.addView(mUserInteractionView, controllerIndex);
        } else {
            this.mUserInteractionView = null;
        }
    }

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

    public View getControlView() {
        return mUserInteractionView;
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
    public void setPlayer(SimpleExoPlayer player, @NonNull PlaybackActionCallback playbackActionCallback) {
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

        if (userController != null) {
            userController.setPlayer(player, playbackActionCallback);
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
     * Gets the {@link SubtitleView}.
     *
     * @return The {@link SubtitleView}, or {@code null} if the layout has been customized and the
     * subtitle view is not present.
     */
    public SubtitleView getSubtitleView() {
        return subtitleView;
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
        if (userController != null) {
            userController.setTrackSelectionHelper(trackSelectionHelper);
        }
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

    public void setMediaModel(@NonNull MediaModel mediaModel, boolean forceShowArtView) {
        if (!mediaModel.isAd() && forceShowArtView) {
            artworkView.setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(mediaModel.getArtworkUrl()).into(artworkView);
        }
        if (userController != null) {
            userController.setMediaModel(mediaModel);
        }
    }

    public void setAvailableAdLeft(int count) {
        if (userController != null) {
            userController.setAvailableAdLeft(count);
        }
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
