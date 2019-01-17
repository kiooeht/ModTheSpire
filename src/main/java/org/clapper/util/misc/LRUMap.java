package org.clapper.util.misc;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>An <tt>LRUMap</tt> implements a <tt>Map</tt> of a fixed maximum size
 * that enforces a least recently used discard policy. When the
 * <tt>LRUMap</tt> is full (i.e., contains the maximum number of entries),
 * any attempt to insert a new entry causes one of the least recently used
 * entries to be discarded.</p>
 *
 * <p>Note:</p>
 *
 * <ul>
 *   <li>The <tt>put()</tt> method "touches" (or "refreshes") an object,
 *       making it "new" again, even if it simply replaces the value for
 *       an existing key.
 *   <li>The <tt>get()</tt> method also refreshes the retrieved object,
 *       but the <tt>iterator()</tt>, <tt>containsValue()</tt> and
 *       <tt>containsKey()</tt> methods do not refresh the objects in the
 *       cache.
 *   <li>This implementation is <b>not</b> synchronized. If multiple threads
 *       access this map concurrently, and at least one of the threads
 *       modifies the map structurally, it must be synchronized externally.
 *       Unlike other <tt>Map</tt> implementations, even a simple
 *       <tt>get()</tt> operation structurally modifies an <tt>LRUMap</tt>.
 *       Synchronization can be accomplished by synchronizing on some
 *       object that naturally encapsulates the map. If no such object
 *       exists, the map should be "wrapped" using the
 *       <tt>Collections.synchronizedMap()</tt> method. This is best done
 *       at creation time, to prevent accidental unsynchronized access to
 *       the map:
 *       <pre>Map m = Collections.synchronizedMap (new LRUMap (...));</pre>
 * </ul>
 *
 * <p>There are other, similar implementations. For instance, see the
 * <a href="http://jakarta.apache.org/commons/collections/apidocs/org/apache/commons/collections/LRUMap.html">LRUMap</a>
 * class in the
 * <a href="http://jakarta.apache.org/commons/collections/">Apache Jakarta Commons Collection</a>
 * API. (This leads to the obvious question: Why write another one? The primary
 * answer is that I did not want to add another third-party library dependency.
 * Plus, I wanted to experiment with this algorithm.)</p>
 */
public class LRUMap<K,V>
    extends AbstractMap<K,V>
    implements Cloneable, Serializable
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /**
     * The default load factor, if one isn't specified to the constructor
     */
    public static final float DEFAULT_LOAD_FACTOR      = 0.75f;

    /**
     * The default initial capacity, if one isn't specified to the
     * constructor
     */
    public static final int   DEFAULT_INITIAL_CAPACITY = 16;

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Set of Map.Entry (really, LRULinkedListEntry) objects returned by
     * the LRUMap.entrySet() method.
     */
    private class EntrySet extends AbstractSet<Map.Entry<K,V>>
    {
        private EntrySet()
        {
            // Nothing to do
        }

        public Iterator<Map.Entry<K,V>> iterator()
        {
            return new Iterator<Map.Entry<K,V>>()
            {
                EntryIterator it = new EntryIterator();

                public Map.Entry<K,V> next()
                {
                    return (Map.Entry<K,V>) it.next();
                }

                public boolean hasNext()
                {
                    return it.hasNext();
                }

                public void remove()
                {
                    it.remove();
                }
            };
         }

        public boolean contains (Object o)
        {
            boolean has = false;

            if (o instanceof Map.Entry)
            {
                Map.Entry e = (Map.Entry) o;
                Object key = e.getKey();

                has = LRUMap.this.containsKey (key);
            }

            return has;
        }

        public boolean remove (Object o)
        {
            return (LRUMap.this.remove (o) != null);
        }

        public int size()
        {
            return LRUMap.this.size();
        }

        public void clear()
        {
            LRUMap.this.clear();
        }
    }

    /**
     * Iterator returned by EntrySet.iterator()
     */
    private class EntryIterator implements Iterator<LRULinkedListEntry>
    {
        private LRULinkedListEntry current;

        EntryIterator()
        {
            current = lruQueue.head;
        }

        public LRULinkedListEntry next()
        {
            LRULinkedListEntry result = current;
            current = current.next;
            return result;
        }

        public boolean hasNext()
        {
            return (current != null);
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Set of key objects returned by the LRUMap.keySet() method.
     */
    private class KeySet extends AbstractSet<K>
    {
        private KeySet()
        {
            // Nothing to do
        }

        public Iterator<K> iterator()
        {
            return new KeySetIterator();
        }

        public boolean contains (Object key)
        {
            return LRUMap.this.containsKey (key);
        }

        public boolean remove (Object key)
        {
            return (LRUMap.this.remove (key) != null);
        }

        public int size()
        {
            return LRUMap.this.size();
        }

        public void clear()
        {
            LRUMap.this.clear();
        }
    }

    /**
     * Iterator returned by KeySet.iterator()
     */
    private class KeySetIterator implements Iterator<K>
    {
        private LRULinkedListEntry current;

        KeySetIterator()
        {
            current = lruQueue.head;
        }

        public K next()
        {
            LRULinkedListEntry result = current;
            current = current.next;
            return result.key;
        }

        public boolean hasNext()
        {
            return (current != null);
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

   /**
     * Shallow set that implements a set of values backed by the map.
     */
    private class ValueSet extends AbstractSet<V>
    {
        private ValueSet()
        {
            // Nothing to do
        }

        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        public boolean contains (Object o)
        {
            return LRUMap.this.containsValue(o);
        }

        public boolean containsAll (Collection c)
        {
            boolean contains = true;

            for (Object o : c)
            {
                if (! contains(o))
                {
                    contains = false;
                    break;
                }
            }

            return contains;
        }

        public boolean isEmpty()
        {
            return LRUMap.this.isEmpty();
        }

        public Iterator<V> iterator()
        {
            return new ValueSetIterator();
        }

        public boolean remove (Object o)
        {
            throw new UnsupportedOperationException();
        }

        public int size()
        {
            return LRUMap.this.size();
        }
    }

    /**
     * Iterator returned by ValueSet.iterator()
     */
    private class ValueSetIterator implements Iterator<V>
    {
        private LRULinkedListEntry current;

        ValueSetIterator()
        {
            current = lruQueue.head;
        }

        public V next()
        {
            LRULinkedListEntry result = current;
            current = current.next;
            return result.value;
        }

        public boolean hasNext()
        {
            return (current != null);
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Entry in the internal linked list (queue) of LRU entries. Implements
     * Map.Entry for convenience.
     */
    private final class LRULinkedListEntry implements Map.Entry<K,V>
    {
        LRULinkedListEntry  previous = null;
        LRULinkedListEntry  next     = null;
        K                   key      = null;
        V                   value    = null;

        LRULinkedListEntry (K key, V value)
        {
            setKeyValue (key, value);
        }

        public boolean equals (Object o)
        {
            return LRULinkedListEntry.class.isInstance (o);
        }

        public int hashCode()
        {
            return key.hashCode();
        }

        public K getKey()
        {
            return key;
        }

        public V getValue()
        {
            return value;
        }

        void setKeyValue (K key, V value)
        {
            this.key   = key;
            this.value = value;
        }

        public V setValue (V value)
        {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    /**
     * Internal linked list that implements the LRU queue. Each entry in the
     * hash map of objects also points to one of these, allowing the hash map
     * to remain untouched even as the linked list entries get reordered.
     */
    private class LRULinkedList
    {
        LRULinkedListEntry  head = null;
        LRULinkedListEntry  tail = null;
        int                 size = 0;

        private LRULinkedList()
        {
            // Nothing to do
        }

        protected void finalize()
            throws Throwable
        {
            clear();
            super.finalize();
        }

        void addToTail (LRULinkedListEntry entry)
        {
            entry.next = null;
            entry.previous = tail;

            if (head == null)
            {
                head = entry;
                tail = entry;
            }

            else
            {
                entry.previous = tail;
                tail.next = entry;
            }

            size++;
        }

        void addToHead (LRULinkedListEntry entry)
        {
            entry.next = null;
            entry.previous = null;

            if (head == null)
            {
                assert (tail == null);
                head = entry;
                tail = entry;
            }

            else
            {
                entry.next = head;
                head.previous = entry;
                head = entry;
            }

            size++;
        }

        void remove (LRULinkedListEntry entry)
        {
            if (entry.next != null)
                entry.next.previous = entry.previous;

            if (entry.previous != null)
                entry.previous.next = entry.next;

            if (entry == head)           // NOPMD (legal reference comparison)
                head = entry.next;

            if (entry == tail)           // NOPMD (legal reference comparison)
                tail = entry.previous;

            entry.next = null;
            entry.previous = null;

            size--;
            assert (size >= 0);
        }

        LRULinkedListEntry removeTail()
        {
            LRULinkedListEntry result = tail;

            if (result != null)
                remove (result);

            return result;

        }

        void moveToHead (LRULinkedListEntry entry)
        {
            remove (entry);
            addToHead (entry);
        }

        void clear()
        {
            while (head != null)
            {
                LRULinkedListEntry next = head.next;

                head.next = null;
                head.previous = null;
                head.key = null;
                head.value = null;

                head = next;
            }

            tail = null;
            size = 0;
        }
    }

    /**
     * Wraps any ObjectRemovalListener passed into addRemovalListener().
     * Keeps track of both the listener and its "automaticOnly" status
     */
    private static class RemovalListenerWrapper
        implements ObjectRemovalListener
    {
        boolean                automaticOnly;
        ObjectRemovalListener  realListener;

        RemovalListenerWrapper (ObjectRemovalListener realListener,
                                boolean               automaticOnly)
        {
            this.realListener  = realListener;
            this.automaticOnly = automaticOnly;
        }

        public void objectRemoved (ObjectRemovalEvent event)
        {
            realListener.objectRemoved (event);
        }
    }

    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                               Type Aliases
    \*----------------------------------------------------------------------*/

    /**
     * Type alias
     */
    private class ListenerMap extends HashMap<ObjectRemovalListener,
                                              RemovalListenerWrapper>
    {
        ListenerMap()
        {
            super();
        }
    }

    /**
     * Type alias for actual hash table
     */
    private class EntryMap extends HashMap<K,LRULinkedListEntry>
    {
        EntryMap (int initialCapacity, float loadFactor)
        {
            super (initialCapacity, loadFactor);
        }
    }

    /*----------------------------------------------------------------------*\
                             Private Variables
    \*----------------------------------------------------------------------*/

    private int            maxCapacity;
    private float          loadFactor;
    private int            initialCapacity;
    private EntryMap       hash;
    private LRULinkedList  lruQueue;
    private ListenerMap    removalListeners = null;

    /*----------------------------------------------------------------------*\
                                Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new empty map with a default capacity and load factor,
     * and the specified maximum capacity.
     *
     * @param maxCapacity the maximum number of entries permitted in the
     *                    map. Must not be negative.
     */
    public LRUMap (int maxCapacity)
    {
        this (DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, maxCapacity);
    }

    /**
     * Construct a new empty map with the specified initial capacity,
     * a default load factor, and the specified maximum capacity.
     *
     * @param initialCapacity  the initial capacity
     * @param maxCapacity      the maximum number of entries permitted in the
     *                         map. Must not be negative.
     */
    public LRUMap (int initialCapacity, int maxCapacity)
    {
        this (initialCapacity, DEFAULT_LOAD_FACTOR, maxCapacity);
    }

    /**
     * Constructs a new, empty map with the specified initial capacity,
     * load factor, and maximum capacity.
     *
     * @param initialCapacity  the initial capacity
     * @param loadFactor       the load factor
     * @param maxCapacity      the maximum number of entries permitted in the
     *                         map. Must not be negative.
     */
    public LRUMap (int initialCapacity, float loadFactor, int maxCapacity)
    {
        assert (maxCapacity > 0);
        assert (loadFactor > 0.0);
        assert (initialCapacity > 0);

        if (initialCapacity > maxCapacity)
            initialCapacity = maxCapacity;

        this.maxCapacity     = maxCapacity;
        this.loadFactor      = loadFactor;
        this.initialCapacity = initialCapacity;
        this.hash            = new EntryMap (initialCapacity, loadFactor);
        this.lruQueue        = new LRULinkedList();
    }

    /**
     * Constructs a new map with the same mappings and parameters as the
     * given <tt>LRUMap</tt>. The initial capacity and load factor is
     * the same as for the parent <tt>HashMap</tt> class. The insertion
     * order of the keys is preserved.
     *
     * @param map  the map whose mappings are to be copied
     */
    public LRUMap (LRUMap<? extends K, ? extends V> map)
    {
        this (map.initialCapacity, map.loadFactor, map.maxCapacity);
        doPutAll (map);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * <p>Add an <tt>EventListener</tt> that will be called whenever an
     * object is removed from the cache. If <tt>automaticOnly</tt> is
     * <tt>true</tt>, then the listener is only notified for objects that
     * are removed automatically when the cache needs to be cleared to make
     * room for new objects. If <tt>automaticOnly</tt> is <tt>false</tt>,
     * then the listener is notified whenever an object is removed for any
     * reason, include a call to the {@link #remove remove()} method.</p>
     *
     * <p>Note that when this map announces the removal of an object, it
     * passes an {@link ObjectRemovalEvent} that contains a
     * <tt>java.util.Map.Entry</tt> object that wraps the actual object
     * that was removed. That way, both the key and the value are available
     * for the removed object.</p>
     *
     * @param listener      the listener to add
     * @param automaticOnly see above
     *
     * @see #removeRemovalListener
     */
    public synchronized void
    addRemovalListener (ObjectRemovalListener listener, boolean automaticOnly)
    {
        if (removalListeners == null)
            removalListeners = new ListenerMap();

        removalListeners.put (listener,
                              new RemovalListenerWrapper (listener,
                                                          automaticOnly));
    }

    /**
     * Remove an <tt>EventListener</tt> from the set of listeners to be invoked
     * when an object is removed from the cache.
     *
     * @param listener the listener to add
     *
     * @return <tt>true</tt> if the listener was in the list and was removed,
     *         <tt>false</tt> otherwise
     *
     * @see #addRemovalListener
     */
    public synchronized boolean
    removeRemovalListener (ObjectRemovalListener listener)
    {
        boolean removed = false;

        if ((removalListeners != null) &&
            (removalListeners.remove(listener) != null))
        {
            removed = true;
        }

        return removed;
    }

    /**
     * Remove all mappings from this map.
     */
    public void clear()
    {
        hash.clear();
        lruQueue.clear();
    }

    /**
     * Determine whether this map contains a mapping for a given key. Note
     * that this implementation of <tt>containsKey()</tt> does not refresh
     * the object in the cache.
     *
     * @param key  the key to find
     *
     * @return <tt>true</tt> if the key is in the map, <tt>false</tt> if not
     */
    public boolean containsKey (Object key)
    {
        return hash.containsKey (key);
    }

    /**
     * Determine whether this map contains a given value. Note that this
     * implementation of <tt>containsValue()</tt> does not refresh the
     * objects in the cache.
     *
     * @param value the value to find
     *
     * @return <tt>true</tt> if the value is in the map, <tt>false</tt> if not
     */
    public boolean containsValue (Object value)
    {
        boolean contains = false;
        for (LRULinkedListEntry entry : hash.values())
        {
            if (entry.getValue().equals(value))
            {
                contains = true;
                break;
            }
        }

        return contains;
    }

    /**
     * Get a set view of the mappings in this map. Each element in this set
     * is a <tt>Map.Entry</tt>. The collection is backed by the map, so
     * changes to the map are reflected in the collection, and vice-versa.
     * The collection supports element removal, which removes the
     * corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return the entry set
     */
    public Set<Map.Entry<K,V>> entrySet()
    {
        return new EntrySet();
    }

    /**
     * Retrieve an object from the map. Retrieving an object from an
     * LRU map "refreshes" the object so that it is among the most recently
     * used objects.
     *
     * @param key  the object's key in the map.
     *
     * @return the associated object, or null if not found
     */
    public V get (Object key)
    {
        V                   value = null;
        LRULinkedListEntry  entry  = (LRULinkedListEntry) hash.get (key);

        if (entry != null)
        {
            // It's there. It's just been accessed, so move it to the
            // top of the linked list.

            assert (entry.key.equals (key)) :
                   "entry.key=" + entry.key + ", key=" + key;

            lruQueue.moveToHead (entry);
            value = entry.value;
        }

        return value;
    }

    /**
     * Get the initial capacity of this <tt>LRUMap</tt>.
     *
     * @return the initial capacity, as passed to the constructor
     *
     * @see #getLoadFactor
     * @see #getMaximumCapacity
     */
    public int getInitialCapacity()
    {
        return initialCapacity;
    }

    /**
     * Get the load factor for this <tt>LRUMap</tt>.
     *
     * @return the load factor, as passed to the constructor
     *
     * @see #getInitialCapacity
     * @see #getMaximumCapacity
     */
    public float getLoadFactor()
    {
        return loadFactor;
    }

    /**
     * Get the maximum capacity of this <tt>LRUMap</tt>.
     *
     * @return the maximum capacity, as passed to the constructor
     *
     * @see #setMaximumCapacity
     * @see #getLoadFactor
     * @see #getInitialCapacity
     */
    public int getMaximumCapacity()
    {
        return maxCapacity;
    }

    /**
     * Determine whether this map is empty or not.
     *
     * @return <tt>true</tt> if the map has no mappings, <tt>false</tt>
     *          otherwise
     */
    public boolean isEmpty()
    {
        return hash.isEmpty();
    }

    /**
     * <p>Return a <tt>Set</tt> view of the keys in the map. The set is
     * backed by the map, so changes to the map are reflected in the set,
     * and vice-versa. The set does supports element removal, which removes
     * the corresponding mapping from this map; however, the
     * <tt>Iterator</tt> returned by the set currently does <b>not</b>
     * support element removal. The set does not support the <tt>add</tt>
     * or <tt>addAll</tt> operations.
     *
     * @return the set of keys in this map
     */
    public Set<K> keySet()
    {
        return new KeySet();
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the key already has a value in this map, the existing value is
     * replaced by the new value, and the old value is replaced. If the key
     * already exists in the map, it is moved to the end of the key
     * insertion order list.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to associate with the specified key
     *
     * @return the previous value associated with the key, or null if (a) there
     *         was no previous value, or (b) the previous value was a null
     */
    public V put (K key, V value)
    {
        return doPut (key, value);
    }

    /**
     * Copies all of the mappings from a specified map to this one. These
     * mappings replace any mappings that this map had for any of the keys.
     *
     * @param map  the map whose mappings are to be copied
     */
    public void putAll (Map<? extends K, ? extends V> map)
    {
        doPutAll(map);
    }

    /**
     * Removes the mapping for a key, if there is one.
     *
     * @param key the key to remove
     *
     * @return the previous value associated with the key, or null if (a) there
     *         was no previous value, or (b) the previous value was a null
     */
    public V remove (Object key)
    {
        V                   value = null;
        LRULinkedListEntry  entry = (LRULinkedListEntry) hash.remove (key);

        if (entry != null)
        {
            value = entry.value;
            lruQueue.remove (entry);

            callRemovalListeners (key, value, false);
        }

        assert (hash.size() == lruQueue.size);

        return value;
    }

    /**
     * Set or change the maximum capacity of this <tt>LRUMap</tt>. If the
     * maximum capacity is reduced to less than the map's current size,
     * then the map is reduced in size by discarding the oldest entries.
     *
     * @param newCapacity  the new maximum capacity
     *
     * @return the old maximum capacity
     *
     * @see #getMaximumCapacity
     */
    public int setMaximumCapacity (int newCapacity)
    {
        assert (newCapacity > 0);

        int oldCapacity = this.maxCapacity;
        clearTo (newCapacity);
        this.maxCapacity = newCapacity;
        return oldCapacity;
    }

    /**
     * Get the number of entries in the map. Note that this value can
     * temporarily exceed the maximum capacity of the map. See the class
     * documentation for details.
     *
     * @return the number of entries in the map
     */
    public int size()
    {
        return lruQueue.size;
    }

    /**
     * <p>Returns a collection view of the values contained in this map. The
     * returned collection is a "thin" view of the values contained in
     * this map. The collection contains proxies for the actual disk-resident
     * values; the values themselves are not loaded until a
     * <tt>Collection</tt> method such as <tt>contains()</tt> is called.</p>
     *
     * <p>The collection is backed by the map, so changes to the map are
     * reflected in the set. If the map is modified while an iteration over
     * the set is in progress, the results of the iteration are undefined.
     * The set does not support any of the <tt>add()</tt> methods.</p>
     *
     * <p><b>Warning:</b>: The <tt>toArray()</tt> methods can be dangerous,
     * since they will attempt to load every value from the data file into
     * an in-memory array.</p>
     *
     * @return a collection view of the values contained in this map.
     *
     * @see #keySet
     * @see #values
     */
    public Collection<V> values()
    {
       return new ValueSet();
    }

    /*----------------------------------------------------------------------*\
                             Protected Methods
    \*----------------------------------------------------------------------*/

    /**
     * Returns a shallow copy of this instance. The keys and values themselves
     * are not cloned.
     *
     * @return a shallow copy of this map
     *
     * @throws CloneNotSupportedException not cloneable
     */
    protected Object clone()
        throws CloneNotSupportedException
    {
        return new LRUMap<K,V> (this);
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    private LRULinkedListEntry clearTo (int size)
    {
        assert (hash.size() == lruQueue.size);
        LRULinkedListEntry oldTail = null;

        while (lruQueue.size > size)
        {
            oldTail = lruQueue.removeTail();

            assert (oldTail != null);

            Object              key = oldTail.key;
            LRULinkedListEntry  rem = (LRULinkedListEntry) hash.remove (key);

            assert (rem != null);
            assert (rem.key == key);

            callRemovalListeners (key, rem.value, true);
        }

        assert (lruQueue.size <= size);
        assert (hash.size() == lruQueue.size);

        return oldTail;
    }

    private synchronized void callRemovalListeners (final Object  key,
                                                    final Object  value,
                                                    boolean       automatic)
    {
        if (removalListeners != null)
        {
            for (Iterator it = removalListeners.values().iterator();
                 it.hasNext(); )
            {
                RemovalListenerWrapper l = (RemovalListenerWrapper) it.next();

                if ((! automatic) && (l.automaticOnly))
                    continue;

                Map.Entry entry = new Map.Entry()
                                  {
                                      public boolean equals (Object o)
                                      {
                                          return false;
                                      }

                                      public Object getKey()
                                      {
                                          return key;
                                      }

                                      public Object getValue()
                                      {
                                          return value;
                                      }

                                      public int hashCode()
                                      {
                                          return key.hashCode();
                                      }

                                      public Object setValue (Object val)
                                      {
                                          return null;
                                      }
                                  };

                l.objectRemoved (new ObjectRemovalEvent (entry));
            }
        }
    }

    /**
     * Actual implementation of putAll(). Extracted to a private method
     * so it can be called from the constructor.
     *
     * @param map  the map from which to extract values
     */
    private void doPutAll(final Map<? extends K, ? extends V> map)
    {
        for (Iterator<? extends K> it = map.keySet().iterator();
             it.hasNext(); )
        {
            K key = it.next();
            V value = map.get (key);

            doPut (key, value);
        }
    }

    private V doPut(final K key, final V value)
    {
        // If the total number of entries is at capacity, then we need to
        // remove one of them to make room. The linked list is a priority
        // queue, of sorts, with least recently used items at the end. So
        // remove the tail entries.

        V                   oldValue = null;
        LRULinkedListEntry  entry    = (LRULinkedListEntry) hash.get (key);

        if (entry == null)
        {
            // Must add a new one. Clear out the cruft. Reuse the last
            // cleared entry, though, rather than allocate a new object.

            entry = clearTo (this.maxCapacity - 1);
            if (entry == null)
                entry = new LRULinkedListEntry (key, value);
            else
                entry.setKeyValue (key, value);

            lruQueue.addToHead (entry);
            hash.put (key, entry);
        }

        else
        {
            // We're replacing the value with a new one. Move the entry to
            // the head of the list.

            oldValue = entry.value;
            entry.value = value;
            lruQueue.moveToHead (entry);
        }

        return oldValue;
    }
}
