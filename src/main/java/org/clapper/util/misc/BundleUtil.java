package org.clapper.util.misc;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 * <tt>ResourceBundle</tt> utilities to aid in localization.
 */
public final class BundleUtil
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    private BundleUtil()
    {
        // Can't be instantiated
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get a string from a bundle, using the default locale.
     *
     * @param bundleName the bundle name
     * @param key        the key to look up
     *
     * @return the value for the key, or the default value
     */
    public static String getString (String bundleName, String key)
    {
        return getMessage (bundleName, null, key, (String) null);
    }

    /**
     * Get a string from a bundle, using the default locale.
     *
     * @param bundleName the bundle name
     * @param key        the key to look up
     * @param defaultMsg the default value, or null
     *
     * @return the value for the key, or the default value
     */
    public static String getString (String bundleName,
                                    String key,
                                    String defaultMsg)
    {
        return getMessage (bundleName, null, key, defaultMsg);
    }

    /**
     * Get a localized message from a bundle.
     *
     * @param bundleName the name of the resource bundle
     * @param locale     the locale
     * @param key        the key
     * @param defaultMsg the default message
     *
     * @return the message, or the default
     */
    public static String getMessage (String bundleName,
                                     Locale locale,
                                     String key,
                                     String defaultMsg)
    {
        return getMessage (bundleName, locale, key, defaultMsg, null);
    }

    /**
     * Get a message from the bundle using the default locale.
     *
     * @param bundleName the name of the resource bundle
     * @param key        the key
     * @param params     parameters for the message
     *
     * @return the message, or the default
     */
    public static String getMessage (String   bundleName,
                                     String   key,
                                     Object[] params)
    {
        return getMessage (bundleName, Locale.getDefault(), key, params);
    }

    /**
     * Get a localized message from the bundle.
     *
     * @param bundleName the name of the resource bundle
     * @param locale     the locale
     * @param key        the key
     * @param defaultMsg the default message
     * @param params     parameters for the message
     *
     * @return the message, or the default
     */
    public static String getMessage (String   bundleName,
                                     Locale   locale,
                                     String   key,
                                     String   defaultMsg,
                                     Object[] params)
    {
        ResourceBundle bundle;
        String         result = null;

        if (locale == null)
            locale = Locale.getDefault();

        bundle = ResourceBundle.getBundle (bundleName, locale);
        if (bundle != null)
        {
            String fmt = null;
            try
            {
                fmt = bundle.getString (key);
            }
            catch (MissingResourceException ex)
            {
            }

            if (fmt == null)
                fmt = defaultMsg;

            if (fmt != null)
                result = MessageFormat.format(fmt, params);
        }

        if (result == null)
            result = defaultMsg;

        return result;
    }

    /**
     * Get a localized message from the bundle.
     *
     * @param bundleName the name of the resource bundle
     * @param locale     the locale
     * @param key        the key
     * @param params     parameters for the message
     *
     * @return the message, or the default
     */
    public static String getMessage (String   bundleName,
                                     Locale   locale,
                                     String   key,
                                     Object[] params)
    {
        ResourceBundle bundle;
        String         result = null;

        if (locale == null)
            locale = Locale.getDefault();

        bundle = ResourceBundle.getBundle (bundleName, locale);
        if (bundle != null)
        {
            try
            {
                String fmt = bundle.getString (key);
                if (fmt != null)
                    result = MessageFormat.format (fmt, params);
            }

            catch (MissingResourceException ex)
            {
            }
        }

        return result;
    }
}
