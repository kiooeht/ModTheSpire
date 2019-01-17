package org.clapper.util.classutil;

import java.lang.reflect.Modifier;

/**
 * <p><tt>AbstractClassFilter</tt> implements a {@link ClassFilter}
 * that matches class names that (a) can be loaded and (b) are abstract. It
 * relies on the pool of classes read by a {@link ClassFinder}; it's
 * not really useful by itself.</p>
 *
 * <p>This class is really just a convenient specialization of the
 * {@link ClassModifiersClassFilter} class.</p>
 *
 * @see ClassFilter
 * @see ClassModifiersClassFilter
 * @see ClassFinder
 * @see Modifier
 */
public class AbstractClassFilter
    extends ClassModifiersClassFilter
{
    /*----------------------------------------------------------------------*\
                            Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new <tt>AbstractClassFilter</tt> that will accept
     * only abstract classes.
     */
    public AbstractClassFilter()
    {
        super (Modifier.ABSTRACT);
    }
}
