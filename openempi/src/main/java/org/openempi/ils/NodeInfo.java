package org.openempi.ils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.openempi.ils.utility.XfrMap;

/**
   Class for returning Domain specific information about what ILS nodes
   exist, etc.
*/
public class NodeInfo extends XfrMap
  implements java.io.Serializable
{
  /**
     The domain id that this node belongs to
  */
  public final static String DOMAIN_ID = "domainId";
  
  /**
     The host:port of the ILS node
  */
  public final static String ILS_NODE = "ilsNode";

  /**
     The type codes supported by this node
  */
  public final static String TYPE_CODES = "typeCodes";

  /**
     The System Name for this node
  */
  public final static String SYSTEM_NAME = "systemName";

  /**
     The Description field for this node
  */
  public final static String DESCRIPTION = "description";

  /**
     The Date the node information was last updated
  */
  public final static String LAST_UPDATE = "lastUpdate";

  /**
     Set to boolean true if this node is running
  */
  public final static String IS_RUNNING = "isRunning";

  /**
     The ILS version running on this node
  */
  public final static String VERSION = "version";

  /**
     Basic constructor.  Creates and empty NodeInfo
  */
  public NodeInfo()
  {
    super();
  }

  /**
     Creates an NodeInfo containing all of the elements of the given NodeInfo

     @param nodeInf The source NodeInfo to copy values from
  */
  public NodeInfo(NodeInfo nodeInf)
  {
    this();
    setMap(nodeInf.getMap());
  }
  
  /**
     Sets the domain Id (namespace) for the request.  The requestor
     is responsible for setting this.

     @param id the domain Id
  */
  public void setDomainId(String id)
  {
    put(DOMAIN_ID, id);
  }

  /**
     @return The domain Id or null if not set
  */
  public String getDomainId()
  {
    return (String) get(DOMAIN_ID);
  }

  /**
     Sets the node host:port information for this node
  */
  public void setNode(String node)
  {
    put(ILS_NODE, node);
  }

  /**
     @return The host:port information for this node
  */
  public String getNode()
  {
    return (String) get(ILS_NODE);
  }

  /**
     Sets an array of Type Codes for this request.  This will override
     any code set with setTypeCode()

     @param code The type code
  */
  public void setTypeCodes(String[] codes)
  {
    HashSet set = new HashSet();
    for(int i=0;i<codes.length;i++) {
      set.add(codes[i]);
    }
    codes = new String[set.size()];
    put(TYPE_CODES, set.toArray(codes));
  }

  /**
     Sets an array of Type Codes for this request.  This will override
     any code set with setTypeCode()

     @param code The type code
  */
  public void setTypeCodes(Set codes)
  {
    setTypeCodes((String[]) codes.toArray(new String[codes.size()]));
  }

  /**
    @return The array of type codes or null
  */
  public String[] getTypeCodes()
  {
    return (String[]) get(TYPE_CODES);
  }

  /**
     @return The location name or null
  */
  public String getSystemName()
  {
    return (String) get(SYSTEM_NAME);
  }
  
  /**
     Sets the location name for the ILS node.  This is normally set
     automatically by the system
     
     @param systemName The name of the ILS node
  */
  public void setSystemName(String systemName)
  {
    put(SYSTEM_NAME, systemName);
  }

  /**
     @return The location name or null
  */
  public String getDescription()
  {
    return (String) get(DESCRIPTION);
  }
  
  /**
     Sets the location name for the ILS node.  This is normally set
     automatically by the system
     
     @param systemName The name of the ILS node
  */
  public void setDescription(String systemName)
  {
    put(DESCRIPTION, systemName);
  }

  /**
     @return The status of this node.  Either I
  */
  public boolean getIsRunning()
  {
    Boolean bool = (Boolean) get(IS_RUNNING);
    if (bool == null)
      return false;
    return bool.booleanValue();
  }
  
  /**
     Sets the location name for the ILS node.  This is normally set
     automatically by the system
     
     @param systemName The name of the ILS node
  */
  public void setIsRunning(boolean flag)
  {
    put(IS_RUNNING, new Boolean(flag));
  }

  /**
     @return The Date the NodeInfo was last updates
  */
  public Date getLastUpdate()
  {
    return (Date) get(LAST_UPDATE);
  }
  
  /**
     Sets the Date this NodeInfo was last updated.
     
     @param lastUpdate The Date last updated
  */
  public void setLastUpdate(Date lastUpdate)
  {
    put(LAST_UPDATE, lastUpdate);
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
