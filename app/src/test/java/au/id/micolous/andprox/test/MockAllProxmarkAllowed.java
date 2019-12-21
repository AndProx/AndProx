package au.id.micolous.andprox.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import au.id.micolous.andprox.device.ConnectivityMode;
import au.id.micolous.andprox.device.ISharedPreferences;

public class MockAllProxmarkAllowed implements ISharedPreferences {
    @Override
    public boolean allowAllProxmarkDevices() {
        return true;
    }

    @Override
    public boolean useAndroidEmulatorHost() {
        return false;
    }

    @Override
    public boolean allowSleep() {
        return false;
    }

    @Override
    public String getTcpHostStr() {
        return null;
    }

    @Override
    public InetAddress getTcpHost() throws UnknownHostException {
        return null;
    }

    @Override
    public int getTcpPort() {
        return 0;
    }

    @Override
    public String getConnectivityModeStr() {
        return null;
    }

    @Override
    public ConnectivityMode getConnectivityMode() {
        return null;
    }

    @Override
    public boolean hasUsbHostSupport() {
        return false;
    }
}
