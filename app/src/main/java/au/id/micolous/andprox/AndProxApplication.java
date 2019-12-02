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

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

import au.id.micolous.andprox.natives.Natives;

/**
 * AndProx application reference.
 */
public class AndProxApplication extends Application {

    private static AndProxApplication sInstance;
    private String mExtraDeviceInfo = "";
    private static final String TAG = "AndProxApplication";
    private boolean mProxmarkDetected = false;
    private boolean mOldProxmarkDetected = false;

    public AndProxApplication() {
        sInstance = this;
    }

    public void setExtraDeviceInfo(String extraDeviceInfo) {
        mExtraDeviceInfo = extraDeviceInfo;
    }

    public void setProxmarkDetected(boolean state) {
        mProxmarkDetected = state;
    }

    public boolean isProxmarkDetected() {
        return mProxmarkDetected;
    }

    public void setOldProxmarkDetected(boolean state) {
        mOldProxmarkDetected = state;
    }

    public boolean isOldProxmarkDetected() {
        return mOldProxmarkDetected;
    }

    public static AndProxApplication getInstance() {
        return sInstance;
    }

    private static String formatMemoryInfo(ActivityManager.MemoryInfo mi) {
        if (mi == null) {
            return "null";
        }

        return String.format(Locale.ENGLISH, "%s (%s free)",
                Utils.formatBytes(mi.totalMem),
                Utils.formatBytes(mi.availMem));
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
            if (am != null) {
                mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error getting memory information", e);
            mi = null;
        }

        return String.format(Locale.ENGLISH,
                        "AndProx version: %s\n" +
                        "PM3 Client version: %s\n" +
                        "Build timestamp: %s\n" +
                        "Model: %s (%s)\n" +
                        "Product: %s\n" +
                        "Manufacturer: %s (%s)\n" +
                        "RAM: %s\n" +
                        "Android OS: %s (API %d)\n" +
                        "Android Build: %s\n\n" +
                        "USB Host: %s\n" +
                        "%s", // extra device info
                // Version:
                getVersionString(),
                Natives.getProxmarkClientVersion(),
                Natives.getProxmarkClientBuildTimestamp(),
                // Model:
                Build.MODEL,
                Build.DEVICE,
                Build.PRODUCT,
                // Manufacturer / brand:
                Build.MANUFACTURER,
                Build.BRAND,
                // RAM:
                formatMemoryInfo(mi),
                // OS:
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                // Build:
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

}
