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
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.util.MimeTypes;
import com.tubitv.media.R;
import com.tubitv.media.databinding.ViewTubiQualityDialogBinding;
import com.tubitv.media.utilities.Utils;

import java.util.Locale;

/**
 * Created by stoyan on 6/2/17.
 */
public class TubiQualityDialogView extends LinearLayout implements View.OnClickListener {
    /**
     * Attached state of this view
     */
    private boolean isAttachedToWindow;

    /**
     * The track groups for the playing media
     */
    private TrackGroupArray trackGroups;

    private MappingTrackSelector.MappedTrackInfo trackInfo;

    private int rendererIndex;

    /**
     * The {@link TubiRadioButton}s for the media tracks
     */
    private TubiRadioButton[][] trackViews;

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
    public void onClick(View v) {

    }

    private void initLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.view_tubi_quality_dialog, this, true);
    }

    public View buildQualityDialog(@NonNull TrackGroupArray trackGroups, @NonNull MappingTrackSelector.MappedTrackInfo trackInfo, int rendererIndex) {
        setTrackGroups(trackGroups);
        setTrackInfo(trackInfo);
        setRendererIndex(rendererIndex);

        // View for clearing the override to allow the selector to use its default selection logic.
        TubiRadioButton qualityAutoView = new TubiRadioButton(getContext());
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
                    trackView.setOnClickListener(this);
                } else {
                    trackView.setFocusable(false);
                    trackView.setEnabled(false);
                }
                trackViews[groupIndex][trackIndex] = trackView;
                mBinding.viewTubiQualityDialogLl.addView(trackView);
            }
        }

        updateViews();
        return this;
    }

    private void updateViews() {
//        disableView.setChecked(isDisabled);
//        defaultView.setChecked(!isDisabled && override == null);
//        for (int i = 0; i < trackViews.length; i++) {
//            for (int j = 0; j < trackViews[i].length; j++) {
//                trackViews[i][j].setChecked(override != null && override.groupIndex == i
//                        && override.containsTrack(j));
//            }
//        }
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

    public void setTrackGroups(@NonNull TrackGroupArray trackGroups) {
        this.trackGroups = trackGroups;
    }

    public void setTrackInfo(@NonNull MappingTrackSelector.MappedTrackInfo trackInfo) {
        this.trackInfo = trackInfo;
    }

    public void setRendererIndex(int rendererIndex) {
        this.rendererIndex = rendererIndex;
    }

}
