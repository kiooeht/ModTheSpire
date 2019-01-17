package org.clapper.util.classutil;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>An <tt>AndClassFilter</tt> logically ANDs other
 * {@link ClassFilter} objects. When its {@link #accept accept()}
 * method is called, the <tt>AndClassFilter</tt> object passes
 * the class name through the contained filters. The class name is only
 * accepted if it is accepted by all contained filters. This
 * class conceptually provides a logical "AND" operator for class name
 * filters.</p>
 *
 * <p>The contained filters are applied in the order they were added to
 * the <tt>AndClassFilter</tt> object. This class's
 * {@link #accept accept()} method stops looping over the contained filters
 * as soon as it encounters one whose <tt>accept()</tt> method returns
 * <tt>false</tt> (implementing a "short-circuited AND" operation.) </p>
 *
 * @see ClassFilter
 * @see OrClassFilter
 * @see NotClassFilter
 * @see ClassFinder
 */
public final class AndClassFilter implements ClassFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private List<ClassFilter> filters = new LinkedList<ClassFilter>();

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>AndClassFilter</tt> with no contained filters.
     */
    public AndClassFilter()
    {
        // Nothing to do
    }

    /**
     * Construct a new <tt>AndClassFilter</tt> with a set of contained
     * filters. Additional filters may be added later, via calls to the
     * {@link #addFilter addFilter()} method.
     *
     * @param filters  filters to add
     */
    public AndClassFilter (ClassFilter... filters)
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
    public AndClassFilter addFilter (ClassFilter filter)
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
     * the contained filters. The class name is accepted if any one of the
     * contained filters accepts it. This method stops looping over the
     * contained filters as soon as it encounters one whose
     * {@link ClassFilter#accept accept()} method returns
     * <tt>false</tt> (implementing a "short-circuited AND" operation.)</p>
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
        boolean accepted = true;

        for (ClassFilter filter : filters)
        {
            accepted = filter.accept (classInfo, classFinder);
            if (! accepted)
                break;
        }

        return accepted;
    }
}
