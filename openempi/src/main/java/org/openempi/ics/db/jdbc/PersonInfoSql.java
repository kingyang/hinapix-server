package org.openempi.ics.db.jdbc;

/**
 * Title:        PersonInfoSql
 * Description:  This object handles all Person Info Table functionality
 * Copyright:    Copyright (c) 2002
 * Company:      CareScience, Inc.
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openempi.data.Address;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DriversLicense;
import org.openempi.data.EmailAddress;
import org.openempi.data.EthnicGroup;
import org.openempi.data.Gender;
import org.openempi.data.MaritalStatus;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.PersonName;
import org.openempi.data.Race;
import org.openempi.data.Religion;
import org.openempi.data.SocialSecurityNumber;
import org.openempi.data.TelephoneNumber;
import org.openempi.ics.db.AttributeType;
import org.openempi.ics.db.DatabaseException;
import org.openempi.ics.db.QueryParamList;
import org.openempi.ics.utility.IcsSqlXML;
import org.openempi.ics.utility.Profile;

/**
 * @author jmangione, CareScience
 * @author mnanchala, CareScience
 * @version 1.10, 20020726
 */
public class PersonInfoSql {

    // singleton
    private static final PersonInfoSql instance = new PersonInfoSql();

    /**
     * Order that columns must exist in the INSERT statement for PERSON_INFO,
     * as defined in the IcsSql XML file.
     */
    public static final int PI_ORD_PI_ID = 1;
    public static final int PI_ORD_DH_ID = 2;
    public static final int PI_ORD_LNAME = 3;
    public static final int PI_ORD_FNAME = 4;
    public static final int PI_ORD_SECNAME = 5;
    public static final int PI_ORD_SUFFIX = 6;
    public static final int PI_ORD_PREFIX = 7;
    public static final int PI_ORD_DEGREE = 8;
    public static final int PI_ORD_NAME_TYPE_CD = 9;
    public static final int PI_ORD_NAME_TYPE_REP_CD = 10;
    public static final int PI_ORD_SEARCH_KEY = 11;
    public static final int PI_ORD_START_DATE = 12;
    public static final int PI_ORD_END_DATE = 13;
    public static final int PI_ORD_ALIAS = 14;
    public static final int PI_ORD_ADDRESS_1 = 15;
    public static final int PI_ORD_ADDRESS_2 = 16;
    public static final int PI_ORD_CITY = 17;
    public static final int PI_ORD_STATE = 18;
    public static final int PI_ORD_ZIP = 19;
    public static final int PI_ORD_COUNTRY = 20;
    public static final int PI_ORD_PARISH_CD = 21;
    public static final int PI_ORD_PA_START_DATE = 22;
    public static final int PI_ORD_PA_END_DATE = 23;
    public static final int PI_ORD_TELECOM_USE_CD = 24;
    public static final int PI_ORD_PHONE_COUNTRY_CD = 25;
    public static final int PI_ORD_PHONE_AREA_CD = 26;
    public static final int PI_ORD_PHONE_NUM = 27;
    public static final int PI_ORD_PHONE_EXT = 28;
    public static final int PI_ORD_EMAIL = 29;
    public static final int PI_ORD_GENDER = 30;
    public static final int PI_ORD_MARITAL_STATUS = 31;
    public static final int PI_ORD_DRV_LIC_NUM = 32;
    public static final int PI_ORD_DRV_LIC_STATE = 33;
    public static final int PI_ORD_DRV_LIC_DATE = 34;
    public static final int PI_ORD_ETHNIC_CD = 35;
    public static final int PI_ORD_RELIGION_CD = 36;
    public static final int PI_ORD_RACE_CD = 37;
    public static final int PI_ORD_PI_IDENTIFIER_CODE = 38;
    public static final int PI_ORD_PI_EFF_DATE = 39;
    public static final int PI_ORD_PI_EXP_DATE = 40;
    public static final int PI_ORD_PATIENT_CONSENT = 41;
    public static final int PI_ORD_ADDRESS_TYPE_CD = 42;
    public static final int PI_ORD_AA_UNIV_ID = 43;
    public static final int PI_ORD_AA_UNIV_ID_TYPE_CD = 44;
    public static final int PI_ORD_AA_NAMESPACE_ID = 45;
    public static final int PI_ORD_AF_UNIV_ID = 46;
    public static final int PI_ORD_AF_UNIV_ID_TYPE_CD = 47;
    public static final int PI_ORD_AF_NAMESPACE_ID = 48;
    public static final int PI_ORD_PI_IDENTIFIER = 49;
    public static final int PI_ORD_SSN = 50;
    public static final int PI_ORD_DOB = 51;
    public static final int PI_ORD_CORPORATE_ID = 52;


    public static final int PI_ORD_AN_IDENTIFIER = 53;
    public static final int PI_ORD_AN_IDENTIFIER_CODE = 54;
    public static final int PI_ORD_AN_AA_UNIV_ID = 55;
    public static final int PI_ORD_AN_AA_UNIV_ID_TYPE_CD = 56;
    public static final int PI_ORD_AN_AA_NAMESPACE_ID = 57;
    public static final int PI_ORD_AN_AF_UNIV_ID = 58;
    public static final int PI_ORD_AN_AF_UNIV_ID_TYPE_CD = 59;
    public static final int PI_ORD_AN_AF_NAMESPACE_ID = 60;


    private static final int PI_COLUMN_MAX = 64; // capacity of List (column count) used for adding Person Info records

    /**
     * These are constants are used in updatePerson() to associate which fields in a MAP are
     * being updated. These are indexese into a ArrayList, AND relate to IcsSql XML file tags <UPDATE-ATTRIBUTE-TYPES>
     */
    //Do NOT change these indexes unless algorithm is also changed!!!
    public static final int UPDATEFLD_PATIENT_CONSENT = 9; // PERSON INFO TABLE
    public static final int UPDATEFLD_ADDRESS = 10;
    public static final int UPDATEFLD_DRV_LICENSE = 11;
    public static final int UPDATEFLD_EMAIL = 12;
    public static final int UPDATEFLD_PERSON_IDENTIFIER = 13;
    public static final int UPDATEFLD_TELEPHONE = 14;
    public static final int UPDATEFLD_RACE = 15;
    public static final int UPDATEFLD_PERSONNAME = 16;
    public static final int UPDATEFLD_GENDER = 17;
    public static final int UPDATEFLD_RELIGION = 18;
    public static final int UPDATEFLD_ETHNIC_GROUP = 19;
    public static final int UPDATEFLD_MARITAL_STATUS = 20;
    public static final int UPDATEFLD_SSN = 21;
    public static final int UPDATEFLD_DOB = 22;

    /**
     * This are fields use in <UPDATE-PERSON-INFO-CONSENT>
     */
    public static final int UPDATECONSENT_PATIENT_CONSENT = 1;
    public static final int UPDATECONSENT_PI_IDENTIFIER = 2;

    private Logger log = Logger.getLogger("ICS");

    private PersonInfoSql() {
    }

    public static final PersonInfoSql getInstance() {
        return instance;
    }


    /**
     * Insert a Person Info Record into the table holding the DENORMALIZATION of all Person Attributes.
     * What?
     * For each Name, Address, Email, Authorizing Authority, etc..., there will be a SINGLE record in this table.
     * If there is more then one instance of ANY of those, they'll be that many records.
     * <B>
     * From the SQL XML, if <PREINSERTID> is null, it'll expect a sequence to be in the first VALUES column,
     * else it'll use the value from that query for the new Primary Key ID.
     * <POSTINSERTID> isn't required since we're not doing anything with the new created.
     *
     * @param icssql the helper class for reading in the IcsSQL XML File
     * @param conn   <code>Connection</code> used for transaction
     * @param vars   indexed by PI_ORD_xyz to extract all column values for a single record.
     * @return void
     * @throws DatabaseException
     */
    private void insertPersonInfo(IcsSqlXML icssql, Connection conn,
                                  Object[] vars)
            throws DatabaseException {
        Profile.begin("PersonInfoSql.insertPersonInfo");
        int newID = 0; // will be the current PERSON_INFO ID used
        ResultSet rs = null;
        PreparedStatement pstmt = null;

        Element sqlElement = null;      // xml element containg the SQL from person element.

        String sqlINSERTINTO = null;    // insert into tbl (col1, col2 ...
        String sqlINSERTVALUES = null;  // values (?, ?, ...
        String sqlINSERTFULL = null;    // complete sql to be executed

        String sqlGETPREID = null;      // If this isn't null, it'll be used to retrieve the next value of the PK ID

        // retrieve the element that contains all SQL needed (including sequence/ID retrieval)
        try {
            sqlElement = icssql.getElement("INSERT-PERSON-INFO");
        } catch (NullPointerException npe) {
            throw new DatabaseException("Cannot retrieve INSERT-PERSON-INFO from SQL XML");
        }

        sqlINSERTINTO = sqlElement.getChildText("SQL-INSERTINTO");
        sqlINSERTVALUES = sqlElement.getChildText("SQL-INSERTVALUES");
        sqlINSERTFULL = sqlINSERTINTO + " " + sqlINSERTVALUES;

        sqlGETPREID = sqlElement.getChildText("SQL-GET-PREINSERTID");

        log.debug("SQL: " + sqlINSERTFULL);

        try {
            if (sqlGETPREID.trim().length() > 0) {
                // First, retrieve the new ID that will be used for this record
                // This ID will also be the one returned.
                Profile.begin("Connection.prepareStatement");
                pstmt = conn.prepareStatement(sqlGETPREID);
                Profile.end("Connection.prepareStatement");
                Profile.begin("PreparedStatement.executeQuery");
                rs = pstmt.executeQuery(); // should only return a single record
                Profile.end("PreparedStatement.executeQuery");
                while (rs.next()) {
                    newID = rs.getInt(1);
                }
                pstmt.close();
                pstmt = null;
                if (newID == 0) {
                    throw new DatabaseException("Cannot retrieve a new PERSON_INFO ID from SQL-GET-PREINSERTID in SQL XML");
                }

                // insert using the new ID
                log.debug("insertPerson(): " + sqlINSERTFULL);
                pstmt = personInfoBuildStmt(conn, sqlINSERTFULL, newID, vars);
            } else {
                // if there's no PREID, then the column must be a sequence that is already inside the VALUE clause.
                pstmt = personInfoBuildStmt(conn, sqlINSERTFULL, vars);
            }

            Profile.begin("PreparedStatement.executeUpdate");
            int rows = pstmt.executeUpdate();
            Profile.end("PreparedStatement.executeUpdate");
            log.debug("insertPersonInfo(): rows inserted = " + rows);
        } catch (NullPointerException npe) {
            throw new DatabaseException("Cannot insert a PERSON INFO record (Check IcsSql.xml file for configuration): " + npe.toString());
        } catch (SQLException se) {
            throw new DatabaseException("Cannot insert a PERSON INFO record: " + se.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (rs != null) rs.close();
            } catch (SQLException se2) {
            }
        }
        Profile.end("PersonInfoSql.insertPersonInfo");
    }


    /**
     * Used to build the PreparedStatement for inserting into a Person_Info table.
     * Expects no Person Info ID in first column (assumes to use a sequence already in VALUES() clause
     * Will subtract 1 from each constant position in order to compensate for the sequence in the first position.
     *
     * @return PreparedStatement
     */
    private static PreparedStatement personInfoBuildStmt(Connection conn, String sql, Object[] vars)
            throws DatabaseException {
        PreparedStatement pstmt = null;

        try {
            Profile.begin("Connection.prepareStatement");
            pstmt = conn.prepareStatement(sql);
            Profile.end("Connection.prepareStatement");

            if (vars[PI_ORD_DH_ID] != null) {
                pstmt.setInt(PI_ORD_DH_ID - 1, ((Integer) vars[PI_ORD_DH_ID]).intValue());
            } else {
                pstmt.setNull(PI_ORD_DH_ID - 1, java.sql.Types.INTEGER);
            }

            pstmt.setString(PI_ORD_LNAME - 1, toUpper((String) vars[PI_ORD_LNAME]));
            pstmt.setString(PI_ORD_FNAME - 1, toUpper((String) vars[PI_ORD_FNAME]));
            pstmt.setString(PI_ORD_SECNAME - 1, toUpper((String) vars[PI_ORD_SECNAME]));
            pstmt.setString(PI_ORD_SUFFIX - 1, (String) vars[PI_ORD_SUFFIX]);
            pstmt.setString(PI_ORD_PREFIX - 1, (String) vars[PI_ORD_PREFIX]);
            pstmt.setString(PI_ORD_DEGREE - 1, (String) vars[PI_ORD_DEGREE]);
            pstmt.setString(PI_ORD_NAME_TYPE_CD - 1, (String) vars[PI_ORD_NAME_TYPE_CD]);
            pstmt.setString(PI_ORD_NAME_TYPE_REP_CD - 1, (String) vars[PI_ORD_NAME_TYPE_REP_CD]);
            pstmt.setString(PI_ORD_SEARCH_KEY - 1, (String) vars[PI_ORD_SEARCH_KEY]);

            if (vars[PI_ORD_START_DATE] != null) {
                pstmt.setTimestamp(PI_ORD_START_DATE - 1, new Timestamp(((java.util.Date) vars[PI_ORD_START_DATE]).getTime()));
            } else {
                pstmt.setNull(PI_ORD_START_DATE - 1, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_END_DATE] != null)
//      { pstmt.setDate(PI_ORD_END_DATE-1, new java.sql.Date( ((java.util.Date) vars[PI_ORD_END_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_END_DATE - 1, new Timestamp(((java.util.Date) vars[PI_ORD_END_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_END_DATE-1, null); }
            {
                pstmt.setNull(PI_ORD_END_DATE - 1, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_ALIAS] != null) {
                pstmt.setInt(PI_ORD_ALIAS - 1, ((Integer) vars[PI_ORD_ALIAS]).intValue());
            } else {
                pstmt.setNull(PI_ORD_ALIAS - 1, java.sql.Types.INTEGER);
            }

            pstmt.setString(PI_ORD_ADDRESS_1 - 1, (String) vars[PI_ORD_ADDRESS_1]);
            pstmt.setString(PI_ORD_ADDRESS_2 - 1, (String) vars[PI_ORD_ADDRESS_2]);
            pstmt.setString(PI_ORD_CITY - 1, (String) vars[PI_ORD_CITY]);
            pstmt.setString(PI_ORD_STATE - 1, (String) vars[PI_ORD_STATE]);
            pstmt.setString(PI_ORD_ZIP - 1, (String) vars[PI_ORD_ZIP]);
            pstmt.setString(PI_ORD_COUNTRY - 1, (String) vars[PI_ORD_COUNTRY]);
            pstmt.setString(PI_ORD_PARISH_CD - 1, (String) vars[PI_ORD_PARISH_CD]);

            if (vars[PI_ORD_PA_START_DATE] != null)
//      { pstmt.setDate(PI_ORD_PA_START_DATE-1, new java.sql.Date( ((java.util.Date) vars[PI_ORD_PA_START_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_PA_START_DATE - 1, new Timestamp(((java.util.Date) vars[PI_ORD_PA_START_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_PA_START_DATE-1, null); }
            {
                pstmt.setNull(PI_ORD_PA_START_DATE - 1, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_PA_END_DATE] != null)
//      { pstmt.setDate(PI_ORD_PA_END_DATE-1, new java.sql.Date( ((java.util.Date) vars[PI_ORD_PA_END_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_PA_END_DATE - 1, new Timestamp(((java.util.Date) vars[PI_ORD_PA_END_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_PA_END_DATE-1, null); }
            {
                pstmt.setNull(PI_ORD_PA_END_DATE - 1, java.sql.Types.DATE);
            }

            pstmt.setString(PI_ORD_TELECOM_USE_CD - 1, (String) vars[PI_ORD_TELECOM_USE_CD]);
            pstmt.setString(PI_ORD_PHONE_COUNTRY_CD - 1, (String) vars[PI_ORD_PHONE_COUNTRY_CD]);
            pstmt.setString(PI_ORD_PHONE_AREA_CD - 1, (String) vars[PI_ORD_PHONE_AREA_CD]);
            pstmt.setString(PI_ORD_PHONE_NUM - 1, (String) vars[PI_ORD_PHONE_NUM]);
            pstmt.setString(PI_ORD_PHONE_EXT - 1, (String) vars[PI_ORD_PHONE_EXT]);
            pstmt.setString(PI_ORD_EMAIL - 1, (String) vars[PI_ORD_EMAIL]);
            pstmt.setString(PI_ORD_GENDER - 1, (String) vars[PI_ORD_GENDER]);
            pstmt.setString(PI_ORD_MARITAL_STATUS - 1, (String) vars[PI_ORD_MARITAL_STATUS]);
            pstmt.setString(PI_ORD_DRV_LIC_NUM - 1, (String) vars[PI_ORD_DRV_LIC_NUM]);
            pstmt.setString(PI_ORD_DRV_LIC_STATE - 1, (String) vars[PI_ORD_DRV_LIC_STATE]);

            if (vars[PI_ORD_DRV_LIC_DATE] != null)
//      { pstmt.setDate(PI_ORD_DRV_LIC_DATE-1, new java.sql.Date( ((java.util.Date) vars[PI_ORD_DRV_LIC_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_DRV_LIC_DATE - 1, new Timestamp(((java.util.Date) vars[PI_ORD_DRV_LIC_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_DRV_LIC_DATE-1, null); }
            {
                pstmt.setNull(PI_ORD_DRV_LIC_DATE - 1, java.sql.Types.DATE);
            }

            pstmt.setString(PI_ORD_ETHNIC_CD - 1, (String) vars[PI_ORD_ETHNIC_CD]);
            pstmt.setString(PI_ORD_RELIGION_CD - 1, (String) vars[PI_ORD_RELIGION_CD]);
            pstmt.setString(PI_ORD_RACE_CD - 1, (String) vars[PI_ORD_RACE_CD]);
            pstmt.setString(PI_ORD_PI_IDENTIFIER_CODE - 1, (String) vars[PI_ORD_PI_IDENTIFIER_CODE]);

            if (vars[PI_ORD_PI_EFF_DATE] != null)
//      { pstmt.setDate(PI_ORD_PI_EFF_DATE-1, new java.sql.Date( ((java.util.Date) vars[PI_ORD_PI_EFF_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_PI_EFF_DATE - 1, new Timestamp(((java.util.Date) vars[PI_ORD_PI_EFF_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_PI_EFF_DATE-1, null); }
            {
                pstmt.setNull(PI_ORD_PI_EFF_DATE - 1, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_PI_EXP_DATE] != null)
//      { pstmt.setDate(PI_ORD_PI_EXP_DATE-1, new java.sql.Date( ((java.util.Date) vars[PI_ORD_PI_EXP_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_PI_EXP_DATE - 1, new Timestamp(((java.util.Date) vars[PI_ORD_PI_EXP_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_PI_EXP_DATE-1, null); }
            {
                pstmt.setNull(PI_ORD_PI_EXP_DATE - 1, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_PATIENT_CONSENT] != null) {
                pstmt.setInt(PI_ORD_PATIENT_CONSENT - 1, ((Integer) vars[PI_ORD_PATIENT_CONSENT]).intValue());
            } else {
                pstmt.setNull(PI_ORD_PATIENT_CONSENT - 1, java.sql.Types.INTEGER);
            }

            pstmt.setString(PI_ORD_ADDRESS_TYPE_CD - 1, (String) vars[PI_ORD_ADDRESS_TYPE_CD]);
            pstmt.setString(PI_ORD_AA_UNIV_ID - 1, (String) vars[PI_ORD_AA_UNIV_ID]);
            pstmt.setString(PI_ORD_AA_UNIV_ID_TYPE_CD - 1, (String) vars[PI_ORD_AA_UNIV_ID_TYPE_CD]);
            pstmt.setString(PI_ORD_AA_NAMESPACE_ID - 1, (String) vars[PI_ORD_AA_NAMESPACE_ID]);
            pstmt.setString(PI_ORD_AF_UNIV_ID - 1, (String) vars[PI_ORD_AF_UNIV_ID]);
            pstmt.setString(PI_ORD_AF_UNIV_ID_TYPE_CD - 1, (String) vars[PI_ORD_AF_UNIV_ID_TYPE_CD]);
            pstmt.setString(PI_ORD_AF_NAMESPACE_ID - 1, (String) vars[PI_ORD_AF_NAMESPACE_ID]);
            pstmt.setString(PI_ORD_PI_IDENTIFIER - 1, (String) vars[PI_ORD_PI_IDENTIFIER]);
            pstmt.setString(PI_ORD_SSN - 1, (String) vars[PI_ORD_SSN]);
            if (vars[PI_ORD_DOB] != null) {
                pstmt.setTimestamp(PI_ORD_DOB - 1, new Timestamp(((java.util.Date) vars[PI_ORD_DOB]).getTime()));
            } else {
                pstmt.setNull(PI_ORD_DOB - 1, java.sql.Types.DATE);
            }
            pstmt.setString(PI_ORD_CORPORATE_ID - 1, (String) vars[PI_ORD_CORPORATE_ID]);

            pstmt.setString(PI_ORD_AN_IDENTIFIER - 1, (String) vars[PI_ORD_AN_IDENTIFIER]);
            pstmt.setString(PI_ORD_AN_IDENTIFIER_CODE - 1, (String) vars[PI_ORD_AN_IDENTIFIER_CODE]);
            pstmt.setString(PI_ORD_AN_AA_UNIV_ID - 1, (String) vars[PI_ORD_AN_AA_UNIV_ID]);
            pstmt.setString(PI_ORD_AN_AA_UNIV_ID_TYPE_CD - 1, (String) vars[PI_ORD_AN_AA_UNIV_ID_TYPE_CD]);
            pstmt.setString(PI_ORD_AN_AA_NAMESPACE_ID - 1, (String) vars[PI_ORD_AN_AA_NAMESPACE_ID]);
            pstmt.setString(PI_ORD_AN_AF_UNIV_ID - 1, (String) vars[PI_ORD_AN_AF_UNIV_ID]);
            pstmt.setString(PI_ORD_AN_AF_UNIV_ID_TYPE_CD - 1, (String) vars[PI_ORD_AN_AF_UNIV_ID_TYPE_CD]);
            pstmt.setString(PI_ORD_AN_AF_NAMESPACE_ID - 1, (String) vars[PI_ORD_AN_AF_NAMESPACE_ID]);
        } catch (SQLException se) {
            throw new DatabaseException("Cannot Prepare Person_Info Insert Statement: " + se.toString());
        }

        return pstmt;
    }


    /**
     * Used to build the PreparedStatement for inserting into a Person_Info table.
     * Expects a Person_Info ID in first column.
     *
     * @return PreparedStatement
     */
    private static PreparedStatement personInfoBuildStmt(Connection conn, String sql, int personInfoId, Object[] vars)
            throws DatabaseException {
        PreparedStatement pstmt = null;

        try {
            Profile.begin("Connection.prepareStatement");
            pstmt = conn.prepareStatement(sql);
            Profile.end("Connection.prepareStatement");

            pstmt.setInt(PI_ORD_PI_ID, personInfoId);

            if (vars[PI_ORD_DH_ID] != null) {
                pstmt.setInt(PI_ORD_DH_ID, ((Integer) vars[PI_ORD_DH_ID]).intValue());
            } else {
                pstmt.setNull(PI_ORD_DH_ID, java.sql.Types.INTEGER);
            }

            pstmt.setString(PI_ORD_LNAME, toUpper((String) vars[PI_ORD_LNAME]));
            pstmt.setString(PI_ORD_FNAME, toUpper((String) vars[PI_ORD_FNAME]));
            pstmt.setString(PI_ORD_SECNAME, toUpper((String) vars[PI_ORD_SECNAME]));
            pstmt.setString(PI_ORD_SUFFIX, (String) vars[PI_ORD_SUFFIX]);
            pstmt.setString(PI_ORD_PREFIX, (String) vars[PI_ORD_PREFIX]);
            pstmt.setString(PI_ORD_DEGREE, (String) vars[PI_ORD_DEGREE]);
            pstmt.setString(PI_ORD_NAME_TYPE_CD, (String) vars[PI_ORD_NAME_TYPE_CD]);
            pstmt.setString(PI_ORD_NAME_TYPE_REP_CD, (String) vars[PI_ORD_NAME_TYPE_REP_CD]);
            pstmt.setString(PI_ORD_SEARCH_KEY, (String) vars[PI_ORD_SEARCH_KEY]);

            if (vars[PI_ORD_START_DATE] != null)
//      { pstmt.setDate(PI_ORD_START_DATE, new java.sql.Date( ((java.util.Date) vars[PI_ORD_START_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_START_DATE, new Timestamp(((java.util.Date) vars[PI_ORD_START_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_START_DATE, null); }
            {
                pstmt.setNull(PI_ORD_START_DATE, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_END_DATE] != null)
//      { pstmt.setDate(PI_ORD_END_DATE, new java.sql.Date( ((java.util.Date) vars[PI_ORD_END_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_END_DATE, new Timestamp(((java.util.Date) vars[PI_ORD_END_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_END_DATE, null); }
            {
                pstmt.setNull(PI_ORD_END_DATE, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_ALIAS] != null) {
                pstmt.setInt(PI_ORD_ALIAS, ((Integer) vars[PI_ORD_ALIAS]).intValue());
            } else {
                pstmt.setNull(PI_ORD_ALIAS, java.sql.Types.INTEGER);
            }

            pstmt.setString(PI_ORD_ADDRESS_1, (String) vars[PI_ORD_ADDRESS_1]);
            pstmt.setString(PI_ORD_ADDRESS_2, (String) vars[PI_ORD_ADDRESS_2]);
            pstmt.setString(PI_ORD_CITY, (String) vars[PI_ORD_CITY]);
            pstmt.setString(PI_ORD_STATE, (String) vars[PI_ORD_STATE]);
            pstmt.setString(PI_ORD_ZIP, (String) vars[PI_ORD_ZIP]);
            pstmt.setString(PI_ORD_COUNTRY, (String) vars[PI_ORD_COUNTRY]);
            pstmt.setString(PI_ORD_PARISH_CD, (String) vars[PI_ORD_PARISH_CD]);

            if (vars[PI_ORD_PA_START_DATE] != null)
//      { pstmt.setDate(PI_ORD_PA_START_DATE, new java.sql.Date( ((java.util.Date) vars[PI_ORD_PA_START_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_PA_START_DATE, new Timestamp(((java.util.Date) vars[PI_ORD_PA_START_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_PA_START_DATE, null); }
            {
                pstmt.setNull(PI_ORD_PA_START_DATE, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_PA_END_DATE] != null)
//      { pstmt.setDate(PI_ORD_PA_END_DATE, new java.sql.Date( ((java.util.Date) vars[PI_ORD_PA_END_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_PA_END_DATE, new Timestamp(((java.util.Date) vars[PI_ORD_PA_END_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_PA_END_DATE, null); }
            {
                pstmt.setNull(PI_ORD_PA_END_DATE, java.sql.Types.DATE);
            }

            pstmt.setString(PI_ORD_TELECOM_USE_CD, (String) vars[PI_ORD_TELECOM_USE_CD]);
            pstmt.setString(PI_ORD_PHONE_COUNTRY_CD, (String) vars[PI_ORD_PHONE_COUNTRY_CD]);
            pstmt.setString(PI_ORD_PHONE_AREA_CD, (String) vars[PI_ORD_PHONE_AREA_CD]);
            pstmt.setString(PI_ORD_PHONE_NUM, (String) vars[PI_ORD_PHONE_NUM]);
            pstmt.setString(PI_ORD_PHONE_EXT, (String) vars[PI_ORD_PHONE_EXT]);
            pstmt.setString(PI_ORD_EMAIL, (String) vars[PI_ORD_EMAIL]);
            pstmt.setString(PI_ORD_GENDER, (String) vars[PI_ORD_GENDER]);
            pstmt.setString(PI_ORD_MARITAL_STATUS, (String) vars[PI_ORD_MARITAL_STATUS]);
            pstmt.setString(PI_ORD_DRV_LIC_NUM, (String) vars[PI_ORD_DRV_LIC_NUM]);
            pstmt.setString(PI_ORD_DRV_LIC_STATE, (String) vars[PI_ORD_DRV_LIC_STATE]);

            if (vars[PI_ORD_DRV_LIC_DATE] != null)
//      { pstmt.setDate(PI_ORD_DRV_LIC_DATE, new java.sql.Date( ((java.util.Date) vars[PI_ORD_DRV_LIC_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_DRV_LIC_DATE, new Timestamp(((java.util.Date) vars[PI_ORD_DRV_LIC_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_DRV_LIC_DATE, null); }
            {
                pstmt.setNull(PI_ORD_DRV_LIC_DATE, java.sql.Types.DATE);
            }

            pstmt.setString(PI_ORD_ETHNIC_CD, (String) vars[PI_ORD_ETHNIC_CD]);
            pstmt.setString(PI_ORD_RELIGION_CD, (String) vars[PI_ORD_RELIGION_CD]);
            pstmt.setString(PI_ORD_RACE_CD, (String) vars[PI_ORD_RACE_CD]);
            pstmt.setString(PI_ORD_PI_IDENTIFIER_CODE, (String) vars[PI_ORD_PI_IDENTIFIER_CODE]);

            if (vars[PI_ORD_PI_EFF_DATE] != null)
//      { pstmt.setDate(PI_ORD_PI_EFF_DATE, new java.sql.Date( ((java.util.Date) vars[PI_ORD_PI_EFF_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_PI_EFF_DATE, new Timestamp(((java.util.Date) vars[PI_ORD_PI_EFF_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_PI_EFF_DATE, null); }
            {
                pstmt.setNull(PI_ORD_PI_EFF_DATE, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_PI_EXP_DATE] != null)
//      { pstmt.setDate(PI_ORD_PI_EXP_DATE, new java.sql.Date( ((java.util.Date) vars[PI_ORD_PI_EXP_DATE]).getTime()) ); }
            {
                pstmt.setTimestamp(PI_ORD_PI_EXP_DATE, new Timestamp(((java.util.Date) vars[PI_ORD_PI_EXP_DATE]).getTime()));
            } else
//      { pstmt.setDate(PI_ORD_PI_EXP_DATE, null); }
            {
                pstmt.setNull(PI_ORD_PI_EXP_DATE, java.sql.Types.DATE);
            }

            if (vars[PI_ORD_PATIENT_CONSENT] != null) {
                pstmt.setInt(PI_ORD_PATIENT_CONSENT, ((Integer) vars[PI_ORD_PATIENT_CONSENT]).intValue());
            } else {
                pstmt.setNull(PI_ORD_PATIENT_CONSENT, java.sql.Types.INTEGER);
            }

            pstmt.setString(PI_ORD_ADDRESS_TYPE_CD, (String) vars[PI_ORD_ADDRESS_TYPE_CD]);
            pstmt.setString(PI_ORD_AA_UNIV_ID, (String) vars[PI_ORD_AA_UNIV_ID]);
            pstmt.setString(PI_ORD_AA_UNIV_ID_TYPE_CD, (String) vars[PI_ORD_AA_UNIV_ID_TYPE_CD]);
            pstmt.setString(PI_ORD_AA_NAMESPACE_ID, (String) vars[PI_ORD_AA_NAMESPACE_ID]);
            pstmt.setString(PI_ORD_AF_UNIV_ID, (String) vars[PI_ORD_AF_UNIV_ID]);
            pstmt.setString(PI_ORD_AF_UNIV_ID_TYPE_CD, (String) vars[PI_ORD_AF_UNIV_ID_TYPE_CD]);
            pstmt.setString(PI_ORD_AF_NAMESPACE_ID, (String) vars[PI_ORD_AF_NAMESPACE_ID]);
            pstmt.setString(PI_ORD_PI_IDENTIFIER, (String) vars[PI_ORD_PI_IDENTIFIER]);
            pstmt.setString(PI_ORD_SSN, (String) vars[PI_ORD_SSN]);
            pstmt.setTimestamp(PI_ORD_DOB, (Timestamp) vars[PI_ORD_DOB]);
            pstmt.setString(PI_ORD_CORPORATE_ID, (String) vars[PI_ORD_CORPORATE_ID]);

            pstmt.setString(PI_ORD_AN_IDENTIFIER, (String) vars[PI_ORD_AN_IDENTIFIER]);
            pstmt.setString(PI_ORD_AN_IDENTIFIER_CODE, (String) vars[PI_ORD_AN_IDENTIFIER_CODE]);
            pstmt.setString(PI_ORD_AN_AA_UNIV_ID, (String) vars[PI_ORD_AN_AA_UNIV_ID]);
            pstmt.setString(PI_ORD_AN_AA_UNIV_ID_TYPE_CD, (String) vars[PI_ORD_AN_AA_UNIV_ID_TYPE_CD]);
            pstmt.setString(PI_ORD_AN_AA_NAMESPACE_ID, (String) vars[PI_ORD_AN_AA_NAMESPACE_ID]);
            pstmt.setString(PI_ORD_AN_AF_UNIV_ID, (String) vars[PI_ORD_AN_AF_UNIV_ID]);
            pstmt.setString(PI_ORD_AN_AF_UNIV_ID_TYPE_CD, (String) vars[PI_ORD_AN_AF_UNIV_ID_TYPE_CD]);
            pstmt.setString(PI_ORD_AN_AF_NAMESPACE_ID, (String) vars[PI_ORD_AN_AF_NAMESPACE_ID]);

        } catch (SQLException se) {
            throw new DatabaseException("Cannot Prepare Person_Info Insert Statement: " + se.toString());
        }

        return pstmt;
    }


    /**
     * Used to build the PreparedStatement for inserting into a Person_Info table.
     * Expects ONLY a Person_Info ID
     *
     * @return PreparedStatement
     */
    private static PreparedStatement personInfoBuildStmt(Connection conn, String sql, int personInfoId)
            throws DatabaseException {
        PreparedStatement pstmt = null;

        try {
            Profile.begin("Connection.prepareStatement");
            pstmt = conn.prepareStatement(sql);
            Profile.end("Connection.prepareStatement");

            pstmt.setInt(PI_ORD_PI_ID, personInfoId);
        } catch (SQLException se) {
            throw new DatabaseException("Cannot Prepare Person_Info Insert Statement: " + se.toString());
        }

        return pstmt;
    }


    /**
     * Used to build the PreparedStatement for update the PI_Identifier in the Person_Info table.
     *
     * @return PreparedStatement
     */
    private static PreparedStatement personInfoBuildStmt(Connection conn, String sql, String identifier, boolean consent)
            throws DatabaseException {
        PreparedStatement pstmt = null;

        try {
            Profile.begin("Connection.prepareStatement");
            pstmt = conn.prepareStatement(sql);
            Profile.end("Connection.prepareStatement");

            pstmt.setInt(UPDATECONSENT_PATIENT_CONSENT, (consent ? 1 : 0));
            pstmt.setString(UPDATECONSENT_PI_IDENTIFIER, identifier);
        } catch (SQLException se) {
            throw new DatabaseException("Cannot Prepare Person_Info UPDATE Patient Consent Statement: " + se.toString());
        }

        return pstmt;
    }


    /**
     * This method is used when INSERTING a new Person Object. It will iterate through each attribute in the person
     * class that is associated with the denormalized Person Info table. If there's more then one instance of an attribute
     * (ex: two addresses) it will insert TWO records.
     *
     * @param icssql the helper class for reading in the IcsSQL XML File
     * @param conn   <code>Connection</code> used for transaction
     * @param dhId   Document Header ID needed for part of the primary key of Person Info
     * @param person <code>Person</code> object containing all attributes.
     * @throws DatabaseException
     * @returns void
     */
    public void insertPersonInfo(IcsSqlXML icssql, Connection conn, int dhId, Person person)
            throws DatabaseException {
        Profile.begin("PersonInfoSql.insertPersonInfo");
        // column values, indexed by PI_ORD_xyz, that will be used to create the Person Info Record
        // Array MUST be pre-sized in order to load with proper indexes.
        Object[] personInfoVars = new Object[PI_COLUMN_MAX];

        // Get all the iterators for each attribute. These will all be combined into this single table.
        Iterator iteratorAddresses = person.getAddresses().iterator();
        Iterator iteratorEthnicGroups = person.getEthnicGroups().iterator();
        Iterator iteratorGenders = person.getGenders().iterator();
        Iterator iteratorMaritalStatii = person.getMaritalStatii().iterator();
        Iterator iteratorNames = person.getNames().iterator();
        Iterator iteratorRaces = person.getRaces().iterator();
        Iterator iteratorReligions = person.getReligions().iterator();
        Iterator iteratorPersonIdentifiers = person.getPersonIdentifiers().iterator();
        Iterator iteratorEmailAddresses = person.getEmailAddresses().iterator();
        Iterator iteratorTelephoneNumbers = person.getTelephoneNumbers().iterator();
        Iterator iteratorDriversLicenses = person.getDriversLicenses().iterator();
        Iterator iteratorSSNs = person.getSocialSecurityNumbers().iterator();
        Iterator iteratorDOBs = person.getDatesOfBirth().iterator();
        Iterator iteratorAccuntNumbers = person.getAccountNumbers().iterator();

        // We're going to enter into a WHILE loop that will shut off when no more attribute instances
        // are available. Typically there should only be ONE loop through this.
        boolean lookForMoreRecords = true; // default to get into the loop

        while (lookForMoreRecords) {
            // Turn this off now. It'll be switched back on only if an attribute has a current record.
            lookForMoreRecords = false;

            // Address
            if (iteratorAddresses.hasNext()) {
                Address addr = (Address) iteratorAddresses.next();
                personInfoVars[PI_ORD_ADDRESS_1] = addr.getAddress1();

                personInfoVars[PI_ORD_ADDRESS_2] = addr.getAddress2();
                personInfoVars[PI_ORD_CITY] = addr.getCity();
                personInfoVars[PI_ORD_STATE] = addr.getState();
                personInfoVars[PI_ORD_ZIP] = addr.getZipCode();
                personInfoVars[PI_ORD_COUNTRY] = addr.getCountry();
                personInfoVars[PI_ORD_ADDRESS_TYPE_CD] = addr.getAddressType();
                personInfoVars[PI_ORD_PARISH_CD] = addr.getParishCode();
                personInfoVars[PI_ORD_PA_START_DATE] = addr.getStartDate();
                personInfoVars[PI_ORD_PA_END_DATE] = addr.getEndDate();

                lookForMoreRecords = true;
            }

            // Ethnic Group
            if (iteratorEthnicGroups.hasNext()) {
                EthnicGroup ethnicGrp = (EthnicGroup) iteratorEthnicGroups.next();
                personInfoVars[PI_ORD_ETHNIC_CD] = ethnicGrp.getValue();

                lookForMoreRecords = true;
            }

            if (iteratorSSNs.hasNext()) {
                SocialSecurityNumber ssn = (SocialSecurityNumber) iteratorSSNs.next();
                personInfoVars[PI_ORD_SSN] = ssn.getSSN();

                lookForMoreRecords = true;
            }

            if (iteratorDOBs.hasNext()) {
                DateOfBirth dob = (DateOfBirth) iteratorDOBs.next();
                personInfoVars[PI_ORD_DOB] = dob.getDOB();

                lookForMoreRecords = true;
            }

            // Gender
            if (iteratorGenders.hasNext()) {
                Gender gender = (Gender) iteratorGenders.next();
                personInfoVars[PI_ORD_GENDER] = gender.getValue();

                lookForMoreRecords = true;
            }

            // Marital Status
            if (iteratorMaritalStatii.hasNext()) {
                MaritalStatus maritalStatus = (MaritalStatus) iteratorMaritalStatii.next();
                personInfoVars[PI_ORD_MARITAL_STATUS] = maritalStatus.getValue();

                lookForMoreRecords = true;
            }

            // Person Name
            if (iteratorNames.hasNext()) {
                PersonName pName = (PersonName) iteratorNames.next();
                personInfoVars[PI_ORD_LNAME] = pName.getLastName();
                personInfoVars[PI_ORD_FNAME] = pName.getFirstName();
                personInfoVars[PI_ORD_SECNAME] = pName.getSecondName();
                personInfoVars[PI_ORD_SUFFIX] = pName.getSuffix();
                personInfoVars[PI_ORD_PREFIX] = pName.getPrefix();
                personInfoVars[PI_ORD_DEGREE] = pName.getDegree();
                personInfoVars[PI_ORD_NAME_TYPE_CD] = pName.getNameTypeCode();
                personInfoVars[PI_ORD_NAME_TYPE_REP_CD] = pName.getNameRepresentationCode();
                personInfoVars[PI_ORD_SEARCH_KEY] = pName.getSearchKey();
                personInfoVars[PI_ORD_START_DATE] = pName.getStartDate();
                personInfoVars[PI_ORD_END_DATE] = pName.getEndDate();
                personInfoVars[PI_ORD_ALIAS] = new Integer((pName.isAlias() ? 1 : 0));

                lookForMoreRecords = true;
            }

            // Race
            if (iteratorRaces.hasNext()) {
                Race race = (Race) iteratorRaces.next();
                personInfoVars[PI_ORD_RACE_CD] = race.getValue();

                lookForMoreRecords = true;
            }

            // Religion
            if (iteratorReligions.hasNext()) {
                Religion religion = (Religion) iteratorReligions.next();
                personInfoVars[PI_ORD_RELIGION_CD] = religion.getValue();

                lookForMoreRecords = true;
            }

            // Person Identifier] = Assigning Authority and Assigning Facility
            if (iteratorPersonIdentifiers.hasNext()) {
                PersonIdentifier pi = (PersonIdentifier) iteratorPersonIdentifiers.next();
                personInfoVars[PI_ORD_PI_IDENTIFIER] = pi.getId();
                personInfoVars[PI_ORD_CORPORATE_ID] = pi.getCorpId();
                personInfoVars[PI_ORD_PI_IDENTIFIER_CODE] = pi.getIdentifierTypeCode();
                personInfoVars[PI_ORD_PI_EFF_DATE] = pi.getEffectiveDate();
                personInfoVars[PI_ORD_PI_EXP_DATE] = pi.getExpirationDate();
                personInfoVars[PI_ORD_PATIENT_CONSENT] = new Integer((pi.getPatientConsent() ? 1 : 0));

                if (pi.getAssigningAuthority() != null) {
                    personInfoVars[PI_ORD_AA_NAMESPACE_ID] = pi.getAssigningAuthority().getNameSpaceID();
                    personInfoVars[PI_ORD_AA_UNIV_ID] = pi.getAssigningAuthority().getUniversalID();
                    personInfoVars[PI_ORD_AA_UNIV_ID_TYPE_CD] = pi.getAssigningAuthority().getUniversalIDType();
                } else {
                    personInfoVars[PI_ORD_AA_NAMESPACE_ID] = null;
                    personInfoVars[PI_ORD_AA_UNIV_ID] = null;
                    personInfoVars[PI_ORD_AA_UNIV_ID_TYPE_CD] = null;
                }

                if (pi.getAssigningFacility() != null) {
                    personInfoVars[PI_ORD_AF_NAMESPACE_ID] = pi.getAssigningFacility().getNameSpaceID();
                    personInfoVars[PI_ORD_AF_UNIV_ID] = pi.getAssigningFacility().getUniversalID();
                    personInfoVars[PI_ORD_AF_UNIV_ID_TYPE_CD] = pi.getAssigningFacility().getUniversalIDType();
                } else {
                    personInfoVars[PI_ORD_AF_NAMESPACE_ID] = null;
                    personInfoVars[PI_ORD_AF_UNIV_ID] = null;
                    personInfoVars[PI_ORD_AF_UNIV_ID_TYPE_CD] = null;
                }


                lookForMoreRecords = true;
            }

            // Email Address
            if (iteratorEmailAddresses.hasNext()) {
                EmailAddress emailAddr = (EmailAddress) iteratorEmailAddresses.next();
                personInfoVars[PI_ORD_EMAIL] = emailAddr.getEmailAddress();

                lookForMoreRecords = true;
            }

            // Telephone Number
            if (iteratorTelephoneNumbers.hasNext()) {
                TelephoneNumber teleNum = (TelephoneNumber) iteratorTelephoneNumbers.next();
                personInfoVars[PI_ORD_TELECOM_USE_CD] = teleNum.getTelecomUseCode();
                personInfoVars[PI_ORD_PHONE_COUNTRY_CD] = teleNum.getCountryCode();
                personInfoVars[PI_ORD_PHONE_AREA_CD] = teleNum.getAreaCode();
                personInfoVars[PI_ORD_PHONE_NUM] = teleNum.getPhoneNumber();
                personInfoVars[PI_ORD_PHONE_EXT] = teleNum.getExtension();

                lookForMoreRecords = true;
            }

            // Drivers License
            if (iteratorDriversLicenses.hasNext()) {
                DriversLicense driverLic = (DriversLicense) iteratorDriversLicenses.next();
                personInfoVars[PI_ORD_DRV_LIC_DATE] = driverLic.getIssueDate();
                personInfoVars[PI_ORD_DRV_LIC_NUM] = driverLic.getNumber();
                personInfoVars[PI_ORD_DRV_LIC_STATE] = driverLic.getState();

                lookForMoreRecords = true;
            }

            // AccuntNumbers
            if (iteratorAccuntNumbers.hasNext()) {
                PersonIdentifier accountNumber = (PersonIdentifier) iteratorAccuntNumbers.next();

                personInfoVars[PI_ORD_AN_IDENTIFIER] = accountNumber.getId();
                personInfoVars[PI_ORD_AN_IDENTIFIER_CODE] = accountNumber.getIdentifierTypeCode();

                if (accountNumber.getAssigningAuthority() != null) {
                    personInfoVars[PI_ORD_AN_AA_NAMESPACE_ID] = accountNumber.getAssigningAuthority().getNameSpaceID();
                    personInfoVars[PI_ORD_AN_AA_UNIV_ID] = accountNumber.getAssigningAuthority().getUniversalID();
                    personInfoVars[PI_ORD_AN_AA_UNIV_ID_TYPE_CD] = accountNumber.getAssigningAuthority().getUniversalIDType();
                } else {
                    personInfoVars[PI_ORD_AN_AA_NAMESPACE_ID] = null;
                    personInfoVars[PI_ORD_AN_AA_UNIV_ID] = null;
                    personInfoVars[PI_ORD_AN_AA_UNIV_ID_TYPE_CD] = null;
                }

                if (accountNumber.getAssigningFacility() != null) {
                    personInfoVars[PI_ORD_AN_AF_NAMESPACE_ID] = accountNumber.getAssigningFacility().getNameSpaceID();
                    personInfoVars[PI_ORD_AN_AF_UNIV_ID] = accountNumber.getAssigningFacility().getUniversalID();
                    personInfoVars[PI_ORD_AN_AF_UNIV_ID_TYPE_CD] = accountNumber.getAssigningFacility().getUniversalIDType();
                } else {
                    personInfoVars[PI_ORD_AN_AF_NAMESPACE_ID] = null;
                    personInfoVars[PI_ORD_AN_AF_UNIV_ID] = null;
                    personInfoVars[PI_ORD_AN_AF_UNIV_ID_TYPE_CD] = null;
                }

                lookForMoreRecords = true;
            }


            // if one or more attributes were added, include Document Header ID and INSERT IT!!!
            // -----------------------------------------------------------------------------
            if (lookForMoreRecords) {
                personInfoVars[PI_ORD_DH_ID] = new Integer(dhId);
                insertPersonInfo(icssql, conn, personInfoVars);
            }

        } // while more attributes (another instance of an attribute) exist
        Profile.end("PersonInfoSql.insertPersonInfo");
    }


    private static String toUpper(String str) {
        if (str == null)
            return null;
        return str.toUpperCase();
    }

}
