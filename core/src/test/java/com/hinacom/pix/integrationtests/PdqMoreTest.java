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
import ca.uhn.hl7v2.model.v25.group.RSP_K21_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v25.message.RSP_K21;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.QAK;
import ca.uhn.hl7v2.parser.PipeParser;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7Parser;

/**
 * Run PdqLoadPatientTest.java before execute this unit test.
 * Test PDQ transaction with PDQ server.
 * 
 * @author Wenzhi Li
 * @version 1.0, Jan 22, 2009
 */
public class PdqMoreTest extends AbstractPixPdqTestCase {

    /* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		createPDQConnection();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
    
	/**
	 * Test PDQ: No search parameter is defined. Should return an error message.
	 */
	public void testNoSearchParameter() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|EHR_MCKESSON|MCEH|Allscripts_PIX|Allscripts_PIX|20090211010237-0800||QBP^Q22^QBP_Q21|6581348296922864261|P|2.5\r"+
			"QPD|QRY_PDQ_1001^Query By Name^IHEDEMO|0204095263131119970058421103832||||||^^^&1.3.6.1.4.1.21367.2009.1.2.300&ISO\r" +
			"RCP|I|10^RD";
			
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AE", msa.getAcknowledgmentCode().getValue());
			QAK qak = (QAK)response.get("QAK");
			assertEquals("AE", qak.getQueryResponseStatus().getValue());
			ERR err = (ERR)response.get("ERR");
			assertEquals("QPD", err.getErrorLocation(0).getSegmentID().getValue());
			assertEquals("3", err.getErrorLocation(0).getFieldPosition().getValue());
			assertEquals("102", err.getHL7ErrorCode().getIdentifier().getValue());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ empty parameter search");
		}
	}

	/**
	 * Test PDQ: Exact First Name Search (found one exact matching patient)
     *    PID.5.2 = CHIP   (First Name)
	 */
	public void testFirstNameSearch() {
		try {
			// PDQ Request Message: 
//			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
//		      "QPD|IHE PDQ Query|QRY11311110|@PID.5.2^CHIP|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
//		      "RCP|I|10^RD|||||";
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11311110|@PID.5.2^CHIP|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
		      "RCP|I|10^RD|||||";
	
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			QAK qak = (QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			RSP_K21_QUERY_RESPONSE qrs = ((RSP_K21)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("MOORE", pid.getPatientName(0).getFamilyName().getSurname().getValue());
			assertEquals("CHIP", pid.getPatientName(0).getGivenName().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ first name search");
		}
	}

	/**
	 * Test PDQ: Exact Last Name and Address Line1 Search (found one exact matching patient)
     *    PID.5.1 = MOORE   (Last Name)
     *    PID.11.1 = 10 PINETREE  (Address Line1)
	 */
	public void testLastNameAddressLine1Search() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11311110|@PID.5.1^MOORE~@PID.11.1^10 PINETREE|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
		      "RCP|I|10^RD|||||";
	
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			QAK qak = (QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			RSP_K21_QUERY_RESPONSE qrs = ((RSP_K21)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("MOORE", pid.getPatientName(0).getFamilyName().getSurname().getValue());
			assertEquals("CHIP", pid.getPatientName(0).getGivenName().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ last name and address line1 search");
		}
	}
	/**
	 * Test PDQ: Exact Last Name and Address Line1 Search (found one exact matching patient)
     *    PID.5.1.1 = MOORE   (Last Name)
     *    PID.11.1.1^10 PINETREE  (Address Line1)
	 */
	public void testLastNameStreetLine1Search2() {
		try {
//			MSH|^~\&|PAT_IDENTITY_X_REF_MGR_MISYS_TLS|ALLSCRIPTS|OTHER_IBM_BRIDGE_TLS|IBM|20090226141540-0500||RSP^K22|OpenPIXPDQ10.243.0.65.19770811854243|P|2.5
//			MSA|AA|6880881378099874844
//			QAK|6058651775104617438922166242613|OK||4|4|0
//			QPD|Q22^Find Candidates^HL7|6058651775104617438922166242613|@PID.5.1.1^MOORE~@PID.11.1.1^10 PINETREE
		
//			MSH|^~\&|OTHER_IBM_BRIDGE_TLS|IBM|PAT_IDENTITY_X_REF_MGR_MISYS|ALLSCRIPTS|20090226131543-0600||QBP^Q22^QBP_Q21|6880881378099874844|P|2.5
//			QPD|Q22^Find Candidates^HL7|6058651775104617438922166242613|@PID.5.1.1^MOORE~@PID.11.1.1^10 PINETREE
//			RCP|I|10^RD			
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|OTHER_IBM_BRIDGE_TLS|IBM|PAT_IDENTITY_X_REF_MGR_MISYS|ALLSCRIPTS|20090226131543-0600||QBP^Q22^QBP_Q21|6880881378099874844|P|2.5\r" + 
		      "QPD|Q22^Find Candidates^HL7|6058651775104617438922166242613|@PID.5.1.1^MOORE~@PID.11.1.1^10 PINETREE\r" +
		      "RCP|I|10^RD";
	
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			QAK qak = (QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			RSP_K21_QUERY_RESPONSE qrs = ((RSP_K21)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("MOORE", pid.getPatientName(0).getFamilyName().getSurname().getValue());
			assertEquals("CHIP", pid.getPatientName(0).getGivenName().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ last name and AddressLine1 search");
		}
	}	

	
	/**
	 * Test PDQ: Exact Last Name and Address Line1 Search (found one exact matching patient)
     *    PID.5.1 = MO*   (Last Name)
     *    PID.8 = F (Sex)
	 */
	public void testPartialLastNameSexSearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11311110|@PID.5.1^MO*~@PID.8^F|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
		      "RCP|I|10^RD|||||";
	
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			QAK qak = (QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			RSP_K21_QUERY_RESPONSE qrs = ((RSP_K21)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("MOHR", pid.getPatientName(0).getFamilyName().getSurname().getValue());
			assertEquals("ALICE", pid.getPatientName(0).getGivenName().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ partial last name and sex search");
		}
	}

}
