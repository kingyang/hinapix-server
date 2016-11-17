/*
 * Title:       PersonIdentifier
 * Description: The PersonIdentifier transient object.
 * Copyright  : (c) 1998-2001
 * Company      CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

import java.util.Date;

/**
 * <b>WARNING: This class ignores <code>patientConsent</code> in equals().</b>
 *
 * @author CareScience
 * @version 1.2, 20030131
 */
public class PersonIdentifier extends TransientObject
{
    
    private DomainIdentifier assigningAuthority_;
    private DomainIdentifier assigningFacility_;
    private String id_;
    private String corpId_;
    private String updated_corpId;
    private String identifierTypeCode_;
    private long effectiveDate_ = -1;
    private long expirationDate_ = -1;
    private boolean patientConsent_ = true;
    
    /**
     * The no-arg constructor
     *
     * @deprecated
     */
    public PersonIdentifier()
    {}
    
    /**
     * Alternate constructor.
     *
     * @param id the ID
     * @param assigningAuthority the assigning authority for this ID
     * @param assigningFacility the assigning facility for this ID
     * @param identifierTypeCode the identifier type code
     *
     * @deprecated
     */
    public PersonIdentifier(String id, DomainIdentifier assigningAuthority,
            DomainIdentifier assigningFacility, String identifierTypeCode)
    {
        id_ = id;
        assigningAuthority_ = assigningAuthority;
        assigningFacility_ = assigningFacility;
        identifierTypeCode_ = identifierTypeCode;
    }
    
    /**
     * Constructor taking all arguments
     *
     * @param documentHeader The DocumentHeader for this ID
     * @param id the ID
     * @param assigningAuthority the assigning authority for this ID
     * @param assigningFacility the assigning facility for this ID
     * @param identifierTypeCode the identifier type code
     */
    public PersonIdentifier(DocumentHeader documentHeader, String id,
            DomainIdentifier assigningAuthority,
            DomainIdentifier assigningFacility, String identifierTypeCode)
    {
        setDocumentHeader(documentHeader);
        setId(id);
        setAssigningAuthority(assigningAuthority);
        setAssigningFacility(assigningFacility);
        setIdentifierTypeCode(identifierTypeCode);
    }
    
    public String getId()
    {
        return  id_;
    }
    
    public void setId(String newId)
    {
        id_ = newId;
    }
    
    public String getCorpId()
    {
        return  corpId_;
    }
    
    public void setCorpId(String newId)
    {
        corpId_ = newId;
    }
    
    public String getUpdatedCorpId()
    {
        return updated_corpId;
    }
    
    public void setUpdatedCorpId(String updatedCorpId)
    {
        this.updated_corpId = updatedCorpId;
    }
    
    public void setAssigningAuthority(DomainIdentifier newAssigningAuthority)
    {
        assigningAuthority_ = newAssigningAuthority;
    }
    
    public DomainIdentifier getAssigningAuthority()
    {
        return assigningAuthority_;
    }
    
    public void setIdentifierTypeCode(String newIdentifierTypeCode)
    {
        identifierTypeCode_ = newIdentifierTypeCode;
    }
    
    public String getIdentifierTypeCode()
    {
        return identifierTypeCode_;
    }
    
    public void setAssigningFacility(DomainIdentifier newAssigningFacility)
    {
        assigningFacility_ = newAssigningFacility;
    }
    
    public DomainIdentifier getAssigningFacility()
    {
        return assigningFacility_;
    }
    
    public void setEffectiveDate(Date date)
    {
        effectiveDate_ = (date == null) ? -1 : date.getTime();
    }
    
    public Date getEffectiveDate()
    {
        return (effectiveDate_ == -1) ? null : new Date(effectiveDate_);
    }
    
    public void setExpirationDate(Date date)
    {
        expirationDate_ = (date == null) ? -1 : date.getTime();
    }
    
    public Date getExpirationDate()
    {
        return (expirationDate_ == -1) ? null : new Date(expirationDate_);
    }
    
    /**
     * Method to set the Patient Consent flag of the identifier
     *
     * @param patientConsent the value of the flag to be set
     */
    public void setPatientConsent(boolean patientConsent)
    {
        patientConsent_ = patientConsent;
    }
    
    /**
     * Method to get the Patient Consent flag value of the identifier
     *
     * @return true if consent granted; false otherwise
     */
    public boolean getPatientConsent()
    {
        return patientConsent_;
    }
    
    public void isValid()
    {
        
        if (id_ == null)
            throw new IllegalArgumentException("Person Identifier may not be null");
        
        assigningAuthority_.isValid();
        assigningFacility_.isValid();
    }
    
    /**
     * Method to check for equals between this and another Object
     *
     * @param obj - The Object to which this Object is compared
     * @return boolean - true if equal; false otherwise
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        
        if (getClass() != obj.getClass())
            return false;
        
        if (obj == this)
            return true;
        
        PersonIdentifier other = (PersonIdentifier)obj;
        
        if (id_ == null || other.id_ == null ||
                assigningAuthority_ == null || other.assigningAuthority_ == null ||
                assigningFacility_ == null || other.assigningFacility_ == null)
            return false;
        
        if (id_.equals(other.id_) &&
                assigningAuthority_.equals(other.assigningAuthority_) &&
                assigningFacility_.equals(other.assigningFacility_))
        {
            boolean equals = false;
            if (corpId_ == null)
            {
                equals = (other.corpId_ == null) ? true : false;
            }
            else
            {
                equals = corpId_.equals(other.corpId_);
            }
            if(equals)
            {
                if (updated_corpId == null)
                {
                    equals = (other.updated_corpId == null) ? true : false;
                }
                else
                {
                    equals = updated_corpId.equals(other.updated_corpId);
                }
            }
            return equals;
        }
        
        return false;
    }
    
    public int hashCode()
    {
        int code = 0;
        if (id_ != null)
            code += id_.hashCode();
        if (corpId_ != null)
        {
            code+=corpId_.hashCode();
        }
        if (assigningAuthority_ != null)
            code += assigningAuthority_.hashCode();
        if (assigningFacility_ != null)
            code += assigningFacility_.hashCode();
        
        return code;
    }
    
}
