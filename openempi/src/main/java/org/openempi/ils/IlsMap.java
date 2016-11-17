package org.openempi.ils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openempi.ils.utility.XfrMap;

/**
   Class for data interchange between ILS nodes and across client interfaces.
   An IlsMap is used for both request and responses for the query() method
   of the IlsService.

   Underlying the IlsMap is a HashMap.  The IlsMap provides for getter/setter
   methods for manipulating the data in the HashMap as well as some constants
   for the attribute names.  The values used should always be standard java
   classes, not any custom classes.  This is because the HashMap will be
   used in a SOAP call to remote nodes and we want to maintain serializable
   compatability between nodes.
*/
public class IlsMap extends XfrMap
  implements java.io.Serializable
{
  // Request paramters 
  public final static String PATIENT_ID = "patientId";
  public final static String DOMAIN_ID = "domainId";
  public final static String FACILITY_ID = "facilityId";
  public final static String HOST_NAME = "hostName";
  public final static String START_DATE = "startDate";
  public final static String END_DATE = "endDate";
  public final static String FROM_LAST_SEARCH = "fromLastSearch";
  public final static String TYPE_CODE = "typeCode";
  public final static String TYPE_CODES = "typeCodes";

  // Response paramters
  public final static String RESULT_URL = "resultUrl";
  public final static String RESULT_DESCRIPTION = "resultDescription";
  public final static String ORDER_URL = "orderUrl";
  public final static String ORDER_DESCRIPTION = "orderDescription";
  public final static String ENCOUNTER_URL = "encounterUrl";
  public final static String ENCOUNTER_DATE = "encounterDate";
  public final static String SYSTEM_NAME = "systemName";

  /**
     Basic constructor.  Creates and empty IlsMap
  */
  public IlsMap()
  {
    super();
  }

  /**
     Creates an IlsMap containing all of the elements of the given IlsMap

     @param ilsMap The source IlsMap to copy values from
  */
  public IlsMap(IlsMap ilsMap)
  {
    this();
    setMap(ilsMap.getMap());
  }
  
  /**
     Creates an IlsMap containing all of the elements of the given HashMap.

     @param ilsMap The source HashMap to copy values from
  */
  public IlsMap(HashMap map)
  {
    this();
    setMap(map);
  }

  /**
     Returns an array of HashMaps from the give array of IlsMaps.

     @param maps The IlsMap array to extract HashMaps from
     @return The HashMap array from the IlsMaps
  */
  public static HashMap[] ilsMapToHashMapArray(IlsMap[] maps)
  {
    HashMap[] ret = new HashMap[maps.length];
    for(int i=0;i<maps.length;i++)
      ret[i] = maps[i].getMap();
    return ret;
  }

  /**
     Returns an array of IlsMaps from the give array of HashMaps.

     @param maps The HashMap array to extract IlsMaps from
     @return The IlsMap array from the HashMaps
  */
  public static IlsMap[] hashMapToIlsMapArray(HashMap[] maps)
  {
    IlsMap[] ret = new IlsMap[maps.length];
    for(int i=0;i<maps.length;i++)
      ret[i] = new IlsMap(maps[i]);
    return ret;
  }

  /**
     Utility method to create an array of IlsMap requests based on the
     parameters given.  The caller is responsible for retrieving a
     recent array of NodeInfo objects using the IlsService getNodeInfo()
     methods.  The other attributes are all required and will construct
     the requests based on them.

     This method will only create requests for nodes which are marked as
     running in the NodeInfo object.  It will also only generate requests
     based on the intersection of the typeCodes requested and the codes
     supported by the specific node and only for nodes that match the
     domainId parameter.

     @param nodes Array of NodeInfo recently retrieved from
                  IlsService.getNodeInfo()
     @param patientId The domain specific patient identifier
     @param domainId The domain/namespace for this patient
     @param facility The facility id for this patient
     @param typeCodes Array of String type codes: RAD, LAB, ADMIN, MRT, PHARM
     @param startDate The beginning date of the search range
     @param endDate The end date of the search range
     @param fromLastSeach If true, startDate is treated as the last search
                          date rather than encounter date.
     @return Array of IlsMaps.  If no requests could be created (no nodes
             up or no typeCode intersection) an empty array is returned.
  */
  public static IlsMap[] generateIlsMaps(NodeInfo[] nodes,
                                         String patientId,
                                         String domainId,
                                         String facilityId,
                                         String[] typeCodes,
                                         Date startDate,
                                         Date endDate,
                                         boolean fromLastSearch)
  {
    ArrayList ret = new ArrayList();

    HashSet reqCodeSet = new HashSet(Arrays.asList(typeCodes));

    for(int i=0; i<nodes.length;i++) {
      if (nodes[i].getIsRunning()
          && nodes[i].getDomainId().equalsIgnoreCase(domainId)) {

        HashSet nodeCodeSet = new HashSet(Arrays.asList(nodes[i].getTypeCodes()));
        nodeCodeSet.retainAll(reqCodeSet);

        if (nodeCodeSet.size() > 0) {
          IlsMap req = new IlsMap();
          req.setTypeCodes(nodeCodeSet);
          req.setDomainId(domainId);
          req.setFacilityId(facilityId);
          req.setHostName(nodes[i].getNode());
          req.setPatientId(patientId);
          req.setStartDate(startDate);
          req.setEndDate(endDate);
          req.setFromLastSearch(fromLastSearch);
          ret.add(req);
        }
      }
    }

    return (IlsMap[])ret.toArray(new IlsMap[ret.size()]);
  }
  
  /**
     Sets the domain Id (namespace) for the request.  The requestor
     is responsible for setting this.

     @param id the domain Id
  */
  public void setDomainId(String id)
  {
    put(DOMAIN_ID, id);
  }

  /**
     @return The domain Id or null if not set
  */
  public String getDomainId()
  {
    return (String) get(DOMAIN_ID);
  }

  /**
     Sets the facility Id for the request.  The requestor
     is responsible for setting this.

     @param id the facility Id
  */
  public void setFacilityId(String id)
  {
    put(FACILITY_ID, id);
  }

  /**
     @return The facility Id or null if not set
  */
  public String getFacilityId()
  {
    return (String) get(FACILITY_ID);
  }

  /**
     Sets a single Type Code for this request.  This will override
     any codes set with setTypeCodes()

     @param code The type code
  */
  public void setTypeCode(String code)
  {
    remove(TYPE_CODES);
    put(TYPE_CODE, code);
  }

  /**
     @return The type code set or null if not specified.
  */
  public String getTypeCode()
  {
    return (String) get(TYPE_CODE);
  }

  /**
     Sets the description for a result.  The ExternalLocatorAdapter
     implementations must set this.

     @param des the description
  */
  public void setResultDescription(String des)
  {
    put(RESULT_DESCRIPTION, des);
  }

  /**
     @return The description for the results
  */
  public String getResultDescription()
  {
    return (String) get(RESULT_DESCRIPTION);
  }

  /**
     Sets the description for a order.  The ExternalLocatorAdapter
     implementations must set this.

     @param des the description
  */
  public void setOrderDescription(String des)
  {
    put(ORDER_DESCRIPTION, des);
  }

  /**
     @return The description for the orders
  */
  public String getOrderDescription()
  {
    return (String) get(ORDER_DESCRIPTION);
  }

  /**
     Sets an array of Type Codes for this request.  This will override
     any code set with setTypeCode()

     @param codes The array of type codes
  */
  public void setTypeCodes(String[] codes)
  {
    remove(TYPE_CODE);
    HashSet set = new HashSet();
    for(int i=0;i<codes.length;i++) {
      set.add(codes[i]);
    }
    codes = new String[set.size()];
    put(TYPE_CODES, set.toArray(codes));
  }

  /**
     Sets a Set of Type Codes for this request.  This will override
     any code set with setTypeCode()

     @param codes The Set of codes to use
  */
  public void setTypeCodes(Set codes)
  {
    put(TYPE_CODES, codes.toArray(new String[codes.size()]));
  }

  /**
    @return The array of type codes or null
  */
  public String[] getTypeCodes()
  {
    return (String[]) get(TYPE_CODES);
  }

  /**
     Sets the patiend id for the query

     @param The patient id
  */
  public void setPatientId(String patientId)
  {
    put(PATIENT_ID, patientId);
  }

  /**
     @return The patient Id or null if not specified
  */
  public String getPatientId()
  {
    return (String) get(PATIENT_ID);
  }

  /**
     Sets the host name of the ILS node processing this request.  This
     is normally set automatically and should not be manually changed

     @param hostName The host name of the Ils node
   */
  public void setHostName(String hostName)
  {
    put(HOST_NAME, hostName);
  }

  /**
     @return The host name of the ILS node processing the request.
   */
  public String getHostName()
  {
    return (String) get(HOST_NAME);
  }

  /**
     Sets the fromLastSearch parameter.  This will control how the
     startDate, endDate are intepreted by some adapters.  If true,
     startDate is interpreted as the date of the last search rather
     than the date of the encounter to search from.  If false,
     startDate is interpreted as the starting encounter date.  On
     some CDRs, this will have no effect on the search.

     If not explicitly set, defaults to "false".

     @param flag true to use last search date, false for encounter date
  */
  public void setFromLastSearch(boolean flag)
  {
    put(FROM_LAST_SEARCH, new Boolean(flag));
  }

  /**
     @return The boolean value for the fromLastSearch parameter
  */
  public boolean getFromLastSearch()
  {
    Boolean bool = (Boolean) get(FROM_LAST_SEARCH);
    if (bool == null)
      return false;
    return bool.booleanValue();
  }

  /**
     Sets the start date for a date range terminated by setEndDate().
     The date specified is generally matched against the encounter or
     service date.

     @param startDate The beginning date of the range.
   */
  public void setStartDate(Date startDate)
  {
    put(START_DATE, startDate);
  }

  /**
     @return The start date or null
  */
  public Date getStartDate()
  {
    return (Date) get(START_DATE);
  }
  
  /**
     Sets the end date for a date range started by setEndDate().
     The date specified is generally matched against the encounter or
     service date.

     @param endDate The end date of the range.
   */
  public void setEndDate(Date endDate)
  {
    put(END_DATE, endDate);
  }
  
  /**
     @return The end date or null
  */
  public Date getEndDate()
  {
    return (Date) get(END_DATE);
  }
  
  /**
     Sets a URL used to retrieve a particular result.  

     @param resultUrl The url for the result
  */
  public void setResultUrl(String resultUrl)
  {
    put(RESULT_URL, resultUrl);
  }
  
  /**
     @return Retults URL or null
  */
  public String getResultUrl()
  {
    return (String) get(RESULT_URL);
  }
  
  /**
     Sets a URL used to retrieve a particular order.  

     @param orderUrl The url for the order
  */
  public void setOrderUrl(String orderUrl)
  {
    put(ORDER_URL, orderUrl);
  }
  
  /**
     @return Order URL or null
  */
  public String getOrderUrl()
  {
    return (String) get(ORDER_URL);
  }
  
  /**
     Sets a URL used to retrieve a particular encounter.  

     @param resultUrl The url for the encounter
  */
  public void setEncounterUrl(String encounterUrl)
  {
    put(ENCOUNTER_URL, encounterUrl);
  }
  
  /**
     @return Encounter URL or null
  */
  public String getEncounterUrl()
  {
    return (String) get(ENCOUNTER_URL);
  }

  /**
     Sets the system name for the ILS node.  This is normally set
     automatically by the system

     @param systemName The name of the ILS node
  */
  public void setSystemName(String systemName)
  {
    put(SYSTEM_NAME, systemName);
  }
  
  /**
    @return The system name or null
  */
  public String getSystemName()
  {
    return (String) get(SYSTEM_NAME);
  }

  /**
     Sets the date of an encounter.  The ExternalLocatorAdapter is
     required to set this

     @param encounterDate the encounterDate
  */
  public void setEncounterDate(Date encounterDate)
  {
    put(ENCOUNTER_DATE, encounterDate);
  }

  /**
     @return The Encounter Date for a response to an ILS request
  */
  public Date getEncounterDate()
  {
    return (Date) get(ENCOUNTER_DATE);
  }


}
