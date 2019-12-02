package au.id.micolous.andprox.behavior.format;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

import javax.inject.Inject;

import au.id.micolous.andprox.Utils;
import au.id.micolous.andprox.behavior.version.ProxmarkDetection;
import au.id.micolous.andprox.device.ISharedPreferences;
import au.id.micolous.andprox.natives.Natives;

public class FormatDeviceImpl implements IFormatDevice {

    private static final String TAG = "AndProxApplication";

    private Context context;
    private ISharedPreferences preferences;
    private ProxmarkDetection proxmarkDetection;

    @Inject
    public FormatDeviceImpl(ProxmarkDetection proxmarkDetection, Context context, ISharedPreferences preferences) {
        this.context = context;
        this.preferences = preferences;
        this.proxmarkDetection = proxmarkDetection;
    }

    @Override
    public String getDeviceInfo() {
        ActivityManager.MemoryInfo mi = null;

        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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
                preferences.hasUsbHostSupport() ? "yes" : "no",
                proxmarkDetection.getExtraDeviceInfo());
    }

    @Override
    public String getVersionString() {
        PackageInfo info = getPackageInfo();
        return String.format("%s (%s)", info.versionName, info.versionCode);
    }

    @Override
    public PackageInfo getPackageInfo() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatMemoryInfo(ActivityManager.MemoryInfo mi) {
        if (mi == null) {
            return "null";
        }

        return String.format(Locale.ENGLISH, "%s (%s free)",
                Utils.formatBytes(mi.totalMem),
                Utils.formatBytes(mi.availMem));
    }
}
