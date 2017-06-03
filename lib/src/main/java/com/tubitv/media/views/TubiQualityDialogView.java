package com.tubitv.media.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.util.MimeTypes;
import com.tubitv.media.R;
import com.tubitv.media.databinding.ViewTubiQualityDialogBinding;
import com.tubitv.media.utilities.Utils;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by stoyan on 6/2/17.
 */
public class TubiQualityDialogView extends LinearLayout implements View.OnClickListener {

    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();

    /**
     * Attached state of this view
     */
    private boolean isAttachedToWindow;

    private MappingTrackSelector selector;

    /**
     * The track groups for the playing media
     */
    private TrackGroupArray trackGroups;

    private MappingTrackSelector.MappedTrackInfo trackInfo;

    private MappingTrackSelector.SelectionOverride override;

    private int rendererIndex;

    /**
     * The {@link TubiRadioButton}s for the media tracks
     */
    private TubiRadioButton[][] trackViews;

    /**
     * The auto quality selection that allows the track selector to select the optimal track
     */
    private TubiRadioButton qualityAutoView;

    /**
     * The binding view
     */
    private ViewTubiQualityDialogBinding mBinding;

    public TubiQualityDialogView(Context context) {
        this(context, null);
    }

    public TubiQualityDialogView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TubiQualityDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
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
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
    }

    @Override
    public void onClick(View view) {
        if (view == qualityAutoView) {
            override = null;
        } else {
            @SuppressWarnings("unchecked")
            Pair<Integer, Integer> tag = (Pair<Integer, Integer>) view.getTag();
            int groupIndex = tag.first;
            int trackIndex = tag.second;
            if (override == null || override.groupIndex != groupIndex) {
                override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, groupIndex, trackIndex);
            } else {
                // The group being modified is adaptive and we already have a non-null override.
                boolean isEnabled = ((TubiRadioButton) view).isChecked();
                if (isEnabled) {
                    // Remove the track from the override.
                    override = null;
                } else {
                    // Add the track to the override.
                    setOverride(groupIndex, trackIndex);
                }
            }
        }
        // Update the views with the new state.
        updateViews();
    }

    private void initLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.view_tubi_quality_dialog, this, true);
    }

    /**
     * Method called when the parent {@link com.afollestad.materialdialogs.MaterialDialog} is
     * dismissed. Sets the new quality
     *
     * @return True if the quality selector is overridden, false otherwise(ie. its on auto select)
     */
    public boolean onSelection() {
        if (selector != null) {
            if (override != null) {
                selector.setSelectionOverride(rendererIndex, trackGroups, override);
                return true;
            } else {
                selector.clearSelectionOverrides(rendererIndex);
                return false;
            }
        }
        return false;
    }

    /**
     * Builds the quality selection list based on the media tracks
     *
     * @param selector      The selector to get all the available tracks
     * @param rendererIndex The render index
     * @return The selection view
     */
    public View buildQualityDialog(@NonNull MappingTrackSelector selector, int rendererIndex) {
        setRendererIndex(rendererIndex);
        setSelector(selector);


        // View for clearing the override to allow the selector to use its default selection logic.
        qualityAutoView = new TubiRadioButton(getContext());
        qualityAutoView.setText(R.string.track_selector_alert_auto);
        qualityAutoView.setFocusable(true);
        qualityAutoView.setOnClickListener(this);
        mBinding.viewTubiQualityDialogLl.addView(qualityAutoView);

        // Per-track views.
        trackViews = new TubiRadioButton[trackGroups.length][];
        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            TrackGroup group = trackGroups.get(groupIndex);
            trackViews[groupIndex] = new TubiRadioButton[group.length];
            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                TubiRadioButton trackView = new TubiRadioButton(getContext());
                trackView.setText(buildTrackName(group.getFormat(trackIndex)));
                if (trackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex)
                        == RendererCapabilities.FORMAT_HANDLED) {
                    trackView.setFocusable(true);
                    trackView.setTag(Pair.create(groupIndex, trackIndex));
                } else {
                    trackView.setFocusable(false);
                    trackView.setEnabled(false);
                }
                trackView.setOnClickListener(this);
                trackViews[groupIndex][trackIndex] = trackView;
                mBinding.viewTubiQualityDialogLl.addView(trackView);
            }
        }

        updateViews();
        return this;
    }

    private void updateViews() {
        for (int i = 0; i < trackViews.length; i++) {
            for (int j = 0; j < trackViews[i].length; j++) {
                if (override == null) {
                    trackViews[i][j].setChecked(false);
                } else {
                    trackViews[i][j].setChecked(override != null && override.groupIndex == i
                            && override.containsTrack(j));
                }
            }
        }
        qualityAutoView.setChecked(override == null);
    }

    private void setOverride(int group, int... tracks) {
        override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, group, tracks);
    }

    private static int[] getTracksRemoving(@NonNull MappingTrackSelector.SelectionOverride override, int removedTrack) {
        int[] tracks = new int[override.length - 1];
        int trackCount = 0;
        for (int i = 0; i < tracks.length + 1; i++) {
            int track = override.tracks[i];
            if (track != removedTrack) {
                tracks[trackCount++] = track;
            }
        }
        return tracks;
    }

    // Track array manipulation.
    private static int[] getTracksAdding(@NonNull MappingTrackSelector.SelectionOverride override, int addedTrack) {
        int[] tracks = override.tracks;
        tracks = Arrays.copyOf(tracks, tracks.length + 1);
        tracks[tracks.length - 1] = addedTrack;
        return tracks;
    }

    private void removeAllTracks() {
        selector.clearSelectionOverrides();
        setOverride(selector.getSelectionOverride(rendererIndex, trackGroups));
    }

    // Track name construction.

    private static String buildTrackName(Format format) {
        String trackName;
        if (MimeTypes.isVideo(format.sampleMimeType)) {
            trackName = !Utils.isEmpty(buildResolutionString(format)) ? buildResolutionString(format) :
                    !Utils.isEmpty(buildBitrateString(format)) ? buildBitrateString(format) :
                            buildTrackIdString(format);
        } else if (MimeTypes.isAudio(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(joinWithSeparator(
                    buildLanguageString(format), buildAudioPropertyString(format)),
                    buildBitrateString(format)), buildTrackIdString(format)),
                    buildSampleMimeTypeString(format));
        } else {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildBitrateString(format)), buildTrackIdString(format)),
                    buildSampleMimeTypeString(format));
        }
        return trackName.length() == 0 ? "unknown" : trackName;
    }

    private static String buildResolutionString(Format format) {
        return format.width == Format.NO_VALUE || format.height == Format.NO_VALUE
                ? "" : format.width + "x" + format.height;
    }

    private static String buildAudioPropertyString(Format format) {
        return format.channelCount == Format.NO_VALUE || format.sampleRate == Format.NO_VALUE
                ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
    }

    private static String buildLanguageString(Format format) {
        return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
                : format.language;
    }

    private static String buildBitrateString(Format format) {
        return format.bitrate == Format.NO_VALUE ? ""
                : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
    }

    private static String joinWithSeparator(String first, String second) {
        return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
    }

    private static String buildTrackIdString(Format format) {
        return format.id == null ? "" : ("id:" + format.id);
    }

    private static String buildSampleMimeTypeString(Format format) {
        return format.sampleMimeType == null ? "" : format.sampleMimeType;
    }

    private void setSelector(MappingTrackSelector selector) {
        this.selector = selector;
        setTrackInfo(selector.getCurrentMappedTrackInfo());
        setTrackGroups(trackInfo.getTrackGroups(rendererIndex));
        setOverride(selector.getSelectionOverride(rendererIndex, trackGroups));
    }

    private void setTrackGroups(@NonNull TrackGroupArray trackGroups) {
        this.trackGroups = trackGroups;
    }

    private void setTrackInfo(@NonNull MappingTrackSelector.MappedTrackInfo trackInfo) {
        this.trackInfo = trackInfo;
    }

    private void setOverride(MappingTrackSelector.SelectionOverride override) {
        this.override = override;
    }

    private void setRendererIndex(int rendererIndex) {
        this.rendererIndex = rendererIndex;
    }
}
