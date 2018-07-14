package com.tubitv.demo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.tubitv.media.fsm.State;
import com.tubitv.media.fsm.concrete.MoviePlayingState;
import com.tubitv.media.fsm.concrete.factory.StateFactory;
import com.tubitv.media.models.MediaModel;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute tubi_tv_quality_on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.tubitv.media.test", appContext.getPackageName());
    }

    @Test
    public void testStateFactory() {
        State state = null;

        assertNull(state);

        StateFactory factory = new StateFactory();

        state = factory.createState(MoviePlayingState.class);

        assertTrue(state instanceof MoviePlayingState);

        //not expecting parameter type
        state = factory.createState(MediaModel.class);
        assertNull(state);

    }
}
