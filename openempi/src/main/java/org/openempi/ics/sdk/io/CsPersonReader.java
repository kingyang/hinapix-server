/*
 * Title:       CsPersonReader
 * Description: Reads a record from file to generate a <code>Person</code>
 * Copyright:   (c) 2001-2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.sdk.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

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

/**
 * Reads comma-separated-value records from a file, generating a
 * <code>Person</code> for each record.
 *
 * @author CareScience, Inc.
 * @version 1.5, 20020528
 */
public class CsPersonReader implements PersonReader {
  /** The record delimiter, set to ','. */
  public static final String DELIMITER = ",";

  /** The number of fields within each record, set to 34. */
  public static final int FIELD_COUNT = 34;

  /** Comment string, set to "#" */
  public static final String COMMENT = "#";

  // Field position within each record
  private static final int ID_AUTHORITY = 0;
  private static final int PATIENT_NUM = 1;
  private static final int LAST_NAME = 2;
  private static final int FIRST_NAME = 3;
  private static final int SECOND_NAME = 4;
  private static final int DOB = 5;
  private static final int SOCSEC = 6;
  private static final int GENDER = 7;
  private static final int ADDRESS_1 = 8;
  private static final int ADDRESS_2 = 9;
  private static final int CITY = 10;
  private static final int STATE = 11;
  private static final int ZIP = 12;
  private static final int AREA_CODE = 13;
  private static final int TELEPHONE = 14;
  private static final int EXTENSION = 15;
  private static final int PREFIX = 16;
  private static final int SUFFIX = 17;
  private static final int DEGREE = 18;
  private static final int ALIAS_LNAME = 19;
  private static final int ALIAS_FNAME = 20;
  private static final int ALIAS_MNAME = 21;
  private static final int ALIAS_PREFIX = 22;
  private static final int ALIAS_SUFFIX = 23;
  private static final int ALIAS_DEGREE = 24;
  private static final int RELIGION = 25;
  private static final int RACE = 26;
  private static final int ETHNIC_GROUP = 27;
  private static final int MARITAL_STATUS = 28;
  private static final int EMAIL = 29;
  private static final int DLN = 30;
  private static final int DL_STATE = 31;
  private static final int DL_ISSUE_DATE = 32;
  private static final int DL_EXPIRE_DATE = 33;
  private BufferedReader reader;
  private int currentLine;
  private static final String UUID = "UUID";
  private static final String DNS = "DNS";
  private static final String IDTYPE = "INTERNAL IDENTIFIER";
  private static final int UNIVERSAL_ID = 0;
  private static final int APPLICATION = 1;
  private static final int NAMESPACE = 3;

  private Logger log = Logger.getLogger("ICS");

  /**
   * The constructor
   *
   * @param file a File object
   * @throws IOException
   */
  public CsPersonReader(File file) throws IOException {
    reader = new BufferedReader(new FileReader(file));
  }

  /**
   * Alternate constructor
   *
   * @param fileName the name of the input file
   * @throws IOException
   */
  public CsPersonReader(String fileName) throws IOException {
    this(new File(fileName));
  }

  /**
   * put your documentation comment here
   * @return
   * @exception IOException
   */
  public boolean checkComment() throws IOException {
    boolean check = false;
    String s = reader.readLine();
    if (s.startsWith(COMMENT))
      check = true;
    return  check;
  }

  /**
   * put your documentation comment here
   * @return
   * @exception IOException
   */
  public Person read() throws IOException {
    String s = reader.readLine();
    currentLine++;
    return  stringToRecord(s);
  }

  /**
   * Get a <code>Person</code> from a record
   *
   * @param s a record string
   * @return Person populated with the values from the record
   * @throws IOException
   */
  private Person stringToRecord(String s) throws IOException {
    Person person = null;
    String namespace = null;
    String univId = null;
    String application = null;
    String facility = null;
    Date today = new Date();
    if (s != null && s.length() > 0) {
      person = new Person();
      StringTokenizer tokenizer = new StringTokenizer(s, DELIMITER);
      int tokenCount = tokenizer.countTokens();
      if (tokenizer.countTokens() != FIELD_COUNT) {
        throw  new IOException("Invalid token count at record: " + currentLine
            + ", " + tokenizer.countTokens() + " found.");
      }
      String[] tokens = new String[tokenizer.countTokens()];
      for (int i = 0; tokenizer.hasMoreTokens(); i++) {
        tokens[i] = tokenizer.nextToken();
      }
      // set the sending application, sending facility,
      // namespace
      namespace = getDomainValue(tokens[ID_AUTHORITY], NAMESPACE);
      univId = getDomainValue(tokens[ID_AUTHORITY], UNIVERSAL_ID);
      application = getDomainValue(tokens[ID_AUTHORITY], APPLICATION);
      facility = getFacilityValue(tokens[ID_AUTHORITY]);
      // create the document header that will be associated with all records
      // in the batch
      DocumentHeader dh = new DocumentHeader();
      dh.setEventCode("ADD");
      dh.setMessageDate(today);
      dh.setSendingApplication(application);
      dh.setSendingFacility(facility);
      DateFormat df = new SimpleDateFormat("ddmmyyyyhhMMss");
      String sequence = df.format(today) + "-" + currentLine;
      dh.setSequenceNumber(sequence);
      person.addDocumentHeader(dh);
      // set the person name
      if (!(tokens[LAST_NAME].equals(null)) && !(tokens[FIRST_NAME].equals(null))) {
        PersonName name = new PersonName(dh, getStringValue(tokens[LAST_NAME]), getStringValue(tokens[FIRST_NAME]),
                                         getStringValue(tokens[SECOND_NAME]));
        if (getStringValue(tokens[DEGREE]) != null) {
          name.setDegree(getStringValue(tokens[DEGREE]));
        }
        if (getStringValue(tokens[PREFIX]) != null) {
          name.setPrefix(getStringValue(tokens[PREFIX]));
        }
        if (getStringValue(tokens[SUFFIX]) != null) {
          name.setSuffix(getStringValue(tokens[SUFFIX]));
        }
        person.addName(name);
        log.debug("stringToRecord(): Person Added = " + name.getFirstName() + " " + name.getLastName());
      }
      // creates the Identifier for this record
      if (!(tokens[PATIENT_NUM].equals(null))) {
        PersonIdentifier pid1 = new PersonIdentifier(dh, getStringValue(tokens[PATIENT_NUM]), new DomainIdentifier(dh, univId,
            univId, DNS), new DomainIdentifier(dh, namespace, univId, DNS), IDTYPE);
        pid1.setDocumentHeader(dh);
        person.addPersonIdentifier(pid1);
      }
      // Set the address
      if (getStringValue(tokens[ADDRESS_1]) != null) {
        Address address = new Address(dh, null, null, null, null, null);
        address.setAddress1(getStringValue(tokens[ADDRESS_1]));
        address.setAddress2(getStringValue(tokens[ADDRESS_2]));
        address.setCity(getStringValue(tokens[CITY]));
        address.setState(getStringValue(tokens[STATE]));
        address.setZipCode(getStringValue(tokens[ZIP]));
        person.addAddress(address);
      }
      if (getStringValue(tokens[TELEPHONE]) != null) {
        TelephoneNumber tn1 = new TelephoneNumber(dh, null, null);
        tn1.setAreaCode(zerosCheck(tokens[AREA_CODE]));
        tn1.setPhoneNumber(zerosCheck(tokens[TELEPHONE]));
        tn1.setExtension(zerosCheck(tokens[EXTENSION]));
        person.addTelephoneNumber(tn1);
      }
      // Set the GENDER
      if (getStringValue(tokens[GENDER]) != null) {
        Gender gen = new Gender(dh, null);
        gen.setCreatedDate(today.getTime());
        gen.setValue(getGender(tokens[GENDER]));
        person.addGender(gen);
      }
      // Set the DOB
      if (getStringValue(tokens[DOB]) != null)
        person.addDateOfBirth(new DateOfBirth(dh, getDate(tokens[DOB])));
      // Set any Alias that might exist
      if (getStringValue(tokens[ALIAS_LNAME]) != null || getStringValue(tokens[ALIAS_FNAME])
          != null) {
        PersonName alias = new PersonName(dh, getStringValue(tokens[ALIAS_LNAME]),
            getStringValue(tokens[ALIAS_FNAME]), getStringValue(tokens[ALIAS_MNAME]));
        if (!(tokens[ALIAS_DEGREE].equals(null))) {
          alias.setDegree(getStringValue(tokens[ALIAS_DEGREE]));
        }
        if (getStringValue(tokens[ALIAS_PREFIX]) != null) {
          alias.setPrefix(getStringValue(tokens[ALIAS_PREFIX]));
        }
        if (getStringValue(tokens[SUFFIX]) != null) {
          alias.setSuffix(getStringValue(tokens[SUFFIX]));
        }
        person.addName(alias);
      }
      // Set the SOCSEC
      if (getStringValue(tokens[SOCSEC]) != null) {
        person.addSocialSecurityNumber(new SocialSecurityNumber(dh, zerosCheck(tokens[SOCSEC])));
      }
      // Set the MARITAL_STATUS
      if (getStringValue(tokens[MARITAL_STATUS]) != null) {
        MaritalStatus mStatus = new MaritalStatus(dh, null);
        mStatus.setCreatedDate(today.getTime());
        mStatus.setValue(getMaritalStatus(tokens[MARITAL_STATUS]));
        person.addMaritalStatus(mStatus);
      }
      // Set the religion
      if (getStringValue(tokens[RELIGION]) != null) {
        Religion rel = new Religion(dh, null);
        rel.setCreatedDate(today.getTime());
        rel.setValue(getStringValue(tokens[RELIGION]));
        person.addReligion(rel);
      }
      // Set the race
      if (getStringValue(tokens[RACE]) != null) {
        Race race = new Race(dh, null);
        race.setCreatedDate(today.getTime());
        race.setValue(getRace(getStringValue(tokens[RACE])));
        person.addRace(race);
      }
      // Set the ethnic group
      if (getStringValue(tokens[ETHNIC_GROUP]) != null) {
        EthnicGroup egroup = new EthnicGroup(dh, null);
        egroup.setCreatedDate(today.getTime());
        egroup.setValue(getEthnicGroup(tokens[ETHNIC_GROUP]));
        person.addEthnicGroup(egroup);
      }
      // Set the Email
      if (getStringValue(tokens[EMAIL]) != null) {
        EmailAddress email = new EmailAddress(dh, null);
        email.setEmailAddress(getStringValue(tokens[EMAIL]));
        person.addEmailAddress(email);
      }
      // Set the Driver Licence
      if (getStringValue(tokens[DLN]) != null) {
        DriversLicense dln = new DriversLicense(dh, null, null,
                new Date());
        dln.setNumber(getStringValue(tokens[DLN]));
        dln.setIssueDate(getDate(tokens[DL_ISSUE_DATE]));
        dln.setState(getStringValue(tokens[DL_STATE]));
        person.addDriversLicense(dln);
      }
    }
    return  person;
  }

  /**
   * put your documentation comment here
   * @param s
   * @param octType
   * @return
   */
  private String getDomainValue(String s, int octType) {
    String domain = null;
    String domVal = "";
    int numDots = 0;
    int counter = 0;
    ArrayList subDomains = null;
    if (s != null && s.length() > 0) {
      domain = new String(s);
      // loop through the string and count the occurences of the dot
      for (int i = 0; i < domain.length(); i++) {
        if (domain.charAt(i) == '.') {
          numDots++;
        }
      }
      // create an array of Strings to hold each octect
      // put each octect substring into the array
      subDomains = new ArrayList(numDots);
      for (counter = 0; counter < numDots; counter++) {
        subDomains.add(counter, domain.substring(0, domain.indexOf(".")));
        domain = domain.substring(domain.indexOf(".") + 1, domain.length());
      }
      subDomains.add(counter, domain);
      // if searching for application return first octect
      //
      if (octType == APPLICATION) {
        if (numDots > 1) {
          domVal = (String)subDomains.get(0);
        }
        else {
          domVal = "";
        }
        log.debug("getDomainValue(): Application = " + domVal);
      }
      // if searching for the universal id, flip the octects and return
      // the input String in reverse order
      // returns com.something
      if (octType == UNIVERSAL_ID) {
        for (int i = subDomains.size() - 1; i > 1; i--) {
          domVal = domVal + (String)subDomains.get(i) + ".";
        }
        domVal = domVal + subDomains.get(1);
        log.debug("getDomainValue(): UniversalID = " + domVal);
      }
      // if searching for the universal id, flip the octects and return
      // the input String in reverse order
      // returns com.something.application
      if (octType == NAMESPACE) {
        for (int i = subDomains.size() - 1; i > 0; i--) {
          domVal = domVal + (String)subDomains.get(i) + ".";
        }
        domVal = domVal + subDomains.get(0);
        log.debug("getDomainValue(): Namespace = " + domVal);
      }
    }
    return  domVal;
  }

  /**
   * put your documentation comment here
   * @param s
   * @return
   */
  private String getFacilityValue(String s) {
    String facility = null;
    if (s != null && s.length() > 0) {
      facility = new String();
      int dotIndex = s.indexOf(".");
      if (dotIndex < 2 && dotIndex > -1) {
        facility = s;
      }
      else {
        facility = process(s.substring(dotIndex + 1, s.length()));
      }
    }
    log.debug("getFacilityValue(): Facility = " + facility);
    return  facility;
  }

  /**
   * Converts a date string to an actual Date
   *
   * @param date in YYYYMMDD format, e.g. 19641020
   * @return Date
   */
  private Date getDate(String s) {
    Date date = null;
    if (s.length() == 8) {
      int year = Integer.parseInt(s.substring(0, 4));
      int month = Integer.parseInt(s.substring(4, 6));
      int day = Integer.parseInt(s.substring(6, 8));
      // 03/28/2001, mabundo - Problem: month field is 0-based, e.g. 0 for January.
      // Therefore, all dates were previously off by a month.
      month--;
      if ((year > 0) && (month >= 0) && (day > 0)) {
        date = new GregorianCalendar(year, month, day).getTime();
      }
    }
    return  date;
  }

  /**
   * put your documentation comment here
   * @param s
   * @return
   */
  private String getGender(String s) {
    String gender = null;
    if (s != null && s.length() > 0) {
      s = s.trim().toUpperCase();
      if (s.equals("M") || s.equals("F") || s.equals("O") || s.equals("U") ||
          s.equals("A") || s.equals("N")) {
        gender = s;
      }
      else
        gender = "U";
    }
    return  gender;
  }

  /**
   * put your documentation comment here
   * @param s
   * @return
   */
  private String getMaritalStatus(String s) {
    String maritalStatus = null;
    if (s != null && s.length() > 0) {
      s = s.trim().toUpperCase();
      if (s.equals("M") || s.equals("S") || s.equals("A") || s.equals("U") ||
          s.equals("O") || s.equals("D") || s.equals("W") || s.equals("C") ||
          s.equals("G") || s.equals("P") || s.equals("R") || s.equals("E") ||
          s.equals("N") || s.equals("I")) {
        maritalStatus = s;
      }
    }
    return  maritalStatus;
  }

  /**
   * put your documentation comment here
   * @param s
   * @return
   */
  private String getRace(String s) {
    String race = null;
    if (s != null && s.length() > 0) {
      s = s.trim().toUpperCase();
      if (s.equals("1002-5") || s.equals("2028-9") || s.equals("2054-5") ||
          s.equals("2076-8") || s.equals("2106-3") || s.equals("2131-1")) {
        race = s;
      }
    }
    return  race;
  }

  /**
   * put your documentation comment here
   * @param s
   * @return
   */
  private String getEthnicGroup(String s) {
    String egroup = null;
    if (s != null && s.length() > 0) {
      s = s.trim().toUpperCase();
      if (s.equals("U") || s.equals("N") || s.equals("H")) {
        egroup = s;
      }
    }
    return  egroup;
  }

  /**
   * put your documentation comment here
   * @param s
   * @return
   */
  private String process(String s) {
    if (s != null && s.length() > 0) {
      s = s.trim();
    }
    return  s;
  }

  /**
   *  checks zeros for some reason?
   */
  private String zerosCheck(String s) {
    if (s != null && s.length() > 0) {
      s = process(s);
      for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) != '0') {
          break;
        }
      }
    }
    return  s;
  }

  /**
   * Gets a person's name as a string.
   *
   * @param name the <code>PersonName</code>
   * @return String the person's name
   */
  private String getNameString(PersonName name) {
    String nameString = null;
    // changed logical in following statement from || to &&
    // added in support for suffix as well
    // kef 7/15/01
    if ((name != null) && (name.getLastName() != null)) {
      String lastName = name.getLastName().trim();
      String firstName = name.getFirstName().trim();
      if (firstName == null) {
        nameString = lastName;
      }
      else {
        nameString = firstName + " " + lastName;
      }
    }
    return  nameString;
  }

  // Trim a string before adding it
  private String getStringValue(String s) {
    s = s.trim();
    if ((s == null) || (s.trim().length() == 0))
      return  null;
    return  s;
  }
}
