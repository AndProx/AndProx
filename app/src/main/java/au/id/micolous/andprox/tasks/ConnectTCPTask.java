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
package au.id.micolous.andprox.tasks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import au.id.micolous.andprox.natives.NativeSerialWrapper;

public class ConnectTCPTask extends ConnectTask {
    private static final String TAG = "ConnectTCPTask";

    private final InetAddress mAddress;
    private final int mPort;

    public ConnectTCPTask(Context context, @NonNull InetAddress addr, int port) {
        super(context);

        mAddress = addr;
        mPort = port;
    }

    @Nullable
    @Override
    protected NativeSerialWrapper connectToDevice(boolean firstTry) {
        Socket s;
        try {
            s = new Socket(mAddress, mPort);
        } catch (IOException e) {
            setResult(new ConnectTaskResult().setCommunicationError());
            Log.d(TAG, "Error opening socket", e);
            return null;
        }

        try {
            s.setTcpNoDelay(true);
            s.setSoTimeout(NativeSerialWrapper.TIMEOUT * 2);
            s.setKeepAlive(true);

            final NativeSerialWrapper nsw = new NativeSerialWrapper(s.getInputStream(),
                    s.getOutputStream(), s);

            setResult(new ConnectTaskResult().setSuccess());
            return nsw;
        } catch (IOException e) {
            Log.d(TAG, "Error connecting", e);
            setResult(new ConnectTaskResult().setCommunicationError());
            return null;
        } finally {
            try {
                s.close();
            } catch (IOException e) {
                Log.d(TAG, "Error closing socket", e);
            }
        }
    }

}
