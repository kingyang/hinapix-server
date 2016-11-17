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
package com.hinacom.pix.integrationtests;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.segment.MSA;
import ca.uhn.hl7v2.parser.PipeParser;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7Parser;

/**
 * Test to load patient a number of patients for PDQ Tests. The patient data
 * are from mesa tests.
 * 
 * @author Wenzhi Li
 * @version 1.0, Feb 22, 2009
 */
public class PixLoadConnectathonPatientTest extends AbstractPixPdqTestCase {
    
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		createPIXConnection();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	    //finally close the connection
	    connection.close();
	}

	/**
	 * Loads all patients
	 */
	public void testLoadPatients() {
    	try {
    		InputStream is = PixLoadConnectathonPatientTest.class.getResourceAsStream("/ConnectathonTestPatients.txt");
    		BufferedReader br = new BufferedReader(new InputStreamReader(is));
        	//skip the first header line
    		String line = br.readLine(); 
    		StringBuffer message = null;
    		while ((line = br.readLine()) != null) {
    			String[] fields = line.split("\t");
    			String globalId = fields[0];
    			String globalAuthority = fields[1] + fields[2];
    			String localId = fields[3];
    			String localAuthority = fields[4] + fields[5];
    			String name = fields[6];
    			String dob = fields[8];
    			String streetline = fields[9];
    			String city = fields[10];
    			String state = fields[11];
    			String zipcode = fields[12];
    			String sex = fields[14];
    			String race = fields[15];
    			String referringDoctor = fields[16];
    			
    			//PIX Feed Message Sample:
//    			MSH|^~\&|MESA_ADT|DOMAIN1_ADMITTING|PAT_IDENTITY_X_REF_MGR_MISYSPLC|ALLSCRIPTS|200902231100||ADT^A04^ADT_A01|113xx01-1|P|2.3.1||||||||
//    			EVN||200902231100||||200902231043
//    			PID|||PDQ113XX01^^^IHENA&1.3.6.1.4.1.21367.2009.1.2.300&ISO^PI||MOORE^CHIP||19380224|M||WH|10 PINETREE^^WEBSTER^MO^63119||||||||||||||||||||||||||||
//    			PV1||O||||||7202^SULLIVAN^KAREN^J^^^MD||||||||||||||||||||||||||||

    			//Create Global ID Feed Message
    			StringBuffer globalFeedMsg = new StringBuffer("MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|PAT_IDENTITY_X_REF_MGR_MISYSPLC|ALLSCRIPTS|200902231100||ADT^A04^ADT_A01|"+globalId+"-g|P|2.3.1||||||||\r");
    			globalFeedMsg.append("EVN||200902231100||||200902231043\r");
    			globalFeedMsg.append("PID|||"+globalId+"^^^"+globalAuthority+"^PI||"+name+"||"+dob+"|"+sex+"||"+race+"|"+streetline+"^^"+city+"^"+state+"^"+zipcode+"||||||||||||||||||||||||||||\r");
    			globalFeedMsg.append("PV1||O||||||"+referringDoctor+"||||||||||||||||||||||||||||");
    			PixFeed(globalFeedMsg.toString());
                Thread.sleep(1000); 
    			
    			//Create Local ID Feed Message
    			StringBuffer localFeedMsg = new StringBuffer("MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|PAT_IDENTITY_X_REF_MGR_MISYSPLC|ALLSCRIPTS|200902231100||ADT^A04^ADT_A01|"+localId+"-l|P|2.3.1||||||||\r");
    			localFeedMsg.append("EVN||200902231100||||200902231043\r");
    			localFeedMsg.append("PID|||"+localId+"^^^"+localAuthority+"^PI||"+name+"||"+dob+"|"+sex+"||"+race+"|"+streetline+"^^"+city+"^"+state+"^"+zipcode+"||||||||||||||||||||||||||||\r");
    			localFeedMsg.append("PV1||O||||||"+referringDoctor+"||||||||||||||||||||||||||||");
    			PixFeed(localFeedMsg.toString());
                Thread.sleep(1000); 
    			
    		}
    		is.close();
    	}catch(Exception e) {
    		e.printStackTrace();
    		fail("Fail to load patients");
    	}
	}
	
	/**
	 * Loads all patients
	 */
//	public void testLoadPatients() {
//    	try {
//    		InputStream is = PixLoadConnectathonPatientTest.class.getResourceAsStream("/tests/ConnectathonTestPatients.txt");
//    		BufferedReader br = new BufferedReader(new InputStreamReader(is));
//    		String line;
//    		StringBuffer message = null;
//    		while ((line = br.readLine()) != null) {
//    			if (line.startsWith("MSH")) {
//    				message = new StringBuffer(line);
//    			} else {
//    				message.append("\r");
//    				message.append(line);
//    				if (line.startsWith("PV1")) {
//    					PixFeed(message.toString());
//    				}
//    			}
//    		}
//    		is.close();
//    	}catch(Exception e) {
//    		e.printStackTrace();
//    		fail("Fail to load patients");
//    	}
//	}
	
	private void PixFeed(String msg) throws Exception {
		    PipeParser pipeParser = new HL7Parser();
		    Message adt = pipeParser.parse(msg);
	
	        Initiator initiator = connection.getInitiator();
		    Message response = initiator.sendAndReceive(adt);
	        String responseString = pipeParser.encode(response);	        
	        MSA msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgementCode().getValue());
		    System.out.println("Received response:\n" + responseString);
	}
	
}
