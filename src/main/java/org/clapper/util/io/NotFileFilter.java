package org.clapper.util.io;

import java.io.FileFilter;
import java.io.File;

/**
 * <tt>NotFileFilter</tt> is a <tt>FileFilter</tt> that wraps another
 * <tt>FileFilter</tt> and negates the sense of the wrapped filter's
 * <tt>accept()</tt> method. This class conceptually provides a logical
 * "NOT" operator for file filters. For example, the following code
 * fragment will create a filter that finds all files that are not
 * directories.
 *
 * <blockquote><pre>
 * NotFileFilter filter = new NotFileFilter (new DirectoryFilter());
 * </pre></blockquote>
 *
 * @see FileFilter
 * @see AndFileFilter
 * @see OrFileFilter
 * @see DirectoryFilter
 */
public class NotFileFilter implements FileFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private FileFilter filter;

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>NotFileFilter</tt> that wraps the
     * specified {@link FileFilter}.
     *
     * @param filter  The {@link FileFilter} to wrap.
     */
    public NotFileFilter (FileFilter filter)
    {
        this.filter = filter;
    }

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Tests whether a file should be included in a file list.
     *
     * @param file  The file to check for acceptance
     *
     * @return <tt>true</tt> if the file matches, <tt>false</tt> if it doesn't
     */
    public boolean accept (File file)
    {
        return ! this.filter.accept (file);
    }
}
