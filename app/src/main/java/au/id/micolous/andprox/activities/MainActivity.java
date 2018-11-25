/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
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

package au.id.micolous.andprox.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbId;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.R;
import au.id.micolous.andprox.Utils;
import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.tasks.ConnectTCPTask;
import au.id.micolous.andprox.tasks.ConnectUSBTask;
import au.id.micolous.andprox.tasks.CopyTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "au.id.micolous.andprox.USB_PERMISSION";
    public static final String ACTION_USB_PERMISSION_AUTOCONNECT = "au.id.micolous.andprox.USB_PERMISSION_AUTOCONNECT";
    private static final int STORAGE_PERMISSION_CALLBACK = 1001;

    private static final String DISMISS_USB_HOST_SUPPORT = "dismiss_usb_host_support";
    private boolean mDismissUSBHostSupport = false;

    private final BroadcastReceiver mUsbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action) || ACTION_USB_PERMISSION_AUTOCONNECT.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.d(TAG, "permission denied for mDevice " + device);
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(R.string.permission_denied)
                                .setTitle(R.string.permission_denied_title)
                                .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                                .setCancelable(false);
                        builder.show();
                    } else if (ACTION_USB_PERMISSION_AUTOCONNECT.equals(action)) {
                        // permission was granted, and now we need to hit up the connect button again
                        btnConnect(null);
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mUsbDeviceChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) || UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                dumpUsbDeviceInfo(manager);
                MainActivity.this.runOnUiThread(MainActivity.this::updateIntroText);
            }
        }
    };

    public static void dumpUsbDeviceInfo(UsbManager manager) {
        // List all the devices
        StringBuilder deviceInfo = new StringBuilder();
        AndProxApplication app = AndProxApplication.getInstance();
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

                if (dev.getVendorId() == UsbId.VENDOR_PROXMARK3 && dev.getProductId() == UsbId.PROXMARK3) {
                    deviceInfo.append("  Detected PM3!\n");
                    app.setProxmarkDetected(true);
                } else if (dev.getVendorId() == UsbId.VENDOR_PROXMARK3_OLD && dev.getProductId() == UsbId.PROXMARK3_OLD) {
                    deviceInfo.append("  Old PM3 firmware -- needs update!\n");
                    app.setOldProxmarkDetected(true);
                }
            }
        }

        Log.d(TAG, deviceInfo.toString());
        app.setExtraDeviceInfo(deviceInfo.toString());

    }

    private void updateIntroText() {
        TextView tvIntroText = findViewById(R.id.tvIntroText);
        if (!AndProxApplication.hasUsbHostSupport()) {
            tvIntroText.setText(R.string.no_usb_host);
            findViewById(R.id.btnConnect).setEnabled(false);
            return;
        }

        findViewById(R.id.btnConnect).setEnabled(true);
        AndProxApplication app = AndProxApplication.getInstance();

        if (app.isProxmarkDetected()) {
            tvIntroText.setText(R.string.intro_text_usb_detected);
        } else if (app.isOldProxmarkDetected()) {
            tvIntroText.setText(R.string.intro_text_old_pm3);
        } else {
            tvIntroText.setText(R.string.intro_text_default);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(DISMISS_USB_HOST_SUPPORT, mDismissUSBHostSupport);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mDismissUSBHostSupport = savedInstanceState.getBoolean(DISMISS_USB_HOST_SUPPORT);
        }

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        UsbManager manager = null;

        if (AndProxApplication.hasUsbHostSupport()) {
            manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        }

        if (manager != null) {
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(mUsbPermissionReceiver, filter);

            // Listen to USB connect / disconnect
            registerReceiver(mUsbDeviceChangeReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
            registerReceiver(mUsbDeviceChangeReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

            dumpUsbDeviceInfo(manager);
            updateIntroText();

            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
            if (availableDrivers.isEmpty()) {
                Log.d(TAG, "onCreate: no devices present");
                return;
            }

            // Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDevice device = driver.getDevice();
            manager.requestPermission(device, mPermissionIntent);
        } else {
            Log.e(TAG, "no USB host support!");
            updateIntroText();

            if (!mDismissUSBHostSupport) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.no_usb_host)
                        .setTitle(R.string.no_usb_host_title)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            mDismissUSBHostSupport = true;
                            dialog.dismiss();
                        })
                        .setCancelable(false);
                builder.show();
            }
        }

        // Request permission for storage right away, as requesting this when a file is explicitly
        // opened is tedious.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CALLBACK);
        } else {
            // We already have permissions
            CopyTask t = new CopyTask(this);
            t.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_CALLBACK:
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    CopyTask t = new CopyTask(this);
                    t.execute();
                } else {
                    // Permission denied.
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.no_storage)
                            .setTitle(R.string.no_storage_title)
                            .setPositiveButton(R.string.retry,
                                    (dialog, which) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CALLBACK))
                            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                            .setCancelable(false);
                    builder.show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mUsbPermissionReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "couldn't unregister USB permission receiver", e);
        }

        try {
            unregisterReceiver(mUsbDeviceChangeReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "couldn't unregister USB device change receiver", e);
        }
    }

    /**
     * Show the System Information activity.
     */
    public void btnSysInfoMain(View view) {
        Intent i = new Intent(MainActivity.this, SysInfoActivity.class);
        startActivity(i);
    }

    /**
     * Show the about activity.
     */
    public void btnAbout(View view) {
        Intent i = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(i);
    }

    /**
     * Attempt to connect to the proxmark
     */
    public void btnConnect(@Nullable View view) {
        AndProxApplication app = AndProxApplication.getInstance();

        if (app.isOldProxmarkDetected()) {
            unsupportedFirmwareError(MainActivity.this);
            return;
        }

        if (AndProxApplication.hasUsbHostSupport()) {
            // If passed with a view, then we are called from the button.
            new ConnectUSBTask(this).execute(view != null);
        }
    }


    public void btnConnectTcp(View view) {
        InetAddress addr;

        try {
            // FIXME: don't hard code this.
            addr = InetAddress.getByName("10.0.2.2"); /* android emulator host */
        } catch (UnknownHostException e) {
            Log.d(TAG, "couldn't resolve name");
            return;
        }

        new ConnectTCPTask(this, addr,1234).execute(true);
    }

    /**
     * Run PM3 client in offline mode.
     */
    public void btnOfflineMode(View view) {
        Natives.initProxmark();

        Intent intent = new Intent(MainActivity.this, CliActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Show an error that the firmware version is unsupported.
     * @param context Context of where we were called from.
     */
    public static void unsupportedFirmwareError(@NonNull Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(Utils.localizeString(R.string.reflash_required_message, Natives.getProxmarkClientVersion()))
                .setTitle(R.string.reflash_required_title)
                .setPositiveButton(R.string.instructions, (dialog, which) -> {
                    context.startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Proxmark/proxmark3/wiki/flashing")));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false);
        builder.show();
    }
}
