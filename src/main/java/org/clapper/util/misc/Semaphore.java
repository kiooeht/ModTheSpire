package org.clapper.util.misc;

/**
 * <p><b>NOTE: This interface, and its subclasses, are deprecated. With
 * J2SE 5.0 (a.k.a., Java 1.5), the JDK provides its own semaphore class,
 * <tt>java.util.concurrent.Semaphore</tt>.</b></p>
 *
 * <p>The <tt>Semaphore</tt> interface specifies a classic counting
 * semaphore. This interface can be implemented in a variety of ways, including
 * (but not limited to) the following:</p>
 *
 * <ul>
 *   <li> using the Java object-locking primitives (see, for instance,
 *        the {@link ObjectLockSemaphore})
 *   <li> using the Java Native Interface (JNI) to access an underlying
 *        operating system semaphore primitive (e.g., the System V semaphore
 *        service on a Unix system)
 * </ul>
 *
 * <p>The following description of a semaphore is paraphrased from <i>An
 * Introduction to Operating Systems</i>, by Harvey M. Deitel
 * (Addison-Wesley, 1983, p. 88):</p>
 *
 * <blockquote>
 *
 * <p>"[Edsgar] Dijkstra developed the concept of semaphores ... as an aid
 * to synchronizing processes. A semaphore is a variable that can be
 * operated upon only by the synchronizing primitives, P and V (letters
 * corresponding to words in Dijkstra's native language, Dutch) defined as
 * follows:</p>
 *
 * <ul>
 *     <li> P(S): wait for S to become greater than zero and then
 *          subtract 1 from S and proceed
 *     <li> V(S): add 1 to S and then proceed
 * </ul>
 *
 * <p>"P allows a process to block itself voluntarily while it waits for
 * an event to occur. V allows another process to wake up a blocked
 * process. Each of these operations must be performed indivisibly; that
 * is, the are not interruptible. The V-operation cannot block the process
 * that performs it."</p>
 *
 * </blockquote>
 *
 * <p>Within this <tt>Semaphore</tt> class, the P operation corresponds to
 * the {@link #acquire()} method, and the V operation corresponds to the
 * {@link #release()} method.</p>
 *
 * <p>In other programming languages, one common use for a semaphore is to
 * lock a critical section. For example, a semaphore is often used to
 * synchronize access to a data structure that's shared between one or more
 * processes or threads. Since the Java language has built in support for
 * interthread synchronization (via the <tt>synchronized</tt> keyword),
 * semaphores are not needed for that purpose in Java.</p>
 *
 * <p>However, semaphores <i>are</i> still useful in some scenarios.
 * Consider the case where you have a fixed-size pool of shared buffers to
 * be shared between all running threads. For whatever reason, you cannot
 * allocate more buffers; you're stuck with the fixed number. How do you
 * control access to the buffers when there are more threads than there are
 * buffers? A semaphore makes this job much easier:</p>
 *
 * <ul>
 *   <li>Create a semaphore that's associated with the buffer pool.
 *   <li>Initialize the semaphore's counter to the number of buffers in the
 *       pool.
 *   <li>Whenever a thread wants a buffer, it must first acquire the semaphore.
 *   <li>Whenever a thread releases a buffer back to the pool, it must also
 *       release the semaphore.
 * </ul>
 *
 * <p>If a thread attempts to allocate a buffer from the pool, and there is
 * at least one buffer in the pool, the thread's attempt to acquire the
 * semaphore will succeed, and it can safely get the buffer. However, if there
 * are no buffers in the pool, the buffer pool semaphore will be 0, and the
 * thread will have to wait until (a) a buffer is returned to the pool,
 * which will "kick" the semaphore and awaken the thread, or (b) the
 * semaphore's <tt>acquire()</tt> method times out.</p>
 *
 * @deprecated J2SE 5.0 now provides a <tt>java.util.concurrent.Semaphore</tt> class
 */
public interface Semaphore
{
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
        throws SemaphoreException;

    /**
     * Acquire this semaphore. If the semaphore isn't available, this
     * method waits forever for the semaphore to become available. Calling
     * this version of <tt>acquire()</tt> is exactly equivalent to calling
     * {@link #acquire(long)} with a timeout value of 0.
     *
     * @return <tt>true</tt> if the semaphore was successfully acquired,
     *         <tt>false</tt> if the timeout expired.
     *
     * @throws SemaphoreException error attempting to acquire semaphore
     *
     * @see #acquire(long)
     */
    public boolean acquire()
        throws SemaphoreException;

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
    public void addToCount (int delta)
        throws SemaphoreException;

    /**
     * Get the semaphore's current value (i.e., its count).
     *
     * @return the current value of the semaphore
     *
     * @throws SemaphoreException error getting semaphore's value
     */
    public int getValue()
        throws SemaphoreException;

    /**
     * Release this semaphore, incrementing its counter.
     *
     * @throws SemaphoreException error getting semaphore's value
     */
    public void release()
        throws SemaphoreException;
}
