/*
 * Title:       IlsConnectorPool
 * Description:
 * Copyright:   (c) 2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils;

import java.util.HashMap;

import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.pool.Pool;

/**
 * A pool of IlsConnector Objects
 *
 * @author Feijian Sun
 * @version 2.1, 20020415
 */
public class IlsConnectorPool
{
  /**
   * the pool of IlsConnector objects
   */
  private static HashMap pools = new HashMap();
  
  /**
   * return an IlsConnector object from the pool
   */
  public static IlsConnector getIlsConnector(String target)
    throws Exception
  {
    Pool pool;

    synchronized(pools) {
      pool = (Pool) pools.get(target);
      if (pool == null) {
        pool = new Pool(IlsConnector.class,
                        IlsProps.getPoolMin(),
                        IlsProps.getPoolMax());
        pool.setBlockOnMax(true);
        pool.setShrinkAfter(IlsProps.getPoolShrink());
        pool.setName(target);
        pools.put(target, pool);
      }
    }
    return (IlsConnector)pool.get();
  }
  
  /**
   * send an IlsConnector object back to the pool
   * @param connector the IlsConnector object to be freed
   */
  public static void freeIlsConnector(String target, IlsConnector connector)
  {
    Pool pool;
    synchronized(pools) {
      pool = (Pool) pools.get(target);
    }
    if (pool != null) 
      pool.release(connector);
  }
}
