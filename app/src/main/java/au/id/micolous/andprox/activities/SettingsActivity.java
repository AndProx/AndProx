/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2018-2019 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.andprox.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.R;
import au.id.micolous.andprox.Utils;

import static au.id.micolous.andprox.AndProxApplication.*;

public class SettingsActivity extends AppCompatPreferenceActivity implements Preference.OnPreferenceChangeListener {

    private final boolean inEmulator = Utils.isRunningInEmulator();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final PreferenceManager manager = getPreferenceManager();

        Preference p = manager.findPreference(PREF_ANDROID_EMU_HOST);
        if (p != null && p instanceof CheckBoxPreference) {
            CheckBoxPreference emuHost = (CheckBoxPreference)p;

            emuHost.setDefaultValue(inEmulator);
            if (!inEmulator) {
                emuHost.setSummary(R.string.emulator_host_unavailable);
                emuHost.setSummaryOff(null);
                emuHost.setSummaryOn(null);
            }
            emuHost.setChecked(AndProxApplication.useAndroidEmulatorHost());
        }

        p = manager.findPreference(PREF_CONN_MODE);
        if (p != null && p instanceof ListPreference) {
            ((ListPreference)p).setValue(AndProxApplication.getConnectivityModeStr());
        }

        Utils.setPreferenceListeners(getPreferenceScreen(), this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // Preference validators
        if (PREF_ANDROID_EMU_HOST.equals(preference.getKey())) {
            return inEmulator || !((Boolean) newValue);
        }

        if (PREF_TCP_PORT.equals(preference.getKey())) {
            int v = 0;
            try {
                v = Integer.parseInt((String) newValue);
            } catch (NumberFormatException ignored) {}
            if (v <= 0 || v > 65535) {
                // Invalid input
                showInputError(R.string.invalid_input_title, R.string.invalid_tcp_port);
                return false;
            }
            return true;
        }

        if (PREF_CONN_MODE.equals(preference.getKey())) {
            final String v = (String) newValue;
            if (PREF_CONN_USB.equals(v) && !hasUsbHostSupport()) {
                showInputError(R.string.no_usb_host_title, R.string.no_usb_host);
                return false;
            }

            return true;
        }

        return true;

    }

    private void showInputError(@StringRes int titleRes, @StringRes int messageRes) {
        new AlertDialog.Builder(this)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }


}
