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
package au.id.micolous.andprox.natives27.androidTest;


import android.support.test.filters.SmallTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

import au.id.micolous.andprox.natives.NativeSerialWrapper;
import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.natives27.androidTest.utils.LogSink;
import au.id.micolous.andprox.natives27.androidTest.utils.UsbCommandMatcher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Basic PM3 library test.
 *
 * This test case mocks out {@link NativeSerialWrapper} in order to make a virtual PM3 device, which
 * can respond to {@link #CMD_VERSION}. It will then spin up the PM3 client in JNI with this mocked
 * {@link NativeSerialWrapper}, and verifies that the client correctly communicated with the device,
 * and that it was able to log a custom version string.
 *
 * This version of the test uses Android Instrumented Tests, which run on a (virtual or physical)
 * Android device. It has complete access to all the Android APIs.
 */
@SmallTest
public class HardwareCommsTest {
    private static final long CMD_VERSION = 0x107;
    private static final long CMD_ACK = 0xff;

    @Mock
    private NativeSerialWrapper mNativeSerialWrapper;

    private LogSink mLogSink = new LogSink();

    private boolean versionPending = false;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mNativeSerialWrapper.send(ArgumentMatchers.argThat(new UsbCommandMatcher(CMD_VERSION))))
                .thenAnswer(invocation -> {
                    versionPending = true;
                    return true;
                });

        when(mNativeSerialWrapper.receive(ArgumentMatchers.any(byte[].class)))
                .thenAnswer(invocation -> {
                    if (versionPending) {
                        versionPending = false;

                        byte[] buffer = invocation.getArgument(0);

                        // Copy a reply into the buffer.
                        Arrays.fill(buffer, (byte)0);
                        ByteBuffer bb = ByteBuffer.wrap(buffer);
                        bb.order(ByteOrder.LITTLE_ENDIAN);
                        bb.putLong(CMD_ACK);
                        bb.putLong(0x270B0A40); // AT91SAM7S512 Rev A
                        bb.putLong(0x100); // 512 bytes used
                        bb.putLong(0); // unused value
                        // Whatever we write next is printed to the log.
                        bb.put("hello HardwareCommsTest".getBytes(Charset.forName("UTF-8")));
                        return UsbCommandMatcher.USB_COMMAND_LENGTH;
                    }

                    return null;
                });

        Natives.initProxmark();
        Natives.registerPrintHandler(mLogSink);
    }

    @Test
    public void testInitSequence() {
        mLogSink.reset();
        assertTrue("The device must be offline at the start", Natives.isOffline());
        Natives.startReaderThread(mNativeSerialWrapper);
        Natives.sendCmdVersion();

        verify(mNativeSerialWrapper).send(ArgumentMatchers.argThat(new UsbCommandMatcher(CMD_VERSION)));

        assertNull("We shouldn't find an error in the log", mLogSink.findInLogLines("got no response"));
        assertNotNull("We should find our custom message in the log", mLogSink.findInLogLines("hello HardwareCommsTest"));

        Natives.stopReaderThread();
    }

    @Test
    public void testDoubleClose() {
        mLogSink.reset();
        Natives.startReaderThread(mNativeSerialWrapper);
        Natives.stopReaderThread();
        Natives.stopReaderThread();
    }

    @After
    public void tearDown() {
        Natives.stopReaderThread();
    }
}
