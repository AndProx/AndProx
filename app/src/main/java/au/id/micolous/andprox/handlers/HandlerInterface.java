package au.id.micolous.andprox.handlers;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Interface to allow different connection modes to hook
 * {@link android.app.Activity#registerReceiver(BroadcastReceiver, IntentFilter)} in order to return
 * standard event types.
 */
public abstract class HandlerInterface implements Parcelable {

    // TODO: Replace with Predicate<Intent> when using API 24+.
    @FunctionalInterface
    public interface IntentPredicate {
        boolean test(Intent intent);
    }

    // Disconnect events

    @Nullable
    public IntentFilter getDisconnectBroadcastFilter() {
        return null;
    }

    @Nullable
    public IntentPredicate getDisconnectBroadcastPredicate() {
        return null;
    }

}
