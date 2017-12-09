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

package au.id.micolous.andprox.hw;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.R;
import au.id.micolous.andprox.natives.TuneResult;

/**
 * Created by michael on 23/12/16.
 */
public class TuneTask extends AsyncTask<Void, Void, TuneResult> {
    private static final String TAG = "TuneTask";
    private AndProxApplication app = AndProxApplication.getInstance();
    private ProgressDialog mProgressDialog;
    private Context mContext;

    public TuneTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = ProgressDialog.show(mContext,
                mContext.getString(R.string.tuning_antenna), mContext.getString(R.string.tuning_antenna_desc),
                true, false);
    }

    @Override
    protected TuneResult doInBackground(Void... voids) {
        // TODO: implement this
        return null;

        /*
        try {
            return app.device.cmdTune();
        } catch (IOException e) {
            Log.e(TAG, "tune error", e);
            return null;
        }
        */
    }

    @Override
    protected void onPostExecute(TuneResult tuneResult) {
        mProgressDialog.hide();
        if (tuneResult != null) {
            // Show the result
            Intent intent = new Intent(mContext, TuneResultActivity.class);
            intent.putExtra(TuneResultActivity.TUNE_RESULT_KEY, tuneResult);
            mContext.startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("sorry!")
                    .setTitle("not implemented yet");
            builder.show();
        }
    }
}
