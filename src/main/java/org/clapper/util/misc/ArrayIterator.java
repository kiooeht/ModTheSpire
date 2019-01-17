package org.clapper.util.misc;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The <tt>ArrayIterator</tt> class provides a bridge between an array of
 * objects and an <tt>Iterator</tt>. It's useful in cases where you have an
 * array, but you need an <tt>Iterator</tt>; using an instance of
 * <tt>ArrayIterator</tt> saves copying the array's contents into a
 * <tt>Collection</tt>, just to get an <tt>Iterator</tt>.
 *
 * @see java.util.Iterator
 */
public class ArrayIterator<T> implements Iterator<T>
{
    /*----------------------------------------------------------------------*\
                           Private Data Elements
    \*----------------------------------------------------------------------*/

    /**
     * The underlying Array.
     */
    private T array[] = null;

    /**
     * The next array index.
     */
    private int nextIndex = 0;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>ArrayIterator</tt> object that will
     * iterate over the specified array of objects.
     *
     * @param array  The array over which to iterate
     */
    public ArrayIterator (T array[])
    {
        this.array = array;
    }

    /**
     * Allocate a new <tt>ArrayIterator</tt> object that will iterate
     * over the specified array of objects, starting at a particular index.
     * The index isn't checked for validity until <tt>next()</tt> is called.
     *
     * @param array  The array over which to iterate
     * @param index  The index at which to start
     */
    public ArrayIterator (T array[], int index)
    {
        this.array = array;
        this.nextIndex  = index;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the index of the next element to be retrieved. This index value
     * might be past the end of the array.
     *
     * @return the index
     */
    public int getNextIndex()
    {
        return nextIndex;
    }

    /**
     * Determine whether the underlying <tt>Iterator</tt> has more
     * elements.
     *
     * @return <tt>true</tt> if and only if a call to
     *         <tt>next()</tt> will return an element,
     *         <tt>false</tt> otherwise.
     *
     * @see #next
     */
    public boolean hasNext()
    {
        return (array != null) && (nextIndex < array.length);
    }

    /**
     * Get the next element from the underlying array.
     *
     * @return the next element from the underlying array
     *
     * @exception java.util.NoSuchElementException
     *            No more elements exist
     *
     * @see #previous()
     * @see java.util.Iterator#next
     */
    public T next() throws NoSuchElementException
    {
        T result = null;

        try
        {
            if (array == null)
                throw new NoSuchElementException(); // NOPMD

            result = array[nextIndex++];
        }

        catch (ArrayIndexOutOfBoundsException ex)
        {
            throw new NoSuchElementException(); // NOPMD
        }

        return result;
    }

    /**
     * Get the previous element from the underlying array. This method
     * decrements the iterator's internal index by one, and returns the
     * corresponding element.
     *
     * @return the previous element from the underlying array
     *
     * @exception java.util.NoSuchElementException
     *            Attempt to move internal index before the first array element
     *
     * @see #next()
     */
    public T previous() throws NoSuchElementException
    {
        T result = null;

        try
        {
            result = array[--nextIndex];
        }

        catch (ArrayIndexOutOfBoundsException ex)
        {
            throw new NoSuchElementException();  // NOPMD
        }

        return result;
    }

    /**
     * Required by the <tt>Iterator</tt> interface, but not supported by
     * this class.
     *
     * @throws UnsupportedOperationException  unconditionally
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
