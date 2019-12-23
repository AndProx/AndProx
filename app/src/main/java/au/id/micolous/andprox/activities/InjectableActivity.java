package au.id.micolous.andprox.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.behavior.firmware.IFirmwareManager;
import au.id.micolous.andprox.behavior.format.IFormatDevice;
import au.id.micolous.andprox.behavior.parse.ProxmarkParser;
import au.id.micolous.andprox.behavior.version.ProxmarkDetection;
import au.id.micolous.andprox.behavior.version.ProxmarkDumpDevice;
import au.id.micolous.andprox.device.ISharedPreferences;
import au.id.micolous.andprox.functional.Supplier;
import au.id.micolous.andprox.tasks.ConnectUSBTask;

public abstract class InjectableActivity extends AppCompatActivity {

    @Inject
    protected ISharedPreferences preferences;

    @Inject
    protected ProxmarkDetection detection;

    @Inject
    protected ProxmarkDumpDevice dumpDevice;

    @Inject
    protected Supplier<ConnectUSBTask> usbTaskSupplier;

    @Inject
    protected ProxmarkParser parser;

    @Inject
    protected IFormatDevice formatDevice;

    @Inject
    protected IFirmwareManager firmwareManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AndProxApplication)getApplicationContext()).inject(this);

    }
}
