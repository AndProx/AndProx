/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2019 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.andprox.natives;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

/**
 * API wrapper between uart_android.c and Java code. This class consumes a {@link SerialInterface},
 * which is the transport layer on the Java side.
 *
 * It adds some extra convenience functionality:
 *
 * - Tracing of transport I/O (with {@link #DEBUG_COMMS}
 * - Converts Java exceptions into shutdown events
 * - Automatic keep-alive on inactivity
 * - Automatic shutdown on device loss
 */
public final class NativeSerialWrapper implements Closeable {
    private static final String TAG = "NativeSerialWrapper";
    private static final boolean DEBUG_COMMS = false;

    private boolean mClosed = false;

    private final SerialInterface mSerialInterface;

    // uart.h defines the timeout as 30ms.
    public static final int TIMEOUT = 30;

    public NativeSerialWrapper(@NonNull SerialInterface iface) {
        mSerialInterface = iface;
    }


    public boolean send(byte[] pbtTx) {
        if (DEBUG_COMMS) {
            Log.d(TAG, String.format("sending %d bytes", pbtTx.length));
        }

        try {
            int l = mSerialInterface.send(pbtTx);

            if (l == pbtTx.length) {
                // All data sent
                return true;
            }

            if (l > 0) {
                // Partial data failure
                Log.w(TAG, "partial write in send");
                return false;
            }

            Log.e(TAG, "no data sent");
        } catch (IOException ex) {
            Log.e(TAG, "IOException in send", ex);
        }

        close();
        return false;
    }

    public int receive(byte[] pbtRx) {
        if (DEBUG_COMMS) {
            Log.d(TAG, String.format("receiving %d bytes", pbtRx.length));
        }

        try {
            return mSerialInterface.receive(pbtRx);
        } catch (IOException ex) {
            Log.e(TAG, "IOException in receive", ex);
            close();
        }

        return -1;
    }

    public void close() {
        if (mClosed) {
            return;
        }
        mClosed = true;

        mSerialInterface.close();
        Natives.stopReaderThread();
        Natives.handleDisconnect(mSerialInterface);
    }
}
