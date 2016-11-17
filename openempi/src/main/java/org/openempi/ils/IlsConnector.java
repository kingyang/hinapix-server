/*
 * Title:       IlsConnector
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.openempi.ils.utility.pool.Poolable;



/**
 * The ILS connector thread, called when a request targets a remote ILS node
 *
 * @author Feijian Sun
 * @version 2.1, 20020425
 */
public class IlsConnector
  implements Poolable, Connector
{
  private IlsTask aTask;
  private IlsMap aRequest;
  private Logger log;
  
  /**
   * default constructor.
   */
  public IlsConnector() throws Exception
  {
    super();
    log = Logger.getLogger("ILS");
  }
  
  /**
   * set the task for the connector to update with the results received and
   * the request to be processed
   * @param aTask the task object
   * @param aRequest the request to be processed
   */
  public void setup(IlsTask aTask, IlsMap aRequest)
  {
    this.aTask = aTask;
    this.aRequest = aRequest;
  }
  
  public void poolInit(Object param)
  {
  }
  
  public void poolDelete()
  {
  }
  
  /**
   * process the request that the connector has been assigned. invoke the ILS
   * webservice on the target ILS node and update the task with all the results
   * received, then at the end, release itself back to the IlsConnector pool
   */
  public void run()
  {
    String target = aRequest.getHostName();
    HashMap[] resSet = null;
    IlsMap[] ret = null;
    
    try {
      log.debug("Sending request to Ils Node " + target);
      Call iwsCall = getIlsWebServiceCall(aRequest.getHostName());
      
      HashMap[] reqSet = new HashMap[] {aRequest.getMap()};
      resSet = (HashMap[])iwsCall.invoke(new Object[] {reqSet});
      
      log.debug("Ils Node " + target +
                " finished <" + (resSet != null ? "" + resSet.length : "null") + ">");
      ret = IlsMap.hashMapToIlsMapArray(resSet);
    } catch (Exception e) {
      ret = null;
      log.error("Ils node " + target + " error: " + e.toString());
    } finally {
      synchronized(aTask) {
        if (ret != null) 
          aTask.addAll(Arrays.asList(ret));
        aTask.taskDone();
        aTask.notifyAll();
      }
      IlsConnectorPool.freeIlsConnector(target, this);
    }
  }
  
  private Call getIlsWebServiceCall(String target)
    throws Exception
  {
    Call iwsCall = null;
    
    try {
      String ilsNameSpace = "http://ils.carescience.com";
      
      iwsCall = (Call) (new Service()).createCall();
      
      iwsCall.setUseSOAPAction(true);
      iwsCall.setSOAPActionURI(ilsNameSpace);
      
      QName opQN = new QName("urn:ilsQuery", "query");
      iwsCall.setOperationName(opQN);
      
      target = "https://" + target + "/IlsWebService/servlet/AxisServlet";
      iwsCall.setTargetEndpointAddress(new URL(target));
    } catch (Exception e) {
      log.error(e, e);
      throw new Exception(e.getMessage());
    }
    
    return iwsCall;
  }
}
