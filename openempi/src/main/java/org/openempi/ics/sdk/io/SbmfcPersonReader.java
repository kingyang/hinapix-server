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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import org.openempi.data.Address;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DocumentHeader;
import org.openempi.data.DomainIdentifier;
import org.openempi.data.Gender;
import org.openempi.data.MaritalStatus;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.PersonName;
import org.openempi.data.SocialSecurityNumber;
import org.openempi.data.TelephoneNumber;

/**
 * Reads records from a file, generating a <code>Person</code> for each record.
 *
 * @author CareScience, Inc.
 * @version 1.8, 20020528
 */
public class SbmfcPersonReader implements PersonReader {

  // The record delimiter
  private static final String DELIMITER = "|";

    /**
   * Comment string "#"
  */
  public static final String COMMENT = "#";

  // The number of fields within each record
  private static final int TOKEN_COUNT = 15;

  // Sansum's namespace ID
  private static final String NAMESPACE_ID = "com.sansum.medtec";

  // Field numbers within each record
  private static final int REC_TYPE = 0;
  private static final int NAME = 1;
  private static final int PATIENT_NUM = 2;
  private static final int ADDRESS_1 = 3;
  private static final int ADDRESS_2 = 4;
  private static final int CITY_ST = 5;
  private static final int ZIP = 6;
  private static final int TELEPHONE_1 = 7;
  private static final int TELEPHONE_2 = 8;
  private static final int GENDER = 9;
  private static final int DOB = 10;
  private static final int PREV_NAME = 11;
  private static final int SOCSEC = 12;
  private static final int MARITAL_STATUS = 13;
  private static final int MEDICAL_ID = 14;
  //private static final int EXPIRE_DATE = -1 // Not provided

  private BufferedReader reader;
  private int currentLine;

  /**
   * The constructor
   *
   * @param file a File object
   * @throws IOException
   */
  public SbmfcPersonReader(File file) throws IOException {
    reader = new BufferedReader(new FileReader(file));
  }

  /**
   * Alternate constructor
   *
   * @param fileName the name of the input file
   * @throws IOException
   */
  public SbmfcPersonReader(String fileName) throws IOException {
    this(new File(fileName));
  }

  public Person read() throws IOException {
    String s = reader.readLine();
    currentLine++;

    return stringToRecord(s);
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

    if (s != null && s.length() > 0) {
      person = new Person();
      DocumentHeader dh = new DocumentHeader();
      dh.setMessageDate(new Date());

      StringTokenizer tokenizer = new StringTokenizer(s, DELIMITER);

      int tokenCount = tokenizer.countTokens();

      if (tokenizer.countTokens() != TOKEN_COUNT) {
        throw new IOException("Invalid token count.");
      }

      String[] tokens = new String[tokenizer.countTokens()];

      for (int i = 0; tokenizer.hasMoreTokens(); i++) {
        tokens[i] = tokenizer.nextToken();
      }

      PersonIdentifier pid1 = new PersonIdentifier(dh, tokens[PATIENT_NUM],
            null, null, "patient.number");

      DomainIdentifier domId1 =
            new DomainIdentifier(dh, NAMESPACE_ID, null, null);
      pid1.setAssigningAuthority(domId1);

      person.addPersonIdentifier(pid1);

      if(!(tokens[MEDICAL_ID].equals(null))){
          PersonIdentifier pid2 = new PersonIdentifier(dh, tokens[MEDICAL_ID],
                null, null, "patient.medicalID");
          DomainIdentifier domId2 =
                new DomainIdentifier(dh, NAMESPACE_ID, null, null);
          pid2.setAssigningAuthority(domId2);

          person.addPersonIdentifier(pid2);
      }

      // Set the NAME
      person.addName(getNameValues(tokens[NAME]));

      // Set the address
      Address address = getCityState(tokens[CITY_ST]);
      address.setDocumentHeader(dh);
      address.setAddress1(tokens[ADDRESS_1]);
      address.setAddress2(tokens[ADDRESS_2]);
      address.setZipCode(tokens[ZIP]);

      if (address != null)
        person.addAddress(address);

      TelephoneNumber tn1 = new TelephoneNumber(dh, null, null);
      tn1.setPhoneNumber(zerosCheck(tokens[TELEPHONE_1]));

      if (tn1 != null)
        person.addTelephoneNumber(tn1);

      TelephoneNumber tn2 = new TelephoneNumber(dh, null, null);
      tn2.setPhoneNumber(zerosCheck(tokens[TELEPHONE_2]));

      if (tn2 != null)
        person.addTelephoneNumber(tn2);

      // Set the GENDER
      Gender gen = new Gender(dh, null);
      gen.setValue(getGender(tokens[GENDER]));
      person.addGender(gen);

      // Set the DOB
      person.addDateOfBirth(new DateOfBirth(dh, getBirthDate(tokens[DOB])));

      // Set the PREV_NAME
      PersonName alias = getPreviousName(tokens[PREV_NAME]);

      if (alias != null)
        person.addName(alias);

      // Set the SOCSEC
      person.addSocialSecurityNumber(new SocialSecurityNumber(dh, zerosCheck(tokens[SOCSEC])));

      // Set the MARITAL_STATUS
      MaritalStatus mStatus = new MaritalStatus(dh, null);
      mStatus.setValue(getMaritalStatus(tokens[MARITAL_STATUS]));
      person.addMaritalStatus(mStatus);
    }
    return person;
  }

  private PersonName getNameValues(String s) {
    PersonName name = null;

    if (s != null && s.length() > 0) {
      name = new PersonName(new DocumentHeader(), null, null, null);

      int commaIndex = s.indexOf(",");
      int spaceIndex = s.indexOf(" ");

      if (commaIndex > -1) {
        name.setLastName(process(s.substring(0, commaIndex)));

        if ((commaIndex + 2) > s.length())
          return name;

        s = s.substring((commaIndex + 2), s.length()).trim();
      }
      else if (spaceIndex > -1 ) {
        name.setLastName(process(s.substring(0, spaceIndex)));
        s = s.substring(spaceIndex + 1, s.length());
      }
      else {
        name.setLastName(process(s));
        return name;
      }
      spaceIndex = s.indexOf(" ");

      if (spaceIndex > -1) {
        name.setFirstName(process(s.substring(0, spaceIndex)));
        name.setSecondName(process(s.substring(spaceIndex, s.length())));
      }
      else {
        name.setFirstName(process(s));
      }
    }
    return name;
  }

  private Address getCityState(String s) {
    Address addr = null;

    if (s != null && s.length() > 0) {
      addr = new Address(new DocumentHeader(), null, null, null, null, null);
      int commaIndex = s.indexOf(",");
      int spaceIndex = s.indexOf(" ");

      if (commaIndex > -1) {
        addr.setCity(process(s.substring(0, commaIndex)));

        if ((commaIndex + 2) > s.length())
          return addr;
        s = s.substring((commaIndex + 2), s.length()).trim();
      }
      else if (spaceIndex > -1) {
        addr.setCity(process(s.substring(0, spaceIndex)));
        if ((spaceIndex + 1) > s.length())
          return addr;
        s = s.substring(spaceIndex + 1, s.length());
      }
      else {
        addr.setCity(process(s));
        return addr;
      }
      addr.setState(process(s));
    }
    return addr;
  }

  private PersonName getPreviousName(String s) {
    return getNameValues(process(s));
  }

  /**
   * Converts a date string to an actual Date
   *
   * @param date in YYYYMMDD format, e.g. 19641020
   * @return Date
   */
  private Date getBirthDate(String s) {
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
    return date;
  }

  private String getGender(String s) {
    String gender = null;

    if (s != null && s.length() > 0) {
      s = s.toUpperCase();

      if (s.equals("M") || s.equals("F")) {
        gender = s;
      }
    }
    return gender;
  }

  private String getMaritalStatus(String s) {
    String maritalStatus = null;

    if (s != null && s.length() > 0) {
      s = s.toUpperCase();

      if (s.equals("M") || s.equals("S") || s.equals("U")) {
        maritalStatus = s;
      }
    }
    return maritalStatus;
  }

  private String process(String s) {
    if (s != null && s.length() > 0) {
      s = s.trim();
    }
    return s;
  }

  private String zerosCheck(String s) {
    if (s != null && s.length() > 0) {
      s = process(s);

      for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) != '0') { break; }
      }
    }
    return s;
  }

  public boolean checkComment() throws IOException{
        boolean check = false;
        String s = reader.readLine();
        if (s.startsWith(COMMENT))
           check = true;
        return check;
  }
}
