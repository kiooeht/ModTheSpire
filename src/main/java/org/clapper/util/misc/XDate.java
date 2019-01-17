package org.clapper.util.misc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Version of <tt>java.util.Date</tt> that provides some extra utility
 * functions.
 */
public class XDate extends Date
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>XDate</tt> so that it represents the time the
     * object was constructed, measure to the nearest millisecond.
     */
    public XDate()
    {
        super();
    }

    /**
     * Create a new <tt>XDate</tt> object and initialize it to represent the
     * specified number of milliseconds since the epoch.
     *
     * @param millis  the milliseconds
     */
    public XDate (long millis)
    {
        super (millis);
    }

    /**
     * Create a new <tt>XDate</tt> object and initialize it to represent the
     * time contained in the specified, existing <tt>Date</tt> object (which
     * may, itself, be an <tt>XDate</tt>).
     *
     * @param date  the date
     */
    public XDate (Date date)
    {
        super (date.getTime());
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Convert this date from its time zone to another. Sample use:
     *
     * <pre>
     * // Convert time in default time zone to UTC
     *
     * XDate now = new XDate();
     * TimeZone tzUTC = TimeZone.getTimeZone ("UTC");
     * Date utc = now.convertToTimeZone (tzUTC);
     * DateFormat fmt = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss z");
     * fmt.setTimeZone (tzUTC);
     * System.out.println (fmt.format (utc));
     * </pre>
     *
     * <p>Or, more simply:</p>
     *
     * <pre>
     * XDate now = new XDate();
     * System.out.println (now.formatInTimeZone ("yyyy/MM/dd HH:mm:ss z",
     *                                           TimeZone.getTimeZone ("UTC")));
     * </pre>
     *
     * @param tz  the time zone to convert the date to
     *
     * @return a new <tt>XDate</tt> object, appropriately converted. This
     *         result can safely be stored in a <tt>java.util.Date</tt>
     *         reference.
     *
     * @see #formatInTimeZone
     */
    public XDate convertToTimeZone (TimeZone tz)
    {
        Calendar calFrom = new GregorianCalendar (TimeZone.getDefault());
        Calendar calTo = new GregorianCalendar (tz);
        calFrom.setTimeInMillis (getTime());
        calTo.setTimeInMillis (calFrom.getTimeInMillis());
        return new XDate (calTo.getTime());
    }

    /**
     * Convenience method to produce a printable date in a specified time
     * zone, using a <tt>SimpleDateFormat</tt>. Calling this method is
     * roughly equivalent to:
     *
     * <pre>
     * XDate now = new XDate();
     * TimeZone tzUTC = TimeZone.getTimeZone ("UTC");
     * Date utc = now.convertToTimeZone (tzUTC);
     * DateFormat fmt = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss z");
     * fmt.setTimeZone (tzUTC);
     * String formattedDate = fmt.format (utc);
     * </pre>
     *
     * @param dateFormat  the date format string to use, in a form that's
     *                    compatible with <tt>java.text.SimpleDateFormat</tt>
     * @param tz          the desired time zone
     *
     * @return the formatted date string
     */
    public String formatInTimeZone (String dateFormat, TimeZone tz)
    {
        Date tzDate = convertToTimeZone (tz);
        DateFormat fmt = new SimpleDateFormat (dateFormat);
        fmt.setTimeZone (tz);
        return fmt.format (tzDate);
    }
}
