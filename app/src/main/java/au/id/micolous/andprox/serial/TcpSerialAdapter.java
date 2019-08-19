/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2018-2019 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.andprox.serial;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import au.id.micolous.andprox.natives.NativeSerialWrapper;
import au.id.micolous.andprox.natives.SerialInterface;

/**
 * TcpSerialAdapter acts as an interface to wrap a {@link Socket} into a
 * {@link SerialInterface}.
 */
public class TcpSerialAdapter implements SerialInterface {
    @Nullable
    private Socket mSocket;

    public TcpSerialAdapter(@NonNull Socket s) throws SocketException {
        mSocket = s;

        s.setTcpNoDelay(true);
        s.setSoTimeout(NativeSerialWrapper.TIMEOUT * 10);
        s.setReuseAddress(true);
    }

    @Override
    public int send(@NonNull byte[] pbtTx) throws IOException {
        if (mSocket == null) {
            return 0;
        }

        mSocket.getOutputStream().write(pbtTx);
        return pbtTx.length;
    }

    @Override
    public int receive(@NonNull byte[] pbtRx) throws IOException {
        if (mSocket == null) {
            return -1;
        }

        if (mSocket.isClosed() || !mSocket.isConnected()) {
            throw new IOException("Socket is closed");
        }

        try {
            return mSocket.getInputStream().read(pbtRx);
        } catch (SocketTimeoutException ignored) {
            return 0;
        }
    }

    @Override
    public void close() {
        if (mSocket == null) {
            return;
        }

        try {
            mSocket.close();
        } catch (IOException e) {
            // ignored
        }
    }
}
