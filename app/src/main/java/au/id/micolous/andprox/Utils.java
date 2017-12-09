package au.id.micolous.andprox;

import android.content.res.Resources;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;

/**
 * Helper utilities in AndProx
 */

public final class Utils {
    /**
     * Given a string resource (R.string), localize the string according to the language preferences
     * on the device.
     *
     * @param stringResource R.string to localize.
     * @param formatArgs     Formatting arguments to pass
     * @return Localized string
     */
    public static String localizeString(@StringRes int stringResource, Object... formatArgs) {
        Resources res = AndProxApplication.getInstance().getResources();
        return res.getString(stringResource, formatArgs);
    }

    /**
     * Given a plural resource (R.plurals), localize the string according to the language preferences
     * on the device.
     *
     * @param pluralResource R.plurals to localize.
     * @param quantity       Quantity to use for pluaralisation rules
     * @param formatArgs     Formatting arguments to pass
     * @return Localized string
     */
    public static String localizePlural(@PluralsRes int pluralResource, int quantity, Object... formatArgs) {
        Resources res = AndProxApplication.getInstance().getResources();
        return res.getQuantityString(pluralResource, quantity, formatArgs);
    }
}
