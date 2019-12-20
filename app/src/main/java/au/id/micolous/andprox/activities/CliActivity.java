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
package au.id.micolous.andprox.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import au.id.micolous.andprox.R;
import au.id.micolous.andprox.handlers.HandlerInterface;
import au.id.micolous.andprox.hw.TuneTask;
import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.tasks.SendCommandTask;

public class CliActivity extends InjectableActivity implements SendCommandTask.SendCommandCallback {
    private static final String TAG = "CliActivity";

    private EditText etCommandInput;
    private TextView tvOutputBuffer;
    private ScrollView svOutputBuffer;
    private boolean mDisconnected = false;

    private FloatingActionButton fabCli;
    @Nullable
    private String lastCommand = null;
    @Nullable
    private HandlerInterface mHandlerInterface = null;
    @Nullable
    private BroadcastReceiver mDisconnectBroadcastReciever = null;

    private static final String LAST_COMMAND = "last_command";
    private static final String OUTPUT_BUFFER = "output_buffer";
    public static final String HANDLER_INTERFACE = "handler_interface";

    private void writePrompt(String cmd) {
        Natives.javaPrintAndLog("proxmark3> " + cmd);
    }

    private void lockEditField() {
        // Lock the edit field to indicate we are waiting for proxmark3
        etCommandInput.setEnabled(false);
        etCommandInput.setHint(R.string.command_waiting);
    }

    private void redrawFab() {
        boolean showFab = svOutputBuffer.getScrollY() < (tvOutputBuffer.getBottom() - svOutputBuffer.getHeight());
        boolean isVisible = fabCli.getVisibility() == View.VISIBLE;

        if (showFab == isVisible) {
            return;
        }

        if (showFab) {
            fabCli.show();
        } else {
            fabCli.hide();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cli);
        if (!preferences.allowSleep()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        SendCommandTask.register(this);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mHandlerInterface = extras.getParcelable(HANDLER_INTERFACE);
            }
        } else {
            mHandlerInterface = savedInstanceState.getParcelable(HANDLER_INTERFACE);
        }

        fabCli = findViewById(R.id.fabCli);
        fabCli.setOnClickListener(v -> scrollToBottom());

        tvOutputBuffer = findViewById(R.id.tvOutputBuffer);
        tvOutputBuffer.setMovementMethod(new ScrollingMovementMethod());
        tvOutputBuffer.setTextIsSelectable(true);
        registerForContextMenu(tvOutputBuffer);

        svOutputBuffer = findViewById(R.id.svOutputBuffer);

        fabCli.hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            svOutputBuffer.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> redrawFab());
        }

        etCommandInput = findViewById(R.id.etCommandInput);
        etCommandInput.setOnEditorActionListener((v, actionId, event) -> {
            // Check if "Go" button on soft keyboard was pressed, or ENTER was pressed on hardware
            // keyboard.
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Send a command to the PM3.
                final String cmd = v.getText().toString();
                v.setText("");
                rawCmd(cmd);
                return true;
            }
            return false;
        });

        Natives.registerPrintHandler(new Natives.PrinterArgs() {
            @Override
            public void onPrintAndLog(String log) {
                runOnUiThread(() -> tvOutputBuffer.append("\n" + log));
            }

            @Override
            public void onPrint(String msg) {
                runOnUiThread(() -> tvOutputBuffer.append(msg));
            }
        });

        if (mHandlerInterface != null) {
            final IntentFilter disconnectFilter = mHandlerInterface.getDisconnectBroadcastFilter();
            final HandlerInterface.IntentPredicate disconnectPredicate = mHandlerInterface.getDisconnectBroadcastPredicate();

            if (disconnectFilter != null) {
                mDisconnectBroadcastReciever = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (disconnectPredicate == null || disconnectPredicate.test(intent)) {
                            CliActivity.this.handleDisconnect(true);
                        }
                    }
                };

                registerReceiver(mDisconnectBroadcastReciever, disconnectFilter);
            }
        }

        Natives.registerDisconnectHandler(iface -> handleDisconnect(false));

        if (SendCommandTask.getProgressingCommands() > 0) {
            lockEditField();
        }

        if (savedInstanceState == null) {
            // first send "version" command
            runOnUiThread(this::version);
        }
    }

    private void handleDisconnect(final boolean shouldStopReaderThread) {
        if (mDisconnected) {
            // Only handle it once.
            return;
        }

        mDisconnected = true;
        Log.e(TAG, "USB device disconnected");

        runOnUiThread(() -> {
            if (shouldStopReaderThread) {
                Natives.stopReaderThread();
            }

            try {
                // Lock the edit field to indicate we can't run
                etCommandInput.setEnabled(false);
                etCommandInput.setHint(R.string.usb_disconnected_title);

                AlertDialog.Builder builder = new AlertDialog.Builder(CliActivity.this);
                builder.setMessage(R.string.usb_disconnected)
                        .setTitle(R.string.usb_disconnected_title)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
                builder.show();
            } catch (Exception ex) {
                Log.w(TAG, "couldn't lock window", ex);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Natives.registerPrintHandler(null);
        if (mDisconnectBroadcastReciever != null) {
            unregisterReceiver(mDisconnectBroadcastReciever);
        }
        CliActivity.this.handleDisconnect(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(OUTPUT_BUFFER, tvOutputBuffer.getText().toString());
        outState.putString(LAST_COMMAND, lastCommand);
        outState.putParcelable(HANDLER_INTERFACE, mHandlerInterface);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        tvOutputBuffer.setText(savedInstanceState.getString(OUTPUT_BUFFER));
        lastCommand = savedInstanceState.getString(LAST_COMMAND);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cli, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.miTuneAntenna).setEnabled(!Natives.isOffline()
                && SendCommandTask.getProgressingCommands() == 0);

        return super.onPrepareOptionsMenu(menu);
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
                tuneAntenna();
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

    // Tunes the antennas
    public void tuneAntenna() {
        writePrompt("hw tune");
        TuneTask t = new TuneTask(this);
        t.execute();
    }

    // Show version info
    public void version() {
        rawCmd("hw version");
    }

    public void rawCmd(String cmd) {
        // Send a command to the PM3.

        if (!"".equals(cmd)) {
            lastCommand = cmd;
        }


        // Send "hw tune" to our nicer tuning UI graphs.
        if ("hw tune".equals(cmd)) {
            tuneAntenna();
            return;
        }

        Log.i(TAG, "Sending command: " + cmd);
        writePrompt(cmd);

        // Scroll to bottom
        scrollToBottom();

        new SendCommandTask().execute(cmd);
        lockEditField();
        return;
    }

    @Override
    public void onCommandFinished() {
        if (SendCommandTask.getProgressingCommands() <= 0) {
            // Unlock the edit field
            etCommandInput.setEnabled(true);
            etCommandInput.setHint(R.string.command_hint);

            // Scroll to bottom
            scrollToBottom();
        }
    }

    private void scrollToBottom() {
        svOutputBuffer.post(() -> svOutputBuffer.smoothScrollTo(0, tvOutputBuffer.getBottom()));
    }
}
