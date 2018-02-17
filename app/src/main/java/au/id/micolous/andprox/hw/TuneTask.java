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
import android.util.Log;

import java.util.Locale;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.R;
import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.natives.TuneResult;

/**
 * Background task for tuning the antenna.
 */
public class TuneTask extends AsyncTask<Void, Void, TuneResult> {
    private static final String TAG = "TuneTask";
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
        try {
            return Natives.sendCmdTune();
        } catch (Exception e) {
            Log.e(TAG, "tune error", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(TuneResult tuneResult) {
        mProgressDialog.hide();
        if (tuneResult != null) {
            // Show results in the standard log
            Natives.javaPrintAndLog(String.format(Locale.ENGLISH, "LF antenna: %3.2f V @ 125 kHz", tuneResult.getVolts125k()));
            Natives.javaPrintAndLog(String.format(Locale.ENGLISH, "LF antenna: %3.2f V @ 134 kHz", tuneResult.getVolts134k()));
            Natives.javaPrintAndLog(String.format(Locale.ENGLISH, "LF optimal: %3.2f V @ %3.2f kHz", tuneResult.getLFPeakVolts(), tuneResult.getLFPeakFrequency()));
            Natives.javaPrintAndLog(String.format(Locale.ENGLISH, "HF antenna: %3.2f V @ 13.56 MHz", tuneResult.getVolts13M()));

            if (tuneResult.getLFAntennaRating() == 0) {
                Natives.javaPrintAndLog("Your LF antenna is unusable");
            } else if (tuneResult.getLFAntennaRating() == 1) {
                Natives.javaPrintAndLog("Your LF antenna is marginal");
            }

            if (tuneResult.getHFAntennaRating() == 0) {
                Natives.javaPrintAndLog("Your HF antenna is unusable");
            } else if (tuneResult.getHFAntennaRating() == 1) {
                Natives.javaPrintAndLog("Your HF antenna is marginal");
            }

            // Show the result activity
            Intent intent = new Intent(mContext, TuneResultActivity.class);
            intent.putExtra(TuneResultActivity.TUNE_RESULT_KEY, tuneResult);
            mContext.startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Proxmark3 did not return a valid response.")
                    .setTitle("Error tuning antennas");
            builder.show();
        }
    }
}
