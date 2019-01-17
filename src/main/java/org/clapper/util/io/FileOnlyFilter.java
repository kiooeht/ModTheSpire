package org.clapper.util.io;

import java.io.*;

/**
 * <tt>FileOnlyFilter</tt> implements a
 * <tt>java.io.FileFilter</tt> that matches anything that isn't a directory.
 * Note that the following two lines produce semantically equivalent filters:
 *
 * <blockquote><pre>
 * FileFilter f1 = new FileOnlyFilter();
 * FileFilter f1 = new NotFileFilter (new DirectoryFilter());
 * </pre></blockquote>
 */
public class FileOnlyFilter implements FileFilter
{
    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    public FileOnlyFilter()
    {
        // Nothing to do
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether the specified file is a directory or not.
     *
     * @return <tt>true</tt> if the file is a directory, <tt>false</tt> if not
     */
    public boolean accept (File f)
    {
        return f.isFile();
    }
}
