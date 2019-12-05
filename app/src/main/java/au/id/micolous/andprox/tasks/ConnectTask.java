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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.ref.WeakReference;

import au.id.micolous.andprox.behavior.parse.ProxmarkParser;
import au.id.micolous.andprox.behavior.version.ProxmarkVersion;
import au.id.micolous.andprox.R;
import au.id.micolous.andprox.activities.CliActivity;
import au.id.micolous.andprox.activities.MainActivity;
import au.id.micolous.andprox.handlers.HandlerInterface;
import au.id.micolous.andprox.natives.NativeSerialWrapper;
import au.id.micolous.andprox.natives.Natives;

/**
 * Task used to connect to a PM3
 */
public abstract class ConnectTask extends AsyncTask<Boolean, Void, ConnectTask.ConnectTaskResult> {
    private static final String TAG = "ConnectTask";

    private ProgressDialog mProgressDialog;
    private WeakReference<Context> mContext;

    class ConnectTaskResult {
        private boolean noDevicesPresent = false;
        private boolean needPermissions = false;
        private boolean alreadyAskedPermissions = false;
        private boolean communicationError = false;
        private boolean timeoutError = false;
        private boolean success = false;
        private boolean unsupported = false;
        private boolean internalError = false;

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

        ConnectTaskResult setUnsuppported() {
            this.unsupported = true;
            return this;
        }

        private ConnectTaskResult setInternalError() {
            this.internalError = true;
            return this;
        }
    }

    private ProxmarkParser parser;

    ConnectTask(Context context, ProxmarkParser parser) {
        mContext = new WeakReference<>(context);
        this.parser = parser;
    }

    public Context getContext() {
        return mContext.get();
    }

    @Override
    protected final void onPreExecute() {
        super.onPreExecute();
        final Context c = mContext.get();

        mProgressDialog = ProgressDialog.show(c,
                c.getString(R.string.connecting_pm3),
                c.getString(R.string.wait_short),
                true, false);

        onPreExecute(c);
    }

    protected void onPreExecute(final Context c) {
        // Do nothing by default.
    }

    @Nullable
    private ConnectTaskResult mResult = null;

    final void setResult(@NonNull ConnectTaskResult result) {
        mResult = result;
    }

    @Nullable
    protected abstract NativeSerialWrapper connectToDevice(boolean firstTry);

    @Override
    protected ConnectTaskResult doInBackground(Boolean... booleans) {
        Natives.initProxmark();

        final NativeSerialWrapper nsw = connectToDevice(booleans[0]);

        if (nsw == null || mResult == null || !mResult.success) {
            if (mResult == null) {
                // Unknown error that resulted in no NativeSerialWrapper.
                mResult = new ConnectTaskResult().setInternalError();
            }

            if (nsw != null) {
                // Close up the NativeSerialWrapper, so that PM3 client stops.
                nsw.close();
            }

            return mResult;
        }

        Natives.startReaderThread(nsw);

        String version = Natives.sendCmdVersion();

        if (version == null) {
            // No version response (timeout).
            nsw.close();
            return new ConnectTaskResult().setTimeoutError();
        }

        // Check if this version is good for us.
        ProxmarkVersion v = parser.apply(version);
        if (v != null && v.isSupportedVersion()) {
            // Port is left open at this point.
            return new ConnectTaskResult().setSuccess();
        } else {
            // Unsupported or unknown version.
            nsw.close();
            return new ConnectTaskResult().setUnsuppported();
        }
    }

    protected void requestPermission(@NonNull Context c) {
        // Do nothing
        Log.d(TAG, "requestPermission unhandled");
    }

    @Override
    protected void onPostExecute(ConnectTaskResult result) {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
            mProgressDialog = null;
        }

        final Context c = mContext.get();

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
                requestPermission(c);
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
        } else if (result.unsupported) {
            //MainActivity.unsupportedFirmwareError(c);
        } else if (result.success) {
            // Start main activity, yay!
            Intent intent = new Intent(c, CliActivity.class);
            intent.putExtra(CliActivity.HANDLER_INTERFACE, getHandlerInterface());
            //intent.putExtra(HomeActivity.HWINFO_PARCEL_KEY, result.hwinfo);
            c.startActivity(intent);
            //finish();
        } else {
            Log.d(TAG, "Unhandled ConnectTaskResult state!");
        }
    }

    @Nullable
    protected HandlerInterface getHandlerInterface() {
        return null;
    }
}
