package com.tubitv.media.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.util.Util;
import com.tubitv.media.R;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.di.FSMModuleTesting;
import com.tubitv.media.di.component.DaggerFsmComonent;
import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.callback.AdInterface;
import com.tubitv.media.fsm.listener.AdPlayingMonitor;
import com.tubitv.media.fsm.listener.CuePointMonitor;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.helpers.MediaHelper;
import com.tubitv.media.interfaces.DoublePlayerInterface;
import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.AdRetriever;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.views.TubiExoPlayerView;

import javax.inject.Inject;


/**
 * Created by allensun on 7/24/17.
 */
public class DoubleViewTubiPlayerActivity extends TubiPlayerActivity implements DoublePlayerInterface {

    private SimpleExoPlayer adPlayer;

    private static final String TAG = "DoubleViewTubiPlayerAct";

    @Inject
    FsmPlayer fsmPlayer;

    @Inject
    PlayerUIController playerUIController;

    @Inject
    AdPlayingMonitor adPlayingMonitor;

    @Inject
    CuePointMonitor cuePointMonitor;

    @Inject
    AdMediaModel adMediaModel;

    @Inject
    AdRetriever adRetriever;

    @Inject
    AdInterface adInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerFsmComonent.builder().fSMModuleTesting(new FSMModuleTesting(null, null, null, null)).build().inject(this);

        prepareAds(adMediaModel);
    }


    @Override
    public void onStart() {
        super.onStart();

        if (Util.SDK_INT > 23) {
            setupAdPlayer();
            prepareFSM();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || adPlayer == null)) {
            setupAdPlayer();
            prepareFSM();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            if (adPlayer != null) {
                adPlayer.release();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if (adPlayer != null) {
                adPlayer.release();
            }
        }
    }

    private void setupAdPlayer() {
        adPlayer = ExoPlayerFactory.newSimpleInstance(this, mTrackSelector);
    }

    private void prepareFSM() {
        //update the playerUIController view
        playerUIController.setContentPlayer(mTubiExoPlayer);
        playerUIController.setAdPlayer(adPlayer);
        playerUIController.setExoPlayerView(mTubiPlayerView);

        //update the MediaModel
        fsmPlayer.setController(playerUIController);
        fsmPlayer.setMovieMedia(mediaModel);
        fsmPlayer.setAdMedia(adMediaModel);
        fsmPlayer.setAdRetriever(adRetriever);
        fsmPlayer.setAdServerInterface(adInterface);

        if (fsmPlayer != null) {
            fsmPlayer.transit(Input.MAKE_AD_CALL);
        }
    }

    @Override
    protected void onPlayerReady() {
        MediaSource mediaSource = createMediaSource();

        playMedia(mediaSource);
    }

    @Override
    protected void initLayout() {
        setContentView(R.layout.activity_double_tubi_player);

        mTubiPlayerView = (TubiExoPlayerView) findViewById(R.id.tubitv_player);
        mTubiPlayerView.requestFocus();
        mTubiPlayerView.setActivity(this);
        mTubiPlayerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

            }
        });

//        //showing ads
//        findViewById(R.id.button_show_ads).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onPrepareAds(adsMediaModel.getMediaSource());
//                showAds();
//            }
//        });

        //stop ads
        findViewById(R.id.button_stop_ads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adShowFinish();
            }
        });
    }

    /**
     * prepare the {@link AdMediaModel} to have the {@link MediaSource} insert into it.
     *
     * @param adMediaModel the adMediaModel.
     */
    private void prepareAds(@Nullable AdMediaModel adMediaModel) {

        for (MediaModel singleMedia : adMediaModel.getListOfAds()) {
            MediaSource adMediaSource = buildMediaSource(singleMedia);
            singleMedia.setMediaSource(adMediaSource);
        }
    }

    @Override
    public void onPrepareAds(MediaSource ads) {
        // only one instance of AdPlayer can be created.
        if (adPlayer == null) {
            adPlayer = ExoPlayerFactory.newSimpleInstance(this, mTrackSelector);
        }
        adPlayer.prepare(ads, true, true);
    }

    @Override
    public void showAds() {

        //TODO: keep track of the main content video's position in case network went bad

        updateResumePosition();

        //pause the primary content video player
        mTubiExoPlayer.setPlayWhenReady(false);

        //set the playerView to the ad video player, and player
        mTubiPlayerView.setPlayer(adPlayer, this);

        //updating the playerView and controller view for different mediaModel whether if it is ads or main content
//        mTubiPlayerView.setMediaModel(adsMediaModel, false);

        adPlayer.setPlayWhenReady(true);

        //add listener every time when display video
        adPlayer.addListener(adPlayingMonitor);
    }

    @Override
    public void adShowFinish() {
        //need to remove the listener in case the
        adPlayer.removeListener(adPlayingMonitor);

        mTubiPlayerView.setPlayer(mTubiExoPlayer, this);

        //updating the playerView and controller view for different mediaModel whether if it is ads or main content
        mTubiPlayerView.setMediaModel(mediaModel, false);

        onPlayerReady();
        mTubiExoPlayer.setPlayWhenReady(true);

        adPlayer.setPlayWhenReady(false);

        //clear the player and its media source
//        adsMediaModel.getMediaSource().releaseSource();

        Log.e(TAG, "Ad show Finish");

    }

    protected MediaSource createMediaSource() {

        mediaModel.setMediaSource(buildMediaSource(mediaModel));

        return MediaHelper.create(mediaModel).getConcatenatedMedia();
    }

    @Override
    public void onProgress(@Nullable MediaModel mediaModel, long milliseconds, long durationMillis) {
        Log.e(TAG, "onProgress: " + "milliseconds: " + milliseconds + " durationMillis: " + durationMillis);

        // monitor the movie progress.
        cuePointMonitor.onMovieProgress(milliseconds, durationMillis);
    }

    @Override
    public void onSeek(@Nullable MediaModel mediaModel, long oldPositionMillis, long newPositionMillis) {
        Log.d(TAG, "onSeek : " + "oldPositionMillis: " + oldPositionMillis + " newPositionMillis: " + newPositionMillis);
    }

    @Override
    public void onPlayToggle(@Nullable MediaModel mediaModel, boolean playing) {
        Log.d(TAG, "onPlayToggle :");
    }

    @Override
    public void onLearnMoreClick(@NonNull MediaModel mediaModel) {

    }

    @Override
    public void onSubtitles(@Nullable MediaModel mediaModel, boolean enabled) {

    }

    @Override
    public void onQuality(@Nullable MediaModel mediaModel) {

    }
}
