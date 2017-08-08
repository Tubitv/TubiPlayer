package com.tubitv.media.di;

import com.tubitv.media.fsm.concrete.factory.StateFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by allensun on 8/7/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
@Module
public class FSMModule {

    @Singleton
    @Provides
    StateFactory provideStateFactory() {
        return new StateFactory();
    }


}
