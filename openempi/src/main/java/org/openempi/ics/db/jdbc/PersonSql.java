package org.openempi.ics.db.jdbc;

/**
 * Title:        Person
 * Description:  This object handles Person Table functions.
 * Copyright:    Copyright (c) 2002
 * Company:      CareScience, Inc.
 *
 * @author J. Mangione
 * @version
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openempi.data.Person;
import org.openempi.data.PersonName;
import org.openempi.ics.db.DatabaseException;
import org.openempi.ics.utility.IcsSqlXML;
import org.openempi.ics.utility.Profile;


/**
 * This class handles all access directly to database, as called by
 * <code>DatabaseServicesJdbc</code>.
 * <br>
 * This class uses defined in <code>IcsSqlXML</code>. It currently supports
 * JDBC 2.0.
 *
 * @author CareScience
 * @author mnanchala, CareScience
 * @version 1.12, 20020730
 */

public class PersonSql {

    // singleton
    private static final PersonSql instance = new PersonSql();

    /**
     * Order that columns must exist in the INSERT statement for PERSON ,
     * as defined in the IcsSql XML file.
     */
    public static final int PERSON_ORD_PERSON_ID = 1;
    public static final int PERSON_ORD_NATIONALITY_CD = 2;
    public static final int PERSON_ORD_NAMESEARCHKEY = 3;
    public static final int PERSON_ORD_PRIMLANG = 4;
    public static final int PERSON_ORD_EXPIRED = 5;
    public static final int PERSON_ORD_ISPROVIDER = 6;
    public static final int PERSON_ORD_MAIDEN_LNAME = 7;
    public static final int PERSON_ORD_MAIDEN_FNAME = 8;
    public static final int PERSON_ORD_MAIDEN_SECNAME = 9;
    public static final int PERSON_ORD_MAIDEN_SUFFIX = 10;
    public static final int PERSON_ORD_MAIDEN_PREFIX = 11;
    public static final int PERSON_ORD_MAIDEN_DEGREE = 12;
    public static final int PERSON_ORD_MAIDEN_NAME_TYPE_CD = 13;
    public static final int PERSON_ORD_MAIDEN_NAME_TYPE_REP_CD = 14;
    public static final int PERSON_ORD_MAIDEN_SEARCH_KEY = 15;
    public static final int PERSON_ORD_BIRTH_PLACE = 16;


    /**
     * These are constants are used in updatePerson() to associate which fields in a MAP are
     * being updated. These are indexese into a ArrayList, AND relate to IcsSql XML file tags <UPDATE-ATTRIBUTE-TYPES>
     */
    //Do NOT change these indexes unless algorithm is also changed!!!
    public static final int UPDATEFLD_EXPIRED = 0; // PERSON TABLE
    public static final int UPDATEFLD_NATIONALITY = 1;
    public static final int UPDATEFLD_PRIMARYLANGUAGE = 2;
    public static final int UPDATEFLD_BIRTHPLACE = 3;
    public static final int UPDATEFLD_MAIDENNAME = 4;

    private static Logger log = Logger.getLogger("ICS");

    private PersonSql() {
    }

    public static final PersonSql getInstance() {
        return instance;
    }

    /**
     * Used to build the PreparedStatement for inserting into a Person table.
     * Expects no person ID (assumes to use a sequence already in VALUES() clause).
     * Will subtract 1 from each constant position in order to compensate for the sequence in the first position.
     * @return PreparedStatement
     */
    private static PreparedStatement personBuildStmt(Connection conn, String sql,
                                                     String nationCd,
                                                     String nameSearchKey,
                                                     String primLanguageCd,
                                                     boolean expired,
                                                     boolean isProvider,
                                                     String birthPlace,
                                                     PersonName maidenName
    )
            throws DatabaseException {
        Profile.begin("PersonSql.personBuildStmt");
        PreparedStatement pstmt = null;

        try {
            log.debug(sql);
            Profile.begin("Connection.prepareStatement");
            pstmt = conn.prepareStatement(sql);
            Profile.end("Connection.prepareStatement");

            pstmt.setString(PERSON_ORD_NATIONALITY_CD - 1, nationCd);
            pstmt.setString(PERSON_ORD_NAMESEARCHKEY - 1, nameSearchKey);
            pstmt.setString(PERSON_ORD_PRIMLANG - 1, primLanguageCd);

            pstmt.setInt(PERSON_ORD_EXPIRED - 1, new Integer((expired ? 1 : 0)).intValue());
            pstmt.setInt(PERSON_ORD_ISPROVIDER - 1, new Integer((isProvider ? 1 : 0)).intValue());

            pstmt.setString(PERSON_ORD_BIRTH_PLACE - 1, birthPlace);


            if (maidenName != null) {
                pstmt.setString(PERSON_ORD_MAIDEN_LNAME - 1, maidenName.getLastName());
                pstmt.setString(PERSON_ORD_MAIDEN_FNAME - 1, maidenName.getFirstName());
                pstmt.setString(PERSON_ORD_MAIDEN_SECNAME - 1, maidenName.getSecondName());
                pstmt.setString(PERSON_ORD_MAIDEN_SUFFIX - 1, maidenName.getSuffix());
                pstmt.setString(PERSON_ORD_MAIDEN_PREFIX - 1, maidenName.getPrefix());
                pstmt.setString(PERSON_ORD_MAIDEN_DEGREE - 1, maidenName.getDegree());
                pstmt.setString(PERSON_ORD_MAIDEN_NAME_TYPE_CD - 1, maidenName.getNameTypeCode());
                pstmt.setString(PERSON_ORD_MAIDEN_NAME_TYPE_REP_CD - 1, maidenName.getNameRepresentationCode());
                pstmt.setString(PERSON_ORD_MAIDEN_SEARCH_KEY - 1, maidenName.getSearchKey());
            } else {
                pstmt.setString(PERSON_ORD_MAIDEN_LNAME - 1, null);
                pstmt.setString(PERSON_ORD_MAIDEN_FNAME - 1, null);
                pstmt.setString(PERSON_ORD_MAIDEN_SECNAME - 1, null);
                pstmt.setString(PERSON_ORD_MAIDEN_SUFFIX - 1, null);
                pstmt.setString(PERSON_ORD_MAIDEN_PREFIX - 1, null);
                pstmt.setString(PERSON_ORD_MAIDEN_DEGREE - 1, null);
                pstmt.setString(PERSON_ORD_MAIDEN_NAME_TYPE_CD - 1, null);
                pstmt.setString(PERSON_ORD_MAIDEN_NAME_TYPE_REP_CD - 1, null);
                pstmt.setString(PERSON_ORD_MAIDEN_SEARCH_KEY - 1, null);
            }

        } catch (SQLException se) {
            throw new DatabaseException("Cannot Prepare Person Insert Statement: " + se.toString());
        }

        Profile.end("PersonSql.personBuildStmt");
        return pstmt;
    }


    /**
     * Used to build the PreparedStatement for inserting into a Person table.
     * Expects a Person ID in first column.
     * @return PreparedStatement
     */
    private static PreparedStatement personBuildStmt(Connection conn, String sql,
                                                     int personId,
                                                     String nationCd,
                                                     String nameSearchKey,
                                                     String primLanguageCd,
                                                     boolean expired,
                                                     boolean isProvider,
                                                     String birthPlace,
                                                     PersonName maidenName
    )
            throws DatabaseException {
        Profile.begin("PersonSql.personBuildStmt");
        PreparedStatement pstmt = null;

        try {
            log.debug(sql);
            Profile.begin("Connection.prepareStatement");
            pstmt = conn.prepareStatement(sql);
            Profile.end("Connection.prepareStatement");

            pstmt.setInt(PERSON_ORD_PERSON_ID, personId);
            pstmt.setString(PERSON_ORD_NATIONALITY_CD, nationCd);
            pstmt.setString(PERSON_ORD_NAMESEARCHKEY, nameSearchKey);
            pstmt.setString(PERSON_ORD_PRIMLANG, primLanguageCd);

            pstmt.setInt(PERSON_ORD_EXPIRED, new Integer((expired ? 1 : 0)).intValue());
            pstmt.setInt(PERSON_ORD_ISPROVIDER, new Integer((isProvider ? 1 : 0)).intValue());

            pstmt.setString(PERSON_ORD_BIRTH_PLACE, birthPlace);


            if (maidenName != null) {
                pstmt.setString(PERSON_ORD_MAIDEN_LNAME, maidenName.getLastName());
                pstmt.setString(PERSON_ORD_MAIDEN_FNAME, maidenName.getFirstName());
                pstmt.setString(PERSON_ORD_MAIDEN_SECNAME, maidenName.getSecondName());
                pstmt.setString(PERSON_ORD_MAIDEN_SUFFIX, maidenName.getSuffix());
                pstmt.setString(PERSON_ORD_MAIDEN_PREFIX, maidenName.getPrefix());
                pstmt.setString(PERSON_ORD_MAIDEN_DEGREE, maidenName.getDegree());
                pstmt.setString(PERSON_ORD_MAIDEN_NAME_TYPE_CD, maidenName.getNameTypeCode());
                pstmt.setString(PERSON_ORD_MAIDEN_NAME_TYPE_REP_CD, maidenName.getNameRepresentationCode());
                pstmt.setString(PERSON_ORD_MAIDEN_SEARCH_KEY, maidenName.getSearchKey());
            } else {
                pstmt.setString(PERSON_ORD_MAIDEN_LNAME, null);
                pstmt.setString(PERSON_ORD_MAIDEN_FNAME, null);
                pstmt.setString(PERSON_ORD_MAIDEN_SECNAME, null);
                pstmt.setString(PERSON_ORD_MAIDEN_SUFFIX, null);
                pstmt.setString(PERSON_ORD_MAIDEN_PREFIX, null);
                pstmt.setString(PERSON_ORD_MAIDEN_DEGREE, null);
                pstmt.setString(PERSON_ORD_MAIDEN_NAME_TYPE_CD, null);
                pstmt.setString(PERSON_ORD_MAIDEN_NAME_TYPE_REP_CD, null);
                pstmt.setString(PERSON_ORD_MAIDEN_SEARCH_KEY, null);
            }

        } catch (SQLException se) {
            throw new DatabaseException("Cannot Prepare Person Insert Statement: " + se.toString());
        }

        Profile.end("PersonSql.personBuildStmt");
        return pstmt;
    }


    /**
     * Used to build the PreparedStatement for updating a Person table.
     * Expects a Person ID as PK for the where clause (only ONE column)
     * @return PreparedStatement
     */
    private static PreparedStatement personBuildStmt(Connection conn, String sql,
                                                     int personId,
                                                     List valueList)
            throws DatabaseException {
        Profile.begin("PersonSql.personBuildStmt");
        PreparedStatement pstmt = null;
        int idx;

        try {
            log.debug(sql);
            Profile.begin("Connection.prepareStatement");
            pstmt = conn.prepareStatement(sql);
            Profile.end("Connection.prepareStatement");

            idx = 1;
            Iterator iter = valueList.iterator();
            while (iter.hasNext()) {
                pstmt.setObject(idx++, iter.next());
            }
            pstmt.setInt(idx++, personId);
        } catch (SQLException se) {
            throw new DatabaseException("Cannot prepare Person Statement: " + se.toString());
        }
        Profile.end("PersonSql.personBuildStmt");
        return pstmt;
    }


    /**
     * Insert a Person Record into the table holding unique Person objects, plus any non-provider data in the Personal table.
     * <B>
     * From the SQL XML, if <PREINSERTID> is null, it'll expect a sequence to be in the first VALUES column,
     * else it'll use the value from that query for the new Primary Key ID.
     * <POSTINSERTID> is used if a sequence is present and the <PREINSERTID> isn't needed. This will pass back the new PKID.
     *
     * @param icssql the helper class for reading in the IcsSQL XML File
     * @param conn <code>Connection</code> used for transaction
     * @param nationCd nationality code
     * @param nameSearchKey
     * @param primLanguageCd primary language code
     * @param birthPlace
     * @throws DatabaseException
     * @return int newly created PersonId for this record.
     */
    public int insertPerson(IcsSqlXML icssql, Connection conn, String nationCd,
                            String nameSearchKey,
                            String primLanguageCd,
                            boolean expired,
                            boolean isProvider,
                            String birthPlace,
                            PersonName maidenName
    )
            throws DatabaseException {
        Profile.begin("PersonSql.insertPerson");
        int newID = 0; // will be the current PERSON ID used for subsequent inserts into related tables.
        ResultSet rs = null;
        PreparedStatement pstmt = null;

        Element sqlElement = null;      // xml element containg the SQL from person element.

        String sqlINSERTINTO = null;    // insert into tbl (col1, col2 ...
        String sqlINSERTVALUES = null;  // values (?, ?, ...
        String sqlINSERTFULL = null;    // complete sql to be executed

        String sqlGETPREID = null;      // If this isn't null, it'll be used to retrieve the next value of the PK ID
        String sqlGETPOSTID = null;     // If the GETPRID is null, this will be used to retrieve the value AFTER the insert (like in sequences)

        // retrieve the element that contains all SQL needed (including sequence/ID retrieval)
        try {
            sqlElement = icssql.getElement("INSERT-PERSON");
        } catch (NullPointerException npe) {
            throw new DatabaseException("Cannot retrieve INSERT-PERSON from SQL XML");
        }

        sqlINSERTINTO = sqlElement.getChildText("SQL-INSERTINTO");
        sqlINSERTVALUES = sqlElement.getChildText("SQL-INSERTVALUES");
        sqlINSERTFULL = sqlINSERTINTO + " " + sqlINSERTVALUES;

        sqlGETPREID = sqlElement.getChildText("SQL-GET-PREINSERTID");
        sqlGETPOSTID = sqlElement.getChildText("SQL-GET-POSTINSERTID");

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
                    throw new DatabaseException("Cannot retrieve a new PERSON ID from SQL-GET-PREINSERTID in SQL XML");
                }

                // insert using the new ID
                pstmt = personBuildStmt(conn, sqlINSERTFULL,
                        newID,
                        nationCd,
                        nameSearchKey,
                        primLanguageCd,
                        expired,
                        isProvider,
                        birthPlace,
                        maidenName
                );

            } else {
                // if there's no PREID, then the column must be a sequence that is already inside the VALUE clause.
                pstmt = personBuildStmt(conn, sqlINSERTFULL,
                        nationCd,
                        nameSearchKey,
                        primLanguageCd,
                        expired,
                        isProvider,
                        birthPlace,
                        maidenName
                );

            }

            log.debug("insertPerson(): " + sqlINSERTFULL);

            Profile.begin("PreparedStatement.executeUpdate");
            int rows = pstmt.executeUpdate();
            Profile.end("PreparedStatement.executeUpdate");
            pstmt.close();
            pstmt = null;
            log.debug("insertPerson(): rows inserted = " + rows);

            // check if there's a query needed to return the proper new Person ID
            if (sqlGETPOSTID.trim().length() > 0) {
                Profile.begin("Connection.prepareStatement");
                pstmt = conn.prepareStatement(sqlGETPOSTID);
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
                    throw new DatabaseException("Cannot retrieve a new PERSON ID from SQL-GET-POSTINSERTID in SQL XML");
                }
            }

        } catch (NullPointerException npe) {
            throw new DatabaseException("Cannot insert a PERSON record (Check IcsSql.xml file for configuration): " + npe.toString());
        } catch (SQLException se) {
            throw new DatabaseException("Cannot insert a PERSON record: " + se.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (rs != null) rs.close();
            } catch (SQLException se2) {
            }
        }

        Profile.end("PersonSql.insertPerson");
        return newID;
    }




    /**
     * This method will create an UPDATE statement from all non-null fields in the Person object and execute it.
     * Will PERSON Table.
     *
     * @param icssql the helper class for reading in the IcsSQL XML File
     * @param conn <code>Connection</code> used for transaction
     * @param personId Primary Key of Person Table uniquely identifying a Person Object
     * @param person
     * @throws DatabaseException
     * @return void
     */
    public void updatePerson(IcsSqlXML icssql, Connection conn, int personId, Person person)
            throws DatabaseException {
        Profile.begin("PersonSql.updatePerson2");
        // Grab all attributes for updating a person from IcsSQL xml file
        Element attrElement = null;
        Element sqlElement = null;
        String sqlUpdateTable = null; // these two are read in from xml
        String sqlUpdateWhere = null;
        String sqlUpdateSet = "";  // this gets built dynamically
        String sqlUpdate = null; // complete SQL statement
        String str = null;  // work variable
        ArrayList valueList = new ArrayList();

        try {
            sqlElement = icssql.getElement("UPDATE-PERSON");
            sqlUpdateTable = sqlElement.getChildText("SQL-UPDATETABLE");
            sqlUpdateWhere = sqlElement.getChildText("SQL-UPDATEWHERE");
        } catch (NullPointerException npe) {
            throw new DatabaseException("Cannot retrieve UPDATE-PERSON or UPDATE-ATTRIBUTE-TYPESS from SQL XML");
        }


        // Check each attribute.
        // If it's not null, then do two things:
        //    1) Get it's attribute-type from the config file
        //    2) build the set statement and concat with the entire update statement
        //
        // How is UPDATE-ATTRIBUTE-TYPES being used?
        // The ATTR-# in the IcsSql XML file maps directly to the indexes in the updateFields_ array.
        // This way we can dynamically build the SET clause and retrieve the real column name/type.


        // Expired
        if (person.isExpired()) {
            try {
                attrElement = icssql.getElement("UPDATE-ATTRIBUTE-TYPES").getChild(IcsSqlXML.ATTR_TAG + String.valueOf(UPDATEFLD_EXPIRED).trim());
            } catch (NullPointerException npe) {
                throw new DatabaseException("Cannot retrieve ATTRIBUTE <" + String.valueOf(UPDATEFLD_EXPIRED) + "> in UPDATE-ATTRIBUTE-TYPES from SQL XML");
            }

            sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("COLNAME"),
                    attrElement.getChildText("COLTYPE"),
                    String.valueOf((person.isExpired() ? 1 : 0)),
                    valueList) + ",";
        }


        // Nationality
        str = person.getNationality();
        if (str != null) {
            try {
                attrElement = icssql.getElement("UPDATE-ATTRIBUTE-TYPES").getChild(IcsSqlXML.ATTR_TAG + String.valueOf(UPDATEFLD_NATIONALITY).trim());
            } catch (NullPointerException npe) {
                throw new DatabaseException("Cannot retrieve ATTRIBUTE <" + String.valueOf(UPDATEFLD_NATIONALITY) + "> in UPDATE-ATTRIBUTE-TYPES from SQL XML");
            }

            sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("COLNAME"),
                    attrElement.getChildText("COLTYPE"),
                    str, valueList) + ",";
        }

        // Primary Language
        str = person.getPrimaryLanguage();
        if (str != null) {
            try {
                attrElement = icssql.getElement("UPDATE-ATTRIBUTE-TYPES").getChild(IcsSqlXML.ATTR_TAG + String.valueOf(UPDATEFLD_PRIMARYLANGUAGE).trim());
            } catch (NullPointerException npe) {
                throw new DatabaseException("Cannot retrieve ATTRIBUTE <" + String.valueOf(UPDATEFLD_PRIMARYLANGUAGE) + "> in UPDATE-ATTRIBUTE-TYPES from SQL XML");
            }

            sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("COLNAME"),
                    attrElement.getChildText("COLTYPE"),
                    str, valueList) + ",";

        }


        // Birth Place
        str = person.getBirthPlace();
        if (str != null) {
            try {
                attrElement = icssql.getElement("UPDATE-ATTRIBUTE-TYPES").getChild(IcsSqlXML.ATTR_TAG + String.valueOf(UPDATEFLD_BIRTHPLACE).trim());
            } catch (NullPointerException npe) {
                throw new DatabaseException("Cannot retrieve ATTRIBUTE <" + String.valueOf(UPDATEFLD_BIRTHPLACE) + "> in UPDATE-ATTRIBUTE-TYPES from SQL XML");
            }

            sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("COLNAME"),
                    attrElement.getChildText("COLTYPE"),
                    str, valueList) + ",";
        }


        // MaidenName (set of attributes)
        PersonName maidenName = person.getMaidenName();
        if (maidenName != null) {
            try {
                attrElement = icssql.getElement("UPDATE-ATTRIBUTE-TYPES").getChild(IcsSqlXML.ATTR_TAG + String.valueOf(UPDATEFLD_MAIDENNAME).trim());
            } catch (NullPointerException npe) {
                throw new DatabaseException("Cannot retrieve ATTRIBUTE <" + String.valueOf(UPDATEFLD_MAIDENNAME) + "> in UPDATE-ATTRIBUTE-TYPES from SQL XML");
            }

            // Grab all non-null fields to place in SET clause
            if (maidenName.getFirstName() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("FNAME"),
                        attrElement.getChildText("FNAMETYPE"),
                        maidenName.getFirstName(), valueList) + ",";
            }

            if (maidenName.getLastName() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("LNAME"),
                        attrElement.getChildText("LNAMETYPE"),
                        maidenName.getLastName(), valueList) + ",";
            }

            if (maidenName.getSecondName() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("SECNAME"),
                        attrElement.getChildText("SECNAMETYPE"),
                        maidenName.getSecondName(), valueList) + ",";
            }

            if (maidenName.getSuffix() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("SUFFIX"),
                        attrElement.getChildText("SUFFIXTYPE"),
                        maidenName.getSuffix(), valueList) + ",";
            }

            if (maidenName.getPrefix() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("PREFIX"),
                        attrElement.getChildText("PREFIXTYPE"),
                        maidenName.getPrefix(), valueList) + ",";
            }

            if (maidenName.getDegree() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("DEGREE"),
                        attrElement.getChildText("DEGREETYPE"),
                        maidenName.getDegree(), valueList) + ",";
            }

            if (maidenName.getNameTypeCode() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("NAMETYPE"),
                        attrElement.getChildText("NAMETYPETYPE"),
                        maidenName.getNameTypeCode(), valueList) + ",";
            }

            if (maidenName.getNameRepresentationCode() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("NAMETYPEREP"),
                        attrElement.getChildText("NAMETYPEREPTYPE"),
                        maidenName.getNameRepresentationCode(), valueList) + ",";
            }

            if (maidenName.getSearchKey() != null) {
                sqlUpdateSet += Sql.buildWhereClause(attrElement.getChildText("SEARCHKEY"),
                        attrElement.getChildText("SEARCHKEYTYPE"),
                        maidenName.getSearchKey(), valueList) + ",";
            }
        } // maidenName not null


        // Now UPDATE the PERSON table
        if (sqlUpdateSet.endsWith(",")) // set + at least one field name (all fieldnames place comma's at end
        {
            // setup the sql, minus the last "," in the SET clause
            sqlUpdate = sqlUpdateTable + " set " + sqlUpdateSet.substring(1, sqlUpdateSet.length() - 1) + " " + sqlUpdateWhere;

            log.debug("updatePerson(): " + sqlUpdate);
            PreparedStatement pstmt = personBuildStmt(conn, sqlUpdate, personId,
                    valueList);

            try {
                Profile.begin("PreparedStatement.executeUpdate");
                pstmt.executeUpdate();
                Profile.end("PreparedStatement.executeUpdate");
            } catch (SQLException se) {
                throw new DatabaseException("Cannot UPDATE Person Table: " + se.toString());
            } finally {
                try {
                    if (pstmt != null) pstmt.close();
                } catch (SQLException se2) {
                }
            }
        } // if endswith()
        Profile.end("PersonSql.updatePerson2");
    }


    /**
     * This method will delete a record from the PERSON table, based on the primary key of Person_Id.
     *
     * @param icssql the helper class for reading in the IcsSQL XML File
     * @param conn <code>Connection</code> used for transaction
     * @param personId Primary Key of Person Table uniquely identifying a Person Object
     * @throws DatabaseException
     * @return void
     */
    public void deletePerson(IcsSqlXML icssql, Connection conn, int personId)
            throws DatabaseException {
        // Grab all attributes for deleting a person from IcsSQL xml file
        Element sqlElement = null;
        String sqlDelete = null; // complete SQL statement

        try {
            sqlElement = icssql.getElement("DELETE-PERSON");
            sqlDelete = sqlElement.getChildText("SQL-DELETE");
        } catch (NullPointerException npe) {
            throw new DatabaseException("Cannot retrieve DELETE-PERSON or DELETE-ATTRIBUTE-TYPES from SQL XML");
        }

        log.debug("deletePerson(): " + sqlDelete);
        PreparedStatement pstmt = personBuildStmt(conn, sqlDelete, personId,
                new ArrayList());

        try {
            Profile.begin("PreparedStatement.executeUpdate");
            pstmt.executeUpdate();
            Profile.end("PreparedStatement.executeUpdate");
        } catch (SQLException se) {
            throw new DatabaseException("Cannot DELETE Person (id=" + String.valueOf(personId) + ") From Table: " + se.toString());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException se2) {
            }
        }

    }

}
