package com.tubitv.media.helpers;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.tubitv.media.R;
import com.tubitv.media.interfaces.TrackSelectionHelperInterface;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiQualityDialogView;

/**
 * Created by stoyan on 5/12/17.
 */
public class TrackSelectionHelper implements DialogInterface.OnDismissListener {

    /**
     * The activity that launches this dialog
     */
    @NonNull
    private final Activity mActivity;
    @NonNull
    private final TrackSelection.Factory adaptiveTrackSelectionFactory;
    /**
     * The track selector used to set the video quality
     */
    private MappingTrackSelector selector;
    /**
     * The dialog view with layouts for the tracks to be selected
     */
    @NonNull
    private TubiQualityDialogView qualityDialogView;
    /**
     * The callback interface when the dialog is dismissed to inform it whether the
     * track selector was overridden
     */
    @NonNull
    private TrackSelectionHelperInterface mCallbackInterface;

    /**
     * Constructor for the helper
     *
     * @param activity The parent activity.
     * @param selector The track selector.
     */
    public TrackSelectionHelper(@NonNull Activity activity, @NonNull MappingTrackSelector selector,
            @NonNull TrackSelection.Factory adaptiveTrackSelectionFactory) {
        this.mActivity = activity;
        this.selector = selector;
        this.adaptiveTrackSelectionFactory = adaptiveTrackSelectionFactory;
    }

    /**
     * Shows the selection dialog for a given renderer.
     *
     * @param rendererIndex The index of the renderer.
     * @param callback      The callback interface when the dialog is dismissed
     */
    public void showSelectionDialog(int rendererIndex, @Nullable TrackSelectionHelperInterface callback) {

        this.mCallbackInterface = callback;

        qualityDialogView = new TubiQualityDialogView(mActivity);
        qualityDialogView.setAdaptiveTrackSelectionFactory(adaptiveTrackSelectionFactory);

        MaterialDialog.Builder materialBuilder = new MaterialDialog.Builder(mActivity);
        materialBuilder.customView(qualityDialogView.buildQualityDialog(selector, rendererIndex), false)
                .title(mActivity.getResources().getString(R.string.track_selector_alert_quality_title))
                .backgroundColor(mActivity.getResources().getColor(R.color.tubi_tv_steel_grey))
                .positiveText(android.R.string.ok)
                .positiveColor(mActivity.getResources().getColor(R.color.tubi_tv_golden_gate))
                .onPositive(qualityDialogView)
                .dismissListener(this)
                .show();
    }

    //Perform changes on the dismiss, no need for just ok click
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mCallbackInterface != null) {
            mCallbackInterface.onTrackSelected(qualityDialogView.onSelection());
        }
        Utils.hideSystemUI(mActivity, false);
    }

    public
    @NonNull
    MappingTrackSelector getSelector() {
        return selector;
    }

}