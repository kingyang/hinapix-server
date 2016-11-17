/*
 * Title:       ExternalLocatorAdapter
 * Description: The ExternalLocatorAdapter interface.
 * Copyright:   (c) 2001-2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils;


/**
  Defines an interface to be implemented on an external system for integration
  with the Information Locator Service (ILS). ILS uses information about
  <i>where</i> a person has been treated to find <i>what</i> clinical
  information is available for that patient. It does not attempt to display the
  clinical information. 
 */
 public interface ExternalLocatorAdapter
 {
   /**
      Returns an array of <code>IlsMap</code> types containing clinical
      record locator information.  The IlsMap contains all of the
      parameters set by the requestor to perform the query.  This will
      typically include the <code>patientId</code>,
      <code>startDate</code>, <code>endDate</code> and <code>typeCode</code>.
      Other paramters may be specified as well.  The system will automatically
      fill in the following paramters for each request: <code>domainId</code>,
      <code>hostName</code>, <code>clientId</code> (for CDRs).

      The findLocators() method is called once per each type code specified
      in the request.  Therefore, the findLocators() method should use
      the IlsMap getCodeType() method to return the specific type.  The
      getCodeTypes() method will return the single type as an array of
      the single element.  Even if the original request from the caller
      had specified multiple types, findLocators() will be called
      separately for each type.

      The implementor is responsible for returning an array of IlsMaps
      representing the results.  The implementor should copy the
      incoming IlsMap into each of the returned IlsMaps before setting
      any additional paramters.  The implementor is required to set the
      following in each response: <code>encounterDate</code>,
      <code>description</code>.

      The <code>resultUrl</code>, <code>orderUrl</code> and
      <code>encounterUrl</code> may be set directly by the implementation,
      or, preferrably, they may the set by setting the values for
      the URL_PARAMS defined in the IlsProps.xml into the IlsMap. The
      appropriate URLs will be automatically constructed from the values set.

      The system will automaticall provide the  <code>locationName</code>
      from the ILS configuration.
      
      @param map The IlsMap representing the request.
      @return Array of IlsMaps representing the responses
      @exception java.rmi.RemoteException
   */
   public IlsMap[] findLocators(IlsMap map)
     throws IlsException;
}
