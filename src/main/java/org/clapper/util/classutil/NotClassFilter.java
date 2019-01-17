package org.clapper.util.classutil;

/**
 * <tt>NotClassFilter</tt> is a {@link ClassFilter} that
 * wraps another {@link ClassFilter} and negates the sense of the
 * wrapped filter's {@link ClassFilter#accept accept()} method. This
 * class conceptually provides a logical "NOT" operator for class name
 * filters. For example, the following code fragment will create a filter
 * that finds all classes that are not interfaces.
 *
 * <blockquote><pre>
 * NotClassFilter filter = new NotClassFilter (new InterfaceOnlyClassFilter());
 * </pre></blockquote>
 *
 * @see ClassFilter
 * @see AndClassFilter
 * @see OrClassFilter
 * @see ClassFinder
 * @see InterfaceOnlyClassFilter
 */
public class NotClassFilter implements ClassFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private ClassFilter filter;

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>NotClassFilter</tt> that wraps the
     * specified {@link ClassFilter}.
     *
     * @param filter  The {@link ClassFilter} to wrap.
     */
    public NotClassFilter (ClassFilter filter)
    {
        this.filter = filter;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Tests whether a class name should be included in a class name
     * list.
     *
     * @param classInfo   the {@link ClassInfo} object to test
     * @param classFinder the invoking {@link ClassFinder} object
     *
     * @return <tt>true</tt> if and only if the name should be included
     *         in the list; <tt>false</tt> otherwise
     */
    public boolean accept (ClassInfo classInfo, ClassFinder classFinder)
    {
        return ! this.filter.accept (classInfo, classFinder);
    }
}
