package au.id.micolous.andprox.behavior.format;

import android.content.pm.PackageInfo;

public interface IFormatDevice {

    /**
     * Dumps all device information that is useful for debugging AndProx.
     *
     * This always returns strings in English, and is only ever used for the SysInfoActivity. This
     * is so that bug reports are readable by the developers. ;)
     */
    String getDeviceInfo();

    String getVersionString();

    PackageInfo getPackageInfo();
}
