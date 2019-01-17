package org.clapper.util.misc;

import java.util.Date;

/**
 * <p><b>NOTE: This class, and its parent interface, are deprecated. With
 * J2SE 5.0 (a.k.a., Java 1.5), the JDK provides its own semaphore class,
 * <tt>java.util.concurrent.Semaphore</tt>.</b></p>
 *
 * <p>The <tt>ObjectLockSemaphore</tt> class implements the {@link Semaphore}
 * interface and provides a classic counting semaphore that uses the Java
 * object-locking primitives (the same locking primitives used to implement
 * synchronized critical sections) to ensure that
 * <tt>ObjectLockSemaphore</tt> objects are atomically created, accessed,
 * updated and destroyed.</p>
 *
 * <p><b>Warning:</b> Do not attempt to acquire or release a semaphore within
 * a critical region--that is, within a "synchronized" section--or you'll
 * risk deadlock. The <tt>Semaphore</tt> class is implemented using the
 * Java VM's object monitor capability, the same capability that controls
 * how synchronized sections work. The following code fragment is likely to
 * cause a deadlock:</p>
 *
 * <blockquote>
 * <pre>
 *
 * private ArrayList bufferPool = ...;
 * private Semaphore sem = new ObjectLockSemaphore (bufferPool.size());
 *
 * public MyBuffer getBuffer()
 * {
 *     MyBuffer result = null;
 *
 *     synchronized (bufferPool)
 *     {
 *                                       // bufferPool is now locked
 *         sem.acquire();                // bufferPool is still locked
 *         result = (MyBuffer) bufferPool.removeElementAt (0);
 *     }
 *
 *     return result;
 * }
 *
 * public void returnBuffer (MyBuffer buf)
 * {
 *     synchronized (bufferPool)
 *     {
 *                                       // bufferPool is now locked
 *         bufferPool.addElement (buf);
 *     }
 *
 *     sem.release();                    // bufferPool is still locked
 * }
 * </pre>
 * </blockquote>
 *
 * <p>Given the above code, assume:
 *
 * <ul>
 *    <li>The semaphore is initialized to the number of buffers in the pool.
 *    <li>All buffers are unavailable (i.e., have been handed out).
 *    <li>Thread A wants a buffer.
 *    <li>Thread B is using one, but is almost finished with it.
 * </ul>
 *
 * <p>Here's how the deadlock can occur:</p>
 *
 * <ol>
 *    <li>Thread A calls the <tt>getBuffer()</tt> method.
 *
 *    <li>Thread A enters the <tt>synchronized (bufferPool)</tt>
 *        block in <tt>getBuffer</tt>. As a result, Thread A acquires
 *        the Java monitor for the <tt>bufferPool</tt> object. Recall
 *        that only one thread can hold a given object's monitor at a time.
 *
 *    <li>Thread A calls <tt>sem.acquire()</tt>. Since there are no
 *        buffers available, the semaphore's value is 0, so Thread A
 *        goes to sleep <b>while it is still holding the monitor lock
 *        on the <tt>bufferPool</tt> object.</b>
 *
 *    <li>Thread B finishes with its buffer. It calls the
 *        <tt>returnBuffer()</tt> method.
 *
 *    <li>Within <tt>returnBuffer()</tt>, Thread B attempts to acquire
 *        the monitor lock for the <tt>bufferPool</tt> object--but
 *        Thread A is already holding that lock, so Thread B goes to sleep,
 *        waiting for Thread A to release the lock.
 *
 *    <li>Deadlock. Thread A is waiting for Thread B to release the semaphore.
 *        Thread B, in turn, is waiting for Thread A to release the lock
 *        on the <tt>bufferPool</tt> object.
 * </ol>
 *
 * <p>This particular deadlock situation is easily avoided as shown:</p>
 *
 * <blockquote>
 * <pre>
 *
 * private ArrayList bufferPool = ...;
 * private Semaphore sem = new ObjectLockSemaphore (bufferPool.size());
 *
 * public MyBuffer getBuffer()
 * {
 *     MyBuffer result = null;
 *
 *     sem.acquire();
 *     synchronized (bufferPool)
 *     {
 *         result = (MyBuffer) bufferPool.removeElementAt (0);
 *     }
 *
 *     return result;
 * }
 *
 * public void returnBuffer (MyBuffer buf)
 * {
 *     synchronized (bufferPool)
 *     {
 *                                       // bufferPool is now locked
 *         bufferPool.addElement (buf);
 *     }
 *
 *     sem.release();                    // bufferPool is still locked
 * }
 * </pre>
 * </blockquote>
 *
 * @deprecated J2SE 5.0 now provides a <tt>java.util.concurrent.Semaphore</tt> class
 */
public class ObjectLockSemaphore implements Semaphore
{
    /*----------------------------------------------------------------------*\
                           Private Data Elements
    \*----------------------------------------------------------------------*/

    /**
     * Current count.
     */
    private int count = 0;

    /*----------------------------------------------------------------------*\
                                Constructor
    \*----------------------------------------------------------------------*/

    /**
     * Allocate a new semaphore with the specified initial count.
     *
     * @param initialCount  Initial semaphore count.
     */
    public ObjectLockSemaphore (int initialCount)
    {
        count = initialCount;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Acquire this semaphore. If the semaphore isn't available, the
     * current thread is put to sleep until either (a) the semaphore is
     * available, or (b) the timeout period expires.
     *
     * @param timeout  Timeout period, in milliseconds. A value of 0 means
     *                 "wait forever, until the semaphore is available." A
     *                 negative value means "return immediately if the
     *                 semaphore is not available."
     *
     * @return <tt>true</tt> if the semaphore was successfully acquired,
     *         <tt>false</tt> if the timeout expired.
     *
     * @throws SemaphoreException error attempting to acquire semaphore
     *
     * @see #acquire()
     */
    public boolean acquire (long timeout)
        throws SemaphoreException
    {
        boolean acquired = false;

        synchronized (this)
        {
            if (count > 0)
                acquired = true;

            else if (timeout == 0)
            {
                waitForever();
                acquired = true;
            }

            else if (timeout > 0)
            {
                // Have to wait for it to become available.

                acquired = waitOrTimeOut (timeout);
            }

            if (acquired)
                count--;

            notifyAll();
        }

        return acquired;
    }

    /**
     * Acquire this semaphore. If the semaphore isn't available, this
     * method waits forever for the semaphore to become available. Calling
     * this version of <tt>acquire()</tt> is exactly equivalent to calling
     * {@link #acquire(long)} with a timeout value of 0.
     *
     * @throws SemaphoreException error attempting to acquire semaphore
     *
     * @see #acquire(long)
     */
    public boolean acquire()
        throws SemaphoreException
    {
        return acquire (0);
    }

    /**
     * Increment the semaphore's current value, as well as its maximum value.
     * This method is useful in cases where the semaphore is controlling
     * access to multiple instances of a resource (e.g, database connections,
     * file descriptors, etc.), and more instances of the controlled resource
     * have become available.
     *
     * @param delta  The amount by which to increment the count.
     *
     * @throws SemaphoreException error updating semaphore's count
     */
    public synchronized void addToCount (int delta)
        throws SemaphoreException
    {
        count += delta;
        notifyAll();
    }

    /**
     * Get the semaphore's current value (i.e., its count).
     *
     * @return the current value of the semaphore
     *
     * @throws SemaphoreException error getting semaphore's value
     */
    public synchronized int getValue()
        throws SemaphoreException
    {
        return count;
    }

    /**
     * Release this semaphore, incrementing its counter.
     *
     * @throws SemaphoreException error getting semaphore's value
     */
    public synchronized void release()
        throws SemaphoreException
    {
        count++;
        notifyAll();
    }

    /**
     * Return a string representation of the semaphore.
     *
     * @return A printable representation of the semaphore.
     */
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append ("Semaphore[");
        buf.append (Integer.toHexString (hashCode()));
        buf.append (", value=");
        buf.append (String.valueOf (count));
        buf.append (']');

        return buf.toString();
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Wait forever for the semaphore to become available. This method must
     * be called from within a block that's synchronized on the semaphore.
     */
    private void waitForever()
    {
        boolean available = false;

        while (! available)
        {
            try
            {
                wait();
                if (count > 0)
                    available = true;
            }

            catch (InterruptedException ex)
            {
            }
        }
    }

    /**
     * Wait for the semaphore to become available, or until the specified
     * timeout expires. The timeout must be positive. This method must
     * be called from within a block that's synchronized on this object.
     *
     * @param timeout  The timeout.
     *
     * @return <tt>true</tt> if the semaphore is available,
     *         <tt>false</tt> if the timeout expired. <b>This method
     *         does not modify the semaphore's counter value.</b>
     */
    private boolean waitOrTimeOut (long timeout)
    {
        boolean  available = false;
        Date     start     = new Date();
        long     elapsed   = 0;

        while ( (! available) && (elapsed < timeout) )
        {
            // Wait until timeout or until we're notified. Note that we can
            // be awakened even though the timeout hasn't expired. Since
            // many threads can be waiting for the same notification, we
            // have to check for the semaphore's availability every time we
            // awaken.

            try
            {
                wait (timeout);
            }

            catch (InterruptedException ex)
            {
            }

            Date end  = new Date();
            elapsed  += end.getTime() - start.getTime();

            // Awake. Did the timeout occur, or do we have the semaphore?

            if (count > 0)
                available = true;
        }

        return available;
    }
}
