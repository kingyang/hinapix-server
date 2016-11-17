/*
 * Title:       DatabaseServicesJdbc
 * Description: The Relational Database implementation of the DatabaseServices
 *              interface. This is ANSI-SQL / JDBC compliant and contains no SQL
 *              embeded directly.
 * Copyright:   (c) 2001-2003
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 * Author:      J. Mangione
 */
package org.openempi.ics.db.jdbc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.openempi.data.Address;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DocumentHeader;
import org.openempi.data.DomainIdentifier;
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
import org.openempi.ics.db.DatabaseServices;
import org.openempi.ics.db.DatabaseServicesListener;
import org.openempi.ics.db.QueryParamList;
import org.openempi.ics.utility.IcsSqlXML;
import org.openempi.ics.utility.JdbcHelper;
import org.openempi.ics.utility.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The Relational Database implementation of the <code>DatabaseServices</code>
 * interface. Database clients should not deal with an instance of this class,
 * but should work with the DatabaseServices interface. Access to the instance
 * of <code>DatabaseServicesJdbc</code> should be controlled through
 * <code>DatabaseServicesFactory</code>.
 * <br>
 * This class uses defined in <code>IcsSqlSML</code>. It currently supports
 * JDBC 2.0.
 *
 * @author CareScience
 * @version 1.14, 20030214
 */
public class DatabaseServicesJdbc implements DatabaseServices
{
    
    /**
     * Order that columns must be returned from the SQL-SELECT statement for Getting A Person Object,
     * as defined in the IcsSql XML file.
     */
    public static final int SEL_ORD_PERSON_ID       = 1;
    public static final int SEL_ORD_MSG_DATE        = 2;
    public static final int SEL_ORD_MSG_CTRL_ID     = 3;
    public static final int SEL_ORD_SEQ_NUM         = 4;
    public static final int SEL_ORD_CONT_PTR        = 5;
    public static final int SEL_ORD_ACK_TYPE        = 6;
    public static final int SEL_ORD_APP_ACK_TYPE    = 7;
    public static final int SEL_ORD_MSG_SECURITY    = 8;
    public static final int SEL_ORD_REC_APP         = 9;
    public static final int SEL_ORD_REC_FAC         = 10;
    public static final int SEL_ORD_SEND_APP        = 11;
    public static final int SEL_ORD_SEND_FAC        = 12;
    public static final int SEL_ORD_EVENT_CD        = 13;
    public static final int SEL_ORD_MSG_TYPE        = 14;
    public static final int SEL_ORD_TRIGGER_EVENT   = 15;
    public static final int SEL_ORD_MSG_STRUCTURE   = 16;
    public static final int SEL_ORD_NATIONALITY_CD  = 17;
    public static final int SEL_ORD_NAMESEARCHKEY   = 18;
    public static final int SEL_ORD_PRIMLANG        = 19;
    public static final int SEL_ORD_SSN             = 20;
    public static final int SEL_ORD_BIRTHDATE       = 21;
    public static final int SEL_ORD_EXPIRED         = 22;
    public static final int SEL_ORD_ISPROVIDER      = 23;
    public static final int SEL_ORD_LNAME           = 24;
    public static final int SEL_ORD_FNAME           = 25;
    public static final int SEL_ORD_SECNAME         = 26;
    public static final int SEL_ORD_SUFFIX          = 27;
    public static final int SEL_ORD_PREFIX          = 28;
    public static final int SEL_ORD_DEGREE          = 29;
    public static final int SEL_ORD_NAMETYPE_CD     = 30;
    public static final int SEL_ORD_NAMETYPEREP_CD  = 31;
    public static final int SEL_ORD_PN_SEARCHKEY    = 32;
    public static final int SEL_ORD_PN_STARTDATE    = 33;
    public static final int SEL_ORD_PN_ENDDATE      = 34;
    public static final int SEL_ORD_PN_ALIAS        = 35;
    public static final int SEL_ORD_ADDRESS_1       = 36;
    public static final int SEL_ORD_ADDRESS_2       = 37;
    public static final int SEL_ORD_CITY            = 38;
    public static final int SEL_ORD_STATEPROV       = 39;
    public static final int SEL_ORD_ZIP             = 40;
    public static final int SEL_ORD_COUNTRY         = 41;
    public static final int SEL_ORD_PARISH_CD       = 42;
    public static final int SEL_ORD_PA_STARTDATE    = 43;
    public static final int SEL_ORD_PA_ENDDATE      = 44;
    public static final int SEL_ORD_ID_TYPE_CD      = 45;
    public static final int SEL_ORD_PI_EFF_DATE     = 46;
    public static final int SEL_ORD_PI_EXP_DATE     = 47;
    public static final int SEL_ORD_CONSENT         = 48;
    public static final int SEL_ORD_AA_UNIV_ID         = 49;
    public static final int SEL_ORD_AA_UNIV_ID_TYPE_CD = 50;
    public static final int SEL_ORD_AA_NAMESPACE_ID    = 51;
    public static final int SEL_ORD_AF_UNIV_ID         = 52;
    public static final int SEL_ORD_AF_UNIV_ID_TYPE_CD = 53;
    public static final int SEL_ORD_AF_NAMESPACE_ID    = 54;
    public static final int SEL_ORD_PH_USE_CD       = 55;
    public static final int SEL_ORD_PH_COUNTRY_CD   = 56;
    public static final int SEL_ORD_PH_AREA_CD      = 57;
    public static final int SEL_ORD_PH_NUM          = 58;
    public static final int SEL_ORD_PH_EXT          = 59;
    public static final int SEL_ORD_EMAIL           = 60;
    public static final int SEL_ORD_GENDER          = 61;
    public static final int SEL_ORD_MARITAL_STATUS  = 62;
    public static final int SEL_ORD_DRV_LIC_NUM     = 63;
    public static final int SEL_ORD_DRV_ISSUE_STATE = 64;
    public static final int SEL_ORD_DRV_ISSUE_DATE  = 65;
    public static final int SEL_ORD_ETHNIC_CD       = 66;
    public static final int SEL_ORD_RELIGION_CD     = 67;
    public static final int SEL_ORD_RACE_CD         = 68;
    public static final int SEL_ORD_PI_IDENTIFIER   = 69;
    public static final int SEL_ORD_MAIDEN_LNAME  = 70;
    public static final int SEL_ORD_MAIDEN_FNAME        = 71;
    public static final int SEL_ORD_MAIDEN_SECNAME      = 72;
    public static final int SEL_ORD_MAIDEN_SUFFIX       = 73;
    public static final int SEL_ORD_MAIDEN_PREFIX       = 74;
    public static final int SEL_ORD_MAIDEN_DEGREE       = 75;
    public static final int SEL_ORD_MAIDEN_NAME_TYPE_CD = 76;
    public static final int SEL_ORD_MAIDEN_NAME_TYPE_REP_CD = 77;
    public static final int SEL_ORD_MAIDEN_SEARCH_KEY       = 78;
    public static final int SEL_ORD_BIRTH_PLACE            = 79;
    public static final int SEL_ORD_DH_ID                  = 80;
    public static final int SEL_ORD_PERSON_INFO_ID         = 81;
    public static final int SEL_ORD_CORPORATE_ID           = 82;
    public static final int SEL_ORD_UPDATED_CORPORATE_ID   = 83;

    private static transient List listeners = new ArrayList();
    
    private IcsSqlXML icssql = IcsSqlXML.getInstance(); // contains all SQL needed from xml file
    private PersonSql personsql = PersonSql.getInstance(); // singelton reference
    
    private DocumentHeaderSql documentheadersql = DocumentHeaderSql.getInstance(); // singleton
    private PersonInfoSql personinfosql = PersonInfoSql.getInstance(); // singleton
    
    // singleton
    private static final DatabaseServicesJdbc instance = new DatabaseServicesJdbc();
    
    private Logger log = Logger.getLogger("ICS");

    // only ran by calling getInstance()
    private DatabaseServicesJdbc()
    {}
    
    /**
     * @return an instance of this class
     */
    public static DatabaseServicesJdbc getInstance()
    {
        return instance;
    }
    
    
    /**
     * Signal registered DatabaseServicesListeners that a <code>Person</code> has
     * been added to the database.
     * <p>
     * @param person <code>Person</code> added to the database
     */
    protected void personAdded(Person person)
    {
        Profile.begin("DatabaseServicesJdbc.personAdded");
        Iterator itr = listeners.iterator();
        
        while (itr.hasNext())
        {
            ((DatabaseServicesListener)itr.next()).personAdded(person);
        }
        Profile.end("DatabaseServicesJdbc.personAdded");
    }
    
    
    /**
     * Signals registered DatabaseServicesListeners that a <code>Person</code> has
     * been updated in the database.
     * <p>
     * @param person <code>Person</code> updated in the database
     */
    protected void personUpdated(Person person)
    {
        Profile.begin("DatabaseServicesJdbc.personUpdated");
        Iterator itr = listeners.iterator();
        
        while (itr.hasNext())
        {
            ((DatabaseServicesListener)itr.next()).personUpdated(person);
        }
        Profile.end("DatabaseServicesJdbc.personUpdated");
    }
    
    
    /**
     * Signals registered <code>DatabaseServicesListeners</code> that two
     * <code>Persons</code> have been merged in the database.
     * <p>
     * @param basePerson the base <code>Person</code>
     * @param mergePerson the <code>Person</code> that was merged with the
     * base <code>Person</code>
     */
    protected void personsMerged(Person basePerson, Person mergePerson)
    {
        Profile.begin("DatabaseServicesJdbc.personsMerged");
        Iterator itr = listeners.iterator();
        
        while (itr.hasNext())
        {
            ((DatabaseServicesListener)itr.next()).personsMerged(basePerson, mergePerson);
        }
        Profile.end("DatabaseServicesJdbc.personsMerged");
    }
    
    
    /**
     * Signals registered <code>DatabaseServicesListeners</code> that a
     * <code>Person</code> has been split in the database.
     * <p>
     * @param originalPerson the <code>Person</code> split in the database.
     * @param newPerson the <code>Person</code> split in the database.
     */
    protected void personSplit(Person originalPerson, Person newPerson)
    {
        Profile.begin("DatabaseServicesJdbc.personSplit");
        Iterator itr = listeners.iterator();
        
        while (itr.hasNext())
        {
            ((DatabaseServicesListener)itr.next()).personSplit(originalPerson, newPerson);
        }
        Profile.end("DatabaseServicesJdbc.personSplit");
    }
    

    /**
     * Add a DocumentHeader object to a Person object.
     * Will return the Document Header created for use in creating the other person attribute objects.
     */
    private DocumentHeader addDocumentHeader(Person p,
            String dhId,
            java.util.Date msgDate,
            String msgCtrlId,
            String seqNum,
            String contPtr,
            String ackType,
            String appAckType,
            String msgSecurity,
            String recApp,
            String recFac,
            String sendApp,
            String sendFac,
            String eventCd,
            String msgType,
            String triggerEvent,
            String msgStructure
            )
    {
        DocumentHeader hd = new DocumentHeader();
        hd.setOid(dhId);
        hd.setMessageDate(msgDate);
        hd.setSequenceNumber(seqNum);
        hd.setContinuationPointer(contPtr);
        hd.setAcceptAckType(ackType);
        hd.setApplicationAckType(appAckType);
        hd.setMessageSecurity(msgSecurity);
        hd.setReceivingApplication(recApp);
        hd.setReceivingFacility(recFac);
        hd.setSendingApplication(sendApp);
        hd.setSendingFacility(sendFac);
        hd.setEventCode(eventCd);
        hd.setMessageType(msgType);
        hd.setTriggerEvent(triggerEvent);
        hd.setMessageStructure(msgStructure);
        
        // no check for nulls required before adding. There will always be a document header.
        p.addDocumentHeader(hd);
        
        return hd;
    }
    
    
    /**
     * Add a Person Name Object to the Person Object.
     */
    private void addPersonName(Person p, DocumentHeader dh,
            String lname,
            String fname,
            String secname,
            String suffix,
            String prefix,
            String degree,
            String nameTypeCd,
            String nameTypeRepCd,
            String searchKey,
            java.util.Date startDate,
            java.util.Date endDate,
            boolean alias,
            String personInfoId
            )
    {
        // only if at least one field is available
        if ( (lname != null) || (fname != null) || (secname != null) || (suffix != null) ||
                (prefix != null) || (degree != null) || (nameTypeCd != null) ||
                (nameTypeRepCd != null) || (searchKey != null) || (startDate != null) ||
                (endDate != null) )
        {
            PersonName pn = new PersonName(dh, lname, fname, secname);
            pn.setOid(personInfoId);
            pn.setSuffix(suffix);
            pn.setPrefix(prefix);
            pn.setDegree(degree);
            pn.setNameTypeCode(nameTypeCd);
            pn.setNameRepresentationCode(nameTypeRepCd);
            //      pn.setSearchKey(searchKey);
            pn.setStartDate(startDate);
            pn.setEndDate(endDate);
            pn.setAlias(alias);
            
            p.addName(pn);
        }
    }
    
    /**
     * Adds a Person Address object to the Person object
     */
    private void addPersonAddress(Person p, DocumentHeader dh,
            String addr1,
            String addr2,
            String cityProv,
            String state,
            String zip,
            String country,
            String parishCd,
            java.util.Date startDate,
            java.util.Date endDate,
            String personInfoId
            )
    {
        // only add if at least one field is available
        if ( (addr1 != null) || (addr2 != null) || (cityProv != null) || (state != null) ||
                (zip != null) || (country != null) || (parishCd != null) || (startDate != null) ||
                (endDate != null) )
        {
            Address pa  = new Address(dh, addr1, addr2, cityProv, state, zip);
            pa.setOid(personInfoId);
            pa.setCountry(country);
            pa.setParishCode(parishCd);
            pa.setStartDate(startDate);
            pa.setEndDate(endDate);
            
            p.addAddress(pa);
        }
    }
    
    
    /**
     * Adds a Person Identifier Object to a Person Object.
     * Will also need to build Domain Identifiers and add to PI objects.
     */
    private void addPersonIdentifier(Person p, DocumentHeader dh,
            String id,
            String idTypeCd,
            java.util.Date effDate,
            java.util.Date expDate,
            boolean consent,
            String assignauth_univId,
            String assignauth_univIdTypeCd,
            String assignauth_nameSpaceId,
            String assignfac_univId,
            String assignfac_univIdTypeCd,
            String assignfac_nameSpaceId,
            String personInfoId,
            String corporateId,
            String updated_corporateId
            )
    {
        // if ANY domain id information isn't available, don't add this.
      /*
    if (  (assignauth_nameSpaceId != null ) &&
          (assignauth_univId != null) &&
          (assignauth_univIdTypeCd != null) &&
          (assignfac_nameSpaceId != null) &&
          (assignfac_univId != null) &&
          (assignfac_univIdTypeCd != null) )
       */
        if (  (assignauth_nameSpaceId != null ) &&
                (assignfac_nameSpaceId != null) &&
                (personInfoId != null) )
        {
            // need to create DomainIdentifiers for Assign Authority and Facility
            DomainIdentifier diAuth = new DomainIdentifier(dh, assignauth_nameSpaceId, assignauth_univId, assignauth_univIdTypeCd);
            DomainIdentifier diFac = new DomainIdentifier(dh, assignfac_nameSpaceId, assignfac_univId, assignfac_univIdTypeCd);
            
            PersonIdentifier pi = new PersonIdentifier(dh, id, diAuth, diFac, idTypeCd);
            pi.setUpdatedCorpId(updated_corporateId);
            pi.setCorpId(corporateId);
            pi.setOid(personInfoId);
            pi.setEffectiveDate(effDate);
            pi.setExpirationDate(expDate);
            pi.setPatientConsent(consent);
            
            p.addPersonIdentifier(pi);
        }
    }
    
    
    /**
     * Add a Telephone Number object to the Person object
     */
    private void addPersonPhone(Person p, DocumentHeader dh,
            String useCd,
            String countryCd,
            String areaCd,
            String num,
            String ext,
            String personInfoId
            )
    {
        // if any phone number info exists, add the record
        if ( (useCd != null) || (countryCd != null) || (areaCd != null) ||
                (num != null) || (ext != null) )
        {
            TelephoneNumber pp = new TelephoneNumber(dh, areaCd, num);
            pp.setOid(personInfoId);
            pp.setTelecomUseCode(useCd);
            pp.setCountryCode(countryCd);
            pp.setExtension(ext);
            
            p.addTelephoneNumber(pp);
        }
    }
    
    
    /**
     * Add an SSN object to the Person object
     */
    private void addPersonSSN(Person p, DocumentHeader dh, String ssn)
    {
        if (ssn != null)
        {
            SocialSecurityNumber pp = new SocialSecurityNumber(dh, ssn);
            p.addSocialSecurityNumber(pp);
        }
    }
    
    /**
     * Add DOB object to the Person object
     */
    private void addPersonDOB(Person p, DocumentHeader dh, Timestamp dob)
    {
        if (dob != null)
        {
            DateOfBirth pp = new DateOfBirth(dh, new java.util.Date(dob.getTime()));
            p.addDateOfBirth(pp);
        }
    }
    
    
    /**
     * Add an EmailAddress object to the Person object
     */
    private void addPersonEmail(Person p, DocumentHeader dh,
            String email,
            String personInfoId
            )
    {
        if ( email != null )
        {
            EmailAddress pe = new EmailAddress(dh, email);
            pe.setOid(personInfoId);
            
            p.addEmailAddress(pe);
        }
    }
    
    
    /**
     * Add a Gender object to the Person object
     */
    private void addPersonGender(Person p, DocumentHeader dh,
            String gender,
            String personInfoId
            )
    {
        if ( gender != null )
        {
            Gender pg = new Gender(dh, gender);
            pg.setOid(personInfoId);
            
            p.addGender(pg);
        }
    }
    
    
    /**
     * Add a Marital Status Object to the Person Object
     */
    private void addPersonMaritalStatus(Person p, DocumentHeader dh,
            String stat,
            String personInfoId
            )
    {
        if ( stat != null )
        {
            MaritalStatus pms = new MaritalStatus(dh, stat);
            pms.setOid(personInfoId);
            
            p.addMaritalStatus(pms);
        }
    }
    
    
    /**
     * Add a Drivers License Object to the Person object
     */
    private void addDriversLicense(Person p, DocumentHeader dh,
            String drvLicNumber,
            String issueState,
            java.util.Date issueDate,
            String personInfoId
            )
    {
        // if State and Date exists
        //        if (  (issueState != null) &&
        //        (issueDate > 0) )
        {
            DriversLicense pdl = new DriversLicense(dh, drvLicNumber, issueState, issueDate);
            pdl.setOid(personInfoId);
            
            p.addDriversLicense(pdl);
        }
    }
    
    
    /**
     * Add an Ethnic Group object to the Person object
     */
    private void addEthnicGroup(Person p, DocumentHeader dh,
            String grpCd,
            String personInfoId
            )
    {
        if ( grpCd != null )
        {
            EthnicGroup peg = new EthnicGroup(dh, grpCd);
            peg.setOid(personInfoId);
            
            p.addEthnicGroup(peg);
        }
    }
    
    
    /**
     * Add a Religion object to the Person Object
     */
    private void addReligion(Person p, DocumentHeader dh,
            String relCd,
            String personInfoId
            )
    {
        if ( relCd != null )
        {
            Religion pr = new Religion(dh, relCd);
            pr.setOid(personInfoId);
            
            p.addReligion(pr);
        }
    }
    
    
    /**
     * Add a Race object to the Person object
     */
    private void addRace(Person p, DocumentHeader dh,
            String raceCd,
            String personInfoId
            )
    {
        if ( raceCd != null )
        {
            Race pr = new Race(dh, raceCd);
            pr.setOid(personInfoId);
            
            p.addRace(pr);
        }
        
    }
    
    /**
     * Returns a list of <code>Person</code> objects matching the specified attribute.
     * <b>
     * SQL this uses is defined in IcsSql XML file, and expects the PERSON_ID to be first in the SELECT -
     * as this is how it's ordered.</b>
     *
     * From IcsSql.xml: <QUERY-GETPERSONS>
     *
     * @param attributeType The <code>Integer</code> attribute type to query against,
     * which should be a type in the <code>AttributeType</code> interface.
     * @param value The <code>Object</code> containing the value of the attribute
     * to search for. This must be a String or a SearchRange.
     * @return List containing the results of the query.
     * @throws DatabaseException
     */
    public List query(Integer attributeType, Object value)
    throws DatabaseException
    {
        return query(attributeType, value, true);
    }
    
    /**
     * Returns a list of <code>Person</code> objects matching the specified attribute.
     * <b>
     * SQL this uses is defined in IcsSql XML file, and expects the PERSON_ID to be first in the SELECT -
     * as this is how it's ordered.</b>
     *
     * From IcsSql.xml: <QUERY-GETPERSONS>
     *
     * @param attributeType The <code>Integer</code> attribute type to query against,
     * which should be a type in the <code>AttributeType</code> interface.
     * @param value The <code>Object</code> containing the value of the attribute
     * to search for. This must be a String or a SearchRange.
     * @return List containing the results of the query.
     * @throws DatabaseException
     */
    public List query(Integer attributeType, Object value,
            boolean usePreparedStatement)
            throws DatabaseException
    {
        QueryParamList params = new QueryParamList(QueryParamList.OR_LIST);
        params.add(attributeType, value);
        return query(params, usePreparedStatement);
    }
    
    
    /**
     * Implementation of the DatabaseServices interface.  Uses the
     * queryIterator() method to construct the returned list.
     */
    public List query(QueryParamList params)
    throws DatabaseException
    {
        return query(params, true);
    }
    
    /**
     * Implementation of the DatabaseServices interface.  Uses the
     * queryIterator() method to construct the returned list.
     */
    public List query(QueryParamList params, boolean usePreparedStatement)
    throws DatabaseException
    {
        Profile.begin("DatabaseServicesJdbc.query");
        
        List query = new ArrayList();
        Iterator iter = null;
        
        try
        {
            iter = queryIterator(params, usePreparedStatement);
            while(iter.hasNext())
            {
                query.add(iter.next());
            }
        }
        catch (DatabaseException e)
        {
            throw e;
        }
        finally
        {
            if (iter != null)
                releaseIterator(iter);
        }
        Profile.end("DatabaseServicesJdbc.query");
        
        return query;
    }
    
    
    
    /**
     * Adds a <code>Person</code> to the database.
     * <b>
     * This will first insert a new Person into the Person table. Then use that new Person ID
     * to insert one or more Document Headers and establish the relationship between them. Finally,
     * for each Document Header, there can be one or more Attributes such as person address, email, etc..
     * The Document Header ID will be used for each of these attributes.</b>
     *
     * From IcsSql.xml: <INSERT-PERSON>, <INSERT-DOCUMENT-HEADER>, <INSERT-PERSON-INFO>
     *
     * @param person The <code>Person</code> to be added to the database.
     * @returns int New person ID created
     * @throws DatabaseException
     */
    public int addPerson(Person person) throws DatabaseException
    {
        // This is a DIRECT copy of the addPerson method only returning the Person ID.
        // This must DIE (hence: deprecated) after the Objectivity migration is complete.
        
        Connection conn = null;
        
        try
        {
            // use these ID's pasted back from INSERTS for relationships
            int personId = 0;
            int dhId = 0;  // document header id
            
            // start this transaction block
            conn = JdbcHelper.getConnection();

//            conn.setAutoCommit(false);
            
            // First, add the actual Person record. This is needed to obtain the new Person_Id
            // that'll be used to insert new Document Headers.
            personId = personsql.insertPerson(icssql, conn, person.getNationality(),
                    person.getNameSearchKey(),
                    person.getPrimaryLanguage(),
                    person.isExpired(),
                    person.isProvider(),
                    person.getBirthPlace(),
                    person.getMaidenName()
                    );
            
            // Next, insert into DOCHDR table for each Document Header object inside the person object.
            // For each DOCUMENT HEADER, we must associate ALL attributes of the person object.
            // There should really only be ONE DOCUMENT HEADER for a transaction, but this will simply
            // copy each attribute to each Doc Header in the event there's more than one.
            Iterator iteratorDocHdr = person.getDocumentHeaders().iterator();
            
            while ( iteratorDocHdr.hasNext() )
            {
                DocumentHeader dh = (DocumentHeader) iteratorDocHdr.next();
                dhId = documentheadersql.insertDocumentHeader(icssql, conn, personId,
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
                
                
                // This will insert ONE OR MORE person info records depending on how many instances of the attributes
                // there are.
                // Will perform this for each document header.
                personinfosql.insertPersonInfo(icssql, conn, dhId, person );
                
            } // while more doc headers
            
            Profile.begin("Connection.commit");
//            conn.commit();
            Profile.end("Connection.commit");
            conn.close();
            
            // signal registered listeners that a Person has been added.
            personAdded(person);
            
            return personId;
        }
        catch (Exception e) // doesn't matter what it is
        {
            try
            {
                // always rollback if we got far enough to have inserted something
                if ( conn != null ) conn.rollback();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( conn != null ) conn.close();
            }
            catch ( SQLException se2 )
            { }
            throw new DatabaseException("Cannot ADD a Person Object: " + e.toString());
        }
    }
    
    public int addPersonInfo(Person person) throws DatabaseException
    {        
        Connection conn = null;
        
        try
        {
            // start this transaction block
            conn = JdbcHelper.getConnection();
                
            // This will insert ONE OR MORE person info records depending on how many instances of the attributes
            // there are.
            // Will perform this for each document header.
            DocumentHeader personDocHeader = (DocumentHeader) person.getDocumentHeaders().get(0);
            personinfosql.insertPersonInfo(icssql, conn, personDocHeader.getOidInt(), person );
                
            conn.close();
            
            // signal registered listeners that a Person has been added.
            personAdded(person);
            
            return personDocHeader.getOidInt();
        }
        catch (Exception e) // doesn't matter what it is
        {
            try
            {
                // always rollback if we got far enough to have inserted something
                if ( conn != null ) conn.rollback();
            }
            catch ( SQLException se2 )
            { }
            try
            {
                if ( conn != null ) conn.close();
            }
            catch ( SQLException se2 )
            { }
            throw new DatabaseException("Cannot ADD a Person Object: " + e.toString());
        }
    }
    
    
    //    /**
    //     * Adds a <code>Person</code> to the database.
    //     * <b>
    //     * This will first insert a new Person into the Person table. Then use that new Person ID
    //     * to insert one or more Document Headers and establish the relationship between them. Finally,
    //     * for each Document Header, there can be one or more Attributes such as person address, email, etc..
    //     * The Document Header ID will be used for each of these attributes.</b>
    //     *
    //     * From IcsSql.xml: <INSERT-PERSON>, <INSERT-DOCUMENT-HEADER>, <INSERT-PERSON-INFO>
    //     *
    //     * @param person The <code>Person</code> to be added to the database.
    //     * @throws DatabaseException
    //     */
    //    public boolean addPerson(Person person) throws DatabaseException
    //    {
    //        Profile.begin("DatabaseServicesJdbc.addPerson");
    //        boolean personAdded = false;
    //        Connection conn = null;
    //
    //        log.debug("Adding A Person");
    //
    //        try
    //        {
    //            // use these ID's pasted back from INSERTS for relationships
    //            int personId = 0;
    //            int dhId = 0;  // document header id
    //
    //            // start this transaction block
    //            conn = JdbcHelper.getConnection();
    //            Profile.begin("Connection.setAutoCommit");
    //            conn.setAutoCommit(false);
    //            Profile.end("Connection.setAutoCommit");
    //
    //            // First, add the actual Person record. This is needed to obtain the new Person_Id
    //            // that'll be used to insert new Document Headers.
    //            personId = personsql.insertPerson(icssql, conn, person.getNationality(),
    //            person.getNameSearchKey(),
    //            person.getPrimaryLanguage(),
    //            person.isExpired(),
    //            person.isProvider(),
    //            person.getBirthPlace(),
    //            person.getMaidenName()
    //            );
    //            person.setOid(personId);
    //
    //            // Next, insert into DOCHDR table for each Document Header object inside the person object.
    //            // For each DOCUMENT HEADER, we must associate ALL attributes of the person object.
    //            // There should really only be ONE DOCUMENT HEADER for a transaction, but this will simply
    //            // copy each attribute to each Doc Header in the event there's more than one.
    //            Iterator iteratorDocHdr = person.getDocumentHeaders().iterator();
    //
    //            while ( iteratorDocHdr.hasNext() )
    //            {
    //                DocumentHeader dh = (DocumentHeader) iteratorDocHdr.next();
    //                dhId = documentheadersql.insertDocumentHeader(icssql, conn, personId,
    //                dh.getMessageDate(),
    //                dh.getMessageControlID(),
    //                dh.getSequenceNumber(),
    //                dh.getContinuationPointer(),
    //                dh.getAcceptAckType(),
    //                dh.getApplicationAckType(),
    //                dh.getMessageStructure(),
    //                dh.getReceivingApplication(),
    //                dh.getReceivingFacility(),
    //                dh.getSendingApplication(),
    //                dh.getSendingFacility(),
    //                dh.getEventCode(),
    //                dh.getMessageType(),
    //                dh.getTriggerEvent(),
    //                dh.getMessageStructure()
    //                );
    //
    //
    //                // This will insert ONE OR MORE person info records depending on how many instances of the attributes
    //                // there are.
    //                // Will perform this for each document header.
    //                personinfosql.insertPersonInfo(icssql, conn, dhId, person );
    //
    //            } // while more doc headers
    //
    //            Profile.begin("Connection.commit");
    //            conn.commit();
    //            Profile.end("Connection.commit");
    //            Profile.begin("Connection.close");
    //            conn.close();
    //            Profile.end("Connection.close");
    //
    //            personAdded = true;
    //
    //            // signal registered listeners that a Person has been added.
    //            personAdded(person);
    //
    //        }
    //        catch (Exception e) // doesn't matter what the exception is
    //        {
    //            try
    //            {
    //                if ( conn != null )conn.rollback();
    //            } // always rollback if we got far enough to have inserted something
    //            catch ( SQLException se2 )
    //            { }
    //            try
    //            {
    //                if ( conn != null ) conn.close();
    //            } catch ( SQLException se2 )
    //            { }
    //            throw new DatabaseException("Cannot ADD a Person Object: " + e.toString());
    //        }
    //        Profile.end("DatabaseServicesJdbc.addPerson");
    //        return personAdded;
    //    }
    
    /**
     * The semantics of this call work as follows. The person at index 0 is to be preserved and the rest of the persons at indices
     * 1-(n-1) should be merged into the person at index 0. Each of the person's at indices 1-(n-1) are expected to only have a single
     * identifier that is being merged. The method uses the identifier to load the persons associated with that identifier. If there are any
     * other identifiers matched to the identifier that is getting eliminated, the matched identifiers are linked to the surviving person
     * at index 0 (the dh_id for those person_info records is modified to match that of the surviving person).
     * 
     * The persons at index 1-(n-1) are then deleted.
     * 
     */
    @SuppressWarnings("unchecked")
	public Person mergePersons(Person[] persons)
    throws DatabaseException
    {
        
        Profile.begin("DatabaseServicesJdbc.mergePersons");
        Person mergedPerson = null;
        Connection conn = null;
        
        if ((persons != null) && (persons.length > 1))
        {
        	// Locate the person that should be preserved
        	mergedPerson = this.findPersonById(persons[0]);
        	if (mergedPerson == null) {
        		return null;
        	}
        	
            try
            {
                // start this transaction block
                conn = JdbcHelper.getConnection();
                
                for (int i=1; i < persons.length; i++) {
                	Person removedPerson = this.findPersonById(persons[i]);
                	if (removedPerson == null) {
                		continue;
                	}
                	
                	for (Iterator remPidIter = removedPerson.getPersonIdentifiers().iterator(); remPidIter.hasNext(); ) {
                		PersonIdentifier remainPid = (PersonIdentifier) remPidIter.next();
                		PersonIdentifier removePid = (PersonIdentifier) removedPerson.getPersonIdentifiers().get(0);
                		// If this is the id that is associated with the person we are removing then skip it.
                		if (remainPid.getId().equalsIgnoreCase(removePid.getId())) {
                			continue;
                		}
                		// This is a person that was matched with the pid we are removing; associate him/her with the merged person
                		attachRemainingPersonWithMergedPerson(conn, remainPid, mergedPerson);
                	}
                	
                	removePersonById(conn, removedPerson.getOidInt());
                }
                Profile.begin("Connection.close");
                conn.close();
                Profile.end("Connection.close");
                
                // Signal registered listeners that Persons have been merged.
                for (int i = 1; i < persons.length; i++)
                {
                    personsMerged(mergedPerson, persons[i]);
                }
            }
            catch (Exception e) // doesn't matter what the exception is
            {
                log.error(e, e);
          
                try
                {
                    if (conn != null)
                    {
                        conn.close();
                    }
                }
                catch ( SQLException se2 )
                {
                    log.error(se2, se2);
                }
                log.error(e, e);
                throw new DatabaseException(e);
            }
        }
        Profile.end("DatabaseServicesJdbc.mergePersons");
        
        log.debug("mergePersons(): mergedPerson = " + mergedPerson);
        return mergedPerson;
    }
    
    private void removePersonById(Connection conn, int oidInt) throws SQLException {
        String sql = "DELETE FROM person WHERE person_id = ?";
        log.debug("Deleting person entry with person_id of " + oidInt);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, oidInt);
        pstmt.execute();
        pstmt.close();
	}
    
    private void removePersonAlias(Connection conn, int id) throws SQLException {
    	String sql = "DELETE FROM person_info where person_info_id = ?";
    	log.debug("Deleting person alias with person_info_id of " + id);
    	PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.execute();
        pstmt.close();
    }

	private void attachRemainingPersonWithMergedPerson(Connection conn, PersonIdentifier removePid,
			Person mergedPerson) throws SQLException {
    	int preservedDocHeaderId = ((DocumentHeader) mergedPerson.getDocumentHeaders().get(0)).getOidInt();
    	int preservedPersonInfoId = removePid.getOidInt();
    	String sql = "UPDATE person_info SET dh_id = ? WHERE person_info_id = ?";
    	log.debug("Associating person identifier entry " + preservedPersonInfoId + 
    			" with document header " + preservedDocHeaderId);
    	PreparedStatement pstmt = conn.prepareStatement(sql);
    	pstmt.setInt(1, preservedDocHeaderId);
    	pstmt.setInt(2, preservedPersonInfoId);
    	pstmt.execute();
    	pstmt.close();
	}

	@SuppressWarnings("unchecked")
	private Person findPersonById(Person person) throws DatabaseException {
    	PersonIdentifier pid = (PersonIdentifier) person.getPersonIdentifiers().get(0);
    	if (pid == null) {
    		return null;
    	}
    	
    	QueryParamList param = new QueryParamList(QueryParamList.AND_LIST);
    	param.add(AttributeType.PERSON_IDENTIFIER, pid.getId());
    	if (pid.getAssigningAuthority().getNameSpaceID() != null && 
    		pid.getAssigningAuthority().getNameSpaceID().length() > 0) {
	    	param.add(AttributeType.AA_NAMESPACE_ID, 
	    			pid.getAssigningAuthority().getNameSpaceID());
    	}
    	if (pid.getAssigningFacility().getNameSpaceID() != null &&
    		pid.getAssigningFacility().getNameSpaceID().length() > 0) {
    		param.add(AttributeType.AF_NAMESPACE_ID,
    			pid.getAssigningFacility().getNameSpaceID());
    	}
		List matches = this.query(param);
    	if (matches != null && matches.size() > 0) {
    		return (Person) matches.get(0);
    	}
    	return null;
    }

	public int splitPerson(Person origPerson,
            DocumentHeader[] docHeaders)
            throws DatabaseException
    {
        Connection conn = null;
        PreparedStatement st = null;
        int personId = 0;
        
        try
        {
            log.debug("Splitting person: " + origPerson.getOidInt());
            
            conn = JdbcHelper.getConnection();
            conn.setAutoCommit(false);
            
            Person person = new Person();
            person.setNationality(origPerson.getNationality());
            person.setNameSearchKey(origPerson.getNameSearchKey());
            person.setPrimaryLanguage(origPerson.getPrimaryLanguage());
            person.setExpired(origPerson.isExpired());
            person.setBirthPlace(origPerson.getBirthPlace());
            person.setMaidenName(origPerson.getMaidenName());
            
            personId = personsql.insertPerson(icssql, conn,
                    person.getNationality(),
                    person.getNameSearchKey(),
                    person.getPrimaryLanguage(),
                    person.isExpired(),
                    person.isProvider(),
                    person.getBirthPlace(),
                    person.getMaidenName());
            
            StringBuffer sql = new StringBuffer();
            sql.append("update document_header set person_id = ? where dh_id in (");
            for(int i=0;i<docHeaders.length;i++)
            {
                if (i > 0)
                    sql.append(",");
                sql.append("?");
            }
            sql.append(")");
            log.debug("Split person Sql: " + sql.toString());
            
            st = conn.prepareStatement(sql.toString());
            
            st.setInt(1, personId);
            for(int i=0;i<docHeaders.length;i++)
                st.setInt(i+2, docHeaders[i].getOidInt());
            
            int rows = st.executeUpdate();
            
            if (rows != docHeaders.length)
            {
                throw new DatabaseException("Not all DocumentHeaders update!  Only " + rows + " of " + docHeaders.length + " updated!");
            }
            conn.commit();
            
            personSplit(origPerson, person);
            
            log.debug("Split successful!  New Id: " + personId);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error(e, e);
            throw new DatabaseException(e);
        }
        finally
        {
            try
            {
                if(st != null)
                {
                    st.close();
                }
                if(conn != null)
                {
                    conn.close();
                }
            }
            catch(SQLException e)
            {
                log.error(e, e);
                throw new DatabaseException(e);
            }
        }
        return personId;
    }
    

    /**
     * Removes a <code>Person</code> from the database.
     * NOT IMPLEMENTED IN THIS PACKAGE. SEE removeObject()
     *
     * @param person the <code>Person</code> to be removed from the database
     * @throws DatabaseException
     * @deprecated - No replacement
     */
    public boolean removePerson(Person person) throws DatabaseException
    { return false; }

    
    /**
     * Removes an Object from the database.
     * If Object ID is String, then it refers to the entire person (Person.Person_Id). Will delete a single Person record.
     * If Object ID is an ArrayList, then it refers to one or more ID's which related to PersonInfo.Person_Info_Id.
     * Will delete one or more Document Header records where dhId's are found for the Person_Info id's.
     * <p>
     * CASCADE DELETE on all Person relationships MUST be implemented in the database for this method to accurately delete all child records.
     * <p>
     * From IcsSql.xml: <DELETE-PERSON>, <DELETE-DOCUMENT-HEADER-FROM-PERSON-INFO>
     *
     * @param obj - The Object ID to be removed from the database. (either single or List)
     * @throws DatabaseException
     */
    public boolean removeObject(Object obj) throws DatabaseException
    {
        boolean objectsRemoved = false;
        Connection conn = null;
        
        try
        {
            if (obj != null)
            {
                // Retrieve connection and set transaction block
                conn = JdbcHelper.getConnection();
//                conn.setAutoCommit(false);
                
                if (obj instanceof String)
                {
                    // If the object is a String, then we're talking about a complete Person,
                    // where OBJ = PERSON.Person_Id. This will be deleted, along with a cascade of it's
                    // Doc Header and Person Info (attribute) records.
                    personsql.deletePerson(icssql, conn, Integer.parseInt( (String) obj) );
                    
                }
                else if (obj instanceof Integer)
                {
                	// If the object is an integer, then we're talking about deleting only
                	// a single person alias from the person_info table.
                	Integer id = (Integer) obj;
                	this.removePersonAlias(conn, id);
                }
                else if (obj instanceof ArrayList)
                {
                    // If the object is an ArrayList, then individual attribute objects (PersonName, Address, etc.)
                    // will exist. Many will share a single OID referring to the Person_Info_Id.
                    // This ArrayList will contain all the OIDs corresponding to the DocumentHeader Objects
                    // Grab each unique OID and delete where OID = DOCUMENT_HEADER.dh_id
                    
                    List objList = (List) obj;
                    
                    ArrayList objUniqueList = new ArrayList();
                    String oid = null; // OID of attributes
                    int docHeaderId = 0; // PK of document header table (OID of attributes)
                    
                    // Iterate through to get the UNIQUE objects (how many rows to delete in Person_Info) and only delete those
                    Iterator itr = objList.iterator();
                    while (itr.hasNext())
                    {
                        oid = (String) itr.next();
                        if ( !objUniqueList.contains(oid) ) // if it doesn't contain this
                        {
                            objUniqueList.add(oid);
                            
                            docHeaderId = Integer.parseInt(oid); // delete this record (primary key of document_header)
                            
                            // This will delete all document headers for these Document_Header PK's, and cascade delete the Person_Info records.
                            documentheadersql.deleteDocumentHeader(icssql, conn, docHeaderId);
                        }
                    }
                    
                } // instanceof ArrayList (PersonInfo)
            } // instanceof String (Person)
            
            Profile.begin("Connection.commit");
//            conn.commit();
            Profile.end("Connection.commit");
            conn.close();
            
            objectsRemoved = true;
        }
        catch ( Exception e )
        {
//            try
//            {
////                if ( conn != null ) conn.rollback();
//            } // always rollback if we got far enough to have deleted something
//            catch ( SQLException se2 )
//            { }
            try
            {
                if ( conn != null ) conn.close();
            } // always rollback if we got far enough to have deleted something
            catch ( SQLException se2 )
            { }
            throw new DatabaseException("Cannot DELETE a Person Object: " + e.toString());
        }
        
        return objectsRemoved;
    }

    /**
     * Deletes ALL database entries.
     *
     * @throws DatabaseException
     */
    public boolean clear() throws DatabaseException
    { return false; }
    
    
    
    /**
     * Adds a <code>DatabaseServicesListener</code>
     *
     * @param listener the <code>DatabaseServicesListener</code> to add
     * @return boolean
     */
    public boolean addListener(DatabaseServicesListener listener)
    {
        return listeners.add(listener);
    }
    
    
    /**
     * Removes a <code>DatabaseServicesListener</code>
     *
     * @param listener the <code>DatabaseServicesListener</code> to remove
     * @return boolean
     */
    public boolean removeListener(DatabaseServicesListener listener)
    {
        return listeners.remove(listener);
    }
    
    /**
     * Jdbc specifie implementation of the queryIterator() interface.
     * This method generates the required SQL, etc. and returns the
     * Iterator which is a local implementation called PersonIteratorJdbc.
     */
    public Iterator queryIterator(QueryParamList params)
    throws DatabaseException
    {
        return queryIterator(params, true);
    }
    
    /**
     * Jdbc specifie implementation of the queryIterator() interface.
     * This method generates the required SQL, etc. and returns the
     * Iterator which is a local implementation called PersonIteratorJdbc.
     */
    public Iterator queryIterator(QueryParamList params,
            boolean usePreparedStatement)
            throws DatabaseException
    {
        Profile.begin("DatabaseServicesJdbc.queryIterator");
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        Iterator iter = null;
        try
        {
            Profile.begin("JdbcHelper.getConnection");
           conn = JdbcHelper.getConnection();
            Profile.end("JdbcHelper.getConnection");
            if (usePreparedStatement)
            {
                st = Sql.buildQuery(icssql, params, conn);
                st.setFetchSize(250);
                Profile.begin("PreparedStatement.executeQuery");
                rs = ((PreparedStatement) st).executeQuery();
                Profile.end("PreparedStatement.executeQuery");
            }
            else
            {
                String sql = Sql.buildStatement(icssql, params, conn);
                log.debug(sql);
                Profile.begin("Connection.createStatement");
                st = conn.createStatement();
                st.setFetchSize(250);
                Profile.end("Connection.createStatement");
                Profile.begin("Statement.executeQuery");
                rs = st.executeQuery(sql);
                Profile.end("Statement.executeQuery");
            }
            iter = (Iterator) new PersonIteratorJdbc(conn, st, rs);
        }
        catch (SQLException se)
        {
            try
            { if ( rs != null ) rs.close(); }
            catch (SQLException e)
            {}
            try
            { if ( st != null ) st.close(); }
            catch (SQLException e)
            {}
            try
            { if ( conn != null ) conn.close(); }
            catch (SQLException e)
            {}
            throw new DatabaseException("Cannot execute SQL against database with error: " + se.toString());
        }
        Profile.end("DatabaseServicesJdbc.queryIterator");
        return iter;
    }
    
    /**
     * Jdbc specific implementation of the releaseIterator() interface.
     * This closes PreparedStatements and releases Connections associated
     * with the Iterator.
     */
    public void releaseIterator(Iterator personIterator)
    throws DatabaseException
    {
    	if (personIterator == null) {
    		return;
    	}
    	
        if (personIterator instanceof PersonIteratorJdbc)
        {
            ((PersonIteratorJdbc)personIterator).release();
        }
        else
            throw new IllegalArgumentException("Iterator not of correct class");
    }
    
    private Person merge(String[] args)
    {
        Person mergedPerson = null;
        String userId = args[0];
        String reason = args[1];
        String arg;
        List personList = new ArrayList();
        
        int i = 2;
        
        while (i < args.length)
        {
            arg = args[i++];
            
            Person person = new Person();
            person.setOid(arg);
            personList.add(person);
        } // end while
        
        Person[] persons = (Person[])personList.toArray(new Person[0]);
        
        try
        {
            mergedPerson = instance.mergePersons(persons);
        }
        catch (DatabaseException e)
        {
            log.error(e, e);
        }
        
        return mergedPerson;
    }
    
    private Person batchMerge(String batchfile)
    {
        Person mergedPerson = null;
        
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(batchfile));
            StringTokenizer st = null;
            StringBuffer reason = new StringBuffer();
            List argList = new ArrayList();
            boolean flag = false;
            
            while (in.ready())
            {
                // Tokenize each line in the file.
                st = new StringTokenizer(in.readLine(), ",");
                
                while (st.hasMoreTokens())
                {
                    String token = st.nextToken();
                    
                    if (token.startsWith("\""))
                    {
                        // Start of 'reason' parameter.
                        flag = true;
                    }
                    else if (token.endsWith("\""))
                    {
                        // End of 'reason' parameter.
                        flag = false;
                        reason.append(token);
                        token = reason.toString();
                    }
                    
                    if (flag)
                    {
                        reason.append(token);
                    }
                    else
                    {
                        log.debug("batchMerge(): token = " + token);
                        argList.add(token);
                    }
                }
                mergedPerson = instance.merge((String[])argList.toArray(new String[0]));
            }
        }
        catch (java.io.IOException e)
        {
            log.error(e, e);
        }
        
        return mergedPerson;
    }
    
    public int updateEID(String domain, String facility, String oldMRN, String newMRN, String oldEID, String newEID) throws DatabaseException
    {
        /* Latest business rules.  HCoffey 3/8/05
         * - update EIDs for all newMRN aliases (domain, facility, MRN 3pk still holds) to newEID
         * - update EIDs for all oldMRN aliases to newEID
         * - lookup all MRNs (p3k) currently referenced by oldEID, update their aliases to newEID
         */
        
        log.debug("Updating EID for aliases with the following...\n" +
                "Domain: "+domain+"\n" +
                "Facility: "+facility+"\n" +
                "oldMRN (MRG pi_identifier): "+oldMRN+"\n" +
                "newMRN (PID pi_identifier): "+newMRN+"\n" +
                "oldEID: "+oldEID+"\n" +
                "newEID: "+newEID);
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = JdbcHelper.getConnection();
            conn.setAutoCommit(false);
            int rowsUpdated = 0;
            //update oldMRNs
            String updateEIDSQL = "update person_info set updated_corporate_id = ? where " +
                    "aa_namespace_id = ? and " +
                    "af_namespace_id = ? and " +
                    "pi_identifier = ?";
            log.debug("updating old MRNs: "+updateEIDSQL);
            ps = conn.prepareStatement(updateEIDSQL);
            ps.setString(1, newEID);
            ps.setString(2, domain);
            ps.setString(3, facility);
            ps.setString(4, oldMRN);
            
            rowsUpdated = ps.executeUpdate();
            
            //update newMRNs
            
            log.debug("updating new MRNs: "+updateEIDSQL);
            //ps = conn.prepareStatement(updateEIDSQL);
            ps.setString(1, newEID);
            ps.setString(2, domain);
            ps.setString(3, facility);
            ps.setString(4, newMRN);
            rowsUpdated = ps.executeUpdate();
            
            //also update the updated_corporate_id if newEID is present in the updated_corp_id
            if(oldEID != null)
            {
                /*
                String updateLegacyMRNSQL = "update person_info set updated_corporate_id = ? where " +
                        "aa_namespace_id = ? and " +
                        "af_namespace_id = ? and " +
                        "pi_identifier in " +
                        "(" +
                        "(select pi_identifier from person_info where " +
                            "aa_namespace_id = ? and " +
                            "af_namespace_id = ? and " +
                            "updated_corporate_id = ? " +
                        ") or " +
                        "(select pi_identifier from person_info where " +
                            "aa_namespace_id = ? and " +
                            "af_namespace_id = ? and " +
                            "updated_corporate_id is null " +
                            "and corporate_id is ? "+
                        "))";
                 */
                String updateLegacyMRNSQL = "update person_info set updated_corporate_id = ? where " +
                        "aa_namespace_id = ? and "+
                        "af_namespace_id = ? and "+
                        "pi_identifier in "+
                        "(select pi_identifier from person_info where " +
                            "aa_namespace_id = ? and " +
                            "af_namespace_id = ? and " +
                            "decode (updated_corporate_id,null,corporate_id,updated_corporate_id ) = ? )"; 
                ps = conn.prepareStatement(updateLegacyMRNSQL);
                ps.setString(1, newEID);
                ps.setString(2, domain);
                ps.setString(3, facility);
                ps.setString(4, domain);
                ps.setString(5, facility);
                ps.setString(6, oldEID);
                rowsUpdated += ps.executeUpdate();
            }
            
            conn.commit();
            return rowsUpdated;
        }
        catch(Exception e)
        {
            throw new DatabaseException(e);
        }
        finally
        {
            try
            {
                ps.close();
                conn.close();
            }
            catch(SQLException sqe)
            {}
        }
    }
    
    /**
     * Private class that implements Iterator and is returned by the
     * queryIterator() method.
     */
    private class PersonIteratorJdbc
            implements Iterator
    {
        private Statement st = null;
        private Connection conn = null;
        private ResultSet rs = null;
        private boolean moreResults = false;
        private String lastOid = null;
        
        public PersonIteratorJdbc(Connection conn, Statement st,
                ResultSet rs)
        {
            this.rs = rs;
            this.st = st;
            this.conn = conn;
            try
            {
                Profile.begin("ResultSet.next");
                moreResults = rs.next();
                Profile.end("ResultSet.next");
            }
            catch (SQLException e)
            {
                log.error(e, e);
                release();
            }
        }
        
        public void finalize()
        {
            release();
        }
        
        public boolean hasNext()
        {
            return moreResults;
        }
        
        public Object next()
        {
            Profile.begin("PersonIteratorJdbc.next");
            Person person = null;
            try
            {
                String currPersonOid = rs.getString(SEL_ORD_PERSON_ID);
                person = new Person(); // OID will remain null to start
                
                person.setOid( currPersonOid );
                person.setNationality( rs.getString(SEL_ORD_NATIONALITY_CD) );
                person.setNameSearchKey( rs.getString(SEL_ORD_NAMESEARCHKEY) );
                person.setPrimaryLanguage( rs.getString(SEL_ORD_PRIMLANG) );
                person.setExpired( rs.getBoolean(SEL_ORD_EXPIRED) );
                person.setProvider( rs.getBoolean(SEL_ORD_ISPROVIDER) );
                person.setBirthPlace( rs.getString(SEL_ORD_BIRTH_PLACE) );
                
                DocumentHeader last = null;
                while(true)
                {
                    DocumentHeader documentHeader;
                    String dhId = rs.getString(SEL_ORD_DH_ID);
                    
                    if (last != null && last.getOid().equals(dhId))
                        documentHeader = last;
                    else
                    {
                        documentHeader = addDocumentHeader(person,
                                dhId,
                                rs.getTimestamp(SEL_ORD_MSG_DATE),
                                rs.getString(SEL_ORD_MSG_CTRL_ID),
                                rs.getString(SEL_ORD_SEQ_NUM),
                                rs.getString(SEL_ORD_CONT_PTR),
                                rs.getString(SEL_ORD_ACK_TYPE),
                                rs.getString(SEL_ORD_APP_ACK_TYPE),
                                rs.getString(SEL_ORD_MSG_SECURITY),
                                rs.getString(SEL_ORD_REC_APP),
                                rs.getString(SEL_ORD_REC_FAC),
                                rs.getString(SEL_ORD_SEND_APP),
                                rs.getString(SEL_ORD_SEND_FAC),
                                rs.getString(SEL_ORD_EVENT_CD),
                                rs.getString(SEL_ORD_MSG_TYPE),
                                rs.getString(SEL_ORD_TRIGGER_EVENT),
                                rs.getString(SEL_ORD_MSG_STRUCTURE)
                                );
                        last = documentHeader;
                    }
                    
                    
                    // build a PersonName object for the Maiden name using doc header, and set to person
                    PersonName maidenName = new PersonName(documentHeader,
                            rs.getString(SEL_ORD_MAIDEN_LNAME),
                            rs.getString(SEL_ORD_MAIDEN_FNAME),
                            rs.getString(SEL_ORD_MAIDEN_SECNAME));
                    person.setMaidenName(maidenName);
                    
                    addPersonSSN(person, documentHeader, rs.getString(SEL_ORD_SSN));
                    addPersonDOB(person, documentHeader, rs.getTimestamp(SEL_ORD_BIRTHDATE));
                    
                    addPersonName(person, documentHeader,
                            rs.getString(SEL_ORD_LNAME),
                            rs.getString(SEL_ORD_FNAME),
                            rs.getString(SEL_ORD_SECNAME),
                            rs.getString(SEL_ORD_SUFFIX),
                            rs.getString(SEL_ORD_PREFIX),
                            rs.getString(SEL_ORD_DEGREE),
                            rs.getString(SEL_ORD_NAMETYPE_CD),
                            rs.getString(SEL_ORD_NAMETYPEREP_CD),
                            rs.getString(SEL_ORD_PN_SEARCHKEY),
                            rs.getTimestamp(SEL_ORD_PN_STARTDATE),
                            rs.getTimestamp(SEL_ORD_PN_ENDDATE),
                            rs.getBoolean(SEL_ORD_PN_ALIAS),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    addPersonAddress(person,  documentHeader,
                            rs.getString(SEL_ORD_ADDRESS_1),
                            rs.getString(SEL_ORD_ADDRESS_2),
                            rs.getString(SEL_ORD_CITY),
                            rs.getString(SEL_ORD_STATEPROV),
                            rs.getString(SEL_ORD_ZIP),
                            rs.getString(SEL_ORD_COUNTRY),
                            rs.getString(SEL_ORD_PARISH_CD),
                            rs.getTimestamp(SEL_ORD_PN_STARTDATE),
                            rs.getTimestamp(SEL_ORD_PN_ENDDATE),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    addPersonIdentifier(person,  documentHeader,
                            rs.getString(SEL_ORD_PI_IDENTIFIER),
                            rs.getString(SEL_ORD_ID_TYPE_CD),
                            rs.getTimestamp(SEL_ORD_PI_EFF_DATE),
                            rs.getTimestamp(SEL_ORD_PI_EXP_DATE),
                            rs.getBoolean(SEL_ORD_CONSENT),
                            rs.getString(SEL_ORD_AA_UNIV_ID),
                            rs.getString(SEL_ORD_AA_UNIV_ID_TYPE_CD),
                            rs.getString(SEL_ORD_AA_NAMESPACE_ID),
                            rs.getString(SEL_ORD_AF_UNIV_ID),
                            rs.getString(SEL_ORD_AF_UNIV_ID_TYPE_CD),
                            rs.getString(SEL_ORD_AF_NAMESPACE_ID),
                            rs.getString(SEL_ORD_PERSON_INFO_ID),
                            rs.getString(SEL_ORD_CORPORATE_ID),
                            rs.getString(SEL_ORD_UPDATED_CORPORATE_ID)
                            );
                    
                    addPersonPhone(person,  documentHeader,
                            rs.getString(SEL_ORD_PH_USE_CD),
                            rs.getString(SEL_ORD_PH_COUNTRY_CD),
                            rs.getString(SEL_ORD_PH_AREA_CD),
                            rs.getString(SEL_ORD_PH_NUM),
                            rs.getString(SEL_ORD_PH_EXT),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    addPersonEmail(person,  documentHeader,
                            rs.getString(SEL_ORD_EMAIL),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    addPersonGender(person,  documentHeader,
                            rs.getString(SEL_ORD_GENDER),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    addPersonMaritalStatus(person,  documentHeader,
                            rs.getString(SEL_ORD_MARITAL_STATUS),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    //                    java.util.Date drvLicDate = rs.getTimestamp(SEL_ORD_DRV_ISSUE_DATE);
                    //                    long drvLicDateLong = 0;
                    //                    if ( drvLicDate != null ) drvLicDateLong = drvLicDate.getTime();
                    
                    addDriversLicense(person,  documentHeader,
                            rs.getString(SEL_ORD_DRV_LIC_NUM),
                            rs.getString(SEL_ORD_DRV_ISSUE_STATE),
                            //drvLicDateLong,
                            rs.getTimestamp(SEL_ORD_DRV_ISSUE_DATE),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    addEthnicGroup(person,  documentHeader,
                            rs.getString(SEL_ORD_ETHNIC_CD),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    addReligion(person,  documentHeader,
                            rs.getString(SEL_ORD_RELIGION_CD),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    
                    addRace(person,  documentHeader,
                            rs.getString(SEL_ORD_RACE_CD),
                            rs.getString(SEL_ORD_PERSON_INFO_ID)
                            );
                    Profile.begin("ResultSet.next");
                    moreResults = rs.next();
                    Profile.end("ResultSet.next");
                    if (moreResults)
                    {
                        String nextPersonOid = rs.getString(SEL_ORD_PERSON_ID);
                        if (!currPersonOid.equalsIgnoreCase(nextPersonOid))
                            break;
                    }
                    else
                        break;
                }
            }
            catch ( SQLException se )
            {
                release();
                throw new NoSuchElementException(se.getMessage());
            }
            finally
            {
                Profile.end("PersonIteratorJdbc.next");
            }
            return person;
        }
        
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
        
        public void release()
        {
            try
            { if ( rs != null ) rs.close(); }
            catch (SQLException se2)
            {}
            try
            { if ( st != null ) st.close(); }
            catch (SQLException se2)
            {}
            try
            { if ( conn != null ) conn.close(); }
            catch (SQLException se2)
            {}
            st = null;
            conn = null;
            rs = null;
            moreResults = false;
        }
    }
    
}



