package org.openempi.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateOfBirth extends TransientObject
{
  
  private Date dob;
  
  public DateOfBirth() {
    documentHeader_ = new DocumentHeader();
  }
  
  public DateOfBirth(DocumentHeader documentHeader){
    documentHeader_ = documentHeader;
  }
  
  /**
   * constructor taking in all arguments
   *
   * @param documentHeader The DocumentHeader associated
   * @param dob The DOB
   */
  public DateOfBirth(DocumentHeader documentHeader, Date dob)
  {
    setDocumentHeader(documentHeader);
    setDOB(dob);
  }
  
  /**
   * constructor taking only dob
   *
   * @param dob The DOB
   */
  public DateOfBirth(Date dob)
  {
    setDOB(dob);
  }
  
  /**
   * Method used to retrieve the dob number
     * @return String the dob number
     */
  public Date getDOB () {
    return  dob;
  }

  /**
     * Method used to change the dob number
     * @param String the new dob number
     */
  public void setDOB (Date dob) {
    this.dob = dob;
  }

  /**
     * Method to check for equals with another Object
     *
     * @param obj The Object to check for equals with
     * @return boolean - True if equal, false otherwise
     */
  public boolean equals (Object obj)
  {
    boolean isEqual = false;

    if (obj != null) {
      if (obj == this) {
        isEqual = true;
      }
      else if (getClass() == obj.getClass()) {
        DateOfBirth other = (DateOfBirth)obj;
        isEqual = true;

        if (dob == null)
          isEqual = (other.dob != null);
        else {
          GregorianCalendar c1 = new GregorianCalendar();
          GregorianCalendar c2 = new GregorianCalendar();
          c1.setTime(dob);
          c2.setTime(other.dob);
          if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR) ||
              c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH) ||
              c1.get(Calendar.DAY_OF_MONTH) != c2.get(Calendar.DAY_OF_MONTH))
            isEqual = false;
        }
      }
    }
    return isEqual;
  }

  public void isValid()
    throws IllegalArgumentException
  {
    if (dob != null) {
      Calendar rightNow = Calendar.getInstance();
      
      // Date of birth must be no greater than 150 years in the past...
      rightNow.add(Calendar.YEAR, -150);
      
      // ...calculate minimum date of birth.
      long oldestMillis = rightNow.getTime().getTime();
      long dobMillis = dob.getTime();
      
      if (dobMillis < oldestMillis)
        throw new IllegalArgumentException("Date of birth too far in the past to be real: " + dob);
    }
  }

  public int hashCode()
  {
    int code = 0;
    if (dob != null)
      code += dob.hashCode();
    return code;
  }
}
