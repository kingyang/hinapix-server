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
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.parser.PipeParser;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7Parser;

/**
 * Test PIX Manager transaction: PIX Query for patient with 
 * unrecognized domain.
 * 
 * The sample came from Mesa tests 10503.
 * 
 * @author Wenzhi Li
 * @version 1.0, Jan 22, 2009
 */
public class PixMesa10503Test extends AbstractPixPdqTestCase {

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

   public void testPixMesa10503() {
		try {
			//Step 1: Pix Query a patient whose domain is not registered
			//Request:
			String msg = "MSH|^~\\&|MESA_PIX_CLIENT|MESA_DEPARTMENT|MESA_XREF|XYZ_HOSPITAL|200603121200||QBP^Q23|10503102|P|2.5||||||||\r" + 
		      "QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10503102|ABC10503^^^XXXX|||||\r" +
		      "RCP|I||||||";
			//Response:	
//			MSH|^~\&|MESA_XREF|XYZ_HOSPITAL|MESA_PIX_CLIENT|MESA_DEPARTMENT|20090124004120-0500||RSP^K23|PIXPDQ_348817|P|2.5
//			MSA|AE|10503102
//			ERR||QPD^1^3^1^4|204^Unknown Key Identifier|E
//			QAK|QRY10503102|AE
//			QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10503102|ABC10503^^^XXXX
			PipeParser pipeParser = new HL7Parser();
			Message request = pipeParser.parse(msg);
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(request);
			String responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			ca.uhn.hl7v2.model.v25.segment.MSA msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
			assertEquals("AE", msa25.getAcknowledgmentCode().getValue());
			assertEquals("10503102", msa25.getMessageControlID().getValue());
			ca.uhn.hl7v2.model.v25.segment.QAK qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
			assertEquals("AE", qak.getQueryResponseStatus().getValue());
			assertEquals("QRY10503102", qak.getQueryTag().getValue());
			ERR err = (ERR)response.get("ERR");
			assertEquals("3", err.getErrorLocation(0).getFieldPosition().getValue());
			assertEquals("4", err.getErrorLocation(0).getComponentNumber().getValue());
			assertEquals("204", err.getHL7ErrorCode().getIdentifier().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PIX Mesa 10503 PIX Query for patient with unrecognized domain");
		}
	   
   }
   

}
