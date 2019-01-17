package org.clapper.util.cmdline;

import java.util.Comparator;

/**
 * Used solely by <tt>UsageInfo</tt>, an instance of this class compares
 * <tt>OptionInfo</tt> items, by short option name or long option name, in
 * a case-insensitive manner.
 *
 * @see UsageInfo
 * @see OptionInfo
 */
final class OptionComparator implements Comparator<OptionInfo>
{
    private boolean ignoreCase = false;

    public OptionComparator()
    {
        // Nothing to do
    }

    public OptionComparator (boolean ignoreCase)
    {
        this.ignoreCase = ignoreCase;
    }

    public int compare (OptionInfo o1, OptionInfo o2)
    {
        String s1 = getComparisonString (o1);
        String s2 = getComparisonString (o2);

        return ignoreCase ? s1.compareToIgnoreCase (s2) : s1.compareTo (s2);
    }

    public boolean equals (Object o)
    {
        return (this.getClass().isInstance (o));
    }

    public int hashCode()                                            // NOPMD
    {
        return super.hashCode();
    }

    private String getComparisonString (OptionInfo opt)
    {
        String result = "";

        if (opt.shortOption != UsageInfo.NO_SHORT_OPTION)
            result = String.valueOf (opt.shortOption);
        else if (opt.longOption != null)
            result = opt.longOption;

        return result;
    }
}
