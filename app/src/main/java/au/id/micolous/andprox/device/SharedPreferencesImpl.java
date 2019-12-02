package au.id.micolous.andprox.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

import au.id.micolous.andprox.Utils;

public class SharedPreferencesImpl implements ISharedPreferences {

    private Context context;
    private SharedPreferences prefs;

    public SharedPreferencesImpl(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public boolean allowAllProxmarkDevices() {
        return getBooleanPref(PREF_ALLOWED_DEVICES, false);
    }

    @Override
    public boolean useAndroidEmulatorHost() {
        return getBooleanPref(PREF_ANDROID_EMU_HOST, Utils.isRunningInEmulator());
    }

    @Override
    public boolean allowSleep() {
        return getBooleanPref(PREF_ALLOW_SLEEP);
    }

    @Nullable
    @Override
    public String getTcpHostStr() {
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
    @Override
    public InetAddress getTcpHost() throws UnknownHostException {
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

    @Override
    public int getTcpPort() {
        return getIntPref(PREF_TCP_PORT, 1234);
    }

    @NonNull
    @Override
    public String getConnectivityModeStr() {
        // defaultValue is NonNull, therefore will always return NonNull
        //noinspection ConstantConditions
        return getStringPref(PREF_CONN_MODE, hasUsbHostSupport() ? PREF_CONN_USB : PREF_CONN_TCP);
    }

    @NonNull
    @Override
    public ConnectivityMode getConnectivityMode() {
        return ConnectivityMode.fromModeString(getConnectivityModeStr());
    }

    @Override
    public boolean hasUsbHostSupport() {
        return hasSystemFeature("android.hardware.usb.host");
    }

    private boolean hasSystemFeature(String feature) {
        return context.getPackageManager().hasSystemFeature(feature);
    }

    private boolean getBooleanPref(final String preference, final boolean defaultValue) {
        return prefs.getBoolean(preference, defaultValue);
    }

    private boolean getBooleanPref(final String preference) {
        return getBooleanPref(preference, false);
    }

    @Nullable
    private String getStringPref(final String preference) {
        return getStringPref(preference, null);
    }

    @Nullable
    private String getStringPref(final String preference, @Nullable final String defaultValue) {
        return prefs.getString(preference, defaultValue);
    }

    private int getIntPref(final String preference, final int defaultValue) {
        final String s = prefs.getString(preference, null);

        if (s != null) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {}
        }

        return defaultValue;
    }
}
