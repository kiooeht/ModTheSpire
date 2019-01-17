package org.clapper.util.classutil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>An <tt>OrClassFilter</tt> contains logically ORs other
 * {@link ClassFilter} objects. When its {@link #accept accept()} 
 * method is called, the <tt>OrClassFilter</tt> object passes
 * the class name through the contained filters. The class name is
 * accepted if it is accepted by any one of the contained filters. This
 * class conceptually provides a logical "OR" operator for class name
 * filters.</p>
 *
 * <p>The contained filters are applied in the order they were added to
 * the <tt>OrClassFilter</tt> object. This class's
 * {@link #accept accept()} method stops looping over the contained filters
 * as soon as it encounters one whose <tt>accept()</tt> method returns
 * <tt>true</tt> (implementing a "short-circuited OR" operation.) </p>
 *
 * @see ClassFilter
 * @see OrClassFilter
 * @see NotClassFilter
 * @see ClassFinder
 */
public final class OrClassFilter implements ClassFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private List<ClassFilter> filters = new LinkedList<ClassFilter>();

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>OrClassFilter</tt> with no contained filters.
     */
    public OrClassFilter()
    {
        // Nothing to do
    }

    /**
     * Construct a new <tt>OrClassFilter</tt> with two contained filters.
     * Additional filters may be added later, via calls to the
     * {@link #addFilter addFilter()} method.
     *
     * @param filters  filters to add
     */
    public OrClassFilter (ClassFilter... filters)
    {
        for (ClassFilter filter : filters)
            addFilter (filter);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Add a filter to the set of contained filters.
     *
     * @param filter the <tt>ClassFilter</tt> to add.
     *
     * @return this object, to permit chained calls.
     *
     * @see #removeFilter
     */
    public OrClassFilter addFilter (ClassFilter filter)
    {
        filters.add (filter);
        return this;
    }

    /**
     * Remove a filter from the set of contained filters.
     *
     * @param filter the <tt>ClassFilter</tt> to remove.
     *
     * @see #addFilter
     */
    public void removeFilter (ClassFilter filter)
    {
        filters.remove (filter);
    }

    /**
     * Get the contained filters, as an unmodifiable collection.
     *
     * @return the unmodifable <tt>Collection</tt>
     */
    public Collection<ClassFilter> getFilters()
    {
        return Collections.unmodifiableCollection (filters);
    }

    /**
     * Get the total number of contained filter objects (not counting any
     * filter objects <i>they</i>, in turn, contain).
     *
     * @return the total
     */
    public int getTotalFilters()
    {
        return filters.size();
    }

    /**
     * <p>Determine whether a class name is to be accepted or not, based on
     * the contained filters. The class name name is accepted if any
     * one of the contained filters accepts it. This method stops
     * looping over the contained filters as soon as it encounters one
     * whose {@link ClassFilter#accept accept()} method returns
     * <tt>true</tt> (implementing a "short-circuited OR" operation.)</p>
     *
     * <p>If the set of contained filters is empty, then this method
     * returns <tt>true</tt>.</p>
     *
     * @param classInfo   the {@link ClassInfo} object to test
     * @param classFinder the invoking {@link ClassFinder} object
     *
     * @return <tt>true</tt> if the name matches, <tt>false</tt> if it doesn't
     */
    public boolean accept (ClassInfo classInfo, ClassFinder classFinder)
    {
        boolean accepted = false;

        if (filters.size() == 0)
            accepted = true;

        else
        {
            for (ClassFilter filter : filters)
            {
                accepted = filter.accept (classInfo, classFinder);
                if (accepted)
                    break;
            }
        }

        return accepted;
    }
}
