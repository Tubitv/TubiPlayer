package com.tubitv.media.helpers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.tubitv.media.R;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiQualityDialogView;
import com.tubitv.media.views.TubiRadioButton;

import java.util.Arrays;

/**
 * Created by stoyan on 5/12/17.
 */
public class TrackSelectionHelper implements View.OnClickListener, MaterialDialog.SingleButtonCallback {

    private static final TrackSelection.Factory FIXED_FACTORY = new FixedTrackSelection.Factory();
    private static final TrackSelection.Factory RANDOM_FACTORY = new RandomTrackSelection.Factory();

    private final MappingTrackSelector selector;
    private final TrackSelection.Factory adaptiveTrackSelectionFactory;

    private MappingTrackSelector.MappedTrackInfo trackInfo;
    private int rendererIndex;
    private TrackGroupArray trackGroups;
    private boolean[] trackGroupsAdaptive;
    private boolean isDisabled;
    private MappingTrackSelector.SelectionOverride override;

    private TubiQualityDialogView qualityDialogView;

    /**
     * The activity that launches this dialog
     */
    @NonNull
    private final Activity mActivity;

    /**
     * @param activity                      The parent activity.
     * @param selector                      The track selector.
     * @param adaptiveTrackSelectionFactory A factory for adaptive {@link TrackSelection}s, or null
     *                                      if the selection helper should not support adaptive tracks.
     */
    public TrackSelectionHelper(@NonNull Activity activity, @NonNull MappingTrackSelector selector,
                                TrackSelection.Factory adaptiveTrackSelectionFactory) {
        this.mActivity = activity;
        this.selector = selector;
        this.adaptiveTrackSelectionFactory = adaptiveTrackSelectionFactory;
    }

    /**
     * Shows the selection dialog for a given renderer.
     *
     * @param title         The dialog's title.
     * @param trackInfo     The current track information.
     * @param rendererIndex The index of the renderer.
     */
    public void showSelectionDialog(CharSequence title, MappingTrackSelector.MappedTrackInfo trackInfo,
                                    int rendererIndex) {

        this.trackInfo = trackInfo;
        this.rendererIndex = rendererIndex;

        trackGroups = trackInfo.getTrackGroups(rendererIndex);
        trackGroupsAdaptive = new boolean[trackGroups.length];
        for (int i = 0; i < trackGroups.length; i++) {
            trackGroupsAdaptive[i] = adaptiveTrackSelectionFactory != null
                    && trackInfo.getAdaptiveSupport(rendererIndex, i, false)
                    != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED
                    && trackGroups.get(i).length > 1;
        }
        isDisabled = selector.getRendererDisabled(rendererIndex);
        override = selector.getSelectionOverride(rendererIndex, trackGroups);

        qualityDialogView = new TubiQualityDialogView(mActivity);
        MaterialDialog.Builder materialBuilder = new MaterialDialog.Builder(mActivity);
        materialBuilder.customView(qualityDialogView.buildQualityDialog(trackGroups,trackInfo, rendererIndex), false)
                .title(title)
                .backgroundColor(mActivity.getResources().getColor(R.color.tubi_tv_steel_grey))
                .positiveText(android.R.string.ok)
                .positiveColor(mActivity.getResources().getColor(R.color.tubi_tv_golden_gate))
                .onPositive(this)
                .show();
    }

    // DialogInterface.OnClickListener
    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        selector.setRendererDisabled(rendererIndex, isDisabled);
        if (override != null) {
            selector.setSelectionOverride(rendererIndex, trackGroups, override);
        } else {
            selector.clearSelectionOverrides(rendererIndex);
        }
        Utils.hideSystemUI(mActivity, false);
    }

    // View.OnClickListener

    @Override
    public void onClick(View view) {
        if (view == qualityDialogView) {
            isDisabled = false;
            override = null;
        }
//        else if (view == enableRandomAdaptationView) {
//            setOverride(override.groupIndex, override.tracks, !enableRandomAdaptationView.isChecked());
//        }
        else {
            isDisabled = false;
            @SuppressWarnings("unchecked")
            Pair<Integer, Integer> tag = (Pair<Integer, Integer>) view.getTag();
            int groupIndex = tag.first;
            int trackIndex = tag.second;
            if (!trackGroupsAdaptive[groupIndex] || override == null
                    || override.groupIndex != groupIndex) {
                override = new MappingTrackSelector.SelectionOverride(FIXED_FACTORY, groupIndex, trackIndex);
            } else {
                // The group being modified is adaptive and we already have a non-null override.
                boolean isEnabled = ((TubiRadioButton) view).isChecked();
                int overrideLength = override.length;
                if (isEnabled) {
                    // Remove the track from the override.
                    if (overrideLength == 1) {
                        // The last track is being removed, so the override becomes empty.
                        override = null;
                        isDisabled = true;
                    } else {
//                        setOverride(groupIndex, getTracksRemoving(override, trackIndex),
//                                enableRandomAdaptationView.isChecked());
                    }
                } else {
                    // Add the track to the override.
//                    setOverride(groupIndex, getTracksAdding(override, trackIndex),
//                            enableRandomAdaptationView.isChecked());
                }
            }
        }
        // Update the views with the new state.
//        updateViews();
    }

    private void setOverride(int group, int[] tracks, boolean enableRandomAdaptation) {
        TrackSelection.Factory factory = tracks.length == 1 ? FIXED_FACTORY
                : (enableRandomAdaptation ? RANDOM_FACTORY : adaptiveTrackSelectionFactory);
        override = new MappingTrackSelector.SelectionOverride(factory, group, tracks);
    }

    // Track array manipulation.

    private static int[] getTracksAdding(MappingTrackSelector.SelectionOverride override, int addedTrack) {
        int[] tracks = override.tracks;
        tracks = Arrays.copyOf(tracks, tracks.length + 1);
        tracks[tracks.length - 1] = addedTrack;
        return tracks;
    }

    private static int[] getTracksRemoving(MappingTrackSelector.SelectionOverride override, int removedTrack) {
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

    public
    @NonNull
    MappingTrackSelector getSelector() {
        return selector;
    }

}