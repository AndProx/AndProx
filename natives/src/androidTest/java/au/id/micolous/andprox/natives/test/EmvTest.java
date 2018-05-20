package au.id.micolous.andprox.natives.test;

import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Test;

import au.id.micolous.andprox.natives.Natives;

/**
 * Runs EMV self-tests
 */

public class EmvTest {
    @Before
    public void setUp() throws Exception {

        Natives.initProxmark();
    }

    @Test
    @LargeTest
    public void testEmv() {
        Natives.sendCmd("hf emv test");
    }
}
