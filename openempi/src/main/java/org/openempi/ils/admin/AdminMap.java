package org.openempi.ils.admin;

import java.util.HashMap;

import org.openempi.ils.utility.XfrMap;

/**
   Class for data interchange between ILS nodes and across client interfaces.
   An AdminMap is used for both request and responses for the query() method
   of the IlsService.

   Underlying the AdminMap is a HashMap.  The AdminMap provides for getter/setter
   methods for manipulating the data in the HashMap as well as some constants
   for the attribute names.  The values used should always be standard java
   classes, not any custom classes.  This is because the HashMap will be
   used in a SOAP call to remote nodes and we want to maintain serializable
   compatability between nodes.
*/
public class AdminMap extends XfrMap
  implements java.io.Serializable
{
  public final static String HOST = "host";
  public final static String IS_RUNNING = "isRunning";
  public final static String VERSION = "version";
  
  /**
     Basic constructor.  Creates and empty AdminMap
  */
  public AdminMap()
  {
    super();
  }

  /**
     Creates an AdminMap containing all of the elements of the given AdminMap

     @param adminMap The source AdminMap to copy values from
  */
  public AdminMap(AdminMap adminMap)
  {
    this();
    setMap(adminMap.getMap());
  }
  
  /**
     Creates an AdminMap containing all of the elements of the given HashMap.

     @param adminMap The source HashMap to copy values from
  */
  public AdminMap(HashMap map)
  {
    this();
    setMap(map);
  }

  /**
     Returns an array of HashMaps from the give array of AdminMaps.

     @param maps The AdminMap array to extract HashMaps from
     @return The HashMap array from the AdminMaps
  */
  public static HashMap[] adminMapToHashMapArray(AdminMap[] maps)
  {
    HashMap[] ret = new HashMap[maps.length];
    for(int i=0;i<maps.length;i++)
      ret[i] = maps[i].getMap();
    return ret;
  }

  /**
     Returns an array of AdminMaps from the give array of HashMaps.

     @param maps The HashMap array to extract AdminMaps from
     @return The AdminMap array from the HashMaps
  */
  public static AdminMap[] hashMapToAdminMapArray(HashMap[] maps)
  {
    AdminMap[] ret = new AdminMap[maps.length];
    for(int i=0;i<maps.length;i++)
      ret[i] = new AdminMap(maps[i]);
    return ret;
  }

  /**
    @return The location name or null
  */
  public String getHost()
  {
    return (String) get(HOST);
  }

  /**
     Sets the host name of the response

     @param host The host name
  */
  public void setHost(String host)
  {
    put(HOST, host);
  }

  /**
     Set the running state of this node

     @param flag true for running, false otherwise
  */
  public void setIsRunning(boolean flag)
  {
    put(IS_RUNNING, new Boolean(flag));
  }

  /**
     @return Returns running state 
  */
  public boolean getIsRunning()
  {
    Boolean bool = (Boolean) get(IS_RUNNING);
    if (bool == null)
      return false;
    return bool.booleanValue();
  }

  /**
    @return The ILS version
  */
  public String getVersion()
  {
    return (String) get(VERSION);
  }

  /**
     Sets the ILS version number

     @param version The version string
  */
  public void setVersion(String version)
  {
    put(VERSION, version);
  }


}
