/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2017-2018 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Under section 7 of the GNU General Public License v3, the following additional
 * terms apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */
package au.id.micolous.andprox.natives.test;

import android.support.test.filters.FlakyTest;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.natives.Resources;
import au.id.micolous.andprox.natives.test.utils.LogSink;

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
