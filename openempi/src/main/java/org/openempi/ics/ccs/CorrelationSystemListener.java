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
package org.openempi.ics.ccs;

import org.openempi.data.Person;

/**
 * The listener interface for receiving action events. Classes interested in
 * processing an action event implement this interface, and the object created
 * with that class is registered with the <code>CorrelationSystem</code>, using
 * the <code>CorrelationSystem.addListener</code> method.
 *
 * @author CareScience
 * @version 1.2, 20020320
 */
public interface CorrelationSystemListener {

  /**
   * Called when a duplicate is found during an add.
   *
   * @param person the <code>Person</code> to be added.
   */
  void duplicateFoundDuringAdd(Person duplicate, Person person);

  /**
   * Called when a similar candidate was detected during an add.
   *
   * @param person the <code>Person</code> to be added.
   */
  void similarFoundDuringAdd(Person similar, Person person);

  /**
   * Called when an expired person was found during an add.
   *
   * @param person the <code>Person</code> to be added.
   */
  void expiredPersonFoundDuringAdd(Person person);
}
