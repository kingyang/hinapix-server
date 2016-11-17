/* Copyright 2009 Misys PLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License. 
 */
package com.hinacom.pix.mesatests;

import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;
import com.hinacom.pix.ihe.log.IMesaLogger;
import com.hinacom.pix.util.AxiomUtil;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;


/**
 * The class writes MESA test messages to a print stream.  It
 * is used to create logs for MESA test submissions.
 * 
 * @author Wenzhi Li
 * @version 1.0, Dec 24, 2008
 */
public class MesaTestLogger implements IMesaLogger {
	
	private static Logger log = Logger.getLogger(MesaTestLogger.class.getName());

	private PrintStream stream = null;
	private DateFormat formatter = null;
	
	private ArrayList<String> savedStrings = null;

	  
	/**
	 * Create a new MESA logger that will write to the provided
	 * stream.
	 * 
	 * @param stream The place to write MESA log messages
	 */
	public MesaTestLogger(PrintStream stream) {
		this.stream = stream;
		this.formatter = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
	}
    /**
     * Create a new MESA logger that will write to the provided
     * stream.
     *
     * @param logfile The logfile name.
     */
    public MesaTestLogger(String logfile) {
        try {
            File file = new File(logfile);
            String parent = file.getParent();
            File parentDir = new File(parent);
            if(!parentDir.exists()) {
            	parentDir.mkdirs();
            }
            this.stream = new PrintStream(new File(logfile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.formatter = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");

    }


    /**
	 * Write a test title to the MESA log.  Used at the start
	 * of a test.
	 * 
	 * @param testNumber
	 */
	public void writeTestBegin(String testNumber) {
		if (stream != null) {
			printTimestamp();
			stream.print("MESA 2008 Test ");
			stream.print(testNumber);
			stream.println(" begin ...");
			printTimestamp();
			try {
				InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
				boolean isFirst = true;
				stream.print("++ Misys Healthcare (IP ");
				for (InetAddress address: addresses) {
					if (!isFirst) stream.print(", ");
					stream.print(address.getHostAddress());
					isFirst = false;
				}
				stream.println(")");
			} catch (UnknownHostException e) {
				log.error("Can't get local machine name and address");
				stream.print("++ Misys Healthcare");
			}
			printTimestamp();
			stream.print("++ ");
			SimpleDateFormat format = new SimpleDateFormat("EEEE MMMM d, yyyy h:mm:ssa z");
			stream.println(format.format(new Date()));
		}
	}
	
	/**
	 * Write a test finish to the MESA log.  Used at the end
	 * of a test.
	 * 
	 * @param testNumber
	 */
	public void writeTestEnd(String testNumber) {
		if (stream != null) {
			printTimestamp();
			stream.print("MESA 2008 Test ");
			stream.print(testNumber);
			stream.println(" end.");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.misyshealthcare.connect.ihe.IMesaLogger#writeString(java.lang.String)
	 */
	public void writeString(String message) {
		if (stream != null) {
			printTimestamp();
			stream.println(message);
		}
	}
	
	public void saveString(String message) {
		if (savedStrings == null) savedStrings = new ArrayList<String>();
		savedStrings.add(message);
	}
	
	public List<String> getSavedStrings() {
		ArrayList<String> saved = savedStrings;
		savedStrings = null;
		return saved;
	}

	/* (non-Javadoc)
	 * @see com.misyshealthcare.connect.ihe.IMesaLogger#writeSoapMessage(javax.xml.soap.SOAPMessage)
	 */
	public void writeSoapMessage(SOAPMessage message) {
		if (stream != null) {
			if (message != null) {
				// Write out the SOAP part of the message
				DOMSource source = new DOMSource(message.getSOAPPart());
				StreamResult result = new StreamResult(new StringWriter());
				Transformer transformer = null;
				try {                                                             
    	    		 TransformerFactory factory = TransformerFactory.newInstance();
					factory.setAttribute("indent-number", new Integer(2));
					transformer = factory.newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.setOutputProperty(OutputKeys.METHOD, "xml");
					transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");	
					transformer.transform(source, result);
					stream.print(result.getWriter().toString());
				} catch (Exception e) {
					log.error("Can't write SOAP message to MESA logger", e);
				}
				// Write out information about the attachments
				Iterator attachments = message.getAttachments();
				while (attachments.hasNext()) {
					AttachmentPart attachment = (AttachmentPart)attachments.next();
					stream.println("Attachment: " + attachment.getContentId() + ", " + attachment.getContentType());
				}
			} else {
				stream.println("NULL");
			}
		}
	}
	
	/**
	 * Write out an Axiom OMElement message to the logger.
	 */
	public void writeAxiomElementMessage(OMElement message) {	
		if (stream != null) {
			if (message != null) {
				StreamResult result = new StreamResult(new StringWriter());
				try {
					AxiomUtil.prettify(message, result);
					stream.print(result.getWriter().toString());
				}catch(Exception e) {
					log.error("Can't write OMElement to logger", e);
				}
			} else {
				stream.println("NULL");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.misyshealthcare.connect.ihe.IMesaLogger#writeHL7Message()
	 */
	public void writeHL7Message(Message message) {
		if (stream != null) {
			if (message != null) {
				// Write out the message
				try {
					stream.print(PipeParser.encode(message, new EncodingCharacters('|', "^~\\&")));
				} catch (HL7Exception e) {
					stream.println("Invalid HL7 message");
				}
			} else {
				stream.println("NULL");
			}
		}
	}

	/**
	 * Write out the body elements in a SOAP message as a list in
	 * the log.
	 * 
	 * @param message The SOAP message to get the elements from
	 */
	public void writeSoapBodyElements(SOAPMessage message) {
		writeString("The elements in the SOAP Body are ...");
		boolean empty = true;
		try {
			SOAPBody body = message.getSOAPBody();
			if (body != null) {
				int count = 1;
				Iterator elements = body.getChildElements();
				while (elements.hasNext()) {
					Node element = (Node) elements.next();
					if (element != null) {
						empty = false;
						if (element.getNodeType() == Node.TEXT_NODE) {
							writeString(" " + count + ") \"" + element.toString() + "\"");
						} else {
							writeString(" " + count + ") " + element.toString());							
						}
						count = count + 1;
					}
				}
			}
		} catch (Exception e) {
			log.error("Can't log SOAP body elements", e);
		}
		if (empty) {
			writeString(" The body is empty.");
		}
	}
	
	/**
	 * Print a timestamp to the MESA logger.
	 */
	private void printTimestamp() {
		stream.print(formatter.format(new Date()));
		stream.print(" ");
	}

}
