package org.clapper.util.misc;

import java.io.Serializable;

/**
 * Stores the location of an on-disk serialized value. Used by
 * <tt>FileHashMap</tt> for its in-memory index and returned sets and
 * iterators. This class is not publicly accessible. It's stored in a
 * separate file, because inner classes cannot be serialized.
 */
class FileHashMapEntry<K>
    implements Serializable, Comparable<FileHashMapEntry>
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The file position.
     */
    private long filePosition = -1;

    /**
     * The length of the stored, serialized object
     */
    private int objectSize = -1;

    /**
     * The caller's key (i.e., the key the caller of FileHashMap.put()
     * specified).
     */
    private K key = null;

    /*----------------------------------------------------------------------*\
                               Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Create a new <tt>FileHashMapEntry</tt> that records the location
     * and length of an item stored in the data file portion of a
     * <tt>FileHashMap</tt> obejct.
     *
     * @param pos   The object's file position. The object may or may not
     *              actually have been written there yet.
     * @param size  The stored object's serialized size, if known, or -1
     *              if the object has never been written. A non-negative
     *              size value will typically be passed when an existing
     *              <tt>FileHashMap</tt> is being reloaded from disk.
     * @param key   The caller's key (i.e., the key the caller of
     *              <tt>FileHashMap.put()</tt> specified). May be null.
     *
     * @see #getFilePosition
     * @see #getObjectSize
     * @see #setObjectSize
     * @see FileHashMap#put
     */
    FileHashMapEntry (long pos, int size, K key)
    {
        this.filePosition = pos;
        this.objectSize   = size;
        this.key          = key;
    }

    /**
     * Create an entry with no associated key. Used primarily to record
     * file gaps. In that case, the object size is really the gap size.
     *
     * @param pos   The object's file position. The object may or may not
     *              actually have been written there yet.
     * @param size  The gap size.
     *
     * @see #getFilePosition
     * @see #getObjectSize
     * @see #setObjectSize
     */
    FileHashMapEntry (long pos, int size)
    {
        this (pos, size, null);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. The comparison
     * key for a <tt>FileHashMapEntry</tt> is the file position value.
     *
     * @param o  The other object
     */
    public int compareTo (FileHashMapEntry o)
    {
        FileHashMapEntry  other    = (FileHashMapEntry) o;
        Long              thisPos  = new Long (this.filePosition);
        Long              otherPos = new Long (other.filePosition);

        return thisPos.compareTo (otherPos);
    }

    /**
     * Display a string version of the contents of this object. Mostly
     * useful for debugging.
     *
     * @return a string representation of the contents of this object
     */
    public String toString()
    {
        return ("FileHashMapEntry[filePosition=" +
                filePosition +
                ", objectSize=" +
                objectSize +
                ", key=" +
                ((key == null) ? "<null>" : key) +
                "]");
    }

    /*----------------------------------------------------------------------*\
                          Package-visible Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the caller's key (i.e., the key the caller passed to
     * <tt>FileHashMap.put()</tt>).
     *
     * @return the key
     *
     * @see #setKey
     * @see FileHashMap#put
     */
    K getKey()
    {
        return key;
    }

    /**
     * Change the key for this entry
     *
     * @param newKey  the new key to use
     *
     * @see #getKey
     */
    void setKey (K newKey)
    {
        this.key = newKey;
    }

    /**
     * Get the file position for this entry.
     *
     * @return the file position
     *
     * @see #setFilePosition
     */
    long getFilePosition()
    {
        return this.filePosition;
    }

    /**
     * Set the file position for this entry.
     *
     * @param pos the new file position
     *
     * @see #getFilePosition
     */
    void setFilePosition (long pos)
    {
        this.filePosition = pos;
    }

    /**
     * Get the number of bytes the serialized object occupies in the
     * random access file.
     *
     * @return the number of bytes occupied by the object
     *
     * @see #setObjectSize
     */
    int getObjectSize()
        throws IllegalStateException
    {
        assert (this.objectSize > 0) : "No object stored yet";
        return this.objectSize;
    }

    /**
     * Get the number of bytes the serialized object occupies in the
     * random access file.
     *
     * @param size the number of bytes occupied by the object
     *
     * @see #getObjectSize
     */
    void setObjectSize (int size)
    {
        this.objectSize = size;
    }
}
