package org.clapper.util.cmdline;

import org.clapper.util.logging.Logger;
import org.clapper.util.misc.BundleUtil;

import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * <p><tt>CommandLineUtility</tt> is an abstract base class for
 * command-line utilities. It provides:</p>
 *
 * <ul>
 *   <li>Parameter-parsing logic, with call-outs for custom parameters
 *   <li>Built-in support for a --logging parameter, which enables logging
 *       via the <tt>java.util.logging</tt> API, by calling
 *       {@link org.clapper.util.logging.Logger#enableLogging()}
 *   <li>Automatic generation of a usage message, with a call-out that
 *       permits subclasses to add subclass-specific usage information.
 *   <li>Automatic reporting of exceptions
 * </ul>
 *
 * <p>To use this class, subclass it, and have the subclass's <tt>main()</tt>
 * method instantiate the subclass, and then call the resulting object's
 * {@link #execute(String[]) execute()} method. The <tt>execute()</tt> method
 * (which resides in this base class) will:</p>
 *
 * <ul>
 *   <li>parse the parameters
 *   <li>call the {@link #runCommand()} method, which must be provided by
 *       the subclass, to initiate processing
 * </ul>
 *
 * <p>Note that this class does <b>not</b> parse options the way GNU
 * <i>getopt()</i> (or even traditional <i>getopt()</i>) does. In particular,
 * it does not permit combining multiple single-character options into one
 * command-line parameter. <tt>CommandLineUtility</tt> may be extended to
 * support that capability in the future; however, it doesn't do that yet.</p>
 *
 * <p>Here's a sample subclass. It takes the usual <tt>--logging</tt>
 * parameter, plus a <tt>-v</tt> (verbose) flag, a numeric count
 * (<tt>-n</tt>) and a file name. (Exactly what it does with those
 * parameters is left as an exercise for the reader.)</p>
 *
 * <blockquote>
 * <pre>
 * {@code
 * public class Foo extends CommandLineUtility
 * {
 *     private boolean verbose  = false;
 *     private int     count    = 1;
 *     private String  filename = null;
 *
 *     public static void main (String[] args)
 *     {
 *         try
 *         {
 *             Foo foo = new Foo();
 *             foo.execute (args);
 *         }
 *
 *         catch (CommandLineUsageException ex)
 *         {
 *             // Already reported
 *
 *             System.exit (1);
 *         }
 *
 *         catch (CommandLineException ex)
 *         {
 *             System.err.println (ex.getMessage());
 *             System.exit (1);
 *         }
 *
 *         System.exit (0);
 *     }
 *
 *     private Foo()
 *     {
 *         super();
 *     }
 *
 *     protected void runCommand() throws CommandLineUtilityException
 *     {
 *         ...
 *     }
 *
 *     protected void parseCustomOption (char shortOption,
 *                                       String longOption,
 *                                       Iterator&lt;String&gt; it)
 *         throws CommandLineUsageException,
 *                NoSuchElementException
 *     {
 *         if (longOption.equals ("verbose") || (shortOption == 'v'))
 *             verbose = true;
 *
 *         else if (longOption.equals ("count") || (shortOption == 'n'))
 *             count = parseIntParameter ((String) it.next());
 *
 *         else
 *             throw new CommandLineUsageException ("Unknown option: " + option);
 *     }
 *
 *     protected void processPostOptionCommandLine (Iterator&lt;String&gt; it)
 *         throws CommandLineUsageException,
 *                NoSuchElementException
 *     {
 *         filename = it.next();
 *     }
 *
 *     protected void getCustomUsageInfo (UsageInfo info)
 *     {
 *         info.addOption ('v', "verbose", null, "Enable verbose messages")
 *         info.addOption ('n', "count", "total", "Read specified file <total> times. Defaults to 1.");
 *         info.addParameter ("filename", "File to process.", false);
 *     }
 * }
 * }
 * </pre>
 * </blockquote>
 *
 * @see ParameterParser
 */
public abstract class CommandLineUtility
{
    /*----------------------------------------------------------------------*\
                             Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                           Private Data Elements
    \*----------------------------------------------------------------------*/

    private ParameterParser paramParser = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Constructor. Initializes this base class.
     */
    protected CommandLineUtility()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called to initiate execution of the command line utility. This
     * method
     *
     * <ul>
     *   <li>parse the parameters
     *   <li>call the {@link #runCommand()} method, which must be provided by
     *       the subclass, to initiate processing.
     * </ul>
     *
     * @param args  The command-line parameters
     *
     * @throws CommandLineException command failed
     */
    public final void execute (String[] args)
        throws CommandLineException
    {
        try
        {
            paramParser = new ParameterParser(getUsageInfo());
            parseParams (args);
            runCommand();
        }

        catch (CommandLineUsageException ex)
        {
            usage (ex.getMessage());
            throw ex;
        }

        catch (CommandLineException ex)
        {
            throw ex;
        }

        catch (Exception ex)
        {
            throw new CommandLineException (ex);
        }
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Called by <tt>parseParams()</tt> to handle any option it doesn't
     * recognize. If the option takes any parameters, the overridden
     * method must extract the parameter by advancing the supplied
     * <tt>Iterator</tt> (which returns <tt>String</tt> objects). This
     * default method simply throws an exception.
     *
     * @param shortOption  the short option character, or
     *                     {@link UsageInfo#NO_SHORT_OPTION} if there isn't
     *                     one (i.e., if this is a long-only option).
     * @param longOption   the long option string, without any leading
     *                     "-" characters, or null if this is a short-only
     *                     option
     * @param it           the <tt>Iterator</tt> for the remainder of the
     *                     command line, for extracting parameters.
     *
     * @throws CommandLineUsageException  on error
     * @throws NoSuchElementException     overran the iterator (i.e., missing
     *                                    parameter)
     */
    protected void parseCustomOption (char             shortOption,
                                      String           longOption,
                                      Iterator<String> it)
        throws CommandLineUsageException,
               NoSuchElementException
    {
        throw new CommandLineUsageException
            (Package.BUNDLE_NAME, "CommandLineUtility.parseCustomOption",
             "(BUG) custom option found, but class {0} provides no " +
             "parseCustomOption method.",
             new Object[] {this.getClass().getName()});
    }

    /**
     * <p>Called by <tt>parseParams()</tt> once option parsing is complete,
     * this method must handle any additional parameters on the command
     * line. It's not necessary for the method to ensure that the iterator
     * has the right number of strings left in it. If you attempt to pull
     * too many parameters from the iterator, it'll throw a
     * <tt>NoSuchElementException</tt>, which <tt>parseParams()</tt> traps
     * and converts into a suitable error message. Similarly, if there are
     * any parameters left in the iterator when this method returns,
     * <tt>parseParams()</tt> throws an exception indicating that there are
     * too many parameters on the command line.</p>
     *
     * <p>This method is called unconditionally, even if there are no
     * parameters left on the command line, so it's a useful place to do
     * post-option consistency checks, as well.</p>
     *
     * @param it   the <tt>Iterator</tt> for the remainder of the
     *             command line
     *
     * @throws CommandLineUsageException  on error
     * @throws NoSuchElementException     attempt to iterate past end of args;
     *                                    <tt>parseParams()</tt> automatically
     *                                    handles this exception, so it's
     *                                    safe for subclass implementations of
     *                                    this method not to handle it
     */
    protected void processPostOptionCommandLine (Iterator<String> it)
        throws CommandLineUsageException,
               NoSuchElementException
    {
        if (it.hasNext())
        {
            throw new CommandLineUsageException
                             (Package.BUNDLE_NAME,
                              "CommandLineUtility.extraParams",
                              "Extra command line parameter(s).");
        }
    }

    /**
     * Called by <tt>parseParams()</tt> to get the custom command-line
     * options and parameters handled by the subclass. This list is used
     * solely to build a usage message. The overridden method must fill the
     * supplied <tt>UsageInfo</tt> object:
     *
     * <ul>
     *   <li> Each parameter must be added to the object, via the
     *        {@link UsageInfo#addParameter UsageInfo.addParameter()} method.
     *
     *   <li> Each option must be added to the object, via the
     *        {@link UsageInfo#addOption UsageInfo.addOption()} method.
     * </ul>
     *
     * That information will be combined with the common options supported
     * by the base class, and used to build a usage message.
     *
     * @param info   The <tt>UsageInfo</tt> object to fill.
     *
     * @see UsageInfo#addOption
     * @see UsageInfo#addParameter
     */
    protected void getCustomUsageInfo (UsageInfo info)
    {
        // Default: does nothing
    }

    /**
     * Actually runs the command. All subclasses are required to provide this
     * method.
     *
     * @throws CommandLineException  on error
     */
    protected abstract void runCommand() throws CommandLineException;

    /**
     * Convenience method that parses an integer value, throwing a
     * <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>,
     * on error.
     *
     * @param value the string value to parse
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseIntParameter(String,int,int)
     * @see #parseIntOptionArgument(char,String,String)
     * @see #parseDoubleParameter(String)
     * @see #parseFloatParameter(String)
     */
    protected int parseIntParameter (String value)
        throws CommandLineUsageException
    {
        try
        {
            return Integer.parseInt (value);
        }

        catch (NumberFormatException ex)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.badIntegerParam",
                             "Bad integer command line parameter \"{0}\"",
                             new Object[] {value});
        }
    }

    /**
     * Convenience method that parses an integer value, throwing a
     * <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error. This version of the method
     * also does range checking.
     *
     * @param value the string value to parse
     * @param min   the minimum legal value for the result
     * @param max   the maximum legal value for the result
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseIntParameter(String)
     * @see #parseIntOptionArgument(char,String,String,int,int)
     * @see #parseDoubleParameter(String,double,double)
     * @see #parseFloatParameter(String,float,float)
     */
    protected int parseIntParameter (String value, int min, int max)
        throws CommandLineUsageException
    {
        int result = parseIntParameter (value);

        if (result < min)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericParamTooSmall",
                             "Numeric parameter {0} is less than the " +
                             "minimum legal value of {1}",
                             new Object[]
                             {
                                 value,
                                 String.valueOf (min)
                             });
        }

        if (result > max)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericParamTooLarge",
                             "Numeric parameter {0} is greater than the " +
                             "maximum legal value of {1}",
                             new Object[]
                             {
                                 value,
                                 String.valueOf (max)
                             });
        }

        return result;
    }

    /**
     * Convenience method that parses an integer option argument, throwing
     * a <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error.
     *
     * @param shortOption short option, as passed to {@link #parseCustomOption}
     * @param longOption  long option, as passed to {@link #parseCustomOption}
     * @param value       the string value to parse
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseIntOptionArgument(char,String,String,int,int)
     * @see #parseDoubleOptionArgument(char,String,String)
     * @see #parseFloatOptionArgument(char,String,String)
     */
    protected int parseIntOptionArgument (char   shortOption,
                                          String longOption,
                                          String value)
        throws CommandLineUsageException
    {
        try
        {
            return Integer.parseInt (value);
        }

        catch (NumberFormatException ex)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.badIntegerOption",
                             "Bad integer command line parameter \"{0}\" " +
                             "for option {1}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption)
                             });
        }
    }

    /**
     * Convenience method that parses an integer option argument, throwing
     * a <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error. This version of the method
     * also does range checking.
     *
     * @param shortOption short option, as passed to {@link #parseCustomOption}
     * @param longOption  long option, as passed to {@link #parseCustomOption}
     * @param value       the string value to parse
     * @param min         the minimum legal value for the result
     * @param max         the maximum legal value for the result
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseIntOptionArgument(char,String,String,int,int)
     * @see #parseDoubleOptionArgument(char,String,String)
     * @see #parseFloatOptionArgument(char,String,String)
     */
    protected int parseIntOptionArgument (char   shortOption,
                                          String longOption,
                                          String value,
                                          int    min,
                                          int    max)
        throws CommandLineUsageException
    {
        int result = parseIntOptionArgument (shortOption, longOption, value);

        if (result < min)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericOptionParamTooSmall",
                             "Numeric parameter {0} for option {1} is less " +
                             "than the minimum legal value of {2}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption),
                                 String.valueOf (min)
                             });
        }

        if (result > max)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericOptionParamTooLarge",
                             "Numeric parameter {0} for option {1} is " +
                             "greater than the maximum legal value of {2}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption),
                                 String.valueOf (max)
                             });
        }

        return result;
    }

    /**
     * Convenience method that parses a floating point value, throwing a
     * <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error.
     *
     * @param value the string value to parse
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseFloatParameter(String,float,float)
     * @see #parseFloatOptionArgument(char,String,String)
     * @see #parseDoubleParameter(String)
     * @see #parseIntParameter(String)
     */
    protected float parseFloatParameter (String value)
        throws CommandLineUsageException
    {
        try
        {
            return Float.parseFloat (value);
        }

        catch (NumberFormatException ex)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.badFloatParam",
                             "Bad floating point command line parameter " +
                             "\"{0}\"",
                             new Object[] {value});
        }
    }

    /**
     * Convenience method that parses a floating point value, throwing a
     * <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error. This version of the method
     * also does range checking.
     *
     * @param value the string value to parse
     * @param min   the minimum legal value for the result
     * @param max   the maximum legal value for the result
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseFloatParameter(String)
     * @see #parseFloatOptionArgument(char,String,String,float,float)
     * @see #parseDoubleParameter(String,double,double)
     * @see #parseIntParameter(String,int,int)
     */
    protected float parseFloatParameter (String value, float min, float max)
        throws CommandLineUsageException
    {
        float result = parseFloatParameter (value);

        if (result < min)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericParamTooSmall",
                             "Numeric parameter {0} is less than the " +
                             "minimum legal value of {1}",
                             new Object[]
                             {
                                 value,
                                 String.valueOf (min)
                             });
        }

        if (result > max)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericParamTooLarge",
                             "Numeric parameter {0} is greater than the " +
                             "maximum legal value of {1}",
                             new Object[]
                             {
                                 value,
                                 String.valueOf (max)
                             });
        }

        return result;
    }

    /**
     * Convenience method that parses a float point option argument,
     * throwing a <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error.
     *
     * @param shortOption short option, as passed to {@link #parseCustomOption}
     * @param longOption  long option, as passed to {@link #parseCustomOption}
     * @param value       the string value to parse
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseFloatOptionArgument(char,String,String,float,float)
     * @see #parseDoubleOptionArgument(char,String,String)
     * @see #parseIntOptionArgument(char,String,String)
     */
    protected float parseFloatOptionArgument (char   shortOption,
                                              String longOption,
                                              String value)
        throws CommandLineUsageException
    {
        try
        {
            return Integer.parseInt (value);
        }

        catch (NumberFormatException ex)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.badFloatParam",
                             "Bad floating point command line parameter " +
                             "\"{0}\" for option {1}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption)
                             });
        }
    }

    /**
     * Convenience method that parses a floating point option argument,
     * throwing a <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error. This version of the method
     * also does range checking.
     *
     * @param shortOption short option, as passed to {@link #parseCustomOption}
     * @param longOption  long option, as passed to {@link #parseCustomOption}
     * @param value       the string value to parse
     * @param min         the minimum legal value for the result
     * @param max         the maximum legal value for the result
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseFloatOptionArgument(char,String,String,float,float)
     * @see #parseDoubleOptionArgument(char,String,String)
     * @see #parseIntOptionArgument(char,String,String)
     */
    protected float parseFloatOptionArgument (char   shortOption,
                                              String longOption,
                                              String value,
                                              float  min,
                                              float  max)
        throws CommandLineUsageException
    {
        float result = parseFloatOptionArgument (shortOption,
                                                 longOption,
                                                 value);

        if (result < min)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericOptionParamTooSmall",
                             "Numeric parameter {0} for option {1} is less " +
                             "than the minimum legal value of {2}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption),
                                 String.valueOf (min)
                             });
        }

        if (result > max)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericOptionParamTooLarge",
                             "Numeric parameter {0} for option {1} is " +
                             "greater than the maximum legal value of {2}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption),
                                 String.valueOf (max)
                             });
        }

        return result;
    }

    /**
     * Convenience method that parses a double value, throwing a
     * <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error.
     *
     * @param value the string value to parse
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseDoubleParameter(String,double,double)
     * @see #parseDoubleOptionArgument(char,String,String)
     * @see #parseFloatParameter(String)
     * @see #parseIntParameter(String)
     */
    protected double parseDoubleParameter (String value)
        throws CommandLineUsageException
    {
        try
        {
            return Double.parseDouble (value);
        }

        catch (NumberFormatException ex)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.badDoubleParam",
                             "Bad double floating point command line " +
                             "parameter \"{0}\"",
                             new Object[] {value});
        }
    }

    /**
     * Convenience method that parses a double value, throwing a
     * <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error. This version of the method
     * also does range checking.
     *
     * @param value the string value to parse
     * @param min   the minimum legal value for the result
     * @param max   the maximum legal value for the result
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseDoubleParameter(String)
     * @see #parseDoubleOptionArgument(char,String,String,double,double)
     * @see #parseFloatParameter(String,float,float)
     * @see #parseIntParameter(String,int,int)
     */
    protected double parseDoubleParameter (String value,
                                           double  min,
                                           double  max)
        throws CommandLineUsageException
    {
        double result = parseDoubleParameter (value);

        if (result < min)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericParamTooSmall",
                             "Numeric parameter {0} is less than the " +
                             "minimum legal value of {1}",
                             new Object[]
                             {
                                 value,
                                 String.valueOf (min)
                             });
        }

        if (result > max)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericParamTooLarge",
                             "Numeric parameter {0} is greater than the " +
                             "maximum legal value of {1}",
                             new Object[]
                             {
                                 value,
                                 String.valueOf (max)
                             });
        }

        return result;
    }

    /**
     * Convenience method that parses a double option argument, throwing a
     * <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error.
     *
     * @param shortOption short option, as passed to {@link #parseCustomOption}
     * @param longOption  long option, as passed to {@link #parseCustomOption}
     * @param value       the string value to parse
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseDoubleOptionArgument(char,String,String,double,double)
     * @see #parseDoubleOptionArgument(char,String,String)
     * @see #parseIntOptionArgument(char,String,String)
     */
    protected double parseDoubleOptionArgument (char   shortOption,
                                                String longOption,
                                                String value)
        throws CommandLineUsageException
    {
        try
        {
            return Double.parseDouble (value);
        }

        catch (NumberFormatException ex)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.badDoubleOption",
                             "Bad double command line parameter " +
                             "\"{0}\" for option {1}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption)
                             });
        }
    }

    /**
     * Convenience method that parses a double floating point option
     * argument, throwing a <tt>CommandLineUsageException</tt>, not a
     * <tt>NumberFormatException</tt>, on error. This version of the method
     * also does range checking.
     *
     * @param shortOption short option, as passed to {@link #parseCustomOption}
     * @param longOption  long option, as passed to {@link #parseCustomOption}
     * @param value       the string value to parse
     * @param min         the minimum legal value for the result
     * @param max         the maximum legal value for the result
     *
     * @return the numeric value
     *
     * @throws CommandLineUsageException bad numeric value
     *
     * @see #parseDoubleOptionArgument(char,String,String,double,double)
     * @see #parseDoubleOptionArgument(char,String,String)
     * @see #parseIntOptionArgument(char,String,String)
     */
    protected double parseDoubleOptionArgument (char   shortOption,
                                                String longOption,
                                                String value,
                                                double  min,
                                                double  max)
        throws CommandLineUsageException
    {
        double result = parseDoubleOptionArgument (shortOption,
                                                   longOption,
                                                   value);

        if (result < min)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericOptionParamTooSmall",
                             "Numeric parameter {0} for option {1} is less " +
                             "than the minimum legal value of {2}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption),
                                 String.valueOf (min)
                             });
        }

        if (result > max)
        {
            throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.numericOptionParamTooSmall",
                             "Numeric parameter {0} for option {1} is " +
                             "greater than the maximum legal value of {2}",
                             new Object[]
                             {
                                 value,
                                 getOptionStringForError (shortOption,
                                                          longOption),
                                 String.valueOf (max)
                             });
        }

        return result;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse the command-line parameters. This method parses the common
     * options; any other option is passed to the
     * <tt>parseCustomOption()</tt> method, which should throw an exception
     * if the option isn't recognized. When the options have all been
     * satisfied, this method then invokes
     * <tt>processPostOptionCommandLine()</tt>.
     *
     * @param args  the command line parameters
     *
     * @throws CommandLineUsageException command line error
     *
     * @see #processPostOptionCommandLine
     * @see #parseCustomOption
     */
    private void parseParams (String args[])
        throws CommandLineUsageException
    {
        ParameterHandler handler = new ParameterHandler()
        {
            public void parseOption(char             shortOption,
                                    String           longOption,
                                    Iterator<String> it)
                throws CommandLineUsageException,
                       NoSuchElementException
            {
                if ((longOption != null) &&
                    (longOption.equals ("logging")))
                {
                    Logger.enableLogging();
                }

                else
                {
                    parseCustomOption (shortOption, longOption, it);
                }
            }

            public void parsePostOptionParameters(Iterator<String> it)
                throws CommandLineUsageException,
                       NoSuchElementException
            {
                processPostOptionCommandLine(it);
            }
        };

        paramParser.parse(args, handler);
    }

    /**
     * Print a usage message.
     *
     * @param prefixMsg  a prefix message to display before dumping the
     *                   usage, or null
     */
    private void usage (String prefixMsg)
    {
        System.err.println(paramParser.getUsageMessage(prefixMsg, 78));
    }

    private UsageInfo getUsageInfo()
    {
        UsageInfo info = new UsageInfo();

        getCustomUsageInfo (info);

        info.addOption (UsageInfo.NO_SHORT_OPTION,
                        "logging",
                        BundleUtil.getMessage (Package.BUNDLE_NAME,
                                               Locale.getDefault(),
                                               "CommandLineUtility.logging",
                                               "Enable logging via the " +
                                               "java.util.logging API."));

        return info;
    }

    private String getOptionStringForError (char   shortOption,
                                            String longOption)
    {
        StringBuffer buf = new StringBuffer();
        boolean      paren = false;

        if (longOption != null)
        {
            buf.append ("--");
            buf.append (longOption);
            paren = true;
        }

        if (shortOption != UsageInfo.NO_SHORT_OPTION)
        {
            if (paren)
                buf.append (" (");

            buf.append ('-');
            buf.append (shortOption);

            if (paren)
                buf.append (')');
        }

        assert (buf.length() > 0);
        return buf.toString();
    }
}
