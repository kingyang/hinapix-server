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
import ca.uhn.hl7v2.model.v25.segment.DSC;
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
public class PdqPaginationTest extends AbstractPixPdqTestCase {

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
	 * Tests PDQ Pagination. There are total 8 matching records in the DB. The test message
	 * came from the PdqMesa Partial ID search but with modified record 
	 * number. The first query fetches 3 records, the second query fetches
	 * 4 records, and the last query 1 record.
	 */
	public void testPaginationSearch() {
		try {
			// PDQ Request Message: fetch the first 3
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110p1|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11335110|@PID.3.1^PDQ113*|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
		      "RCP|I|3^RD|||||";
			PipeParser pipeParser = new HL7Parser();
			Message pdq = pipeParser.parse(msg);
			
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(pdq);
			String responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			QAK qak = (QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			assertEquals("8", qak.getHitCount().getValue());
			assertEquals("3", qak.getThisPayload().getValue());
			assertEquals("5", qak.getHitsRemaining().getValue());
			RSP_K21_QUERY_RESPONSE qrs = ((RSP_K21)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("PDQ113XX01", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals(3, ((RSP_K21)response).getQUERY_RESPONSEReps());
			DSC dsc = (DSC)response.get("DSC");
			
			//Continued PDQ Request Message: fetch another 4 
			msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110p2|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11335110|@PID.3.1^PDQ113*|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
		      "RCP|I|4^RD|||||\r" + 
			  "DSC|" + dsc.getContinuationPointer() + "|I";
			pdq = pipeParser.parse(msg);
			
			response = initiator.sendAndReceive(pdq);
			responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			qak = (QAK)response.get("QAK");
			assertEquals("8", qak.getHitCount().getValue());
			assertEquals("4", qak.getThisPayload().getValue());
			assertEquals("1", qak.getHitsRemaining().getValue());
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			qrs = ((RSP_K21)response).getQUERY_RESPONSE();
			pid = qrs.getPID();
			assertEquals("PDQ113XX04", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals(4, ((RSP_K21)response).getQUERY_RESPONSEReps());
			dsc = (DSC)response.get("DSC");
			
			//The third PDQ Request Message: fetch the last 4, but one remaining 
			msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110p3|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11335110|@PID.3.1^PDQ113*|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
		      "RCP|I|4^RD|||||\r" + 
			  "DSC|" + dsc.getContinuationPointer() + "|I";
			pdq = pipeParser.parse(msg);
			
			response = initiator.sendAndReceive(pdq);
			responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			qak = (QAK)response.get("QAK");
			assertEquals("8", qak.getHitCount().getValue());
			assertEquals("1", qak.getThisPayload().getValue());
			assertEquals("0", qak.getHitsRemaining().getValue());
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			qrs = ((RSP_K21)response).getQUERY_RESPONSE();
			pid = qrs.getPID();
			assertEquals("PDQ113XX13", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals(1, ((RSP_K21)response).getQUERY_RESPONSEReps());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ pagination search");			
		}	
	}

	/**
	 * Tests PDQ Cancel Query. There are total 8 matching records in the DB. The test message
	 * came from the PdqMesa Partial ID search but with modified record 
	 * number. The first query fetches 3 records, then cancel query. And PDQ 
	 * fetch again would fail.
	 */
	public void testPDQCancelQuery() {
		try {
			// PDQ Request Message: fetch the first 3
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110c1|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11335110|@PID.3.1^PDQ113*|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
		      "RCP|I|3^RD|||||";
			PipeParser pipeParser = new HL7Parser();
			Message pdq = pipeParser.parse(msg);
			
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(pdq);
			String responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			QAK qak = (QAK)response.get("QAK");
			assertEquals("OK", qak.getQueryResponseStatus().getValue());
			assertEquals("8", qak.getHitCount().getValue());
			assertEquals("3", qak.getThisPayload().getValue());
			assertEquals("5", qak.getHitsRemaining().getValue());
			RSP_K21_QUERY_RESPONSE qrs = ((RSP_K21)response).getQUERY_RESPONSE();
			PID pid = qrs.getPID();
			assertEquals("PDQ113XX01", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals(3, ((RSP_K21)response).getQUERY_RESPONSEReps());
			DSC dsc = (DSC)response.get("DSC");
			
			//Cancel PDQ Query 
			msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QCN^J01|11311110c2|P|2.5||||||||\r" + 
		      "QID|QRY11335110|IHE PDQ Query";
			pdq = pipeParser.parse(msg);
			
			response = initiator.sendAndReceive(pdq);
			responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			
			//Send PDQ Query again. It should receives an error message  
			msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110c3|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11335110|@PID.3.1^PDQ113*|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
		      "RCP|I|4^RD|||||\r" + 
			  "DSC|" + dsc.getContinuationPointer() + "|I";
			pdq = pipeParser.parse(msg);
			
			response = initiator.sendAndReceive(pdq);
			responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AE", msa.getAcknowledgmentCode().getValue());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ cancel query");			
		}	
	}

}
