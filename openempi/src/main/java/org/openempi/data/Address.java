/*
 * Title      : Address
 * Description:
 * Copyright  : (c) 1998-2001
 * Company    : CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

import java.util.Date;

/**
 * The <code>Address</code> transient object. Each transient object is mirrored
 * by a persistent object in Objectivity.
 *
 * @author CareScience
 * @version 1.9, 20020528
 */
public class Address extends TransientObject {

    private String address1_;
    private String address2_;
    private String city_;
    private String state_Province_;
    private String zip_;
    private String country_;
    private String addressType_;
    private String parishCode_;
    private long startDate_ = -1L;
    private long endDate_ = -1L;

    /** Default constructor
     *
     * @deprecated
     */
    public Address() {
        documentHeader_ = new DocumentHeader();
    }

    /**
     * Overloaded constructor
     * @param <code>Document</code>
     *
     * @deprecated
     */
    public Address(DocumentHeader documentHeader) {
        documentHeader_ = documentHeader;
    }

    /**
     * Overloaded constructor
     * @param address1
     * @param address2
     * @param city
     * @param state
     * @param zip
     *
     * @deprecated
     */
    public Address(String address1, String address2, String city,
            String state, String zip) {
        setDocumentHeader(new DocumentHeader());
        setAddress1(address1);
        setAddress2(address2);
        setCity(city);
        setState(state);
        setZipCode(zip);
    }

    /**
     * Overloaded constructor
     * @param documentHeader
     * @param address1
     * @param address2
     * @param city
     * @param state
     * @param zip
     */
    public Address(DocumentHeader documentHeader, String address1,
            String address2, String city, String state, String zip) {
        setDocumentHeader(documentHeader);
        setAddress1(address1);
        setAddress2(address2);
        setCity(city);
        setState(state);
        setZipCode(zip);
    }

    /**
     * Set the first part of the Address
     * @param newAddress1
     */
    public void setAddress1 (String newAddress1) {
        address1_ = newAddress1;
    }

    /**
     * Method used to get the first part of address
     * @return String the first part of address
     */
    public String getAddress1 () {
        return  address1_;
    }

    /**
      * Method used to set the second part of the address
      * @param String the second part of address
      */
    public void setAddress2 (String newAddress2) {
        address2_ = newAddress2;
    }

    /**
     *  Method used to get the address
     *  @return String the address
     */
    public String getAddress2 () {
        return  address2_;
    }

    /**
     *  Method used to set the city
     *  @param String the city name
     */
    public void setCity (String newCity) {
        city_ = newCity;
    }

    /**
     * Method used to get the city
     * @return String the city
     */
    public String getCity () {
        return  city_;
    }

    /**
     * Method used to set the state name
     * @param String the state name
     */
    public void setState (String newState) {
        state_Province_ = newState;
    }

    /**
     * Method used to retrieve the state name
     * @return String the state name
     */
    public String getState () {
        return  state_Province_;
    }

    /**
     * Method used to set the zip code
     * @param String the new zip code
     */
    public void setZipCode (String newZip) {
        zip_ = newZip;
    }

    /**
     * Method used to retrieve the zip code
     * @return String the zip code
     */
    public String getZipCode () {
        return  zip_;
    }

    /**
     * Method used to set the country name
     * @param String the country name
     */
    public void setCountry (String newCountry) {
        country_ = newCountry;
    }

    /**
     * Method used to get the country name
     * @return String the country name
     */
    public String getCountry () {
        return  country_;
    }

    /**
     * Method used to set the address type
     * @param String the new address type
     */
    public void setAddressType (String newAddressType) {
        addressType_ = newAddressType;
    }

    /**
     * Method used to get the address type
     * @return String the address type
     */
    public String getAddressType () {
        return  addressType_;
    }

    /**
     * Get the parish code
     * @return String
     */
    public String getParishCode () {
        return  parishCode_;
    }

    /**
     * Set the parish code
     * @param parishCode
     */
    public void setParishCode (String parishCode) {
        parishCode_ = parishCode;
    }

    /**
     * Get the start date
     * @return Date
     */
    public Date getStartDate () {
        return(startDate_ == -1) ? null : new Date(startDate_);
    }

    /**
     * Set the start date
     * @param date
     */
    public void setStartDate (Date date) {
        startDate_ = (date == null) ? -1 : date.getTime();
    }

    /**
     * Get the end date
     * @return Date
     */
    public Date getEndDate () {
        return(endDate_ == -1) ? null : new Date(endDate_);
    }

    /**
     * Set the end date
     * @param date
     */
    public void setEndDate (Date date) {
        endDate_ = (date == null) ? -1 : date.getTime();
    }

    /**
     * Compare the supplied object to this object and return a boolean
     * value indicating if they are equal.
     *
     * @param obj The object to compare
     * @return boolean
     */
    public boolean equals (Object obj) {

        boolean isEqual = false;

        if (obj != null) {
            if (obj == this) {
                isEqual = true;
            }
            else if (getClass() == obj.getClass()) {

                Address address = (Address)obj;

                isEqual = true;

                if ((address1_ == null)
                        ? (address.address1_ != null)
                        : (!address1_.equals(address.address1_))) {
                    isEqual =  false;
                }
                else if ((address2_ == null)
                        ? (address.address2_ != null)
                        : (!address2_.equals(address.address2_))) {
                    isEqual =  false;
                }
                else if ((city_ == null)
                        ? (address.city_ != null)
                        : (!city_.equals(address.city_))) {
                    isEqual =  false;
                }
                else if ((state_Province_ == null)
                        ? (address.state_Province_ != null)
                        : (!state_Province_.equals(address.state_Province_))) {
                    isEqual =  false;
                }
                else if ((zip_ == null)
                        ? (address.zip_ != null)
                        : (!zip_.equals(address.zip_))) {
                    isEqual =  false;
                }
                else if ((country_ == null)
                        ? (address.country_ != null)
                        : (!country_.equals(address.country_))) {
                    isEqual =  false;
                }
                else if ((addressType_ == null)
                        ? (address.addressType_ != null)
                        : (!addressType_.equals(address.addressType_))) {
                    isEqual =  false;
                }
                else if ((parishCode_ == null)
                        ? (address.parishCode_ != null)
                        : (!parishCode_.equals(address.parishCode_))) {
                    isEqual =  false;
                }
                else if (startDate_ != address.startDate_) {
                    isEqual =  false;
                }
                else if (endDate_ != address.endDate_) {
                    isEqual =  false;
                }
            }
        }
        return isEqual;
    }

  public int hashCode()
  {
    int code = 0;
    if (address1_ != null)
      code += address1_.hashCode();
    if (address2_ != null)
      code += address2_.hashCode();
    if (city_ != null)
      code += city_.hashCode();
    if (state_Province_ != null)
      code += state_Province_.hashCode();
    if (zip_ != null)
      code += zip_.hashCode();
    if (country_ != null)
      code += country_.hashCode();
    if (addressType_ != null)
      code += addressType_.hashCode();
    if (parishCode_ != null)
      code += parishCode_.hashCode();
    return code;
  }
  
}
