package org.clapper.util.io;

import java.io.FilenameFilter;
import java.io.File;

/**
 * <tt>NotFilenameFilter</tt> is a <tt>FilenameFilter</tt> that wraps another
 * <tt>FilenameFilter</tt> and negates the sense of the wrapped filter's
 * <tt>accept()</tt> method. This class conceptually provides a logical
 * "NOT" operator for file filters. For example, the following code
 * fragment will create a filter that finds all files that do not start with
 * the letter "A".
 *
 * <blockquote><pre>
 * NotFilenameFilter filter = new NotFilenameFilter (new RegexFilenameFilter ("^[Aa]", FileFilterMatchType.NAME));
 * </pre></blockquote>
 *
 * @see FilenameFilter
 * @see AndFilenameFilter
 * @see OrFilenameFilter
 */
public class NotFilenameFilter implements FilenameFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private FilenameFilter filter;

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>NotFilenameFilter</tt> that wraps the
     * specified {@link FilenameFilter}.
     *
     * @param filter  The {@link FilenameFilter} to wrap.
     */
    public NotFilenameFilter (FilenameFilter filter)
    {
        this.filter = filter;
    }

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Tests whether a file should be included in a file list.
     *
     * @param dir   The directory containing the file.
     * @param name  the file name
     *
     * @return <tt>true</tt> if the file matches, <tt>false</tt> if it doesn't
     */
    public boolean accept (File dir, String name)
    {
        return ! this.filter.accept (dir, name);
    }
}
