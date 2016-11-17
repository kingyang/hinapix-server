package org.openempi.ics.utility;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Simple generic cache class that supports a maximum cache size and
 * element age-out using least recently used rules.
 */
public class Cache
{
    /**
     The maximum number of elements in the cache.  If 0, there is no
     limit.
     */
    private int maxSize = 0;
    
    /**
     Maximum age of any element in the cache.  Elements that haven't
     been referenced in ageMillis will be dropped from the cache.
     A value of 0 means no age limit.
     */
    private long ageMillis = 0;
    
    /**
     If true, this cache uses least-recently-used logic to age out elements.
     If false, elements are aged out based on when they entered the cache.
     */
    private boolean lru = false;
    
    /**
     The HashMap that backs the cache
     */
    private HashMap map = new HashMap();
    
    /**
     TreeSet to keep the cache oraganized by age
     */
    private TreeSet set = new TreeSet();
    
    /**
     Set the cache to age-out based on LRU rules or by age of element
     in the cache.
   
     @param flag true, cache ages out least recently used elements, false
                 ages out oldest elements in cache.
     */
    public synchronized void setLRU(boolean flag)
    {
        lru = flag;
        ageOut();
    }
    
    /**
     Sets the maximum number of elements allowed in the cache.
   
     @param size The maximum number of elements, or 0 for unlimited
     @throws IllegalArgumentException if size is < 0
     */
    public synchronized void setSize(int size)
    {
        if (size < 0)
            throw new IllegalArgumentException("Size must be >= 0");
        maxSize = size;
        resize();
    }
    
    /**
     Sets the maximum age for any element in the cache before it
     will be aged out.  The Cache uses least recently used rules to
     determine element age.
   
     @param ageMills The maximum element age in milliseconds or 0 for
                     no age limit
     @throws IllegalArgumentException if ageMillis is < 0
     */
    public synchronized void setAgeMillis(long ageMillis)
    {
        if (ageMillis < 0)
            throw new IllegalArgumentException("ageMillis must be >= 0");
        this.ageMillis = ageMillis;
        ageOut();
    }
    
    /**
     Performs a Cache search for the given key.  If the key is found
     in the Cache, its value is returned.  If the key is not found in the
     cache, null is returned.   When a element is found and returned, the
     elements age is reset to the current time to maintain it in the cache.
     Keys are compared using the key Objects equals() method.
   
     @param key The key to search in the cache
     @return The Object that matches the key in the cache or null if no
             match.
     */
    public synchronized Object get(Object key)
    {
        if(key == null)
            return null;
        ageOut();
        Key keyObj = new Key(key);
        Object obj = map.get(keyObj);
        if (lru && ageMillis != 0 && obj != null)
            set.add(keyObj);  // Updates element age
        // System.out.println("Cache get for key '" + key + "' returned " + obj);
        return obj;
    }
    
    /**
     Adds a new key/value pair to the cache.  If the key is already
     in the cache, its value is replaced by the given value.
     Keys are compared using the key Objects equals() method.
   
     @param key The key to insert into the cache
     @param value The Object representing the desired cache value for the key
     */
    public synchronized void put(Object key, Object value)
    {
        if(key == null || value == null)
            return;
        // System.out.println("Cache put for key '" + key + "' with value " + value);
        Key keyObj = new Key(key);
        map.put(keyObj, value);
        set.add(keyObj);
        resize();
    }
    
    /**
     Removes the specified key/value pair from the cache based on the
     key given.  If the key is found, the pair are removed and the value
     is returned.  If the key is not found, nothing is done and null is
     returned.
   
     @param key The key to search the cache for
     @return The Object that matches the key in the cache or null if no
             match.
     */
    public synchronized Object remove(Object key)
    {
        if(key == null)
            return null;
        Key keyObj = new Key(key);
        set.remove(keyObj);
        Object obj = map.remove(keyObj);
        // System.out.println("Cache remove key '" + keyObj.key + "' with value " + obj);
        return obj;
    }
    
    /**
     Resizes the Cache to ensure that it is within the maxSize setting
     for this cache.  If the cache is already below maxSize or maxSize
     is zero, nothing is done.  If the cache is larger than maxSize,
     then elements are removed from the cache in LRU order.
     */
    private void resize()
    {
        if (maxSize > 0)
        {
            while (set.size() > maxSize)
            {
                Key keyObj = (Key) set.first();
                set.remove(keyObj);
                Object value = map.remove(keyObj);
                // System.out.println("Cache resize remove key '" + keyObj.key + "' with value " + value);
            }
        }
    }
    
    /**
     Ages out all elements from the cache that haven't been used in more
     than ageMillis milliseconds.  If ageMillis is 0, nothing is done.
     */
    private void ageOut()
    {
        if (ageMillis > 0)
        {
            long now = System.currentTimeMillis();
            while(set.size() > 0)
            {
                Key keyObj = (Key) set.first();
                if (keyObj.tstamp + ageMillis < now)
                {
                    set.remove(keyObj);
                    Object value = map.remove(keyObj);
                    // System.out.println("Cache age-out remove key '" + keyObj.key + "' with value " + value);
                }
                else
                    break;
            }
        }
    }
    
    public void clear()
    {
        map.clear();
        set.clear();
    }
    
    /**
     Private class to wrap the Cache keys for use in the TreeSet.
     This class contains the key timestamp and implements Comparable
     so that the TreeSet can accurately sort the Cache elements
     by age.
     */
    private class Key
            implements Comparable
    {
        /**
       The key Object passed in from one of the Cache interfaces
         */
        private Object key;
        
        /**
       The time this key entry was last referenced
         */
        private long tstamp;
        
        /**
       Constructs the Key object from the key given.
       Sets the timestap to the current time.
         */
        public Key(Object key)
        {
            this.key = key;
            this.tstamp = System.currentTimeMillis();
        }
        
        /**
       Required by the Comparable interfaces, this compares the
       timestamps of the keys to maintain the proper sort in the TreeSet.
         */
        public int compareTo(Object obj)
        {
            Key other = (Key) obj;
            if (tstamp > other.tstamp)
                return 1;
            if (tstamp < other.tstamp)
                return -1;
            return 0;
        }
        
        /**
       Implements the equals methods by comparing the key attribute
       of the Key.  The TreeSet will use this to match identical
       keys for get/put calls.
         */
        public boolean equals(Object obj)
        {
            return key.equals(((Key)obj).key);
        }
        
        public String toString()
        {
            return key.toString();
        }
        
        public int hashCode()
        {
            return key.hashCode();
        }
        
    }
    
    
}
