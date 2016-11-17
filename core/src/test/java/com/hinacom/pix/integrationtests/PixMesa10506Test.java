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

import java.util.Date;

import com.hinacom.pix.ihe.impl_v2.hl7.HL7Parser;
import org.openempi.data.Address;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DocumentHeader;
import org.openempi.data.DomainIdentifier;
import org.openempi.data.Gender;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.PersonName;
import org.openempi.data.SocialSecurityNumber;
import org.openempi.ics.pids.PersonIdService;

import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.segment.MSA;
import ca.uhn.hl7v2.model.v25.group.RSP_K23_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v25.message.RSP_K23;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Test PIX Manager transactions: PIX Update.
 * <p>Test Description is available from PIX Mesa 10506 </br>
 * http://ihewiki.wustl.edu/wiki/index.php/MESA/Patient_Cross_Reference_Manager#Test_Case_10506:_PIX_Query.2C_Patient_Update</p>
 * 
 * Test case 10506 covers PIX Patient Feed, the ADT^A08 message, and PIX queries. 
 * <ul>
 * <li>Patient TAU^TERI is registered in domain HIMS2005 with �correct� demographics.</li> 
 * <li>This patient is then registered in XREF2005 with incorrect demographics. The demographics are sufficiently different that a Cross Reference Manager should not link these two records.</li> 
 * <li>A PIX query is sent, and the expected response is NF.</li>
 * <li>A patient update message is sent for the patient in domain XREF2005 that should synchronize the demographics with those seen in HIMS2005. The Cross Reference Manager should now link the two records.</li>
 * <li>A second PIX query is sent. The response should show that the records are linked. </li>
 *</ul> 
 *
 * @author Wenzhi Li
 * @version 1.0, Jan 22, 2009
 */
public class PixMesa10506Test extends AbstractPixPdqTestCase {

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

   public void testPixMesa10506() {
		try {
			//Step 1: PIX Feed TAU^TERI in the domain HIMSS2005
			String msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10506101|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PIX10506^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||TAU^TERI|RILEY^KARA|19680908|F||WH|15 NORTHBRIDGE^^OAK BROOK^IL^60523|||||||10506-101|499-80-1234||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			Initiator initiator = connection.getInitiator();
			initiator.setTimeoutMillis(60000);
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("10506101", msa.getMessageControlID().getValue());

			//Step 2: PIX Feed TOW^T in the XREF2005 domain
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN2_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10506102|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||ABC10506^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI||TOW^T|ORILEY^KAREN|19680908|F||WH|15 NORTHMOOR^^CHICAGO^IL^60523|||||||10506-201|501-80-5678||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			
			//Step 3: PIX Query Search for TAU^TERI in XREF2005 domain should find no link		
			//Request:
 			msg = "MSH|^~\\&|MESA_PIX_CLIENT|MESA_DEPARTMENT|MESA_XREF|XYZ_HOSPITAL|200603121200||QBP^Q23|10506103|P|2.5||||||||\r" + 
		      "QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10506103|ABC10506^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI|||||\r" +
		      "RCP|I||||||";
			//Response:			
			Message request = pipeParser.parse(msg);
			response = initiator.sendAndReceive(request);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			ca.uhn.hl7v2.model.v25.segment.MSA msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
			assertEquals("AA", msa25.getAcknowledgmentCode().getValue());
			assertEquals("10506103", msa25.getMessageControlID().getValue());
			ca.uhn.hl7v2.model.v25.segment.QAK qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
			assertEquals("NF", qak.getQueryResponseStatus().getValue());
			assertEquals("QRY10506103", qak.getQueryTag().getValue());

			//Step 4: PIX Update LINCOLN^MARY in the domain HIMSS2005
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A08|10506104|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||ABC10506^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI||TAU^TERI|RILEY^KARA|19680908|F||WH|NORTHBRIDGE^^OAK BROOK^IL^60523|||||||10506-201|499-80-1234||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());

			//Step 5: PIX Query Search for TAU^TERI in XREF2005 domain should now find the link		
			//Request:
 			msg = "MSH|^~\\&|MESA_PIX_CLIENT|MESA_DEPARTMENT|MESA_XREF|XYZ_HOSPITAL|200603121200||QBP^Q23|10506105|P|2.5||||||||\r" + 
		      "QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10506105|ABC10506^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI|||||\r" +
		      "RCP|I||||||";
			//Response:			
			request = pipeParser.parse(msg);
			response = initiator.sendAndReceive(request);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
			assertEquals("AA", msa25.getAcknowledgmentCode().getValue());
			assertEquals("10506105", msa25.getMessageControlID().getValue());
			qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			assertEquals("QRY10506105", qak.getQueryTag().getValue());
			RSP_K23_QUERY_RESPONSE qrs = ((RSP_K23)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("PIX10506", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals("HIMSS2005", pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue());
			assertEquals("1.3.6.1.4.1.21367.2005.1.1", pid.getPatientIdentifierList(0).getAssigningAuthority().getUniversalID().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PIX Mesa 10506 PIX Update");
		}
	   
   }
   
	private void clearPatients() {
		try {
			PersonIdService pid = getPersonIdService();
			{
				Person p1 = createPerson("HIMSS2005", "HIMSS2005", "PIX10506", "TAU", "TERI",
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
				Person p2 = createPerson("XREF2005", "XREF2005", "ABC10506", "TAU", "TERI",
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
