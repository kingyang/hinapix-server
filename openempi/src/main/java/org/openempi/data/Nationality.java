/*
 * Title:        Nationality
 * Description:  Class representing the Nationality of a Person Object
 * Copyright:    (c) 2001 - 2002
 * Company:      CareScience, Inc.
 *               3600 Market Street
 *               6th Floor
 *               Philadelphia, PA 19104, U.S.A.
 */
package org.openempi.data;

/**
 * The <code>Nationality</code> transient object. Each transient object is
 * mirrored by a persistent object in Objectivity.
 *
 * @author mnanchal
 * @version 1.5, 20020528
 */
public class Nationality extends PersonDataElement {

    /**
     * Constructor
     *
     * @param documentHeader
     * @deprecated
     */
    public Nationality(DocumentHeader documentHeader) {
        documentHeader_ = documentHeader;
    }

    /**
     * Constructor with all arguments
     *
     * @param documentHeader The DocumentHeader associated
     * @param value The value of the Nationality
     */
    public Nationality(DocumentHeader documentHeader, String value) {
        setDocumentHeader(documentHeader);
        setValue(value);
    }

    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (obj != null) {
            if (obj == this) {
                isEqual = true;
            }
            else if (getClass() == obj.getClass()) {
                Nationality nationality = (Nationality) obj;
                isEqual = true;

                if (!super.equals(obj)) {
                    isEqual = false;
                }
            }
        }

        return isEqual;
    }
}
