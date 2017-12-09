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

package au.id.micolous.andprox.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.R;
import au.id.micolous.andprox.natives.NativeSerialWrapper;
import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.natives.Resources;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ACTION_USB_PERMISSION = "au.id.micolous.andprox.USB_PERMISSION";
    private static final String ACTION_USB_PERMISSION_AUTOCONNECT = "au.id.micolous.andprox.USB_PERMISSION_AUTOCONNECT";
    private static final int STORAGE_PERMISSION_CALLBACK = 1001;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
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
                                .setTitle(R.string.permission_denied_title);
                        builder.show();
                    } else if (ACTION_USB_PERMISSION_AUTOCONNECT.equals(action)) {
                        // permission was granted, and now we need to hit up the connect button again
                        btnConnect(null);
                    }
                }
            }
        }
    };

    private void dumpUsbDeviceInfo(UsbManager manager) {
        // List all the devices
        StringBuilder deviceInfo = new StringBuilder();

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        deviceInfo.append(String.format(Locale.ENGLISH, "Found %d USB device(s):\n", deviceList.size()));

        for (UsbDevice d : deviceList.values()) {
            deviceInfo.append(String.format(Locale.ENGLISH, "- %s (%04x : %04x)\n", d.getDeviceName(), d.getVendorId(), d.getProductId()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                deviceInfo.append(String.format(Locale.ENGLISH, "  Name: %s\n", d.getProductName()));
            }
        }

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        deviceInfo.append(String.format(Locale.ENGLISH, "\nFound %d suitable driver(s):\n", availableDrivers.size()));

        for (UsbSerialDriver d : availableDrivers) {
            deviceInfo.append(String.format(Locale.ENGLISH, "- %s (%04x : %04x)\n",
                    d.getDevice().getDeviceName(), d.getDevice().getVendorId(), d.getDevice().getProductId()));

            for (UsbSerialPort p : d.getPorts()) {
                deviceInfo.append(String.format(Locale.ENGLISH, "  Port %d: %s", p.getPortNumber(), p.getClass().getSimpleName()));
            }
        }

        Log.d(TAG, deviceInfo.toString());
        AndProxApplication.getInstance().setExtraDeviceInfo(deviceInfo.toString());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (AndProxApplication.hasUsbHostSupport()) {
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(mUsbReceiver, filter);

            dumpUsbDeviceInfo(manager);
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
            findViewById(R.id.btnConnect).setEnabled(false);
            ((TextView) findViewById(R.id.tvIntroText)).setText(R.string.no_usb_host);

            if (savedInstanceState == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.no_usb_host)
                        .setTitle(R.string.no_usb_host_title);
                builder.show();
            }
        }

        // Request permission for storage right away, as requesting this when a file is explicitly
        // opened is tedious.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CALLBACK);
        } else {
            // We already have permissions
            CopyTask t = new CopyTask();
            t.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_CALLBACK:
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    CopyTask t = new CopyTask();
                    t.execute();
                } else {
                    // Permission denied.
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.no_storage)
                            .setTitle(R.string.no_storage_title);
                    builder.show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mUsbReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "couldn't unregister USB receiver", e);
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
    public void btnConnect(View view) {
        if (AndProxApplication.hasUsbHostSupport()) {
            // If passed with a view, then we are called from the button.
            new ConnectTask().execute(view != null);
        }
    }

    /**
     * Run PM3 client in offline mode.
     */
    public void btnOfflineMode(View view) {
        Natives.initProxmark();
        Natives.unsetSerialPort();

        Intent intent = new Intent(MainActivity.this, CliActivity.class);
        startActivity(intent);
        finish();
    }

    private class ConnectTaskResult {
        boolean noDevicesPresent = false;
        boolean needPermissions = false;
        boolean alreadyAskedPermissions = false;
        boolean communicationError = false;
        boolean timeoutError = false;
        boolean success = false;

        ConnectTaskResult setNoDevicesPresent() {
            this.noDevicesPresent = true;
            return this;
        }

        ConnectTaskResult setNeedPermissions() {
            this.needPermissions = true;
            return this;
        }

        ConnectTaskResult setAlreadyAskedPermissions() {
            this.needPermissions = true;
            this.alreadyAskedPermissions = true;
            return this;
        }

        ConnectTaskResult setCommunicationError() {
            this.communicationError = true;
            return this;
        }

        ConnectTaskResult setTimeoutError() {
            this.timeoutError = true;
            return this;
        }

        ConnectTaskResult setSuccess() {
            this.success = true;
            return this;
        }
    }

    private class ConnectTask extends AsyncTask<Boolean, Void, ConnectTaskResult> {
        private ProgressDialog mProgressDialog;
        private UsbManager mUsbManager;
        private UsbDevice mDevice = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(MainActivity.this, "Connecting to Proxmark3", "This normally takes a moment", true, false);
            mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        }

        @Override
        protected ConnectTaskResult doInBackground(Boolean... booleans) {
            // List all the devices
            dumpUsbDeviceInfo(mUsbManager);

            // Try to connect to proxmark
            // Find all available drivers from attached devices.
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
            Log.d(TAG, String.format("Found %d available driver(s)", availableDrivers.size()));
            if (availableDrivers.isEmpty()) {
                return new ConnectTaskResult().setNoDevicesPresent();
            }

            // Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);
            mDevice = driver.getDevice();
            Log.d(TAG, String.format("Connecting to %s (%04x:%04x)...", mDevice.getDeviceName(), mDevice.getVendorId(), mDevice.getProductId()));
            UsbDeviceConnection connection = null;

            try {
                connection = mUsbManager.openDevice(mDevice);
            } catch (SecurityException e) {
                Log.e(TAG, "Error opening USB mDevice, no permission!", e);
            }
            if (connection == null) {
                Log.e(TAG, "error opening usb mDevice");
                if (booleans[0]) {
                    // We were called from a button press, so we can ask again.
                    Log.d(TAG, "asking for permission again");
                    return new ConnectTaskResult().setNeedPermissions();
                } else {
                    // Permissions still not working, but we have a null view (called from mUsbReceiver)
                    Log.e(TAG, "permissions still not working");

                    return new ConnectTaskResult().setAlreadyAskedPermissions();
                }
            }

            // Read some data! Most have just one port (port 0).
            UsbSerialPort port = driver.getPorts().get(0);
            boolean success = false;
            try {
                port.open(connection);
                port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                NativeSerialWrapper nsw = new NativeSerialWrapper(port);
                Natives.initProxmark();
                Natives.setSerialPort(nsw);
                Natives.startReaderThread();

                Natives.sendCmdVersion();
                success = true;

                /*
                dev = new ProxmarkDevice(port);

                // Pump out the messages for a bit
                try {
                    while (dev.recvMessage() != null) {
                    }
                } catch (IOException _) {}

                hwinfo = dev.cmdVersion();
                */


            } catch (IOException e) {
                // Deal with error.
                Log.e(TAG, "ioexception in connect", e);
                try {
                    port.close();
                } catch (IOException e2) {
                    Log.d(TAG, "Error closing socket", e2);
                }

                return new ConnectTaskResult().setCommunicationError();
            }

            if (!success) {
                return new ConnectTaskResult().setTimeoutError();
            } else {
                // Stash the mDevice connection somewhere we don't need to parcel it.  HomeActivity
                // will clean up after us.

                //AndProxApplication.getInstance().device = dev;
            }

            // Port is left open at this point.
            return new ConnectTaskResult().setSuccess();
        }

        @Override
        protected void onPostExecute(ConnectTaskResult result) {
            mProgressDialog.hide();

            if (result.noDevicesPresent) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.no_devices_present)
                        .setTitle(R.string.no_devices_present_title);
                builder.show();
            } else if (result.needPermissions) {
                if (!result.alreadyAskedPermissions) {
                    // Ask for permission
                    Log.d(TAG, "requesting permissions");
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION_AUTOCONNECT), 0);
                    mUsbManager.requestPermission(mDevice, mPermissionIntent);
                }
            } else if (result.communicationError) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.communication_error)
                        .setTitle(R.string.communication_error_title);
                builder.show();

            } else if (result.timeoutError) {
                Log.e(TAG, "cmdVersion failed");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.connection_timeout)
                        .setTitle(R.string.connection_timeout_title);
                builder.show();
            } else if (result.success) {
                // Start main activity, yay!
                Intent intent = new Intent(MainActivity.this, CliActivity.class);
                //intent.putExtra(HomeActivity.HWINFO_PARCEL_KEY, result.hwinfo);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "Unhandled ConnectTaskResult state!");
            }
        }
    }

    private class CopyTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(MainActivity.this, "Copying assets", "This may take a few moments", true, false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return Resources.extractPM3Resources(MainActivity.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.hide();

            if (!result) {
                Toast.makeText(MainActivity.this, "Error copying files", Toast.LENGTH_LONG).show();
            }
        }
    }
}
