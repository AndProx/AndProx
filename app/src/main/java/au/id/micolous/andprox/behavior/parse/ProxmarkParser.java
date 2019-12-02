package au.id.micolous.andprox.behavior.parse;

import android.arch.core.util.Function;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.id.micolous.andprox.behavior.version.ProxmarkVersion;
import au.id.micolous.andprox.device.ISharedPreferences;

public class ProxmarkParser implements Function<String, ProxmarkVersion> {

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

    private ISharedPreferences preferences;

    public ProxmarkParser(ISharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public ProxmarkVersion apply(String s) {
        if (s == null) {
            return null;
        }

        ProxmarkVersion v = new ProxmarkVersion(preferences);
        // References:
        //  - mainline:
        //    SendVersion: https://github.com/proxmark/proxmark3/blob/master/armsrc/appmain.c
        //  - iceman:
        //    SendVersion: https://github.com/iceman1001/proxmark3/blob/master/armsrc/appmain.c

        String lowerS = s.toLowerCase(Locale.ENGLISH);

        if ((lowerS.contains("version information appears invalid") ||
                lowerS.contains("version information not available"))) {
            v.setBranch(ProxmarkVersion.Branch.ERROR);
            return v;
        }

        if (lowerS.contains("taobao") || lowerS.contains("alibaba") || lowerS.contains("163.com")) {
            v.setBranch(ProxmarkVersion.Branch.CHINA);
            return v;
        }

        if ((lowerS.contains("[ arm ]") && lowerS.contains("[ fpga ]"))) {
            // Looks like Iceman firmware.
            // TODO: parse this string better when we can support iceman version
            v.setBranch(ProxmarkVersion.Branch.ICEMAN);
            return v;
        }

        // Lets parse up the firmware details.
        String lines[] = s.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("os: ") && v.getOsVersion() == null) {
                v.setOsVersion(line.substring(4));
            }

            if (line.startsWith("bootrom: ") && v.getBootromVersion() == null) {
                v.setBootromVersion(line.substring(9));
            }
        }

        if (v.getOsVersion() == null) {
            v.setBranch(ProxmarkVersion.Branch.UNKNOWN);
        } else {
            if (v.getOsVersion().contains("iceman")) {
                v.setBranch(ProxmarkVersion.Branch.ICEMAN);
            }

            if (v.getOsVersion().contains("-dirty") || v.getOsVersion().contains("-unclean")) {
                v.setDirty(true);
            }

            if (v.getOsVersion().contains("-suspect")) {
                v.setSuspect(true);
            }

            v.setSuperSuspect(v.getOsVersion().contains("/-suspect"));
            v.setOsBuildTime(parseIsoDateTime(v.getOsVersion()));

            try {
                Matcher m = PM3_VERSION_MATCHER.matcher(v.getOsVersion());
                if (m.find()) {
                    v.setOsMajorVersion(Integer.parseInt(m.group(1)));
                    v.setOsMinorVersion(Integer.parseInt(m.group(2)));
                    v.setOsPatchVersion(Integer.parseInt(m.group(3)));

                    if (m.groupCount() == 6) {
                        // Git version available too!
                        v.setOsCommitCount(Integer.parseInt(m.group(5)));
                        v.setOsCommitHash(m.group(6));
                    }
                }
            } catch (NumberFormatException ignored) {}

            if (v.getBranch() == ProxmarkVersion.Branch.UNKNOWN && v.getOSBuildTime() != null) {
                v.setBranch(ProxmarkVersion.Branch.OFFICIAL);
            }
        }

        if (v.getBootromVersion() != null) {
            v.setBootromSuperSuspect(v.getBootromVersion().contains("/-suspect"));
            v.setBootromBuildTime(parseIsoDateTime(v.getBootromVersion()));
        }


        return v;
    }

    /**
     * Searches for something that looks like an ISO 8601 datetime string, and attempts to parse it.
     *
     * @param s The string to parse.
     * @return The value of the parsed Calendar, or null on error.
     */
    @Nullable
    private Calendar parseIsoDateTime(@NonNull String s) {
        try {
            Matcher m = ISO_DATE_MATCHER.matcher(s);
            if (m.find()) {
                int year = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int day = Integer.parseInt(m.group(3));

                int hour = Integer.parseInt(m.group(5));
                int minute = Integer.parseInt(m.group(6));
                int second = Integer.parseInt(m.group(7));

                Calendar o = new GregorianCalendar(TimeZone.getTimeZone("Etc/UTC"));
                o.set(Calendar.YEAR, year);
                o.set(Calendar.MONTH, month - 1);
                o.set(Calendar.DAY_OF_MONTH, day);
                o.set(Calendar.HOUR_OF_DAY, hour);
                o.set(Calendar.MINUTE, minute);
                o.set(Calendar.SECOND, second);
                o.set(Calendar.MILLISECOND, 0);
                return o;
            }
        } catch (NumberFormatException ignored) {}

        return null;
    }
}
