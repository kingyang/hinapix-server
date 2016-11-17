/*
 * Title:       DatabaseException
 * Description: The DatabaseException class.
 * Copyright:   (c) 2001
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.db;

/**
 * Exception thrown by <code>DatabaseServices</code>.
 *
 * @author CareScience
 * @version 1.1, 20011004
 * @see DatabaseServices
 */
public class DatabaseException extends org.openempi.ics.utility.IcsException {

  /**
   * Constructs a <code>DatabaseException</code> with no specified detail message.
   */
  public DatabaseException()
  {
    super();
  }

  /**
   * Constructs a <code>DatabaseException</code> with the specified detail message.
   *
   * @param message the detail message
   */
  public DatabaseException(String message)
  {
    super(message);
    printStackTrace();
  }

  /**
   * Wraps a <code>Throwable</code>.
   *
   * @param throwable the <code>Throwable</code> to wrap
   */
  public DatabaseException(Throwable throwable)
  {
    super(throwable);
    printStackTrace();
  }
}
