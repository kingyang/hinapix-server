/*
 * Title      : PersonName
 * Description:
 * Copyright  : (c) 1998-2002
 * Company    : CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

import java.util.Date;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The <code>PersonName</code> transient object. Each transient object is
 * mirrored by a persistent object in Objectivity.
 * 
 * @author CareScience
 * @version 1.3, 20030131
 */
public class PersonName extends TransientObject {

	private String lastName_;

	private String firstName_;

	private String secondName_;

	private String suffix_;

	private String prefix_;

	private String degree_;

	private String nameTypeCode_;

	private String nameTypeRepresentationCode_;

	private long startDate_ = -1;

	private long endDate_ = -1;

	private boolean alias_;

	/**
	 * No-arg constructor
	 * 
	 * @deprecated
	 */
	public PersonName() {
		this(null);
	}

	/**
	 * Overloaded constructor
	 * 
	 * @param <code>Document</code>
	 * @deprecated
	 */
	public PersonName(DocumentHeader documentHeader) {
		this(documentHeader, null, null, null);
	}

	/**
	 * Overloaded constructor
	 * 
	 * @param lastName
	 * @param firstName
	 * @param secondName
	 * @deprecated
	 */
	public PersonName(String lastName, String firstName, String secondName) {
		this(new DocumentHeader(), lastName, firstName, secondName);
	}

	/**
	 * Overloaded constructor
	 * 
	 * @param documentHeader
	 * @param lastName
	 * @param firstName
	 * @param secondName
	 */
	public PersonName(DocumentHeader documentHeader, String lastName,
			String firstName, String secondName) {
		setDocumentHeader(documentHeader);
		setLastName(lastName);
		setFirstName(firstName);
		setSecondName(secondName);
	}

	public String getSearchKey() {
		/* Lazy evaluation of getSearchKey() */
		return SearchKeyGenerator.getInstance().generateKey(getCleanName());
	}

	public String getLastName() {
		return lastName_;
	}

	public void setLastName(String newLastName) {
		lastName_ = newLastName;
	}

	public void setFirstName(String firstName) {
		firstName_ = firstName;
	}

	public boolean isAlias() {
		return alias_;
	}

	public void setAlias(boolean value) {
		alias_ = value;
	}

	public String getFirstName() {
		return firstName_;
	}

	public void setSecondName(String newSecondName) {
		secondName_ = newSecondName;
	}

	public String getSecondName() {
		return secondName_;
	}

	public void setSuffix(String newSuffix) {
		suffix_ = newSuffix;
	}

	public String getSuffix() {
		return suffix_;
	}

	public void setPrefix(String newPrefix) {
		prefix_ = newPrefix;
	}

	public String getPrefix() {
		return prefix_;
	}

	public void setDegree(String newDegree) {
		degree_ = newDegree;
	}

	public String getDegree() {
		return degree_;
	}

	public void setNameTypeCode(String newNameTypeCode) {
		nameTypeCode_ = newNameTypeCode;
	}

	public String getNameTypeCode() {
		return nameTypeCode_;
	}

	public void setNameRepresentationCode(String newNameRepresentationCode) {
		nameTypeRepresentationCode_ = newNameRepresentationCode;
	}

	public String getNameRepresentationCode() {
		return nameTypeRepresentationCode_;
	}

	public void setStartDate(Date date) {
		startDate_ = (date == null) ? -1 : date.getTime();
	}

	public Date getStartDate() {
		return (startDate_ == -1) ? null : new Date(startDate_);
	}

	/**
	 * Method used to set the end date of the person name validity
	 * 
	 * @param String
	 *            the end date
	 */
	public void setEndDate(Date date) {
		endDate_ = (date == null) ? -1 : date.getTime();
	}

	public Date getEndDate() {
		return (endDate_ == -1) ? null : new Date(endDate_);
	}

	public void isValid() {
		// At least one of these must not be null.
		if (firstName_ == null && lastName_ == null)
			throw new IllegalArgumentException(
					"Both the first and last names may not be null");
	}

	public boolean equals(Object obj) {
		boolean isEqual = false;

		if (obj != null) {
			if (obj == this) {
				isEqual = true;
			} else if (getClass() == obj.getClass()) {
				PersonName person = (PersonName) obj;
				isEqual = true;

				if ((lastName_ == null) ? (person.lastName_ != null)
						: (!lastName_.equalsIgnoreCase(person.lastName_))) {

					isEqual = false;
				} else if ((firstName_ == null) ? (person.firstName_ != null)
						: (!firstName_.equalsIgnoreCase(person.firstName_))) {
					isEqual = false;
				} else if ((secondName_ == null) ? (person.secondName_ != null)
						: (!secondName_.equalsIgnoreCase(person.secondName_))) {
					isEqual = false;
				} else if ((suffix_ == null) ? (person.suffix_ != null)
						: (!suffix_.equalsIgnoreCase(person.suffix_))) {
					isEqual = false;
				} else if ((prefix_ == null) ? (person.prefix_ != null)
						: (!prefix_.equalsIgnoreCase(person.prefix_))) {
					isEqual = false;
				} else if ((degree_ == null) ? (person.degree_ != null)
						: (!degree_.equalsIgnoreCase(person.degree_))) {
					isEqual = false;
				} else if ((nameTypeCode_ == null) ? (person.nameTypeCode_ != null)
						: (!nameTypeCode_
								.equalsIgnoreCase(person.nameTypeCode_))) {
					isEqual = false;
				} else if ((nameTypeRepresentationCode_ == null) ? (person.nameTypeRepresentationCode_ != null)
						: (!nameTypeRepresentationCode_
								.equalsIgnoreCase(person.nameTypeRepresentationCode_))) {
					isEqual = false;
				} else if (startDate_ != person.startDate_) {
					isEqual = false;
				} else if (endDate_ != person.endDate_) {
					isEqual = false;
				}
			}
		}
		return isEqual;
	}

	/**
	 * Another version of getName() except this strips the first name of
	 * anything beyond the first word. Intended to be used by SearchKeyGenerator
	 * to generate keys based on first + last name while stripping out
	 * occasional extra crap found in the first name field (middle initial,
	 * etc).
	 */
	public String getCleanName() {
		String lastName = getLastName();
		String firstName = getFirstName();
		String name = null;

		if (lastName != null) {
			if (firstName == null)
				name = lastName;
			else {
				int idx = firstName.indexOf(' ');
				if (idx != -1)
					firstName = firstName.substring(0, idx);
				name = firstName + " " + lastName;
			}
		} else if (firstName != null) {
			name = firstName;
		}
		return name;
	}

	/**
	 * Returns a String composed of the first and last names separated by a
	 * space.
	 * 
	 * @return a String composed of the first and last names separated by a
	 *         space.
	 */
	public String getName() {
		String name = null;

		if (getLastName() != null) {
			String lastName = getLastName();
			String firstName = getFirstName();

			if (firstName == null) {
				name = lastName;
			} else {
				name = firstName + " " + lastName;
			}
		}

		return name;
	}

	public int hashCode() {
		int code = 0;

		if (lastName_ != null)
			code += lastName_.hashCode();
		if (firstName_ != null)
			code += firstName_.hashCode();
		if (secondName_ != null)
			code += secondName_.hashCode();
		if (suffix_ != null)
			code += suffix_.hashCode();
		if (prefix_ != null)
			code += prefix_.hashCode();
		return code;
	}

	public String toString() {
		return new ToStringBuilder(this).append("lastName_", lastName_).append(
				"firstName_", firstName_).toString();
	}
}
