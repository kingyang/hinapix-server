package org.openempi.data;


public class SocialSecurityNumber extends TransientObject
{
  
  private String ssn;
  
  public SocialSecurityNumber() {
    documentHeader_ = new DocumentHeader();
  }
  
  public SocialSecurityNumber(DocumentHeader documentHeader){
    documentHeader_ = documentHeader;
  }
  
  /**
   * constructor taking in all arguments
   *
   * @param documentHeader The DocumentHeader associated
   * @param ssn The SSN
   */
  public SocialSecurityNumber(DocumentHeader documentHeader, String ssn)
  {
    setDocumentHeader(documentHeader);
    setSSN(ssn);
  }
  
  /**
   * constructor taking in just SSN
   *
   * @param ssn The SSN
   */
  public SocialSecurityNumber(String ssn)
  {
    setSSN(ssn);
  }
  
  /**
     Method used to retrieve the ssn number
     @return String the ssn number
  */
  public String getSSN () {
    return  ssn;
  }

  /**
     Method used to change the ssn number
     @param String the new ssn number
  */
  public void setSSN (String ssn) {
    this.ssn = ssn;
  }

  /**
     Returns true if the SocialSecurityNumber obj given matches this.

     @param obj Another SocialSecurityNumber object to compare
     @return true if the ssn values are equal or both are null, otherwise
             returns false.
   */
  public boolean equals (Object obj)
  {
    boolean isEqual = false;

    if (obj != null) {
      if (obj == this) {
        isEqual = true;
      }
      else if (getClass() == obj.getClass()) {
        SocialSecurityNumber other = (SocialSecurityNumber)obj;
        isEqual = true;

        if ((ssn == null)
            ? (other.ssn != null)
            : (!ssn.equals(other.ssn))) {

          isEqual = false;
        }
      }
    }
    return isEqual;
  }

  /**
     Tests for validity of this SocialSecurityNumber.  Currently,
     it must be exactly 9 digits in length.

     @exception IllegalArgumentException if this ssn is invalid
  */
  public void isValid()
    throws IllegalArgumentException
  {
    if (ssn != null) {
      if (ssn.length() != 9) 
        throw new IllegalArgumentException("Social Security Number size != 9 : " +ssn);

      try {
        Integer intObj = Integer.valueOf(ssn);
        if (intObj.intValue() < 0)
          throw new IllegalArgumentException("Social Security Number invalid : " +ssn);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Social Security Number invalid : " +ssn);
      }
    }
  }

  public int hashCode()
  {
    int code = 0;
    if (ssn != null)
      code += ssn.hashCode();
    return code;
  }
}
