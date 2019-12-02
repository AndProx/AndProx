package au.id.micolous.andprox.device;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface ISharedPreferences {

    String PREF_ALLOWED_DEVICES = "pref_android_allowed_devices";
    String PREF_CONN_MODE = "pref_conn_mode";
    String PREF_ANDROID_EMU_HOST = "pref_android_emu_host";
    String PREF_TCP_HOST = "pref_tcp_host";
    String PREF_TCP_PORT = "pref_tcp_port";
    String PREF_ALLOW_SLEEP = "pref_allow_sleep";

    String PREF_CONN_USB = "usb";
    String PREF_CONN_TCP = "tcp";
    String PREF_CONN_NONE = "none";

    String DEFAULT_IP = "127.0.0.1";

    boolean allowAllProxmarkDevices();

    boolean useAndroidEmulatorHost();

    boolean allowSleep();

    String getTcpHostStr();

    InetAddress getTcpHost() throws UnknownHostException;

    int getTcpPort();

    String getConnectivityModeStr();

    ConnectivityMode getConnectivityMode();

    boolean hasUsbHostSupport();

}
