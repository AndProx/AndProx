package au.id.micolous.andprox.natives.test;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.Suppress;

import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.natives.Resources;

/**
 * Test hardnested attacks.
 *
 * This test is flaky due to high RAM requirements (~2GB).
 */
@Suppress
public class HardnestedTest extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Hardnested attacks need some resources.
        if (!Resources.extractPM3Resources(getContext())) {
            throw new Exception("couldn't create resource directory");
        }

        // Now we can actually init
        Natives.initProxmark();
        Natives.unsetSerialPort();

    }

    @MediumTest
    public void testHardnested() {
        Natives.sendCmd("hf mf hardnested t 1 000000000000");
    }
}
