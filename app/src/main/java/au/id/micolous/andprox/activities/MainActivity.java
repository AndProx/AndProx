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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import au.id.micolous.andprox.R;
import au.id.micolous.andprox.Utils;
import au.id.micolous.andprox.device.ConnectivityMode;
import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.tasks.ConnectTCPTask;
import au.id.micolous.andprox.tasks.CopyTask;


public class MainActivity extends InjectableActivity {


    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "au.id.micolous.andprox.USB_PERMISSION";
    public static final String ACTION_USB_PERMISSION_AUTOCONNECT = "au.id.micolous.andprox.USB_PERMISSION_AUTOCONNECT";
    private static final int STORAGE_PERMISSION_CALLBACK = 1001;
    private static final int SETTINGS_CALLBACK = 1002;

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
                dumpDevice.dumpUsbDeviceInfo(manager);
                MainActivity.this.runOnUiThread(MainActivity.this::updateIntroText);
            }
        }
    };

    private void updateIntroText() {
        TextView tvIntroText = findViewById(R.id.tvIntroText);
        final Button btnConnect = findViewById(R.id.btnConnect);
        ConnectivityMode mode = preferences.getConnectivityMode();
        btnConnect.setText(mode.getConnectButtonText());

        switch (mode) {
            case USB:
                if (!preferences.hasUsbHostSupport()) {
                    tvIntroText.setText(R.string.no_usb_host);
                    btnConnect.setEnabled(false);
                    return;
                }

                btnConnect.setEnabled(true);

                if (detection.isProxmarkDetected()) {
                    tvIntroText.setText(R.string.intro_text_usb_detected);
                } else if (detection.isOldProxmarkDetected()) {
                    tvIntroText.setText(R.string.intro_text_old_pm3);
                } else {
                    tvIntroText.setText(R.string.intro_text_default);
                }
                break;

            case TCP:
                tvIntroText.setText(Utils.localizeString(this, R.string.intro_text_tcp, preferences.getTcpHostStr(), preferences.getTcpPort()));
                btnConnect.setEnabled(true);
                break;

            case NONE:
                tvIntroText.setText(R.string.intro_text_none);
                btnConnect.setEnabled(true);
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        UsbManager manager = null;

        if (preferences.hasUsbHostSupport()) {
            manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        }

        if (manager != null) {
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(mUsbPermissionReceiver, filter);

            // Listen to USB connect / disconnect
            registerReceiver(mUsbDeviceChangeReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
            registerReceiver(mUsbDeviceChangeReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

            dumpDevice.dumpUsbDeviceInfo(manager);
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
            Log.w(TAG, "no USB host support!");
            updateIntroText();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SETTINGS_CALLBACK:
                updateIntroText();
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
        InetAddress addr = null;

        switch (preferences.getConnectivityMode()) {
            case USB:
                if (detection.isOldProxmarkDetected()) {
                    firmwareManager.unsupportedFirmwareError();
                    return;
                }

                if (preferences.hasUsbHostSupport()) {
                    // If passed with a view, then we are called from the button.
                    usbTaskSupplier.get().execute(view != null);
                }
                break;

            case TCP:
                try {
                    addr = preferences.getTcpHost();
                } catch (UnknownHostException e) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.tcp_error)
                            .setMessage(Utils.localizeString(this, R.string.tcp_error_host_not_found,
                                    preferences.getTcpHostStr(), e.getLocalizedMessage()))
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .show();
                    return;
                }

                if (addr == null) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.tcp_error)
                            .setMessage(Utils.localizeString(this, R.string.tcp_error_host_not_found,
                                    preferences.getTcpHostStr(), "null"))
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .show();
                    return;
                }

                new ConnectTCPTask(this, parser, firmwareManager,
                        addr, preferences.getTcpPort()).execute(true);
                break;

            case NONE:
                Natives.initProxmark();
                Intent intent = new Intent(MainActivity.this, CliActivity.class);
                startActivity(intent);
                finish();
                break;
        }

    }

    public void btnSettings(View view) {
        final Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_CALLBACK);
    }
}
