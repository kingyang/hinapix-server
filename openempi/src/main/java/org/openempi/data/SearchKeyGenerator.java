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

All rights reserved. Any rights not expressly granted herein are reserved.
*/
package org.openempi.data;

import org.openempi.ics.ccs.OpenEmpiStringComparator;

/**
 * The SearchKeyGenerator is a search key and search range factory.  Search keys
 * and ranges are used to search for a string value or range in an database.
 * The keys and ranges filter common string variations including keyboarding
 * errors, nicknames, sequence variations, noise and phonetic variations so that
 * similar strings are hashed to the same rows in the database.
 *
 * @author CareScience, Inc.
 * @author Karl Fankhauser
 * @version 1.2, 20010914
 */
public class SearchKeyGenerator {

  // singleton
  private static final SearchKeyGenerator INSTANCE = new SearchKeyGenerator();
  private static final String NAME_SEARCH_SERVICE = "name1   ";
  private static final String SERVICE = new String(NAME_SEARCH_SERVICE);

  private SearchKeyGenerator() {}

  /**
   * Returns the singleton instance of the SearchKeyGenerator.
   */
  public static SearchKeyGenerator getInstance() {
    return INSTANCE;
  }

  /**
   * Generates a search key from a string.
   */
  public String generateKey(String s) {
    String searchKey = null;

    if (s != null) {
    	OpenEmpiStringComparator jistObj = new OpenEmpiStringComparator(SERVICE, s);
      searchKey = jistObj.getKey(0);
    }

    return searchKey;
  }

  /**
   * Generates a search range from a string.
   */
  public SearchRange generateSearchRange(String s) {
    SearchRange range = null;

    if (s != null) {
    	OpenEmpiStringComparator jistObj = new OpenEmpiStringComparator(SERVICE, s);

      range = new SearchRange();
      range.setStart(jistObj.getStartRange(0));
      //range.setEnd(jistObj.getStartRange(0));
      range.setEnd(jistObj.getEndRange(0));
    }

    return range;
  }

  /**
   * Generates multiple search keys from a string containing multiple tokens.
   */
  public String[] generateMultipleKeys(String s) {
    String[] keys = null;

    if (s != null) {
    	OpenEmpiStringComparator jistObj = new OpenEmpiStringComparator(SERVICE, s);

      keys = new String[jistObj.getKeyCountInt()];

      for (int i = 0; i < keys.length; i++) {
        keys[i] = jistObj.getRangeEntry(i);
      }
    }
    return  keys;
  }
  
  public static void main(String[] args) {
	  SearchRange searchRange = SearchKeyGenerator.getInstance().generateSearchRange("test");
	  System.out.println("Search range for test is " + searchRange);
  }
}
