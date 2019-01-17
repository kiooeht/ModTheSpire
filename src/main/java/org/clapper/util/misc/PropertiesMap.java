package org.clapper.util.misc;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Properties;

import java.io.Serializable;

/**
 * An <tt>PropertiesMap</tt> provides a thin <tt>Map&lt;String,String&gt;</tt>
 * wrapper around a <tt>java.util.Properties</tt> object, allowing the
 * <tt>Properties</tt> object to be used where a type-safe <tt>Map</tt> is
 * expected. (One such place is with a
 * {@link org.clapper.util.text.VariableSubstituter} class.)
 *
 * @see java.util.Properties
 */
public class PropertiesMap
    extends AbstractMap<String,String>
    implements Serializable
{
    /*----------------------------------------------------------------------*\
                             Public Constants
    \*----------------------------------------------------------------------*/

    /*----------------------------------------------------------------------*\
                               Inner Classes
    \*----------------------------------------------------------------------*/

    /**
     * Entry in the internal linked list (queue) of LRU entries. Implements
     * Map.Entry for convenience.
     */
    private class PropertiesMapEntry implements Map.Entry<String, String>
    {
        private String key = null;

        PropertiesMapEntry (String key)
        {
            this.key = key;
        }

        public boolean equals (Object o)
        {
            return PropertiesMapEntry.class.isInstance (o);
        }

        public int hashCode()
        {
            return key.hashCode();
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return PropertiesMap.this.get (key);
        }

        public String setValue (String value)
        {
            return PropertiesMap.this.put (key, value);
        }
    }

    /**
     * Set of Map.Entry objects returned by the PropertiesMap.entrySet()
     * method.
     */
    private class EntrySet extends AbstractSet<Map.Entry<String, String>>
    {
        private Set<PropertiesMapEntry> entrySetResult;

        EntrySet()
        {
            entrySetResult = new HashSet<PropertiesMapEntry>();

            // Load the entry set.

            KeySet keySet = new KeySet();
            for (Iterator<String> it = keySet.iterator(); it.hasNext(); )
                entrySetResult.add (new PropertiesMapEntry (it.next()));
        }

        public Iterator<Map.Entry<String, String>> iterator()
        {
            return new Iterator<Map.Entry<String, String>>()
            {
                Iterator<PropertiesMapEntry> it = entrySetResult.iterator();

                public Map.Entry<String, String> next()
                {
                    //return (Map.Entry<String, String>) it.next();
                    return it.next();
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

                has = PropertiesMap.this.containsKey (key);
            }

            return has;
        }

        public boolean remove (Object o)
        {
            return (PropertiesMap.this.remove (o) != null);
        }

        public int size()
        {
            return PropertiesMap.this.size();
        }

        public void clear()
        {
            PropertiesMap.this.clear();
        }
    }

    /**
     * Set of key objects returned by the PropertiesMap.keySet() method.
     */
    private class KeySet extends AbstractSet<String>
    {
        private KeySet()
        {
            // Nothing to do
        }

        public Iterator<String> iterator()
        {
            return new KeySetIterator();
        }

        public boolean contains (Object key)
        {
            return PropertiesMap.this.containsKey (key);
        }

        public boolean remove (Object key)
        {
            return (PropertiesMap.this.remove (key) != null);
        }

        public int size()
        {
            return PropertiesMap.this.size();
        }

        public void clear()
        {
            PropertiesMap.this.clear();
        }
    }

    /**
     * Iterator returned by KeySet.iterator()
     */
    private class KeySetIterator implements Iterator<String>
    {
        private Enumeration<?> propertyNames;

        KeySetIterator()
        {
            propertyNames = PropertiesMap.this.properties.propertyNames();
        }

        public String next()
        {
            return (String) propertyNames.nextElement();
        }

        public boolean hasNext()
        {
            return propertyNames.hasMoreElements();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
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
                             Private Variables
    \*----------------------------------------------------------------------*/

    private EntrySet entrySetResult = null;
    private Properties properties = null;

    /*----------------------------------------------------------------------*\
                                Constructors
    \*----------------------------------------------------------------------*/

    /**
     * Construct a map to wrap the specified <tt>Properties</tt> object.
     *
     * @param properties the <tt>Properties</tt> object
     */
    public PropertiesMap (Properties properties)
    {
        this.properties = properties;
    }

    /*----------------------------------------------------------------------*\
                              Public Methods
    \*----------------------------------------------------------------------*/

    /**
     * Remove all mappings from this map.
     */
    public void clear()
    {
        properties.clear();
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
        return properties.containsKey (key);
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
        return properties.containsValue (value);
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
     * @return nothing
     *
     * @throws UnsupportedOperationException unconditionally
     */
    public Set<Map.Entry<String, String>> entrySet()
    {
        synchronized (this)
        {
            if (entrySetResult == null)
                entrySetResult = new EntrySet();
        }

        return entrySetResult;
    }

    /**
     * Retrieve an object from the map. Retrieving an object from an
     * LRU map "refreshes" the object so that it is among the most recently
     * used objects.
     *
     * @param key  the object's key in the map. Must be a string.
     *
     * @return the associated object, or null if not found
     *
     * @throws ClassCastException  if <tt>key</tt> is not a string
     */
    public String get (Object key)
    {
        return properties.getProperty ((String) key);
    }

    /**
     * Determine whether this map is empty or not.
     *
     * @return <tt>true</tt> if the map has no mappings, <tt>false</tt>
     *          otherwise
     */
    public boolean isEmpty()
    {
        return properties.isEmpty();
    }

    /**
     * <p>Return a <tt>Set</tt> view of the keys in the map. The set is
     * partially backed by the map. Changes to the map are not necessarily
     * reflected in the set, and vice-versa. The set does not support
     * element removal.</p>
     *
     * @return the set of keys in this map
     */
    public Set<String> keySet()
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
    public String put (String key, String value)
    {
        String previous = (String) properties.setProperty (key, value);
        entrySetResult = null;
        return previous;
    }

    /**
     * Removes the mapping for a key, if there is one. Not supported by
     * this class.
     *
     * @param key the key to remove
     *
     * @return the previous value associated with the key, or null if (a) there
     *         was no previous value, or (b) the previous value was a null
     *
     * @throws UnsupportedOperationException unconditionally
     */
    public String remove (Object key)
    {
        throw new UnsupportedOperationException();
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
        return properties.size();
    }
}
