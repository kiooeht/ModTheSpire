package org.clapper.util.io;

import java.util.LinkedList;
import java.util.List;

import java.io.FileFilter;
import java.io.File;

/**
 * <p>An <tt>AndFileFilter</tt> contains logically ANDs other
 * <tt>java.io.FileFilter</tt> objects. When its {@link #accept accept()}
 * method is called, the <tt>AndFileFilter</tt> object passes the file
 * through the contained filters. The file is only accepted if it is
 * accepted by all contained filters. This class conceptually provides a
 * logical "AND" operator for file filters.</p>
 *
 * <p>The contained filters are applied in the order they were added to the
 * <tt>AndFileFilter</tt> object. This class's {@link #accept accept()}
 * method stops looping over the contained filters as soon as it encounters
 * one whose <tt>accept()</tt> method returns <tt>false</tt> (implementing
 * a "short-circuited AND" operation.) </p>
 *
 * @see FileFilter
 * @see OrFileFilter
 * @see NotFileFilter
 * @see AndFilenameFilter
 * @see RegexFileFilter
 */
public final class AndFileFilter implements FileFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private List<FileFilter> filters = new LinkedList<FileFilter>();

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>AndFileFilter</tt> with no contained filters.
     */
    public AndFileFilter()
    {
        // nothing to do
    }

    /**
     * Construct a new <tt>AndFileFilter</tt> with a set of contained filters.
     * Additional filters may be added later, via calls to the
     * {@link #addFilter addFilter()} method.
     *
     * @param filters  filters to add
     */
    public AndFileFilter (FileFilter... filters)
    {
        for (FileFilter filter : filters)
            addFilter (filter);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add a filter to the set of contained filters.
     *
     * @param filter the <tt>FileFilter</tt> to add.
     *
     * @return this object, to permit chained calls.
     *
     * @see #removeFilter
     */
    public AndFileFilter addFilter (FileFilter filter)
    {
        filters.add (filter);
        return this;
    }

    /**
     * Remove a filter from the set of contained filters.
     *
     * @param filter the <tt>FileFilter</tt> to remove.
     *
     * @see #addFilter
     */
    public void removeFilter (FileFilter filter)
    {
        filters.remove (filter);
    }

    /**
     * <p>Determine whether a file is to be accepted or not, based on the
     * contained filters. The file is accepted if any one of the contained
     * filters accepts it. This method stops looping over the contained
     * filters as soon as it encounters one whose <tt>accept()</tt> method
     * returns <tt>false</tt> (implementing a "short-circuited AND"
     * operation.)</p>
     *
     * <p>If the set of contained filters is empty, then this method
     * returns <tt>true</tt>.</p>
     *
     * @param file  The file to check for acceptance
     *
     * @return <tt>true</tt> if the file matches, <tt>false</tt> if it doesn't
     */
    public boolean accept (File file)
    {
        boolean accepted = true;

        for (FileFilter filter : filters)
        {
            accepted = filter.accept (file);
            if (! accepted)
                break;
        }

        return accepted;
    }
}
