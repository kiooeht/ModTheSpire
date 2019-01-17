package org.clapper.util.io;

import java.io.FilenameFilter;
import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p><tt>RegexFilenameFilter</tt> implements a <tt>java.io.FilenameFilter</tt>
 * class that matches files using a regular expression. Multiple regular
 * expression filters can be combined using {@link AndFilenameFilter}
 * and/or {@link OrFilenameFilter} objects.</p>
 *
 * <p>A <tt>RegexFilenameFilter</tt> can be configured to operate on just the
 * simple file name, or on the file's path.</p>
 *
 * <p><tt>RegexFilenameFilter</tt> uses the <tt>java.util.regex</tt>
 * regular expression classes.</p>
 *
 * @see AndFilenameFilter
 * @see OrFilenameFilter
 * @see NotFilenameFilter
 * @see RegexFileFilter
 */
public class RegexFilenameFilter
    implements FilenameFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private FileFilterMatchType matchType = FileFilterMatchType.PATH;
    private Pattern pattern;

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>RegexFilenameFilter</tt> using the specified
     * pattern, with an implied match type of
     * <tt>FileFilterMatchType.FILENAME</tt>.
     *
     * @param regex the regular expression to add

     * @throws PatternSyntaxException  bad regular expression
     */
    public RegexFilenameFilter (String regex)
        throws PatternSyntaxException
    {
        this (regex, FileFilterMatchType.FILENAME);
    }

    /**
     * Construct a new <tt>RegexFilenameFilter</tt> using the specified
     * pattern.
     *
     * @param regex     the regular expression to add
     * @param matchType <tt>FileFilterMatchType.FILENAME</tt> to match just the
     *                  filename, <tt>FileFilterMatchType.PATH</tt> to match
     *                  the path (via <tt>java.io.File.getPath()</tt>)
     *
     * @throws PatternSyntaxException  bad regular expression
     */
    public RegexFilenameFilter (String regex, FileFilterMatchType matchType)
        throws PatternSyntaxException
    {
        this.matchType = matchType;
        pattern = Pattern.compile (regex);
    }

    /**
     * Construct a new <tt>RegexFilenameFilter</tt> using the specified
     * pattern.
     *
     * @param regex      the regular expression to add
     * @param regexFlags regular expression compilation flags (e.g.,
     *                   <tt>Pattern.CASE_INSENSITIVE</tt>). See
     *                   the Javadocs for <tt>java.util.regex</tt> for
     *                   legal values.
     * @param matchType <tt>FileFilterMatchType.FILENAME</tt> to match just the
     *                  filename, <tt>FileFilterMatchType.PATH</tt> to match
     *                  the path (via <tt>java.io.File.getPath()</tt>)
     *
     * @throws PatternSyntaxException  bad regular expression
     */
    public RegexFilenameFilter (String              regex,
                                int                 regexFlags,
                                FileFilterMatchType matchType)
        throws PatternSyntaxException
    {
        this.matchType = matchType;
        pattern = Pattern.compile (regex, regexFlags);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether a file is to be accepted or not, based on the
     * regular expressions in the <i>reject</i> and <i>accept</i> lists.
     *
     * @param dir   The directory containing the file. Ignored if
     *              the match type is <tt>FileFilterMatchType.FILENAME</tt>.
     *              Used to build the path to match when the match type is
     *              <tt>FileFilterMatchType.PATH</tt>
     * @param name  the file name
     *
     * @return <tt>true</tt> if the file matches, <tt>false</tt> if it doesn't
     */
    public boolean accept (File dir, String name)
    {
        if (matchType == FileFilterMatchType.PATH)
            name = new File (dir, name).getPath();

        return pattern.matcher (name).find();
    }
}
