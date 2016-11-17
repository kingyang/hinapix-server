/*
 * Title:       IdentityServiceException
 * Description: The IdentityServiceException class.
 * Copyright:   Copyright (c) 2001
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.pids;

import org.openempi.ics.utility.IcsException;

/**
 * General exception thrown by the <code>IdentityService</code>.
 *
 * @author mabundo, CareScience
 * @version 1.4, 20011015
 * @see IdentityService
 */
public class IdentityServiceException
  extends IcsException
{

  /**
   * Constructs a <code>IdentityServiceException</code> with no specified detail message.
   */
  public IdentityServiceException()
  {
    super();
  }

  /**
   * Constructs a <code>IdentityServiceException</code> with the specified detail message.
   *
   * @param message the detail message
   */
  public IdentityServiceException(String message)
  {
    super(message);
  }

  /**
   * Wraps a <code>Throwable</code>.
   *
   * @param throwable the <code>Throwable</code> to wrap
   */
  public IdentityServiceException(Throwable throwable)
  {
    super(throwable);
  }
}
