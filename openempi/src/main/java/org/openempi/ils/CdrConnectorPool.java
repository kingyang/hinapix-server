/*
 * Title:       CdrConnectorPool
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils;

import org.apache.log4j.Logger;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.pool.Pool;

/**
 * A pool of CdrConnector Objects
 *
 * @author Feijian Sun
 * @version 2.1, 20020415
 */
public class CdrConnectorPool
{
  /**
   * the pool of CdrConnector objects
   */
  private static Pool connectorPool;
  
  /**
   *
   */
  static {
    try {
      connectorPool = new Pool(CdrConnector.class,
                               IlsProps.getPoolMin(),
                               IlsProps.getPoolMax());
      connectorPool.setBlockOnMax(true);
      connectorPool.setShrinkAfter(IlsProps.getPoolShrink());
    } catch (Exception e) {
      Logger log = Logger.getLogger("ILS");
      log.fatal(e);
    }
  }
  
  /**
   * return an CdrConnector object from the pool
   */
  public static CdrConnector getCdrConnector() throws Exception
  {
    return (CdrConnector)connectorPool.get();
  }
  
  /**
   * send an CdrConnector object back to the pool
   * @param connector the CdrConnector object to be freed
   */
  public static void freeCdrConnector(CdrConnector connector)
  {
    connectorPool.release(connector);
  }
}
