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
package au.id.micolous.andprox.tasks;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import au.id.micolous.andprox.natives.Natives;

/**
 * Dispatches commands to the PM3 client thread.
 */

public class SendCommandTask extends AsyncTask<String, Void, Void> {
    public interface SendCommandCallback {
        void onCommandFinished();
    }

    private static WeakReference<SendCommandCallback> callback = null;
    /**
     * This is a simple ref-counter for how many things are in flight.
     */
    private static int progressingCommands = 0;

    public static void register(SendCommandCallback c) {
        callback = new WeakReference<>(c);
    }

    /**
     * Returns the number of commands that are currently in progress.
     */
    public static int getProgressingCommands() {
        return progressingCommands;
    }

    private static SendCommandCallback getDoneCallback() {
        if (callback != null) {
            SendCommandCallback c = callback.get();
            if (c != null) {
                return c;
            }
        } else {
            // The weak reference has gone, also destroy it.
            callback = null;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressingCommands++;
    }

    @Override
    protected Void doInBackground(String... cmds) {
        for (String cmd : cmds) {
            Natives.sendCmd(cmd);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressingCommands--;
        SendCommandCallback c = getDoneCallback();
        if (c != null) c.onCommandFinished();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        progressingCommands--;
        SendCommandCallback c = getDoneCallback();
        if (c != null) c.onCommandFinished();
    }
}
