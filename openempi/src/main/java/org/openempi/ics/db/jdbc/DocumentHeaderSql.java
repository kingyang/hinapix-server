package org.openempi.ics.db.jdbc;

/**
 * Title:        DocumentHeaderSql
 * Description:  This object handles all Document Header Table functions
 * Copyright:    Copyright (c) 2002
 * Company:      CareScience, Inc.
 */


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openempi.data.DocumentHeader;
import org.openempi.ics.db.DatabaseException;
import org.openempi.ics.utility.IcsSqlXML;
import org.openempi.ics.utility.Profile;


/**
 * @version 1.13, 20020821
 * @author jmangione, CareScience
 * @author mnanchala, CareScience
 */
public class DocumentHeaderSql {

  // singleton
  private static final DocumentHeaderSql instance = new DocumentHeaderSql();

  /**
   * Order that columns must exist in the INSERT statement for DOCUMENT HEADER,
   * as defined in the IcsSql XML file.
   */
  public static final int DH_ORD_HD_ID                = 1;
  public static final int DH_ORD_PERSON_ID            = 2;
  public static final int DH_ORD_MSG_DATE             = 3;
  public static final int DH_ORD_MSG_CTRL_ID          = 4;
  public static final int DH_ORD_SEQ_NUM              = 5;
  public static final int DH_ORD_CONT_PTR             = 6;
  public static final int DH_ORD_ACK_TYPE             = 7;
  public static final int DH_ORD_APP_ACK_TYPE         = 8;
  public static final int DH_ORD_MSG_SECURITY         = 9;
  public static final int DH_ORD_REC_APP              = 10;
  public static final int DH_ORD_REC_FAC              = 11;
  public static final int DH_ORD_SEND_APP             = 12;
  public static final int DH_ORD_SEND_FAC             = 13;
  public static final int DH_ORD_EVENT_CD             = 14;
  public static final int DH_ORD_MSG_TYPE             = 15;
  public static final int DH_ORD_TRIGGER_EVENT        = 16;
  public static final int DH_ORD_MSG_STRUCTURE        = 17;

  /**
   * These are constants are used in updatePerson() to associate which fields in a MAP are
   * being updated. These are indexese into a ArrayList, AND relate to IcsSql XML file tags <UPDATE-ATTRIBUTE-TYPES>
   */
  //Do NOT change these indexes unless algorithm is also changed!!!
  public static final int UPDATEFLD_DH                = 8; // DOCUMENT HEADER TABLE

  private Logger log = Logger.getLogger("ICS");
  
  private DocumentHeaderSql() {}

  public static final DocumentHeaderSql getInstance()
  { return instance; }

  /**
   * Insert a Document Header Record into the table holding unique Document Headers objects.
   * <B>
   * From the SQL XML, if <PREINSERTID> is null, it'll expect a sequence to be in the first VALUES column,
   * else it'll use the value from that query for the new Primary Key ID.
   * <POSTINSERTID> is used if a sequence is present and the <PREINSERTID> isn't needed. This will pass back the new PKID.
   *
   * @param icssql the helper class for reading in the IcsSQL XML File
   * @param conn <code>Connection</code> used for transaction
   * @param personId Primary Key of the Person Table uniquely identifying this person
   * @param messageDate Document Header message date
   * @param sequenceNumber Document Header sequence number
   * @param continuationPointer Document Header continuation pointer
   * @param acceptAckType Document Header Acceptance Acknowledgement Type
   * @param applicationAckType Document Header Application Acknowledgement Type
   * @param messageSecurity Document Header message security
   * @param receivingApplication Document Header receiving application
   * @param receivingFacility Document Header receiving facility
   * @param sendingApplication Document Header sending application
   * @param sendingFacility Document Header sending facility
   * @param eventCode Document Header event code
   * @param messageType Document Header message type
   * @param triggerEvent Document Header trigger event
   * @param messageStructure Document Header message structure
   * @throws DatabaseException
   * @return int newly created DocumentHeader_ID for this record.
   */
  public int insertDocumentHeader(IcsSqlXML icssql, Connection conn, int personId,
                            java.util.Date messageDate,
                            String messageControlID,
                            String sequenceNumber,
                            String continuationPointer,
                            String acceptAckType,
                            String applicationAckType,
                            String messageSecurity,
                            String receivingApplication,
                            String receivingFacility,
                            String sendingApplication,
                            String sendingFacility,
                            String eventCode,
                            String messageType,
                            String triggerEvent,
                            String messageStructure
                            )
          throws DatabaseException
  {
      Profile.begin("DocumentHeaderSql.insertDocumentHeader");
    int newID = 0; // will be the current PERSON ID used for subsequent inserts into related tables.
    ResultSet rs = null;
    PreparedStatement pstmt = null;

    Element sqlElement = null;      // xml element containg the SQL from doc header element.

    String sqlINSERTINTO = null;    // insert into tbl (col1, col2 ...
    String sqlINSERTVALUES = null;  // values (?, ?, ...
    String sqlINSERTFULL = null;    // complete sql to be executed

    String sqlGETPREID = null;      // If this isn't null, it'll be used to retrieve the next value of the PK ID
    String sqlGETPOSTID = null;     // If the GETPRID is null, this will be used to retrieve the value AFTER the insert (like in sequences)

    // retrieve the element that contains all SQL needed (including sequence/ID retrieval)
    try
    { sqlElement = icssql.getElement("INSERT-DOCUMENT-HEADER"); }
    catch ( NullPointerException npe )
    { throw new DatabaseException("Cannot retrieve INSERT-DOCUMENT-HEADER from SQL XML"); }

    sqlINSERTINTO = sqlElement.getChildText("SQL-INSERTINTO");
    sqlINSERTVALUES = sqlElement.getChildText("SQL-INSERTVALUES");
    sqlINSERTFULL = sqlINSERTINTO + " " + sqlINSERTVALUES;

    sqlGETPREID = sqlElement.getChildText("SQL-GET-PREINSERTID");
    sqlGETPOSTID = sqlElement.getChildText("SQL-GET-POSTINSERTID");


    try
    {
      if ( sqlGETPREID.trim().length() > 0 )
      {
        // First, retrieve the new ID that will be used for this record
        // This ID will also be the one returned.
        Profile.begin("Connection.prepareStatement");
        pstmt = conn.prepareStatement(sqlGETPREID);
        Profile.end("Connection.prepareStatement");
        Profile.begin("PreparedStatement.executeQuery");
        rs = pstmt.executeQuery(); // should only return a single record
        Profile.end("PreparedStatement.executeQuery");
        while ( rs.next() ) { newID = rs.getInt(1); }
        pstmt.close(); pstmt = null;
        if ( newID == 0 )
        { throw new DatabaseException("Cannot retrieve a new DOCUMENT HEADER ID from SQL-GET-PREINSERTID in SQL XML"); }

        // insert using the new ID
        pstmt = documentHeaderBuildStmt(conn, sqlINSERTFULL,
                            newID,
                            personId,
                            messageDate,
                            messageControlID,
                            sequenceNumber,
                            continuationPointer,
                            acceptAckType,
                            applicationAckType,
                            messageSecurity,
                            receivingApplication,
                            receivingFacility,
                            sendingApplication,
                            sendingFacility,
                            eventCode,
                            messageType,
                            triggerEvent,
                            messageStructure
                            );
      }
      else
      {
        // if there's no PREID, then the column must be a sequence that is already inside the VALUE clause.
        pstmt = documentHeaderBuildStmt( conn, sqlINSERTFULL,
                            personId,
                            messageDate,
                            messageControlID,
                            sequenceNumber,
                            continuationPointer,
                            acceptAckType,
                            applicationAckType,
                            messageSecurity,
                            receivingApplication,
                            receivingFacility,
                            sendingApplication,
                            sendingFacility,
                            eventCode,
                            messageType,
                            triggerEvent,
                            messageStructure
                            );
      }

      log.debug("insertDocumentHeader(): " + sqlINSERTFULL);
      Profile.begin("PreparedStatement.executeUpdate");
      int rows = pstmt.executeUpdate();
      Profile.end("PreparedStatement.executeUpdate");
      log.debug("insertDocumentHeader(): rows inserted = " + rows);

      // check if there's a query needed to return the proper new Person ID
      if ( sqlGETPOSTID.trim().length() > 0 )
      {
        Profile.begin("Connection.prepareStatement");
        pstmt = conn.prepareStatement(sqlGETPOSTID);
        Profile.end("Connection.prepareStatement");
        Profile.begin("PreparedStatement.executeQuery");
        rs = pstmt.executeQuery(); // should only return a single record
        Profile.end("PreparedStatement.executeQuery");
        while ( rs.next() ) { newID = rs.getInt(1); }
        pstmt.close(); pstmt = null;
        if ( newID == 0 )
        { throw new DatabaseException("Cannot retrieve a new DOCUMENT HEADER ID from SQL-GET-POSTINSERTID in SQL XML"); }
      }

    }
    catch ( NullPointerException npe )
    { throw new DatabaseException("Cannot insert a DOCUMENT HEADER record (Check IcsSql.xml file for configuration): " + npe.toString()); }
    catch ( SQLException se )
    { throw new DatabaseException("Cannot insert a DOCUMENT HEADER record: " + se.toString()); }
    finally
    {
      try
      {
        if ( pstmt != null )  pstmt.close();
        if ( rs != null )     rs.close();
      }
      catch (SQLException se2) {}
    }

    Profile.end("DocumentHeaderSql.insertDocumentHeader");
    return newID;
  }



  /**
   * Used to build the PreparedStatement for inserting into a Document Header table.
   * Expects no Doc Header ID (assumes to use a sequence already in VALUES() clause).
   * Will subtract 1 from each constant position in order to compensate for the sequence in the first position.
   *
   * @return PreparedStatement
   */
  private static PreparedStatement documentHeaderBuildStmt(Connection conn, String sql,
                            int personId,
                            java.util.Date messageDate,
                            String messageControlID,
                            String sequenceNumber,
                            String continuationPointer,
                            String acceptAckType,
                            String applicationAckType,
                            String messageSecurity,
                            String receivingApplication,
                            String receivingFacility,
                            String sendingApplication,
                            String sendingFacility,
                            String eventCode,
                            String messageType,
                            String triggerEvent,
                            String messageStructure
                            )
      throws DatabaseException
  {
    PreparedStatement pstmt = null;

    try
    {
      Profile.begin("Connection.prepareStatement");
      pstmt = conn.prepareStatement( sql );
      Profile.end("Connection.prepareStatement");

      pstmt.setInt(DH_ORD_PERSON_ID-1, personId);

      if ( messageDate != null )
      { 
          pstmt.setTimestamp(DH_ORD_MSG_DATE-1, new Timestamp(messageDate.getTime()) ); 
      }
      else
      { 
          pstmt.setTimestamp(DH_ORD_MSG_DATE-1, new Timestamp(System.currentTimeMillis()));
      }

      pstmt.setString(DH_ORD_MSG_CTRL_ID-1, messageControlID);
      pstmt.setString(DH_ORD_SEQ_NUM-1, sequenceNumber);
      pstmt.setString(DH_ORD_CONT_PTR-1, continuationPointer);
      pstmt.setString(DH_ORD_ACK_TYPE-1, acceptAckType);
      pstmt.setString(DH_ORD_APP_ACK_TYPE-1, applicationAckType);
      pstmt.setString(DH_ORD_MSG_SECURITY-1, messageSecurity);
      pstmt.setString(DH_ORD_REC_APP-1, receivingApplication);
      pstmt.setString(DH_ORD_REC_FAC-1, receivingFacility);
      pstmt.setString(DH_ORD_SEND_APP-1, sendingApplication);
      pstmt.setString(DH_ORD_SEND_FAC-1, sendingFacility);
      pstmt.setString(DH_ORD_EVENT_CD-1, eventCode);
      pstmt.setString(DH_ORD_MSG_TYPE-1, messageType);
      pstmt.setString(DH_ORD_TRIGGER_EVENT-1, triggerEvent);
      pstmt.setString(DH_ORD_MSG_STRUCTURE-1, messageStructure);
    }
    catch ( SQLException se )
    { throw new DatabaseException("Cannot Prepare Document Header Insert Statement: " + se.toString()); }

    return pstmt;
  }



  /**
   * Used to build the PreparedStatement for deleting from a Document Header table.
   * Expects a Doc Header ID in first column only.
   *
   * @return PreparedStatement
   */
  private static PreparedStatement documentHeaderBuildStmt(Connection conn, String sql, int dhId)
      throws DatabaseException
  {
    PreparedStatement pstmt = null;

    try
    {
      Profile.begin("Connection.prepareStatement");
      pstmt = conn.prepareStatement( sql );
      Profile.end("Connection.prepareStatement");
      pstmt.setInt(DH_ORD_HD_ID, dhId);
    }
    catch ( SQLException se )
    { throw new DatabaseException("Cannot Prepare Document Header Insert Statement: " + se.toString()); }

    return pstmt;
  }


  /**
   * Used to build the PreparedStatement for inserting into a Document Header table.
   * Expects a Doc Header ID in first column.
   *
   * @return PreparedStatement
   */
  private static PreparedStatement documentHeaderBuildStmt(Connection conn, String sql,
                            int dhId,
                            int personId,
                            java.util.Date messageDate,
                            String messageControlID,
                            String sequenceNumber,
                            String continuationPointer,
                            String acceptAckType,
                            String applicationAckType,
                            String messageSecurity,
                            String receivingApplication,
                            String receivingFacility,
                            String sendingApplication,
                            String sendingFacility,
                            String eventCode,
                            String messageType,
                            String triggerEvent,
                            String messageStructure
                            )
      throws DatabaseException
  {
    PreparedStatement pstmt = null;

    try
    {
      Profile.begin("Connection.prepareStatement");
      pstmt = conn.prepareStatement( sql );
      Profile.end("Connection.prepareStatement");

      pstmt.setInt(DH_ORD_HD_ID, dhId);
      pstmt.setInt(DH_ORD_PERSON_ID, personId);

      if ( messageDate != null )
      {
          pstmt.setTimestamp(DH_ORD_MSG_DATE, new Timestamp(messageDate.getTime()) ); 
      }
      else
      { 
          pstmt.setTimestamp(DH_ORD_MSG_DATE, new Timestamp(System.currentTimeMillis()));
      }

      pstmt.setString(DH_ORD_MSG_CTRL_ID, messageControlID);
      pstmt.setString(DH_ORD_SEQ_NUM, sequenceNumber);
      pstmt.setString(DH_ORD_CONT_PTR, continuationPointer);
      pstmt.setString(DH_ORD_ACK_TYPE, acceptAckType);
      pstmt.setString(DH_ORD_APP_ACK_TYPE, applicationAckType);
      pstmt.setString(DH_ORD_MSG_SECURITY, messageSecurity);
      pstmt.setString(DH_ORD_REC_APP, receivingApplication);
      pstmt.setString(DH_ORD_REC_FAC, receivingFacility);
      pstmt.setString(DH_ORD_SEND_APP, sendingApplication);
      pstmt.setString(DH_ORD_SEND_FAC, sendingFacility);
      pstmt.setString(DH_ORD_EVENT_CD, eventCode);
      pstmt.setString(DH_ORD_MSG_TYPE, messageType);
      pstmt.setString(DH_ORD_TRIGGER_EVENT, triggerEvent);
      pstmt.setString(DH_ORD_MSG_STRUCTURE, messageStructure);
    }
    catch ( SQLException se )
    { throw new DatabaseException("Cannot Prepare Document Header Insert Statement: " + se.toString()); }

    return pstmt;
  }

  /**
   * This method is used when an UPDATE to the PERSON object is performed. This really means updating the
   * Person Table, but physically INSERTING new records into both the Document Header and Person Info tables.
   *
   * @param icssql the helper class for reading in the IcsSQL XML File
   * @param conn <code>Connection</code> used for transaction
   * @param updateFields_ contains each possible field indexed by position of UPDATEFLD_ and contains the real fieldname.
   * @param updates contains mapping of fields+names to be updated.
   * @param personId Primary Key of the Person Table uniquely identifying this person
   * @throws DatabaseException
   * @return int newly created DocumentHeader_ID for this record.
   * @deprecated
   */
  public int insertDocumentHeader(IcsSqlXML icssql, Connection conn,
                            int personId,
                            ArrayList updateFields_,
                            Map updates
                            )
          throws DatabaseException
  {
      Profile.begin("DocumentHeaderSql.insertDocumentHeader");

    int dhId = 0; // return Document Header ID.

    // Grab all attributes for updating a DocumentHeader from IcsSQL xml file
    Element attrElement = null;

    // If we're updating the DOCUMENT HEADER, it's considered a single attribute.
    // Create a new document header, then INSERT A NEW RECORD based on the current PersonID
    // -----------------------------------------------------------------------------------
    if (updates.containsKey( updateFields_.get(UPDATEFLD_DH) ))
    {

      DocumentHeader dh = (
              (DocumentHeader) updates.get(
                      DatabaseServicesJdbc.DOCUMENT_HEADER ) == null)
               ? null
               : (DocumentHeader) updates.get( DatabaseServicesJdbc.DOCUMENT_HEADER );

      if ( dh != null )
      {
          dhId = insertDocumentHeader(icssql, conn, personId,
                            dh.getMessageDate(),
                            dh.getMessageControlID(),
                            dh.getSequenceNumber(),
                            dh.getContinuationPointer(),
                            dh.getAcceptAckType(),
                            dh.getApplicationAckType(),
                            dh.getMessageStructure(),
                            dh.getReceivingApplication(),
                            dh.getReceivingFacility(),
                            dh.getSendingApplication(),
                            dh.getSendingFacility(),
                            dh.getEventCode(),
                            dh.getMessageType(),
                            dh.getTriggerEvent(),
                            dh.getMessageStructure()
                            );
      }
    } // if  Document Header Table

      Profile.end("DocumentHeaderSql.insertDocumentHeader");
    return dhId;
  }


  /**
   * This method will delete a record from the DOCUMENT_HEADER table, based on the primary key of hd_id.
   * <p>
   * A CASCADE DELETE should be on the document_header table so the person_info records will be deleted when this method is executed.
   *
   * @param icssql the helper class for reading in the IcsSQL XML File
   * @param conn <code>Connection</code> used for transaction
   * @param dhId Primary Key of Document Header Table
   * @throws DatabaseException
   * @return void
   */
  public void deleteDocumentHeader(IcsSqlXML icssql, Connection conn, int dhId)
      throws DatabaseException
  {
    // Grab all attributes for deleting a person from IcsSQL xml file
    Element sqlElement = null;
    String sqlDelete = null; // complete SQL statement

    try
    {
      sqlElement =  icssql.getElement("DELETE-DOCUMENT-HEADER");
      sqlDelete = sqlElement.getChildText("SQL-DELETE");
    }
    catch ( NullPointerException npe )
    { throw new DatabaseException("Cannot retrieve DELETE-DOCUMENT-HEADER or DELETE-ATTRIBUTE-TYPES from SQL XML"); }

    log.debug("deleteDocumentHeader() " + sqlDelete);
    PreparedStatement pstmt = documentHeaderBuildStmt(conn, sqlDelete, dhId);

    try
    {
        Profile.begin("PreparedStatement.executeUpdate");
        pstmt.executeUpdate();
        Profile.end("PreparedStatement.executeUpdate");
    }
    catch ( SQLException se )
    { throw new DatabaseException("Cannot DELETE Document Header (id="+String.valueOf(dhId)+") From Table: " + se.toString()); }
    finally
    {
      try { if ( pstmt != null ) pstmt.close(); }
      catch (SQLException se2) {}
    }

  }

  /**
   * This method will delete a record from the DOCUMENT_HEADER table, based on the HD_ID's that exist
   * for the given PersonInfoId's passed into this method.
   * <p>
   * This method is used for deleting Document Headers from Person_Info records that will be deleted,
   * where only the PersonInfoId is known. A CASCADE DELETE should be on the document_header table
   * so the person_info records will be deleted when this method is executed.
   *
   * @param icssql the helper class for reading in the IcsSQL XML File
   * @param conn <code>Connection</code> used for transaction
   * @param personInfoId Primary Key of Person_Info table to be queried for which DH_ID's to be deleted.
   * @throws DatabaseException
   * @return void
   *
   * @deprecated. use deleteDocumentHeader(IcsSqlXML icssql, Connection conn, int dhId)
   */
  public void deleteDocumentHeaderFromPersonInfo(IcsSqlXML icssql, Connection conn, int personInfoId)
      throws DatabaseException
  {
    // Grab all attributes for deleting a person from IcsSQL xml file
    Element sqlElement = null;
    String sqlDelete = null; // complete SQL statement

    try
    {
      sqlElement =  icssql.getElement("DELETE-DOCUMENT-HEADER-FROM-PERSON-INFO");
      sqlDelete = sqlElement.getChildText("SQL-DELETE");
    }
    catch ( NullPointerException npe )
    { throw new DatabaseException("Cannot retrieve DELETE-DOCUMENT-HEADER-FROM-PERSON-INFO or DELETE-ATTRIBUTE-TYPES from SQL XML"); }

    log.debug("deleteDocumentHeader() " + sqlDelete);
    PreparedStatement pstmt = documentHeaderBuildStmt(conn, sqlDelete, personInfoId);

    try
    {
        Profile.begin("PreparedStatement.executeUpdate");
        pstmt.executeUpdate();
        Profile.end("PreparedStatement.executeUpdate");
    }
    catch ( SQLException se )
    { throw new DatabaseException("Cannot DELETE Document Header from Person Info (id="+String.valueOf(personInfoId)+") From Table: " + se.toString()); }
    finally
    {
      try { if ( pstmt != null ) pstmt.close(); }
      catch (SQLException se2) {}
    }

  }

}
