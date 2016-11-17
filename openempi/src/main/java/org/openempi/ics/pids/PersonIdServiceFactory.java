/*
 * Title      : PersonIdServiceFactory
 * Description: A factory class to return an instance of the PersonIdService
 * Copyright  : Copyright (c) 2001
 * Company    : CareScience
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.pids;

/**
 * A factory class to return an instance of the <code>PersonIdService</code>
 *
 * @author mabundo, CareScience
 * @version 1.3, 20010705
 * @see PersonIdService
 */
public class PersonIdServiceFactory {

  /**
   * This private constructor is defined so the compiler won't
   * generate a default public constructor.
   */
  private PersonIdServiceFactory() { }

  /**
   * @return a reference to the only instance of this class.
   */
  public static PersonIdService getInstance()
  {
    return null;
  }
}
