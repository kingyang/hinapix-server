package org.openempi.ics.loader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openempi.data.Address;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DocumentHeader;
import org.openempi.data.DomainIdentifier;
import org.openempi.data.Gender;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.PersonName;
import org.openempi.data.SocialSecurityNumber;
import org.openempi.data.TelephoneNumber;
import org.apache.commons.lang.builder.ToStringBuilder;

public class NominalSetFileLoader extends AbstractFileLoader
{
	private final static int MAX_FIELD_COUNT = 14;
	private long id = 100000;
	private long ssn = 555555555;
	
	protected Person processLine(String line, int lineIndex) {
		// Skip the first line since its a header.
		if (lineIndex == 0) {
			return null;
		}
		log.debug("Needs to parse the line " + line);
		try {
			PersonData cdcPerson = getPerson(line);
			Person person = createPerson("Nominal", "Nominal", new Long(id++).toString(), cdcPerson.getAddressOne(),
					cdcPerson.getAddressTwo(), cdcPerson.getCity(), cdcPerson.getState(), cdcPerson.getZip(),
					cdcPerson.getLastName(), cdcPerson.getFirstName(), "",
					cdcPerson.getGender(), new Long(ssn++).toString(),
					cdcPerson.getDob(), cdcPerson.getAreaCode(), cdcPerson.getPhoneNum());
			return person;
		} catch (ParseException e) {
			log.warn("Failed to parse file line: " + line + " due to " + e);
			return null;
		}
	}

	/**
LNAME,FNAME,SECNAME,ADDRESS_1,ADDRESS_2,CITY,STATE_PROV,ZIP,COUNTRY,PHONE_AREA_CD,PHONE_NUM,GENDER,SSN,DOB
	 */
	private PersonData getPerson(String line) throws ParseException {
		String[] fields = new String[MAX_FIELD_COUNT];
		int length = line.length();
		int begin=0;
		int end=0;
		int fieldIndex=0;
		while (end < length) {
			while (end < length-1 && line.charAt(end) != ',') {
				end++;
			}
			if (end == length -1 ) {
				break;
			}
			fields[fieldIndex++] = line.substring(begin+1, end-1);
			end++;
			begin=end;
		}
		fields[fieldIndex] = line.substring(begin+1, end);
		PersonData person = new PersonData();
		person.setLastName(fields[0]);
		person.setFirstName(fields[1]);
		person.setAddressOne(fields[3]);
		person.setAddressTwo(fields[4]);
		person.setCity(fields[5]);
		person.setState(fields[6]);
		person.setZip(fields[7]);
		person.setCountry(fields[8]);
		person.setAreaCode(fields[9]);
		person.setPhoneNum(fields[10]);
		person.setGender(fields[11]);
		person.setSsn(fields[12]);
		String dob = fields[13];
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		Date dobDate = format.parse(dob);
		person.setDob(dobDate);
		return person;
	}

	private PersonIdentifier createPersonIdentifier(DocumentHeader dh,
			String domain, String facility, String pid) {
		return new PersonIdentifier(dh, pid, new DomainIdentifier(dh, domain,
				domain, "L"),
				new DomainIdentifier(dh, facility, facility, "L"), null);
	}

	private Person createPerson(String domain, String facility, String pid, String address1, String address2, String city,
			String state, String zip, String lname, String fname, String suffix, String gender, String ssn, Date dob,
			String areaCode, String phoneNumber) {
		Person person = new Person();
		DocumentHeader dh = new DocumentHeader();
		dh.setMessageDate(new Date(System.currentTimeMillis()));
		dh.setSendingFacility(domain);

		if (lname != null && lname.length() > 0)
			person.addName(new PersonName(dh, lname.toUpperCase(), fname
					.toUpperCase(), null));
		if (gender != null && gender.length() > 0)
			person.addGender(new Gender(dh, gender));
		if (dob != null)
			person.addDateOfBirth(new DateOfBirth(dh, dob));
		if (ssn != null && ssn.length() > 0)
			person.addSocialSecurityNumber(new SocialSecurityNumber(dh, ssn));
		person.addPersonIdentifier(createPersonIdentifier(dh, domain, facility,
				pid));
		Address address = new Address(dh, address1, address2, city, state, zip);
		person.addAddress(address);
		TelephoneNumber number = new TelephoneNumber(dh, areaCode, phoneNumber);
		person.addTelephoneNumber(number);
		person.addDocumentHeader(dh);
		return person;
	}
	
	/**
	LNAME,FNAME,SECNAME,ADDRESS_1,ADDRESS_2,CITY,STATE_PROV,ZIP,COUNTRY,PHONE_AREA_CD,PHONE_NUM,GENDER,SSN,DOB
		 */	
	private class PersonData
	{
		private String firstName;
		private String lastName;
		private String addressOne;
		private String addressTwo;
		private String city;
		private String state;
		private String zip;
		private String country;
		private String areaCode;
		private String phoneNum;
		private Date dob;
		private String gender;
		private String ssn;
		
		public Date getDob() {
			return dob;
		}
		public void setDob(Date dob) {
			this.dob = dob;
		}
		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getGender() {
			return gender;
		}
		public void setGender(String gender) {
			this.gender = gender;
		}
		public String getLastName() {
			return lastName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	
		public int hashCode() {
			final int PRIME = 31;
			int result = super.hashCode();
			result = PRIME * result + ((addressOne == null) ? 0 : addressOne.hashCode());
			result = PRIME * result + ((addressTwo == null) ? 0 : addressTwo.hashCode());
			result = PRIME * result + ((areaCode == null) ? 0 : areaCode.hashCode());
			result = PRIME * result + ((city == null) ? 0 : city.hashCode());
			result = PRIME * result + ((country == null) ? 0 : country.hashCode());
			result = PRIME * result + ((dob == null) ? 0 : dob.hashCode());
			result = PRIME * result + ((firstName == null) ? 0 : firstName.hashCode());
			result = PRIME * result + ((gender == null) ? 0 : gender.hashCode());
			result = PRIME * result + ((lastName == null) ? 0 : lastName.hashCode());
			result = PRIME * result + ((phoneNum == null) ? 0 : phoneNum.hashCode());
			result = PRIME * result + ((state == null) ? 0 : state.hashCode());
			result = PRIME * result + ((zip == null) ? 0 : zip.hashCode());
			return result;
		}
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			final PersonData other = (PersonData) obj;
			if (addressOne == null) {
				if (other.addressOne != null)
					return false;
			} else if (!addressOne.equals(other.addressOne))
				return false;
			if (addressTwo == null) {
				if (other.addressTwo != null)
					return false;
			} else if (!addressTwo.equals(other.addressTwo))
				return false;
			if (areaCode == null) {
				if (other.areaCode != null)
					return false;
			} else if (!areaCode.equals(other.areaCode))
				return false;
			if (city == null) {
				if (other.city != null)
					return false;
			} else if (!city.equals(other.city))
				return false;
			if (country == null) {
				if (other.country != null)
					return false;
			} else if (!country.equals(other.country))
				return false;
			if (dob == null) {
				if (other.dob != null)
					return false;
			} else if (!dob.equals(other.dob))
				return false;
			if (firstName == null) {
				if (other.firstName != null)
					return false;
			} else if (!firstName.equals(other.firstName))
				return false;
			if (gender == null) {
				if (other.gender != null)
					return false;
			} else if (!gender.equals(other.gender))
				return false;
			if (lastName == null) {
				if (other.lastName != null)
					return false;
			} else if (!lastName.equals(other.lastName))
				return false;
			if (phoneNum == null) {
				if (other.phoneNum != null)
					return false;
			} else if (!phoneNum.equals(other.phoneNum))
				return false;
			if (state == null) {
				if (other.state != null)
					return false;
			} else if (!state.equals(other.state))
				return false;
			if (zip == null) {
				if (other.zip != null)
					return false;
			} else if (!zip.equals(other.zip))
				return false;
			return true;
		}
		public String getAddressOne() {
			return addressOne;
		}
		public void setAddressOne(String addressOne) {
			this.addressOne = addressOne;
		}
		public String getAddressTwo() {
			return addressTwo;
		}
		public void setAddressTwo(String addressTwo) {
			this.addressTwo = addressTwo;
		}
		public String getAreaCode() {
			return areaCode;
		}
		public void setAreaCode(String areaCode) {
			this.areaCode = areaCode;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getCountry() {
			return country;
		}
		public void setCountry(String country) {
			this.country = country;
		}
		public String getPhoneNum() {
			return phoneNum;
		}
		public void setPhoneNum(String phoneNum) {
			this.phoneNum = phoneNum;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public String getZip() {
			return zip;
		}
		public void setZip(String zip) {
			this.zip = zip;
		}
		public String getSsn() {
			return ssn;
		}
		public void setSsn(String ssn) {
			this.ssn = ssn;
		}
		public String toString() {
			return new ToStringBuilder(this).append("firstName", firstName)
					.append("lastName", lastName).append("addressOne",
							addressOne).append("addressTwo", addressTwo)
					.append("city", city).append("state", state).append("zip",
							zip).append("country", country).append("areaCode",
							areaCode).append("phoneNum", phoneNum).append(
							"dob", dob).append("gender", gender).append("ssn",
							ssn).toString();
		}
	}
}
