package com.tubitv.media.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.util.Util;
import com.tubitv.media.R;
import com.tubitv.media.helpers.MediaHelper;
import com.tubitv.media.interfaces.DoublePlayerInterface;
import com.tubitv.media.models.MediaModel;
import com.tubitv.media.utilities.AdVideoEventListener;
import com.tubitv.media.views.TubiExoPlayerView;

/**
 * Created by allensun on 7/24/17.
 */
public class DoubleViewTubiPlayerActivity extends TubiPlayerActivity implements DoublePlayerInterface {

    private SimpleExoPlayer adPlayer;

    private MediaModel adsMediaModel;

    private ExoPlayer.EventListener adVideoEventListener;

    private static final String TAG = "DoubleViewTubiPlayerAct";


    @Override
    public void onProgress(@Nullable MediaModel mediaModel, long milliseconds, long durationMillis) {

        Log.d(TAG, "onProgress: "+ "milliseconds: " + milliseconds + " durationMillis: "+ durationMillis);
    }

    @Override
    public void onSeek(@Nullable MediaModel mediaModel, long oldPositionMillis, long newPositionMillis) {
        Log.d(TAG, "onSeek : " + "oldPositionMillis: "+ oldPositionMillis + " newPositionMillis: "+newPositionMillis);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adVideoEventListener = new AdVideoEventListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initAds();

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

        //showing ads
        findViewById(R.id.button_show_ads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrepareAds(adsMediaModel.getMediaSource());
                showAds();
            }
        });

        //stop ads
        findViewById(R.id.button_stop_ads).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                adShowFinish();
            }
        });
    }

    private void initAds() {

        // sports car
        adsMediaModel = MediaModel.ad("http://c13.adrise.tv/ads/transcodes/004130/1050072/v0617070213-640x360-SD-,764,1057,k.mp4.m3u8",
                "https://github.com/stoyand");

        MediaSource adMediaSource = buildMediaSource(adsMediaModel);
        adsMediaModel.setMediaSource(adMediaSource);

    }

    @Override
    public void onPrepareAds(MediaSource ads) {
        // only one instance of AdPlayer can be created.
        if (adPlayer == null) {
            adPlayer = ExoPlayerFactory.newSimpleInstance(this, mTrackSelector);
            adPlayer.addListener(adVideoEventListener);
        }
        adPlayer.prepare(ads, true, true);
    }

    @Override
    public void showAds() {

        //keep tracks of main content video's position
        updateResumePosition();

        //pause the primary content video player
        mTubiExoPlayer.setPlayWhenReady(false);


        //set the playerView to the ad video player, and player
        mTubiPlayerView.setPlayer(adPlayer, this);

        //updating the playerView and controller view for different mediaModel whether if it is ads or main content
        mTubiPlayerView.setMediaModel(adsMediaModel,false);

        adPlayer.setPlayWhenReady(true);

    }


    @Override
    public void adShowFinish() {
        adPlayer.setPlayWhenReady(false);

        mTubiPlayerView.setPlayer(mTubiExoPlayer, this);

        //updating the playerView and controller view for different mediaModel whether if it is ads or main content
        mTubiPlayerView.setMediaModel(mediaModel,false);

        mTubiExoPlayer.setPlayWhenReady(true);

        Log.e(TAG, "Ad show Finish");

    }

    protected MediaSource createMediaSource() {

        mediaModel.setMediaSource(buildMediaSource(mediaModel));

        // blue apron
//        MediaModel ad1 = MediaModel.ad("http://c11.adrise.tv/ads/transcodes/003572/940826/v0329081907-1280x720-HD-,740,1285,1622,2138,3632,k.mp4.m3u8",
//                null);
//        ad1.setMediaSource(buildMediaSource(ad1));
//
//        // sports car
//        MediaModel ad2 = MediaModel.ad("http://c13.adrise.tv/ads/transcodes/004130/1050072/v0617070213-640x360-SD-,764,1057,k.mp4.m3u8",
//                "https://github.com/stoyand");
//        ad2.setMediaSource(buildMediaSource(ad2));
        return MediaHelper.create(mediaModel).getConcatenatedMedia();
    }
}
