package com.tubitv.media.helpers;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.tubitv.media.R;
import com.tubitv.media.interfaces.TrackSelectionHelperInterface;
import com.tubitv.media.utilities.Utils;
import com.tubitv.media.views.TubiQualityDialogView;

/**
 * Created by stoyan on 5/12/17.
 */
public class TrackSelectionHelper implements MaterialDialog.SingleButtonCallback {

    private final MappingTrackSelector selector;

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
     * The activity that launches this dialog
     */
    @NonNull
    private final Activity mActivity;

    /**
     * Constructor for the helper
     *
     * @param activity                      The parent activity.
     * @param selector                      The track selector.
     */
    public TrackSelectionHelper(@NonNull Activity activity, @NonNull MappingTrackSelector selector) {
        this.mActivity = activity;
        this.selector = selector;
    }

    /**
     * Shows the selection dialog for a given renderer.
     *
     * @param rendererIndex The index of the renderer.
     * @param callback      The callback interface when the dialog is dismissed
     */
    public void showSelectionDialog(int rendererIndex, @NonNull TrackSelectionHelperInterface callback) {

        this.mCallbackInterface = callback;

        qualityDialogView = new TubiQualityDialogView(mActivity);
        MaterialDialog.Builder materialBuilder = new MaterialDialog.Builder(mActivity);
        materialBuilder.customView(qualityDialogView.buildQualityDialog(selector, rendererIndex), false)
                .title(mActivity.getResources().getString(R.string.track_selector_alert_quality_title))
                .backgroundColor(mActivity.getResources().getColor(R.color.tubi_tv_steel_grey))
                .positiveText(android.R.string.ok)
                .positiveColor(mActivity.getResources().getColor(R.color.tubi_tv_golden_gate))
                .onPositive(this)
                .show();
    }

    // DialogInterface.OnClickListener
    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        mCallbackInterface.onTrackSelected(qualityDialogView.onSelection());
        Utils.hideSystemUI(mActivity, false);
    }

    public
    @NonNull
    MappingTrackSelector getSelector() {
        return selector;
    }

}