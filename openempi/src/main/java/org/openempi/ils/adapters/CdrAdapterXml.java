/*
 * Title:       CdrAdapterXml
 * Description: Create XML document in a SOAP envelope. Open HTTP connection.
 *              Sends document to the URL and also writes the document to a file.
 * Copyright:   (c) 2001-2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.openempi.ils.ExternalLocatorAdapter;
import org.openempi.ils.IlsException;
import org.openempi.ils.IlsMap;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.IlsSystemProps;
import org.openempi.ils.utility.xml.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A class that creates an XML document in a SOAP envelope.
 * Opens an HTTP connection. Sends the document to the URL and to a file.
 *
 * @author dngo, CareScience
 * @version 1.6, 20020304
 */
public class CdrAdapterXml implements ExternalLocatorAdapter
{
  private ByteArrayOutputStream ostream = new ByteArrayOutputStream();
  private DocumentBuilder db;
  private IlsSystemProps prop;
  private IlsMap request;
  private Logger log;

  public CdrAdapterXml()
    throws javax.xml.parsers.ParserConfigurationException
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    db = dbf.newDocumentBuilder();
  }

  public IlsMap[] findLocators(IlsMap request)
    throws IlsException
  {
    Document newDoc = null;
    
    log = Logger.getLogger("ILS");
    this.request = request;
    String type = request.getTypeCode();
    prop = IlsProps.getSystemPropsForType(request.getDomainId(), type);
    
    try {
      Document cdrDoc = createDoc(request.getPatientId(),
                                  "", // Leave this,host system fails if not ""
                                  type,
                                  request.getStartDate(),
                                  request.getEndDate());
      
      /* write the document to the output stream */
      writeToByteArray(cdrDoc, ostream);
      
      if (log.isDebugEnabled()) {
        /* write the output stream out to a file */
        FileOutputStream outStream = new FileOutputStream(prop.getInterfaceProperty("Outfile"));
        outStream.write(ostream.toByteArray());
        outStream.close();
      }

      /* send the document to a URL */
      newDoc = sendToUrl(cdrDoc, prop.getInterfaceProperty("Url"));

      /* get Locators */
      return foundLocators(newDoc);
    }
    catch (IOException ioEx) {
      log.error(ioEx,ioEx);
    }
    return new IlsMap[0];
  }

  /**
   * Populates an XML document.
   *
   * @throws IOException
   */
  private Document createDoc(String patientID,
                             String providerID,
                             String type,
                             Date startDateRange,
                             Date endDateRange)
    throws IOException
  {
    Document cdrDoc = db.newDocument();

    /* create root findLocators */
    Element findLoc = cdrDoc.createElement("findLocators");
    cdrDoc.appendChild(findLoc);

    /* create patient element and attributes */
    Element patient = cdrDoc.createElement("PATIENT");
    findLoc.appendChild(patient);

    /* create patient's identifier element and attributes */
    Element identifier = cdrDoc.createElement("IDENTIFIER");
    identifier.setAttribute("ID", patientID);
    patient.appendChild(identifier);

    /* create provider element and attributes */
    Element provider = cdrDoc.createElement("PROVIDER");
    findLoc.appendChild(provider);

    /* create provider's identifier element and attributes */
    Element ident2 = cdrDoc.createElement("IDENTIFIER");
    ident2.setAttribute("SYSTEM", prop.getInterfaceProperty("System"));
    ident2.setAttribute("ID", providerID);
    provider.appendChild(ident2);

    /* create type element and attribute */
    Element iType = cdrDoc.createElement("INFO_TYPES");
    iType.setAttribute("ITYPE", type);
    findLoc.appendChild(iType);

    String classes = prop.getInterfaceProperty("Classes");
    StringTokenizer tok = new StringTokenizer(classes,",");

    while(tok.hasMoreTokens()) {
      String cls = tok.nextToken();
      iType.appendChild(createTextNode(cdrDoc, "INFO_CLASSES", cls));
    }

    /*
    if ((encounterIDs != null) && (encounterIDs.length > 0)) {
      Element encounters = cdrDoc.createElement("ENCOUNTERS");
      findLoc.appendChild(encounters);
    }
    */

    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
    
    String start = df.format(startDateRange);
    log.debug("StartDate="+start);

    String end = df.format(endDateRange);
    log.debug("EndDate="+ end);

    /* create date_range element and attributes */
    Element dateRange = cdrDoc.createElement("DATE_RANGE");
    dateRange.setAttribute("StartDateRange", start);
    dateRange.setAttribute("EndDateRange", end);
    findLoc.appendChild(dateRange);

    return cdrDoc;
  }

  /**
   * Creates a text Node in the XML Document.
   *
   * @param theDoc The Document in which the Node should be created
   * @param nodeName The name of the Node
   * @param nodeValue The node value
   * @return Node
   */
  private Node createTextNode(Document theDoc, String nodeName, String nodeValue) {
    Element docEl = null;
    if ((theDoc != null) && (nodeName != null) && (nodeValue != null)) {
      docEl = theDoc.createElement(nodeName);
      Node nValue = theDoc.createTextNode(nodeValue);
      docEl.appendChild(nValue);
    }
    return  docEl;
  }

  /**
   * Writes a <code>Document</code> to an output stream.
   *
   * @param cdrDoc The XML Document created
   * @param ostream the output stream
   * @throws IOException
   */
  private void writeToByteArray(Document cdrDoc, ByteArrayOutputStream ostream)
    throws IOException
  {
    if ((cdrDoc != null) && (ostream != null)) {
      XMLWriter writer = new XMLWriter(ostream);
      writer.writeXML(cdrDoc);
    }
  }

  /**
   * Method to send the XML document to a desired URL
   *
   * @param doc the XML Document to be sent
   * @urlName the String form of the URL
   */
  private Document sendToUrl(Document doc, String urlName)
  {
    Document inputDoc = null;
    byte[] bites = null;
    URL url = null;
    HttpURLConnection httpConn = null;
    OutputStream urlStream = null;
    InputStream in = null;

    if ((doc != null) && (urlName != null)) {
      try {
        url = new URL(urlName);
        httpConn = (HttpURLConnection)url.openConnection();
        httpConn.setRequestProperty("Connection", "Keep-Alive");
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);

        urlStream = httpConn.getOutputStream();
        urlStream.write(ostream.toByteArray());

        log.debug("Data written");

        log.debug("Response message is " + httpConn.getResponseMessage());

        log.debug("Response code is " + httpConn.getResponseCode());

        in = httpConn.getInputStream();

        bites = new byte[httpConn.getContentLength()];

        byte ch = (byte) in.read();
        int i=0;

        while (ch != -1) {
          bites[i++] = ch;
          ch = (byte)in.read();
        }

        log.debug("FindLocator Response: " + new String(bites));

        inputDoc = db.parse(new ByteArrayInputStream(bites));

        /* close streams */
        urlStream.close();
        in.close();

        /* close connection */
        httpConn.disconnect();
      }
      catch (SAXException saxEx) {
        log.error(saxEx,saxEx);
      }
      catch (IOException ioEx) {
        log.error(ioEx, ioEx);
      }
    }
    return inputDoc;
  }

  private IlsMap[] foundLocators(Document newDoc)
  {
    IlsMap[] locators = null;

    if (newDoc != null) {
      /* parse the result Document */
      List locatorList = parseDocument(newDoc);

      /* convert the list to an array */
      if (locatorList != null)
        locators = (IlsMap[])locatorList.toArray(new IlsMap[0]);

      /* this loop is for debug only - can be removed if required */
      if (log.isDebugEnabled()) {
        if (locators.length > 0) {
          for (int i = 0; i < locators.length; i++) 
            log.debug("Printing Locator" + i + ": " + locators[i]);
        }
      }
    }

    return locators;
  }

  /**
   * This method parses the <code>Document</code>. Creates a new
   * <code>HashMap</code> and assigns values to the <code>HashMap</code>.
   * @param newDoc the <code>Document</code>.
   * @return List containing <code>HashMaps</code>. Each <code>HashMap</code>
   * contains record locator information.
   */
  private List parseDocument(Document newDoc)
  {
    Node node;
    Element el;
    NodeList nodes;
    List locatorList = new ArrayList();         /* Create the list */

    if (newDoc != null) {
      NodeList foundLocList = newDoc.getElementsByTagName("foundLocator");

      IlsMap locator = null;

      for (int i = 0; i < foundLocList.getLength(); i++) {
        locator = new IlsMap(request);
        Element foundLoc = (Element) foundLocList.item(i);

        /* Look for PARAM elements */
        nodes = foundLoc.getElementsByTagName("PARAM");

        for (int j = 0; j < nodes.getLength(); j++) {
          el = (Element) nodes.item(j);

          if (el != null) {
            /* populate el into HashMap */
            locator.put(el.getAttribute("Name"), el.getAttribute("Value"));
          }
        }

        /* Look for INFO_TYPE elements */
        nodes = foundLoc.getElementsByTagName("INFO_TYPE");

        for (int k = 0; k < nodes.getLength(); k++) {
          el = (Element) nodes.item(k);

          if (el != null) {
            /* populate info type into HashMap */
            locator.setTypeCode(el.getAttribute("ITYPE"));
          }
        }

        /* Look for EVENT_DATE elements */
        nodes = foundLoc.getElementsByTagName("EVENT_DATE");

        for (int n = 0; n < nodes.getLength(); n++) {
          el = (Element) nodes.item(n);

          if (el != null && (node = el.getFirstChild()) != null) {
            /* populate event date into HashMap */
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
            Date dt = df.parse(node.getNodeValue(), new ParsePosition(0));
            locator.setEncounterDate(dt);
          }
        }

        /* Look for RESULT_DESCRIPTION elements */
        nodes = foundLoc.getElementsByTagName("RESULT_DESCRIPTION");

        for (int q = 0; q < nodes.getLength(); q++) {
          el = (Element) nodes.item(q);
          
          if (el != null && (node = el.getFirstChild()) != null) {
            locator.setResultDescription(node.getNodeValue());
          }
        }

        /* Add each locator to the list */
        locatorList.add(locator);
      }
    }

    return locatorList;
  }
}
