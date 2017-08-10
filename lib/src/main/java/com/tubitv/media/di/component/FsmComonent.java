package com.tubitv.media.di.component;

import com.tubitv.media.activities.DoubleViewTubiPlayerActivity;
import com.tubitv.media.di.FSMModule;
import com.tubitv.media.fsm.concrete.factory.StateFactory;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by allensun on 8/7/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
@Singleton
@Component(modules = FSMModule.class)
public interface FsmComonent {

    //for testing purpose
    StateFactory getStateFactory();

    void inject(DoubleViewTubiPlayerActivity activity);
}
