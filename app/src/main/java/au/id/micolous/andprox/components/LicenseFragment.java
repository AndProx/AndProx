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
package au.id.micolous.andprox.components;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import au.id.micolous.andprox.R;
import dagger.android.support.DaggerFragment;

/**
 * Fragment which displays all the license acknowledgements.
 */
public class LicenseFragment extends DaggerFragment {
    private static final String TAG = LicenseFragment.class.getSimpleName();

    public LicenseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LicenseFragment.
     */
    public static LicenseFragment newInstance() {
        return new LicenseFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_license, container, false);
        TextView t = v.findViewById(R.id.tvLicenseText);

        // These strings are deliberately not localised.
        readLicenseTextFromAsset(t, "NOTICE.AndProx.txt");

        readLicenseTextFromAsset(t, "third_party/NOTICE.proxmark3.txt");
        readLicenseTextFromAsset(t, "third_party/NOTICE.jansson.txt");
        readLicenseTextFromAsset(t, "third_party/NOTICE.lua.txt");
        readLicenseTextFromAsset(t, "third_party/NOTICE.zlib.txt");

        readLicenseTextFromAsset(t, "third_party/NOTICE.GraphView.txt");
        readLicenseTextFromAsset(t, "third_party/NOTICE.usb-serial-for-android.txt");
        readLicenseTextFromAsset(t, "third_party/NOTICE.webrtc.txt");

        return v;
    }

    private void readLicenseTextFromAsset(@NonNull TextView t, @NonNull String path) {
        final Context ctx = getContext();
        if (ctx == null) {
            licenseError(t, path);
            return;
        }

        final AssetManager assets = ctx.getAssets();
        if (assets == null) {
            licenseError(t, path);
            return;
        }

        try {
            final InputStream s = assets.open(path, AssetManager.ACCESS_RANDOM);
            final Scanner sc = new Scanner(s, "UTF-8");
            final String o = sc.useDelimiter("\\A").hasNext() ? sc.next() : "";

            t.append(o);
            t.append("\n\n");
        } catch (IOException e) {
            //noinspection StringConcatenation
            Log.w(TAG, "Error reading license: " + path, e);
            licenseError(t, path);
        }

    }

    private void licenseError(@NonNull TextView t, @NonNull String path) {
        t.append("\n\n** Error reading license notice from " + path + "\n\n");
    }
}
