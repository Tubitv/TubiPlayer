package com.tubitv.media.di;

import android.view.View;
import android.webkit.WebView;

import com.google.android.exoplayer2.ExoPlayer;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.fsm.state_machine.FsmPlayer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by allensun on 8/7/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
@Module
public class FSMModule {

    private ExoPlayer mainPlayer;

    private ExoPlayer adPlayer;

    private WebView webView;

    private View rootView;

    public FSMModule(ExoPlayer mainPlayer, ExoPlayer adPlayer, WebView webView, View rootView) {
        this.mainPlayer = mainPlayer;
        this.adPlayer = adPlayer;
        this.webView = webView;
        this.rootView = rootView;
    }

    @Singleton
    @Provides
    StateFactory provideStateFactory() {
        return new StateFactory();
    }

    @Singleton
    @Provides
    FsmPlayer provideFsmPlayer(StateFactory factory){
        return new FsmPlayer(factory);
    }


//    PlayerUIController provideController(){
//        return new PlayerUIController()
//    }




}
