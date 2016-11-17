/*
 * Title:       CdrAdapterSql
 * Description: Implements the ExternalLocatorAdapter interface.
 * Copyright:   (c) 2001-2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.adapters;

import java.util.Date;

import org.openempi.ils.ExternalLocatorAdapter;
import org.openempi.ils.IlsException;
import org.openempi.ils.IlsMap;


public class CdrAdapterDemo
  implements ExternalLocatorAdapter
{
  public IlsMap[] findLocators(IlsMap req)
    throws IlsException
  {
    req.put("ipid", req.getPatientId());
    req.put("qid", req.getPatientId());
    String type = req.getTypeCode();
    if (type.equals("LAB"))
      req.setResultDescription("Kodak eReport");
    else if (type.equals("RAD"))
      req.setResultDescription("Kodak Imaging");
    else
      req.setResultDescription("Test Result");
    req.setEncounterDate(new Date());
    IlsMap[] locators = { req };
    return locators;
  }
}
