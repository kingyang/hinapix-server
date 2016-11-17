/*
 * Title      : EmailAddress
 * Description:
 * Copyright  : (c) 1998-2001
 * Company    : CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

/**
 * The <code>EmailAddress</code> transient object. Each transient object is
 * mirrored by a persistent object in Objectivity.
 *
 * @author CareScience
 * @version 1.6, 20020528
 */
public class EmailAddress extends TransientObject {

    private String emailAddress_;

    /** Default constructor
     *
     * @deprecated
     */
    public EmailAddress() {
        documentHeader_ = new DocumentHeader();
    }

    /**
     * Overloaded constructor
     *
     * @param <code>Document</code>
     * @deprecated
     */
    public EmailAddress(DocumentHeader documentHeader){
        documentHeader_ = documentHeader;
    }

    /**
     * Constructor taking in all arguments
     *
     * @param documentHeader The DocumentHeader associated
     * @param emailAddress The Email Address
     */
    public EmailAddress(DocumentHeader documentHeader, String emailAddress) {
        setDocumentHeader(documentHeader);
        setEmailAddress(emailAddress);
    }

    public void setEmailAddress (String emailAddress) {
        emailAddress_ = emailAddress;
    }

    public String getEmailAddress () {
        return  emailAddress_;
    }

    public boolean equals (Object obj) {
        boolean isEqual = false;

        if (obj != null) {
            if (obj == this) {
                isEqual = true;
            }
            else if (getClass() == obj.getClass()) {
                EmailAddress address = (EmailAddress)obj;
                isEqual = true;

                if ((emailAddress_ == null)
                    ? (address.emailAddress_ != null)
                    : (!emailAddress_.equals(address.emailAddress_))) {

                    isEqual = false;
                }
            }
        }
        return isEqual;
    }
}
