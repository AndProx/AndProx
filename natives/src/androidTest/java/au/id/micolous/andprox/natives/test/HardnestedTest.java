package au.id.micolous.andprox.natives.test;

import android.support.test.filters.FlakyTest;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.natives.Resources;

/**
 * Test hardnested attacks.
 *
 * This test is flaky due to high RAM requirements (~2GB). It crashes in the Android Emulator.
 */
@FlakyTest
public class HardnestedTest extends AndroidTestCase {
    private LogSink mLogSink;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mLogSink = new LogSink();

        // Hardnested attacks need some resources.
        if (!Resources.extractPM3Resources(getContext())) {
            throw new Exception("couldn't create resource directory");
        }

        // Now we can actually init
        Natives.initProxmark();
    }

    @LargeTest
    public void testHardnested() {
        mLogSink.reset();
        Natives.sendCmd("hf mf hardnested t 1 000000000000");
        String logLine = mLogSink.findInLogLines("Brute force phase completed. Key found: 000000000000");
        assertNotNull(logLine);
    }
}
