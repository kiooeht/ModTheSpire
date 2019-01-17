package org.clapper.util.misc;

import java.util.EventObject;

/**
 * <p>An <tt>ObjectRemovalEvent</tt> is an event that is propagated to
 * certain event listeners when an object is removed from some kind of
 * a store or data structure. For instance, the {@link LRUMap} class supports
 * this event through its {@link LRUMap#addRemovalListener} method.</p>
 *
 * @see ObjectRemovalListener
 * @see LRUMap#addRemovalListener
 */
public class ObjectRemovalEvent extends EventObject
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                                Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Construct a <tt>ObjectRemovalEvent</tt> event to announce the removal
     * of an object from a data store.
     *
     * @param source the object being removed
     */
    public ObjectRemovalEvent (Object source)
    {
        super (source);
    }
}
