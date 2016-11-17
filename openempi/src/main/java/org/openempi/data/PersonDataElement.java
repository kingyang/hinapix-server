/*
 * Title:        PersonDataElement
 * Description:  Abstract Class from which some of the Data Elements of a
 *               Person Object inherit their functionality
 * Copyright:    (c) 2001 - 2002
 * Company:      CareScience, Inc.
 *               3600 Market Street
 *               6th Floor
 *               Philadelphia, PA 19104, U.S.A.
 */
package org.openempi.data;

/**
 * This is the Parent Class for the Classes Gender, Race, MaritalStatus,
 * Religion and EthnicGroup in this package.
 *
 * @author mnanchal
 * @version 1.4, 20020326
 */
public abstract class PersonDataElement extends TransientObject {

  private String value_;
  private int sizeOfValue_;
  private long createdDate_;
  private long expiredDate_;
  private boolean expired_;

  /**
   * Gets the value of the Data Element
   * <p>
   * @return String The Value of the Data Element
   */
  public final String getValue() { return value_; }

  /**
   * Sets the value of the Data Element
   * <p>
   * @param newValue The Value of the Data Element
   */
  public final void setValue(String newValue) { value_ = newValue; }

  /**
   * Gets the status of the Data Element Object
   * <p>
   * @return boolean The Expired Flag
   */
  public final boolean isExpired() { return expired_; }

  /**
   * Sets the status of the Data Element Object
   * <p>
   * @param newExpired The Expired Flag
   */
  public final void setExpired(boolean newExpired) { expired_ = newExpired; }

  /**
   * Gets the size of the value of the Data Element
   * <p>
   * @return int The Size of the Value
   */
  public final int getSizeOfValue() { return sizeOfValue_; }

  /**
   * Sets the size of the value of the Data Element
   * <p>
   * @param newSizeOfValue The Size of the Value
   */
  public final void setSizeOfValue(int newSizeOfValue) {
    sizeOfValue_ = newSizeOfValue;
  }

  /**
   * Gets the Created Date of the Data Element
   * <p>
   * @return long The Created Date
   */
  public final long getCreatedDate() { return createdDate_; }

  /**
   * Sets the Created Date of the Data Element
   * <p>
   * @param newCreateDate The Created Date
   */
  public final void setCreatedDate(long newCreatedDate) {
    createdDate_ = newCreatedDate;
  }

  /**
   * Gets the Expired Date of the Data Element
   * <p>
   * @return long The Expired Date
   */
  public final long getExpiredDate() { return expiredDate_; }

  /**
   * Sets the Expired Date of the Data Element
   * <p>
   * @param newExpireDate The Expired Date
   */
  public final void setExpiredDate(long newExpiredDate) {
    expiredDate_ = newExpiredDate;
  }

  public boolean equals(Object obj) {
    boolean isEqual = false;

    if (obj != null) {
      if (obj == this) {
        isEqual = true;
      }
      else if (getClass() == obj.getClass()) {
        PersonDataElement personDataElement = (PersonDataElement) obj;
        isEqual = true;

        if (isExpired() != personDataElement.isExpired()) {
          isEqual = false;
        }
        else if ((getSizeOfValue() == 0)
                 ? (personDataElement.getSizeOfValue() != 0)
                 : (getSizeOfValue() != personDataElement.getSizeOfValue())) {
          isEqual = false;
        }
        else if ((getValue() == null)
                 ? (personDataElement.getValue() != null)
                 : (!getValue().equals(personDataElement.getValue()))) {
          isEqual = false;
        }
      }
    }
    return isEqual;
  }


  public int hashCode()
  {
    int code = 0;
    if (value_ != null)
      code = value_.hashCode();
    return code;
  }

}
