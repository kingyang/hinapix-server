/*
 * Title:        Race
 * Description:  Class representing the Race of a Person Object
 * Copyright:    (c) 2001 - 2002
 * Company:      CareScience, Inc.
 *               3600 Market Street
 *               6th Floor
 *               Philadelphia, PA 19104, U.S.A.
 */
package org.openempi.data;

/**
 * The <code>Race</code> transient object. Each transient object is mirrored
 * by a persistent object in Objectivity.
 *
 * @author dngo
 * @version 1.8, 20020528
 */
public class Race extends PersonDataElement {

    /**
     * no-arg constructor
     *
     * @deprecated
     */
    public Race() {}

    /**
     * overloaded constructor
     *
     * @deprecated
     */
    public Race(DocumentHeader documentHeader) {
        documentHeader_ = documentHeader;
    }

    /**
     * Constructor with all arguments
     *
     * @param documentHeader The DocumentHeader associated
     * @param value The value of the Race
     */
    public Race(DocumentHeader documentHeader, String value) {
        setDocumentHeader(documentHeader);
        setValue(value);
    }

    /**
     * Method to check for equals with an Object
     *
     * @param obj The Object to be compared to
     * @return boolean - true if equal, false otherwise
     */
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (obj != null) {
            if (obj == this) {
                isEqual = true;
            }
            else if (getClass() == obj.getClass()) {
                Race race = (Race) obj;
                isEqual = true;

                if (!super.equals(obj)) {
                    isEqual = false;
                }
            }
        }

        return isEqual;
    }
}
