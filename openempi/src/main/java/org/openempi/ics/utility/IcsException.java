/*
 * Title:       IcsException
 * Description: The IcsException class.
 * Copyright:   (c) 2001
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.utility;

/**
 * Base class for all CDE exceptions.
 *
 * @author CareScience
 * @version 1.1, 20011004
 */
public class IcsException
  extends Exception
{
  /**
   * Constructs a <code>IcsException</code> with no specified detail message.
   */
  public IcsException() {
    super();
  }

  /**
   * Constructs a <code>IcsException</code> with the specified detail message.
   *
   * @param message the detail message
   */
  public IcsException(String message)
  {
    super(message);
  }

  /**
   * Wraps a <code>Throwable</code> within a <code>IcsException</code>.
   *
   * @param throwable the <code>Throwable</code> to wrap
   */
  public IcsException(Throwable throwable)
  {
    this(throwable.toString());
  }
}
