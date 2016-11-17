/*
 * Title:       AdminWebService
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.admin;

import java.util.HashMap;

import org.openempi.ILS_Version;
import org.openempi.ils.utility.IlsProps;

/**
 *
 *
 * @author Feijian Sun
 * @version 2.0, 20020127
 */
public class AdminWebService
{

  public HashMap getStatus() throws Exception
  {
    AdminMap map = new AdminMap();
    map.setIsRunning(true);
    map.setHost(IlsProps.getHost());
    map.setVersion(ILS_Version.getVersion());
    return map.getMap();
  }
  
}
