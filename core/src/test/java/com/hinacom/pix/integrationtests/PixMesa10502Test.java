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
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.parser.PipeParser;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7Parser;

/**
 * Test PIX Manager transactions: PIX Feed and PIX Query for
 * an unregistered Patient.
 * 
 * The sample came from Mesa tests 10502.
 * 
 * @author Wenzhi Li
 * @version 1.0, Jan 22, 2009
 */
public class PixMesa10502Test extends AbstractPixPdqTestCase {

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

   public void testPixMesa10502() {
		try {
			//Step 1: PIX feed to create one patient
			String msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10502102|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||PIX10502^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||BETA^BETTY||19570131|F||AI|2815 JORIE BLVD^^OAK BROOK^IL^60523|||||||10502-101|||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("10502102", msa.getMessageControlID().getValue());

			//Step 2: PIX feed to create another patient
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A04^ADT_A01|10502104|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||XYZ10502^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI||CROSS^KEN||19801223|M||WH|10034 CLAYTON RD^^LADUE^MO^63124|||||||10502-201|||||||||||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("10502104", msa.getMessageControlID().getValue());
			
			//Step 3: Pix Query a patient which is not registered previously
			//Request:
 			msg = "MSH|^~\\&|MESA_PIX_CLIENT|MESA_DEPARTMENT|MESA_XREF|XYZ_HOSPITAL|200603121200||QBP^Q23|10502106|P|2.5||||||||\r" + 
		      "QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10502106|ABC10502^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI|||||\r" +
		      "RCP|I||||||";
			//Response:	
// 			MSH|^~\&|MESA_XREF|XYZ_HOSPITAL|MESA_PIX_CLIENT|MESA_DEPARTMENT|20090123232148-0500||RSP^K23|PIXPDQ_348549|P|2.5
// 			MSA|AE|10502106
// 			ERR||QPD^1^3^1^1|204^Unknown Key Identifier|E
// 			QAK|QRY10502106|AE
// 			QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10502106|ABC10502^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI
			Message request = pipeParser.parse(msg);
			response = initiator.sendAndReceive(request);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			ca.uhn.hl7v2.model.v25.segment.MSA msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
			assertEquals("AE", msa25.getAcknowledgmentCode().getValue());
			assertEquals("10502106", msa25.getMessageControlID().getValue());
			ca.uhn.hl7v2.model.v25.segment.QAK qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
			assertEquals("AE", qak.getQueryResponseStatus().getValue());
			assertEquals("QRY10502106", qak.getQueryTag().getValue());
			ERR err = (ERR)response.get("ERR");
			assertEquals("3", err.getErrorLocation(0).getFieldPosition().getValue());
			assertEquals("1", err.getErrorLocation(0).getComponentNumber().getValue());
			assertEquals("204", err.getHL7ErrorCode().getIdentifier().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PIX Mesa 10502 PIX Feed and Query for unregistered patient.");
		}
	   
   }
   

}
