package org.clapper.util.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p><tt>SparseList</tt> implements a sparse array. This class is identical to
 * <tt>java.util.ArrayList</tt>, except that it permits assignment to array
 * indexes that are beyond the current length of the list, using the expected
 * <tt>set()</tt> and <tt>add()</tt> methods. The list is extended and 
 * null-filled in that case.</p>
 *
 * <p><strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access an <tt>ArrayList</tt> instance concurrently, and at
 * least one of the threads modifies the list structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation that
 * adds or deletes one or more elements, or explicitly resizes the backing
 * array; merely setting the value of an element is not a structural
 * modification.)  This is typically accomplished by synchronizing on some
 * object that naturally encapsulates the list.  If no such object exists, the
 * list should be wrapped using the <tt>Collections.synchronizedList</tt>
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:</p>
 *
 * <pre>
 *	List list = Collections.synchronizedList(new SparseList());
 * </pre>
 *
 * <p>The iterators returned by this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if list is structurally
 * modified at any time after the iterator is created, in any way except
 * through the iterator's own remove or add methods, the iterator will throw a
 * <tt>ConcurrentModificationException</tt>.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.</p>
 */
public class SparseArrayList<T> extends ArrayList<T>
{
    /*----------------------------------------------------------------------*\
                               Private Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                             Private Instance Data
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                                   Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new <tt>SparseList</tt> object with the same default
     * initial capacity as a <tt>java.util.ArrayList</tt> object created
     * with its default constructor.
     */
    public SparseArrayList()
    {
        super();
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity  the initial capacity of the list.
     *
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public SparseArrayList(int initialCapacity)
    {
        super(initialCapacity);
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator. The <tt>SparseList</tt> instance has an initial capacity
     * of 110% the size of the specified collection.
     *
     * @param c the collection whose elements are to be placed into this list.
     *
     * @throws NullPointerException if the specified collection is null.
     */
    public SparseArrayList(Collection<T> c)
    {
        super (c);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices). If
     * the specified index is beyond the end of the list, then this method
     * behaves exactly like {@link #set(int,Object)}.
     *
     * @param index   index at which the specified element is to be inserted.
     * @param element element to be inserted.
     *
     * @throws IndexOutOfBoundsException if the index is negative.
     */
    @Override
    public void add(int index, T element)
    {
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("Index " + index +
                                                " is negative.");
        }

	if (index >= size())
            set(index, element);
        else
            super.add(index, element);
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element. Unlike a regular <tt>ArrayList</tt>, this method
     * does not throw an exception if the passed-in index is beyond the end
     * of the array; instead, it extends the array (via
     * <tt>ensureCapacity()</tt>) so that the index is legal. (But, the
     * index <strong>must</strong> be positive.)
     *
     * @param index   index of element to replace or store
     * @param element element to be stored at the specified position
     *
     * @return the element previously at the specified position, or null
     *
     * @throws IndexOutOfBoundsException if <tt>index</tt> is negative
     */
    @Override
    public T set(int index, T element)
        throws IndexOutOfBoundsException
    {
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("Index " + index +
                                                " is negative.");
        }

        int curSize = size();
        if (index >= curSize)
        {
            int newSize = index + 1;
            ensureCapacity (newSize);
            while (curSize++ < newSize)
                super.add(null);
        }

        return super.set(index, element);
    }
}
