package org.clapper.util.classutil;

import java.lang.reflect.Modifier;

/**
 * <p><tt>InterfaceOnlyClassFilter</tt> implements a {@link ClassFilter}
 * that matches class names that (a) can be loaded and (b) are interfaces. It
 * relies on the pool of classes read by a {@link ClassFinder}; it's
 * not really useful by itself.</p>
 *
 * <p>This class is really just a convenient specialization of the
 * {@link ClassModifiersClassFilter} class.</p>
 */
public class InterfaceOnlyClassFilter
    extends ClassModifiersClassFilter
{
    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>InterfaceOnlyClassFilter</tt> that will accept
     * only classes that are interfaces.
     */
    public InterfaceOnlyClassFilter()
    {
        super (Modifier.INTERFACE);
    }
}
