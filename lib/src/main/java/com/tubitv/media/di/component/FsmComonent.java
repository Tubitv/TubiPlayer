package com.tubitv.media.di.component;

import com.tubitv.media.activities.DoubleViewTubiPlayerActivity;
import com.tubitv.media.di.PlayerModuleDefault;
import com.tubitv.media.di.annotation.ActicityScope;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import dagger.Component;

/**
 * Created by allensun on 8/7/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
@ActicityScope
@Component(modules = PlayerModuleDefault.class)
public interface FsmComonent {

    //for testing purpose
    StateFactory getStateFactory();

    void inject(DoubleViewTubiPlayerActivity activity);
}
