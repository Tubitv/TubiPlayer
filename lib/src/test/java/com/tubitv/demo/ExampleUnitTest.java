package com.tubitv.demo;

import com.tubitv.media.fsm.listener.CuePointMonitor;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Example local unit test, which will execute tubi_tv_quality_on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    long[] a;

    long range = 0;

    @Before
    public void setup() {
        a = new long[] { 0, 4, 10, 25, 29, 40, 50, 60, 65, 68, 909 };
    }

    @Test
    public void searchWithOutRange() throws Exception {
        int resultPos = CuePointMonitor.binarySerchExactly(a, 60 + range);
        assertThat(resultPos, is(7));

        resultPos = CuePointMonitor.binarySerchExactly(a, 0 + range);
        assertThat(resultPos >= 0, is(true));

        resultPos = CuePointMonitor.binarySerchExactly(a, 10 + range);
        assertThat(resultPos >= 0, is(true));

        resultPos = CuePointMonitor.binarySerchExactly(a, 25 + range);
        assertThat(resultPos >= 0, is(true));

        resultPos = CuePointMonitor.binarySerchExactly(a, 909 + range);
        assertThat(resultPos, is(10));

    }

    //    @Test
    //    public void searchWithRange() throws Exception {
    //        int resultPos = CuePointMonitor.binarySerchWithRange(a,60 + range);
    //        assertThat(resultPos,is(7));
    //
    //        resultPos = CuePointMonitor.binarySerchWithRange(a,0+ range);
    //        assertThat(resultPos>=0,is(true));
    //
    //        resultPos = CuePointMonitor.binarySerchWithRange(a,10+ range);
    //        assertThat(resultPos>=0,is(true));
    //
    //        resultPos = CuePointMonitor.binarySerchWithRange(a,25+ range);
    //        assertThat(resultPos>=0,is(true));
    //
    //        resultPos = CuePointMonitor.binarySerchWithRange(a, 909+ range);
    //        assertThat(resultPos,is(10));
    //    }

    @Test
    public void test() {
        long[] temp = removePreroll(a);
        //        int size = temp.length;

        for (int i = 0; i < temp.length; i++) {
            System.out.print(" " + temp[i] + " ");
        }

    }

    private long[] removePreroll(long[] array) {
        if (array.length <= 1) {
            return array;
        }

        return Arrays.copyOfRange(array, 1, array.length);
    }
}