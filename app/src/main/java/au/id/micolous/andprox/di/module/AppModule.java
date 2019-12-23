package au.id.micolous.andprox.di.module;

import android.content.Context;

import javax.inject.Singleton;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.behavior.firmware.FirmwareManagerImpl;
import au.id.micolous.andprox.behavior.firmware.IFirmwareManager;
import au.id.micolous.andprox.behavior.format.FormatDeviceImpl;
import au.id.micolous.andprox.behavior.format.IFormatDevice;
import au.id.micolous.andprox.behavior.parse.ProxmarkParser;
import au.id.micolous.andprox.behavior.version.ProxmarkDetection;
import au.id.micolous.andprox.behavior.version.ProxmarkDumpDevice;
import au.id.micolous.andprox.device.ISharedPreferences;
import au.id.micolous.andprox.device.SharedPreferencesImpl;
import au.id.micolous.andprox.functional.Supplier;
import au.id.micolous.andprox.tasks.ConnectUSBTask;
import au.id.micolous.andprox.tasks.ConnectUSBTaskSupplier;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private AndProxApplication andProxApplication;

    public AppModule(AndProxApplication application) {
        this.andProxApplication = application;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return this.andProxApplication.getApplicationContext();
    }

    @Provides
    @Singleton
    public ISharedPreferences provideSharedPreferences(Context context) {
        return new SharedPreferencesImpl(context);
    }

    @Provides
    @Singleton
    public ProxmarkDetection provideProxmarkDetection() {
        return new ProxmarkDetection();
    }

    @Provides
    @Singleton
    public IFormatDevice provideFormatDevice(ProxmarkDetection detection, Context context, ISharedPreferences sharedPreferences) {
        return new FormatDeviceImpl(detection, context, sharedPreferences);
    }

    @Provides
    public ProxmarkParser provideProxmarkParser(ISharedPreferences preferences) {
        return new ProxmarkParser(preferences);
    }

    @Provides
    public ProxmarkDumpDevice provideProxmarkDump(ProxmarkDetection detection, ISharedPreferences preferences) {
        return new ProxmarkDumpDevice(detection, preferences);
    }

    @Provides
    public Supplier<ConnectUSBTask> providesConnectUSBTaskSupplier(Context context, ProxmarkParser parser,
                                                                   ProxmarkDumpDevice dumpDevice, IFirmwareManager firmwareManager) {
        return new ConnectUSBTaskSupplier(context, parser, dumpDevice, firmwareManager);
    }

    @Provides
    public IFirmwareManager providesFirmwareManager(Context context) {
        return new FirmwareManagerImpl(context);
    }

}
