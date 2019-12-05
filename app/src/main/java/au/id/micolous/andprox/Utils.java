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
package au.id.micolous.andprox;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Helper utilities in AndProx
 */

public final class Utils {
    private static final String[] EMULATOR_MODELS = {
            "google_sdk",
            "sdk",
            "sdk_gphone_x86",
            "sdk_google_phone_x86",
            "sdk_google_atv_x86",
            "sdk_phone_armv7",
            "sdk_phone_x86",
            "sdk_x86",
    };

    private static final String TAG = Utils.class.getSimpleName();

    @Nullable
    private static final InetAddress EMULATOR_HOST_IP;

    static {
        InetAddress a = null;

        if (isRunningInEmulator()) {
            try {
                a = InetAddress.getByAddress(new byte[]{10, 0, 2, 2});
            } catch (UnknownHostException e) {
                // Shouldn't happen, may indicate a lack of IPv4 support?
                Log.w(TAG, "unknown host for emulator IP -- should not happen!", e);
            }
        }

        EMULATOR_HOST_IP = a;
    }


    /**
     * Given a string resource (R.string), localize the string according to the language preferences
     * on the device.
     *
     * @param stringResource R.string to localize.
     * @param formatArgs     Formatting arguments to pass
     * @return Localized string
     */
    public static String localizeString(Context context, @StringRes int stringResource, Object... formatArgs) {
        Resources res = context.getResources();
        return res.getString(stringResource, formatArgs);
    }

    /**
     * Given a plural resource (R.plurals), localize the string according to the language preferences
     * on the device.
     *
     * @param pluralResource R.plurals to localize.
     * @param quantity       Quantity to use for pluaralisation rules
     * @param formatArgs     Formatting arguments to pass
     * @return Localized string
     */
    public static String localizePlural(Context context, @PluralsRes int pluralResource, int quantity, Object... formatArgs) {
        Resources res = context.getResources();
        return res.getQuantityString(pluralResource, quantity, formatArgs);
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return String.format(Locale.ENGLISH, "%d bytes", bytes);
        }

        double fbytes = bytes / 1024.0;
        if (fbytes < 1024) {
            return String.format(Locale.ENGLISH, "%.1f KiB", fbytes);
        }

        fbytes /= 1024.0;
        if (fbytes < 1024) {
            return String.format(Locale.ENGLISH, "%.1f MiB", fbytes);
        }

        fbytes /= 1024.0;
        if (fbytes < 1024) {
            return String.format(Locale.ENGLISH, "%.1f GiB", fbytes);
        }

        fbytes /= 1024.0;
        return String.format(Locale.ENGLISH, "%.1f TiB", fbytes);
    }

    public static boolean isRunningInEmulator() {
        for (String s : EMULATOR_MODELS) {
            if (s.equals(Build.PRODUCT)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public static InetAddress getEmulatorHostIp() {
        return EMULATOR_HOST_IP;
    }

    public static void setPreferenceListeners(@NonNull Preference p, Preference.OnPreferenceChangeListener listener) {
        if (p instanceof PreferenceGroup) {
            final PreferenceGroup group = (PreferenceGroup)p;
            for (int i=0; i<group.getPreferenceCount(); i++) {
                setPreferenceListeners(group.getPreference(i), listener);
            }
        } else {
            p.setOnPreferenceChangeListener(listener);
        }

    }
}
