package org.openempi.ics.pids;

import java.util.Iterator;
import java.util.List;

import org.openempi.data.DomainIdentifier;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.ics.utility.Cache;
import org.openempi.ics.utility.ICSProperties;
import org.openempi.ics.utility.Profile;

/**
 * Derived implementation of the Cache class to add Person specific handling.
 * This adds methods specific to the needs of ICS for managing the Cache
 * of PersonIdentifiers.
 */
public class PersonCache
{
    private static Cache pidCache = null;
    private static Cache oidCache = null;
    private static PersonCache instance = null;
    
    private PersonCache()
    {
    }
    
    public static synchronized PersonCache getInstance()
    {
        // Doing this here rather then in static{} block because I want to make
        // sure proper initialization has occurred of logging facilities
        if (instance == null)
        {
            pidCache = new Cache();
            pidCache.setSize(ICSProperties.getInt("CACHE_SIZE", 1000));
            pidCache.setAgeMillis(ICSProperties.getInt("CACHE_AGE_SECS", 3600) * 1000);
            pidCache.setLRU(false);
            oidCache = new Cache();
            oidCache.setSize(ICSProperties.getInt("CACHE_SIZE", 1000));
            oidCache.setAgeMillis(ICSProperties.getInt("CACHE_AGE_SECS", 3600) * 1000);
            oidCache.setLRU(false);
            
            instance = new PersonCache();
        }
        return instance;
    }
    
    public void clear()
    {
        pidCache.clear();
        oidCache.clear();
    }
    
    public List getPid(PersonIdentifier pid)
    {
        Profile.begin("PersonCache.getPid");
        List ret = (List) pidCache.get(getKey(pid));
        Profile.end("PersonCache.getPid");
        return ret;
    }
    
    public Person getOid(int oid)
    {
        Profile.begin("PersonCache.getOid");
        Person ret = (Person) oidCache.get(Integer.toString(oid));
        Profile.end("PersonCache.getOid");
        return ret;
    }
    
    public void put(PersonIdentifier pid, List val)
    {
        Profile.begin("PersonCache.put");
        pidCache.put(getKey(pid), val);
        Iterator iter = val.iterator();
        while(iter.hasNext())
        {
            Person p = (Person) iter.next();
            oidCache.put(p.getOid(), p);
        }
        Profile.end("PersonCache.put");
    }
    
    public void put(int oid, Person p)
    {
        Profile.begin("PersonCache.put");
        oidCache.put(Integer.toString(oid), p);
        Profile.end("PersonCache.put");
    }
    
    public void remove(Person p)
    {
        Profile.begin("PersonCache.remove");
        Iterator ids = p.getPersonIdentifiers().iterator();
        while (ids.hasNext())
            pidCache.remove(getKey((PersonIdentifier) ids.next()));
        oidCache.remove(p.getOid());
        Profile.end("PersonCache.remove");
    }
    
    public void remove(Person[] persons)
    {
        for(int i=0; i<persons.length; i++)
            remove(persons[i]);
    }
    
    private String getKey(PersonIdentifier pid)
    {
        DomainIdentifier did;
        String id = pid.getId();
        String domain = null;
        String facility = null;
        
        if ((did = pid.getAssigningAuthority()) != null)
            domain = did.getNameSpaceID();
        if ((did = pid.getAssigningFacility()) != null)
            facility = did.getNameSpaceID();
        
        return id + ":" + domain + ":" + facility;
    }
    
}
