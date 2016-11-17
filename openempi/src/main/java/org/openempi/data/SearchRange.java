package org.openempi.data;

/*
   Any use of the Material is governed by the terms of the actual license
   agreement between CareScience and the user. Any reproduction or
   redistribution, by any means - whether mechanical or electronic -
   without the express written permission of CareScience is strictly
   prohibited. The Material includes information protected by copyrights
   and/or patents held by CareScience and/or the University of
   Pennsylvania.

   It is understood by users of this Material that the information
   contained herein is intended to serve as a guide and basis for general
   comparisons only, and NOT as the sole basis upon which any specific
   action is to be recommended or undertaken. All users of this site and
   its data agree to hold CareScience harmless from any and all claims,
   losses, damages, obligations or liabilities, directly or indirectly
   relating to these materials, caused thereby or arising therefrom.

   CareScience assumes no responsibility for errors or omission in these
   materials. CareScience does not warrant the accuracy or completeness of
   the information, text, graphics, links or other items contained within
   these materials. CareScience shall not be liable for any special,
   indirect, incidental, or consequential damages, including without
   limitation, lost revenues or lost profits, which may result from the use
   of these materials. CareScience may make changes to these materials, or
   to the products described therein, at any time without notice.

   THESE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO, THE IMPLIED
   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
   NON-INFRINGEMENT.

   THE DOCUMENTS AND RELATED GRAPHICS PUBLISHED IN THIS DOCUMENT COULD
   INCLUDE TECHNICAL INACCURACIES OR TYPOGRAPHICAL ERRORS. CHANGES ARE
   PERIODICALLY ADDED TO THE MATERIAL HEREIN. CareScience AND/OR ITS
   SUPPLIERS MAY MAKE IMPROVEMENTS AND/OR CHANGES IN THE PRODUCT(S) AND/OR
   THE PROGRAM(S) DESCRIBED HEREIN AT ANY TIME.

   � Copyright 1998-2001 CareScience, 3600 Market Street, 6th Floor
   Philadelphia, PA 19104 U.S.A.

   All rights reserved. Any rights not expressly granted herein are
   reserved.
*/

/**
 * A SearchRange provides information used during a database query to select
 * a range of items from the database.
 */
public class SearchRange {

    private String start;
    private String end;

    /**
     * Returns the start of the search range.
     */
    public String getStart () {
        return  start;
    }

    /**
     * Sets the start of the search range.
     */
    public void setStart (String newStart) {
        start = newStart;
    }

    /**
     * Sets the end of the search range.
     */
    public void setEnd (String newEnd) {
        end = newEnd;
    }

    /**
     * Returns the end of the search range.
     */
    public String getEnd () {
        return  end;
    }

    public boolean equals (Object obj) {
        if ((obj == null) || (!(obj instanceof SearchRange)))
            return  false;
        if (obj == this)
            return  true;
        SearchRange range = (SearchRange)obj;
        if ((end == null) ? (range.end != null) : (!end.equals(range.end)))
            return  false;
        if ((start == null) ? (range.start != null) : (!start.equals(range.start)))
            return  false;
        return  true;
    }

    public String toString() {
      return start + " " + end;
    }
}
