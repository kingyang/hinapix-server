/*
 * Title:       Utils
 * Description: A collection of utility methods.
 * Copyright:   (c) 1998-2002
 * Company:     CareScience
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.utility;

import java.io.File;

/**
 * A collection of static utility methods.
 *
 * @author mabundo, CareScience
 * @version 1.15, 20020318
 */
public class Utils
{

  /** This class should never be instantiated, all methods are static. */
  private Utils() {}

  /**
     Given a config file, will return the full, absolute path
     to the file.  If the file name is already an absolute path, 
     that same path is returned.  If the file is not absolute,
     the full path to the config is returned. 
     
     @param file The file name or full path to the config file
     @return The canonical absolute path to the config file
  */
  public static String configDir(String file)
  {
    try {
      if (file != null) {
        File f = new File(file);
        if (! f.isAbsolute()) {
          // This finds to domain root for weblogic 6/7
          String domain = System.getProperty("weblogic.Domain");
          if (domain != null) 
            f = new File("config/" + domain + "/" + file);
        }
        return f.getCanonicalPath();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
}
