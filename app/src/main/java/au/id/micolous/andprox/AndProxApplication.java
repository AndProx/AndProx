/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016 Michael Farrell <micolous+git@gmail.com>
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
 * Under section 7 of the GNU General Public License v3, the following "further
 * restrictions" apply to this program:
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

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.util.Locale;

import au.id.micolous.andprox.natives.Natives;

/**
 * AndProx application reference.
 */

public class AndProxApplication extends Application {
    private static AndProxApplication sInstance;
    private String mExtraDeviceInfo = "";
    private static final String TAG = "AndProxApplication";

    public AndProxApplication() {
        sInstance = this;
    }

    public void setExtraDeviceInfo(String extraDeviceInfo) {
        mExtraDeviceInfo = extraDeviceInfo;
    }

    public static AndProxApplication getInstance() {
        return sInstance;
    }

    private static String formatBytes(long bytes) {
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

    private static String formatMemoryInfo(ActivityManager.MemoryInfo mi) {
        if (mi == null) {
            return "null";
        }

        return String.format(Locale.ENGLISH, "%s (%s free)",
                formatBytes(mi.totalMem),
                formatBytes(mi.availMem));
    }

    /**
     * Dumps all device information that is useful for debugging AndProx.
     *
     * This always returns strings in English, and is only ever used for the SysInfoActivity. This
     * is so that bug reports are readable by the developers. ;)
     */
    public static String getDeviceInfo(Context ctx) {
        ActivityManager.MemoryInfo mi = null;

        try {
            ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
        } catch (Exception e) {
            Log.w(TAG, "Error getting memory information", e);
            mi = null;
        }

        return String.format(Locale.ENGLISH,
                        "AndProx version: %s\n" +
                        "PM3 Client version: %s\n" +
                        "Build timestamp: %s\n" +
                        "Model: %s (%s)\n" +
                        "Manufacturer: %s (%s)\n" +
                        "RAM: %s\n"+
                        "Android OS: %s (%s)\n\n" +
                        "USB Host: %s\n" +
                        "%s", // extra device info
                // Version:
                getVersionString(),
                Natives.getProxmarkClientVersion(),
                Natives.getProxmarkClientBuildTimestamp(),
                // Model:
                Build.MODEL,
                Build.DEVICE,
                // Manufacturer / brand:
                Build.MANUFACTURER,
                Build.BRAND,
                // RAM:
                formatMemoryInfo(mi),
                // OS:
                Build.VERSION.RELEASE,
                Build.ID,
                // USB:
                hasUsbHostSupport() ? "yes" : "no",
                getInstance().mExtraDeviceInfo);
    }

    public static String getVersionString() {
        PackageInfo info = getPackageInfo();
        return String.format("%s (%s)", info.versionName, info.versionCode);
    }

    private static PackageInfo getPackageInfo() {
        try {
            AndProxApplication app = AndProxApplication.getInstance();
            return app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean hasSystemFeature(String feature) {
        AndProxApplication app = AndProxApplication.getInstance();
        return app.getPackageManager().hasSystemFeature(feature);
    }

    public static boolean hasUsbHostSupport() {
        return hasSystemFeature("android.hardware.usb.host");
    }
}
