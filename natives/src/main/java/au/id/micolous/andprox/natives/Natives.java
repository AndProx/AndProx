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
    public static void javaPrintAndLog(String log) {
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
     * Starts a reader thread to pump events from the PM3.
     */
    public native static void startReaderThread(NativeSerialWrapper nsw);

    /**
     * Stops the reader thread pumping events from the PM3.
     */
    public native static void stopReaderThread();

    /**
     * Sends "hw version" (get hardware version).  Currently does not return this data...
     */
    public native static String sendCmdVersion();

    /**
     * Sends an arbitrary command to the PM3 library.
     * @param cmd A command string.
     */
    public native static void sendCmd(String cmd);

    /**
     * Tunes all the antennas on the PM3, and returns a TuneResult describing the parameters of the
     * antennas.
     * @return A TuneResult on success, else null.
     */
    public static TuneResult sendCmdTune() {
        return sendCmdTune(true, true);
    }

    /**
     * Tunes the antennas on the PM3, and returns a TuneResult describing the parameters of the
     * antennas.
     * @param lf If true, tune low frequency antenna.
     * @param hf If true, tune high frequency antenna.
     * @return A TuneResult on success, else null.
     */
    public native static TuneResult sendCmdTune(boolean lf, boolean hf);

    /**
     * Gets the version number of the Proxmark3 client.
     */
    public native static String getProxmarkClientVersion();

    /**
     * Gets the build timestamp of the Proxmark3 client.
     */
    public native static String getProxmarkClientBuildTimestamp();

    /**
     * Reports on whether the PM3 client is in the "offline" state.
     *
     * "offline" state is where there is no PM3 to communicate with.
     * @return True if "offline", false if online.
     */
    public native static boolean isOffline();

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
