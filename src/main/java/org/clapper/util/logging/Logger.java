package org.clapper.util.logging;

import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;

/**
 * <p><tt>Logger</tt> wraps the
 * <a href="http://jakarta.apache.org/commons/logging/guide.html" target="_top">Jakarta Commons Logging</a>
 * API and provides a slightly simpler, but similar, interface. This
 * class supports most of the logging methods, but it doesn't actually
 * instantiate an underlying <tt>java.util.logging.Logger</tt> object until
 * (or unless) a thread explicitly calls the {@link #enableLogging} method.
 * The first call to <tt>enableLogging()</tt> traverses the list of
 * instantiated <tt>org.clapper.util.Logger</tt> objects and creates
 * underlying <tt>java.util.logging.Logger</tt> objects for each one. Any
 * <tt>Logger</tt> objects created after <tt>enableLogging()</tt> is called
 * are automatically enabled.</p>
 *
 * <p>This approach prevents any interaction with the real logging layer,
 * unless logging is explicitly enabled (e.g., because a command-line
 * option has been specified). This approach was originally taken to avoid
 * annoying startup messages from the Log4J logging implementation, which
 * insists on writing warning messages to the console when it can't find a
 * configuration file.</p>
 *
 * <p>However, this class can be reimplemented in terms of other logging
 * layers (and, in fact, has been implemented solely in terms of the JDK
 * 1.4 native logging library in the past). This object's main purpose
 * now is to insulate applications from the underlying logging technology,
 * so that technology can be changed, if necessary, without having an
 * impact on applications that use this class.</p>
 *
 * <p>The level mappings used by this class are identical to those used
 * by Commons Logging. (e.g.,, a "debug" message uses the same level as
 * a Commons Logging "debug" message.) Those mappings are:</p>
 *
 * <table>
 *   <caption>Mappings</caption>
 *   <tr>
 *     <th><tt>org.clapper.util.logging.Logger</tt> method</th>
 *     <th>Corresponding <tt>java.util.logging.Level</tt> value</th>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>debug()</tt></td>
 *     <td><tt>FINE</tt></td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>error()</tt></td>
 *     <td><tt>SEVERE</tt></td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>fatal()</tt></td>
 *     <td><tt>SEVERE</tt></td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>info()</tt></td>
 *     <td><tt>INFO</tt></td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>trace()</tt></td>
 *     <td><tt>FINEST</tt></td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>warn()</tt></td>
 *     <td><tt>WARNING</tt></td>
 *   </tr>
 * </table>
 *
 * <p>If you prefer to use Commons Logging directly, then, by all means,
 * use it for your applications.</p>
 *
 * <p>WARNING: If your application installs its own class loader (e.g.,
 * as the thread context class loader), you may have problems with
 * both Jakarta Commons Logging and <tt>java.util.logging</tt> (which
 * Jakarta Commons Logging uses as its default logging implementation).</p>
 *
 * <ul>
 *   <li> The <tt>java.util.logging</tt> layer explicitly bootstraps
 *        itself using the system class loader (i.e., the class loader
 *        that examines the CLASSPATH setting). In some cases, this can
 *        cause problems. For instance, an application that installs its
 *        own class loader and expects to use that class loader to find
 *        a custom-built <tt>java.util.logging</tt> formatter will not
 *        end up using its own formatter, because the formatter class
 *        is not available in the CLASSPATH, and that's where
 *        <tt>java.util.logging</tt> expects to find it. In situations
 *        like that, the best bet is to substitute a better-behaved
 *        underlying logging layer such as
 *        <a href="http://logging.apache.org/log4j/docs/" target="_top">Log4J</a>.
 *
 *   <li> Jakarta Commons Logging uses a "discovery" process that plays
 *        class loader games. You can solve the problem with Commons Logging
 *        by explicitly specifying the underlying logging API to use. For
 *        example:
 *
 *        <pre>
 * java -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger -Dlog4j.configuration=file:/path/to/log4j-debug.properties</pre>
 * </ul>
 *
 * @see #enableLogging
 * @see <a href="http://jakarta.apache.org/commons/logging/index.html" target="_top">Jakarta Commons Logging API</a>
 */
public class Logger
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * Log level constant for debug messages. Defined here for backward
     * compatibility.
     *
     * @see LogLevel#DEBUG
     */
    public static LogLevel LEVEL_DEBUG = LogLevel.DEBUG;

    /**
     * Log level constant for error messages. Defined here for backward
     * compatibility.
     *
     * @see LogLevel#ERROR
     */
    public static LogLevel LEVEL_ERROR = LogLevel.ERROR;

    /**
     * Log level constant for fatal-error messages. Defined here for
     * backward compatibility.
     *
     * @see LogLevel#FATAL
     */
    public static LogLevel LEVEL_FATAL = LogLevel.FATAL;

    /**
     * Log level constant for informational messages. Defined here for
     * backward compatibility.
     *
     * @see LogLevel#INFO
     */
    public static LogLevel LEVEL_INFO = LogLevel.INFO;

    /**
     * Log level constant for trace messages. Defined here for backward
     * compatibility.
     *
     * @see LogLevel#TRACE
     */
    public static LogLevel LEVEL_TRACE = LogLevel.TRACE;

    /**
     * Log level constant for warning messages. Defined here for backward
     * compatibility.
     *
     * @see LogLevel#WARNING
     */
    public static LogLevel LEVEL_WARNING = LogLevel.WARNING;

    /*----------------------------------------------------------------------*\
                           Private Instance Data
    \*----------------------------------------------------------------------*/

    /**
     * The real logging object. Not instantiated unless asked for.
     */
    private org.apache.commons.logging.Log realLogger = null;

    /**
     * The class name to use when instantiating the underlying Log object.
     */
    private String className = null;

    /**
     * The list of existing <tt>Logger</tt> objects.
     */
    private static Collection<Logger> loggers = new ArrayList<Logger>();

    /**
     * Whether or not logging is enabled.
     */
    private static boolean enabled = false;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>Logger</tt> object
     *
     * @param className  the class name to associate with the object
     */
    public Logger (String className)
    {
        this.className = className;

        synchronized (loggers)
        {
            loggers.add (this);

            // Handle the case where the logger is instantiated after all
            // the loggers are enabled.

            if (enabled)
                enableLogger (this);
        }
    }

    /**
     * Construct a new <tt>Logger</tt> object
     *
     * @param cls  the class whose name should be associated with the object
     */
    public Logger (Class cls)
    {
        this (cls.getName());
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Enable logging by instantiating the underlying <tt>Log</tt> objects.
     * Enables logging for all instantiated <tt>Logger</tt> objects, and
     * ensures that any subsequently instantiated <tt>Logger</tt> objects will
     * be enabled.
     *
     * @throws UnsupportedOperationException  could not find Commons Logging
     *                                        API
     */
    public static void enableLogging()
        throws UnsupportedOperationException
    {
        synchronized (loggers)
        {
            if (! enabled)
            {
                for (Logger logger : loggers)
                    enableLogger (logger);

                enabled = true;
            }
        }
    }

    /**
     * Log a message with debug log level.
     *
     * @param message  The message to convert to a string and log
     */
    public void debug (Object message)
    {
        if (realLogger != null)
            realLogger.debug (message.toString());
    }

    /**
     * Log an error with debug log level.
     *
     * @param message  The message to convert to a string and log
     * @param ex       The exception to log with the message
     *
     */
    public void debug (Object message, Throwable ex)
    {
        if (realLogger != null)
            realLogger.debug (message.toString(), ex);
    }

    /**
     * Log a message with error log level.
     *
     * @param message  The message to convert to a string and log
     */
    public void error (Object message)
    {
        if (realLogger != null)
            realLogger.error (message.toString());
    }

    /**
     * Log an error with error log level.
     *
     * @param message  The message to convert to a string and log
     * @param ex       The exception to log with the message
     *
     */
    public void error (Object message, Throwable ex)
    {
        if (realLogger != null)
            realLogger.error (message.toString(), ex);
    }

    /**
     * Log a message with fatal log level.
     *
     * @param message  The message to convert to a string and log
     */
    public void fatal (Object message)
    {
        if (realLogger != null)
            realLogger.fatal (message.toString());
    }

    /**
     * Log an error with fatal log level.
     *
     * @param message  The message to convert to a string and log
     * @param ex       The exception to log with the message
     *
     */
    public void fatal (Object message, Throwable ex)
    {
        if (realLogger != null)
            realLogger.fatal (message.toString(), ex);
    }

    /**
     * Log a message with info log level.
     *
     * @param message  The message to convert to a string and log
     */
    public void info (Object message)
    {
        if (realLogger != null)
            realLogger.info (message.toString());
    }

    /**
     * Log an error with info log level.
     *
     * @param message  The message to convert to a string and log
     * @param ex       The exception to log with the message
     *
     */
    public void info (Object message, Throwable ex)
    {
        if (realLogger != null)
            realLogger.info (message.toString(), ex);
    }

    /**
     * Log a message at a specified log level.
     *
     * @param level   the log level
     * @param message the message
     */
    public void message (LogLevel level, Object message)
    {
        switch (level)
        {
            case DEBUG:
                debug (message);
                break;

            case ERROR:
                error (message);
                break;

            case FATAL:
                fatal (message);
                break;

            case INFO:
                info (message);
                break;

            case TRACE:
                trace (message);
                break;

            case WARNING:
                warn (message);
                break;

            default:
                assert (false);
        }
    }

    /**
     * Log a message at a specified log level.
     *
     * @param level   the log level
     * @param message the message
     * @param ex       The exception to log with the message
     */
    public void message (LogLevel level, Object message, Throwable ex)
    {
        switch (level)
        {
            case DEBUG:
                debug (message, ex);
                break;

            case ERROR:
                error (message, ex);
                break;

            case FATAL:
                fatal (message, ex);
                break;

            case INFO:
                info (message, ex);
                break;

            case TRACE:
                trace (message, ex);
                break;

            case WARNING:
                warn (message, ex);
                break;

            default:
                assert (false);
        }
    }

    /**
     * Log a message with trace log level.
     *
     * @param message  The message to convert to a string and log
     */
    public void trace (Object message)
    {
        if (realLogger != null)
            realLogger.trace (message.toString());
    }

    /**
     * Log an error with trace log level.
     *
     * @param message  The message to convert to a string and log
     * @param ex       The exception to log with the message
     *
     */
    public void trace (Object message, Throwable ex)
    {
        if (realLogger != null)
            realLogger.trace (message.toString(), ex);
    }

    /**
     * Log a message with warn log level.
     *
     * @param message  The message to convert to a string and log
     */
    public void warn (Object message)
    {
        if (realLogger != null)
            realLogger.warn (message.toString());
    }

    /**
     * Log an error with warn log level.
     *
     * @param message  The message to convert to a string and log
     * @param ex       The exception to log with the message
     *
     */
    public void warn (Object message, Throwable ex)
    {
        if (realLogger != null)
            realLogger.warn (message.toString(), ex);
    }

    /**
     * Determine whether debug logging is currently enabled.
     *
     * @return <tt>true</tt> if debug logging is enabled,
     *         <tt>false</tt> otherwise
     */
    public boolean isDebugEnabled()
    {
        return (realLogger == null) ? false
                                    : realLogger.isDebugEnabled();
    }

    /**
     * Determine whether error logging is currently enabled.
     *
     * @return <tt>true</tt> if error logging is enabled,
     *         <tt>false</tt> otherwise
     */
    public boolean isErrorEnabled()
    {
        return (realLogger == null) ? false
                                    : realLogger.isErrorEnabled();
    }

    /**
     * Determine whether fatal logging is currently enabled.
     *
     * @return <tt>true</tt> if fatal logging is enabled,
     *         <tt>false</tt> otherwise
     */
    public boolean isFatalEnabled()
    {
        return (realLogger == null) ? false
                                    : realLogger.isFatalEnabled();
    }

    /**
     * Determine whether info logging is currently enabled.
     *
     * @return <tt>true</tt> if info logging is enabled,
     *         <tt>false</tt> otherwise
     */
    public boolean isInfoEnabled()
    {
        return (realLogger == null) ? false
                                    : realLogger.isInfoEnabled();
    }

    /**
     * Determine whether trace logging is currently enabled.
     *
     * @return <tt>true</tt> if trace logging is enabled,
     *         <tt>false</tt> otherwise
     */
    public boolean isTraceEnabled()
    {
        return (realLogger == null) ? false
                                    : realLogger.isTraceEnabled();
    }

    /**
     * Determine whether warn logging is currently enabled.
     *
     * @return <tt>true</tt> if warn logging is enabled,
     *         <tt>false</tt> otherwise
     */
    public boolean isWarningEnabled()
    {
        return (realLogger == null) ? false
                                    : realLogger.isWarnEnabled();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private static void enableLogger (Logger logger)
        throws UnsupportedOperationException
    {
        synchronized (logger)
        {
            if (logger.realLogger == null)
                logger.realLogger = LogFactory.getLog (logger.className);
        }
    }
}
