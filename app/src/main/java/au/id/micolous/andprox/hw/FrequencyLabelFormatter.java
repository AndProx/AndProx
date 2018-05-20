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

package au.id.micolous.andprox.hw;

import com.jjoe64.graphview.DefaultLabelFormatter;

/**
 * This class hacks a log scale into GraphView.
 *
 * We pass the divisors, multiplied by -1, as raw data in TuneResult.
 *
 * This class tells the label formatter to convert those divisors to raw kHz for the X (frequency)
 * axis, and converts millivolts to volts.
 */

public class FrequencyLabelFormatter extends DefaultLabelFormatter {

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // There are 256 divisor, and the frequency is mapped as:
            //    freq_khz = 12000 / (-n + 1)
            // Divisor 95 is 125kHz, and 89 is 133.3 kHz (~134kHz)

            // The official proxmark3 gui represents these as raw values, but we should represent it in
            // frequency order instead.

            value = 12000. / (-value + 1);
        } else {
            value /= 1000.;
        }
        String s = super.formatLabel(value, isValueX);

        if (isValueX) {
            s += "kHz";
        } else {
            s += "v";
        }

        return s;
    }
}
