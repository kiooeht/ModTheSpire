package org.clapper.util.cmdline;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

/**
 * An instance of this class is used to specify usage information for a
 * command-line utility. See
 * {@link CommandLineUtility#getCustomUsageInfo(UsageInfo)}
 * for more information how to use this class.
 *
 * @see CommandLineUtility
 * @see CommandLineUtility#getCustomUsageInfo(UsageInfo)
 */
public final class UsageInfo
{
    /*----------------------------------------------------------------------*\
                                 Constants
    \*----------------------------------------------------------------------*/

    /**
     * Constant to use for a short option, to indicate that there is no
     * short option.
     */
    public static final char NO_SHORT_OPTION = '\u0000';

    /**
     * The option prefix strings used at the command line.
     */
    public static final char SHORT_OPTION_PREFIX = '-';
    public static final String LONG_OPTION_PREFIX = "--";

    /*----------------------------------------------------------------------*\
                           Private Data Elements
    \*----------------------------------------------------------------------*/

    private Map<Character, OptionInfo> shortOptionMap =
                                        new HashMap<Character, OptionInfo>();
    private Map<String, OptionInfo> longOptionMap =
                                        new HashMap<String, OptionInfo>();
    private Set<OptionInfo> allOptions =
                            new TreeSet<OptionInfo> (new OptionComparator());
    private Map<String, String> paramMap = new HashMap<String, String>();
    private Set<String> requiredParams = new HashSet<String>();
    private List<String> paramNames = new ArrayList<String>();
    private String  usagePrologue = null;
    private String  usageTrailer = null;
    private String  commandName = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>UsageInfo()</tt> object.
     */
    public UsageInfo()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the command name to be used in the usage output. If not specified,
     * <tt>java classname</tt> is used.
     *
     * @return the command name, or null if not set
     *
     * @see #setCommandName
     */
    public String getCommandName()
    {
        return this.commandName;
    }

    /**
     * Set the command name to be used in the usage output. If not specified,
     * <tt>java classname</tt> is used.
     *
     * @param commandName the command name
     *
     * @see #getCommandName
     */
    public void setCommandName (String commandName)
    {
        this.commandName = commandName;
    }

    /**
     * Add an option and its explanation to the usage information.
     * This method is shorthand for:
     *
     * <blockquote><pre>{@link #addOption(char,String,String,String) addOption(}(shortOption, longOption, null, explanation)</pre></blockquote>
     *
     * That is, it's useful for options that take no parameters.
     *
     * @param shortOption  The single-character short option (e.g., 'a'
     *                     for "-a"), or {@link #NO_SHORT_OPTION} to indicate
     *                     that there is no short option.
     * @param longOption   The corresponding long option, if any, or null.
     *                     The option should be specified without any leading
     *                     "-" character (e.g., "logging", not "--logging").
     * @param explanation  A one-line explanation for the option. The line
     *                     can be as long as you want, and can contain multiple
     *                     sentences, but it must not contain a newline.
     *                     It will be automatically broken up into multiple
     *                     lines as necessary. If the explanation is null,
     *                     then the option is "hidden" (i.e., not displayed
     *                     in the usage message). This is useful, for instance,
     *                     when you've deprecated an option but are retaining
     *                     it for backward compatibility.
     */
    public void addOption (char   shortOption,
                           String longOption,
                           String explanation)
    {
        addOption (shortOption, longOption, null, explanation);
    }

    /**
     * Add an option and its explanation to the usage information.
     * Examples:
     *
     * <blockquote><pre>
     * usageInfo.addOption ('n', "total-images", "num",
     *                      "Total number of images to generate");
     * usageInfo.addOption ('v', "verbose", null,
     *                      "Enable verbose mode")
     * </pre></blockquote>
     *
     * @param shortOption  The single-character short option (e.g., 'a'
     *                     for "-a"), or {@link #NO_SHORT_OPTION} to indicate
     *                     that there is no short option.
     * @param longOption   The corresponding long option, if any, or null.
     *                     The option should be specified without any leading
     *                     "-" character (e.g., "logging", not "--logging").
     * @param argToken     A token to represent the option's parameter, if any,
     *                     in the generated usage message. null if the option
     *                     takes no parameters.
     * @param explanation  A one-line explanation for the option. The line
     *                     can be as long as you want, and can contain multiple
     *                     sentences, but it must not contain a newline.
     *                     It will be automatically broken up into multiple
     *                     lines as necessary. If the explanation is null,
     *                     then the option is "hidden" (i.e., not displayed
     *                     in the usage message). This is useful, for instance,
     *                     when you've deprecated an option but are retaining
     *                     it for backward compatibility.
     */
    public void addOption (char   shortOption,
                           String longOption,
                           String argToken,
                           String explanation)
    {
        if ((longOption != null) && (longOption.charAt(0) == '-'))
        {
            throw new IllegalArgumentException
                ("(BUG) Long option \"" +
                 longOption +
                 "\", registered via UsageInfo.addOption(), starts " +
                 "with \"-\".");
        }

        if ((shortOption == NO_SHORT_OPTION) && (longOption == null))
        {
            throw new IllegalArgumentException
                ("(BUG) shortOption parameter is NO_SHORT_OPTION, and " +
                 "longOption parameter is null.");
        }

        OptionInfo optionInfo = new OptionInfo (shortOption,
                                                longOption,
                                                argToken,
                                                explanation);

        if (shortOption != NO_SHORT_OPTION)
            shortOptionMap.put (shortOption, optionInfo);

        if (longOption != null)
            longOptionMap.put (longOption, optionInfo);

        allOptions.add (optionInfo);
    }

    /**
     * Add a positional parameter (i.e., one that follows the options) and
     * its explanation to the usage information at the end of the list of
     * positional parameters.
     *
     * @param param        The parameter placeholder string
     * @param explanation  A one-line explanation for the parameter. The line
     *                     can be as long as you want, and can contain multiple
     *                     sentences, but it must not contain a newline.
     *                     It will be automatically broken up into multiple
     *                     lines as necessary.
     * @param required     <tt>true</tt> if the parameter is required,
     *                     <tt>false</tt> if the parameter is optional
     */
    public void addParameter (String  param,
                              String  explanation,
                              boolean required)
    {
        if (param.charAt(0) == '-')
        {
            throw new IllegalArgumentException
                ("(BUG) Option \"" +
                 param +
                 "\" registered via UsageInfo.addParameter(), instead of " +
                 "via UsageInfo.addOption().");
        }

        paramMap.put (param, explanation);
        paramNames.add (param);
        if (required)
            requiredParams.add (param);
    }

    /**
     * Add a prologue to be displayed before the standard usage message.
     * The prologue string should be one line; it'll be broken up, wrapped,
     * and displayed as a paragraph that precedes the usage message.
     *
     * @param prologue   the prologue string
     *
     * @see #addUsageTrailer
     */
    public void addUsagePrologue (String prologue)
    {
        this.usagePrologue = prologue;
    }

    /**
     * Add a trailer to be displayed after the standard usage message. The
     * trailer string should be one line; it'll be broken up, wrapped, and
     * displayed as a paragraph following the usage message.
     *
     * @param trailer   the trailer string
     *
     * @see #addUsageTrailer
     */
    public void addUsageTrailer (String trailer)
    {
        this.usageTrailer = trailer;
    }

    /*----------------------------------------------------------------------*\
                              Package Methods
    \*----------------------------------------------------------------------*/

    String[] getParameterNames()
    {
        String[]         names = new String[paramNames.size()];
        int              i;
        Iterator<String> it;

        for (i = 0, it = paramNames.iterator(); it.hasNext(); i++)
            names[i] = it.next();

        return names;
    }

    boolean parameterIsRequired (String name)
    {
        return requiredParams.contains (name);
    }

    String getParameterExplanation (String name)
    {
        return paramMap.get (name);
    }

    OptionInfo getOptionInfo (char shortOption)
    {
        return shortOptionMap.get (shortOption);
    }

    OptionInfo getOptionInfo (String longOption)
    {
        return longOptionMap.get (longOption);
    }

    OptionInfo[] getOptions()
    {
        OptionInfo[]         options = new OptionInfo[allOptions.size()];
        int                  i;
        Iterator<OptionInfo> it;

        for (i = 0, it = allOptions.iterator(); it.hasNext(); i++)
            options[i] = it.next();

        // Now, sort by option name.

        Arrays.sort (options, new OptionComparator (true));
        return options;
    }

    String getUsagePrologue()
    {
        return this.usagePrologue;
    }

    String getUsageTrailer()
    {
        return this.usageTrailer;
    }
}
