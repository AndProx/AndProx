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
package au.id.micolous.andprox.tasks;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

import au.id.micolous.andprox.behavior.parse.ProxmarkParser;
import au.id.micolous.andprox.behavior.version.ProxmarkDumpDevice;
import au.id.micolous.andprox.handlers.HandlerInterface;
import au.id.micolous.andprox.handlers.UsbBroadcastHandler;
import au.id.micolous.andprox.natives.NativeSerialWrapper;
import au.id.micolous.andprox.serial.UsbSerialAdapter;

import static au.id.micolous.andprox.activities.MainActivity.ACTION_USB_PERMISSION_AUTOCONNECT;

public class ConnectUSBTask extends ConnectTask {
    private static final String TAG = "ConnectUSBTask";

    private UsbManager mUsbManager;
    private UsbDevice mDevice = null;

    private ProxmarkDumpDevice dumpDevice;

    public ConnectUSBTask(Context context, ProxmarkParser parser, ProxmarkDumpDevice dumpDevice) {
        super(context, parser);
        this.dumpDevice = dumpDevice;
    }

    @Override
    protected final void onPreExecute(final Context c) {
        mUsbManager = (UsbManager) c.getSystemService(Context.USB_SERVICE);
    }

    @Override
    @Nullable
    protected NativeSerialWrapper connectToDevice(boolean firstTry) {
        // List all the devices
        dumpDevice.dumpUsbDeviceInfo(mUsbManager);

        // Try to connect to proxmark
        // Find all available drivers from attached devices.
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        Log.d(TAG, String.format("Found %d available driver(s)", availableDrivers.size()));
        if (availableDrivers.isEmpty()) {
            setResult(new ConnectTaskResult().setNoDevicesPresent());
            return null;
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
            if (firstTry) {
                // We were called from a button press, so we can ask again.
                Log.d(TAG, "asking for permission again");
                setResult(new ConnectTaskResult().setNeedPermissions());
                return null;
            } else {
                // Permissions still not working, but we have a null view (called from mUsbReceiver)
                Log.e(TAG, "permissions still not working");

                setResult(new ConnectTaskResult().setAlreadyAskedPermissions());
                return null;
            }
        }

        // Read some data! Most have just one port (port 0).
        UsbSerialPort port = driver.getPorts().get(0);

        try {
            port.open(connection);
            //port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            Log.e(TAG, "ioexception in connect", e);

            try {
                port.close();
            } catch (IOException e1) {
                Log.d(TAG, "error closing socket", e1);
            }

            setResult(new ConnectTaskResult().setCommunicationError());
            return null;
        }

        // We have an open port, pass it back.
        setResult(new ConnectTaskResult().setSuccess());
        return new NativeSerialWrapper(new UsbSerialAdapter(port));
    }

    @Override
    protected void requestPermission(@NonNull Context c) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0,
                new Intent(ACTION_USB_PERMISSION_AUTOCONNECT), 0);
        mUsbManager.requestPermission(mDevice, pendingIntent);
    }

    @Nullable
    @Override
    protected HandlerInterface getHandlerInterface() {
        return new UsbBroadcastHandler(mDevice);
    }
}
