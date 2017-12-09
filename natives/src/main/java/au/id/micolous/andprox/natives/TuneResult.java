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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wraps a tuning result from the PM3.
 * TODO: implement this
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

    private int[] graphData;

    TuneResult(int[] graphData) {
        this.graphData = graphData;
    }

    protected TuneResult(Parcel in) {
        graphData = in.createIntArray();
    }


    public int[] getGraphData() {
        return graphData;
    }

    public double getMilliVolts125k() {
        return 125;
    }

    public double getMilliVolts134k() {
        return 134;
    }

    public double getLFPeakMillivolts() {
        return 1000;
    }

    public double getLFPeakFrequency() {
        return 123;
    }

    public double getMilliVolts13M() {
        return 1356;
    }

    public int getLFAntennaRating() {
        return 1;
    }

    public int getHFAntennaRating() {
        return 2;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(graphData);
    }
}
