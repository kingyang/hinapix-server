package org.openempi.ics.loader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DocumentHeader;
import org.openempi.data.DomainIdentifier;
import org.openempi.data.Gender;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.PersonName;
import org.openempi.data.SocialSecurityNumber;
import org.apache.commons.lang.builder.ToStringBuilder;

public class CDCFileLoader extends AbstractFileLoader
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
			CDCPersonData cdcPerson = getCDCPerson(line);
			Person person = createPerson("CDC", "CDC", new Long(id++).toString(), 
					cdcPerson.getLastName(), cdcPerson.getFirstName(), cdcPerson.getSuffix(),
					cdcPerson.getGender(), new Long(ssn++).toString(),
					cdcPerson.getDob());
			return person;
		} catch (ParseException e) {
			log.warn("Failed to parse file line: " + line + " due to " + e);
			return null;
		}
	}

	private CDCPersonData getCDCPerson(String line) throws ParseException {
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
			fields[fieldIndex++] = line.substring(begin, end);
			end++;
			begin=end;
		}
		CDCPersonData person = new CDCPersonData();
		person.setLastName(fields[0]);
		person.setFirstName(fields[1]);
		person.setMiddleName(fields[2]);
		person.setSuffix(fields[3]);
		String dob = fields[4];
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date dobDate = format.parse(dob);
		person.setDob(dobDate);
		person.setGender(fields[5]);
		return person;
	}

	private PersonIdentifier createPersonIdentifier(DocumentHeader dh,
			String domain, String facility, String pid) {
		return new PersonIdentifier(dh, pid, new DomainIdentifier(dh, domain,
				domain, "L"),
				new DomainIdentifier(dh, facility, facility, "L"), null);
	}

	private Person createPerson(String domain, String facility, String pid,
			String lname, String fname, String suffix, String gender, String ssn, Date dob) {
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

		person.addDocumentHeader(dh);
		return person;
	}
	
	private class CDCPersonData
	{
		private String firstName;
		private String lastName;
		private String middleName;
		private String suffix;
		private Date dob;
		private String gender;
		
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
		public String getMiddleName() {
			return middleName;
		}
		public void setMiddleName(String middleName) {
			this.middleName = middleName;
		}
		
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + ((dob == null) ? 0 : dob.hashCode());
			result = PRIME * result + ((firstName == null) ? 0 : firstName.hashCode());
			result = PRIME * result + ((gender == null) ? 0 : gender.hashCode());
			result = PRIME * result + ((lastName == null) ? 0 : lastName.hashCode());
			result = PRIME * result + ((middleName == null) ? 0 : middleName.hashCode());
			return result;
		}
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final CDCPersonData other = (CDCPersonData) obj;
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
			if (middleName == null) {
				if (other.middleName != null)
					return false;
			} else if (!middleName.equals(other.middleName))
				return false;
			return true;
		}
		public String getSuffix() {
			return suffix;
		}
		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}
		public String toString() {
			return new ToStringBuilder(this).append("firstName", firstName)
					.append("lastName", lastName).append("middleName",
							middleName).append("suffix", suffix).append("dob",
							dob).append("gender", gender).toString();
		}
	}
}
