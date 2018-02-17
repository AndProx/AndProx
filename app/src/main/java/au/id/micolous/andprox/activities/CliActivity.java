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
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import au.id.micolous.andprox.R;
import au.id.micolous.andprox.SendCommandTask;
import au.id.micolous.andprox.hw.TuneTask;
import au.id.micolous.andprox.natives.Natives;

public class CliActivity extends AppCompatActivity implements SendCommandTask.DoneCallback {
    private static final String TAG = "CliActivity";

    private EditText etCommandInput;
    private TextView tvOutputBuffer;
    private BroadcastReceiver mUsbReceiver;
    private String lastCommand = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli);

        tvOutputBuffer = findViewById(R.id.tvOutputBuffer);
        tvOutputBuffer.setMovementMethod(new ScrollingMovementMethod());

        etCommandInput = findViewById(R.id.etCommandInput);
        etCommandInput.setOnEditorActionListener((v, actionId, event) -> {
            // Check if "Go" button on soft keyboard was pressed, or ENTER was pressed on hardware
            // keyboard.
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Send a command to the PM3.
                String cmd = v.getText().toString();

                if (!"".equals(cmd)) {
                    lastCommand = cmd;
                }
                v.setText("");
                tvOutputBuffer.append("\nproxmark3> " + cmd);

                Log.i(TAG, "Sending command: " + cmd);
                new SendCommandTask(this).execute(cmd);

                // Lock the edit field to indicate we are waiting for proxmark3
                etCommandInput.setEnabled(false);
                etCommandInput.setHint(R.string.command_waiting);
                return true;
            }
            return false;
        });

        Natives.registerPrintAndLogHandler(log -> {
            runOnUiThread(() -> tvOutputBuffer.append("\n" + log));
        });

        mUsbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        Natives.stopReaderThread();
                        Natives.unsetSerialPort();

                        Log.e(TAG, "USB device disconnected");
                        AlertDialog.Builder builder = new AlertDialog.Builder(CliActivity.this);
                        builder.setMessage(R.string.usb_disconnected)
                                .setTitle(R.string.usb_disconnected_title);
                        builder.show();
                        finish();
                    }
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Natives.registerPrintAndLogHandler(null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cli, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.miSysInfo:
                i = new Intent(CliActivity.this, SysInfoActivity.class);
                startActivity(i);
                return true;

            case R.id.miAbout:
                i = new Intent(CliActivity.this, AboutActivity.class);
                startActivity(i);
                return true;

            case R.id.miTuneAntenna:
                TuneTask t = new TuneTask(this);
                t.execute();
                return true;

            case R.id.miRecall:
                if (lastCommand != null) {
                    EditText etCommandInput = findViewById(R.id.etCommandInput);
                    etCommandInput.setText(lastCommand);
                    etCommandInput.setSelection(lastCommand.length());
                } else {
                    Toast.makeText(CliActivity.this, R.string.no_cmd_buffer, Toast.LENGTH_LONG).show();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCommandFinished() {
        // Unlock the edit field
        etCommandInput.setEnabled(true);
        etCommandInput.setHint(R.string.command_hint);
    }
}
