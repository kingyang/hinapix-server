/*
 *
 * Copyright 2002 CareScience, Inc. All Rights Reserved.
 *
 */
package org.openempi.ils;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openempi.ils.utility.IlsProps;

/**
 * Ils service remote implementation
 */

public class IlsServiceImpl
{
  private static final long WAIT_TIME = 2000;

  public HashMap[] sendRequestHashMap(HashMap[] reqSet)
    throws IlsException
  {

    IlsMap[] inMaps = IlsMap.hashMapToIlsMapArray(reqSet);
    IlsMap[] outMaps = sendRequest(inMaps);
    HashMap[] resSet = IlsMap.ilsMapToHashMapArray(outMaps);

    return resSet;
  }


  public IlsMap[] sendRequest(IlsMap[] ilsReqs)
    throws IlsException
  {
    IlsTask task = new IlsTask();
    Logger log = Logger.getLogger("ILS");
        
    try {
      log.debug("This Ils Node = " + IlsProps.getHost());
      
      ThreadGroup ilsThreadGroup = new ThreadGroup("IlsService");
      
      for (int i = 0; i < ilsReqs.length; i++) {
        Thread t;
        IlsMap aRequest = ilsReqs[i];
        String target = aRequest.getHostName();

        // Remove port info for comparison
        int colonIdx = target.indexOf(':');
        if (colonIdx != -1) 
          target = target.substring(0, colonIdx);
        
        log.debug("Target Ils Node = " + target);
        Connector connector = null;
        if (target != null && target.equals(IlsProps.getHost())) 
          connector = CdrConnectorPool.getCdrConnector();
        else
          connector = IlsConnectorPool.getIlsConnector(aRequest.getHostName());
        task.taskStart();
        connector.setup(task, aRequest);
        t = new Thread(ilsThreadGroup, connector);
        t.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        t.start();
      }
      
      log.debug("waiting for results ...");
      
      // wait for all the connector threads done
      
      long millisToGo = IlsProps.getTimeout();
      long timeout = System.currentTimeMillis() + millisToGo;
      synchronized (task) {
        while(task.count() > 0) {
          log.debug("Waiting for " + task.count() + " task(s) to finish");
          log.debug("Timeout in " + millisToGo/1000 + " seconds.");
          try {
            task.wait(millisToGo);
          } 
          catch (Exception ie) {
          }
          millisToGo = timeout - System.currentTimeMillis();
          if (millisToGo <= 0) {
            log.warn("Timeout while waiting for ILS request to return!");
            if (ilsReqs.length == 1) {
              // We will only throw the timeout exception if we had a single
              // request.  If we had multiple requests in the array we will
              // continue to process those.
              throw new IlsException("Timeout occurred while waiting for an ILS request to return!");
            } else
              break;
          }
        }
      }
      
      log.debug("complete a task and return!");
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new IlsException(e.getMessage());
    }
    finally {
    }
    return task.getResponses();
  }
  
    
}
