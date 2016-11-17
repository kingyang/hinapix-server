package org.openempi.ils.utility;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
   Class for data interchange between ILS nodes and across client interfaces.
   An XfrMap is used for both request and responses for the query() method
   of the IlsService.

   Underlying the XfrMap is a HashMap.  The XfrMap provides for getter/setter
   methods for manipulating the data in the HashMap as well as some constants
   for the attribute names.  The values used should always be standard java
   classes, not any custom classes.  This is because the HashMap will be
   used in a SOAP call to remote nodes and we want to maintain serializable
   compatability between nodes.
*/
public class XfrMap
  implements java.io.Serializable
{
  /**
     The actual Map containing the request data
   */
  private HashMap map;

  /**
     Returns an array of HashMaps from the give array of XfrMaps.

     @param maps The XfrMap array to extract HashMaps from
     @return The HashMap array from the XfrMaps
  */
  public static HashMap[] xfrMapToHashMapArray(XfrMap[] maps)
  {
    HashMap[] ret = new HashMap[maps.length];
    for(int i=0;i<maps.length;i++)
      ret[i] = maps[i].getMap();
    return ret;
  }

  /**
     Returns an array of XfrMaps from the give array of HashMaps.

     @param maps The HashMap array to extract XfrMaps from
     @return The XfrMap array from the HashMaps
  */
  public static XfrMap[] hashMapToXfrMapArray(HashMap[] maps)
  {
    XfrMap[] ret = new XfrMap[maps.length];
    for(int i=0;i<maps.length;i++)
      ret[i] = new XfrMap(maps[i]);
    return ret;
  }

  /**
     Basic constructor.  Creates and empty XfrMap
  */
  public XfrMap()
  {
    map = new HashMap();
  }

  /**
     Creates an XfrMap containing all of the elements of the given XfrMap

     @param xfrMap The source XfrMap to copy values from
  */
  public XfrMap(XfrMap xfrMap)
  {
    this();
    setMap(xfrMap.map);
  }
  
  /**
     Creates an XfrMap containing all of the elements of the given Map.

     @param xfrMap The source Map to copy values from
  */
  public XfrMap(Map map)
  {
    this();
    setMap(map);
  }

  /**
     Converts a Calendar object to a Date.  Axis SOAP converts Dates
     to Gregorian calendars so these methods facilitate and hide all of
     that.

     @param cal The Calendar object to get the Date from
     @return The Date for cal or null if cal is null
  */
  private Date calToDate(Calendar cal)
  {
    if (cal == null)
      return null;
    return cal.getTime();
  }
  
  /**
     Converts a Date object to a Calendar.  Axis SOAP converts Dates
     to Gregorian calendars so these methods facilitate and hide all of
     that.

     @param date The Date object to get a Calendar object for
     @return The Calendar for date or null if date is null
  */
  private Calendar dateToCal(Date date)
  {
    if (date == null)
      return null;
    Calendar c = new GregorianCalendar();
    c.setTime(date);
    return c;
  }

  /*
    Basic Map putter for key/value pairs.  This class automatically
    handles Date to Calendar conversions

    @param key The attribute name
    @param val The object value to store
  */
  public void put(String key, Object val)
  {
    if (val instanceof Date) 
      map.put(key, dateToCal((Date) val));
    else
      map.put(key, val);
  }
  
  /*
    Copies all of the mappings from inMap into the XfrMap.  Same as
    calling put() on each element from inMap into the XfrMap.

    @param inMap The source Map
    @param val The object value to store
  */
  public void putAll(Map inMap)
  {
    Iterator iter = inMap.keySet().iterator();
    while(iter.hasNext()) {
      String key = (String) iter.next();
      Object val = inMap.get(key);
      put(key, val);
    }
  }
  
  /*
    Basic Map getter for key/value pairs.  This class automatically
    handles Calendar to Date conversions

    @param key The attribute name to look up
    @return The value for key or null if key is not found
  */
  public Object get(String key)
  {
    Object val = map.get(key);
    if (val == null)
      return null;
    if (val instanceof GregorianCalendar)
      return calToDate((Calendar) val);
    return val;
  }
  
  /*
    Removes the specified key from the map

    @param key The attribute name to look up
    @return The value of the key
  */
  public Object remove(String key)
  {
    return map.remove(key);
  }
  
  /**
     Overlays the given Map over the XfrMap.  All key/values for the
     XfrMap will now be the same as in inMap.

     @param inMap The Map to use to set the XfrMap
  */
  public void setMap(Map inMap)
  {
    map.clear();
    putAll(inMap);
  }

  /**
     Returns the HashMap for this XfrMap.  This will convert all
     Calendar values to Dates automatically

     @return The HashMap of all key/values for the XfrMap
  */
  public HashMap getMap()
  {
    HashMap ret = new HashMap();
    Iterator iter = map.keySet().iterator();
    while(iter.hasNext()) {
      String key = (String) iter.next();
      Object val = get(key);
      ret.put(key, val);
    }
    return ret;
  }

  /**
     @return String representation of the XfrMap
  */
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    Iterator iter = map.keySet().iterator();
    while(iter.hasNext()) {
      Object key = iter.next();
      Object val = map.get(key);
      buf.append(key).append("=");
      if (val instanceof String[]) {
        String[] foo = (String[]) val;
        buf.append("(");
        for(int i=0;i<foo.length;i++) {
          if (i > 0)
            buf.append(",");
          buf.append(foo[i]);
        }
        buf.append(")\n");
      } else if (val instanceof Calendar) {
        buf.append(calToDate((Calendar) val)).append("\n");
      } else {
        buf.append(val).append("\n");
      }
    }
    return buf.toString();
  }

}
