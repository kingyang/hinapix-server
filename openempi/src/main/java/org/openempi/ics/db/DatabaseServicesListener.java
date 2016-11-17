/*
 * Title:       DatabaseServicesListener
 * Description: Interface to alert listeners of database actions.
 * Copyright:   (c) 2001-2003
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.db;

import org.openempi.data.Person;


/**
 * Interface that alerts registered listeners to database actions.
 *
 * @author CareScience
 * @version 1.4, 20030214
 */
public interface DatabaseServicesListener {

  /**
   * Called when a <code>Person</code> is added to the database.
   * <p>
   * @param person the <code>Person</code> added to the database.
   */
  void personAdded(Person person);

  /**
   * Called when a <code>Person</code> is updated in the database.
   * <p>
   * @param person the <code>Person</code> updated in the database.
   */
  void personUpdated(Person person);

  /**
   * Called when two <code>Persons</code> are merged in the database.
   * <p>
   * @param basePerson the base <code>Person</code>
   * @param mergePerson the <code>Person</code> that was merged with the
   * base <code>Person</code>
   */
  void personsMerged(Person basePerson, Person mergePerson);

  /**
   * Called when a <code>Person</code> is split in the database.
   * <p>
   * @param originalPerson the original <code>Person</code> before splitting.
   * @param newPerson the newly-created <code>Person</code> resulting from the split.
   */
  void personSplit(Person originalPerson, Person newPerson);

  /**
   * Called when a <code>Person</code> is removed from the database.
   * <p>
   * @param person the <code>Person</code> removed from the database.
   */
  void personRemoved(Person person);
}
