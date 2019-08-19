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
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import au.id.micolous.andprox.natives.Natives;

/**
 * AndProx application reference.
 */
public class AndProxApplication extends Application {
    public static final String PREF_CONN_MODE = "pref_conn_mode";
    public static final String PREF_ANDROID_EMU_HOST = "pref_android_emu_host";
    public static final String PREF_TCP_HOST = "pref_tcp_host";
    public static final String PREF_TCP_PORT = "pref_tcp_port";
    public static final String PREF_ALLOW_SLEEP = "pref_allow_sleep";

    public static final String PREF_CONN_USB = "usb";
    public static final String PREF_CONN_TCP = "tcp";
    public static final String PREF_CONN_NONE = "none";

    private static final String DEFAULT_IP = "127.0.0.1";

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

    private static boolean hasSystemFeature(String feature) {
        AndProxApplication app = AndProxApplication.getInstance();
        return app.getPackageManager().hasSystemFeature(feature);
    }

    public static boolean hasUsbHostSupport() {
        return hasSystemFeature("android.hardware.usb.host");
    }

    private static SharedPreferences getPrefrences() {
        return PreferenceManager.getDefaultSharedPreferences(getInstance());
    }

    private static boolean getBooleanPref(final String preference, final boolean defaultValue) {
        final SharedPreferences prefs = getPrefrences();
        return prefs.getBoolean(preference, defaultValue);
    }

    private static boolean getBooleanPref(final String preference) {
        return getBooleanPref(preference, false);
    }

    @Nullable
    private static String getStringPref(final String preference) {
        return getStringPref(preference, null);
    }

    @Nullable
    private static String getStringPref(final String preference, @Nullable final String defaultValue) {
        final SharedPreferences prefs = getPrefrences();
        return prefs.getString(preference, defaultValue);
    }

    private static int getIntPref(final String preference, final int defaultValue) {
        final SharedPreferences prefs = getPrefrences();
        final String s = prefs.getString(preference, null);

        if (s != null) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {}
        }

        return defaultValue;
    }

    public static boolean useAndroidEmulatorHost() {
        return getBooleanPref(PREF_ANDROID_EMU_HOST, Utils.isRunningInEmulator());
    }

    public static boolean allowSleep() {
        return getBooleanPref(PREF_ALLOW_SLEEP);
    }

    @Nullable
    public static String getTcpHostStr() {
        if (useAndroidEmulatorHost()) {
            InetAddress a = Utils.getEmulatorHostIp();

            if (a != null) {
                return a.getHostAddress();
            }
        }

        final String addr = getStringPref(PREF_TCP_HOST, DEFAULT_IP);
        if (addr == null || addr.isEmpty()) {
            return null;
        }

        return addr;
    }

    @Nullable
    public static InetAddress getTcpHost() throws UnknownHostException {
        if (useAndroidEmulatorHost()) {
            InetAddress a = Utils.getEmulatorHostIp();

            if (a != null) {
                return a;
            }
        }

        final String addr = getStringPref(PREF_TCP_HOST, DEFAULT_IP);
        if (addr == null || addr.isEmpty()) {
            return null;
        }

        return InetAddress.getByName(addr);

    }

    public static int getTcpPort() {
        return getIntPref(PREF_TCP_PORT, 1234);
    }

    @NonNull
    public static String getConnectivityModeStr() {
        // defaultValue is NonNull, therefore will always return NonNull
        //noinspection ConstantConditions
        return getStringPref(PREF_CONN_MODE, hasUsbHostSupport() ? PREF_CONN_USB : PREF_CONN_TCP);
    }

    @NonNull
    public static ConnectivityMode getConnectivityMode() {
        return ConnectivityMode.fromModeString(getConnectivityModeStr());
    }

}
