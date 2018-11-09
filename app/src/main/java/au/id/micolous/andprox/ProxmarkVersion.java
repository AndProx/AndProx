/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2018 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.andprox;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // 2000-01-31 23:59:59
    // 2000/01/31 23:59: 1
    // 2000/ 1/31 at  9:59:59
    private static Pattern ISO_DATE_MATCHER = Pattern.compile(
            "(\\d{4})[-\\/]\\s?(\\d{1,2})[-\\/]\\s?(\\d{1,2}) (at )?\\s?(\\d{1,2}):\\s?(\\d{1,2}):\\s?(\\d{1,2})");

    // v3.0.1
    // v3.0.1-1234-g1234567
    private static Pattern PM3_VERSION_MATCHER = Pattern.compile(
            "v(\\d+)\\.(\\d+)\\.(\\d+)(-(\\d+)-g([\\da-f]{7}))?"
    );

    private ProxmarkVersion() {}

    @Nullable
    public static ProxmarkVersion parse(@Nullable String s) {
        if (s == null) {
            return null;
        }

        ProxmarkVersion v = new ProxmarkVersion();
        // References:
        //  - mainline:
        //    SendVersion: https://github.com/proxmark/proxmark3/blob/master/armsrc/appmain.c
        //  - iceman:
        //    SendVersion: https://github.com/iceman1001/proxmark3/blob/master/armsrc/appmain.c

        String lowerS = s.toLowerCase(Locale.ENGLISH);

        if (lowerS.contains("version information appears invalid") ||
                lowerS.contains("version information not available")) {
            v.mBranch = Branch.ERROR;
            return v;
        }

        if (lowerS.contains("taobao") || lowerS.contains("alibaba") || lowerS.contains("163.com") ||
                lowerS.contains(" /-suspect")) {
            v.mBranch = Branch.CHINA;
            return v;
        }

        if ((lowerS.contains("[ arm ]") && lowerS.contains("[ fpga ]"))) {
            // Looks like Iceman firmware.
            // TODO: parse this string better when we can support iceman version
            v.mBranch = Branch.ICEMAN;
            return v;
        }

        // Lets parse up the firmware details.
        String lines[] = s.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("os: ") && v.mOsVersion == null) {
                v.mOsVersion = line.substring(4);
            }

            if (line.startsWith("bootrom: ") && v.mBootromVersion == null) {
                v.mBootromVersion = line.substring(9);
            }
        }

        if (v.mOsVersion == null) {
            v.mBranch = Branch.UNKNOWN;
        } else {
            if (v.mOsVersion.contains("iceman")) {
                v.mBranch = Branch.ICEMAN;
            }

            if (v.mOsVersion.contains("-suspect") || v.mOsVersion.contains("-dirty")) {
                v.mDirty = true;
            }

            try {
                Matcher m = ISO_DATE_MATCHER.matcher(v.mOsVersion);
                if (m.find()) {
                    int year = Integer.parseInt(m.group(1));
                    int month = Integer.parseInt(m.group(2));
                    int day = Integer.parseInt(m.group(3));

                    int hour = Integer.parseInt(m.group(5));
                    int minute = Integer.parseInt(m.group(6));
                    int second = Integer.parseInt(m.group(7));

                    v.mOsBuildTime = new GregorianCalendar(TimeZone.getTimeZone("Etc/UTC"));
                    v.mOsBuildTime.set(Calendar.YEAR, year);
                    v.mOsBuildTime.set(Calendar.MONTH, month - 1);
                    v.mOsBuildTime.set(Calendar.DAY_OF_MONTH, day);
                    v.mOsBuildTime.set(Calendar.HOUR_OF_DAY, hour);
                    v.mOsBuildTime.set(Calendar.MINUTE, minute);
                    v.mOsBuildTime.set(Calendar.SECOND, second);
                    v.mOsBuildTime.set(Calendar.MILLISECOND, 0);
                }
            } catch (NumberFormatException ignored) {}

            try {
                Matcher m = PM3_VERSION_MATCHER.matcher(v.mOsVersion);
                if (m.find()) {
                    v.mOsMajorVersion = Integer.parseInt(m.group(1));
                    v.mOsMinorVersion = Integer.parseInt(m.group(2));
                    v.mOsPatchVersion = Integer.parseInt(m.group(3));

                    if (m.groupCount() == 6) {
                        // Git version available too!
                        v.mOsCommitCount = Integer.parseInt(m.group(5));
                        v.mOsCommitHash = m.group(6);
                    }
                }
            } catch (NumberFormatException ignored) {}

            if (v.mBranch == Branch.UNKNOWN && v.mOsBuildTime != null) {
                v.mBranch = Branch.OFFICIAL;
            }
        }

        if (v.mBootromVersion != null) {
            try {
                Matcher m = ISO_DATE_MATCHER.matcher(v.mBootromVersion);
                if (m.matches()) {
                    int year = Integer.parseInt(m.group(1));
                    int month = Integer.parseInt(m.group(2));
                    int day = Integer.parseInt(m.group(3));

                    int hour = Integer.parseInt(m.group(5));
                    int minute = Integer.parseInt(m.group(6));
                    int second = Integer.parseInt(m.group(7));

                    v.mBootromBuildTime = new GregorianCalendar(TimeZone.getTimeZone("Etc/UTC"));
                    v.mBootromBuildTime.set(Calendar.YEAR, year);
                    v.mBootromBuildTime.set(Calendar.MONTH, month - 1);
                    v.mBootromBuildTime.set(Calendar.DAY_OF_MONTH, day);
                    v.mBootromBuildTime.set(Calendar.HOUR_OF_DAY, hour);
                    v.mBootromBuildTime.set(Calendar.MINUTE, minute);
                    v.mBootromBuildTime.set(Calendar.SECOND, second);
                    v.mBootromBuildTime.set(Calendar.MILLISECOND, 0);
                }
            } catch (NumberFormatException ignored) {}
        }


        return v;
    }

    @NonNull
    private Branch mBranch = Branch.UNKNOWN;

    @Nullable
    private String mOsVersion = null;

    @Nullable
    private String mBootromVersion = null;

    private boolean mDirty = false;

    private Calendar mOsBuildTime = null;
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

    public boolean isSupportedVersion() {
        if (mBranch != Branch.OFFICIAL) return false;

        // Declare compatibility with 3.x.x for now, except 3.0.0.
        if (mOsMajorVersion != 3) return false;
        if (mOsMinorVersion == 0 && mOsPatchVersion == 0) return false;

        // 3.0.1 release = 2017-06-10
        // $ TZ=UTC date --date='@1496275200.000'
        // Thu Jun  1 00:00:00 UTC 2017
        if (mOsBuildTime.getTimeInMillis() < 1496275200000L) return false;

        return true;
    }

    /**
     * Does this version contain code that was not checked in to git? (dirty / suspect)
     */
    public boolean isDirty() {
        return mDirty;
    }

    public Calendar getOSBuildTime() {
        return mOsBuildTime;
    }

    public Calendar getBootromBuildTime() {
        return mBootromBuildTime;
    }

    public int getOSMajorVersion() {
        return mOsMajorVersion;
    }

    public int getOSMinorVersion() {
        return mOsMinorVersion;
    }

    public int getOSPatchVersion() {
        return mOsPatchVersion;
    }

    public int getOSCommitCount() {
        return mOsCommitCount;
    }

    @Nullable
    public String getOSCommitHash() {
        return mOsCommitHash;
    }
}
