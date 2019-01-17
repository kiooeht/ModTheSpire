package org.clapper.util.classutil;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p><tt>RegexClassFilter</tt> is a {@link ClassFilter} that matches class
 * names using a regular expression. Multiple regular expression filters
 * can be combined using {@link AndClassFilter} and/or
 * {@link OrClassFilter} objects.</p>
 *
 * <p>This class does not have to load the classes it's filtering; it
 * matches on the class name only.</p>
 *
 * <p><tt>RegexClassFilter</tt> uses the <tt>java.util.regex</tt>
 * regular expression classes.</p>
 *
 * @see ClassFilter
 * @see AndClassFilter
 * @see OrClassFilter
 * @see NotClassFilter
 * @see ClassFinder
 */
public class RegexClassFilter
    implements ClassFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private Pattern pattern;

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>RegexClassFilter</tt> using the specified
     * pattern.
     *
     * @param regex  the regular expression to add
     *
     * @throws PatternSyntaxException  bad regular expression
     */
    public RegexClassFilter (String regex)
        throws PatternSyntaxException
    {
        pattern = Pattern.compile (regex);
    }

    /**
     * Construct a new <tt>RegexClassFilter</tt> using the specified
     * pattern.
     *
     * @param regex      the regular expression to add
     * @param regexFlags regular expression compilation flags (e.g.,
     *                   <tt>Pattern.CASE_INSENSITIVE</tt>). See
     *                   the Javadocs for <tt>java.util.regex</tt> for
     *                   legal values.
     *
     * @throws PatternSyntaxException  bad regular expression
     */
    public RegexClassFilter (String regex, int regexFlags)
        throws PatternSyntaxException
    {
        pattern = Pattern.compile (regex, regexFlags);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether a class name is to be accepted or not, based on
     * the regular expression specified to the constructor.
     *
     * @param classInfo   the {@link ClassInfo} object to test
     * @param classFinder the invoking {@link ClassFinder} object
     *
     * @return <tt>true</tt> if the class name matches,
     *         <tt>false</tt> if it doesn't
     */
    public boolean accept (ClassInfo classInfo, ClassFinder classFinder)
    {
        return pattern.matcher (classInfo.getClassName()).find();
    }
}
