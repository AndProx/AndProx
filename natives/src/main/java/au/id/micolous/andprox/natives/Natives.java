/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2017 Michael Farrell <micolous+git@gmail.com>
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

package au.id.micolous.andprox.natives;

import android.os.Environment;
import android.util.Log;

public class Natives {
    private static final String TAG = "Natives";
    private static PrinterArgs printAndLogHandler;
    public static final String PM3_STORAGE_ROOT = Environment.getExternalStorageDirectory() + "/proxmark3/";

    public interface PrinterArgs {
        /**
         * Called when a single log line is sent by the client.
         *
         * This blocks the Receiver thread in the client, so should execute quickly (or
         * asynchronously).
         * @param log A single log line to be displayed.
         */
        void onPrint(String log);
    }

    /**
     * Registers a handler to receive PrintAndLog commands from the Proxmark3 client.
     *
     * Only one handler can be active at any time.
     * @param pa A PrinterArgs implementation to receive the messages.
     */
    public static void registerPrintAndLogHandler(PrinterArgs pa) {
        printAndLogHandler = pa;
    }

    /**
     * Used internally by natives.c to surface PrintAndLog events into Java space.
     * @param log A log line
     */
    static void javaPrintAndLog(String log) {
        Log.d(TAG, log);

        if (printAndLogHandler != null) {
            printAndLogHandler.onPrint(log);
        }
    }

    static void javaPrintf(String log) {
        // TODO
        Log.d(TAG, log);
    }

    /**
     * Sets up the Proxmark3 client, and clears existing context.
     */
    public native static void initProxmark();

    /**
     * Sets the serial port to be used by the Proxmark3 client, and sets the client to "online"
     * mode.
     * @param nsw A NativeSerialWrapper which wraps a serial port connection.
     */
    public native static void setSerialPort(NativeSerialWrapper nsw);

    /**
     * Unsets the serial port to be used by the Proxmark3 client, and sets the client to "offline"
     * mode.
     */
    public native static void unsetSerialPort();

    /**
     * Starts a reader thread to pump events from the PM3.
     */
    public native static void startReaderThread();

    /**
     * Stops the reader thread pumping events from the PM3.
     */
    public native static void stopReaderThread();

    /**
     * Sends "hw version" (get hardware version).  Currently does not return this data...
     */
    public native static void sendCmdVersion();

    /**
     * Sends an arbitrary command to the PM3 library.
     * @param cmd A command string.
     */
    public native static void sendCmd(String cmd);

    public native static String getProxmarkClientVersion();

    public native static String getProxmarkClientBuildTimestamp();

    /**
     * Used by native interfaces to get the storage path.
     * @return Path to storage for the app.
     */
    static String getPM3StorageRoot() {
        return PM3_STORAGE_ROOT;
    }

    static {
        System.loadLibrary("natives");
    }
}
