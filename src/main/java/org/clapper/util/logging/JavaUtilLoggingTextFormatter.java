package org.clapper.util.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Implements a log formatter for the <tt>java.util.logging</tt>
 * infrastructure that formats each non-exception log record as a simple,
 * single-line text string. (Exception stack traces will cause a message to
 * consume multiple lines.) This formatter currently cannot be configured
 * in any way, though that may change in subsequent releases. The output
 * format is similar to
 * <a href="http://logging.apache.org/log4j/">Log4J's</a>
 * <tt>PatternLayout</tt> class, with a format of "%d %-5p (%c{1}): %m%n".
 */
public class JavaUtilLoggingTextFormatter extends Formatter
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss.SSS");

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new JavaUtilLoggingTextLogFormatter.
     */
    public JavaUtilLoggingTextFormatter()
    {
        super();
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * <p>Format the given log record and return the formatted string.</p>
     *
     * <p>The resulting formatted String will normally include a localized
     * and formated version of the LogRecord's message field.</p>
     *
     * @param record the log record to be formatted
     *
     * @return the formatted log record
     */
    public String format (LogRecord record)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter (sw);
        String loggerName = record.getLoggerName();
        Throwable ex = record.getThrown();
        String message = super.formatMessage (record);

        pw.print (DATE_FORMAT.format (new Date (record.getMillis())));
        pw.print (' ');
        pw.print (record.getLevel().toString());
        pw.print (" (");

        // Logger name is a class name. Strip all but the last part of it.

        int i = loggerName.lastIndexOf (".");
        if ((i != -1) && (i < (loggerName.length() - 1)))
            loggerName = loggerName.substring (i + 1);

        pw.print (loggerName);
        pw.print (") ");

        if ((message != null) && (message.trim().length() > 0))
            pw.println (message);

        if (ex != null)
            ex.printStackTrace (pw);

        return sw.toString();
    }
}
