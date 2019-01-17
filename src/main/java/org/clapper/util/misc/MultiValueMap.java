package org.clapper.util.misc;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <p><tt>MultivalueMap</tt> implements a hash table that permits multiple
 * values per key. It's very similar to the <tt>MultiValueMap</tt> class
 * provided by the
 * <a href="http://jakarta.apache.org/commons/collections">Jakarta Commons
 * Collections</a> API, except that this class uses Java 5 generics.
 *
 * <p>Any value placed into a <tt>MultivalueMap</tt> must implement
 * <tt>java.lang.Comparable</tt>.</p>
 */
public class MultiValueMap<K,V> extends AbstractMap<K,V> implements Cloneable
{
    /*----------------------------------------------------------------------*\
                           Public Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Used to allocate a new <tt>Collection</tt> for the values associated
     * with a key. Callers may specified their own implementation of this
     * interface to cause a <tt>MultiValueMap</tt> object to use a different
     * <tt>Collection</tt> class other <tt>ArrayList</tt>.
     */
    public interface ValuesCollectionAllocator<V>
    {
        /**
         * Allocate a new <tt>Collection</tt> class for use in storing the
         * values for a key.
         *
         * @return a <tt>Collection</tt> object
         */
        public Collection<V> newValuesCollection();
    }

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    private class MultiValueMapEntry implements Map.Entry<K,V>
    {
        private K key;
        private V value;

        MultiValueMapEntry(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        public boolean equals(Object o)
        {
            boolean eq = false;

            if (o instanceof Map.Entry)
            {
                Map.Entry other = (Map.Entry) o;

                eq = other.getKey().equals(key) &&
                     other.getValue().equals(value);
            }

            return eq;
        }

        public K getKey()
        {
            return key;
        }

        public V getValue()
        {
            return value;
        }

        public int hashCode()
        {
            return keyValueHashCode(key, value);
        }

        public V setValue(V value)
        {
            throw new UnsupportedOperationException();
        }
    }

    private class MultiValueMapEntryIterator
        implements Iterator<Map.Entry<K,V>>
    {
        private Iterator<K> keys = MultiValueMap.this.keySet().iterator();
        private Iterator<V> curValues = null;
        private MultiValueMapEntry lastReturned = null;

        MultiValueMapEntryIterator()
        {
        }

         public boolean hasNext()
         {
             boolean has = (curValues != null) && (curValues.hasNext());

             if (! has)
             {
                 // Values exhausted. Are there any keys left?

                 has = keys.hasNext();
             }

             return has;
         }

         public Map.Entry<K,V> next()
         {
             Map.Entry<K,V> result = null;

             if (! hasNext())
                 throw new NoSuchElementException();

             if ((curValues == null) || (! curValues.hasNext()))
             {
                 // Exhausted the values for this key. Move on to next
                 // key.

                 final K key = keys.next();
                 curValues = MultiValueMap.this.getCollection(key)
                                               .iterator();
                 final V value = curValues.next();
                 lastReturned = new MultiValueMapEntry(key, value);
                 result = lastReturned;
             }

             return result;
         }

         public void remove()
         {
             if (lastReturned == null)
                 throw new IllegalStateException("Nothing to remove");

             MultiValueMap.this.remove(lastReturned.getKey(),
                                       lastReturned.getValue());
         }
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>>
    {
        private EntrySet()
        {
            // Nothing to do
        }

        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Map.Entry<K,V> o)
        {
            return MultiValueMap.this.containsKeyValue(o.getKey(),
                                                       o.getValue());
        }

        public Iterator<Map.Entry<K,V>> iterator()
        {
            return new MultiValueMapEntryIterator();
        }

        public boolean remove(Object o)
        {
            MultiValueMapEntry entry = (MultiValueMapEntry) o;
            return MultiValueMap.this.remove(entry.getKey(), entry.getValue());
        }

        public int size()
        {
            return MultiValueMap.this.size();
        }
    }

    /*----------------------------------------------------------------------*\
                            Private Data Items
    \*----------------------------------------------------------------------*/

    /**
     * The underlying Map where items are really stored.
     */
    private Map<K,Collection<V>> map = null;

    /**
     * The collection values allocator.
     */
    private ValuesCollectionAllocator<V> valuesCollectionAllocator =
        new ValuesCollectionAllocator<V>()
        {
            public Collection<V> newValuesCollection()
            {
                return new ArrayList<V>();
            }
        };

    /*----------------------------------------------------------------------*\
                               Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Constructs a new, empty map with a default capacity and load factor.
     * This class's default load factor is the same as the default load factor
     * for the <tt>java.util.HashMap</tt> class.
     */
    public MultiValueMap()
    {
        this.map = new HashMap<K, Collection<V>>();
    }

    /**
     * Constructs a new, empty map with a default capacity and load factor.
     * This class's default load factor is the same as the default load factor
     * for the <tt>java.util.HashMap</tt> class.
     *
     * @param valuesCollectionAllocator object to use to allocate collections
     *                                  of values for a key.
     */
    public MultiValueMap(ValuesCollectionAllocator<V> valuesCollectionAllocator)
    {
        this.map = new HashMap<K, Collection<V>>();
        this.valuesCollectionAllocator = valuesCollectionAllocator;
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and
     * the specified load factor. Note that the load factor and capacity
     * refer to the number of keys in the table, not the number of values.
     *
     * @param initialCapacity   the initial capacity of the <tt>Map</tt>
     * @param loadFactor        the load factor
     *
     * @throws IllegalArgumentException if the initial capacity is negative,
     *                                  or if the load factor is nonpostive.
     */
    public MultiValueMap(int initialCapacity, float loadFactor)
    {
        this.map = new HashMap<K, Collection<V>>(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and
     * the specified load factor. Note that the load factor and capacity
     * refer to the number of keys in the table, not the number of values.
     *
     * @param initialCapacity           the initial capacity
     * @param loadFactor                the load factor
     * @param valuesCollectionAllocator object to use to allocate collections
     *                                  of values for a key.
     *
     * @throws IllegalArgumentException if the initial capacity is negative,
     *                                  or if the load factor is nonpostive.
     */
    public MultiValueMap(int                          initialCapacity,
                         float                        loadFactor,
                         ValuesCollectionAllocator<V> valuesCollectionAllocator)
    {
        this.map = new HashMap<K, Collection<V>>(initialCapacity, loadFactor);
        this.valuesCollectionAllocator = valuesCollectionAllocator;
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and
     * the default load factor. Note that the load factor and capacity
     * refer to the number of keys in the table, not the number of values.
     *
     * @param initialCapacity   the initial capacity
     *
     * @throws IllegalArgumentException if the initial capacity is negative,
     *                                  or if the load factor is nonpostive.
     */
    public MultiValueMap(int initialCapacity)
    {
        this.map = new HashMap<K, Collection<V>>(initialCapacity);
    }

    /**
     * Construct a new map from the contents of an existing map. The new
     * map is a shallow copy of the existing map.
     *
     * @param otherMap  the map to clone
     */
    public MultiValueMap(MultiValueMap<K,V> otherMap)
    {
        otherMap.makeShallowCopyInto(this);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Removes all mappings from this map.
     */
    public void clear()
    {
        map.clear();
    }

    /**
     * Returns a shallow copy of this <tt>MultivalueMap</tt> instance.
     * The keys and values themselves are not cloned.
     *
     * @throws CloneNotSupportedException never, but it's part of the signature
     */
    public Object clone()                                             // NOPMD
        throws CloneNotSupportedException
    {
        MultiValueMap<K,V> newMap = new MultiValueMap<K,V>();
        makeShallowCopyInto (newMap);
        return newMap;
    }

    /**
     * Returns <tt>true</tt> if this map contains at least one value for
     * the specified key.
     *
     * @param key key whose presence in this map is to be tested
     *
     * @return <tt>true</tt> if this map contains at least one value for the
     *         key, <tt>false</tt> otherwise.
     *
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map.
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *                              does not not permit <tt>null</tt> keys.
     *
     * @see #totalValuesForKey
     */
    public boolean containsKey (Object key)
    {
        return map.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value. The values are compared via their
     * <tt>compareTo()</tt> and/or <tt>equals()</tt> methods, so this method
     * is only useful if the map contains values of the same type.
     *
     * @param value value whose presence in this map is to be tested.
     *
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value, <tt>false</tt> otherwise.
     */
    public boolean containsValue(Object value)
    {
        boolean found = false;
        Iterator<Collection<V>> it = map.values().iterator();

        while ((! found) && it.hasNext())
        {
             Collection<V> values = it.next();

            if (values.contains(value))
                found = true;
        }

        return found;
    }

    /**
     * Returns <tt>true</tt> if this map contains the specified value for
     * the specified key. The values are compared via their
     * <tt>compareTo()</tt> and/or <tt>equals()</tt> methods.
     *
     * @param key   the key
     * @param value the value
     *
     * @return <tt>true</tt> if the set of values for the specified key
     *         contains the specified value
     */
    public boolean containsKeyValue(K key, V value)
    {
        boolean found  = false;
        Collection<V> values = getCollection(key);

        if (values != null)
        {
            for (V possibleValue : values)
            {
                found = value.equals(possibleValue);
                if (found)
                    break;
            }
        }

        return found;
    }

    /**
     * Returns an unmodifiable <tt>Set</tt> view of the mappings contained
     * in this map. Each element in the returned collection is a
     * <tt>Map.Entry</tt>; each <tt>Map.Entry</tt> contains a key and a
     * value. Even though the return value is a <tt>Set</tt>, it will still
     * contain all key/value pairs for a given key. The returned
     * <tt>Set</tt> is backed by this map, so any changes to the map are
     * automatically reflected in the set.
     *
     * @return a <tt>Set</tt> view of the mappings contained in this map
     *
     * @see #keySet
     * @see #values
     */
    public Set<Map.Entry<K,V>> entrySet()
    {
        return new EntrySet();
    }

    /**
     * <p>Compares the specified object with this map for equality. Returns
     * <tt>true</tt> if the given object is also a <tt>MultivalueMap</tt>
     * and the two maps represent the same mappings.
     * maps <tt>t1</tt> and <tt>t2</tt> represent the same mappings if
     * <tt>t1.entrySet().equals(t2.entrySet())</tt>. This ensures that the
     * <tt>equals</tt> method works properly across different
     * implementations of the <tt>Map</tt> interface.</p>
     *
     * <p><b>Warning:</b>: Because this method must compare the actual
     * values stored in the map, and the values in a file, this method can
     * be quite slow.</p>
     *
     * @param o object to be compared for equality with this map.
     *
     * @return <tt>true</tt> if the specified object is equal to this map.
     */
    public boolean equals (Object o)
    {
        boolean eq = false;

        if (o instanceof MultiValueMap)
            eq = ((MultiValueMap) o).entrySet().equals(this.entrySet());

        return eq;
    }

    /**
     * <p>Returns an unmodifiable <tt>Collection</tt> containing all values
     * associated with the the specified key. Returns <tt>null</tt> if the
     * map contains no mapping for this key.</p>
     *
     * @param key key whose associated collection of values is to be returned.
     *
     * @return an unmodifiable <tt>Collection</tt> containing all values
     *         associated with the the specified key, or * <tt>null</tt> if
     *         the map contains no values for this key.
     *
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map.
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *                              does not not permit <tt>null</tt> keys.
     *
     * @see #containsKey
     * @see #getFirstValueForKey
     */
    public Collection<V> getCollection(K key)
    {
        Collection<V> values = map.get(key);

        if (values != null)
            values = Collections.unmodifiableCollection(values);

        return values;
    }

    /**
     * Synonym for {@link #getFirstValueForKey}, required by the <tt>Map</tt>
     * interface.
     *
     * @param key  the key
     *
     * @return the first value for the key, or null if not found
     */
    public V get(Object key)
    {
        V result = null;
        Collection<V> values = map.get(key);

        if (values != null)
            result = values.iterator().next();

        return result;
    }

    /**
     * <p>Returns the first value in the set of values associated with a
     * key. This method is especially useful when you know that there is
     * only a single value associated with the key. Note that "first"
     * does not mean "first value ever associated with the key." Instead,
     * it means "first value in the sorted list of values for the key."</p>
     *
     * @param key key whose associated value is to be returned.
     *
     * @return the first value for the key, where first is defined as above,
     *         or null, if the key has no values
     *
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *                              does not not permit <tt>null</tt> keys.
     *
     * @see #containsKey
     * @see #get
     * @see #totalValuesForKey
     */
    public V getFirstValueForKey(K key)
    {
        V result = null;
        Collection<V> values = map.get(key);

        if (values != null)
        {
            Iterator<V> it = values.iterator();

            if (it.hasNext())
                result = it.next();
        }

        return result;
    }

    /**
     * <p>Returns the hash code value for this map. The hash code of a map
     * is defined to be the sum of the hash codes of each entry in the
     * map's <tt>entrySet()</tt> view. This ensures that
     * <tt>t1.equals(t2)</tt> implies that
     * <tt>t1.hashCode()==t2.hashCode()</tt> for any two maps <tt>t1</tt>
     * and <tt>t2</tt>, as required by the general contract of
     * Object.hashCode.</p>
     *
     * @return the hash code value for this map.
     *
     * @see #equals(Object)
     */
    public int hashCode()
    {
        Set<Map.Entry<K,V>> entries = this.entrySet();
        int result  = 0;

        for (Map.Entry<K,V> entry : entries)
            result |= entry.hashCode();

        return result;
    }

    /**
     * Determine whether the map is empty.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    /**
     * Returns a <tt>Set</tt> containing all the keys in this map.
     *
     * <p>The set is backed by the map, so changes to the map are reflected
     * in the set. If the map is modified while an iteration over the set
     * is in progress, the results of the iteration are undefined. Neither
     * the set nor its associated iterator supports any of the
     * set-modification methods (e.g., <tt>add()</tt>, <tt>remove()</tt>,
     * etc). If you attempt to call any of those methods, the called method
     * will throw an <tt>UnsupportedOperationException</tt>.</p>
     *
     * @return a set view of the keys contained in this map.
     *
     * @see #getValuesForKey
     * @see #values()
     */
    public Set<K> keySet()
    {
        return map.keySet();
    }

    /**
     * <p>Associates the specified value with the specified key in this
     * map. If the map previously contained a mapping for this key, this
     * value is added to the list of values associated with the key. This
     * map class does not permit a null value to be stored.</p>
     *
     * @param key   key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     *
     * @return null, always
     *
     * @throws ClassCastException        if the class of the specified key or
     *                                   value prevents it from being stored
     *                                   in this map.
     * @throws IllegalArgumentException  if some aspect of this key or value
     *                                   prevents it from being stored in this
     *                                   map.
     * @throws NullPointerException      the specified key or value is
     *                                   <tt>null</tt>.
     */
    public V put(K key, V value)
    {
        Collection<V> values = (Collection<V>) map.get(key);

        if (values == null)
        {
            values = valuesCollectionAllocator.newValuesCollection();
            map.put(key, values);
        }

        values.add(value);
        return null;
    }

    /**
     * <p>Copies all of the mappings from the specified <tt>Map</tt> to
     * this map. These mappings will be added to any mappings that this map
     * had for any of the keys currently in the specified map.</p>
     *
     * @param fromMap Mappings to be stored in this map.
     *
     * @throws ClassCastException       if the class of a key or value in the
     *                                  specified map prevents it from being
     *                                  stored in this map.
     * @throws IllegalArgumentException some aspect of a key or value in the
     *                                  specified map prevents it from being
     *                                  stored in this map.
     * @throws NullPointerException     the specified key or value is
     *                                  <tt>null</tt>.
     */
    public void putAll(Map<? extends K,? extends V> fromMap)
    {
        for (K key : fromMap.keySet())
        {
            V value = fromMap.get(key);

            if (value != null)
                this.put(key, value);
        }
    }

    /**
     * <p>Copies all of the mappings from the specified
     * <tt>MultivalueMap</tt> to this map. These mappings will be added to
     * any mappings that this map had for any of the keys currently in the
     * specified map.</p>
     *
     * @param fromMap Mappings to be stored in this map.
     *
     * @throws ClassCastException       if the class of a key or value in the
     *                                  specified map prevents it from being
     *                                  stored in this map.
     * @throws IllegalArgumentException some aspect of a key or value in the
     *                                  specified map prevents it from being
     *                                  stored in this map.
     * @throws NullPointerException     the specified key or value is
     *                                  <tt>null</tt>.
     */
    public void putAll (MultiValueMap<K,V> fromMap)
    {
        for (K key : fromMap.keySet())
        {
            Collection<V> values = fromMap.getCollection(key);
            if (values != null)
            {
                for (V value : values)
                    this.put(key, value);
            }
        }
    }

    /**
     * Assocates all the objects in a <tt>Collection</tt> with a key. This
     * method is equivalent to the following code fragment:
     *
     * <blockquote><pre>
     * for (Iterator it = values.iterator(); it.hasNext(); )
     *    map.put (key, it.next());
     * </pre></blockquote>
     *
     * @param key    the key
     * @param values the collection of values to associate with the key
     */
    public void putAll(K key, Collection<V> values)
    {
        for (V value : values)
            put (key, value);
    }

    /**
     * <p>Removes all mappings for a key from this map, if present.</p>
     *
     * @param key key whose mappings are to be removed from the map.
     *
     * @return <tt>Collection</tt> of values associated with specified key,
     *         or <tt>null</tt> if there was no mapping for key.
     */
    public Collection<V> delete(K key)
    {
        return this.map.remove(key);
    }

    /**
     * Removes a single value from the set of values associated with a
     * key.
     *
     * @param key    the key
     * @param value  the value to find and remove
     *
     * @return <tt>true</tt> if the value was found and removed.
     *         <tt>false</tt> if the value isn't associated with the key.
     */
    public boolean remove(Object key, Object value)
    {
        boolean removed = false;

        synchronized (this)
        {
            Collection<V> values = map.get(key);

            if (values != null)
            {
                removed = values.remove(value);
                if (values.size() == 0)
                    map.remove(key);
            }
        }

        return removed;
    }

    /**
     * <p>Returns the number of key-value mappings in this map. If the map
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.</p>
     *
     * @return the number of key-value mappings in this map.
     */
    public int size()
    {
        int total = 0;

        for (K key : keySet())
        {
            Collection<V> valuesForKey = map.get(key);
            if (valuesForKey != null)
                total += valuesForKey.size();
        }

        return total;
    }

    /**
     * Gets the total number of values mapped to a specific key.
     *
     * @param key  the key to test
     *
     * @return the number of values mapped to the key, or 0 if the key
     *         isn't present in the map.
     *
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *                              does not not permit <tt>null</tt> keys.
     *
     * @see #getValuesForKey
     */
    public int totalValuesForKey(K key)
    {
        int           total  = 0;
        Collection<V> values = getCollection(key);

        if (values != null)
            total = values.size();

        return total;
    }

    /**
     * <p>Returns a collection view of the values contained in this map.
     * The returned collection is a "thin" view of the values contained in
     * this map. If a value is associated with more than one key (as
     * determined by the value's <tt>equals()</tt> method), it will only
     * appear once in the returned <tt>Collection</tt>. The values are
     * sorted (via their <tt>compareTo()</tt> methods) in the returned
     * <tt>Collection</tt>.</p>
     *
     * <p>Warning: Unlike the SDK's <tt>Map</tt> class, the returned
     * <tt>Collection</tt> is <b>not</b> backed by this map; instead, it
     * represents a snapshot of the values in the map. Subsequent changes
     * to this map object are <b>not</b> reflected in the returned
     * <tt>Collection</tt>.</p>
     *
     * @return a collection view of the values contained in this map.
     *
     * @see #keySet
     * @see #getValuesForKey
     */
    public Collection<V> values()
    {
        Collection<V> result = new ArrayList<V>();

        for (K key : keySet())
            result.addAll(getCollection(key));

        return result;
    }

    /**
     * Return an unmodifiable <tt>Collection</tt> of all the values for a
     * specific key.
     *
     * @param key   The key
     *
     * @return an unmodifiable <tt>Collection</tt> of all the values
     *         associated with the key, or <tt>null</tt> if there are no
     *         values associated with the key
     *
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *                              does not not permit <tt>null</tt> keys.
     *
     * @see #keySet()
     * @see #totalValuesForKey
     * @see #values()
     */
    public Collection<V> getValuesForKey (K key)
    {
        Collection<V> values = getCollection(key);

        if (values != null)
            values = Collections.unmodifiableCollection(values);

        return values;
    }

    /**
     * Copy all the values for a specific key into a caller-supplied
     * <tt>Collection</tt>.
     *
     * @param key    The key
     * @param values The <tt>Collection</tt> to receive the values
     *
     * @return the number of values copied to the collection
     *
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *                              does not not permit <tt>null</tt> keys.
     *
     * @see #keySet()
     * @see #totalValuesForKey
     * @see #values()
     */
    public int getValuesForKey(K key, Collection<V> values)
    {
        Collection<V> valuesForKey = map.get (key);
        int           total        = 0;

        if (valuesForKey != null)
        {
            values.addAll(valuesForKey);
            total = valuesForKey.size();
        }

        return total;
    }

    /*----------------------------------------------------------------------*\
                              Private Methods
    \*----------------------------------------------------------------------*/

    /**
     * Calculate a combined hash code for a key/value pair.
     *
     * @param key   the key
     * @param value the value
     *
     * @return the hash code
     */
    private int keyValueHashCode (Object key, Object value)
    {
        // Put the string representations of the key and value together,
        // with a delimiter that's unlikely to be in the string
        // representation, and use that string's hash code. If the value
        // is null, use an unlikely placeholder.

        if (value == null)
            value = "\u0002";

        return (new String (key.toString() + "\u0001" + value.toString()))
               .hashCode();
    }

    /**
     * Create a shallow copy of this map into another, existing (presumably
     * empty) map.
     *
     * @param otherMap  the other map to receive the values
     */
    private void makeShallowCopyInto (MultiValueMap<K,V> otherMap)
    {
        for (K key : map.keySet())
        {
            // Copy the collection, though, don't just pass a reference to
            // the same one.

            Collection<V> values = this.map.get(key);
            Collection<V> newValues =
                this.valuesCollectionAllocator.newValuesCollection();

            newValues.addAll(values);
            otherMap.map.put(key, newValues);
        }
    }
}
