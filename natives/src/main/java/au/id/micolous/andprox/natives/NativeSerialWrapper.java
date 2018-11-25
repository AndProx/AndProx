/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
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
 * API wrapper between uart_android.c and Java code.
 */
public final class NativeSerialWrapper implements Closeable {
    private static final String TAG = "NativeSerialWrapper";
    private static final boolean DEBUG_COMMS = false;

    private boolean mClosed = false;
    private boolean m20PingSent = false;

    private final SerialInterface mSerialInterface;

    private long mLastMessageRecieved;

    // uart.h defines the timeout as 30ms.
    public static final int TIMEOUT = 30;

    public NativeSerialWrapper(@NonNull SerialInterface iface) {
        mSerialInterface = iface;
        mLastMessageRecieved = System.currentTimeMillis();
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
            int len = mSerialInterface.receive(pbtRx);

            final long now = System.currentTimeMillis();

            if (len > 0 || mLastMessageRecieved > now) {
                mLastMessageRecieved = now;
                m20PingSent = false;
            } else {
                final long delta = now - mLastMessageRecieved;
                if (delta > 30000) {
                    // No message in 30 sec, abort
                    Log.d(TAG, "No activity in 30sec, shutting down");
                    close();
                    return -1;
                } else if (delta > 20000) {
                    if (!m20PingSent) {
                        m20PingSent = true;
                        Log.d(TAG, "No activity in 20sec, sending ping");
                        // No message in 20 sec, send a ping in the background
                        new Thread(Natives::sendCmdPing).start();
                    }
                }
            }
            return len;
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
