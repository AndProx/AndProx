package au.id.micolous.andprox.behavior.version;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbId;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import au.id.micolous.andprox.device.ISharedPreferences;

public class ProxmarkDumpDevice {

    private static final String TAG = "ProxmarkDumpDevice";

    private ProxmarkDetection app;
    private ISharedPreferences preferences;

    @Inject
    public ProxmarkDumpDevice(ProxmarkDetection app, ISharedPreferences preferences) {
        this.app = app;
        this.preferences = preferences;
    }

    public void dumpUsbDeviceInfo(UsbManager manager) {
        // List all the devices
        StringBuilder deviceInfo = new StringBuilder();
        app.setProxmarkDetected(false);
        app.setOldProxmarkDetected(false);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        deviceInfo.append(String.format(Locale.ENGLISH, "Found %d USB device(s):\n", deviceList.size()));

        for (UsbDevice d : deviceList.values()) {
            deviceInfo.append(String.format(Locale.ENGLISH, "- %s (%04x:%04x)\n", d.getDeviceName(), d.getVendorId(), d.getProductId()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                deviceInfo.append(String.format(Locale.ENGLISH, "  Name: %s\n", d.getProductName()));
            }

            if (d.getSerialNumber() != null) {
                deviceInfo.append(String.format(Locale.ENGLISH, "  Serial: %s\n", d.getSerialNumber()));
            } else {
                deviceInfo.append("  Could not retrieve serial number!\n");
            }
        }

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        deviceInfo.append(String.format(Locale.ENGLISH, "\nFound %d suitable driver(s):\n", availableDrivers.size()));

        for (UsbSerialDriver d : availableDrivers) {
            UsbDevice dev = d.getDevice();

            deviceInfo.append(String.format(Locale.ENGLISH, "- %s (%04x:%04x)\n",
                    dev.getDeviceName(), dev.getVendorId(), dev.getProductId()));

            for (UsbSerialPort p : d.getPorts()) {
                deviceInfo.append(String.format(Locale.ENGLISH, "  Port %d: %s\n", p.getPortNumber(), p.getClass().getSimpleName()));

                if (preferences.allowAllProxmarkDevices()) {
                    deviceInfo.append("  Detected PM3!\n");
                    app.setProxmarkDetected(true);
                } else {
                    if (dev.getVendorId() == UsbId.VENDOR_PROXMARK3 && dev.getProductId() == UsbId.PROXMARK3) {
                        deviceInfo.append("  Detected PM3!\n");
                        app.setProxmarkDetected(true);
                    } else if (dev.getVendorId() == UsbId.VENDOR_PROXMARK3_OLD && dev.getProductId() == UsbId.PROXMARK3_OLD) {
                        deviceInfo.append("  Old PM3 firmware -- needs update!\n");
                        app.setOldProxmarkDetected(true);
                    }
                }
            }
        }

        Log.d(TAG, deviceInfo.toString());
        app.setExtraDeviceInfo(deviceInfo.toString());
    }

}
