package org.clapper.util.io;

import java.io.FileFilter;
import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p><tt>RegexFileFilter</tt> implements a <tt>java.io.FileFilter</tt>
 * class that matches files using a regular expression. Multiple regular
 * expression filters can be combined using {@link AndFileFilter}
 * and/or {@link OrFileFilter} objects.</p>
 *
 * <p>A <tt>RegexFileFilter</tt> can be configured to operate on just the
 * simple file name, or on the file's path.</p>
 *
 * <p><tt>RegexFileFilter</tt> uses the <tt>java.util.regex</tt>
 * regular expression classes.</p>
 *
 * @see AndFileFilter
 * @see OrFileFilter
 * @see NotFileFilter
 * @see RegexFilenameFilter
 */
public class RegexFileFilter
    implements FileFilter
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
     * Construct a new <tt>RegexFileFilter</tt> using the specified
     * pattern.
     *
     * @param regex     the regular expression to add
     * @param matchType <tt>FileFilterMatchType.FILENAME</tt> to match just the
     *                  filename, <tt>FileFilterMatchType.PATH</tt> to match
     *                  the path (via <tt>java.io.File.getPath()</tt>)
     *
     * @throws PatternSyntaxException  bad regular expression
     */
    public RegexFileFilter (String regex, FileFilterMatchType matchType)
        throws PatternSyntaxException
    {
        this.matchType = matchType;
        pattern = Pattern.compile (regex);
    }

    /**
     * Construct a new <tt>RegexFileFilter</tt> using the specified
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
    public RegexFileFilter (String              regex,
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
     * @param file  The file to test. If the match type is
     *              <tt>FileFilterMatchType.FILENAME</tt>, then the value
     *              of <tt>file.getPath()</tt> is compared to the regular
     *              expression. If the match type is
     *              <tt>FileFilterMatchType.PATH</tt>, then the value of
     *              <tt>file.getName()</tt> is compared to the regular
     *              expression.
     *
     * @return <tt>true</tt> if the file matches, <tt>false</tt> if it doesn't
     */
    public boolean accept (File file)
    {
        String name = null;

        switch (matchType)
        {
            case PATH:
                name = file.getPath();
                break;

            case FILENAME:
                name = file.getName();
                break;

            default:
                assert (false);
        }

        return pattern.matcher (name).find();
    }
}
