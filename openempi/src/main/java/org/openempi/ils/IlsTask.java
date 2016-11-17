package org.openempi.ils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
   Class used to pass information between the IlsServiceImpl and the
   IlsConnector and CdrConnector threads.  For each request received
   by the IlsServiceImpl, an IlsTask is created to track the total
   number of tasks (IlsConnector and CdrConnector threads) spawned
   by the request and to track all the responses.  This class
   is synchronized because multiple threads fiddle with each instance
   to update state.  
*/
public class IlsTask
{
  /**
     List of IlsMap responses from all of the IlsConnector and CdrConnector
     threads
  */
  private List ilsRes = new ArrayList();

  /**
     The total number of currently active tasks (Cdr/IlsConnectors)
  */
  private int taskCnt = 0;

  protected IlsTask()
  {
  }

  /**
     Add the entire given collection of IlsMaps to the response list.

     @param c Collection of IlsMaps from a CdrConnector or IlsConnector
  */
  protected synchronized void addAll(Collection c)
  {
    ilsRes.addAll(c);
  }

  /**
     Returns the current set of IlsMap responses as an Array.

     @return IlsMap Array of responses
  */
  protected synchronized IlsMap[] getResponses()
  {
    return (IlsMap[]) ilsRes.toArray(new IlsMap[ilsRes.size()]);
  }

  /**
     Called by the IlsServiceImpl when it spawns a new IlsConnector
     or CdrConnector to bump the current task count.
   */
  protected synchronized void taskStart()
  {
    taskCnt++;
  }

  /**
     Called by the CdrConnectors or IlsConnectors when they have completed
     to decrement the current task count.
   */
  protected synchronized void taskDone()
  {
    taskCnt--;
  }

  /**
     @return The current active task count
  */
  protected synchronized int count()
  {
    return taskCnt;
  }
}
