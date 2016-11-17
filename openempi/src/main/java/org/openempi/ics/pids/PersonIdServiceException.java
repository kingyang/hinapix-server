/*
 * Title:       PersonIdServiceException
 * Description: General exception thrown by the <code>PersonIdService</code>.
 * Copyright:   (c) 2001-2003
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.pids;

/**
 * General exception thrown by the <code>PersonIdService</code>.
 *
 * @author mabundo, CareScience
 * @version 1.3, 20030122
 * @see PersonIdService
 */
public class PersonIdServiceException
  extends org.openempi.ics.utility.IcsException
{

  /**
   * Constructs a <code>PersonIdServiceException</code> with no specified detail message.
   */
  public PersonIdServiceException()
  {
    super();
  }

  /**
   * Constructs a <code>PersonIdServiceException</code> with the specified detail message.
   *
   * @param message the detail message
   */
  public PersonIdServiceException(String message)
  {
    super(message);
  }

  /**
   * Wraps a <code>Throwable</code>.
   *
   * @param throwable the <code>Throwable</code> to wrap
   */
  public PersonIdServiceException(Throwable throwable)
  {
    super(throwable);
  }
}
