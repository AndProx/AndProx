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
package au.id.micolous.andprox;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public enum ConnectivityMode {
    USB(AndProxApplication.PREF_CONN_USB, R.string.connect_button_usb),
    TCP(AndProxApplication.PREF_CONN_TCP, R.string.connect_button_tcp),
    NONE(AndProxApplication.PREF_CONN_NONE, R.string.connect_button_none);

    @NonNull
    private final String mMode;
    @StringRes
    private final int mConnectButtonText;

    ConnectivityMode(@NonNull String mode, @StringRes int connectButtonText) {
        mMode = mode;
        mConnectButtonText = connectButtonText;
    }

    static ConnectivityMode fromModeString(@NonNull String mode) {
        for (ConnectivityMode m : ConnectivityMode.values()) {
            if (m.mMode.equals(mode)) {
                return m;
            }
        }

        return NONE;
    }

    @StringRes
    public int getConnectButtonText() {
        return mConnectButtonText;
    }

}
