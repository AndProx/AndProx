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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import au.id.micolous.andprox.R;
import au.id.micolous.andprox.Utils;
import au.id.micolous.andprox.natives.Resources;

/**
 * Task to copy PM3's static files to storage.
 */
public class CopyTask extends AsyncTask<Void, Pair<Integer, Integer>, Boolean> {
    private ProgressDialog mProgressDialog;

    private WeakReference<Context> mContext;

    public CopyTask(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        final Context c = mContext.get();
        mProgressDialog = new ProgressDialog(c);
        mProgressDialog.setTitle(R.string.copying_assets);
        mProgressDialog.setMessage(Utils.localizeString(R.string.wait_long));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @SafeVarargs
    @Override
    protected final void onProgressUpdate(Pair<Integer, Integer>... values) {
        if (values.length == 0 || values[0] == null) return;

        mProgressDialog.setMax(values[0].second);
        mProgressDialog.setProgress(values[0].first);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return Resources.extractPM3Resources(mContext.get(), this::publishProgress);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
            mProgressDialog = null;
        }

        if (!result) {
            Toast.makeText(mContext.get(), R.string.error_copying, Toast.LENGTH_LONG).show();
        }
    }
}