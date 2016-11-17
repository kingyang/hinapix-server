/*
 * Title:       CdrAdapterFactory
 * Description: Factory class
 *
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.adapters;

import org.apache.log4j.Logger;
import org.openempi.ils.ExternalLocatorAdapter;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.IlsSystemProps;

/**
 * Factory class.
 *
 * @author Arlan Pope
 * @version 1.5, 20020517
 */
public class CdrAdapterFactory
{
  /**
   * Returns the adapter specified in IlsProps.xml.
   *
   * @return ExternalLocatorAdapter
   * @throws CdrAdapterException
   */
  public static ExternalLocatorAdapter getAdapter(String domain, String type) 
      throws CdrAdapterException 
  {
    Logger log = Logger.getLogger("ILS");
    ExternalLocatorAdapter elAdapter = null;
    try {
      IlsSystemProps prop = IlsProps.getSystemPropsForType(domain, type);
      Class cdr = prop.getInterfaceClass();
      log.debug("className = " + cdr);
      elAdapter = (ExternalLocatorAdapter)cdr.newInstance();
    }
    catch (Exception e) {
      log.error(e, e);
    }

    return elAdapter;
  }
}
