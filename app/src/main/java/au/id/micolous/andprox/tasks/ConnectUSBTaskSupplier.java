package au.id.micolous.andprox.tasks;


import android.content.Context;

import javax.inject.Inject;

import au.id.micolous.andprox.behavior.firmware.IFirmwareManager;
import au.id.micolous.andprox.behavior.parse.ProxmarkParser;
import au.id.micolous.andprox.behavior.version.ProxmarkDumpDevice;
import au.id.micolous.andprox.functional.Supplier;

public class ConnectUSBTaskSupplier implements Supplier<ConnectUSBTask> {

    private Context context;
    private ProxmarkParser parser;
    private ProxmarkDumpDevice dumpDevice;
    private IFirmwareManager firmwareManager;

    @Inject
    public ConnectUSBTaskSupplier(Context context, ProxmarkParser parser,
                                  ProxmarkDumpDevice dumpDevice, IFirmwareManager firmwareManager) {
        this.context = context;
        this.parser = parser;
        this.dumpDevice = dumpDevice;
        this.firmwareManager = firmwareManager;
    }

    @Override
    public ConnectUSBTask get() {
        return new ConnectUSBTask(this.context, this.parser,
                this.dumpDevice, this.firmwareManager);
    }
}
