package org.clapper.util.io;

import java.util.LinkedList;
import java.util.List;

import java.io.FilenameFilter;
import java.io.File;

/**
 * <p>An <tt>OrFilenameFilter</tt> logically ORs other
 * <tt>java.io.FilenameFilter</tt> objects. When its
 * {@link #accept accept()} method is called, the <tt>OrFilenameFilter</tt>
 * object passes the file through the contained filters. The file is
 * accepted if it is accepted by any of the contained filters. This class
 * conceptually provides a logical "OR" operator for file filters.</p>
 *
 * <p>The contained filters are applied in the order they were added to the
 * <tt>OrFilenameFilter</tt> object. This class's {@link #accept accept()}
 * method stops looping over the contained filters as soon as it encounters
 * one whose <tt>accept()</tt> method returns <tt>true</tt> (implementing
 * a "short-circuited OR" operation.) </p>
 *
 * @see FilenameFilter
 * @see AndFilenameFilter
 * @see NotFilenameFilter
 * @see OrFilenameFilter
 * @see RegexFilenameFilter
 */
public final class OrFilenameFilter implements FilenameFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private List<FilenameFilter> filters = new LinkedList<FilenameFilter>();

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>OrFilenameFilter</tt> with no contained filters.
     */
    public OrFilenameFilter()
    {
        // nothing to do
    }

    /**
     * Construct a new <tt>OrFilenameFilter</tt> with two contained filters.
     * Additional filters may be added later, via calls to the
     * {@link #addFilter addFilter()} method.
     *
     * @param filters  filters to add
     */
    public OrFilenameFilter (FilenameFilter... filters)
    {
        for (FilenameFilter filter : filters)
            addFilter (filter);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add a filter to the set of contained filters.
     *
     * @param filter the <tt>FilenameFilter</tt> to add.
     *
     * @return this object, to permit chained calls.
     *
     * @see #removeFilter
     */
    public OrFilenameFilter addFilter (FilenameFilter filter)
    {
        filters.add (filter);
        return this;
    }

    /**
     * Remove a filter from the set of contained filters.
     *
     * @param filter the <tt>FilenameFilter</tt> to remove.
     *
     * @see #addFilter
     */
    public void removeFilter (FilenameFilter filter)
    {
        filters.remove (filter);
    }

    /**
     * <p>Determine whether a file is to be accepted or not, based on the
     * contained filters. The file is accepted if any one of the contained
     * filters accepts it. This method stops looping over the contained
     * filters as soon as it encounters one whose <tt>accept()</tt> method
     * returns <tt>true</tt> (implementing a "short-circuited OR"
     * operation.)</p>
     *
     * <p>If the set of contained filters is empty, then this method
     * returns <tt>true</tt>.</p>
     *
     * @param dir   The directory containing the file.
     * @param name  the file name
     *
     * @return <tt>true</tt> if the file matches, <tt>false</tt> if it doesn't
     */
    public boolean accept (File dir, String name)
    {
        boolean accepted = false;

        if (filters.size() == 0)
            accepted = true;

        else
        {
            for (FilenameFilter filter : filters)
            {
                accepted = filter.accept (dir, name);
                if (accepted)
                    break;
            }
        }

        return accepted;
    }
}
