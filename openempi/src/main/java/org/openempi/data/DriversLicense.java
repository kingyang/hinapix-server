/*
 * Title:       DriversLicense
 * Description:
 * Copyright  : (c) 1998-2001
 * Company      CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

import java.util.Date;

/**
 * The <code>DriversLicencse</code> transient object. Each transient object is
 * mirrored by a persistent object in Objectivity.
 *
 * @author CareScience
 * @version 1.6, 20020528
 */
public class DriversLicense extends TransientObject {

    private String licenseNumber_;
    private String issuingState_;
    private Date issueDate_;

    /**
     * no-arg constructor
     *
     * @deprecated
     */
    public DriversLicense() {
        documentHeader_ = new DocumentHeader();
    }

    /**
     * Overloaded constructor
     * @param <code>Document</code>
     *
     * @deprecated
     */
    public DriversLicense(DocumentHeader documentHeader){
        documentHeader_ = documentHeader;
    }

    /**
     * constructor taking in all arguments
     *
     * @param documentHeader The DocumentHeader associated
     * @param licenseNumber The Drivers License Number
     * @param issuingState The issuing State
     * @param issueDate The issue Date
     */
    public DriversLicense(DocumentHeader documentHeader, String licenseNumber,
            String issuingState, Date issueDate) {
        setDocumentHeader(documentHeader);
        setNumber(licenseNumber);
        setState(issuingState);
        setIssueDate(issueDate);
    }

    /**
     * Method used to retrieve the license number
     * @return String the license number
     */
    public String getNumber () {
        return  licenseNumber_;
    }

    /**
     * Method used to change the license number
     * @param String the new license number
     */
    public void setNumber (String license) {
        licenseNumber_ = license;
    }

    /**
     * Method used to get the state for the license
     * @return String the state on the license
     */
    public String getState () {
        return  issuingState_;
    }

    /**
     * Method used to change the state
     * @param String the new state
     */
    public void setState (String state) {
        issuingState_ = state;
    }

    /**
     * Method used to retrieve the issue date of the licese
     * @return Date the issue date
     */
    public Date getIssueDate () {
        //return  (issueDate_ == -1) ? null : new Date(issueDate_);
        return issueDate_;
    }

    /**
     * Method used to set the issue date of the license
     *
     * @param Date the new issue date
     */
    public void setIssueDate (Date date) {
        //issueDate_ = (date == null) ? -1 : date.getTime();
        issueDate_ = date;
    }

    /**
     * Method to check for equals with another Object
     *
     * @param obj The Object to check for equals with
     * @return boolean - True if equal, false otherwise
     */
    public boolean equals (Object obj) {
        boolean isEqual = false;

        if (obj != null) {
            if (obj == this) {
                isEqual = true;
            }
            else if (getClass() == obj.getClass()) {
                DriversLicense license = (DriversLicense)obj;
                isEqual = true;

                if ((licenseNumber_ == null)
                    ? (license.licenseNumber_ != null)
                    : (!licenseNumber_.equals(license.licenseNumber_))) {

                    isEqual = false;
                }
                else if ((issuingState_ == null)
                        ? (license.issuingState_ != null)
                        : (!issuingState_.equals(license.issuingState_))) {
                    isEqual = false;
                }
                else if ((issueDate_ == null)
                        ? (license.issueDate_ != null)
                        : (!issueDate_.equals(license.issueDate_))) {
                    isEqual = false;
                }
//                else if (issueDate_ != license.issueDate_) {
//                    isEqual = false;
//                }
            }
        }
        return isEqual;
    }

  public int hashCode()
  {
    int code = (issueDate_ == null) ? 0: issueDate_.hashCode();
    if (licenseNumber_ != null)
      code += licenseNumber_.hashCode();
    if (issuingState_ != null)
      code += issuingState_.hashCode();
    return code;
  }
}
