package au.id.micolous.andprox.behavior.version;

public class ProxmarkDetection {

    private String mExtraDeviceInfo;
    private boolean mProxmarkDetected;
    private boolean mOldProxmarkDetected;

    public void setExtraDeviceInfo(String extraDeviceInfo) {
        mExtraDeviceInfo = extraDeviceInfo;
    }

    public String getExtraDeviceInfo() {
        return mExtraDeviceInfo;
    }

    public void setProxmarkDetected(boolean state) {
        mProxmarkDetected = state;
    }

    public boolean isProxmarkDetected() {
        return mProxmarkDetected;
    }

    public void setOldProxmarkDetected(boolean state) {
        mOldProxmarkDetected = state;
    }

    public boolean isOldProxmarkDetected() {
        return mOldProxmarkDetected;
    }
}
