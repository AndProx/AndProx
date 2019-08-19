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
package au.id.micolous.andprox.natives;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wraps a tuning result from the PM3.
 */
public class TuneResult implements Parcelable {
    public static final Creator<TuneResult> CREATOR = new Creator<TuneResult>() {
        @Override
        public TuneResult createFromParcel(Parcel in) {
            return new TuneResult(in);
        }

        @Override
        public TuneResult[] newArray(int size) {
            return new TuneResult[size];
        }
    };

    // Thresholds for antenna quality.
    private static final double LF_UNUSABLE_V = 3.0;
    private static final double LF_MARGINAL_V = 15.0;
    private static final double HF_UNUSABLE_V = 3.2;
    private static final double HF_MARGINAL_V = 8.0;

    private int[] mGraphData;
    private double mVolts125k;
    private double mVolts134k;
    private double mVolts13M;
    private double mLFPeakFrequency;
    private double mLFPeakVolts;

    public TuneResult(long arg0, long arg1, long arg2, int[] graphData) {
        // cmddata.c:CmdTuneSamples
        mVolts125k = (arg0 & 0xffff) / 500.0;
        mVolts134k = ((arg0 >> 16) & 0xffff) / 500.0;
        mVolts13M = (arg1 & 0xffff) / 1000.0;
        mLFPeakFrequency = 12000.0 / ((arg2 & 0xffff) + 1);
        mLFPeakVolts = ((arg2 >> 16) & 0xffff) / 500.0;

        mGraphData = graphData;
    }

    protected TuneResult(Parcel in) {
        mGraphData = in.createIntArray();
        mVolts125k = in.readDouble();
        mVolts134k = in.readDouble();
        mVolts13M = in.readDouble();
        mLFPeakFrequency = in.readDouble();
        mLFPeakVolts = in.readDouble();
    }


    public int[] getGraphData() {
        return mGraphData;
    }

    /**
     * The voltage of the low frequency antenna at 125 kHz, in volts.
     */
    public double getVolts125k() {
        return mVolts125k;
    }

    /**
     * The voltage of the low frequency antenna at 134 kHz, in volts.
     */
    public double getVolts134k() {
        return mVolts134k;
    }

    /**
     * The peak voltage of the low frequency antenna, in volts.
     */
    public double getLFPeakVolts() {
        return mLFPeakVolts;
    }

    /**
     * The frequency at which the low frequency antenna reaches peak voltage, in kHz.
     */
    public double getLFPeakFrequency() {
        return mLFPeakFrequency;
    }

    /**
     * The voltage of the high frequency antenna at 13.56 MHz, in volts.
     */
    public double getVolts13M() {
        return mVolts13M;
    }

    /**
     * Rating of LF antenna quality.
     * @return 0 if unusable, 1 if marginal, 2 if good.
     */
    public int getLFAntennaRating() {
        if (mLFPeakVolts < LF_UNUSABLE_V) {
            return 0;
        }
        return mLFPeakFrequency < LF_MARGINAL_V ? 1 : 2;
    }

    /**
     * Rating of HF antenna quality.
     * @return 0 if unusable, 1 if marginal, 2 if good.
     */
    public int getHFAntennaRating() {
        if (mVolts13M < HF_UNUSABLE_V) {
            return 0;
        }
        return mVolts13M < HF_MARGINAL_V ? 1 : 2;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(mGraphData);
        dest.writeDouble(mVolts125k);
        dest.writeDouble(mVolts134k);
        dest.writeDouble(mVolts13M);
        dest.writeDouble(mLFPeakFrequency);
        dest.writeDouble(mLFPeakVolts);
    }
}
