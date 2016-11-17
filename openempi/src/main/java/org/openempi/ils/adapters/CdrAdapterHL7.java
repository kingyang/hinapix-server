/*
 * Title:       CdrAdapterHL7
 * Description: Open and write a data stream to a server socket.
 * Copyright:   (c) 2001-2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.adapters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.openempi.ils.ExternalLocatorAdapter;
import org.openempi.ils.IlsException;
import org.openempi.ils.IlsMap;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.IlsSystemProps;

/**
   Adapter class for the CDR to communicate using HL7 Messages via sockets. The
   class opens a socket connection and sends a QRY as a data stream to a server
   socket and receives an RSP.
 */

public class CdrAdapterHL7
  implements ExternalLocatorAdapter
{
  private static final String startBlock = "" + new Character((char)0x0B);
  private static final String carrReturn = "\r";
  private static final String endBlock = "" + new Character((char)0x1C);

  private OutputStreamWriter output;
  private BufferedReader input;
  private Socket mySocket;
  private DateFormat df;
  private DateFormat adf;
  private Calendar cal;
  private int port;
  private IlsSystemProps prop;
  private IlsMap request;
  private Logger log;
  
  /**
     Opens the connection to the remote host.  

     @exception IOException on socket failure
  */
  public void openConnection()
    throws IOException
  {
    String portNumber = prop.getInterfaceProperty("Port");
    String host = prop.getInterfaceProperty("Host");

    port = Integer.parseInt(portNumber);
    mySocket = new Socket(host, port);
    
    output = new OutputStreamWriter(mySocket.getOutputStream());
    
    input = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
  }

  /**
     Closes the open socket connect
  */
  public void closeConnection()
  {
    try {
      if (output != null)
        output.close();
    } catch (IOException ioEx) {
    }

    try {
      if (input != null)
        input.close();
    } catch (IOException ioEx) {
    }

    try {
      if (mySocket != null)
        mySocket.close();
    } catch (IOException ioEx) {
    }
  }

  /**
   * Implementation of required interface.
   */
  public IlsMap[] findLocators(IlsMap request) 
    throws IlsException
  {
    try {
      log = Logger.getLogger("ILS");
      this.request = request;
      String type = request.getTypeCode();
      prop = IlsProps.getSystemPropsForType(request.getDomainId(), type);
      
      df = new SimpleDateFormat("yyyyMMddhhmm");
      adf = new SimpleDateFormat("yyyyMMddhhmmss");
      cal = new GregorianCalendar();
      
      openConnection();
      
      String inputStr = sendReceiveData(request.getPatientId(),
                                        type,
                                        request.getStartDate(),
                                        request.getEndDate());
      
      log.debug("Received Input Stream: " + inputStr);

      return receiveLocators(inputStr);
    } catch (Exception e) {
      log.error(e, e);
      throw new IlsException(e.getMessage());
    } finally {
      closeConnection();
    }
  }

  /**
    Method to send and receive data via the Socket Connection
   
    @param patientID The Patient ID of the <code>Person</code>
    @param type The type code being queried
    @param startDateRange Start date for search
    @param endDateRange End date for search
    @return String The HL7 response to the query request
   */
  private String sendReceiveData(String patientID,
                                 String type,
                                 Date startDateRange,
                                 Date endDateRange)
  {

    byte[] bites = null;
    StringBuffer sBuffer = new StringBuffer();
    String dataString = null;
    String msh = null;
    String qrd = null;
    String qrf = null;
    String types = null;

    String classes = prop.getInterfaceProperty("Classes");

    /* Constructs a string and sends over socket */
    if (this != null && output != null && input != null) {
      try {
        cal.setTime(new Date());
        msh = startBlock + "MSH|^~\\&" +            /* add the start block */
          "|ILS| |" +                         /* Sending application*/
          " | |" +                            /* Receiving application */
          df.format(cal.getTime()) +          /* Time stamp */
          "| |QRY^Q01|" +                     /* Message type */
          "CDE" + df.format(cal.getTime()) +  /* Sequence number */
          "|P|" +                             /* Production - Processing ID */
          "2.3.1" + carrReturn;               /* HL7 version ID */
        
        qrd = "QRD|" +
          df.format(cal.getTime()) +          /* Time stamp */
          "|D" +                              /* Display - Query format code */
          "|I" +                              /* Immediate - Query priority */
          "|" + adf.format(cal.getTime()) +   /* Query ID */
          "| | | |" +
          patientID +                         /* Who subject filter*/
          "|RES" +                            /* What subject filter */
          "|" + type +                       /* What department data code */
          carrReturn;
        
        qrf = "QRF| |" +
          df.format(startDateRange) + "|" +   /* When data start date time */
          df.format(endDateRange) + "| |" +   /* When data end date time */
          classes + "| | | |" + carrReturn; /* What user qualifier */
        
        qrf = qrf + endBlock + carrReturn; /* add the end block */
        
        dataString = msh + qrd + qrf;
        
        log.debug(dataString);
        
        output.write(dataString, 0, dataString.length());
        output.flush();
        
        /* reading from the socket */
        if (input != null) {
          String str;
          while ((str = input.readLine()) != null) 
            sBuffer.append(str).append("\n");
        }
        else {
          log.warn("InputStream is null !!");
        }
        
        /* close the output stream, input stream and the socket */
        output.close();
        input.close();
        mySocket.close();
      }
      catch (IOException ioEx) {
        log.error(ioEx, ioEx);
      }
    }
    else {
      log.error("Error while sending or receiving information over the socket");
    }
    
    return sBuffer.toString();
  }

  /**
     Method to format the response String into Locator Maps
     
     @param newString The response String to be formatted
     @return IlsMap[] The array of Locator Maps
   */
  private IlsMap[] receiveLocators(String str) 
  {
    List locatorList = new ArrayList();

    IlsMap locator = null;
    int pos = 0;
    
    while (true) {
      locator = new IlsMap(request);
      pos = createMap(str, pos, locator);
      if (pos == -1)
        break;
      locatorList.add(locator);
    }

    return (IlsMap[])locatorList.toArray(new IlsMap[0]);
  }

  /**
     Parses the next DSP block from the str.  Each DSP block looks like
     <pre>
       DSP|||<i>val</i>
     </pre>
     which can terminated by either a carriage return or a pipe.  Anyway,
     This will parse out the DSP block starting at the given position and
     append the val section into the StringBuffer.

     @param str The input str
     @param pos The position in the str to start looking
     @param val A StringBuffer to append the DSP value to
     @return The position in the string after the DSP block parsed.
             This can be passed into a subsequent call to getDSP()
             to parse the next block.  -1 is returned at the end of
             string or on parse error.
  */
  private int getDSP(String str, int pos, StringBuffer val)
  {
    int start, end;
    
    str = str.substring(pos);
    start = str.indexOf("DSP");
    if (start == -1)
      return -1;
    start += 3;
    end = str.indexOf("DSP", start);
    if (end == -1)
      end = str.indexOf(endBlock, start);
    if (end == -1)
      return -1;
    str = str.substring(start, end-1);
    if (str.charAt(str.length()-2) == '|')
      str = str.substring(0,str.length()-2);
    start = str.lastIndexOf("|");
    if (start == -1)
      return -1;
    val.append(str.substring(start+1));
    return end+pos;
  }

  
  /**
    Method to create a Map; representing a locator, by parsing a String.
   
    @param str The String to be parsed
    @param pos The position in str to start parsing from
    @param map The IlsMap to fill in the parsed values from
    @return The position in str after the parsed section. This may
            be passed to subsequent calls to createMap() to parse
            the next IlsMap.  -1 is returned at the end of the string
            or on parse error.
   */
  private int createMap(String str, int pos, IlsMap map)
  {
    String strin = null;
    int next;

    log.debug("str: " + str);

    /* get the encounterDate */
    StringBuffer buf;

    next = pos;
    next = getDSP(str, next, buf = new StringBuffer());
    if (next == -1) {
      log.debug("DSP not found");
      return -1;
    }
    map.setEncounterDate(df.parse(buf.toString(), new ParsePosition(0)));
    log.debug("encounterDate=" + map.getEncounterDate());

    next = getDSP(str, next, buf = new StringBuffer());
    if (next == -1) {
      log.debug("DSP not found");
      return -1;
    }
    map.setTypeCode(buf.toString());
    log.debug("typeCode="+ map.getTypeCode());

    next = getDSP(str, next, buf = new StringBuffer());
    if (next == -1) {
      log.debug("DSP not found");
      return -1;
    }
    // Ignore class codes

    next = getDSP(str, next, buf = new StringBuffer());
    if (next == -1) {
      log.debug("DSP not found");
      return -1;
    }
    map.setResultDescription(buf.toString());
    log.debug("resultDescription="+ map.getResultDescription());

    next = getDSP(str, next, buf = new StringBuffer());
    if (next == -1) {
      log.debug("DSP not found");
      return -1;
    }
    String avps = buf.toString();
    StringTokenizer avpsTok = new StringTokenizer(avps, ";");
    while(avpsTok.hasMoreTokens()) {
      String avp = avpsTok.nextToken();
      StringTokenizer avpTok = new StringTokenizer(avp, "=");
      String att = avpTok.nextToken();
      String val = avpTok.nextToken();
      map.put(att, val);
      log.debug(att + "=" + val);
    }

    return next;
  }

}
