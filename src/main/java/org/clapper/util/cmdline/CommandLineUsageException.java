package org.clapper.util.cmdline;

/**
 * Thrown by a command-line utility to indicate an error on the command line.
 * The {@link CommandLineUtility} class intercepts this exception and displays
 * the command's usage summary.
 */
public class CommandLineUsageException extends CommandLineException
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                               Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor, for an exception with no nested exception and
     * no message.
     */
    public CommandLineUsageException()
    {
        super();
    }

    /**
     * Constructs an exception containing another exception, but no message
     * of its own.
     *
     * @param exception  the exception to contain
     */
    public CommandLineUsageException (Throwable exception)
    {
        super (exception);
    }

    /**
     * Constructs an exception containing an error message, but no
     * nested exception.
     *
     * @param message  the message to associate with this exception
     */
    public CommandLineUsageException (String message)
    {
        super (message);
    }

    /**
     * Constructs an exception containing another exception and a message.
     *
     * @param message    the message to associate with this exception
     * @param exception  the exception to contain
     */
    public CommandLineUsageException (String message, Throwable exception)
    {
        super (message, exception);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #CommandLineUsageException(String,String,String,Object[])}
     * constructor, with a null pointer for the <tt>Object[]</tt> parameter.
     * Calls to
     * {@link org.clapper.util.misc.NestedException#getMessage(java.util.Locale)}
     * will attempt to retrieve the top-most message (i.e., the message
     * from this exception, not from nested exceptions) by querying the
     * named resource bundle. Calls to
     * {@link org.clapper.util.misc.NestedException#printStackTrace(PrintWriter,java.util.Locale)}
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     *
     * @see #CommandLineUsageException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public CommandLineUsageException (String bundleName,
                                      String messageKey,
                                      String defaultMsg)
    {
        super (bundleName, messageKey, defaultMsg);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #CommandLineUsageException(String,String,String,Object[],Throwable)}
     * constructor, with a null pointer for the <tt>Throwable</tt> parameter.
     * Calls to
     * {@link org.clapper.util.misc.NestedException#getMessage(java.util.Locale)}
     * will attempt to retrieve the top-most message (i.e., the message
     * from this exception, not from nested exceptions) by querying the
     * named resource bundle. Calls to
     * {@link org.clapper.util.misc.NestedException#printStackTrace(PrintWriter,java.util.Locale)}
     * will do the same, where applicable. The message is not retrieved
     * until one of those methods is called, because the desired locale is
     * passed into <tt>getMessage()</tt> and <tt>printStackTrace()</tt>,
     * not this constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     *
     * @see #CommandLineUsageException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public CommandLineUsageException (String   bundleName,
                                      String   messageKey,
                                      String   defaultMsg,
                                      Object[] msgParams)
    {
        super (bundleName, messageKey, defaultMsg, msgParams);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message (in case the resource bundle can't be found), and
     * another exception. Using this constructor is equivalent to calling the
     * {@link #CommandLineUsageException(String,String,String,Object[],Throwable)}
     * constructor, with a null pointer for the <tt>Object[]</tt>
     * parameter. Calls to {@link #getMessage(java.util.Locale)} will attempt
     * to retrieve the top-most message (i.e., the message from this
     * exception, not from nested exceptions) by querying the named
     * resource bundle. Calls to
     * {@link org.clapper.util.misc.NestedException#printStackTrace(PrintWriter,java.util.Locale)} will do the same,
     * where applicable. The message is not retrieved until one of those
     * methods is called, because the desired locale is passed into
     * <tt>getMessage()</tt> and <tt>printStackTrace()</tt>, not this
     * constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param exception   the exception to nest
     *
     * @see #CommandLineUsageException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public CommandLineUsageException (String    bundleName,
                                      String    messageKey,
                                      String    defaultMsg,
                                      Throwable exception)
    {
        this (bundleName, messageKey, defaultMsg, null, exception);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message format (in case the resource bundle can't be
     * found), arguments to be incorporated in the message via
     * <tt>java.text.MessageFormat</tt>, and another exception.
     * Calls to
     * {@link org.clapper.util.misc.NestedException#getMessage(java.util.Locale)} will attempt to retrieve
     * the top-most message (i.e., the message from this exception, not from
     * nested exceptions) by querying the named resource bundle. Calls to
     * {@link org.clapper.util.misc.NestedException#printStackTrace(PrintWriter,java.util.Locale)}
     * will do the same,
     * where applicable. The message is not retrieved until one of those
     * methods is called, because the desired locale is passed into
     * <tt>getMessage()</tt> and <tt>printStackTrace()</tt>, not this
     * constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     * @param exception   exception to be nested
     *
     * @see #CommandLineUsageException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public CommandLineUsageException (String    bundleName,
                                      String    messageKey,
                                      String    defaultMsg,
                                      Object[]  msgParams,
                                      Throwable exception)
    {
        super (bundleName, messageKey, defaultMsg, msgParams, exception);
    }
}
