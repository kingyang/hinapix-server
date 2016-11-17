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

import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.segment.MSA;
import ca.uhn.hl7v2.model.v25.group.RSP_K23_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v25.message.RSP_K23;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7Parser;

/**
 * Test PIX Manager transactions: PIX Feed and PIX Query. 
 * First PIX Query find one matching; the second Query found 
 * nothing.
 * 
 * The sample came from Mesa tests 10501.
 * 
 * @author Wenzhi Li
 * @version 1.0, Jan 22, 2009
 */
public class PixMesa10501Test extends AbstractPixPdqTestCase {

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

   public void testPixMesa10501() {
		try {
			//Step 1: PIX Feed one patient in HIMSS2005
			String msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10501102|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PIX10501^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||ALPHA^ALAN||19380224|M||WH|1 PINETREE^^WEBSTER^MO^63119|||||||10501-101|||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("10501102", msa.getMessageControlID().getValue());

			//Step 2: PIX Feed another patient in HIMSS2005
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN2_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10501106|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||XYZ10501^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI||ALPHA^ALAN||19380224|M||WH|1 PINETREE LN^^WEBSTER GROVES^MO^63119|||||||10501-201|||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("10501106", msa.getMessageControlID().getValue());
			
			//Step 3: PIX Feed a third patient which can be matched with the first patient
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10501104|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||ABC10501^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||SIMPSON^CARL||19380224|M||BL|5 PINETREE^^WEBSTER^MO^63119|||||||10501-102|||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("10501104", msa.getMessageControlID().getValue());
			
            //Step 4: PIX Query Search, and found one matched patient		
			//Request:
 			msg = "MSH|^~\\&|MESA_PIX_CLIENT|MESA_DEPARTMENT|MESA_XREF|XYZ_HOSPITAL|200603121200||QBP^Q23|10501108|P|2.5||||||||\r" + 
		      "QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10501108|PIX10501^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI|||||\r" +
		      "RCP|I||||||";
			//Response:			
//			MSH|^~\&|MESA_XREF|XYZ_HOSPITAL|MESA_PIX_CLIENT|MESA_DEPARTMENT|||RSP^K23|MESA4954190f|P|2.5||||||||
//			MSA|AA|10501108||||
//			QAK|QRY10501108|OK
//			QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10501108|PIX10501^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI|||||
//			PID|||XYZ10501^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI|| ||||||||||||||||||||||||||||||||||
			Message request = pipeParser.parse(msg);
			response = initiator.sendAndReceive(request);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			ca.uhn.hl7v2.model.v25.segment.MSA msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
			assertEquals("AA", msa25.getAcknowledgmentCode().getValue());
			assertEquals("10501108", msa25.getMessageControlID().getValue());
			ca.uhn.hl7v2.model.v25.segment.QAK qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			assertEquals("QRY10501108", qak.getQueryTag().getValue());
			RSP_K23_QUERY_RESPONSE qrs = ((RSP_K23)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("XYZ10501", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals("XREF2005", pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue());
			assertEquals("1.3.6.1.4.1.21367.2005.1.2", pid.getPatientIdentifierList(0).getAssigningAuthority().getUniversalID().getValue());

			//Step 5: PIX Query Search, and found no matched patient		
			//Request:
 			msg = "MSH|^~\\&|MESA_PIX_CLIENT|MESA_DEPARTMENT|MESA_XREF|XYZ_HOSPITAL|200603121200||QBP^Q23|10501110|P|2.5||||||||\r" + 
		      "QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10501110|ABC10501^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI|||||\r" +
		      "RCP|I||||||";
			//Response:
//			MSH|^~\&|MESA_XREF|XYZ_HOSPITAL|MESA_PIX_CLIENT|MESA_DEPARTMENT|||RSP^K23|MESA49541943|P|2.5||||||||
//			MSA|AA|10501110||||
//			QAK|QRY10501110|NF
//			QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10501110|ABC10501^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI|||||
			request = pipeParser.parse(msg);
			response = initiator.sendAndReceive(request);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
			assertEquals("AA", msa25.getAcknowledgmentCode().getValue());
			assertEquals("10501110", msa25.getMessageControlID().getValue());
			qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
			assertEquals("NF", qak.getQueryResponseStatus().getValue());
			assertEquals("QRY10501110", qak.getQueryTag().getValue());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PIX Mesa 10501 PIX Feed and Query");
		}
	   
   }
   

}
