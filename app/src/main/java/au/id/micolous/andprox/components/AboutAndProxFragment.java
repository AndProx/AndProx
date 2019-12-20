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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import au.id.micolous.andprox.R;
import au.id.micolous.andprox.Utils;
import au.id.micolous.andprox.behavior.format.IFormatDevice;
import au.id.micolous.andprox.natives.Natives;


/**
 * Fragment which displays the AndProx and Proxmark3 versions.
 */
public class AboutAndProxFragment extends InjectableFragment {

    public AboutAndProxFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AboutAndProxFragment.
     */
    public static AboutAndProxFragment newInstance() {
        return new AboutAndProxFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about_and_prox, container, false);

        ((TextView)v.findViewById(R.id.tvVersionString)).setText(
                Utils.localizeString(getContext(), R.string.app_version, formatDevice.getVersionString()));

        ((TextView)v.findViewById(R.id.tvPm3ClientVersion)).setText(
                Utils.localizeString(getContext(), R.string.pm3_client_version, Natives.getProxmarkClientVersion()));

        ((TextView)v.findViewById(R.id.tvPm3BuildTS)).setText(
                Utils.localizeString(getContext(), R.string.pm3_build_ts, Natives.getProxmarkClientBuildTimestamp()));

        v.findViewById(R.id.btnWebsite).setOnClickListener(v1 -> startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AndProx/AndProx"))));

        return v;
    }

}
