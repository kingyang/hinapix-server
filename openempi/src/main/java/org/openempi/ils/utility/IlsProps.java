package org.openempi.ils.utility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class IlsProps
{
  /**
     Defines the system property that may be used to locate the IlsProps.xml
     file
  */
  private static String PROP_FILE_PATH = "com.carescience.xmlPropFile";

  /**
     Private singleton instance of this class
  */
  private static IlsProps instance;

  /**
     The fully qualified path to the IlsProps.xml file
  */
  private String propFile;

  /**
     The Host parameter.  The hostname that must match the incoming
     request
  */
  private String host;

  /**
     The logging level to use.  Should be one of the log4j log level
     type strings
  */
  private String logLevel;

  /**
     The number of millis to wait for any request before throwing a
     timeout exception
  */
  private long timeout = 60000;

  /**
     Number of millis between refresh cycles for the node status info
  */
  private long nodeStatusRefresh = 60000;

  /**
     The path to the java keystore used for SSL stuff
  */
  private String keyStoreLoc;

  /**
     The password for the keyStore to access it
  */
  private String keyStorePwd;

  /**
     The password for the private key in the keystore
  */
  private String keyPwd;

  /**
     A Map of Lists of IlsSystemProp objects.  The key for the Map is
     the ILS_DOMAIN.  Each List element is a different instance of
     the IlsSystemMap, possibly different by Types.
  */
  private Map systemProps;

  /**
     The minimum size for the connector pool.
  */
  private int poolMin = 0;

  /**
     The maximum size for the connector pool.  This determines the max
     number of request that can be simulteanously processed per host.
  */
  private int poolMax = 25;

  /**
     Number of seconds before inactive pool elements are deleted
  */
  private int poolShrink = 60;

  private IlsProps()
  {
    systemProps = new HashMap();
  }
  
  public static void load()
  {
    boolean encryptFile = false;
    Logger log = Logger.getLogger("ILS");

    log.setLevel(Level.ALL);
    instance = new IlsProps();
    instance.propFile = System.getProperty(PROP_FILE_PATH);
    if (instance.propFile == null || instance.propFile == "") 
      instance.propFile = "IlsProps.xml";
    try {
      instance.propFile = Utils.configDir(instance.propFile);
      Document docNode = (new SAXBuilder()).build(new File(instance.propFile));
      Element rootNode = docNode.getRootElement();

      Iterator children = rootNode.getChildren().iterator();
      String name, text;
      Attribute attr;
      while (children.hasNext()) {
        Element child = (Element) children.next();
        name = child.getName();
        text = child.getTextTrim();
        if (name.equalsIgnoreCase("SystemProps")) {
          IlsSystemProps prop = new IlsSystemProps();
          prop.load(child);
          List pList = (List) instance.systemProps.get(prop.getDomain());
          if (pList == null) {
            pList = new ArrayList();
            instance.systemProps.put(prop.getDomain(), pList);
          }
          pList.add(prop);
        } else if (name.equalsIgnoreCase("Host")) {
          instance.host = text;
        } else if (name.equalsIgnoreCase("LogLevel")) {
          instance.logLevel = text;
        } else if (name.equalsIgnoreCase("RequestTimeout")) {
          String val = text;
          instance.timeout = Long.valueOf(val).longValue() * 1000;
        } else if (name.equalsIgnoreCase("NodeStatusRefresh")) {
          String val = text;
          instance.nodeStatusRefresh = Long.valueOf(val).longValue() * 1000;
        } else if (name.equalsIgnoreCase("RequestPoolMin")) {
          String val = text;
          instance.poolMin = Integer.valueOf(val).intValue();
        } else if (name.equalsIgnoreCase("RequestPoolMax")) {
          String val = text;
          instance.poolMax = Integer.valueOf(val).intValue();
        } else if (name.equalsIgnoreCase("RequestPoolShrink")) {
          String val = text;
          instance.poolShrink = Integer.valueOf(val).intValue() * 1000;
        } else if (name.equalsIgnoreCase("KeystoreLoc")) {
          instance.keyStoreLoc = text;
        } else if (name.equalsIgnoreCase("KeystorePwd")) {
          String key = text;
          if (key.startsWith("{encrypt}"))
            instance.keyStorePwd = DataEncryptor.decrypt(key.substring(9, key.length()));
          else {
            encryptFile = true;
            instance.keyStorePwd = key;
          }
        } else if (name.equalsIgnoreCase("KeyPwd")) {
          String key = text;
          if (key.startsWith("{encrypt}"))
            instance.keyPwd = DataEncryptor.decrypt(key.substring(9, key.length()));
          else {
            encryptFile = true;
            instance.keyPwd = key;
          }
        }
      }
      if (encryptFile) {
        // Encrypt the Attribute values and write out the new Document.
        CSPropsXMLEncryptor propEncptr = new CSPropsXMLEncryptor(instance.propFile);
        propEncptr.doEncrypt(rootNode, "KeystorePwd");
        propEncptr.doEncrypt(rootNode, "KeyPwd");
        propEncptr.writeFile(docNode);
      }

      if (instance.host == null) {
        log.error("<Host> not specified!");
        throw new IllegalArgumentException("Configuration error");
      }

    } catch (Exception e) {
      log.error("Can't read the properties file: " + instance.propFile);
    }


    log.info("Configured host: " + IlsProps.getHost());
    Iterator iter = IlsProps.getSystemPropsDomains().iterator();
    while(iter.hasNext()) {
      Class iface;
      String name;
      
      String domain = (String) iter.next();
      List list = (List) IlsProps.getSystemProps(domain);
      log.info("  Domain: " + domain);
      Iterator listIter = list.iterator();
      while(listIter.hasNext()) {
        IlsSystemProps prop = (IlsSystemProps) listIter.next();
        log.info("    Name: " + prop.getSystemName());
        log.info("    Types: " + prop.getTypes());

        if ((iface = prop.getInterfaceClass()) != null) 
          log.info("    Interface: " + iface);
        if ((name = prop.getInterfaceHost()) != null) 
          log.info("    Interface: " + name);
      }
    }
    String level = IlsProps.getLogLevel();
    if (level == null)
      level = "WARN";
    log.info("Setting log level to " + level);
    log.setLevel(Level.toLevel(level));
    
  }

  public static String getPropFile()
  {
    return instance.propFile;
  }

  public static String getHost()
  {
    return instance.host;
  }

  public static String getKeyStoreLoc()
  {
    return instance.keyStoreLoc;
  }

  public static String getKeyStorePwd()
  {
    return instance.keyStorePwd;
  }

  public static String getKeyPwd()
  {
    return instance.keyPwd;
  }

  public static Set getSystemPropsDomains()
  {
    return instance.systemProps.keySet();
  }

  public static List getSystemProps(String domain)
  {
    return (List) instance.systemProps.get(domain);
  }

  public static IlsSystemProps getSystemPropsForType(String domain,
                                                     String type)
  {
    List list = getSystemProps(domain);
    if (list == null)
      return null;
    Iterator iter = list.iterator();
    while(iter.hasNext()) {
      IlsSystemProps prop = (IlsSystemProps) iter.next();
      Set types = prop.getTypes();
      if (types.contains(type))
        return prop;
    }
    return null;
  }

  public static long getTimeout()
  {
    return instance.timeout;
  }

  public static long getNodeStatusRefresh()
  {
    return instance.nodeStatusRefresh;
  }

  public static int getPoolMin()
  {
    return instance.poolMin;
  }

  public static int getPoolMax()
  {
    return instance.poolMax;
  }

  public static int getPoolShrink()
  {
    return instance.poolShrink;
  }

  public static String getLogLevel()
  {
    return instance.logLevel;
  }

}
