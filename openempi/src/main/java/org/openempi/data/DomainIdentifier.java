/*
 * Title      : DomainIdentifier
 * Description:
 * Copyright  : (c) 1998-2003
 * Company    : CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

/**
 * The <code>DomainIdentifier</code> class.
 *
 * @author CareScience
 * @version 1.2, 20030203
 */
public class DomainIdentifier extends TransientObject {

    private String namespaceID_;
    private String universalID_;
    private String universalIDType_;

    /**
     * The no-arg constructor
     *
     * @deprecated
     */
    public DomainIdentifier() {}

    /**
     * Overloaded constructor
     *
     * @deprecated
     */
    public DomainIdentifier(String namespaceID) {
        namespaceID_ = namespaceID;
    }

    /**
     * Overloaded constructor
     *
     * @deprecated
     */
    public DomainIdentifier(String namespaceID, String universalID,
            String universalIDType) {
        setNameSpaceID(namespaceID);
        setUniversalID(universalID);
        setUniversalIDType(universalIDType);
    }

    /**
     * Constructor with all arguments
     *
     * @param documentHeader The DocumentHeader associated
     * @param namespaceID The Namespace ID of the DomainIdentifier
     * @param universalID The universal ID of the DomainIdentifier
     * @param universalIDType The Type of the universal ID
     */
    public DomainIdentifier(DocumentHeader documentHeader, String namespaceID,
            String universalID, String universalIDType) {
        setDocumentHeader(documentHeader);
        setNameSpaceID(namespaceID);
        setUniversalID(universalID);
        setUniversalIDType(universalIDType);
    }

    public String getNameSpaceID() {
        return namespaceID_;
    }

    public void setNameSpaceID(String newNameSpaceID) {
        namespaceID_ = newNameSpaceID;
    }

    public String getUniversalID() {
        return universalID_;
    }

    public void setUniversalID(String newUniversalID) {
        universalID_ = newUniversalID;
    }

    public String getUniversalIDType() {
        return universalIDType_;
    }

    public void setUniversalIDType(String new_type) {
        universalIDType_ = new_type;
    }

    /**
     * Checks for validity of this <code>DomainIdentifier</code>.
     *
     * @throws IllegalArgumentException if anything is bad
     */
    public void isValid() {
        if (namespaceID_ == null) 
          throw new IllegalArgumentException("Namespace Id may not be null");

        if (universalID_ == null) 
          throw new IllegalArgumentException("Universal Id may not be null");

        if (universalIDType_ == null) 
          throw new IllegalArgumentException("Universal Id Type may not be null");
    }

  public boolean equals(Object obj)
  {
    if (obj == null)
      return false;
      
    if (getClass() != obj.getClass())
      return false;

    if (obj == this)
      return true;

    DomainIdentifier other = (DomainIdentifier)obj;

    if (namespaceID_ == null || other.namespaceID_ == null)
      return false;
    
    if (namespaceID_.equals(other.namespaceID_))
      return true;

    return false;
  }

  public String toString() {
    return getClass().getName() + ": " + namespaceID_ + ", " +
      universalID_ + ", " + universalIDType_;
  }

  public int hashCode()
  {
    int code = 0;
    if (namespaceID_ != null)
      code += namespaceID_.hashCode();
    if (universalID_ != null)
      code += universalID_.hashCode();
    if (universalIDType_ != null)
      code += universalIDType_.hashCode();
    return code;
  }
}
