package org.openempi.ils.utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.openempi.ils.ExternalLocatorAdapter;

public class IlsSystemProps
{
  public static final String URL_PARAM = "N";
  public static final String URL_LITERAL = "Y";

  /**
     The Domain for this system property section
  */
  private String domain;

  /**
     The user-friendly version of the system name
  */
  private String systemName;

  /**
     Optional system description string
  */
  private String description;

  /**
     Custom fixed paramters automatically added to the IlsMap
  */
  private Map customParams;

  /**
     List of the Type codes supported for this system
  */
  private Set types;

  /**
     The base part of the result URL up to, but not including the paramters
  */
  private String resultURLBase;

  /**
     A Map of the URL paramters for the result
  */
  private Map resultURLParams;

  /**
     The base part of the order URL up to, but not including the paramters
  */
  private String orderURLBase;

  /**
     A Map of the URL paramters for the order
  */
  private Map orderURLParams;
  
  /**
     The base part of the encounter URL up to, but not including the paramters
  */
  private String encounterURLBase;
  
  /**
     A Map of the URL paramters for the encounter
  */
  private Map encounterURLParams;
  
  /**
     The classname for the adapter interface.  The class must implement
     ExternalLocatorAdapter.  If this is non-null, interfaceHost will
     always be null.
  */
  private Class interfaceClass;

  /**
     The hostname:port for a remote node.  If this is non-null,
     interfaceClass will always be null.
  */
  private String interfaceHost;


  private Logger log = Logger.getLogger("ILS");

  /**
     The XML node of the interface section so specialized parameters
     can get accessed.
  */
  private Element interfaceNode;

  public IlsSystemProps()
  {
    customParams = new HashMap();
    types = new HashSet();
  }
  
  public static void loadAll(Element root)
  {
    Iterator children = root.getChildren().iterator();
    while (children.hasNext()) {
      Element child = (Element) children.next();
      if (child.getName().equalsIgnoreCase("SystemProps")) {
        IlsSystemProps prop = new IlsSystemProps();
        prop.load(child);
      }
    }
  }

  public void load(Element el)
  {
    String name, text;
    Attribute attr;
    boolean error = false;

    Iterator props = el.getChildren().iterator();
    while (props.hasNext()) {
      Element prop = (Element) props.next();
      name = prop.getName();
      text = prop.getTextTrim();
      if (name.equalsIgnoreCase("Domain")) {
        domain = text;
      } else if (name.equalsIgnoreCase("SystemName")) {
        systemName = text;
      } else if (name.equalsIgnoreCase("Description")) {
        description = text;
      } else if (name.equalsIgnoreCase("CustomParams")) {
        parseCustomParams(prop);
      } else if (name.equalsIgnoreCase("SystemTypes")) {
        parseSystemTypes(prop);
      } else if (name.equalsIgnoreCase("ResultUrl")) {
        resultURLParams = new HashMap();
        attr = prop.getAttribute("Base");
        resultURLBase = attr.getValue().trim();
        parseURLParams(prop, resultURLParams);
      } else if (name.equalsIgnoreCase("OrderUrl")) {
        orderURLParams = new HashMap();
        attr = prop.getAttribute("Base");
        orderURLBase = attr.getValue().trim();
        parseURLParams(prop, orderURLParams);
      } else if (name.equalsIgnoreCase("EncounterUrl")) {
        encounterURLParams = new HashMap();
        attr = prop.getAttribute("Base");
        encounterURLBase = attr.getValue().trim();
        parseURLParams(prop, encounterURLParams);
      } else if (name.equalsIgnoreCase("Interface")) {
        interfaceNode = prop;
        attr = prop.getAttribute("Class");
        if (attr != null) {
          String clsName = attr.getValue().trim();
          try {
            interfaceClass = Class.forName(clsName);
            Object obj = interfaceClass.newInstance();
            if (! (obj instanceof ExternalLocatorAdapter)) {
              log.error("<Interface> Class " + clsName +
                        " does not implement ExternalLocatorAdapter!");
              error = true;
            }
          } catch (ClassNotFoundException cnfe) {
            log.error("<Interface> Class " + clsName + " does not exist!");
            error = true;
          } catch (Exception e) {
            log.error("<Interface> Class " + clsName +
                      " cannot be instantiated!");
            error = true;
          }
        }
        attr = prop.getAttribute("Host");
        if (attr != null) 
          interfaceHost = attr.getValue().trim();
      }
    }
    if (domain == null) {
      log.error("<Domain> missing from <SystemProps>!");
      error = true;
    }
    if (systemName == null) {
      log.error("<SystemName> missing from <SystemProps> for " + domain + "!");
      error = true;
    }
    if (types.size() == 0) {
      log.error("No <SystemTypes> specified for <SystemProps> for " + domain + "!");
      error = true;
    }
    if (interfaceClass != null && interfaceHost != null) {
      log.error("<Interface>" + domain + " cannot specify both Class and Host attributes!");
      error = true;
    }
    if (interfaceClass == null && interfaceHost == null) {
      log.error("<Interface>" + domain + " must specify either Class or Host attributes!");
      error = true;
    }
    if (error)
      throw new IllegalArgumentException("Configuration error");
  }

  private void parseCustomParams(Element prop)
  {
    Iterator params = prop.getChildren().iterator();
    String name, val, text;
    Attribute attr;
    while (params.hasNext()) {
      Element param = (Element) params.next();
      name = param.getName();
      text = param.getTextTrim();
      if (name.equalsIgnoreCase("Param")) {
        attr = param.getAttribute("Value");
        if (attr == null) {
          log.error("<Param> " + text +
                    " in <CustomParams> has no Value specified!");
          throw new IllegalArgumentException("Configuration error");
        }
        val = attr.getValue().trim();
        customParams.put(text, val);
      }
    }
  }

  private void parseURLParams(Element prop, Map map)
  {
    Iterator urls = prop.getChildren().iterator();
    String name, val;
    while (urls.hasNext()) {
      Element url = (Element) urls.next();
      name = url.getName();
      if (name.equalsIgnoreCase("Param")) 
        map.put(url.getTextTrim(), URL_PARAM);
      else if (name.equalsIgnoreCase("Literal")) 
        map.put(url.getTextTrim(), URL_LITERAL);
    }
  }

  private void parseSystemTypes(Element prop)
  {
    Iterator typesIter = prop.getChildren().iterator();
    String name, val;
    while (typesIter.hasNext()) {
      Element el = (Element) typesIter.next();
      name = el.getName();
      if (name.equalsIgnoreCase("Type")) 
        types.add(el.getTextTrim().toUpperCase());
    }
  }

  public String getDomain()
  {
    return domain;
  }

  public String getSystemName()
  {
    return systemName;
  }

  public String getDescription()
  {
    return description;
  }

  public Class getInterfaceClass()
  {
    return interfaceClass;
  }
  
  public String getInterfaceHost()
  {
    return interfaceHost;
  }
  
  public int getInterfacePropertyInt(String name, int defVal)
  {
    String ret = getInterfaceProperty(name);
    if (ret == null)
      return defVal;
    return Integer.valueOf(ret).intValue();
  }
  
  public short getInterfacePropertyShort(String name, short defVal)
  {
    String ret = getInterfaceProperty(name);
    if (ret == null)
      return defVal;
    return Short.valueOf(ret).shortValue();
  }
  
  public double getInterfacePropertyDouble(String name, double defVal)
  {
    String ret = getInterfaceProperty(name);
    if (ret == null)
      return defVal;
    return Double.valueOf(ret).doubleValue();
  }
  
  public float getInterfacePropertyFloat(String name, float defVal)
  {
    String ret = getInterfaceProperty(name);
    if (ret == null)
      return defVal;
    return Float.valueOf(ret).floatValue();
  }
  
  public long getInterfacePropertyLong(String name, long defVal)
  {
    String ret = getInterfaceProperty(name);
    if (ret == null)
      return defVal;
    return Long.valueOf(ret).longValue();
  }
  
  public String getInterfaceProperty(String name, String defVal)
  {
    String ret = getInterfaceProperty(name);
    if (ret == null)
      ret = defVal;
    return ret;
  }
  
  public String getInterfaceProperty(String name)
  {
    return getInterfaceProperty(name, (Map) null);
  }
  
  public String getInterfaceProperty(String name, Map map)
  {
    Element el = getInterfaceElement(name, map);
    if (el == null)
      return null;
    return el.getTextTrim();
  }
  
  public Element getInterfaceElement(String name)
  {
    return getInterfaceElement(name, (Map) null);
  }
  
  public Element getInterfaceElement(String name, Map map)
  {
    Iterator iter = interfaceNode.getChildren().iterator();
    while (iter.hasNext()) {
      Element el = (Element) iter.next();
      if (el.getName().equalsIgnoreCase(name)) {
        Iterator attrIter = el.getAttributes().iterator();
        boolean match = true;
        while (attrIter.hasNext()) {
          Attribute attr = (Attribute) attrIter.next();
          String val = (String) attr.getValue().trim();
          String mapVal = map.get(attr.getName()).toString();
          if (mapVal == null || ! val.equalsIgnoreCase(mapVal)) {
            match = false;
            break;
          }
        }
        if (match)
          return el;
      }
    }
    return null;
  }
  
  public Set getTypes()
  {
    return types;
  }

  public String getResultURLBase()
  {
    return resultURLBase;
  }

  public Map getResultURLParams()
  {
    return resultURLParams;
  }

  public String getOrderURLBase()
  {
    return orderURLBase;
  }

  public Map getOrderURLParams()
  {
    return orderURLParams;
  }
  public String getEncounterURLBase()
  {
    return encounterURLBase;
  }

  public Map getEncounterURLParams()
  {
    return encounterURLParams;
  }

  public Map getCustomParams()
  {
    return customParams;
  }

}
