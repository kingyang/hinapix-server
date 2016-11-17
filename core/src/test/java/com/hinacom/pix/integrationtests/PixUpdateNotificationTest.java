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
 * Test PIX Manager Update Notification Transactions. 
 * 
 * The sample came from Connectathon 2009.
 * 
 * @author Wenzhi Li
 * @version 1.0, Feb 23, 2009
 */
public class PixUpdateNotificationTest extends AbstractPixPdqTestCase {

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
	 * Before running this test, you need to configure at least one PIX Consumer
	 * that accept PIX Update Notification. See relevant configuration docs. 
	 * 
	 * This test case tests PIX Update Notification:
	 * <p>1. Create a patient in master (global) domain. ANDERS^MARIAN, 1944.04.04, Female, Addr=444 Main St., Seattle, WA, 36013.
	 * This patient is admitted with the Master Assigning Authority value. 
	 * </p> 
	 * <p>2. The Update Notification is sent from the PIX Mgr to the PIX Consumer. It contains the Master Assigning Authority.  
	 * </p> 
	 * <p>3. Admit the same patient at a different local domain. ANDERS^MARIAN, 1944.04.04, Female, Addr=444 Main St., Seattle, WA, 36013.
	 * This patient is admitted with the Local Assigning Authority value of the PIX Source. 
	 * </p> 
	 * <p>4. The PIX Manager cross-references patient Marion Anders. The Update Notification is sent from the PIX Mgr to the PIX Consumer.
	 * It contains ids from both the Local Assigning Authority of the PIX Source and the Master Assinging Authority.  
	 * </p> 
	 * <p>5. Note - the following steps are optional. Although they follow the scenario in the Technical Framework, not all PIX Managers will unlink the patients because of the address change. </br> 
	 *  On the Local Patient Identity source, change Marian Ander's address to 111 New Street, Portland, Oregon. 
	 * The PIX Mgr determines this patient is in the local domain is no longer the same patient as that in the master 
	 * affinity domain. It sends an update notification to the Consumer with Marian Anders' patient id from the Local 
	 * Assigning Authority.
	 * PIX Manager sends a second update notification with Marian Anders' patient id from the Master Assigning Authority. 
	 *  
	 * </p>
	 */
	public void testPixUpdateNotification() {
		try {
			//Step 1: PIX Feed ANDERS with Global (Master Patient Id
			String msg = "MSH|^~\\&|XDSDEMO_ADT|XDSDEMO|PAT_IDENTITY_X_REF_MGR_MISYSPLC|ALLSCRIPTS|200901271417||ADT^A04^ADT_A01|00000688|P|2.3.1||||||||\r" + 
		      "EVN||200901271417||||200901271417\r" +
		      "PID|||465884^^^IHENA&1.3.6.1.4.1.21367.2009.1.2.300&ISO||ANDERS^MARIAN||19440404|F|||444 Main St.^^Seattle^WA^36013^USA|||||||463423||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			PipeParser pipeParser = new HL7Parser();
			Message adt = pipeParser.parse(msg);
			Initiator initiator = connection.getInitiator();
			Message response = initiator.sendAndReceive(adt);
			String responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			MSA msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("00000688", msa.getMessageControlID().getValue());
	
			//Wait for five seconds
			Thread.sleep(5000);
			
			//Step 2: PIX Feed the same patient with Local patient id.
			msg = "MSH|^~\\&|XDSDEMO_ADT|XDSDEMO|PAT_IDENTITY_X_REF_MGR_MISYSPLC|ALLSCRIPTS|200901271417||ADT^A04^ADT_A01|00000738|P|2.3.1||||||||\r" + 
		      "EVN||200901271417||||200901271417\r" +
		      "PID|||465885^^^IHELOCAL&1.3.6.1.4.1.21367.2009.1.2.310&ISO||ANDERS^MARIAN||19440404|F|||444 Main St.^^Seattle^WA^36013^USA|||||||463423||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			assertEquals("00000738", msa.getMessageControlID().getValue());

			Thread.sleep(5000);
			//Step 3: PIX Update the local ANDERS 
			msg = "MSH|^~\\&|MESA_ADT|DOMAIN1_ADMITTING|MESA_XREF|XYZ_HOSPITAL|200310011100||ADT^A08|10506104|P|2.3.1||||||||\r" + 
		      "EVN||200310011100||||200310011043\r" +
		      "PID|||465885^^^IHELOCAL&1.3.6.1.4.1.21367.2009.1.2.310&ISO||ANDERS^MARIAN||19440404|F|||111 New Street^^Portland^OR^37765^USA|||||||463423||||||||||||\r" +
		      "PV1||O||||||||||||||||||||||||||||||||||||||||||||||||||";
			adt = pipeParser.parse(msg);
			response = initiator.sendAndReceive(adt);
			responseString = pipeParser.encode(response);	        
			System.out.println("Received response:\n" + responseString);
			msa = (MSA)response.get("MSA");
			assertEquals("AA", msa.getAcknowledgementCode().getValue());
			
		}catch(Exception e) {
			e.printStackTrace();
			fail("Fail to test PIX");
		}

	}


}
