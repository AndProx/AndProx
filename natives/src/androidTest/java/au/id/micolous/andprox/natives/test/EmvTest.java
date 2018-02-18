package au.id.micolous.andprox.natives.test;

import android.test.suitebuilder.annotation.LargeTest;

import junit.framework.TestCase;

import au.id.micolous.andprox.natives.Natives;

/**
 * Runs EMV self-tests
 */

public class EmvTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Natives.initProxmark();
    }

    @LargeTest
    public void testEmv() {
        Natives.sendCmd("hf emv test");
    }
}
