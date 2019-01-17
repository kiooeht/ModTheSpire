package org.clapper.util.cmdline;

/**
 * Used solely by <tt>UsageInfo</tt>, this class contains information about
 * an option.
 *
 * @see UsageInfo
 */
final class OptionInfo
{
    char    shortOption;
    String  longOption;
    String  argToken;
    String  explanation;

    OptionInfo (char   shortOption,
                String longOption,
                String argToken,
                String explanation)
    {
        this.shortOption = shortOption;
        this.longOption  = longOption;
        this.argToken    = argToken;
        this.explanation = explanation;
    }
}
