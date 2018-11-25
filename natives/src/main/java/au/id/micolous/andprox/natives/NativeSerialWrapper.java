/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2017 Michael Farrell <micolous+git@gmail.com>
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
import android.support.annotation.Nullable;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * API wrapper between uart_android.c and usb-serial-for-android.
 */
public class NativeSerialWrapper {
    private static final String TAG = "NativeSerialWrapper";

    @Nullable
    private final UsbSerialPort mPort;

    @Nullable
    private final InputStream mInputStream;
    @Nullable
    private final OutputStream mOutputStream;

    @NonNull
    private final Closeable mCloseable;

    // uart.h defines the timeout as 30ms.
    public static final int TIMEOUT = 30;

    public NativeSerialWrapper(@NonNull UsbSerialPort port) {
        mPort = port;
        mInputStream = null;
        mOutputStream = null;
        mCloseable = port;
    }

    public NativeSerialWrapper(@NonNull InputStream inputStream,
                               @NonNull OutputStream outputStream,
                               @NonNull Closeable closeable) {
        mPort = null;
        mInputStream = inputStream;
        mOutputStream = outputStream;
        mCloseable = closeable;
    }

    public boolean send(byte[] pbtTx) {
        //Log.d(TAG, String.format("sending %d bytes", pbtTx.length));
        try {
            if (mPort != null) {
                mPort.write(pbtTx, TIMEOUT);
                return true;
            } else if (mOutputStream != null) {
                mOutputStream.write(pbtTx);
                return true;
            }
        } catch (IOException ex) {
            Log.e(TAG, "IOException in send", ex);
        }

        return false;
    }

    public int receive(byte[] pbtRx) {
        //Log.d(TAG, String.format("receiving %d bytes", pbtRx.length));
        try {
            if (mPort != null) {
                return mPort.read(pbtRx, TIMEOUT);
            } else if (mInputStream != null) {
                return mInputStream.read(pbtRx);
            }
        } catch (IOException ex) {
            Log.e(TAG, "IOException in receive", ex);
        }

        return -1;
    }

    public void close() {
        try {
            mCloseable.close();
        } catch (IOException ex) {
            Log.e(TAG, "IOException in close", ex);
        }
    }
}
