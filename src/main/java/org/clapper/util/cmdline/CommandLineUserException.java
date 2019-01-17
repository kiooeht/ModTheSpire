package org.clapper.util.cmdline;

/**
 * <p>Thrown to indicate a user error <i>other than</i> a usage exception.
 * A {@link CommandLineUsageException}, if thrown, will cause the
 * {@link CommandLineUtility} class to print the command line usage summary.
 * Sometimes, you want to handle other user errors slightly differently.
 * For example, it's fairly common for a {@link CommandLineUtility} subclass's
 * <tt>main()</tt> method to handle exceptions like this:</p>
 *
 * <ul>
 *  <li>Don't print anything for a {@link CommandLineUsageException}, since
 *      the superclass handles that. Don't print a stack trace, either; a
 *      stack trace doesn't go well with a usage summary.
 *  <li>Don't print a stack trace for a <tt>CommandLineUserException</tt>;
 *      stack traces are for programmers, and this is some kind of user 
 *      error other than a command-line error.
 *  <li>For other errors, print (or log) a stack trace.
 * </ul>
 */
public class CommandLineUserException extends CommandLineException
{
    /*----------------------------------------------------------------------*\
                               Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/
    /**
     * Default constructor, for an exception with no nested exception and
     * no message.
     */
    public CommandLineUserException()
    {
        super();
    }

    /**
     * Constructs an exception containing another exception, but no message
     * of its own.
     *
     * @param exception  the exception to contain
     */
    public CommandLineUserException(Throwable exception)
    {
        super (exception);
    }

    /**
     * Constructs an exception containing an error message, but no
     * nested exception.
     *
     * @param message  the message to associate with this exception
     */
    public CommandLineUserException(String message)
    {
        super (message);
    }

    /**
     * Constructs an exception containing another exception and a message.
     *
     * @param message    the message to associate with this exception
     * @param exception  the exception to contain
     */
    public CommandLineUserException(String message, Throwable exception)
    {
        super (message, exception);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #CommandLineUserException(String,String,String,Object[])}
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
     * @see #CommandLineUserException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public CommandLineUserException (String bundleName,
                                     String messageKey,
                                     String defaultMsg)
    {
        super(bundleName, messageKey, defaultMsg);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #CommandLineUserException(String,String,String,Object[],Throwable)}
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
     * @see #CommandLineUserException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public CommandLineUserException (String   bundleName,
                                     String   messageKey,
                                     String   defaultMsg,
                                     Object[] msgParams)
    {
        super(bundleName, messageKey, defaultMsg, msgParams);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message (in case the resource bundle can't be found), and
     * another exception. Using this constructor is equivalent to calling the
     * {@link #CommandLineUserException(String,String,String,Object[],Throwable)}
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
     * @see #CommandLineUserException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public CommandLineUserException(String    bundleName,
                                    String    messageKey,
                                    String    defaultMsg,
                                    Throwable exception)
    {
        this(bundleName, messageKey, defaultMsg, null, exception);
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
     * @see #CommandLineUserException(String,String,String,Object[])
     * @see org.clapper.util.misc.NestedException#getMessage(java.util.Locale)
     */
    public CommandLineUserException(String    bundleName,
                                    String    messageKey,
                                    String    defaultMsg,
                                    Object[]  msgParams,
                                    Throwable exception)
    {
        super(bundleName, messageKey, defaultMsg, msgParams, exception);
    }
}
