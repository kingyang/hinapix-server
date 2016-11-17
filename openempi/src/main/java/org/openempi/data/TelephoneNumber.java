/*
 * Title      : TelephoneNumber
 * Description:
 * Copyright  : (c) 1998-2001
 * Company    : CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

/**
 * The <code>TelephoneNumber</code> transient object. Each transient object is
 * mirrored by a persistent object in Objectivity.
 *
 * @author CareScience
 * @version 1.8, 20020528
 */
public class TelephoneNumber extends TransientObject {

    private String telecomUseCode_;
    private String countryCode_;
    private String areaCode_;
    private String phoneNumber_;
    private String extension_;

    /** Default constructor
     *
     * @deprecated
     */
    public TelephoneNumber() {
        documentHeader_ = new DocumentHeader();
    }

    /**
     * Overloaded constructor
     * @param areaCode
     * @param phoneNumber
     * @deprecated
     */
    public TelephoneNumber(String areaCode, String phoneNumber) {
        documentHeader_ = new DocumentHeader();
        areaCode_ = areaCode;
        phoneNumber_ = phoneNumber;
    }

    /**
     * Overloaded constructor
     * @param documentHeader
     * @param areaCode
     * @param phoneNumber
     */
    public TelephoneNumber(DocumentHeader documentHeader, String areaCode,
            String phoneNumber) {
        setDocumentHeader(documentHeader);
        setAreaCode(areaCode);
        setPhoneNumber(phoneNumber);
    }

    public String getTelecomUseCode () {
        return telecomUseCode_;
    }

    public void setTelecomUseCode (String telUseCode) {
        telecomUseCode_ = telUseCode;
    }

    public String getCountryCode () {
        return countryCode_;
    }

    public void setCountryCode (String countryCode) {
        countryCode_ = countryCode;
    }

    public String getAreaCode () {
        return areaCode_;
    }

    public void setAreaCode (String areaCode) {
        areaCode_ = areaCode;
    }

    public String getPhoneNumber () {
        return phoneNumber_;
    }

    public void setPhoneNumber (String phoneNumber) {
        phoneNumber_ = phoneNumber;
    }

    public String getExtension () {
        return extension_;
    }

    public void setExtension (String extension) {
        extension_ = extension;
    }

    /**
     * Method to compare and check for 'Equals' on another Object
     * @param obj The Object to be compared
     * @return boolean Returns true if equal and false if not
     */
    public boolean equals (Object obj) {
        boolean isEqual = false;

        if (obj != null) {
            if (obj == this) {
                isEqual = true;
            }
            else if (getClass() == obj.getClass()) {

                TelephoneNumber tn = (TelephoneNumber) obj;
                isEqual = true;

                if ((areaCode_ == null)
                    ? (tn.areaCode_ != null)
                    : (!getNonNullString(areaCode_).equals(
                            getNonNullString(tn.areaCode_)))) {

                    isEqual = false;
                }
                else if ((countryCode_ == null)
                        ? (tn.countryCode_ != null)
                        : (!getNonNullString(countryCode_).equals(
                                getNonNullString(tn.countryCode_)))) {
                    isEqual = false;
                }
                else if ((extension_ == null)
                        ? (tn.extension_ != null)
                        : (!getNonNullString(extension_).equals(
                                getNonNullString(tn.extension_)))) {
                    isEqual = false;
                }
                else if ((phoneNumber_ == null)
                        ? (tn.phoneNumber_ != null)
                        : (!getNonNullString(phoneNumber_).equals(
                                getNonNullString(tn.phoneNumber_)))) {
                    isEqual = false;
                }
            }
        }
        return isEqual;
    }

    /**
     *  returns a Non Null String
     */
    private String getNonNullString(String s) {
        return(s == null) ? "" : s;
    }

  public int hashCode()
  {
    int code = 0;

    if (telecomUseCode_ != null)
      code += telecomUseCode_.hashCode();
    if (countryCode_ != null)
      code += countryCode_.hashCode();
    if (areaCode_ != null)
      code += areaCode_.hashCode();
    if (phoneNumber_ != null)
      code += phoneNumber_.hashCode();
    if (extension_ != null)
      code += extension_.hashCode();
    
    return code;
  }
  
}
