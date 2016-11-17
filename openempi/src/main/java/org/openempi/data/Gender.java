/*
 * Title:        Gender
 * Description:  Class representing the Gender of a Person Object
 * Copyright:    (c) 2001 - 2002
 * Company:      CareScience, Inc.
 *               3600 Market Street
 *               6th Floor
 *               Philadelphia, PA 19104, U.S.A.
 */
package org.openempi.data;

/**
 * The <code>Gender</code> transient object. Each transient object is mirrored
 * by a persistent object in Objectivity.
 *
 * @author dngo
 * @version 1.8, 20020528
 */
public class Gender extends PersonDataElement {

    /**
     * no-arg constructor
     *
     * @deprecated
     */
    public Gender() {}

    /**
     * overloaded constructor
     *
     * @deprecated
     */
    public Gender(DocumentHeader documentHeader) {
        documentHeader_ = documentHeader;
    }

    /**
     * Constructor with all arguments
     *
     * @param documentHeader The DocumentHeader associated
     * @param value The value of the Gender
     */
    public Gender(DocumentHeader documentHeader, String value) {
        setDocumentHeader(documentHeader);
        setValue(value);
    }

    public Gender(String value) {
        setValue(value);
    }
    
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (obj != null) {
            if (obj == this) {
                isEqual = true;
            }
            else if (getClass() == obj.getClass()) {
                Gender gender = (Gender) obj;
                isEqual = true;

                if (!super.equals(obj)) {
                    isEqual = false;
                }
            }
        }

        return isEqual;
    }

}
