package com.tubitv.demo;

import com.tubitv.media.models.AdMediaModel;
import com.tubitv.media.models.MediaModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by allensun on 10/6/17.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class AdMediaModelTest {

    AdMediaModel adMediaModel;

    @Before
    public void setup() {

        List<MediaModel> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            MediaModel mediaModel = MediaModel.ad(null, String.valueOf(i), false);
            list.add(mediaModel);
        }

        adMediaModel = new AdMediaModel(list);
    }

    @Test
    public void playeWatchVideo() {
        for (int i = 0; i < 4; i++) {
            assertThat(adMediaModel.nextAD().getClickThroughUrl().equalsIgnoreCase(String.valueOf(i)), is(true));
            adMediaModel.popFirstAd();
        }
    }
}
