package org.clapper.util.cmdline;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.clapper.util.io.WordWrapWriter;
import org.clapper.util.misc.ArrayIterator;
import org.clapper.util.misc.BundleUtil;
import org.clapper.util.text.TextUtil;

/**
 * <p>This class provides a command line parameter and option parser, suitable
 * for use in command line utilities, as well as other contexts where a command
 * line option-style syntax is desired. The {@link CommandLineUtility} class
 * implicitly uses this class to parse its parameters, but there's no reason
 * an application cannot use a <tt>ParameterParser</tt> object directly.</p>
 *
 * <p><tt>ParameterParser</tt> supports both short single-character options
 * (e.g., <tt>-h</tt>) and GNU-style long options (e.g., <tt>--help</tt>).
 * A single option can have both a short and a long variant. Similarly, it's
 * possible to have <i>only</i> a short or a long variant for an option. In
 * addition, this class will parse non-option parameters that follow any
 * options.</p>
 *
 * <p>Note that this class does <b>not</b> parse options the way GNU
 * <i>getopt()</i> (or even traditional <i>getopt()</i>) does. In particular,
 * it does not permit combining multiple single-character options into one
 * command-line parameter. The parsing logic may be extended to support that
 * capability in the future; however, it doesn't do that yet.</p>
 *
 * <p>Using this class is straightforward:</p>
 *
 * <ul>
 *   <li>Create a {@link UsageInfo} object that describes the options and
 *       parameters. This object is also used when generating a usage summary
 *       message. (See the {@link #getUsageMessage getUsageMessage()} method.)
 *   <li>Create a class (often, a local or anonymous one will do) that implements
 *       the {@link ParameterHandler} interface. An instance of this class will
 *       be passed to the parser, to be invoked as the parser encounters options
 *       and parameters.
 *   <li>Instantiate a <tt>ParameterParser</tt> object and call its
 *       {@link #parse parse()} method, passing it three arguments:
 *       <ol>
 *         <li>The array of parameters to be parsed. Options are assumed to
 *             precede non-option parameters.
 *         <li>The {@link UsageInfo} object that describes the expected
 *             options and parameters.
 *         <li>An instance of the class that implements the
 *             {@link ParameterHandler} interface.
 *       </ol>
 * </ul>
 *
 * <p><b>Example:</b></p>
 *
 * <p>This example handles the following options and parameters, for a
 * fictitious copy utility.</p>
 *
 * <table>
 *   <caption>Options, Parameters, and their Meanings</caption>
 *   <tr>
 *     <th>Option/Parameter</th>
 *     <th>Meaning</th>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>--help</tt> or <tt>-h</tt></td>
 *     <td>Get detailed help</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>--force</tt> or <tt>-f</tt></td>
 *     <td>Perform the copy even if the destination file already exists.</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>--time yyyymmddHHMMSS</tt> or<br><tt>-t yymmddHHMMSS</tt></td>
 *     <td>After the copy, set the timestamp of the destination file to the
 *         specified datetime.</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>srcFile</tt></td>
 *     <td>Path to file to be copied.</td>
 *   </tr>
 *
 *   <tr>
 *     <td><tt>destFile</tt></td>
 *     <td>Path to destinationn file.</td>
 *   </tr>
 * </table>
 *
 * <p>Here's some sample code that will parse those options. In practice, of
 * course, you'd usually use the {@link CommandLineUtility} infrastructure,
 * which provides these capabilities and more; however, for the purposes of
 * illustration, we'll stick to <tt>ParameterParser</tt> here. This example
 * omits constructors, a <tt>main()</tt> method, and other methods necessary
 * build a truly runnable utility.</p>
 *
 * <pre>
 * {@code
 * public class ExampleParser implements ParameterHandler
 * {
 *     private Date timestamp= null;
 *     private boolean forceCopy = false;
 *     private boolean showHelpOnly = false;
 *
 *     public void parse(String[] args) throws CommandLineUtilityException
 *     {
 *         UsageInfo usageInfo = new UsageInfo();
 *
 *         usageInfo.addOption('h', "help", "Get detailed help");
 *         usageInfo.addOption('f', "force",
 *                             "Perform copy even if destination file exists.");
 *         usageInfo.addOption('t', "time", "yymmddHHMMSS",
 *                             "Set timestamp of destination file to specified " +
 *                             "datetime");
 *         usageInfo.addParameter("srcFile", "Path to file to be copied", true);
 *         usageInfo.addParameter("destFile", "Path to destination file", true);
 *
 *         ParameterParser paramParser = new ParameterParser();
 *         try
 *         {
 *             paramParser.parse(args, this);
 *         }
 *
 *         catch (Exception ex)
 *         {
 *             System.err.println(paramParser.getUsageMessage(null, 80));
 *         }
 *     }
 *
 *     public void parseOption(char shortOption, String longOption, Iterator&lt;String&gt; it)
 *         throws CommandLineUsageException,
 *                NoSuchElementException
 *     {
 *         switch (shortOption)
 *         {
 *             case 'h':
 *                 doHelp();
 *                 this.showHelpOnly = true;
 *                 break;
 *
 *             case 'f':
 *                 forceCopy = true;
 *                 break;
 *
 *             case 't':
 *                 parseTimestamp(it.next());
 *                 break;
 *
 *             default:
 *                 if (longOption == null)
 *                     throw new CommandLineUsageException("Unknown option: " +
 *                                                         UsageInfo.SHORT_OPTION_PREFIX +
 *                                                         shortOption);
 *                 else
 *                     throw new CommandLineUsageException("Unknown option: " +
 *                                                         UsageInfo.LONG_OPTION_PREFIX +
 *                                                         longOption);
 *                 break;
 *         }
 *     }
 *
 *     public void parsePostOptionParameters(Iterator&lt;String&gt; it)
 *         throws CommandLineUsageException,
 *                NoSuchElementException
 *     {
 *         this.sourceFile = new File(it.next());
 *         this.targetFile = new File(it.next());
 *     }
 * }
 * }
 * </pre>
 *
 * @see ParameterHandler
 * @see CommandLineUtility
 */
public final class ParameterParser
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /**
     * Maximum length of option string (see usage()) that can be concatenated
     * with first line of option's explanation. Strings longer than this are
     * printed on a line by themselves.
     */
    private static final int MAX_OPTION_STRING_LENGTH = 35;

    /*----------------------------------------------------------------------*\
                           Private Data Elements
    \*----------------------------------------------------------------------*/

    private UsageInfo usageInfo = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>ParameterParser</tt> that parses a specific set
     * of options.
     *
     * @param usageInfo  the {@link UsageInfo} object that describes the
     *                   parameters to be parsed
     */
    public ParameterParser(UsageInfo usageInfo)
    {
        this.usageInfo = usageInfo;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Parse a set of command-line parameters. The {@link UsageInfo}
     * object dictates the arguments to be parsed. The {@link ParameterHandler}
     * object handles the encountered parameters.
     *
     * @param params       parameters to parse
     * @param paramHandler handles the arguments
     *
     * @throws CommandLineUsageException on error
     */
    public void parse(String[] params, ParameterHandler paramHandler)
        throws CommandLineUsageException
    {
        ArrayIterator<String> it = new ArrayIterator<String>(params);

        try
        {
            while (it.hasNext())
            {
                String arg = it.next();

                if (! (arg.charAt(0) == UsageInfo.SHORT_OPTION_PREFIX) )
                {
                    // Move iterator back, since we've already advanced
                    // past the last option and retrieved the first
                    // non-option.

                    it.previous();
                    break;
                }

                // First, verify that the option is legal.

                OptionInfo optionInfo = null;

                if (arg.length() == 2)
                    optionInfo = usageInfo.getOptionInfo(arg.charAt(1));
                else
                {
                    if (! arg.startsWith(UsageInfo.LONG_OPTION_PREFIX))
                    {
                        throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.badLongOption",
                             "Option \"{0}\" is not a single-character " +
                             "short option, but it does not start with " +
                             "\"{1}\", as long options must.",
                             new Object[] {arg, UsageInfo.LONG_OPTION_PREFIX});
                    }

                    optionInfo = usageInfo.getOptionInfo(arg.substring(2));
                }

                if (optionInfo == null)
                {
                    throw new CommandLineUsageException
                            (Package.BUNDLE_NAME,
                             "CommandLineUtility.unknownOption",
                             "Unknown option: \"{0}\"",
                             new Object[] {arg});
                }

                // Okay, now handle the options.

                paramHandler.parseOption(optionInfo.shortOption,
                                         optionInfo.longOption,
                                         it);
            }

            paramHandler.parsePostOptionParameters(it);

            // Should be no parameters left now.

            if (it.hasNext())
            {
                throw new CommandLineUsageException
                             (Package.BUNDLE_NAME,
                              "CommandLineUtility.tooManyParams",
                              "Too many parameters.");
            }
        }

        catch (NoSuchElementException ex)
        {
            throw new CommandLineUsageException
                             (Package.BUNDLE_NAME,
                              "CommandLineUtility.missingParams",
                              "Missing command line parameter(s).");
        }

        catch (ArrayIndexOutOfBoundsException ex)
        {
            throw new CommandLineUsageException
                             (Package.BUNDLE_NAME,
                              "CommandLineUtility.missingParams",
                              "Missing command line parameter(s).");
        }
    }

    /**
     * Generate a usage message.
     *
     * @param prefixMsg     prefix (e.g., error) message to use, or null
     *
     * @return the usage string, with embedded newlines
     */
    public String getUsageMessage(String prefixMsg)
    {
        return getUsageMessage(prefixMsg, 0);
    }

    /**
     * Generate a usage message.
     *
     * @param prefixMsg     prefix (e.g., error) message to use, or null
     * @param maxLineLength maximum output line length, or 0 to disable line
     *                      wrapping
     *
     * @return the usage string, with embedded newlines
     */
    public String getUsageMessage(String prefixMsg, int maxLineLength)
    {
        StringWriter     buf = new StringWriter();
        WordWrapWriter   out;
        String[]         strings;
        int              i;
        int              maxParamLength = 0;
        int              maxOptionLength = 0;
        String           s;
        StringBuffer     usageLine = new StringBuffer();
        OptionInfo[]     options;
        OptionInfo       opt;
        Locale           locale = Locale.getDefault();

        if (maxLineLength <= 0)
            maxLineLength = Integer.MAX_VALUE;

        out = new WordWrapWriter(buf, maxLineLength);

        if (prefixMsg != null)
        {
            out.println();
            out.println(prefixMsg);
            out.println();
        }

        // Now, print the summary line.

        String commandName = usageInfo.getCommandName();
        if (commandName != null)
        {
            usageLine.append (commandName);
        }

        else
        {
            usageLine.append("java ");
            usageLine.append(getClass().getName());
        }

        usageLine.append(' ');
        usageLine.append(BundleUtil.getMessage(Package.BUNDLE_NAME,
                                               locale,
                                               "CommandLineUtility.options1",
                                               "[options]"));
        usageLine.append(' ');

        // Add the parameter placeholders. We'll also calculate the maximum
        // parameter name length in this loop, to save an iteration later.

        strings = usageInfo.getParameterNames();
        if (strings.length > 0)
        {
            for (i = 0; i < strings.length; i++)
            {
                usageLine.append(' ');

                boolean optional = true;
                if (usageInfo.parameterIsRequired(strings[i]))
                    optional = false;

                if (optional)
                    usageLine.append('[');
                usageLine.append(strings[i]);
                if (optional)
                    usageLine.append(']');
                maxParamLength = Math.max(maxParamLength,
                                          strings[i].length() + 1);
            }
        }

        if ( (s = usageInfo.getUsagePrologue()) != null)
            out.println(s);

        s = BundleUtil.getMessage(Package.BUNDLE_NAME,
                                  locale,
                                  "CommandLineUtility.usage",
                                  "Usage:");
        out.setPrefix(s + " ");
        out.println(usageLine.toString());
        out.setPrefix (null);
        out.println();

        // Find the largest option name.

        out.println(BundleUtil.getMessage(Package.BUNDLE_NAME,
                                          locale,
                                          "CommandLineUtility.options2",
                                          "OPTIONS:"));
        out.println();

        maxOptionLength = 2;
        options = usageInfo.getOptions();
        for (i = 0; i < options.length; i++)
        {
            opt = options[i];

            // An option with a null explanation is hidden.

            if (opt.explanation == null)
                continue;

            if (opt.longOption != null)
            {
                // Allow room for short option, long option and argument,
                // if any.
                //
                // -x, --long-x <arg>

                int    len = 0;
                String sep = "";
                if (opt.shortOption != UsageInfo.NO_SHORT_OPTION)
                {
                    len = 2;    // -x
                    sep = ", ";
                }

                if (opt.longOption != null)
                {
                    len += (sep.length()
                        + UsageInfo.LONG_OPTION_PREFIX.length()
                        + opt.longOption.length());
                }

                if (opt.argToken != null)
                    len += (opt.argToken.length() + 1);

                maxOptionLength = Math.max(maxOptionLength, len + 1);
            }
        }

        if (maxOptionLength > MAX_OPTION_STRING_LENGTH)
            maxOptionLength = MAX_OPTION_STRING_LENGTH;

        // Now, print the options.

        StringBuffer optString = new StringBuffer();
        for (i = 0; i < options.length; i++)
        {
            opt = options[i];

            // An option with a null explanation is hidden.

            if (opt.explanation == null)
                continue;

            // If there's a short option, print it first. Then do the
            // long one.

            optString.setLength (0);
            String sep = "";

            if (opt.shortOption != UsageInfo.NO_SHORT_OPTION)
            {
                optString.append(UsageInfo.SHORT_OPTION_PREFIX);
                optString.append(opt.shortOption);
                sep = ", ";
            }

            if (opt.longOption != null)
            {
                optString.append(sep);
                optString.append(UsageInfo.LONG_OPTION_PREFIX);
                optString.append(opt.longOption);
            }

            if (opt.argToken != null)
            {
                optString.append(' ');
                optString.append(opt.argToken);
            }

            s = optString.toString();
            if (s.length() > maxOptionLength)
            {
                out.println (s);
                out.setPrefix(padString(" ", maxOptionLength));
            }

            else
            {
                out.setPrefix(padString(optString.toString(),
                                        maxOptionLength));
            }

            out.println(opt.explanation);
            out.setPrefix(null);
        }

        // Print the parameters. We already have size of the the largest
        // parameter name.

        strings = usageInfo.getParameterNames();
        if (strings.length > 0)
        {
            out.println();
            out.println(BundleUtil.getMessage(Package.BUNDLE_NAME,
                                              locale,
                                              "CommandLineUtility.params",
                                              "PARAMETERS:"));
            out.println();

            // Now, print the parameters.

            for (i = 0; i < strings.length; i++)
            {
                out.setPrefix(padString(strings[i], maxParamLength));
                out.println(usageInfo.getParameterExplanation(strings[i]));
                out.setPrefix(null);
            }
        }

        if ( (s = usageInfo.getUsageTrailer()) != null)
        {
            out.println();
            out.println(s);
        }

        out.flush();

        return buf.toString();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private String padString(String s, int toLength)
    {
        return TextUtil.leftJustifyString(s, toLength);
    }
}
