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
public class PixQueryTest extends AbstractPixPdqTestCase {

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
	 * This one tests both Global Patient and Local patient (the same logic patient with
	 * the same patient demographics) can be saved. Also, giving Global patient ID, the PIX query
	 * should find the Local patient Id, and vice versa.
	 */
	public void testGloadAndLocalPIXFeed() {
		try {
			//Step 1: PIX Feed one patient with Global Patient Id
			String msg = "MSH|^~\\&|XDSDEMO_ADT|XDSDEMO|PAT_IDENTITY_X_REF_MGR_MISYSPLC|ALLSCRIPTS|200901271417||ADT^A04^ADT_A01|00000669|P|2.3.1||||||||\r" + 
		      "EVN||200901271417||||200901271417\r" +
		      "PID|||463423^^^IHENA&1.3.6.1.4.1.21367.2009.1.2.300&ISO||Foo^May||19821223|F|||75 Rooselvelt Ave^^Atlanta^GA^30322^USA||^^^may.foo@hotmail.com|||||463423||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("00000669", msa.getMessageControlID().getValue());
	
			//Step 2: PIX Feed the same patient with Local patient id.
			msg = "MSH|^~\\&|XDSDEMO_ADT|XDSDEMO|PAT_IDENTITY_X_REF_MGR_MISYSPLC|ALLSCRIPTS|200901271417||ADT^A04^ADT_A01|00000732|P|2.3.1||||||||\r" + 
		      "EVN||200901271417||||200901271417\r" +
		      "PID|||189367602^^^SAEH&1.3.6.1.4.1.21367.2009.1.2.400&ISO||Foo^May||19821223|F|||75 Rooselvelt Ave^^Atlanta^GA^30322^USA||^^^may.foo@hotmail.com|||||189367602||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("00000732", msa.getMessageControlID().getValue());

            //Step 3: PIX Query Search, giving the local ID, should find the global id.		
			//Request:
 			msg = "MSH|^~\\&|IHESAMPLEAPP|SAMPLEFAC|PAT_IDENTITY_X_REF_MGR_MISYSPLC|ALLSCRIPTS|20090127083847-0800||QBP^Q23^QBP_Q21|9375798387190401682|P|2.5\r" + 
		      "QPD|Q23^Get Corresponding IDs^HL7nnnn|0028859687442070744361964623637|189367602^^^&1.3.6.1.4.1.21367.2009.1.2.400&ISO\r" +
		      "RCP|I";
			Message request = pipeParser.parse(msg);
		    response = initiator.sendAndReceive(request);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			ca.uhn.hl7v2.model.v25.segment.MSA msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
			assertEquals("AA", msa25.getAcknowledgmentCode().getValue());
			assertEquals("9375798387190401682", msa25.getMessageControlID().getValue());
			ca.uhn.hl7v2.model.v25.segment.QAK qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			assertEquals("0028859687442070744361964623637", qak.getQueryTag().getValue());
			RSP_K23_QUERY_RESPONSE qrs = ((RSP_K23)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("463423", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals("IHENA", pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue());
			assertEquals("1.3.6.1.4.1.21367.2009.1.2.300", pid.getPatientIdentifierList(0).getAssigningAuthority().getUniversalID().getValue());
			
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PIX");
		}

	}

//   public void testPixMesa10501() {
//		try {
//            //Step 1: PIX Query Search, and found one matched patient		
//			//Request:
// 			String msg = "MSH|^~\\&|IHESAMPLEAPP|SAMPLEFAC|PAT_IDENTITY_X_REF_MGR_INITIATE|INITIATE|20090127083847-0800||QBP^Q23^QBP_Q21|9375798387190401682|P|2.5\r" + 
//		      "QPD|Q23^Get Corresponding IDs^HL7nnnn|0028859687442070744361964623637|2009012701^^^&1.3.6.1.4.1.21367.2009.1.2.300&ISO\r" +
//		      "RCP|I";
//			//Response:			
////			MSH|^~\&|MESA_XREF|XYZ_HOSPITAL|MESA_PIX_CLIENT|MESA_DEPARTMENT|||RSP^K23|MESA4954190f|P|2.5||||||||
////			MSA|AA|10501108||||
////			QAK|QRY10501108|OK
////			QPD|QRY_1001^Query for Corresponding Identifiers^IHEDEMO|QRY10501108|PIX10501^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO^PI|||||
////			PID|||XYZ10501^^^XREF2005&1.3.6.1.4.1.21367.2005.1.2&ISO^PI|| ||||||||||||||||||||||||||||||||||
//			PipeParser pipeParser = new PipeParser();			
//			Message request = pipeParser.parse(msg);
//			Initiator initiator = connection.getInitiator();
//			Message response = initiator.sendAndReceive(request);
//			String responseString = pipeParser.encode(response);	        
//			System.out.println("Received response:\n" + responseString);
//			ca.uhn.hl7v2.model.v25.segment.MSA msa25 = (ca.uhn.hl7v2.model.v25.segment.MSA)response.get("MSA");
//			assertEquals("AA", msa25.getAcknowledgmentCode().getValue());
//			assertEquals("10501108", msa25.getMessageControlID().getValue());
//			ca.uhn.hl7v2.model.v25.segment.QAK qak = (ca.uhn.hl7v2.model.v25.segment.QAK)response.get("QAK");
//			assertEquals("OK", qak.getQueryResponseStatus().getValue());
//			assertEquals("QRY10501108", qak.getQueryTag().getValue());
//			RSP_K23_QUERY_RESPONSE qrs = ((RSP_K23)response).getQUERY_RESPONSE();
//			PID pid = qrs.getPID();
//			assertEquals("XYZ10501", pid.getPatientIdentifierList(0).getIDNumber().getValue());
//			assertEquals("XREF2005", pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue());
//			assertEquals("1.3.6.1.4.1.21367.2005.1.2", pid.getPatientIdentifierList(0).getAssigningAuthority().getUniversalID().getValue());
//		}catch(Exception e) {
//			e.printStackTrace();
//			fail("Fail to test PIX Query search");
//		}
//	   
//   }
   

}
