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

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import au.id.micolous.andprox.R;
import au.id.micolous.andprox.databinding.ActivityTuneResultBinding;
import au.id.micolous.andprox.natives.TuneResult;

public class TuneResultActivity extends AppCompatActivity {
    public static final String TUNE_RESULT_KEY = "tune_result";

    public LineGraphSeries<DataPoint> getLineGraph(TuneResult result) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

        // There are 256 divisor, and the frequency is mapped as:
        //    freq_khz = 12000 / (n + 1)
        // Divisor 95 is 125kHz, and 89 is 133.3 kHz (~134kHz)
        //
        // The official proxmark3 gui represents these as raw values, but we should represent it in
        // frequency order instead.
        //
        // The number given is millivolts
        int[] graphData = result.getGraphData();

        for (int x=255; x >= 0; x--) {
            series.appendData(new DataPoint(-x, graphData[x]), true, 256);
        }

        return series;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tune_result);
        ActivityTuneResultBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_tune_result);
        TuneResult tuneResult = getIntent().getParcelableExtra(TUNE_RESULT_KEY);
        binding.setTuneResult(tuneResult);

        GraphView gphTuning = findViewById(R.id.gphTuning);
        LineGraphSeries<DataPoint> tuningSeries = getLineGraph(tuneResult);
        gphTuning.addSeries(tuningSeries);

        gphTuning.getViewport().setScalable(true);
        gphTuning.getViewport().setScrollable(true);

        // Translate the graph around on a pseudo-log scale
        gphTuning.getGridLabelRenderer().setLabelFormatter(new FrequencyLabelFormatter());

    }
}
