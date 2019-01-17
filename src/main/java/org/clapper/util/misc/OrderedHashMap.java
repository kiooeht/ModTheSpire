package org.clapper.util.misc;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>An <tt>OrderedHashMap</tt> is a <tt>java.util.HashMap</tt> with one
 * additional property: It maintains a list of the map's keys in the order
 * they were added to the map. This additional capability imposes a small
 * amount of extra overhead on insertion and a larger amount of overhead on
 * key removal, but absolutely no additional key lookup overhead. This
 * class is conceptually similar to (though less feature-rich than) the
 * <tt>java.util.LinkedHashMap</tt> class added in JDK 1.4.</p>
 *
 * @deprecated Please use java.util.LinkedHashMap, instead
 *
 * @see #keysInInsertionOrder
 * @see #getKeysInInsertionOrder
 */
public class OrderedHashMap<K,V>
    extends HashMap<K,V>
    implements Cloneable, Serializable
{
    /*----------------------------------------------------------------------*\
                         Private Static Variables
    \*----------------------------------------------------------------------*/

    /**
     * See JDK 1.5 version of java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /*----------------------------------------------------------------------*\
                             Private Variables
    \*----------------------------------------------------------------------*/

    private LinkedList<K> keysInOrder = new LinkedList<K>();

    /*----------------------------------------------------------------------*\
                                Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Construct a new empty map with a default capacity and load factor.
     */
    public OrderedHashMap()
    {
        super();
    }

    /**
     * Construct a new empty map with the specified initial capacity and
     * a default load factor.
     *
     * @param initialCapacity  the initial capacity
     */
    public OrderedHashMap (int initialCapacity)
    {
        super (initialCapacity);
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and
     * the specified load factor.
     *
     * @param initialCapacity  the initial capacity
     * @param loadFactor       the load factor
     */
    public OrderedHashMap (int initialCapacity, float loadFactor)
    {
        super (initialCapacity, loadFactor);
    }

    /**
     * Constructs a new map with the same mappings as the given map. The
     * initial capacity and load factor is the same as for the parent
     * <tt>HashMap</tt> class. Since there's no way of knowing in what
     * order entries were made in the original map, the order imposed on
     * them in this map is the order in which they are returned by the
     * <tt>keySet()</tt> method.
     *
     * @param map  the map whose mappings are to be copied
     *
     * @see #OrderedHashMap(OrderedHashMap)
     */
    public OrderedHashMap (Map<? extends K, ? extends V> map)
    {
        super (map);
        keysInOrder.addAll (map.keySet());
    }

    /**
     * Constructs a new map with the same mappings as the given
     * <tt>OrderedHashMap</tt>. The initial capacity and load factor is the
     * same as for the parent <tt>HashMap</tt> class. The insertion order
     * of the keys is preserved.
     *
     * @param map  the map whose mappings are to be copied
     */
    public OrderedHashMap (OrderedHashMap<? extends K, ? extends V> map)
    {
        super (map);
        keysInOrder.addAll (map.keysInOrder);
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Get the keys in the order they were added to this map.
     *
     * @param list  the <tt>List</tt> to receive the keys
     *
     * @return the number of keys added to the list
     *
     * @see #keysInInsertionOrder
     */
    public int getKeysInInsertionOrder (List<? super K> list)
    {
        // Casting to Collection removes a stupid JDK 1.6-beta compiler
        // complaint. Doing this:
        //
        // list.addAll(keysInOrder)
        //
        // results in this compiler error:
        //
        // reference to addAll is ambiguous, both method 
        // addAll(java.util.Collection<? extends E>) in 
        // java.util.Collection<capture#142 of ? super K> and method 
        // addAll(java.util.Collection<? extends E>) in 
        // java.util.List<capture#142 of ? super K> match
        //
        // This problem is new with 1.6. Casting solves the problem.

        ((Collection<? super K>) list).addAll(keysInOrder);

        return keysInOrder.size();
    }

    /**
     * Get the keys in the order they were added to this map.
     *
     * @return a new <tt>List</tt> containing the keys in insertion order
     *
     * @see #getKeysInInsertionOrder
     */
    public List<K> keysInInsertionOrder()
    {
        return new ArrayList<K> (keysInOrder);
    }

    /**
     * Returns a shallow copy of this instance. The keys and values themselves
     * are not cloned.
     *
     * @return a shallow copy of this map
     */
    public Object clone()                                            // NOPMD
    {
        return new OrderedHashMap<K,V> (this);
    }

    /**
     * Remove all mappings from this map.
     */
    public void clear()
    {
        super.clear();
        keysInOrder.clear();
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
        V oldValue = super.put (key, value);

        keysInOrder.remove (key);
        keysInOrder.add (key);

        return oldValue;
    }

    /**
     * Copies all of the mappings from a specified map to this one. These
     * mappings replace any mappings that this map had for any of the keys
     * in this map. Since there's no way of knowing in what order entries
     * were made in the original map, they are appended to the key
     * insertion order list in the order they are returned by the map's
     * <tt>keySet()</tt> method.
     *
     * @param map  the map whose mappings are to be copied
     *
     * @see #putAll(OrderedHashMap)
     */
    public void putAll (Map<? extends K, ? extends V> map)
    {
        for (Iterator<? extends K> it = map.keySet().iterator();
             it.hasNext(); )
        {
            K key = it.next();
            V value = map.get (key);

            this.put (key, value);
        }
    }


    /**
     * Copies all of the mappings from a specified map to this one. These
     * mappings replace any mappings that this map had for any of the keys
     * in this map. The keys are appended to this map's key insertion order
     * list in the order they were originally added to <tt>map</tt>.
     *
     * @param map  the map whose mappings are to be copied
     *
     * @see #putAll(OrderedHashMap)
     * @see #keysInInsertionOrder
     */
    public void putAll (OrderedHashMap<? extends K, ? extends V> map)
    {
        for (Iterator<? extends K> it = map.keysInInsertionOrder().iterator();
             it.hasNext(); )
        {
            K key = it.next();
            V value = map.get (key);

            this.put (key, value);
        }
    }

    /**
     * Removes the mapping for a key, if there is one. The key is also removed
     * from the key insertion order list.
     *
     * @param key   the key to remove
     *
     * @return the previous value associated with the key, or null if (a) there
     *         was no previous value, or (b) the previous value was a null
     */
    public V remove (Object key)
    {
        V oldValue = super.remove (key);

        keysInOrder.remove (key);

        return oldValue;
    }
}
