package au.id.micolous.andprox.tasks;


import android.content.Context;

import javax.inject.Inject;

import au.id.micolous.andprox.behavior.parse.ProxmarkParser;
import au.id.micolous.andprox.behavior.version.ProxmarkDumpDevice;
import au.id.micolous.andprox.functional.Supplier;

public class ConnectUSBTaskSupplier implements Supplier<ConnectUSBTask> {

    private Context context;
    private ProxmarkParser parser;
    private ProxmarkDumpDevice dumpDevice;

    @Inject
    public ConnectUSBTaskSupplier(Context context, ProxmarkParser parser, ProxmarkDumpDevice dumpDevice) {
        this.context = context;
        this.parser = parser;
        this.dumpDevice = dumpDevice;
    }

    @Override
    public ConnectUSBTask get() {
        return new ConnectUSBTask(this.context, this.parser, this.dumpDevice);
    }
}
