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
package au.id.micolous.andprox.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.R;

/**
 * Displays system information and debugging info that is useful to troubleshoot AndProx issues.
 */
public class SysInfoActivity extends AppCompatActivity {

    private String debugOutput;
    private static final String CLIP_TITLE = "AndProx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_info);
        setTitle(R.string.sys_info_title);

        // Generate debugging information
        debugOutput = AndProxApplication.getDeviceInfo();
        TextView tvDebugOutput = findViewById(R.id.tvDebugOutput);
        tvDebugOutput.setText(debugOutput);
        tvDebugOutput.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sys_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miCopySysInfo:
                ClipboardManager manager = (ClipboardManager)getSystemService(Activity.CLIPBOARD_SERVICE);
                if (manager == null) {
                    Toast.makeText(this, R.string.clipboard_denied, Toast.LENGTH_LONG).show();
                    return true;
                }

                manager.setPrimaryClip(ClipData.newPlainText(CLIP_TITLE, debugOutput));
                Toast.makeText(this, R.string.clipboard_copied, Toast.LENGTH_LONG).show();
                return true;

            case R.id.miShareSysInfo:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, debugOutput);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
