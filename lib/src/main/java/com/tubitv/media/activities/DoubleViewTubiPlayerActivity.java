package com.tubitv.media.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.util.Util;
import com.tubitv.media.R;
import com.tubitv.media.controller.PlayerComponentController;
import com.tubitv.media.controller.PlayerUIController;
import com.tubitv.media.di.FSMModuleTesting;
import com.tubitv.media.di.component.DaggerFsmComonent;
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
    AdRetriever adRetriever;

    @Inject
    AdInterface adInterface;

    @Inject
    PlayerComponentController playerComponentController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FSMModuleTesting requirement object such as ExoPlayer haven't been initialized yet
        DaggerFsmComonent.builder().fSMModuleTesting(new FSMModuleTesting(null, null, null, null)).build().inject(this);
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
                releaseAdPlayer();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if (adPlayer != null) {
                releaseAdPlayer();
            }
        }
    }

    private void setupAdPlayer() {
        adPlayer = ExoPlayerFactory.newSimpleInstance(this, mTrackSelector);
    }

    private void releaseAdPlayer() {
        if (adPlayer != null) {
            updateAdResumePosition();
            adPlayer.release();
            adPlayer = null;
        }
    }

    private void updateAdResumePosition() {
        if (adPlayer != null && playerUIController != null) {
            int adResumeWindow = adPlayer.getCurrentWindowIndex();
            long adResumePosition = adPlayer.isCurrentWindowSeekable() ? Math.max(0, adPlayer.getCurrentPosition())
                    : C.TIME_UNSET;
            playerUIController.setAdResumeInfo(adResumeWindow, adResumePosition);
        }
    }

    @Override
    protected void updateResumePosition() {
        super.updateResumePosition();
        if (mTubiExoPlayer != null && playerUIController != null) {
            int resumeWindow = mTubiExoPlayer.getCurrentWindowIndex();
            long resumePosition = mTubiExoPlayer.isCurrentWindowSeekable() ? Math.max(0, mTubiExoPlayer.getCurrentPosition())
                    : C.TIME_UNSET;
            playerUIController.setMovieResumeInfo(resumeWindow, resumePosition);
        }
    }

    /**
     * prepare / set up FSM and inject all the elements into the FSM
     */
    private void prepareFSM() {
        //update the playerUIController view, need to update the view everything when two ExoPlayer being recreated in activity lifecycle.
        playerUIController.setContentPlayer(mTubiExoPlayer);
        playerUIController.setAdPlayer(adPlayer);
        playerUIController.setExoPlayerView(mTubiPlayerView);

        //update the MediaModel
        fsmPlayer.setController(playerUIController);
        fsmPlayer.setMovieMedia(mediaModel);
//        fsmPlayer.setAdMedia(adMediaModel);
        fsmPlayer.setAdRetriever(adRetriever);
        fsmPlayer.setAdServerInterface(adInterface);

        //set the PlayerComponentController.
        playerComponentController.setAdPlayingMonitor(adPlayingMonitor);
        playerComponentController.setTubiPlaybackInterface(this);
        playerComponentController.setDoublePlayerInterface(this);
        fsmPlayer.setPlayerComponentController(playerComponentController);

        //let disable pre-roll ads first, assume that movie will always be played first.
//        if (fsmPlayer != null) {
//            fsmPlayer.transit(Input.MAKE_AD_CALL);
//        }
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

    @Override
    public void onPrepareAds(@Nullable AdMediaModel adMediaModel) {

        for (MediaModel singleMedia : adMediaModel.getListOfAds()) {
            MediaSource adMediaSource = buildMediaSource(singleMedia);
            singleMedia.setMediaSource(adMediaSource);
        }
    }


    @Override
    public void showAds() {

//        //TODO: keep track of the main content video's position in case network went bad
//
//        updateResumePosition();
//
//        //pause the primary content video player
//        mTubiExoPlayer.setPlayWhenReady(false);
//
//        //set the playerView to the ad video player, and player
//        mTubiPlayerView.setPlayer(adPlayer, this);
//
//        //updating the playerView and controller view for different mediaModel whether if it is ads or main content
//        mTubiPlayerView.setMediaModel(null, false);
//
//        adPlayer.setPlayWhenReady(true);
//
//        //add listener every time when display video
//        adPlayer.addListener(adPlayingMonitor);
    }

    @Override
    public void adShowFinish() {
//        //need to remove the listener in case the
//        adPlayer.removeListener(adPlayingMonitor);
//
//        mTubiPlayerView.setPlayer(mTubiExoPlayer, this);
//
//        //updating the playerView and controller view for different mediaModel whether if it is ads or main content
//        mTubiPlayerView.setMediaModel(mediaModel, false);
//
//        onPlayerReady();
//        mTubiExoPlayer.setPlayWhenReady(true);
//
//        adPlayer.setPlayWhenReady(false);
//
//        //clear the player and its media source
////        adsMediaModel.getMediaSource().releaseSource();
//
//        Log.e(TAG, "Ad show Finish");

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
