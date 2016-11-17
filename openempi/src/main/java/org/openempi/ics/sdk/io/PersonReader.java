/*
 * Title:       PersonReader
 * Description: Reads a record from file to generate a <code>Person</code>
 * Copyright:   (c) 2001
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.sdk.io;

import java.io.IOException;

import org.openempi.data.Person;

/**
 * An interface that defines a method to generate a <code>Person</code> from a
 * record in a file.
 *
 * @author CareScience, Inc.
 * @version 1.3, 20010711
 */
public interface PersonReader {

  /**
   * Reads a record from a file to generate a <code>Person</code>
   *
   * @return Person populated with the values from the record
   * @throws IOException
   */
  public Person read() throws IOException;

    /**
   * Reads a record from a file and checks for existance of comment delimiters
   *
   * @return boolean
   * @throws IOException
   */
  public boolean checkComment() throws IOException;


}
