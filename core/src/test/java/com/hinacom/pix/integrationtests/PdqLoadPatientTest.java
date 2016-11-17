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

import com.hinacom.pix.ihe.impl_v2.hl7.HL7Parser;
import com.hinacom.pix.ihe.pdq.PdqQuery;


import junit.framework.TestCase;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Test to load patient a number of patients for PDQ Tests. The patient data
 * are from mesa tests.
 * 
 * @author Wenzhi Li
 * @version 1.0, Jan 22, 2009
 */
public class PdqLoadPatientTest extends AbstractPixPdqTestCase {
    
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
	}

	/**
	 * Loads 8 patients
	 */
	public void testLoadPatients() {
    	try {
			//Register Patient 1:	
			String msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|113xx102|P|2.5||||||||\r" + 
			    	      "EVN||200310011100||||200310011043\r" +
			    	      "PID|||PDQ113XX01^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||MOORE^CHIP||19380224|M||WH|10 PINETREE^^WEBSTER^MO^63119|||||||1130001-101|||||||||||||N||||||||\r"+
			    	      "PV1||O||||||5101^NELL^FREDERICK^P^^DR|||||||||||ONE1130001^^^&1.3.6.1.4.1.21367.2005.1.1&ISO|||||||||||||||||||||||||200310011045|||||||V|";
		
		    PipeParser pipeParser = new HL7Parser();
		    Message adt = pipeParser.parse(msg);
	
	        Initiator initiator = connection.getInitiator();
		    Message response = initiator.sendAndReceive(adt);
	        String responseString = pipeParser.encode(response);	        
	        MSA msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgmentCode().getValue());
		    System.out.println("Received response:\n" + responseString);
 
    	  //Register Patient 2:	
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|113xx104|P|2.5||||||||\r"+
			      "EVN||200310011100||||200310011043\r" +
			      "PID|||PDQ113XX02^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||MOORE^RALPH||19480724|M||WH|510 S KINGSHIGHWAY^^ST. LOUIS^MO^63110|||||||1130002-101|||||||||||||N||||||||\r"+  
			      "PV1||O||||||5101^NELL^FREDERICK^P^^DR|||||||||||ONE1130002^^^&1.3.6.1.4.1.21367.2005.1.1&ISO|||||||||||||||||||||||||200310011045|||||||V|";
		    adt = pipeParser.parse(msg);
		    response = initiator.sendAndReceive(adt);
	        responseString = pipeParser.encode(response);	        
	        msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgmentCode().getValue());
		    System.out.println("Received response:\n" + responseString);

			//Register Patient 3:		
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|113xx106|P|2.5||||||||\r"+
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PDQ113XX03^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||MOHR^ALICE||19580131|F||BL|820 JORIE BLVD.^^OAK BROOK^IL^60523|||||||1130004-101|||||||||||||N||||||||\r"+  
		      "PV1||O||||||5101^NELL^FREDERICK^P^^DR|||||||||||ONE1130004^^^&1.3.6.1.4.1.21367.2005.1.1&ISO|||||||||||||||||||||||||200310011045|||||||V|";
		    adt = pipeParser.parse(msg);
		    response = initiator.sendAndReceive(adt);
	        responseString = pipeParser.encode(response);	        
	        msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgmentCode().getValue());
		    System.out.println("Received response:\n" + responseString);

		  //Register Patient 4:
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|113xx108|P|2.5||||||||\r"+
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PDQ113XX04^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||MOODY^WARREN||19780820|M||BL|1000 CLAYTON RD^^CLAYTON^MO^63105|||||||1130004-101|||||||||||||N||||||||\r"+  
		      "PV1||O||||||5101^NELL^FREDERICK^P^^DR|||||||||||ONE1130004^^^&1.3.6.1.4.1.21367.2005.1.1&ISO|||||||||||||||||||||||||200310011045|||||||V|";
		    adt = pipeParser.parse(msg);
		    response = initiator.sendAndReceive(adt);
	        responseString = pipeParser.encode(response);	        
	        msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgmentCode().getValue());
		    System.out.println("Received response:\n" + responseString);
			
		  //Register Patient 5:
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|113xx110|P|2.5||||||||\r"+
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PDQ113XX05^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||MOONEY^STAN||19780920|M||BL|100 TAYLOR^^ST LOUIS^MO^63110|||||||1130005-101|||||||||||||N||||||||\r"+  
		      "PV1||O||||||5101^NELL^FREDERICK^P^^DR|||||||||||ONE1130005^^^&1.3.6.1.4.1.21367.2005.1.1&ISO|||||||||||||||||||||||||200310011045|||||||V|";
		    adt = pipeParser.parse(msg);
		    response = initiator.sendAndReceive(adt);
	        responseString = pipeParser.encode(response);	        
	        msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgmentCode().getValue());
		    System.out.println("Received response:\n" + responseString);
			
		  //Register Patient 11:
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|113xx112|P|2.5||||||||\r"+
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PDQ113XX11^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||SANDERS^CHIP||19420224|M||WH|100 EUCLID^^WEBSTER^MO^63119|||||||1130011-101|||||||||||||N||||||||\r"+  
		      "PV1||O||||||5101^NELL^FREDERICK^P^^DR|||||||||||ONE1130011^^^&1.3.6.1.4.1.21367.2005.1.1&ISO|||||||||||||||||||||||||200310011045|||||||V|";
		    adt = pipeParser.parse(msg);
		    response = initiator.sendAndReceive(adt);
	        responseString = pipeParser.encode(response);	        
	        msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgmentCode().getValue());
	        System.out.println("Received response:\n" + responseString);

		  //Register Patient 12:
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|113xx114|P|2.5||||||||\r"+
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PDQ113XX12^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||RENDEL^GRACE||19720814|F||WH|200 BEMISTON^^CLAYTON^MO^63105|||||||1130012-101|||||||||||||N||||||||\r"+  
		      "PV1||O||||||5101^NELL^FREDERICK^P^^DR|||||||||||ONE1130012^^^&1.3.6.1.4.1.21367.2005.1.1&ISO|||||||||||||||||||||||||200310011045|||||||V|";
		    adt = pipeParser.parse(msg);
		    response = initiator.sendAndReceive(adt);
	        responseString = pipeParser.encode(response);	        
	        msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgmentCode().getValue());
	        System.out.println("Received response:\n" + responseString);
			
		  //Register Patient 13:
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|113xx116|P|2.5||||||||\r"+
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PDQ113XX13^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||FERGUSON^CHIP||19930914|M||WH|4970 OAKLAND^^ST LOUIS^MO^63110|||||||1130013-101|||||||||||||N||||||||\r"+  
		      "PV1||O||||||5101^NELL^FREDERICK^P^^DR|||||||||||ONE1130013^^^&1.3.6.1.4.1.21367.2005.1.1&ISO|||||||||||||||||||||||||200310011045|||||||V|";
		    adt = pipeParser.parse(msg);
		    response = initiator.sendAndReceive(adt);
	        responseString = pipeParser.encode(response);	        
	        msa = (MSA)response.get("MSA");
	        assertEquals("AA", msa.getAcknowledgmentCode().getValue());
	        System.out.println("Received response:\n" + responseString);
		    
		    //finally close the connection
		    connection.close();
    	}catch(Exception e){
    		e.printStackTrace();
    		fail("Fail to load patients");
    	}
	}
	
}
