/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2018-2019 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Under section 7 of the GNU General Public License v3, the following additional
 * terms apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */
package au.id.micolous.andprox.behavior.version;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

import au.id.micolous.andprox.device.ISharedPreferences;

/**
 * Version detection routine for Proxmark3 firmware.
 *
 * {@link au.id.micolous.andprox.test.VersionTest} covers many scenarios.
 */
public class ProxmarkVersion {


    public enum Branch {
        /** We don't know */
        UNKNOWN,
        /** PM3 is explicitly reporting some error */
        ERROR,
        /** https://github.com/Proxmark/proxmark3 */
        OFFICIAL,
        /** https://github.com/iceman1001/proxmark3 */
        ICEMAN,
        /** questionable firmware sources, probably old, many don't comply with GPL license */
        CHINA,
    }

    private ISharedPreferences preferences;

    public ProxmarkVersion(ISharedPreferences preferences) {
        this.preferences = preferences;
    }

    @NonNull
    private Branch mBranch = Branch.UNKNOWN;

    @Nullable
    private String mOsVersion = null;

    @Nullable
    private String mBootromVersion = null;

    private boolean mDirty = false;
    private boolean mSuspect = false;
    private boolean mSuperSuspect = false;
    private boolean mBootromSuperSuspect = false;

    @Nullable
    private Calendar mOsBuildTime = null;
    @Nullable
    private Calendar mBootromBuildTime = null;

    private int mOsMajorVersion = 0;
    private int mOsMinorVersion = 0;
    private int mOsPatchVersion = 0;
    private int mOsCommitCount = 0;
    private String mOsCommitHash = null;

    @NonNull
    public Branch getBranch() {
        return mBranch;
    }

    public void setBranch(@NonNull Branch mBranch) {
        this.mBranch = mBranch;
    }

    @Nullable
    public String getOsVersion() {
        return mOsVersion;
    }

    public void setOsVersion(@Nullable String mOsVersion) {
        this.mOsVersion = mOsVersion;
    }

    @Nullable
    public String getBootromVersion() {
        return mBootromVersion;
    }

    public void setBootromVersion(@Nullable String mBootromVersion) {
        this.mBootromVersion = mBootromVersion;
    }

    public void setBootromSuperSuspect(boolean mBootromSuperSuspect) {
        this.mBootromSuperSuspect = mBootromSuperSuspect;
    }

    public void setBootromBuildTime(@Nullable Calendar mBootromBuildTime) {
        this.mBootromBuildTime = mBootromBuildTime;
    }

    public boolean isSupportedVersion() {
        if (preferences.allowAllProxmarkDevices()) return true;
        if (mBranch != Branch.OFFICIAL) return false;
        if (mSuperSuspect) return false;

        // Declare compatibility with 3.x.x, except 3.0.x.
        if (mOsMajorVersion != 3) return false;
        if (mOsMinorVersion == 0) return false;

        // 3.1.0 release = 2018-10-10
        // TZ=UTC date --date="@1538352000.000"
        // Mon Oct  1 00:00:00 UTC 2018
        return mOsBuildTime != null && mOsBuildTime.getTimeInMillis() >= 1538352000000L;
    }

    /**
     * Does this version contain code that was not checked in to git? (dirty / unclean)
     */
    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean mDirty) {
        this.mDirty = mDirty;
    }

    /**
     * Does this version contain "suspect" tag. (This appears to be ~all builds)
     */
    public boolean isSuspect() {
        return mSuspect;
    }

    public void setSuspect(boolean mSuspect) {
        this.mSuspect = mSuspect;
    }

    /**
     * Does this firmware look like it's missing all git version data?
     */
    public boolean isSuperSuspect() {
        return mSuperSuspect;
    }

    public void setSuperSuspect(boolean mSuperSuspect) {
        this.mSuperSuspect = mSuperSuspect;
    }

    public Calendar getOSBuildTime() {
        return mOsBuildTime;
    }

    public void setOsBuildTime(@Nullable Calendar mOsBuildTime) {
        this.mOsBuildTime = mOsBuildTime;
    }

    public Calendar getBootromBuildTime() {
        return mBootromBuildTime;
    }

    public int getOSMajorVersion() {
        return mOsMajorVersion;
    }

    public void setOsMajorVersion(int mOsMajorVersion) {
        this.mOsMajorVersion = mOsMajorVersion;
    }

    public int getOSMinorVersion() {
        return mOsMinorVersion;
    }

    public void setOsMinorVersion(int mOsMinorVersion) {
        this.mOsMinorVersion = mOsMinorVersion;
    }

    public int getOSPatchVersion() {
        return mOsPatchVersion;
    }

    public void setOsPatchVersion(int mOsPatchVersion) {
        this.mOsPatchVersion = mOsPatchVersion;
    }

    public int getOSCommitCount() {
        return mOsCommitCount;
    }

    public void setOsCommitCount(int mOsCommitCount) {
        this.mOsCommitCount = mOsCommitCount;
    }

    @Nullable
    public String getOSCommitHash() {
        return mOsCommitHash;
    }

    public void setOsCommitHash(String mOsCommitHash) {
        this.mOsCommitHash = mOsCommitHash;
    }
}
