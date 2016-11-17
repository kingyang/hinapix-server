/*
 * Title:        IcsEvent
 * Description:  Class that reflects an individual ICS EVENT, and stores it.
 * Copyright:    (c) 2002-2003
 * Company:      CareScience, Inc.
 *               3600 Market Street
 *               6th Floor
 *               Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openempi.data.DateOfBirth;
import org.openempi.data.Person;
import org.openempi.data.SocialSecurityNumber;

/**
 * This class encapsulates all functionality of creating and logging a single
 * ICS Event from <code>CorrelationSystem</code> and <code>DatabaseServices</code>.
 * <p>
 * All events are logged in the <b>ICS_EVENT_LOG</b> database table, with SQL
 * defined in IcsSql.xml.
 *
 * @author  J. Mangione
 * @author  M. Abundo
 * @version 1.4, 20030128
 */
public class IcsEvent {

  /** Correlation System - Duplicate person found */
  public final static String EVENT_ONADD_DUPLICATE_FOUND  = "DUPLICATE FOUND ON ADD";

  /** Correlation System - Similar person found */
  public final static String EVENT_ONADD_SIMILAR_FOUND    = "SIMILAR MATCH FOUND ON ADD";

  /** Correlation System - Expired person found */
  public final static String EVENT_ONADD_EXPIRED_FOUND    = "EXPIRED PERSON FOUND ON ADD";

  /** Database Services - Person Added */
  public final static String EVENT_PERSON_ADDED           = "PERSON ADDED";

  /** Database Services - Person Updated */
  public final static String EVENT_PERSON_UPDATED         = "PERSON UPDATED";

  /** Database Services - Person Removed */
  public final static String EVENT_PERSON_REMOVED         = "PERSON REMOVED";

  /** Database Services - Persons Merged */
  public final static String EVENT_PERSONS_MERGED         = "PERSONS MERGED";

  /** Database Services - Person Split */
  public final static String EVENT_PERSON_SPLIT           = "PERSON SPLIT";

  /** Database Services - Patient Consent Updated */
  public final static String EVENT_CONSENT_UPDATED        = "PATIENT CONSENT UPDATED";

  /**
   * Order that columns must exist in the INSERT statement for ICS_EVENT_LOG ,
   * as defined in the IcsSql XML file.
   */
  public static final int EVENT_ID                = 1;
  public static final int EVENT_TYPE              = 2;
  public static final int EVENT_DATE              = 3;
  public static final int EVENT_CURR_PERSON_ID    = 4;
  public static final int EVENT_CURR_PERSON_SSN   = 5;
  public static final int EVENT_CURR_PERSON_DOB   = 6;
  public static final int EVENT_ALT_PERSON_ID     = 7;
  public static final int EVENT_ALT_PERSON_SSN    = 8;
  public static final int EVENT_ALT_PERSON_DOB    = 9;
  public static final int EVENT_REASON            = 10;
  public static final int EVENT_USER              = 11;

  private String _eventType;
  private String _eventReason;
  private java.util.Date _eventDate;
  private Person _currPerson;
  private Person _altPerson;
  private String _userID;

  private boolean _isPersisted = false;

  // contains all SQL needed from xml file
  private IcsSqlXML icssql = IcsSqlXML.getInstance();

  private Logger log = Logger.getLogger("ICS");

  /**
   * Creates a new event to be logged.
   * <p>
   * @param eventType IcsEvent.EVENT_ type
   * @param eventDate Date/Time of this event
   * @param currPerson Person event is logged for
   * @param altPerson Similar or Duplicate person compared to current person
   */
  public IcsEvent(String eventType,
                  java.util.Date eventDate,
                  Person currPerson,
                  Person altPerson ) {
    _eventType = eventType;
    _eventDate = eventDate;
    _currPerson = currPerson;
    _altPerson = altPerson;
  }

  /**
   * Overloaded constructor which also takes a user ID and "reason" parameters.
   * <p>
   * @param eventType IcsEvent.EVENT_ type
   * @param eventDate Date/Time of this event
   * @param currPerson Person event is logged for
   * @param altPerson Similar or Duplicate person compared to current person
   * @param eventReason reason for this event
   * @param userID the user ID
   */
  public IcsEvent(String eventType,
                  java.util.Date eventDate,
                  Person currPerson,
                  Person altPerson,
                  String eventReason,
                  String userID) {
    this(eventType, eventDate, currPerson, altPerson);
    _userID = userID;
    _eventReason = eventReason;
  }

  /**
   * Saves this log to designated storage as defined in IcsSql.xml.
   * @return boolean success or failure of save.
   */
  public boolean persist() {
    Profile.begin("IcsEvent.persist");
    Connection conn = null;
    Statement st = null;
    ResultSet rs = null;
    PreparedStatement pstmt = null;

    Element sqlElement = null;      // xml element containg the SQL from person element.

    String sqlINSERTINTO = null;    // insert into tbl (col1, col2 ...
    String sqlINSERTVALUES = null;  // values (?, ?, ...
    String sqlINSERTFULL = null;    // complete sql to be executed
    String sqlGETPREID = null;      // if not using sequences, this grabs next available PK

    try {
      // retrieve the SQL from the IcsSql.xml properties file
      sqlElement = icssql.getElement("INSERT-EVENT-LOG");

      sqlINSERTINTO = sqlElement.getChildText("SQL-INSERTINTO");
      sqlINSERTVALUES = sqlElement.getChildText("SQL-INSERTVALUES");
      sqlINSERTFULL = sqlINSERTINTO + " " + sqlINSERTVALUES;

      sqlGETPREID = sqlElement.getChildText("SQL-GET-PREINSERTID");

      // retrieve connection from Jdbc Helper and build the statement
      conn = JdbcHelper.getConnection();

      if ( sqlGETPREID.trim().length() > 0 ) {
        // First, retrieve the new ID that will be used for this record
        int newID = 0;  //

        Profile.begin("Connection.createStatement");
        st = conn.createStatement();
        Profile.end("Connection.createStatement");

        Profile.begin("Statement.executeQuery");
        // should only return a single record
        rs = st.executeQuery(sqlGETPREID);
        Profile.end("Statement.executeQuery");

        while ( rs.next() ) {
          newID = rs.getInt(1);
        }

        if ( newID == 0 ) {
          throw new SQLException("Cannot retrieve a new ICS EVENT LOG ID from SQL-GET-PREINSERTID in SQL XML");
        }

        // insert using the new ID
        pstmt = eventLogBuildStmt(conn, sqlINSERTFULL, newID);
      }
      else {
        // if there's no PREID, then the column must be a sequence that is already inside the VALUE clause.
        pstmt = eventLogBuildStmt(conn, sqlINSERTFULL);
      }

      // will return null if problem encountered in buildstmt
      if ( pstmt != null ) {
          Profile.begin("PreparedStatement.execute");
          pstmt.execute();
          Profile.end("PreparedStatement.execute");

          Profile.begin("Connection.commit");
          conn.commit();
          Profile.end("Connection.commit");
      }
      _isPersisted = true;
    }
    catch ( SQLException se ) {
      log.error(se,se);
    }
    catch ( NullPointerException npe ) {
      log.error(npe,npe);
    }
    finally {
      if (pstmt != null) {
        try {
          pstmt.close();
        }
        catch ( SQLException se1 ) {
          log.error(se1,se1);
        }
      }
      if (conn != null) {
        try {
          conn.close();
        }
        catch ( SQLException se2 ) {
          log.error(se2,se2);
        }
      }
    }

    Profile.end("IcsEvent.persist");
    return _isPersisted;
  }

  public String toString() {
    String currID = null;

    if (_currPerson != null) {
      currID = _currPerson.getOid();
    }

    String altID = null;

    if ( _altPerson != null ) {
      altID = _altPerson.getOid();
    }

    StringBuffer sb = new StringBuffer();

    sb.append(getClass().getName() + ":");
    sb.append(_eventType);
    sb.append(", " + _eventDate);
    sb.append(", CURR PERSON=" + currID);
    sb.append(", ALT PERSON=" + altID);
    sb.append(", EVENT REASON=" + _eventReason);
    sb.append(", EVENT USER=" + _userID);
    sb.append(", isPersisted=" + _isPersisted);

    return sb.toString();
  }

  public boolean isPersisted() {
    return _isPersisted;
  }

  /**
   * Used to build the PreparedStatement for inserting into a ICS_EVENT_LOG table.
   * Expects a Ics_Log_Id ID in first column.
   * @return PreparedStatement
   */
  private PreparedStatement eventLogBuildStmt(Connection conn, String sql, int eventLogId) {
    PreparedStatement pstmt = null;
    List list = null;
    
    try {
      Profile.begin("Connection.prepareStatement");
      pstmt = conn.prepareStatement( sql );
      Profile.end("Connection.prepareStatement");

      // set all attributes using class constants as position of prepared statement fields
      pstmt.setInt(EVENT_ID, eventLogId);
      pstmt.setString(EVENT_TYPE, _eventType);
      pstmt.setTimestamp(EVENT_DATE, new Timestamp(_eventDate.getTime()) );
      pstmt.setString(EVENT_CURR_PERSON_ID, _currPerson.getOid() );
      
      list = _currPerson.getSocialSecurityNumbers();
      if (list != null && list.size() > 0) {
        SocialSecurityNumber ssn = (SocialSecurityNumber) list.get(0);
        pstmt.setString(EVENT_CURR_PERSON_SSN, ssn.getSSN());
      } else
        pstmt.setString(EVENT_CURR_PERSON_SSN, null);
      
      list = _currPerson.getDatesOfBirth();
      if (list != null && list.size() > 0) {
        DateOfBirth dob = (DateOfBirth) list.get(0);
        pstmt.setTimestamp(EVENT_CURR_PERSON_DOB, new Timestamp(dob.getDOB().getTime()));
      } else
        pstmt.setTimestamp(EVENT_CURR_PERSON_DOB, null);
      
      // do only if Alt person exists
      if ( _altPerson != null ) {
        pstmt.setString(EVENT_ALT_PERSON_ID, _altPerson.getOid() );

        list = _altPerson.getSocialSecurityNumbers();
        if (list != null && list.size() > 0) {
          SocialSecurityNumber ssn = (SocialSecurityNumber) list.get(0);
          pstmt.setString(EVENT_ALT_PERSON_SSN, ssn.getSSN());
        } else
          pstmt.setString(EVENT_ALT_PERSON_SSN, null);
      
        list = _altPerson.getDatesOfBirth();
        if (list != null && list.size() > 0) {
          DateOfBirth dob = (DateOfBirth) list.get(0);
          pstmt.setTimestamp(EVENT_ALT_PERSON_DOB, new Timestamp(dob.getDOB().getTime()));
        } else
          pstmt.setTimestamp(EVENT_ALT_PERSON_DOB, null);
      
      }
      else {
        // only Current person exists. Set these to null
        pstmt.setString(EVENT_ALT_PERSON_ID, null);
        pstmt.setString(EVENT_ALT_PERSON_SSN, null);
        pstmt.setString(EVENT_ALT_PERSON_DOB, null);
      }

    }
    catch ( SQLException se ) {
      log.error(se,se);
    }

    return pstmt;
  }

  /**
   * Used to build the PreparedStatement for inserting into a ICS_EVENT_LOG table.
   * Expects sequence for ID already in values clause
   * @return PreparedStatement
   */
  private PreparedStatement eventLogBuildStmt(Connection conn, String sql) {
    PreparedStatement pstmt = null;
    List list = null;
    
    try {
      Profile.begin("Connection.prepareStatement");
      pstmt = conn.prepareStatement( sql );
      Profile.end("Connection.prepareStatement");

      // set all attributes using class constants as position of prepared statement fields minus first for ID
      pstmt.setString(EVENT_TYPE-1, _eventType);
      pstmt.setTimestamp(EVENT_DATE-1, new Timestamp(_eventDate.getTime()) );
      pstmt.setString(EVENT_CURR_PERSON_ID-1, _currPerson.getOid() );

      list = _currPerson.getSocialSecurityNumbers();
      if (list != null && list.size() > 0) {
        SocialSecurityNumber ssn = (SocialSecurityNumber) list.get(0);
        pstmt.setString(EVENT_CURR_PERSON_SSN-1, ssn.getSSN());
      } else
        pstmt.setString(EVENT_CURR_PERSON_SSN-1, null);

      list = _currPerson.getDatesOfBirth();
      if (list != null && list.size() > 0) {
        DateOfBirth dob = (DateOfBirth) list.get(0);
        pstmt.setTimestamp(EVENT_CURR_PERSON_DOB-1, new Timestamp(dob.getDOB().getTime()));
      } else
        pstmt.setTimestamp(EVENT_CURR_PERSON_DOB-1, null);
      
      // do only if Alt person exists
      if ( _altPerson != null ) {
        pstmt.setString(EVENT_ALT_PERSON_ID-1, _altPerson.getOid() );

        list = _altPerson.getSocialSecurityNumbers();
        if (list != null && list.size() > 0) {
          SocialSecurityNumber ssn = (SocialSecurityNumber) list.get(0);
          pstmt.setString(EVENT_ALT_PERSON_SSN-1, ssn.getSSN());
        } else
          pstmt.setString(EVENT_ALT_PERSON_SSN-1, null);
        
        list = _altPerson.getDatesOfBirth();
        if (list != null && list.size() > 0) {
          DateOfBirth dob = (DateOfBirth) list.get(0);
          pstmt.setTimestamp(EVENT_ALT_PERSON_DOB-1, new Timestamp(dob.getDOB().getTime()));
        } else
          pstmt.setTimestamp(EVENT_ALT_PERSON_DOB-1, null);
        
      }
      else {
        // only Current person exists. Set these to null
        pstmt.setString(EVENT_ALT_PERSON_ID-1, null);
        pstmt.setString(EVENT_ALT_PERSON_SSN-1, null);
        pstmt.setString(EVENT_ALT_PERSON_DOB-1, null);
      }

      pstmt.setString(EVENT_REASON-1, _eventReason );
      pstmt.setString(EVENT_USER-1, _userID);
    }
    catch ( SQLException se ) {
      log.error(se,se);
    }

    return pstmt;
  }
}
