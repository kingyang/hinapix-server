/*
 * Title:       AdminConnector
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.admin;

import java.net.URL;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.openempi.ils.utility.IlsProps;

/**
 *
 *
 * @author Feijian Sun
 * @version 2.1, 20020425
 */
public class AdminConnector implements Runnable
{
  private Logger log = Logger.getLogger("ILS");
  public AdminMap map = null;
  public String target = null;
    
  public AdminConnector(String target)
  {
    this.target = target;
  }
  
  /**
   *
   */
  public void run()
  {
    map = new AdminMap();

    try {
      log.debug("Sending Ils Admin Request to Node: " + target);
      Call awsCall = generateCall("getStatus");
      HashMap ret  = (HashMap) awsCall.invoke(new Object[]{});
      map.setMap(ret);
      log.debug("Ils Admin Request " + target + " done");
      Thread.currentThread().notifyAll();
    } catch (IllegalMonitorStateException e) {
      // Ignore this, it is thrown on notifyAll when timeout has occurred
    } catch (Exception e) {
      log.error("Ils Admin Request " + target + " error: " + e.toString());
      log.debug("Ils Admin Request " + target + " error: " + e.toString(), e);
    }
  }
  
  private Call generateCall(String method)
  {
    try {
      
      Call awsCall = (Call) (new Service()).createCall();
      
      awsCall.setUseSOAPAction(true);
      awsCall.setSOAPActionURI("http://ils.carescience.com");
      
      QName opQN = new QName("urn:ilsAdmin", method);
      awsCall.setOperationName(opQN);
      
      String url = "https://" + target + "/IlsWebService/servlet/AxisServlet";
      awsCall.setTargetEndpointAddress(new URL(url));
      return awsCall;
    } catch (Exception e) {
      log.error(e, e);
    }
    return null;
  }
  
  public AdminMap callForStatus()
  {
    long timeout = IlsProps.getTimeout();
    Thread t = new Thread(this);
    t.setContextClassLoader(Thread.currentThread().getContextClassLoader());
    
    synchronized (t) {
      try {
        t.start();
        t.wait(timeout);
      } catch (Exception e) {
        log.error(e, e);
      }
    }
    return map;
  }
  
}
