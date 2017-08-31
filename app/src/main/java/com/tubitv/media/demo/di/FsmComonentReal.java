package com.tubitv.media.demo.di;

import com.tubitv.media.demo.RealActivity;
import com.tubitv.media.di.annotation.ActicityScope;

import dagger.Component;

/**
 * Created by allensun on 8/29/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
@ActicityScope
@Component(modules = FSMModuleReal.class)
public interface FsmComonentReal {

    void inject(RealActivity activity);
}
