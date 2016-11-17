/*
 *
 * Copyright 2002 CareScience, Inc. All Rights Reserved.
 *
 */
package org.openempi.ils;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.openempi.ILS_Version;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.Utils;

/**
   Start up class for ILS.  This class is run when the ILS is initialized
   by the Servlet engine (it is not actually a servlet, but this is an
   easy way to create a startup class).  
 */
public class IlsStartup
  extends HttpServlet
{
  public void init(ServletConfig config)
    throws ServletException
  {
    DOMConfigurator.configure(Utils.configDir("IlsLog4j.xml"));  // Configures log4j
    Logger log = Logger.getLogger("ILS");
    log.info("Ils Initialization: " + ILS_Version.getVersion());
    IlsProps.load();
    AxisSSLSocketFactory.init();
    Thread t = new Thread(new Timer());
    t.setContextClassLoader(Thread.currentThread().getContextClassLoader());
    t.start();
  }
  
  /**
     This Timer class is used to periodically refresh certain aspects of
     the ILS.  It currently performs the following functions each time it
     wakes up:

     Calls IlsServiceBean.refreshNodes() to update available ILS node status
     Checks if the IlsProps.xml has be modified an reloads it.
  */
  private static class Timer
    implements Runnable
  {
    public void run()
    {
      File propFile = new File(IlsProps.getPropFile());
      long modTime = propFile.lastModified();
      boolean rebuild = true;
      
      while(true) {
        try {
          NodeInfoList.refreshNodes(rebuild);
          rebuild = false;
          Thread.currentThread().sleep(IlsProps.getNodeStatusRefresh());
          if (propFile.lastModified() != modTime) {
            modTime = propFile.lastModified();
            IlsProps.load();
            rebuild = true;
          }
        } catch (Exception e) {
        }
      }
    }
  }

}
