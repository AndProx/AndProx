package au.id.micolous.andprox.handlers;

import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Defines {@link IntentPredicate} and {@link IntentFilter} for USB device connections.
 */
public final class UsbBroadcastHandler extends HandlerInterface implements Parcelable {
    private static final IntentFilter DISCONNECT_INTENT_FILTER =
            new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);

    private final String mDeviceName;

    public UsbBroadcastHandler(@NonNull UsbDevice device) {
        mDeviceName = device.getDeviceName();
    }

    protected UsbBroadcastHandler(Parcel p) {
        mDeviceName = p.readString();
    }

    @Nullable
    @Override
    public IntentFilter getDisconnectBroadcastFilter() {
        return DISCONNECT_INTENT_FILTER;
    }

    @Nullable
    @Override
    public IntentPredicate getDisconnectBroadcastPredicate() {
        return intent -> {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            return device != null && device.getDeviceName().equals(mDeviceName);
        };
    }

    // Parcelable implementation.
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDeviceName);
    }

    public static final Creator<UsbBroadcastHandler> CREATOR = new Creator<UsbBroadcastHandler>() {
        @Override
        public UsbBroadcastHandler createFromParcel(Parcel in) {
            return new UsbBroadcastHandler(in);
        }

        @Override
        public UsbBroadcastHandler[] newArray(int size) {
            return new UsbBroadcastHandler[size];
        }
    };

}
