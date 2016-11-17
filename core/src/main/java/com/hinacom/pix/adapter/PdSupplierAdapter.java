/* Copyright 2009 Misys PLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License. 
 */
package com.hinacom.pix.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DocumentHeader;
import org.openempi.data.DomainIdentifier;
import org.openempi.data.Gender;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.SocialSecurityNumber;
import org.openempi.data.TelephoneNumber;
import org.openempi.ics.pids.IdentityService;
import org.openempi.ics.pids.IdentityServiceHome;
import com.hinacom.pix.data.DriversLicense;
import com.hinacom.pix.data.MessageHeader;
import com.hinacom.pix.data.Patient;
import com.hinacom.pix.data.PatientIdentifier;
import com.hinacom.pix.data.PersonName;
import com.hinacom.pix.ihe.IPdSupplierAdapter;
import com.hinacom.pix.ihe.PdSupplierException;
import com.hinacom.pix.ihe.pdq.PdqQuery;
import com.hinacom.pix.ihe.pdq.PdqResult;

import com.misyshealthcare.connect.base.SharedEnums;
import com.misyshealthcare.connect.base.demographicdata.Address;
import com.misyshealthcare.connect.base.demographicdata.PhoneNumber;
import com.misyshealthcare.connect.net.Identifier;
import org.openempi.ics.pids.IdentityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This adapter implements the <code>IPdSupplierAdapter</code> interface. 
 * It is the bridge between the <code>PdSupplier</code> of OpenPixPDq
 * and OpenEMPI. PixManager Patient data are passed via this adapter.
 * 
 * @author Wenzhi Li
 * @version 1.0, Dec 15, 2008
 */
public class PdSupplierAdapter implements IPdSupplierAdapter {

	private static Logger log = Logger.getLogger(PdSupplierAdapter.class);
    @Autowired
	private IdentityService identityService;
    private IdentityServiceHome identityServiceHome;

	/**
	 * Finds a list of matched patients based on PDQ query parameters.
	 *
	 * @param query the <code>PdqQuery</code>
	 * @param header the <code>MessageHeader</code>
	 * @throws PdSupplierException when there is trouble finding the patients
	 * @return a <code>PdqResult</code> which contains a list of list 
	 *         of <code>Patient</code  The first list is a list 
	 *         of different logic patients, while the second list is a list of 
	 *         the same patient in different domain systems. PdqResult also 
	 *         contains a continuation reference number.
	 * @see PdqResult        
	 */
	public PdqResult findPatients(PdqQuery query, MessageHeader header)
			throws PdSupplierException {
		// identityService = getIdentityService();
		try {
			List results = identityService.findCandidates(getPerson(query, header), 0.0);
			if (results == null) {
				throw new PdSupplierException("Failed to find patients");
			}
			//Converts to Patients
			List<List<Patient>> allPatients = new ArrayList<List<Patient>>();
			for (int i=0; i<results.size(); i++) {
				List<Patient> patients = new ArrayList<Patient>();
				Person p = (Person)results.get(i);
				Patient patient = toPatient(p);
				patients.add(patient);
				allPatients.add(patients);
			}
			return new PdqResult(allPatients);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new PdSupplierException(e);
		}

	}

	/**
	 * Cancels the existing PDQ Query whose reference id is given by pointer.
	 * 
	 *
	 * @param messageQueryName the messageQueryName 
	 * @throws PdSupplierException when there is trouble canceling the query.
	 */
	public void cancelQuery(String queryTag, String messageQueryName)
			throws PdSupplierException {
		//TODO: Add Implementation
		//Need to keep a mapping between the continuation reference 
		//id and the given queryTag.
	}
	
	/**
	 * Converts from <code>Person</code> used by OpenEMPI to
	 * a <code>Patient</code> used by OpenPIXPDQ.
	 * 
	 * @param person the <code>Person</code> object to be converted from
	 * @return a <code>Patient</code> 
	 */
	private Patient toPatient(Person person) {
		Patient ret = new Patient();
		//Address
		ret.setAddresses(toAddress(person.getAddresses()));
		//Gender
		SharedEnums.SexType sex = toSex(person.getGenders());
		if (sex != null) ret.setAdministrativeSex(sex);
		//BirthDate
		ret.setBirthDateTime(toBirthDate(person.getDatesOfBirth()));
		//birth place
		ret.setBirthPlace(person.getBirthPlace());
		//Drivers' License
		ret.setDriversLicense(toDriversLicense(person.getDriversLicenses()));
		//enthnic group
		ret.setEthnicGroup(toEthnicGroup(person.getEthnicGroups()));
		//marital Status
		ret.setMaritalStatus(toMaritalStatus(person.getMaritalStatii()));
		// Mothers Maiden Name
		ret.setMonthersMaidenName(toPersonName(person.getMaidenName()));
		//Patient ID
		ret.setPatientIds(toPatientIdentifier(person.getPersonIdentifiers()));
		//Patient Name
		ret.setPatientName(toPatientName(person.getNames()));
		//PhoneNumbers
		ret.setPhoneNumbers(toPhoneNumbers(person.getTelephoneNumbers()));
		//Language
		ret.setPrimaryLanguage(person.getPrimaryLanguage());
		//Race
		ret.setRace(toRace(person.getRaces()));
		//Religion
		ret.setReligion(toReligion(person.getReligions()));
		//SSN
		ret.setSsn(toSSN(person.getSocialSecurityNumbers()));
		// OpenEMPI does not have Patient Account Number
		// OpenEMPI does not have Visit
		return ret;
	}
	
	/**
	 * Converts from <code>Race</code> used by OpenEMPI to
	 * a <code>Race</code> used by OpenPIXPDQ.
	 * 
	 * @param races 
	 * @return 
	 */
	private String toRace(List races) {
		if (races==null ||races.size()<=0)
			return null;
		
		org.openempi.data.Race from = (org.openempi.data.Race)races.get(0);
		return from.getValue();
	}
	/**
	 * Converts from <code>EthnicGroup</code> used by OpenEMPI to
	 * a <code>EthnicGroup</code> used by OpenPIXPDQ.
	 * 
	 * @param ethnicgroups the list of <code>EthnicGroup</code> objects to be converted from 
	 * @return 
	 */
	private String toEthnicGroup(List ethnicgroups) {
		if (ethnicgroups==null ||ethnicgroups.size()<=0)
			return null;
		
		org.openempi.data.EthnicGroup from = (org.openempi.data.EthnicGroup)ethnicgroups.get(0);
		return from.getValue();
	}
	/**
	 * Converts from <code>MaritalStatus</code> used by OpenEMPI to
	 * a <code>MaritalStatus</code> used by OpenPIXPDQ.
	 * 
	 * @param maritalsatus the list of <code>Religion</code> objects to be converted from
	 * @return 
	 */
	private String toMaritalStatus(List maritalsatus) {
		if (maritalsatus==null ||maritalsatus.size()<=0)
			return null;
		
		org.openempi.data.MaritalStatus from = (org.openempi.data.MaritalStatus)maritalsatus.get(0);
		return from.getValue();
	}
	/**
	 * Converts from <code>TelephoneNumber</code> used by OpenEMPI to
	 * a <code>PhoneNumber</code> used by OpenPIXPDQ.
	 * 
	 * @param phone the list of <code>TelephoneNumber</code> objects to be converted from
	 * @return a list of <code>PhoneNumber</code> objects.
	 */
	private List<PhoneNumber> toPhoneNumbers(List phone) {
		List<PhoneNumber> ret = new ArrayList<PhoneNumber>();
		if (phone==null ||phone.size()<=0)
			return ret;

	    for ( int i=0; i<phone.size(); i++) {
			TelephoneNumber from = (TelephoneNumber)phone.get(i);
			PhoneNumber to = toPhoneNumber(from);			
			ret.add(to);
		}
		return ret;
	}
	/**
	 * Converts from <code>Religion</code> used by OpenEMPI to
	 * a <code>Religion</code> used by OpenPIXPDQ.
	 * 
	 * @param religion the list of <code>Religion</code> objects to be converted from
	 * @return 
	 */
	private String toReligion(List religion) {
		if (religion==null ||religion.size()<=0)
			return null;
		
		org.openempi.data.Religion from = (org.openempi.data.Religion)religion.get(0);
		return from.getValue();
	}
	/**
	 * Converts from <code>PersonIdentifier</code> used by OpenEMPI to
	 * a <code>PatientIdentifier</code> used by OpenPIXPDQ.
	 * 
	 * @param ids the list of <code>PersonIdentifier</code> objects to be converted from
	 * @return a list of <code>PatientIdentifier</code> objects.
	 */
	private List<PatientIdentifier> toPatientIdentifier(List ids) {
		List<PatientIdentifier> ret = new ArrayList<PatientIdentifier>();
		if (ids == null) return ret;

		for ( int i=0; i<ids.size(); i++) {
			PersonIdentifier from = (PersonIdentifier)ids.get(i);
			PatientIdentifier to = toPatientIdentifier(from);			
			ret.add(to);
		}
		return ret;
	}
	/**
	 * Converts from <code>PersonName</code> used by OpenEMPI to
	 * a <code>PersonName</code> used by OpenPIXPDQ.
	 * 
	 * @param from the <code>PersonName</code> object to be converted from
	 * @return a <code>PersonName</code>.
	 */
	private PersonName toPersonName(org.openempi.data.PersonName from){
		if (from == null) return null;
		
		if (from.getFirstName()==null && from.getLastName()==null && 
				from.getSecondName()==null) 
			return null;
		
		PersonName ret = new PersonName();
		ret.setFirstName(from.getFirstName());
		ret.setLastName(from.getLastName());
		ret.setSecondName(from.getSecondName());
		ret.setPrefix(from.getPrefix());
		ret.setSuffix(from.getSuffix());
		ret.setDegree(from.getDegree());
		ret.setNameTypeCode(from.getNameTypeCode());
		ret.setNameRepresentationCode(from.getNameRepresentationCode());
		return ret;
	}
	/**
	 * Converts from <code>TelephoneNumber</code> used by OpenEMPI to
	 * a <code>PhoneNumber</code> used by OpenPIXPDQ.
	 * 
	 * @param from the <code>TelephoneNumber</code> object to be converted from
	 * @return a <code>PhoneNumber</code>.
	 */
	private PhoneNumber toPhoneNumber(TelephoneNumber from) {
		if (from == null) return null;
		
		PhoneNumber ret = new PhoneNumber();
		ret.setNumber(from.getPhoneNumber());
		ret.setAreaCode(from.getAreaCode());
		ret.setCountryCode(from.getCountryCode());
		ret.setExtension(from.getExtension());
		return ret;
	}
	/**
	 * Converts from <code>PersonName</code> used by OpenEMPI to
	 * a <code>PersonName</code> used by OpenPIXPDQ.
	 * 
	 * @param names the list of <code>PersonName</code> objects to be converted from
	 * @return a <code>PersonName</code>.
	 */
	private PersonName toPatientName(List names) {
		if (names == null || names.size() <= 0)
			return null;
		
		org.openempi.data.PersonName from = (org.openempi.data.PersonName)names.get(0);
		PersonName ret = new PersonName();
		ret.setFirstName(from.getFirstName());
		ret.setLastName(from.getLastName());
		ret.setSecondName(from.getSecondName());
		ret.setPrefix(from.getPrefix());
		ret.setSuffix(from.getSuffix());
		ret.setDegree(from.getDegree());
		ret.setNameTypeCode(from.getNameTypeCode());
		ret.setNameRepresentationCode(from.getNameRepresentationCode());
		return ret;
	}
	/**
	 * Converts from <code>SocialSecurityNumber</code> used by OpenEMPI to
	 * a SSN String used by OpenPIXPDQ.
	 * 
	 * @param ssn the list of <code>SocialSecurityNumber</code> objects to be converted from
	 * @return a SSN String value.
	 */
	private String toSSN(List ssn) {
		if (ssn == null || ssn.size() <= 0)
			return null;
		
		SocialSecurityNumber from = (SocialSecurityNumber)ssn.get(0);
		return from.getSSN();
	}
	/**
	 * Converts from <code>DriversLicense</code> used by OpenEMPI to
	 * a <code>DriversLicense</code> used by OpenPIXPDQ.
	 * 
	 * @param licenses the list of <code>DriversLicense</code> objects to be converted from
	 * @return a <code>DriversLicense</code>.
	 */
	private DriversLicense toDriversLicense(List licenses) {
		if (licenses == null ||licenses.size() <= 0) 
			return null;
		
			org.openempi.data.DriversLicense from = (org.openempi.data.DriversLicense)licenses.get(0);
			DriversLicense ret = new DriversLicense();
			ret.setIssuingState(from.getState());
			ret.setLicenseNumber(from.getNumber());
			return ret;
	}
	/**
	 * Converts from <code>DateOfBirth</code> used by OpenEMPI to
	 * a <code>BirthDate</code> used by OpenPIXPDQ.
	 * 
	 * @param dobs the <code>Gender</code> to be converted from
	 * @return a <code>Calendar</code> 
	 */
	private Calendar toBirthDate(List dobs) {
		if (dobs == null || dobs.size() <= 0 )
			return null;
		
		DateOfBirth dob = (DateOfBirth)dobs.get(0);
		Calendar cal = GregorianCalendar.getInstance();
	    cal.setTime(dob.getDOB());
	    return cal;
	}
	/**
	 * Converts from <code>Gender</code> used by OpenEMPI to
	 * a <code>AdministrativeSex</code> used by OpenPIXPDQ.
	 * 
	 * @param genders the <code>Gender</code> to be converted from
	 * @return a <code>SexType</code> of SharedEnums 
	 */
	private SharedEnums.SexType toSex(List genders) {
	    if (genders.size() >= 1) {
	    	Gender gender = (Gender)genders.get(0);
			return SharedEnums.SexType.getByString(gender.getValue()); 
		} 
	    return null;
	}
	/**
	 * Converts from <code>Address</code> list used by OpenEMPI to
	 * the <code>Address</code> list used by OpenPIXPDQ.
	 * 
	 * @param addresses the list of <code>Address</code> to be converted from
	 * @return the list of <code>Address</code> objects 
	 */
	private List<Address> toAddress(List addresses) {
		List<Address> ret = new ArrayList<Address>();
		if (addresses == null || addresses.size() <= 0) return ret;
		
//		for ( int i=0; i<addresses.size(); i++) {
			org.openempi.data.Address from = (org.openempi.data.Address)addresses.get(0);
			Address to = new Address();
			to.setAddLine1(from.getAddress1());
			to.setAddLine2(from.getAddress2());
			to.setAddCity(from.getCity());
			to.setAddState(from.getState());
			to.setAddCountry(from.getCountry());
			to.setAddZip(from.getZipCode());
			to.setAddType(SharedEnums.AddressType.hl7ValueOf(from.getAddressType()));
			ret.add(to);
//		}
		return ret;
	}
	
	/**
	 * Converts from <code>PersonIdentifier</code> used by OpenEMPI to
	 * a <code>PatientIdentifier</code> used by OpenPIXPDQ.
	 * 
	 * @param from the <code>PersonIdentifier</code> to be converted from
	 * @return a <code>PatientIdentifier</code> 
	 */
	private PatientIdentifier toPatientIdentifier(PersonIdentifier from) {
		if (from == null) return null;
 
		PatientIdentifier ret = new PatientIdentifier();
		ret.setId(from.getId());
		ret.setAssigningAuthority(toIdentifier(from.getAssigningAuthority()));
		ret.setAssigningFacility(toIdentifier(from.getAssigningFacility()));
		ret.setIdentifierTypeCode(from.getIdentifierTypeCode());
		return ret;
	}
	/**
	 * Converts from a <code>DomainIdentifier</code> used by OpenEMPI to an
	 * <code>Identifier</code> used by OpenPIXPDQ
	 * 
	 * @param from the <code>DomainIdentifier</code> object to be converted from
	 * @return an <code>Identifier</code>
	 */
	private Identifier toIdentifier(DomainIdentifier from) {
		Identifier ret = new Identifier(from.getNameSpaceID(), 
				from.getUniversalID(), from.getUniversalIDType());
		return ret;
	}

	/**
	 * Converts from <code>PdqQuery</code> used by OpenPIXPDQ to
	 * a <code>Patient</code> used by OpenEMPI.
	 * 
	 * @param query the <code>PdqQuery</code> object to be converted from
	 * @param mh the <code>MessageHeader</code> object to be converted from
	 * @return a <code>Person</code> 
	 */
	private Person getPerson(PdqQuery query, MessageHeader mh) {

		DocumentHeader dh = new DocumentHeader();
		dh.setMessageType(mh.getMessageCode());
		dh.setEventCode(mh.getTriggerEvent());
		if (mh.getMessgeDate() != null) 
			dh.setMessageDate(mh.getMessgeDate().getTime());
		else 
			dh.setMessageDate(new Date());
		Person person = new Person();
		person.addDocumentHeader(dh);
		if (query.getPersonName() != null) {
			person.addName(getPersonName(dh, query));
		}
		if (query.getSsn() != null ) {
			person.addSocialSecurityNumber(new SocialSecurityNumber(handleSsn(query.getSsn())));
		}
		if (query.getSex() != null) {
			person.addGender(new Gender(query.getSex().getCDAValue()));
		}
		if (query.getBirthDate() != null) {
			person.addDateOfBirth(new DateOfBirth(query.getBirthDate()
					.getTime()));
		}
		if (query.getDriversLicense() != null) {
			person.addDriversLicense(getDriversLicence(dh, query));
		}
		if (query.getAddress() != null ) {
			person.addAddress(getAddress(dh, query.getAddress()));
		}
		if(query.getPhone() != null) { 
			person.addTelephoneNumber(getPhoneNumber(dh, query.getPhone()));
		}
		if (query.getPatientIdentifier() != null) {
			person.addPersonIdentifier(getPid(dh, query));
		}
        if (query.getPatientAccountNumber() != null) {
            person.addAccountNumber(getPan(dh, query));
        }
		return person;
	}

	private String handleSsn(String ssn) {
		//OpenEMPI only takes 9 digit ssn. Need to remove "-". 
		if (ssn == null) return null;
		
		String ret = ssn; 
		if (ssn.length() > 9) {
			ret = ssn.replaceAll("-", "");
		}
		return ret;
	}

	private org.openempi.data.PersonName getPersonName(DocumentHeader dh,
			PdqQuery query) {

		PersonName from = query.getPersonName();
		if (from == null) return null;
		
		org.openempi.data.PersonName to = new org.openempi.data.PersonName(
				dh, handlePrefixSuffix(query, from.getLastName()), 
				    handlePrefixSuffix(query, from.getFirstName()),
				    handlePrefixSuffix(query, from.getSecondName()));
		to.setPrefix(from.getPrefix());
		to.setSuffix(from.getSuffix());
		to.setDegree(from.getDegree());
		to.setNameTypeCode(from.getNameTypeCode());
		to.setNameRepresentationCode(from.getNameRepresentationCode());

		return to;
	}
	
	/**
	 * Replaces prefix and suffix from the OpenPIXPDQ with the default prefix/suffix
	 * character used by OpenEMPI (namely, %). 
	 * 
	 * @param query
	 * @param from
	 * @return
	 */
	private String handlePrefixSuffix(PdqQuery query, String from) {
		if (from == null) return null;
		
		String ret = from;
		String prefix = query.getPrefix();
		String suffix = query.getSuffix();
		
		if (prefix != null) {
			if (from.startsWith(prefix)) {
				ret = "%" + ret.substring(prefix.length());
			}
		}
		if (suffix != null) {
			if (from.endsWith(suffix)) {
				ret = ret.substring(0, ret.length()-suffix.length()) + "%";
			}
		}
		return ret;
	}

	private PersonIdentifier getPid(DocumentHeader dh, PdqQuery query) {
		PatientIdentifier pid = query.getPatientIdentifier();
		if (pid == null) 
			return null;
		if (pid.getId()==null && pid.getAssigningAuthority()==null && 
				pid.getAssigningFacility()==null)
			return null;
		
		DomainIdentifier assigningAuthority = getDomainId(dh, query, pid.getAssigningAuthority());

		//OpenEMPI requires assigning facility which may not be available from a PdqQuery
		//so use assigning authority as a workaround.
		DomainIdentifier assigningFacility = isValidIdentifier(pid.getAssigningFacility()) ? 
										     getDomainId(dh, query, pid.getAssigningAuthority()) : assigningAuthority;
		
		PersonIdentifier personid = new PersonIdentifier(dh, handlePrefixSuffix(query,pid.getId()),
				assigningAuthority, assigningFacility, pid.getIdentifierTypeCode());
		if (pid.getEffectiveDate() != null) {
			personid.setEffectiveDate(pid.getEffectiveDate().getTime());
		}
		if (pid.getExpirationDate() != null) {
			personid.setExpirationDate(pid.getExpirationDate().getTime());
		}
		return personid;
	}

    private PersonIdentifier getPan(DocumentHeader dh, PdqQuery query) {
        PatientIdentifier pan = query.getPatientAccountNumber();
        if (pan == null)
            return null;
        if (pan.getId()==null && pan.getAssigningAuthority()==null &&
                pan.getAssigningFacility()==null)
            return null;

        DomainIdentifier assigningAuthority = getDomainId(dh, query, pan.getAssigningAuthority());

        //OpenEMPI requires assigning facility which may not be available from a PdqQuery
        //so use assigning authority as a workaround.
        DomainIdentifier assigningFacility = isValidIdentifier(pan.getAssigningFacility()) ?
                getDomainId(dh, query, pan.getAssigningAuthority()) : assigningAuthority;

        PersonIdentifier personid = new PersonIdentifier(dh, handlePrefixSuffix(query,pan.getId()),
                assigningAuthority, assigningFacility, pan.getIdentifierTypeCode());
        if (pan.getEffectiveDate() != null) {
            personid.setEffectiveDate(pan.getEffectiveDate().getTime());
        }
        if (pan.getExpirationDate() != null) {
            personid.setExpirationDate(pan.getExpirationDate().getTime());
        }
        return personid;
    }

	private DomainIdentifier getDomainId(DocumentHeader dh, PdqQuery query, Identifier id) {
		if (id == null) {
			//Should return null, but OpenEMPI currently expects an empty Did.
			return new DomainIdentifier(dh, null, null, null);
		}
		
		DomainIdentifier did = new DomainIdentifier(dh, 
				handlePrefixSuffix(query, id.getNamespaceId()), 
				handlePrefixSuffix(query, id.getUniversalId()),
				id.getUniversalIdType());
		return did;
	}
	
	private boolean isValidIdentifier(Identifier id) {
		if (id == null || id.getNamespaceId() == null)
			return false;
		else
			return true;
	}

	private org.openempi.data.DriversLicense getDriversLicence(DocumentHeader dh, PdqQuery query) {

		if (query.getDriversLicense() == null) return null;
		
		Date date = null;
		if (query.getDriversLicense().getExpirationDate() != null) {
			date = query.getDriversLicense().getExpirationDate().getTime();
		}
		org.openempi.data.DriversLicense license = new org.openempi.data.DriversLicense(dh, query.getDriversLicense()
				.getLicenseNumber(), query.getDriversLicense()
				.getIssuingState(), date);
		
		return license;
	}

	private org.openempi.data.Address getAddress(DocumentHeader dh,
			Address address) {
		if (address==null) return null;
		
		org.openempi.data.Address add = new org.openempi.data.Address(dh,
				address.getAddLine1(), address.getAddLine2(), address
						.getAddCity(), address.getAddState(), address
						.getAddZip());

		if (address.getAddType() != null) {
			add.setAddressType(address.getAddType().getHL7Value());
		}
		add.setCountry(address.getAddCountry());

		return add;
	}

	private TelephoneNumber getPhoneNumber(DocumentHeader dh, PhoneNumber phone) {
		if (phone == null) return null;
		
		TelephoneNumber tpn = new TelephoneNumber(dh, phone.getAreaCode(),phone.getNumber());

		tpn.setCountryCode(phone.getCountryCode());
		tpn.setExtension(phone.getExtension());
		if(phone.getType() != null) {
			tpn.setTelecomUseCode(phone.getType().getCDAValue());
		}

		return tpn;
	}

	// Identity Service
	private IdentityService getIdentityService() {
		try {
			/*
			if (identityService == null) {
				identityService = getIdentityServiceHome().create();
			}*/
			identityService = new IdentityServiceImpl();
		} catch (Exception e) {
			 log.error("Failed while attempting to retrieve reference to the IdentityService. Error: " + e);
			 e.printStackTrace();
			throw new RuntimeException("Failed while obtaining reference to the IdentityService.");
		}
		return identityService;
	}

	private IdentityServiceHome getIdentityServiceHome() throws NamingException {
		if (identityServiceHome == null) {
			identityServiceHome = getIdentityHome();
		}
		return identityServiceHome;
	}

	/**
	 * Get the home interface
	 */
	protected IdentityServiceHome getIdentityHome() throws NamingException {
		Context ctx = this.getInitialContext();
		Object o = ctx.lookup("ejb/IdentityService");
		IdentityServiceHome intf = (IdentityServiceHome) PortableRemoteObject
				.narrow(o, IdentityServiceHome.class);
		return intf;
	}

	/**
	 * Get the initial naming context
	 */
	protected Context getInitialContext() throws NamingException {
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		props.put(Context.URL_PKG_PREFIXES,
				"org.jboss.naming:org.jnp.interfaces");
		props.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		Context ctx = new InitialContext(props);
		return ctx;
	}

	
}
