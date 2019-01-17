package org.clapper.util.classutil;

/**
 * <p><tt>ClassModifiersClassFilter</tt> is a {@link ClassFilter} that
 * matches class names that (a) can be loaded and (b) match a set of class
 * modifiers (as defined by the constants in the
 * <tt>java.lang.reflect.Modifier</tt> class). For instance, the the
 * following code fragment defines a filter that will match only public
 * final classes:</p>
 *
 * <blockquote><pre>
 * import java.lang.reflect.Modifier;
 *
 * ...
 *
 * ClassFilter = new ClassModifiersClassFilter (Modifier.PUBLIC | Modifier.FINAL);
 * </pre></blockquote>
 *
 * <p>This class relies on the pool of classes read by a
 * {@link ClassFinder}; it's not really useful by itself.</p>
 *
 * @see ClassFilter
 * @see ClassFinder
 * @see java.lang.reflect.Modifier
 */
public class ClassModifiersClassFilter implements ClassFilter
{
    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    private int modifiers   = 0;

    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>ClassModifiersClassFilter</tt> that will accept
     * any classes with the specified modifiers.
     *
     * @param modifiers  the bit-field of modifier flags. See the
     *                   <tt>java.lang.reflect.Modifier</tt> class for
     *                   legal values.
     */
    public ClassModifiersClassFilter (int modifiers)
    {
        super();
        this.modifiers = modifiers;
    }

    /**
     * Tests whether a class name should be included in a class name
     * list.
     *
     * @param classInfo   the loaded information about the class
     * @param classFinder the {@link ClassFinder} that called this filter
     *                    (mostly for access to <tt>ClassFinder</tt>
     *                    utility methods)
     *
     * @return <tt>true</tt> if and only if the name should be included
     *         in the list; <tt>false</tt> otherwise
     */
    public boolean accept (ClassInfo classInfo, ClassFinder classFinder)
    {
        return ((classInfo.getModifier() & modifiers) != 0);
    }
}
