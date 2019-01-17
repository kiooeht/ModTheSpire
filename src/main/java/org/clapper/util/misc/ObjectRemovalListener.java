package org.clapper.util.misc;

import java.util.EventListener;

/**
 * <p>An <tt>ObjectRemovalListener</tt> is an <tt>EventListener</tt> that
 * can be registered with certain data store objects to receive an
 * {@link ObjectRemovalEvent} whenever an object is removed from the data
 * store.</p>
 *
 * @see ObjectRemovalEvent
 * @see LRUMap#addRemovalListener
 */
public interface ObjectRemovalListener extends EventListener
{
    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * This method gets called when an object is removed from a store.
     *
     * @param event the <tt>ObjectRemovalEvent</tt> event
     */
    public void objectRemoved (ObjectRemovalEvent event);
}
