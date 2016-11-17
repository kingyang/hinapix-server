/*
 * Title:        EthnicGroup
 * Description:  Class representing the Ethnic Group of a Person Object
 * Copyright:    (c) 2001 - 2002
 * Company:      CareScience, Inc.
 *               3600 Market Street
 *               6th Floor
 *               Philadelphia, PA 19104, U.S.A.
 */
package org.openempi.data;

/**
 * The <code>EthnicGroup</code> transient object. Each transient object is
 * mirrored by a persistent object in Objectivity.
 *
 * @author dngo
 * @version 1.9, 20020528
 */
public class EthnicGroup extends PersonDataElement {

    /**
     * no -arg constructor
     *
     * @deprecated
     */
    public EthnicGroup() {}

    /**
     * overloaded constructor
     *
     * @deprecated
     */
    public EthnicGroup(DocumentHeader documentHeader) {
        documentHeader_ = documentHeader;
    }

    /**
     * Constructor with all arguments
     *
     * @param documentHeader The DocumentHeader associated
     * @param value The value of the Ethnic Group
     */
    public EthnicGroup(DocumentHeader documentHeader, String value) {
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
                EthnicGroup ethnicGroup = (EthnicGroup) obj;
                isEqual = true;

                if (!super.equals(obj)) {
                    isEqual = false;
                }
            }
        }

        return isEqual;
    }
}
