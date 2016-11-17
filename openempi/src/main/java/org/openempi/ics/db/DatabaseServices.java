/*
 * Title:       DatabaseServices
 * Description: Interface used by database clients.
 * Copyright:   (c) 2001-2003
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.db;

import java.util.Iterator;
import java.util.List;

import org.openempi.data.DocumentHeader;
import org.openempi.data.Person;
import org.openempi.ics.db.jdbc.PersonInfoSql;

/**
 * Interface used by database clients; database-specific implementation details
 * are abstracted away from the client. Instances of the class are retrieved
 * through the <code>DatabaseServicesFactory</code>. Database implementation
 * classes MUST implement this interface to maintain compatibility with existing
 * database clients.
 *
 * @author CareScience
 * @version 1.6, 20030122
 * @see AttributeType
 * @see DatabaseException
 * @see DatabaseServicesFactory
 */
public interface DatabaseServices
{
    /* static final fields */
    public static final String OID = "Oid";
    public static final String RACE = "Race";
    public static final String NOTES = "Notes";
    public static final String GENDER = "Gender";
    public static final String EXPIRED = "Expired";
    public static final String ADDRESS = "Address";
    public static final String RELIGION = "Religion";
    public static final String PERSON_NAME = "PersonName";
    public static final String BIRTH_PLACE = "BirthPlace";
    public static final String MAIDEN_NAME = "MaidenName";
    public static final String NATIONALITY = "Nationality";
    public static final String ETHNIC_GROUP = "EthnicGroup";
    public static final String DATE_OF_BIRTH = "DateOfBirth";
    public static final String EMAIL_ADDRESS = "EmailAddress";
    public static final String MARITAL_STATUS = "MaritalStatus";
    public static final String SOCIAL_SECURITY = "SocialSecurity";
    public static final String DOCUMENT_HEADER = "DocumentHeader";
    public static final String DRIVERS_LICENSE = "DriversLicense";
    public static final String PATIENT_CONSENT = "PatientConsent";
    public static final String TELEPHONE_NUMBER = "TelephoneNumber";
    public static final String PRIMARY_LANGUAGE = "PrimaryLanguage";
    public static final String PERSON_IDENTIFIER = "PersonIdentifier";

  /**
   * Returns a list of <code>Person</code> objects matching the specified attribute.
   * <p>
   * @param attributeType The <code>Integer</code> attribute type to query against,
   * which should be a type in the <code>AttributeType</code> interface.
   * @param value The <code>Object</code> containing the value of the attribute
   * to search for. This must be a String or a SearchRange.
   * @return <code>List</code> of <code>Person</code> objects matching the
   * specified attribute
   * @throws DatabaseException
   */
  public List query(Integer attributeType, Object value)
    throws DatabaseException;

  /**
   * Returns a list of <code>Person</code> objects matching the specified
   * QueryParamList.

    @param params A QueryParamList.  These will be used to generate the query.
   * @return <code>List</code> of <code>Person</code> objects matching the
   * specified attribute
   * @throws DatabaseException
   */
  public List query(QueryParamList params)
    throws DatabaseException;

  /**
   * Returns a list of <code>Person</code> objects matching the specified
   * QueryParamList.

    @param params A QueryParamList.  These will be used to generate the query.
    @param usePreparedStatement A hint to make sure the query uses
                                PreparedStatements.  This is present to
                                force it to false for certain performance
                                quirks.
   * @return <code>List</code> of <code>Person</code> objects matching the
   * specified attribute
   * @throws DatabaseException
   */
  public List query(QueryParamList params, boolean usePreparedStatement)
    throws DatabaseException;

  /**
   * Returns a list of <code>Person</code> objects matching the specified attribute.
   * <p>
   * @param attributeType The <code>Integer</code> attribute type to query against,
   * which should be a type in the <code>AttributeType</code> interface.
   * @param value The <code>Object</code> containing the value of the attribute
   * to search for. This must be a String or a SearchRange.
    @param usePreparedStatement A hint to make sure the query uses
                                PreparedStatements.  This is present to
                                force it to false for certain performance
                                quirks.
   * @return List containing the results of the query.
   * @throws DatabaseException
   */
  public List query(Integer attributeType, Object value,
                    boolean usePreparedStatement)
    throws DatabaseException;

  /**
     Returns an Iterator of Person objects that match the QueryParamList
     value specified.  The total result set in the iterator will include
     everything that matches based on the sql constructed from the
     QueryParamList.  If there are no matches an empty Iterator will be
     returned.  Caller must call releaseIterator() on the returned Iterator
     when processing is finished.

    @param params A QueryParamList.  These will be used to generate the query.
    @return Iterator of Person objects
    @throws DatabaseException
   */
  public Iterator queryIterator(QueryParamList params)
      throws DatabaseException;

  /**
     Returns an Iterator of Person objects that match the QueryParamList
     value specified.  The total result set in the iterator will include
     everything that matches based on the sql constructed from the
     QueryParamList.  If there are no matches an empty Iterator will be
     returned.  Caller must call releaseIterator() on the returned Iterator
     when processing is finished.

    @param params A QueryParamList.  These will be used to generate the query.
    @param usePreparedStatement A hint to make sure the query uses
                                PreparedStatements.  This is present to
                                force it to false for certain performance
                                quirks.
    @return Iterator of Person objects
    @throws DatabaseException
   */
  public Iterator queryIterator(QueryParamList params,
                                boolean usePreparedStatement)
      throws DatabaseException;

  /**
     Called to release the iterator returned from the queryIterator()
     method.  This method is responsible for closing out the
     query, releasing connections, etc.  Once releaseIterator() has
     been called, the Iterator will no longer be usable.

    @param personIterator The iterator returned from queryIterator.
    @throws DatabaseException
   */
  public void releaseIterator(Iterator personIterator)
      throws DatabaseException;

  /**
   * Adds a <code>Person</code> to the database.
   * <p>
   * @param person The <code>Person</code> to be added to the database.
   * @return Person ID of the added person
   * @throws DatabaseException
   */
  public int addPerson(Person person) throws DatabaseException;

  /**
   * Adds a <code>Person</code> to the database.
   * <p>
   * @param person The <code>Person</code> to be added to the database.
   * @return Person ID of the added person
   * @throws DatabaseException
   */
  public int addPersonInfo(Person person) throws DatabaseException;
  
  /**
   * Removes a <code>Person</code> from the database.
   * <p>
   * @param person the <code>Person</code> to be removed from the database
   * @return true if the person was removed, false if not
   * @throws DatabaseException
   * @deprecated - No replacement
   */
  public boolean removePerson(Person person) throws DatabaseException;

  /**
   * Removes an Object from the database.
   * <p>
   * @param obj the Object to be removed from the database
   * @return true if the object was removed, false if not
   * @throws DatabaseException
   */
  public boolean removeObject(Object obj) throws DatabaseException;


  
  /**
   * This method is used to version the EID of the given person described by
   * the given 4 part key (domain, facility, MRN, EID).  This method will
   * populate the updated_corporate_id for these aliases.
   * @param facility 
   * @param MRN 
   * @param domain 
   * @param oldEID 
   * @param newEID 
   * @throws DatabaseException
   * @return The number of rows updated
   */
  public int updateEID(String domain, String facility, String MRN, String newMRN, String oldEID, String newEID) throws DatabaseException;

  /**
   * Merges multiple <code>Persons</code> into a single <code>Person</code> in
   * the database.
   * <p>
   * @param persons array of <code>Persons</code> to be merged in the database.
   * The first <code>Person</code> in the array is the one to which all the
   * others are merged.
   * @return the merged <code>Person</code>, or null if merge failed.
   * @throws DatabaseException
   */
  public Person mergePersons(Person[] persons)
    throws DatabaseException;

  /**
   * Splits a <code>Person</code> into 2 <code>Persons</code> in
   * the database.
   * <p>
   * @param person the <code>Person</code> to be split
   * @param docHeaders the array of <code>DocumentHeaders</code> to be
   * associated with the newly-created <code>Person</code>
   * @return the newly created <code>Person</code>, or null if split failed.
   * @throws DatabaseException
   */
  public int splitPerson(Person person, DocumentHeader[] docHeaders)
    throws DatabaseException;

  /**
   * Adds a <code>DatabaseServicesListener</code>.
   *
   * @param listener the <code>DatabaseServicesListener</code> to add
   * @return true if the listener was added, false if not
   */
  public boolean addListener(DatabaseServicesListener listener);

  /**
   * Removes a <code>DatabaseServicesListener</code>.
   *
   * @param listener the <code>DatabaseServicesListener</code> to remove
   * @return true if the listener was removed, false if not
   */
  public boolean removeListener(DatabaseServicesListener listener);
}
