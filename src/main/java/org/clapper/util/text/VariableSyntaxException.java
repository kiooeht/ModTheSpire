package org.clapper.util.text;

import org.clapper.util.misc.*;

/**
 * A <tt>VariableSyntaxException</tt> is thrown by the
 * {@link VariableSubstituter} subclasses and
 * {@link VariableDereferencer} subclasses to indicate a syntax error in
 * a variable reference.
 *
 * @see NestedException
 */
public class VariableSyntaxException extends VariableSubstitutionException
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Default constructor, for an exception with no nested exception and
     * no message.
     */
    public VariableSyntaxException()
    {
        super();
    }

    /**
     * Constructs an exception containing another exception, but no message
     * of its own.
     *
     * @param exception  the exception to contain
     */
    public VariableSyntaxException(Throwable exception)
    {
        super (exception);
    }

    /**
     * Constructs an exception containing an error message, but no
     * nested exception.
     *
     * @param message  the message to associate with this exception
     */
    public VariableSyntaxException(String message)
    {
        super (message);
    }

    /**
     * Constructs an exception containing another exception and a message.
     *
     * @param message    the message to associate with this exception
     * @param exception  the exception to contain
     */
    public VariableSyntaxException(String message, Throwable exception)
    {
        super (message, exception);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, and a default message (in case the resource bundle can't be
     * found). Using this constructor is equivalent to calling the
     * {@link #VariableSyntaxException(String,String,String,Object[])}
     * constructor, with a null pointer for the <tt>Object[]</tt> parameter.
     * Calls to {@link #getMessage(Locale)} will attempt to retrieve
     * the top-most message (i.e., the message from this exception, not
     * from nested exceptions) by querying the named resource bundle.
     * Calls to {@link #printStackTrace(PrintWriter,Locale)} will do the same,
     * where applicable. The message is not retrieved until one of those
     * methods is called, because the desired locale is passed into
     * <tt>getMessage()</tt> and <tt>printStackTrace()</tt>, not this
     * constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     *
     * @see #VariableSyntaxException(String,String,String,Object[])
     * @see NestedException#getMessage(Locale)
     */
    public VariableSyntaxException(String bundleName,
                                   String messageKey,
                                   String defaultMsg)
    {
        this(bundleName, messageKey, defaultMsg, null, null);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message format (in case the resource bundle can't be
     * found), and arguments to be incorporated in the message via
     * <tt>java.text.MessageFormat</tt>. Calls to {@link #getMessage(Locale)}
     * will attempt to retrieve the top-most message (i.e., the message from
     * this exception, not from nested exceptions) by querying the named
     * resource bundle. Calls to {@link #printStackTrace(PrintWriter,Locale)}
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
     * @see #VariableSyntaxException(String,String,String,Object[])
     * @see NestedException#getMessage(Locale)
     */
    public VariableSyntaxException(String   bundleName,
                                   String   messageKey,
                                   String   defaultMsg,
                                   Object[] msgParams)
    {
        this(bundleName, messageKey, defaultMsg, msgParams, null);
    }

    /**
     * Constructs an exception containing a resource bundle name, a message
     * key, a default message (in case the resource bundle can't be found), and
     * another exception. Using this constructor is equivalent to calling the
     * {@link #VariableSyntaxException(String,String,String,Object[])}
     * constructor, with a null pointer for the <tt>Object[]</tt>
     * parameter. Calls to {@link #getMessage(Locale)} will attempt to
     * retrieve the top-most message (i.e., the message from this
     * exception, not from nested exceptions) by querying the named
     * resource bundle. Calls to
     * {@link #printStackTrace(PrintWriter,Locale)} will do the same, where
     * applicable. The message is not retrieved until one of those methods
     * is called, because the desired locale is passed into
     * <tt>getMessage()</tt> and <tt>printStackTrace()</tt>, not this
     * constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param exception   the exception to nest
     *
     * @see #VariableSyntaxException(String,String,String,Object[])
     * @see #getMessage(Locale)
     */
    public VariableSyntaxException(String    bundleName,
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
     * Calls to {@link #getMessage(Locale)} will attempt to retrieve the
     * top-most message (i.e., the message from this exception, not from
     * nested exceptions) by querying the named resource bundle. Calls to
     * {@link #printStackTrace(PrintWriter,Locale)} will do the same, where
     * applicable. The message is not retrieved until one of those methods
     * is called, because the desired locale is passed into
     * <tt>getMessage()</tt> and <tt>printStackTrace()</tt>, not this
     * constructor.
     *
     * @param bundleName  resource bundle name
     * @param messageKey  the key to the message to find in the bundle
     * @param defaultMsg  the default message
     * @param msgParams   parameters to the message, if any, or null
     * @param exception   exception to be nested
     *
     * @see #VariableSyntaxException(String,String,String,Object[])
     * @see #getMessage(Locale)
     */
    public VariableSyntaxException(String    bundleName,
                                   String    messageKey,
                                   String    defaultMsg,
                                   Object[]  msgParams,
                                   Throwable exception)
    {
        super(bundleName, messageKey, defaultMsg, msgParams, exception);
    }
}
