package org.clapper.util.text;

import org.clapper.util.misc.*;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class contains methods to parse and format time durations. A time
 * duration is a string like "1 second", "10 minutes", "360 days", "4
 * hours, 30 seconds". This class can
 *
 * <ul>
 *   <li> parse strings of that form to produce delta values (long integers)
 *   <li> produce such strings by subtracting two dates or subtracting
 *        a date and a duration,
 *   <li> add a duration to or subtract a duration from a <tt>Date</tt>,
 *        producing a new <tt>Date</tt>
 * </ul>
 *
 * The parser recognizes the following intervals and synonyms. (The names are
 * in English; if resource bundles for other languages exist, the names will
 * obviously be different.)
 *
 * <ul>
 *   <li> milliseconds (millisecond, ms)
 *   <li> seconds (seconds, sec, secs)
 *   <li> minutes (minute, min, mins)
 *   <li> hours (hour, hr, hrs)
 *   <li> days (day)
 *   <li> weeks (week)
 * </ul>
 *
 * Years and months are omitted to avoid the irregularity of leap years and
 * different month lengths, respectively. Weeks are honored on input only.
 *
 * @since org.clapper.util version 2.4.1
 */
public final class Duration
{
    /*----------------------------------------------------------------------*\
                             Private Classes
    \*----------------------------------------------------------------------*/

    private static enum DurationType
    {
        MILLISECOND,
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK
    };

    private static class DurationForFormat
    {
        final String singular;
        final String plural;

        DurationForFormat(String singular, String plural)
        {
            this.singular = singular;
            this.plural   = plural;
        }
    };

    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final long SECOND_MS = 1000;
    private static final long MINUTE_MS = SECOND_MS * 60;
    private static final long HOUR_MS   = MINUTE_MS * 60;
    private static final long DAY_MS    = HOUR_MS * 24;
    private static final long WEEK_MS   = DAY_MS * 7;

    private static final String BUNDLE_NAME = Duration.class.getName() +
                                              "Bundle";

    /*----------------------------------------------------------------------*\
                             Private Data Items
    \*----------------------------------------------------------------------*/

    private long duration_in_ms = 0L;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor. Equivalent to <tt>new Duration(0)</tt>.
     */
    public Duration()
    {
        this(0L);
    }

    /**
     * Create a new <tt>Duration</tt> object from a long integer representing
     * some elapsed number of milliseconds.
     *
     * @param milliseconds  the elapsed milliseconds
     */
    public Duration(long milliseconds)
    {
        duration_in_ms = milliseconds;
    }

    /**
     * Create a new <tt>Duration</tt> object by determining the amount
     * of time between two dates.
     *
     * @param date1  the first date
     * @param date2  the second date
     */

    public Duration(Date date1, Date date2)
    {
        long ms1 = date1.getTime();
        long ms2 = date2.getTime();
        this.duration_in_ms = Math.abs(ms1 - ms2);
    }

    /**
     * Create a new <tt>Duration</tt> object by parsing the specified
     * duration string. The words in the string are interpreted according to
     * the specified locale. If this method cannot locate an appropriate
     * resource bundle for the locale, it uses the default bundle (which may
     * result in an exception).
     *
     * @param s      the string to parse
     * @param locale locale to use
     *
     * @throws ParseException bad string
     */
    public Duration(String s, Locale locale) throws ParseException
    {
        parse(s, locale);
    }

    /**
     * Create a new <tt>Duration</tt> object by parsing the specified
     * duration string. The current locale is used to interpret the strings.
     *
     * @param s  the string to parse
     *
     * @throws ParseException bad string
     */
    public Duration(String s) throws ParseException
    {
        parse(s, Locale.getDefault());
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the stored duration value, in milliseconds.
     *
     * @return the stored duration value
     */
    public long getDuration()
    {
        return duration_in_ms;
    }

    /**
     * Format the duration value as a string, like the kind of string
     * handled by the {@link #parse} method. This version formats the string
     * using the default locale. For a complete description of what this
     * method produces, see {@link #format(Locale)}.
     *
     * @return the formatted string
     */
    public String format()
    {
        return format(Locale.getDefault());
    }

    /**
     * <p>Format the duration value as a string, like the kind of string
     * handled by the {@link #parse} method. For instance, a duration value
     * of 1 is formatted as "1 millisecond". A duration value of 86460000
     * is formatted as "1 day, 1 hour".</p>
     *
     * @param locale  Locale to use for the strings, or null for the default.
     *                If there's no bundle for the specified locale, English
     *                is used.
     *
     * @return the string
     */
    public String format(Locale locale)
    {
        Map<DurationType,DurationForFormat> map = getFormatterMap(locale);

        long milliseconds = duration_in_ms;
        long days = milliseconds / DAY_MS;
        milliseconds -= (days * DAY_MS);
        long hours = milliseconds / HOUR_MS;
        milliseconds -= (hours * HOUR_MS);
        long minutes = milliseconds / MINUTE_MS;
        milliseconds -= (minutes * MINUTE_MS);
        long seconds = milliseconds / SECOND_MS;
        milliseconds -= (seconds * SECOND_MS);

        StringBuilder buf = new StringBuilder();
        String sep = "";

        if (days > 0)
        {
            formatForDurationUnit(days, map.get(DurationType.DAY), buf, sep);
            sep = ", ";
        }

        if (hours > 0)
        {
            formatForDurationUnit(hours, map.get(DurationType.HOUR), buf, sep);
            sep = ", ";
        }

        if (minutes > 0)
        {
            formatForDurationUnit(minutes, map.get(DurationType.MINUTE), buf, sep);
            sep = ", ";
        }

        if (seconds > 0)
        {
            formatForDurationUnit(seconds, map.get(DurationType.SECOND), buf, sep);
            sep = ", ";
        }

        if (milliseconds > 0)
        {
            formatForDurationUnit(milliseconds, map.get(DurationType.MILLISECOND), buf, sep);
            sep = ", ";
        }

        return buf.toString();
    }

    /**
     * Parse a string containing a textual description of a duration,
     * setting this object's value to the result of the parse. This method
     * parses the tokens using the default locale. See the version of
     * {@link #parse(String,Locale) parse()} that takes a <tt>Locale</tt>
     * parameter for a more complete explanation of the supported tokens.
     *
     * @param s  the string to parse
     *
     * @throws ParseException parse error
     */
    public void parse(String s)
        throws ParseException
    {
        parse(s, Locale.getDefault());
    }

    /**
     * Parse a string containing a textual description of a duration,
     * setting this object's value to the result of the parse. The string
     * contains one or more token pairs. The token pairs are separated by
     * commas or white space. Each resulting token pair has a numeric token
     * followed by a duration value. Examples will clarify:
     *
     * <ul>
     *   <li>3 days, 19 hours
     *   <li>12 minutes
     *   <li>1 hour, 10 minutes, 33 seconds, 5 milliseconds
     *   <li>5 minutes 3 seconds
     * </ul>
     *
     * @param s      the string to parse
     * @param locale the locale to use when interpreting the tokens, or null
     *               for the default.
     *
     * @throws ParseException parse error
     */
    public void parse(String s, Locale locale)
        throws ParseException
    {
        // First, get the list of legal tokens.

        Map<String,DurationType> tokenMap = getParserMap(locale);

        // Next, break the incoming string into tokens on white space and
        // commas. There must be an even number of tokens.

        String[] tokens = TextUtil.split(s, ", ");
        if ((tokens.length % 2) != 0)
        {
            throw new ParseException("Malformed duration string \"" + s + "\"",
                                      0);
        }

        // Now, parse each pair. The first value in each pair must be a number.
        // The second value must be a duration token.
        duration_in_ms = 0;
        for (int i = 0; i < tokens.length; i += 2)
        {
            // First, parse the number.

            long num = 0;
            try
            {
                num = Long.parseLong(tokens[i]);
                if (num < 0)
                {
                    throw new ParseException("Unexpected negative value in \"" +
                                             s + "\"",
                                             0);
                }
            }

            catch (NumberFormatException ex)
            {
                throw new ParseException("In \"" + s +
                                         "\", Expected numeric token \"" +
                                         tokens[i] + "\" is not numeric.",
                                         0);
            }

            // Next, the duration string.

            DurationType t = tokenMap.get(tokens[i+1].toLowerCase());
            if (t == null)
            {
                throw new ParseException("In \"" + s + "\", found unknown " +
                                         "duration \"" + tokens[i + 1] + "\"",
                                         0);
            }

            switch (t)
            {
                case MILLISECOND:
                    duration_in_ms += num;
                    break;

                case SECOND:
                    duration_in_ms += (num * SECOND_MS);
                    break;

                case MINUTE:
                    duration_in_ms += (num * MINUTE_MS);
                    break;

                case HOUR:
                    duration_in_ms += (num * HOUR_MS);
                    break;

                case DAY:
                    duration_in_ms += (num * DAY_MS);
                    break;

                case WEEK:
                    duration_in_ms += (num * WEEK_MS);
                    break;

                default:
                    assert(false);
            }
        }
    }

    /**
     * Return a string representation of this duration. Note that this method
     * is <i>not</i> the same as the {@link #format} method. <tt>format()</tt>
     * produces a natural language phrase, whereas <tt>toString()</tt> just
     * returns the equivalent of
     * <tt>String.valueOf(Duration.getDuration())</tt>.
     *
     * @return the stringified duration value
     */
    @Override
    public String toString()
    {
        return String.valueOf(getDuration());
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private void formatForDurationUnit(long              count,
                                       DurationForFormat tokens,
                                       StringBuilder     buf,
                                       String            separator)
    {
        if (tokens == null)
            throw new IllegalStateException("Unexpected null DurationForFormat");

        if (count > 0)
        {
            String duration = (count == 1) ? tokens.singular : tokens.plural;
            buf.append(separator);
            buf.append(String.valueOf(count));
            buf.append(" ");
            buf.append(duration);
        }
    }

    private Map<DurationType,DurationForFormat> getFormatterMap(Locale locale)
    {
        Map<DurationType,DurationForFormat> map =
            new HashMap<DurationType,DurationForFormat>();
        XResourceBundle bundle =
            XResourceBundle.getXResourceBundle(BUNDLE_NAME, locale);
        loadBundle(bundle, null, map);

        return map;
    }

    private Map<String,DurationType> getParserMap(Locale locale)
        throws IllegalStateException
    {
        Map<String,DurationType> map = new HashMap<String,DurationType>();
        XResourceBundle bundle = XResourceBundle.getXResourceBundle(BUNDLE_NAME,
                                                                    locale);
        loadBundle(bundle, map, null);

        // Also allow the default.

        bundle = XResourceBundle.getXResourceBundle(BUNDLE_NAME);
        loadBundle(bundle, map, null);
        return map;
    }

    private void loadBundle(XResourceBundle                     bundle,
                            Map<String,DurationType>            mapByToken,
                            Map<DurationType,DurationForFormat> mapByType)
        throws IllegalStateException
    {
        parseTokensFor(DurationType.MILLISECOND,
                       bundle.getString("millisecondTokens",
                                        "millisecond/milliseconds/ms"),
                       mapByToken,
                       mapByType);
        parseTokensFor(DurationType.SECOND,
                       bundle.getString("secondTokens",
                                        "second/seconds/sec/secs"),
                       mapByToken,
                       mapByType);
        parseTokensFor(DurationType.MINUTE,
                       bundle.getString("minuteTokens",
                                        "minute/minutes/min/mins"),
                       mapByToken,
                       mapByType);
        parseTokensFor(DurationType.HOUR,
                       bundle.getString("hourTokens",
                                        "hour/hours/hr/hrs"),
                       mapByToken,
                       mapByType);
        parseTokensFor(DurationType.DAY,
                       bundle.getString("dayTokens", "day/days"),
                       mapByToken,
                       mapByType);
        parseTokensFor(DurationType.WEEK,
                       bundle.getString("weekTokens", "week/weeks"),
                       mapByToken,
                       mapByType);
    }

    private void parseTokensFor(DurationType                        durationType,
                                String                              unparsedValue,
                                Map<String,DurationType>            mapByToken,
                                Map<DurationType,DurationForFormat> mapByType)
        throws IllegalStateException
    {
        String[] tokens = TextUtil.split(unparsedValue, '/');
        if (tokens.length < 2)
        {
            throw new IllegalStateException("Error in resource bundle: Must " +
                                            "have at least two tokens in " +
                                            "duration string. \"" +
                                             unparsedValue + "\" only has " +
                                             tokens.length);
        }

        if (mapByToken != null)
        {
            for (String token : tokens)
                mapByToken.put(token.toLowerCase(), durationType);
        }

        if (mapByType != null)
        {
            mapByType.put(durationType,
                          new DurationForFormat(tokens[0].toLowerCase(),
                                                tokens[1].toLowerCase()));
        }
    }
}
