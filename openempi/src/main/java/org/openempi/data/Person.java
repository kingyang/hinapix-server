/*
 * Title      : Person
 * Description: The Person transient object.
 * Copyright  : (c) 1998-2003
 * Company    : CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openempi.ics.utility.Utils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The <code>Person</code> transient object. Each transient object is mirrored
 * by a persistent object in Objectivity.
 *
 * @author CareScience
 * @version 1.6, 20030214
 */
public class Person extends TransientObject {
    private List races_ = new ArrayList();

    private List names_ = new ArrayList();

    private List genders_ = new ArrayList();

    private List religions_ = new ArrayList();

    private List addresses_ = new ArrayList();

    private List personIds_ = new ArrayList();

    private List ethnicGroups_ = new ArrayList();

    private List maritalStatii_ = new ArrayList();

    private List emailAddresses_ = new ArrayList();

    private List driversLicenses_ = new ArrayList();

    private List telephoneNumbers_ = new ArrayList();

    private List documentHeaders_ = new ArrayList();

    private List ssns = new ArrayList();

    private List dobs = new ArrayList();

    private String nationality_;

    private String nameSearchKey_;

    private String primaryLanguage_;

    private String birthPlace_;

    private PersonName maidenName_;

    private boolean expired_ = false;

    private boolean isProvider_ = false;

    // This is here to force inclusion of these two classes in the ejbclient
    private static EventData crap1 = new EventData();

    private static MessageHeaderInfo crap2 = new MessageHeaderInfo();

    private List accountNumbers_ = new ArrayList();

    /**
     * Gets the <code>Attributes</code> sorted by <code>DocumentHeader</code>
     *
     * @param docHeader the <code>DocumentHeader</code> with which the
     *                  <code>Attributes</code> are associated
     * @return Map The collection of attributes related to this
     * <code>DocumentHeader</code>
     */
    public Map getAttributesByDocumentHeader(DocumentHeader docHeader) {
        Map attributeMap = new HashMap();

		/* get Names */
        Iterator itr = getNames().iterator();
        List nameList = new ArrayList();

        while (itr.hasNext()) {
            PersonName name = (PersonName) itr.next();
            if ((name.getDocumentHeader() != null)
                    && (name.getDocumentHeader().equals(docHeader))) {
                nameList.add(name);
            }
        }
        attributeMap.put("names", nameList);

        itr = getAddresses().iterator();
        List addrList = new ArrayList();

        while (itr.hasNext()) {
            Address address = (Address) itr.next();
            if ((address.getDocumentHeader() != null)
                    && (address.getDocumentHeader().equals(docHeader))) {
                addrList.add(address);
            }
        }
        attributeMap.put("addresses", addrList);

        itr = getSocialSecurityNumbers().iterator();
        List ssnList = new ArrayList();

        while (itr.hasNext()) {
            SocialSecurityNumber ssn = (SocialSecurityNumber) itr.next();
            if ((ssn.getDocumentHeader() != null)
                    && (ssn.getDocumentHeader().equals(docHeader))) {
                ssnList.add(ssn);
            }
        }
        attributeMap.put("ssns", ssnList);

        itr = getDatesOfBirth().iterator();
        List dobList = new ArrayList();

        while (itr.hasNext()) {
            DateOfBirth dob = (DateOfBirth) itr.next();
            if ((dob.getDocumentHeader() != null)
                    && (dob.getDocumentHeader().equals(docHeader))) {
                dobList.add(dob);
            }
        }
        attributeMap.put("dobs", dobList);

        itr = getDriversLicenses().iterator();
        List licenseList = new ArrayList();

        while (itr.hasNext()) {
            DriversLicense license = (DriversLicense) itr.next();
            if ((license.getDocumentHeader() != null)
                    && (license.getDocumentHeader().equals(docHeader))) {
                licenseList.add(license);
            }
        }
        attributeMap.put("driversLicenses", licenseList);

        itr = getEmailAddresses().iterator();
        List emailList = new ArrayList();

        while (itr.hasNext()) {
            EmailAddress email = (EmailAddress) itr.next();
            if ((email.getDocumentHeader() != null)
                    && (email.getDocumentHeader().equals(docHeader))) {
                emailList.add(email);
            }
        }
        attributeMap.put("emailAddresses", emailList);

        itr = getEthnicGroups().iterator();
        List ethnicGrpList = new ArrayList();

        while (itr.hasNext()) {
            EthnicGroup eGroup = (EthnicGroup) itr.next();
            if ((eGroup.getDocumentHeader() != null)
                    && (eGroup.getDocumentHeader().equals(docHeader))) {
                ethnicGrpList.add(eGroup);
            }
        }
        attributeMap.put("ethnicGroups", ethnicGrpList);

        itr = getGenders().iterator();
        List genderList = new ArrayList();

        while (itr.hasNext()) {
            Gender gender = (Gender) itr.next();
            if ((gender.getDocumentHeader() != null)
                    && (gender.getDocumentHeader().equals(docHeader))) {
                genderList.add(gender);
            }
        }
        attributeMap.put("genders", genderList);

        itr = getMaritalStatii().iterator();
        List mStatusList = new ArrayList();

        while (itr.hasNext()) {
            MaritalStatus mStatus = (MaritalStatus) itr.next();
            if ((mStatus.getDocumentHeader() != null)
                    && (mStatus.getDocumentHeader().equals(docHeader))) {
                mStatusList.add(mStatus);
            }
        }
        attributeMap.put("maritalStatii", mStatusList);

        itr = getPersonIdentifiers().iterator();
        List pIdentifierList = new ArrayList();

        while (itr.hasNext()) {
            PersonIdentifier pid = (PersonIdentifier) itr.next();
            if ((pid.getDocumentHeader() != null)
                    && (pid.getDocumentHeader().equals(docHeader))) {
                pIdentifierList.add(pid);
            }
        }
        attributeMap.put("personIdentifiers", pIdentifierList);

        itr = getRaces().iterator();
        List raceList = new ArrayList();

        while (itr.hasNext()) {
            Race race = (Race) itr.next();
            if ((race.getDocumentHeader() != null)
                    && (race.getDocumentHeader().equals(docHeader))) {
                raceList.add(race);
            }
        }
        attributeMap.put("races", raceList);

        itr = getReligions().iterator();
        List religionList = new ArrayList();

        while (itr.hasNext()) {
            Religion religion = (Religion) itr.next();
            if ((religion.getDocumentHeader() != null)
                    && (religion.getDocumentHeader().equals(docHeader))) {
                religionList.add(religion);
            }
        }
        attributeMap.put("religions", religionList);

        itr = getTelephoneNumbers().iterator();
        List phoneList = new ArrayList();

        while (itr.hasNext()) {
            TelephoneNumber phone = (TelephoneNumber) itr.next();
            if ((phone.getDocumentHeader() != null)
                    && (phone.getDocumentHeader().equals(docHeader))) {
                phoneList.add(phone);
            }
        }
        attributeMap.put("telephoneNumbers", phoneList);

        itr = getAccountNumbers().iterator();
        List accountNumberList = new ArrayList();
        while (itr.hasNext()) {
            PersonIdentifier accountNumber = (PersonIdentifier) itr.next();
            if ((accountNumber.getDocumentHeader() != null)
                    && (accountNumber.getDocumentHeader().equals(docHeader))) {
                accountNumberList.add(accountNumber);
            }
        }
        attributeMap.put("accountNumbers", phoneList);

        return attributeMap;
    }

    /**
     * Adds a <code>DocumentHeader</code>.
     *
     * @param newDocumentHeader the <code>DocumentHeader</code> to be added
     */
    public void addDocumentHeader(DocumentHeader newDocumentHeader) {
        documentHeaders_.add(newDocumentHeader);
    }

    /**
     * Updates a <code>Person's</code> attributes with those from another
     * <code>Person</code>.
     *
     * @param person the <code>Person</code> whose attributes will be used for
     *               the update.
     */
    public void updatePerson(Person person) {
        races_.addAll(person.getRaces());
        names_.addAll(person.getNames());
        genders_.addAll(person.getGenders());
        religions_.addAll(person.getReligions());
        addresses_.addAll(person.getAddresses());
        personIds_.addAll(person.getPersonIdentifiers());
        ethnicGroups_.addAll(person.getEthnicGroups());
        maritalStatii_.addAll(person.getMaritalStatii());
        emailAddresses_.addAll(person.getEmailAddresses());
        driversLicenses_.addAll(person.getDriversLicenses());
        telephoneNumbers_.addAll(person.getTelephoneNumbers());
        accountNumbers_.addAll(person.getAccountNumbers());
        documentHeaders_.addAll(person.getDocumentHeaders());
    }

    /**
     * Removes a <code>DocumentHeader</code>.
     *
     * @param documentHeader the <code>DocumentHeader</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeDocumentHeader(DocumentHeader documentHeader) {
        return documentHeaders_.remove(documentHeader);
    }

    /**
     * Gets the read-only <code>DocumentHeaders</code> list.
     *
     * @return List The <code>DocumentHeader</code> list
     */
    public final List getDocumentHeaders() {
        return Collections.unmodifiableList(documentHeaders_);
    }

    /**
     * Clears the <code>DocumentHeader</code> list.
     */
    public void clearDocumentHeaders() {
        documentHeaders_.clear();
    }

    /**
     * Adds a <code>PersonName</code>.
     *
     * @param newName the <code>PersonName</code> to be added
     */
    public void addName(PersonName newName) {
        names_.add(newName);
    }

    /**
     * Removes a <code>PersonName</code>.
     *
     * @param name the <code>PersonName</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeName(PersonName name) {
        return names_.remove(name);
    }

    /**
     * Gets the read-only <code>PersonName</code> list.
     *
     * @return List The <code>PersonName</code> list
     */
    public List getNames() {
        return Collections.unmodifiableList(names_);
    }

    /**
     * Clears the <code>PersonName</code> list.
     */
    public void clearNames() {
        names_.clear();
    }

    /**
     * Adds a <code>EthnicGroup</code>.
     *
     * @param newEthnicGroup the <code>EthnicGroup</code> to be added
     */
    public void addEthnicGroup(EthnicGroup newEthnicGroup) {
        ethnicGroups_.add(newEthnicGroup);
    }

    /**
     * Removes a <code>EthnicGroup</code>.
     *
     * @param ethnicGroup the <code>EthnicGroup</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeEthnicGroup(EthnicGroup ethnicGroup) {
        return ethnicGroups_.remove(ethnicGroup);
    }

    /**
     * Gets the read-only <code>EthnicGroup</code> list.
     *
     * @return List The <code>EthnicGroup</code> list
     */
    public List getEthnicGroups() {
        return Collections.unmodifiableList(ethnicGroups_);
    }

    /**
     * Clears the <code>EthnicGroup</code> list.
     */
    public void clearEthnicGroups() {
        ethnicGroups_.clear();
    }

    /**
     * Adds a <code>Religion</code>.
     *
     * @param newReligion the <code>Religion</code> to be added
     */
    public void addReligion(Religion newReligion) {
        religions_.add(newReligion);
    }

    /**
     * Removes a <code>Religion</code>.
     *
     * @param religion the <code>Religion</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeReligion(Religion religion) {
        return religions_.remove(religion);
    }

    /**
     * Gets the read-only <code>Religion</code> list.
     *
     * @return List The <code>Religion</code> list
     */
    public List getReligions() {
        return Collections.unmodifiableList(religions_);
    }

    /**
     * Clears the <code>Religion</code> list.
     */
    public void clearReligions() {
        religions_.clear();
    }

    /**
     * Adds a <code>MaritalStatus</code>.
     *
     * @param newMaritalStatus the <code>MaritalStatus</code> to be added
     */
    public void addMaritalStatus(MaritalStatus newMaritalStatus) {
        maritalStatii_.add(newMaritalStatus);
    }

    /**
     * Removes a <code>MaritalStatus</code>.
     *
     * @param mStatus the <code>MaritalStatus</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeMaritalStatus(MaritalStatus mStatus) {
        return maritalStatii_.remove(mStatus);
    }

    /**
     * Gets the read-only <code>MaritalStatus</code> list.
     *
     * @return List The <code>MaritalStatus</code> list
     */
    public List getMaritalStatii() {
        return Collections.unmodifiableList(maritalStatii_);
    }

    /**
     * Clears the <code>MaritalStatus</code> list.
     */
    public void clearMaritalStatii() {
        maritalStatii_.clear();
    }

    /**
     * Adds a <code>Race</code>.
     *
     * @param newRace the <code>Race</code> to be added
     */
    public void addRace(Race newRace) {
        races_.add(newRace);
    }

    /**
     * Removes a <code>Race</code>.
     *
     * @param race the <code>Race</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeRace(Race race) {
        return races_.remove(race);
    }

    /**
     * Gets the read-only <code>Race</code> list.
     *
     * @return List The <code>Race</code> list
     */
    public List getRaces() {
        return Collections.unmodifiableList(races_);
    }

    /**
     * Clears the <code>Race</code> list.
     */
    public void clearRaces() {
        races_.clear();
    }

    /**
     * Adds a <code>Gender</code>.
     *
     * @param newGender the <code>Gender</code> to be added
     */
    public void addGender(Gender newGender) {
        genders_.add(newGender);
    }

    /**
     * Removes a <code>Gender</code>.
     *
     * @param gender the <code>Gender</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeGender(Gender gender) {
        return genders_.remove(gender);
    }

    /**
     * Gets the read-only <code>Genders</code> list.
     *
     * @return List The <code>Gender</code> list
     */
    public List getGenders() {
        return Collections.unmodifiableList(genders_);
    }

    /**
     * Clears the <code>Gender</code> list.
     */
    public void clearGenders() {
        genders_.clear();
    }

    /**
     * Adds a <code>TelephoneNumber</code>.
     *
     * @param newTelephoneNumber the <code>TelephoneNumber</code> to be added
     */
    public void addTelephoneNumber(TelephoneNumber newTelephoneNumber) {
        telephoneNumbers_.add(newTelephoneNumber);
    }

    /**
     * Removes a <code>TelephoneNumber</code>.
     *
     * @param telephoneNumber the <code>TelephoneNumber</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeTelephoneNumber(TelephoneNumber telephoneNumber) {
        return telephoneNumbers_.remove(telephoneNumber);
    }

    /**
     * Gets the read-only <code>TelephoneNumber</code> list.
     *
     * @return List The <code>TelephoneNumber</code> list
     */
    public List getTelephoneNumbers() {
        return Collections.unmodifiableList(telephoneNumbers_);
    }

    /**
     * Clears the <code>TelephoneNumber</code> list.
     */
    public void clearTelephoneNumbers() {
        telephoneNumbers_.clear();
    }

    /**
     * Adds a <code>EmailAddress</code>.
     *
     * @param newEmailAddress the <code>EmailAddress</code> to be added
     */
    public void addEmailAddress(EmailAddress newEmailAddress) {
        emailAddresses_.add(newEmailAddress);
    }

    /**
     * Removes a <code>EmailAddress</code>.
     *
     * @param emailAddress the <code>EmailAddresses</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeEmailAddress(EmailAddress emailAddress) {
        return emailAddresses_.remove(emailAddress);
    }

    /**
     * Gets the read-only <code>EmailAddress</code> list.
     *
     * @return List The <code>EmailAddress</code> list
     */
    public List getEmailAddresses() {
        return Collections.unmodifiableList(emailAddresses_);
    }

    /**
     * Clears the <code>EmailAddress</code> list.
     */
    public void clearEmailAddresses() {
        emailAddresses_.clear();
    }

    /**
     * Adds a <code>Address</code>.
     *
     * @param newAddress the <code>Address</code> to be added
     */
    public void addAddress(Address newAddress) {
        addresses_.add(newAddress);
    }

    /**
     * Removes a <code>Address</code>.
     *
     * @param address the <code>Address</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeAddress(Address address) {
        return addresses_.remove(address);
    }

    /**
     * Gets the read-only <code>Address</code> list.
     *
     * @return List The <code>Address</code> list
     */
    public List getAddresses() {
        return Collections.unmodifiableList(addresses_);
    }

    /**
     * Clears the <code>Address</code> list.
     */
    public void clearAddresses() {
        addresses_.clear();
    }

    /**
     * Adds a <code>PersonIdentifier</code>.
     *
     * @param newPersonIdentifier the <code>PersonIdentifier</code> to be added
     */
    public void addPersonIdentifier(PersonIdentifier newPersonIdentifier) {
        personIds_.add(newPersonIdentifier);
    }

    /**
     * Removes a <code>PersonIdentifier</code>.
     *
     * @param personId the <code>PersonIdentifier</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removePersonIdentifier(PersonIdentifier personId) {
        return personIds_.remove(personId);
    }

    /**
     * Gets the read-only <code>PersonIdentifier</code> list.
     *
     * @return List The <code>PersonIdentifier</code> list
     */
    public List getPersonIdentifiers() {
        return Collections.unmodifiableList(personIds_);
    }

    /**
     * Clears the <code>PersonIdentifier</code> list.
     */
    public void clearPersonIdentifiers() {
        personIds_.clear();
    }

    /**
     * Adds a <code>DriversLicense</code>.
     *
     * @param newDriversLicense the <code>DriversLicense</code> to be added
     */
    public void addDriversLicense(DriversLicense newDriversLicense) {
        driversLicenses_.add(newDriversLicense);
    }

    /**
     * Removes a <code>DriversLicense</code>.
     *
     * @param driversLicense the <code>DriversLicense</code> to be removed
     * @return boolean true if removed, false otherwise
     */
    public boolean removeDriversLicense(DriversLicense driversLicense) {
        return driversLicenses_.remove(driversLicense);
    }

    /**
     * Gets the read-only <code>DriversLicense</code> list.
     *
     * @return List The <code>DriversLicense</code> list
     */
    public List getDriversLicenses() {
        return Collections.unmodifiableList(driversLicenses_);
    }

    /**
     * Clears the <code>DriversLicense</code> list.
     */
    public void clearDriversLicenses() {
        driversLicenses_.clear();
    }

    /**
     * Checks whether this <code>Person</code> is a Provider.
     *
     * @return boolean true if Provider, false otherwise
     */
    public boolean isProvider() {
        return isProvider_;
    }

    /**
     * Sets the Provider flag.
     *
     * @param value the value to be set for the isProvider flag
     */
    public void setProvider(boolean value) {
        isProvider_ = value;
    }

    /**
     * Checks whether this <code>Person</code> is Expired.
     *
     * @return boolean true if Expired, false otherwise
     */
    public boolean isExpired() {
        return expired_;
    }

    /**
     * Sets the isExpired flag.
     *
     * @param value the value to be set for the isExpired flag
     */
    public void setExpired(boolean value) {
        expired_ = value;
    }

    /**
     * Gets the maiden name.
     *
     * @return PersonName The MaidenName
     */
    public PersonName getMaidenName() {
        return maidenName_;
    }

    /**
     * Sets the maiden name.
     *
     * @param name the value of MaidenName to be set for this <code>Person</code>
     */
    public void setMaidenName(PersonName name) {
        maidenName_ = name;
    }

    /**
     * Sets the date of birth.
     *
     * @param dob the date of birth to be set
     */
    public void addDateOfBirth(DateOfBirth dob) {
        dobs.add(dob);
    }

    /**
     * Gets the date of birth.
     *
     * @return Date The date of birth
     */
    public Date removeDateOfBirth(DateOfBirth dob) {
        return (Date) dobs.remove(dobs.indexOf(dob));
    }

    /**
     * Gets the read-only <code>dob</code> list.
     *
     * @return List The <code>dob</code> list
     */
    public List getDatesOfBirth() {
        return Collections.unmodifiableList(dobs);
    }

    /**
     * Sets the PrimaryLanguage.
     *
     * @param newPrimaryLanguage the value of PrimaryLanguage to be set for the current
     *                           <code>Person</code>
     */
    public void setPrimaryLanguage(String newPrimaryLanguage) {
        primaryLanguage_ = newPrimaryLanguage != null ? newPrimaryLanguage
                .trim() : null;
    }

    /**
     * Gets the PrimaryLanguage.
     *
     * @return String The PrimaryLanguage
     */
    public String getPrimaryLanguage() {
        return primaryLanguage_;
    }

    /**
     * Sets the NameSearchKey.
     *
     * @param newNameSearchKey the value of NameSearchKey to be set for the current
     *                         <code>Person</code>
     */
    public void setNameSearchKey(String newNameSearchKey) {
        nameSearchKey_ = newNameSearchKey != null ? newNameSearchKey.trim()
                : null;
    }

    /**
     * Gets the NameSearchKey.
     *
     * @return String The NameSearchKey
     */
    public String getNameSearchKey() {
        return nameSearchKey_;
    }

    /**
     * Adds the Social Security number.
     *
     * @param ssn the value of Social Security number to be set
     */
    public void addSocialSecurityNumber(SocialSecurityNumber ssn) {
        ssns.add(ssn);
    }

    /**
     * Gets the Social Security number.
     *
     * @return String The Social Security number for this<code>Person</code>
     */
    public String removeSocialSecurityNumber(SocialSecurityNumber ssn) {
        return (String) ssns.remove(ssns.indexOf(ssn));
    }

    /**
     * Gets the read-only <code>ssn</code> list.
     *
     * @return List The <code>ssn</code> list
     */
    public List getSocialSecurityNumbers() {
        return Collections.unmodifiableList(ssns);
    }

    /**
     * Sets the <code>Nationality</code>.
     *
     * @param newNationality the <code>Nationality</code> to be set for this
     *                       <code>Person</code>
     */
    public void setNationality(String newNationality) {
        nationality_ = newNationality != null ? newNationality.trim() : null;
    }

    /**
     * Gets the <code>Nationality</code>.
     *
     * @return String The <code>Nationality</code>
     */
    public String getNationality() {
        return nationality_;
    }

    /**
     * Sets the place of birth.
     *
     * @param newBirthPlace the place of birth to be set for this <code>Person</code>
     */
    public void setBirthPlace(String newBirthPlace) {
        birthPlace_ = newBirthPlace;
    }

    /**
     * Gets the place of birth.
     *
     * @return Address The place of birth
     */
    public String getBirthPlace() {
        return birthPlace_;
    }


    public void addAccountNumber(PersonIdentifier accountNumber) {
        accountNumbers_.add(accountNumber);
    }

    public boolean removeAccountNumber(PersonIdentifier accountNumber) {
        return accountNumbers_.remove(accountNumber);
    }

    public List getAccountNumbers() {
        return Collections.unmodifiableList(accountNumbers_);
    }

    public void clearAccountNumbers() {
        accountNumbers_.clear();
    }


    /**
     * Checks if an input <code>Person</code> has the same domain.
     * <p/>
     *
     * @param person the input <code>Person</code>
     * @return boolean true if from same domain, false otherwise
     */
    public boolean checkDomain(Person person) {
        boolean sameDomain = false;

        if (person != null) {
            DocumentHeader personHeader = Utils.getFirstHeader(this
                    .getDocumentHeaders());
            DocumentHeader matchHeader = Utils.getFirstHeader(person
                    .getDocumentHeaders());

            String pSendFacil = personHeader.getSendingFacility();
            String mSendFacil = matchHeader.getSendingFacility();

            // Check if the sending facilities are the same.
            if ((pSendFacil != null) && (!pSendFacil.equals(""))) {
                if ((mSendFacil != null) && (!mSendFacil.equals(""))) {
                    if (pSendFacil.equals(mSendFacil)) {
                        sameDomain = true;
                    }
                }
            }

            if (sameDomain != true) {
                String pSendApp = personHeader.getSendingApplication();
                String mSendApp = matchHeader.getSendingApplication();

                // Check if the sending applications are the same.
                if ((pSendApp != null) && (!pSendApp.equals(""))) {
                    if ((mSendApp != null) && (!mSendApp.equals(""))) {
                        if (pSendApp.equals(mSendApp)) {
                            sameDomain = true;
                        }
                    }
                }
            }
        }
        return sameDomain;
    }

//	public boolean checkPiDomain(Person person) {
//		boolean sameDomain = false;
//		
//		if (person != null && person.getP) {
//			
//		}
//	}

    /**
     * Checks for validity of this <code>Person</code>.
     *
     * @throws IllegalArgumentException if anything is bad
     */
    public void isValid() throws IllegalArgumentException {
        Iterator it = null;

        // Try-catch blocks added to force an exception in case of an empty
        // iterator.
        try {
            it = personIds_.iterator();

            // Call next() on iterator to force an exception if it's empty.
            PersonIdentifier personID = (PersonIdentifier) it.next();

            personID.isValid();

            while (it.hasNext()) {
                personID = (PersonIdentifier) it.next();

                personID.isValid();
            }
        } catch (java.util.NoSuchElementException e) {
            throw new IllegalArgumentException(
                    "Must have at least one PersonIdentifier.");
        }

        try {
            it = names_.iterator();

            // Call next() on iterator to force an exception if it's empty.
            PersonName name = (PersonName) it.next();

            name.isValid();

            while (it.hasNext()) {
                name = (PersonName) it.next();

                name.isValid();
            }
        } catch (java.util.NoSuchElementException e) {
            throw new IllegalArgumentException(
                    "Must have at least one PersonName.");
        }

        try {
            it = documentHeaders_.iterator();

            // Call next() on iterator to force an exception if it's empty.
            DocumentHeader dh = (DocumentHeader) it.next();

            dh.isValid();

            while (it.hasNext()) {
                dh = (DocumentHeader) it.next();

                dh.isValid();
            }
        } catch (java.util.NoSuchElementException e) {
            throw new IllegalArgumentException(
                    "Must have at least one DocumentHeader.");
        }

        it = ssns.iterator();
        SocialSecurityNumber ssn;

        while (it.hasNext()) {
            ssn = (SocialSecurityNumber) it.next();
            ssn.isValid();
        }

        it = dobs.iterator();
        DateOfBirth dob;

        while (it.hasNext()) {
            dob = (DateOfBirth) it.next();
            dob.isValid();
        }

    }

    /**
     * Prints the name and the person identifier of this person.
     */
    public String printDetails() {
        List names = getNames();
        List ids = getPersonIdentifiers();
        String name = null;
        String id = null;

		/* get the first PersonName for the Person */
        if ((names != null) && (names.size() > 0)) {
            PersonName pName = (PersonName) names.get(0);
            name = pName.getFirstName() + " " + pName.getLastName();
        }

		/* get the first PersonIdentifier for the Person */
        if ((ids != null) && (ids.size() > 0)) {
            PersonIdentifier pId = (PersonIdentifier) ids.get(0);
            id = pId.getId();

			/* get the Assigning Facility */
            if (pId.getAssigningFacility() != null) {
                id = id + "@" + pId.getAssigningFacility().getNameSpaceID();
            }

			/* get the Assigning Authority */
            else if (pId.getAssigningAuthority() != null) {
                id = id + "@" + pId.getAssigningAuthority().getNameSpaceID();
            }
        }

        return "PERSON_ID=" + getOid() + ", " + name + ", " + id;
    }


    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (obj != null) {
            if (obj == this) {
                isEqual = true;
            } else if (getClass() == obj.getClass()) {
                Person other = (Person) obj;
                isEqual = true;

                if (!super.equals(obj)) {
                    isEqual = false;
                } else if (expired_ != other.expired_) {
                    isEqual = false;
                } else if ((maidenName_ == null) ? (other.maidenName_ != null)
                        : (!maidenName_.equals(other.maidenName_))) {
                    isEqual = false;
                } else if ((primaryLanguage_ == null) ? (other.primaryLanguage_ != null)
                        : (!primaryLanguage_.equals(other.primaryLanguage_))) {
                    isEqual = false;
                } else if ((birthPlace_ == null) ? (other.birthPlace_ != null)
                        : (!birthPlace_.equals(other.birthPlace_))) {
                    isEqual = false;
                } else if ((nationality_ == null) ? (other.nationality_ != null)
                        : (!nationality_.equals(other.nationality_))) {
                    isEqual = false;
                } else if (!getSocialSecurityNumbers().equals(
                        other.getSocialSecurityNumbers())) {
                    isEqual = false;
                } else if (!getDatesOfBirth().equals(other.getDatesOfBirth())) {
                    isEqual = false;
                } else if (!getRaces().equals(other.getRaces())) {
                    isEqual = false;
                } else if (!getNames().equals(other.getNames())) {
                    isEqual = false;
                } else if (!getGenders().equals(other.getGenders())) {
                    isEqual = false;
                } else if (!getAddresses().equals(other.getAddresses())) {
                    isEqual = false;
                } else if (!getPersonIdentifiers().equals(
                        other.getPersonIdentifiers())) {
                    isEqual = false;
                } else if (!getReligions().equals(other.getReligions())) {
                    isEqual = false;
                } else if (!getEthnicGroups().equals(other.getEthnicGroups())) {
                    isEqual = false;
                } else if (!getMaritalStatii().equals(other.getMaritalStatii())) {
                    isEqual = false;
                } else if (!getEmailAddresses().equals(
                        other.getEmailAddresses())) {
                    isEqual = false;
                } else if (!getDriversLicenses().equals(
                        other.getDriversLicenses())) {
                    isEqual = false;
                } else if (!getTelephoneNumbers().equals(
                        other.getTelephoneNumbers())) {
                    isEqual = false;
                }else if (!getAccountNumbers().equals(other.getAccountNumbers())) {
                    isEqual = false;
                }
            }
        }
        return isEqual;
    }

    public int hashCode() {
        int code = 0;
        Iterator iter;

        iter = getDatesOfBirth().iterator();
        while (iter.hasNext()) {
            DateOfBirth dob = (DateOfBirth) iter.next();
            code += dob.hashCode();
        }
        iter = getSocialSecurityNumbers().iterator();
        while (iter.hasNext()) {
            SocialSecurityNumber ssn = (SocialSecurityNumber) iter.next();
            code += ssn.hashCode();
        }
        iter = getNames().iterator();
        while (iter.hasNext()) {
            PersonName name = (PersonName) iter.next();
            code += name.hashCode();
        }
        iter = getGenders().iterator();
        while (iter.hasNext()) {
            Gender gender = (Gender) iter.next();
            code += gender.hashCode();
        }
        iter = getAddresses().iterator();
        while (iter.hasNext()) {
            Address address = (Address) iter.next();
            code += address.hashCode();
        }
        iter = getPersonIdentifiers().iterator();
        while (iter.hasNext()) {
            PersonIdentifier personIdentifier = (PersonIdentifier) iter.next();
            code += personIdentifier.hashCode();
        }
        iter = getDriversLicenses().iterator();
        while (iter.hasNext()) {
            DriversLicense driversLicense = (DriversLicense) iter.next();
            code += driversLicense.hashCode();
        }
        iter = getTelephoneNumbers().iterator();
        while (iter.hasNext()) {
            TelephoneNumber telephoneNumber = (TelephoneNumber) iter.next();
            code += telephoneNumber.hashCode();
        }
        return code;

    }

    public String toString() {
        return new ToStringBuilder(this).append("names_", names_).toString();
    }


}
