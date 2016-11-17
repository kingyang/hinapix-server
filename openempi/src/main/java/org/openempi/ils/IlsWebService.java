/*
 * Title:       IlsWebService
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 *
 *
 * @author Feijian Sun
 * @version 2.0, 20020127
 */
public class IlsWebService
{
  /**
   * send the requests to related ILS system, then return the results back
   */
  public HashMap[] query(HashMap[] reqSet) throws Exception
  {
    Logger log = Logger.getLogger("ILS");

    if (reqSet == null)
      return null;

    log.debug("SOAP request received with " + reqSet.length + " IlsMaps!");
    IlsServiceImpl ils = new IlsServiceImpl();
    HashMap[] resSet = ils.sendRequestHashMap(reqSet);
    log.debug("SOAP request complete with " + resSet.length + " IlsMaps!");
    
    return resSet;
  }

}
