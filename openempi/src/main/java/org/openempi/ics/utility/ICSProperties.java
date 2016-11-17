/*
 * Title:       ICSProperties
 * Description: Utility class to get various system properties.
 * Copyright:   (c) 1998-2001
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ICSProperties
{
  private static final ICSProperties instance = new ICSProperties();
  private static Properties props;
  private static long lastModified = -1;
  private static String propFile = null;

  static {
    propFile = System.getProperty("ics.properties.file");
    if (propFile == null) 
      propFile = "ICS.properties";
    propFile = Utils.configDir(propFile);
  }
  
  private ICSProperties()
  {
  }

  public static void load()
  {
    try {
      String filePath = ICSProperties.class.getResource("/ICS.properties").getPath();
      File foo = new File(filePath);
      long t = foo.lastModified();
      if (t > lastModified) {
        lastModified = t;
        props = new Properties();
        InputStream is = new FileInputStream(propFile);
        props.load(is);
        is.close();
        String level = props.getProperty("LOG_LEVEL", "WARN");
        Logger log = Logger.getLogger("ICS");
        log.info("Setting log level to " + level);
        log.setLevel(Level.toLevel(level));
      }
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace();
    }
  }
  
  /**
   * Searches for the property with the specified key in this property list.
   *
   * @param key the property key.
   * @return the value of the property, or null if the property is not found.
   */
  public static String getProperty(String key)
  {
    load();
    String value = props.getProperty(key);
    return value.trim();
  }

  public static String getProperty(String key, String defVal)
  {
    load();
    String value = props.getProperty(key, defVal);
    return value.trim();
  }

  public static boolean getBoolean(String key, boolean defVal)
  {
    load();
    String value = props.getProperty(key);
    if (value == null)
      return defVal;
    return Boolean.valueOf(value.trim()).booleanValue();
  }

  public static double getDouble(String key, double defVal)
  {
    load();
    String value = props.getProperty(key);
    if (value == null)
      return defVal;
    return Double.valueOf(value.trim()).doubleValue();
  }

  public static int getInt(String key, int defVal)
  {
    load();
    String value = props.getProperty(key);
    if (value == null)
      return defVal;
    return Integer.valueOf(value.trim()).intValue();
  }

}
