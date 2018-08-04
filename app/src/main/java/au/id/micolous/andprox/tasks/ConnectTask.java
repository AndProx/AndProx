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
package au.id.micolous.andprox.tasks;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import au.id.micolous.andprox.ProxmarkVersion;
import au.id.micolous.andprox.R;
import au.id.micolous.andprox.activities.CliActivity;
import au.id.micolous.andprox.natives.NativeSerialWrapper;
import au.id.micolous.andprox.natives.Natives;

import static au.id.micolous.andprox.activities.MainActivity.ACTION_USB_PERMISSION_AUTOCONNECT;
import static au.id.micolous.andprox.activities.MainActivity.dumpUsbDeviceInfo;

/**
 * Task used to connect to a PM3
 */
public class ConnectTask extends AsyncTask<Boolean, Void, ConnectTask.ConnectTaskResult> {
    private static final String TAG = "ConnectTask";

    private ProgressDialog mProgressDialog;
    private UsbManager mUsbManager;
    private UsbDevice mDevice = null;
    private WeakReference<Context> mContext;

    class ConnectTaskResult {
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

    public ConnectTask(Context context) {
        mContext = new WeakReference<>(context);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context c = mContext.get();

        mProgressDialog = ProgressDialog.show(c,
                c.getString(R.string.connecting_pm3),
                c.getString(R.string.wait_short),
                true, false);
        mUsbManager = (UsbManager) c.getSystemService(Context.USB_SERVICE);

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
            //port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            NativeSerialWrapper nsw = new NativeSerialWrapper(port);
            Natives.initProxmark();
            Natives.startReaderThread(nsw);

            String version = Natives.sendCmdVersion();

            // Check if this version is good for us.
            ProxmarkVersion v = ProxmarkVersion.parse(version);

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
        if (mProgressDialog != null) {
            mProgressDialog.hide();
            mProgressDialog = null;
        }

        Context c = mContext.get();

        if (result.noDevicesPresent) {
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage(R.string.no_devices_present)
                    .setTitle(R.string.no_devices_present_title)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                    .setCancelable(false);
            builder.show();
        } else if (result.needPermissions) {
            if (!result.alreadyAskedPermissions) {
                // Ask for permission
                Log.d(TAG, "requesting permissions");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, new Intent(ACTION_USB_PERMISSION_AUTOCONNECT), 0);
                mUsbManager.requestPermission(mDevice, pendingIntent);
            }
        } else if (result.communicationError) {
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage(R.string.communication_error)
                    .setTitle(R.string.communication_error_title)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                    .setCancelable(false);
            builder.show();

        } else if (result.timeoutError) {
            Log.e(TAG, "cmdVersion failed");
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage(R.string.connection_timeout)
                    .setTitle(R.string.connection_timeout_title)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                    .setCancelable(false);
            builder.show();
        } else if (result.success) {
            // Start main activity, yay!
            Intent intent = new Intent(c, CliActivity.class);
            //intent.putExtra(HomeActivity.HWINFO_PARCEL_KEY, result.hwinfo);
            c.startActivity(intent);
            //finish();
        } else {
            Log.d(TAG, "Unhandled ConnectTaskResult state!");
        }
    }
}
