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
import junit.framework.TestCase;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.RSP_K21_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v25.message.RSP_K21;
import ca.uhn.hl7v2.model.v25.segment.MSA;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.QAK;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Test PDQ transaction with PDQ server. The sample came from
 * Mesa tests 11311 through 11350.
 * 
 * @author Wenzhi Li
 * @version 1.0, Jan 22, 2009
 */
public class PdqMesa113xxTest extends AbstractPixPdqTestCase {

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
	 * Test PDQ MESA: Exact Name Search (found one exact matching patient)
	 *    PID.5.1.1 = MOORE (Last Name)
     *    PID.5.2 = CHIP   (First Name)
	 */
	public void testExactNameSearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11311110|@PID.5.1.1^MOORE~@PID.5.2^CHIP|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
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
			fail("Fail to test PDQ exact name search");
		}
	}
	/**
	 * Test PDQ MESA: Exact Name Search (found one exact matching patient)
	 *    PID.5.1.1 = MOORE (Last Name)
     *    PID.5.2 = CHIP    (First Name)
     *    PID.8 = M         (Sex)
     *    PID.7.1 = 19380224(DOB)
	 */
	public void testLastNameFirstNameSexDobSearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11311110|@PID.5.1.1^MOORE~@PID.5.2^CHIP~@PID.8^M~@PID.7.1^19380224|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
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
			fail("Fail to test PDQ exact name search");
		}
	}	
	/**
	 * Test PDQ: Exact Name Search (found zero matching patient with the given return domain).
	 * This test is exactly the same as the above one. Instead of finding one matching patient,
	 * it will get zero patient.
	 * 
	 *    PID.5.1.1 = MOORE (Last Name)
     *    PID.5.2 = CHIP   (First Name)
	 */
	public void testNoReturnDomainSearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11311110|@PID.5.1.1^MOORE~@PID.5.2^CHIP|||||^^^XREF2005\r" +
		      "RCP|I|10^RD|||||";
//Response:	
//			MSH|^~\&|MESA_PD_SUPPLIER|XYZ_HOSPITAL|MESA_PD_CONSUMER|MESA_DEPARTMENT|20090126095629-0500||RSP^K22|OpenPIXPDQ10.100.210.127:350614|P|2.5
//			MSA|AA|11311110
//			QAK|QRY11311110|NF
//			QPD|IHE PDQ Query|QRY11311110|@PID.5.1.1^MOORE~@PID.5.2^CHIP|||||^^^XREF2005
			
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			//System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgmentCode().getValue());
			QAK qak = (QAK)response.get("QAK");
			assertEquals("NF", qak.getQueryResponseStatus().getValue());

		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ exact name search");
		}
	}	
	/**
	 * Test PDQ MESA: Exact Name Search (found no matching patient)
	 *    PID.5.1.1 = ZEBRA  (Last Name)
	 */
	public void testExactNameNotFoundSearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11312110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11312110|@PID.5.1.1^ZEBRA|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
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
			assertEquals("NF", qak.getQueryResponseStatus().getValue());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ exact name search");
		}
	}

	
	/**
	 * Test PDQ MESA: Partial Name Search (find one exact matching patient)
	 *    PID.5.1.1 = MOO*  (Last Name)
	 */
	public void testPartialNameSearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11315110|@PID.5.1.1^MOO*|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
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
			assertEquals(4, ((RSP_K21)response).getQUERY_RESPONSEReps());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ partial name search");			
		}
	}

	/**
	 * Test PDQ MESA: Complete ID Search - Unspecified Domain (found one exact matching patient)
	 *    PID.3.1 = PDQ113XX05  (Patient ID)
	 */
	public void testCompleteIDSearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11320110|@PID.3.1^PDQ113XX05|||||\r" +
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
			assertEquals("PDQ113XX05", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals("MOONEY", pid.getPatientName(0).getFamilyName().getSurname().getValue());
			assertEquals("STAN", pid.getPatientName(0).getGivenName().getValue());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ complete ID search");			
		}	
	}

	/**
	 * Test PDQ MESA: Complete ID Search - Single Domain
     * (found one exact matching patient)
	 *    PID.3.1 = PDQ113XX02    (Patient ID) 
	 *    domain = HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO
     *   
	 */
	public void testCompleteIDSearchSingleDomain() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11325110|@PID.3.1^PDQ113XX02|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
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
			assertEquals("PDQ113XX02", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals("HIMSS2005", pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue());
			assertEquals("1.3.6.1.4.1.21367.2005.1.1", pid.getPatientIdentifierList(0).getAssigningAuthority().getUniversalID().getValue());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ Complete ID Search");			
		}	
	}

	/**
	 * Test PDQ MESA: Partial ID Search - Single Domain (Should return all 8 patients)
	 *    PID.3.1^PDQ113*    (Patient ID)
	 */
	public void testPartialIDSearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11311110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11335110|@PID.3.1^PDQ113*|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
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
			assertEquals("PDQ113XX01", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals(8, ((RSP_K21)response).getQUERY_RESPONSEReps());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ partial ID search");			
		}	
	}

	/**
	 * Test PDQ MESA: Multi Key Search (found one exact matching patient)
	 *    PID.5.1.1 = MOORE  (Last Name)
	 *    PID.7.1 = 19380224 (DOB)
	 */
	public void testMultiKeySearch() {
		try {
			// PDQ Request Message: 
			String msg = "MSH|^~\\&|MESA_PD_CONSUMER|MESA_DEPARTMENT|MESA_PD_SUPPLIER|XYZ_HOSPITAL|||QBP^Q22|11350110|P|2.5||||||||\r" + 
		      "QPD|IHE PDQ Query|QRY11350110|@PID.5.1.1^MOORE~@PID.7.1^19380224|||||^^^HIMSS2005&1.3.6.1.4.1.21367.2005.1.1&ISO\r" +
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
			assertEquals("PDQ113XX01", pid.getPatientIdentifierList(0).getIDNumber().getValue());
			assertEquals("MOORE", pid.getPatientName(0).getFamilyName().getSurname().getValue());
			assertEquals("19380224", pid.getDateTimeOfBirth().getTime().getValue());
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PDQ multi key search");			
		}	
	}

}
