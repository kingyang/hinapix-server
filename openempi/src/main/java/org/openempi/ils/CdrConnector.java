/*
 * Title:       CdrConnector
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openempi.ils.adapters.CdrAdapterFactory;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.IlsSystemProps;
import org.openempi.ils.utility.pool.Poolable;

/**
 * The CDR connector thread, called when a request targets the local CDR
 *
 * @author Feijian Sun
 * @version 2.1, 20020425
 */
public class CdrConnector
  implements Poolable, Connector
{
  private IlsTask aTask;
  private IlsMap aRequest;
  private int threadCnt;
  private Logger log;
  
  /**
   * default constructor.
   */
  public CdrConnector() throws Exception
  {
    super();
    log = Logger.getLogger("ILS");
  }
  
  /**
   * set the task for the connector to update with the results returned and
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
   * process the request that the connector has been assigned. query the local
   * CDR and update the task with all the results returned, then at the end,
   * release itself back to the CdrConnector pool
   */
  public void run()
  {
    String target = aRequest.getHostName();
    ArrayList resSet = null;
    
    try {
      log.debug("Request IlsMap:\n" + aRequest.toString());
      
      resSet = query(aRequest);
      if (resSet != null) 
        log.debug("Finished with response IlsMaps:\n" + resSet.toString());
      else
        log.debug("Finished with <null>");
      
    } catch (Exception e) {
      resSet = null;
      log.error(e, e);
    } finally {
      synchronized(aTask) {
        if (resSet != null) 
          aTask.addAll(resSet);
        aTask.taskDone();
        aTask.notifyAll();
      }
      CdrConnectorPool.freeCdrConnector(this);
    }
  }
  
  private ArrayList query(IlsMap aRequest)
    throws Exception
  {
    ExternalLocatorAdapter adapter = null;
    ArrayList ret = new ArrayList();
    ArrayList list;
    try {
      IlsMap[] maps = null;
      String[] codes = aRequest.getTypeCodes();
      String code;
      if (codes == null) {
        code = aRequest.getTypeCode();
        if (code == null)
          throw new IllegalArgumentException("No Type codes specified for ILS request");
        String[] foo = { code };
        codes = foo;
      }

      String domainId = aRequest.getDomainId();
      if (domainId == null) 
          throw new IllegalArgumentException("No domainId specified in ILS request!");
      for(int codeIdx = 0; codeIdx < codes.length; codeIdx++) {
        code = codes[codeIdx];
        aRequest.setTypeCode(code);
        IlsSystemProps prop = IlsProps.getSystemPropsForType(domainId, code);
        if (prop == null) {
          log.warn("Request received for domain " + domainId + " but that domain is not configured or does not support type " + code);
          continue;
        }
        aRequest.setSystemName(prop.getSystemName());
        aRequest.putAll(prop.getCustomParams());

        adapter = CdrAdapterFactory.getAdapter(domainId, code);
        maps = adapter.findLocators(aRequest);

        if (maps != null) {
          parseUrl(aRequest, maps, prop.getResultURLBase(),
                   prop.getResultURLParams(), IlsMap.RESULT_URL);
          
          parseUrl(aRequest, maps, prop.getOrderURLBase(),
                   prop.getOrderURLParams(), IlsMap.ORDER_URL);
          
          parseUrl(aRequest, maps, prop.getEncounterURLBase(),
                   prop.getEncounterURLParams(), IlsMap.ENCOUNTER_URL);
          
          ret.addAll(Arrays.asList(maps));
        }
      }
    } catch (Exception e) {
      log.error(e, e);
    }
    return ret;
  }

  private void parseUrl(IlsMap aRequest, IlsMap[] resMap,
                        String urlBase, Map params, String mapSet) 
    throws Exception
  {
    if (urlBase == null || params == null)
      return;
    
    int nParams = params.size();
    
    for (int i = 0; i < resMap.length; i++) {
      IlsMap ilsResult = resMap[i];

      // The URI's query component.
      StringBuffer url = new StringBuffer();
      url.append(urlBase);
      if (params.size() > 0) {
	url.append("?");
	Iterator iter = params.keySet().iterator();
	String amper = "";
	while(iter.hasNext()) {
	  String param = (String) iter.next();
	  String lit = (String) params.get(param);
	  url.append(amper);
	  
	  if (lit == IlsSystemProps.URL_LITERAL) {
	    url.append(param);
	  } else {
	    Object obj = ilsResult.get(param);
	    if (obj == null) {
	      log.warn("URL parameter " + param + " does not have a value specified");
	    } else {
	      String paramValue = obj.toString();
	      url.append(param).append("=").append(URLEncoder.encode(paramValue));
	    }
	  }
	  amper="&";
	} 
      }
      ilsResult.put(mapSet, url.toString());

      // Set the locator URL.
      log.debug("URL = " + url);
    }
  }
}
