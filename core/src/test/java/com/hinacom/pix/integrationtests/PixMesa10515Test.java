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
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.ics.pids.PersonIdService;

import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.segment.MSA;
import ca.uhn.hl7v2.model.v25.group.RSP_K23_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v25.message.RSP_K23;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Test PIX Manager transactions: PIX Merge.
 * <p>Test Description is available from PIX Mesa 10515 </br>
 * http://ihewiki.wustl.edu/wiki/index.php/MESA/Patient_Cross_Reference_Manager#Test_Case_10515:_PIX_Patient_Feed:_A40</p>
 * 
 * This test uses two different assigning authority domains and two patients. WASHINGTON^MARY and LINCOLN^MARY are the same person registered at different times. WASHINGTON is a maiden name. When she marries, she takes on the LINCOLN name and a new address. This history triggers the following sequence: 
 *<ul> 
 *<li>WASHINGTON registers in domain 1</li> 
 *<li>WASHINGTON registers in domain 2</li> 
 *<li>WASHINGTON marries, takes on the name LINCOLN and changes address at the same time. New A04 registration message is sent in domain 1 (because she forgot to tell the administrator of her name change). 
 * That means the A04 message above is for LINCOLN with a new address but same DOB </li>
 *<li>Someone recognizes the mistake and merges the records in domain 1. LINCOLN is the surviving record</li> 
 *<li>Domain 2 has no direct knowledge of the merge. A query from domain 2 is made with the ID for WASHINGTON</li> 
 *<li>The PIX manager should return the surviving ID for LINCOLN in domain 1.</li> 
 *</ul> 
 *
 * @author Wenzhi Li
 * @version 1.0, Jan 22, 2009
 */
public class PixMesa10515Test extends AbstractPixPdqTestCase {

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
		clearPatients();
	}

   public void testPixMesa10515() {
		try {
			//Step 1: PIX Feed WASHINGTON in the domain HIMSS2005
			String msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10515101|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PIX10515W^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||WASHINGTON^MARY||19220224|F||WH|1 RIDGE TOP^^WEBSTER^MO^63119|||||||10515-101|||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("10515101", msa.getMessageControlID().getValue());

			//Step 2: PIX Feed WASHINTONG in another domain XREF2005
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN2_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10515102|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||XYZ10515W^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI||WASHINGTON^MARY||19220224|F||WH|1 RIDGE TOP^^WEBSTER^MO^63119|||||||10515-401|||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			
			//Step 3: PIX Feed LINCOLN^MARY in the domain HIMSS2005
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10515103|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PIX10515L^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||LINCOLN^MARY||19220224|F||WH|15 FORSYTH^^UNIV CITY^MO^63105|||||||10515-102|||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());

			//Step 4: Merge patient WASHINTON^MARY with LINCOLN^MARY
			msg ="MSH|^~\\&|PAT_IDENTITY_X_REF_MGR_IBM1|IBM1|EHR_MISYS|MISYS|20060919004624-0400||ADT^A40^ADT_A39|10515104|P|2.3.1||||||||\r"+
			"EVN|A40|20060919004624||||20060919004340\r"+
			"PID|||PIX10515L^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI|\r"+
			"MRG|PIX10515W^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI|\r";		
			adt = pipeParser.parse(msg); 
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());

			//Step 5: PIX Query Search for WASHINGTON^MARY of in XREF2005 domain should find LINCOLN^MARY		
			//Request:
 			msg = "MSH|^~\\&|MESA_PIX_CLIENT|MESA_DEPARTMENT|MESA_XREF|XYZ_HOSPITAL|200603121200||QBP^Q23|10515105|P|2.5||||||||\r" + 
		      "QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10515105|XYZ10515W^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI|||||\r" +
		      "RCP|I||||||";
			//Response:			
			Message request = pipeParser.parse(msg);
			response = initiator.sendAndReceive(request);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			ca.uhn.hl7v2.model.v25.segment.MSA msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
			assertEquals("AA", msa25.getAcknowledgmentCode().getValue());
			assertEquals("10515105", msa25.getMessageControlID().getValue());
			ca.uhn.hl7v2.model.v25.segment.QAK qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			assertEquals("QRY10515105", qak.getQueryTag().getValue());
			RSP_K23_QUERY_RESPONSE qrs = ((RSP_K23)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("PIX10515L", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals("HIMSS2005", pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue());
			assertEquals("1.3.6.1.4.1.21367.2005.1.1", pid.getPatientIdentifierList(0).getAssigningAuthority().getUniversalID().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PIX Mesa 10515 PIX Merge");
		}
	   
   }
   
	private void clearPatients() {
		try {
			PersonIdService pid = getPersonIdService();
			{
				Person p1 = createPerson("HIMSS2005", "HIMSS2005", "PIX10515L", "LINCOLN", "MARY",
						"F", null, null, null, null, null, null);
				PersonIdentifier pi = (PersonIdentifier) p1.getPersonIdentifiers().get(0);
				pi.getAssigningAuthority().setUniversalID("1.3.6.1.4.1.21367.2005.1.1");
				pi.getAssigningAuthority().setUniversalIDType("ISO");
				log.debug("Person identifier: " + pi.getId() + " in domain is: Namespace: "
						+ pi.getAssigningAuthority().getNameSpaceID() + ", Universal ID: "
						+ pi.getAssigningAuthority().getUniversalID() + ", ID Type: "
						+ pi.getAssigningAuthority().getUniversalIDType());
				pid.removePerson(p1);
			}
			{
				Person p2 = createPerson("XREF2005", "XREF2005", "XYZ10515W", "WASHINGTON", "MARY",
						"F", null, null, null, null, null, null);
				PersonIdentifier pi = (PersonIdentifier) p2.getPersonIdentifiers().get(0);
				pi.getAssigningAuthority().setUniversalID("1.3.6.1.4.1.21367.2005.1.2");
				pi.getAssigningAuthority().setUniversalIDType("ISO");
				log.debug("Person identifier: " + pi.getId() + " in domain is: Namespace: "
						+ pi.getAssigningAuthority().getNameSpaceID() + ", Universal ID: "
						+ pi.getAssigningAuthority().getUniversalID() + ", ID Type: "
						+ pi.getAssigningAuthority().getUniversalIDType());
				pid.removePerson(p2);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown: " + e);			
		}
	}

}
