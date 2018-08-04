package au.id.micolous.andprox;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ProxmarkVersion {
    public enum Branch {
        UNKNOWN,
        OFFICIAL,
        ICEMAN
    }

    private ProxmarkVersion() {}

    @Nullable
    public static ProxmarkVersion parse(@Nullable String s) {
        if (s == null) {
            return null;
        }

        ProxmarkVersion v = new ProxmarkVersion();


        return v;
    }

    @NonNull
    private Branch mBranch;

    @NonNull
    public Branch getBranch() {
        return mBranch;
    }

}
