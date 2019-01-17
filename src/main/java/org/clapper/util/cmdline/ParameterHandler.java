package org.clapper.util.cmdline;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Thie interface defines the callback methods used by a
 * {@link ParameterParser} object, when its 
 * {@link ParameterParser#parse parse()} method is called.
 *
 * @see ParameterParser
 * @see CommandLineUtility
 */
public interface ParameterHandler
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Handles a parsed option.
     *
     * @param shortOption  the (character) short option, if any; otherwise,
     *                     the constant {@link UsageInfo#NO_SHORT_OPTION}.
     * @param longOption   the (string) long option, if any; otherwise,
     *                     null.
     * @param it           An <tt>Iterator</tt> from which to retrieve any
     *                     value(s) for the option
     *
     * @throws CommandLineUsageException  on error
     * @throws NoSuchElementException     attempt to iterate past end of args;
     *                                    {@link ParameterParser#parse}
     *                                    automatically handles this exception,
     *                                    so it's safe for implementations of
     *                                    this method not to handle it
     */
    public void parseOption(char             shortOption,
                            String           longOption, 
                            Iterator<String> it)
        throws CommandLineUsageException,
               NoSuchElementException;

    /**
     * Handles all parameters that appear after the end of the options. If there
     * are no such parameters, the implementation of this method should just
     * return without doing anything.
     *
     * @param it  the <tt>Iterator</tt> containing the parameters
     *
     * @throws CommandLineUsageException  on error
     * @throws NoSuchElementException     attempt to iterate past end of args;
     *                                    {@link ParameterParser#parse}
     *                                    automatically handles this exception,
     *                                    so it's safe for implementations of
     *                                    this method not to handle it
     */
    public void parsePostOptionParameters(Iterator<String> it)
        throws CommandLineUsageException,
               NoSuchElementException;
}
