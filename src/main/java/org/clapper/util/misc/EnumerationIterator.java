package org.clapper.util.misc;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * <p>The <tt>EnumerationIterator</tt> class is an adapter that makes a
 * <tt>java.util.Enumeration</tt> object look and behave like a
 * <tt>java.util.Iterator</tt> objects. The <tt>EnumerationIterator</tt>
 * class implements the <tt>Iterator</tt> interface and wraps an existing
 * <tt>Enumeration</tt> object. This class is the conceptual opposite of
 * the <tt>Collections.enumeration()</tt> method in the <tt>java.util</tt>
 * package.</p>
 *
 * <p>You can also use an instance of this class to wrap an
 * <tt>Enumeration</tt> for use in a JDK 1.5-style <i>for each</i> loop.
 * For instance:</p>
 *
 * <blockquote><pre>
 * {@code
 * Vector<String> v = ...
 * for (String s : new EnumerationIterator<String> (v.elements()))
 *     ...
 * }
 * </pre></blockquote>
 *
 * @see java.util.Iterator
 * @see java.util.Enumeration
 */
public class EnumerationIterator<T> implements Iterator<T>, Iterable<T>
{
    /*----------------------------------------------------------------------*\
                           Private Data Elements
    \*----------------------------------------------------------------------*/

    /**
     * The underlying Enumeration.
     */
    private Enumeration<T> enumeration = null;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>EnumerationIterator</tt> object that will
     * forward its calls to the specified <tt>Enumeration</tt>.
     *
     * @param enumeration  The <tt>Enumeration</tt> to which to forward calls
     */
    public EnumerationIterator (Enumeration<T> enumeration)
    {
        this.enumeration = enumeration;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Determine whether the underlying <tt>Enumeration</tt> has more
     * elements.
     *
     * @return <tt>true</tt> if and only if a call to
     *         <tt>next()</tt> will return an element,
     *         <tt>false</tt> otherwise.
     *
     * @see #next()
     * @see Enumeration#hasMoreElements
     */
    public boolean hasNext()
    {
        return enumeration.hasMoreElements();
    }

    /**
     * Returns this iterator. Necessary for the <tt>Iterable</tt> interface.
     *
     * @return this object
     */
    public Iterator<T> iterator()
    {
        return this;
    }

    /**
     * Get the next element from the underlying <tt>Enumeration</tt>.
     *
     * @return the next element from the underlying <tt>Enumeration</tt>
     *
     * @exception NoSuchElementException No more elements exist
     *
     * @see Iterator#next
     */
    public T next() throws NoSuchElementException
    {
        return enumeration.nextElement();
    }

    /**
     * Removes from the underlying collection the last element returned by
     * the iterator. Not supported by this class.
     *
     * @throws IllegalStateException         doesn't
     * @throws UnsupportedOperationException unconditionally
     */
    public void remove()
        throws IllegalStateException,
               UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }
}
