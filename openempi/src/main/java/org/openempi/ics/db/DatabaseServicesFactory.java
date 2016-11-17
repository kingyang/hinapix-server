/*
 * Title:       DatabaseServicesFactory
 * Description: The DatabaseServicesFactory class.
 * Copyright:   (c) 2001
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.db;

import org.openempi.ics.db.jdbc.DatabaseServicesJdbc;

/**
 * A factory class to return the appropriate database implementation class.
 * Currently, this can only return an instance of
 * <code>com.carescience.ics.db.jdbc.DatabaseServicesJdbc</code>. If a different
 * database implementation is used, this class will have to be expanded.
 *
 * @author CareScience
 * @version 1.5, 20020614
 * @see DatabaseServices
 */
public class DatabaseServicesFactory {

  /**
   * Private constructor as this class should never be instantiated.
   */
  private DatabaseServicesFactory() {}

  /**
   * Gets an instance of <code>DatabaseServices</code>.
   *
   * @return an instance of <code>DatabaseServices</code>
   */
  public static DatabaseServices getInstance() {
    return DatabaseServicesJdbc.getInstance();
  }
}
