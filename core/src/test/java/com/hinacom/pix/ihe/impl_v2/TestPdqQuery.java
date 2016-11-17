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
package com.hinacom.pix.ihe.impl_v2;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import junit.framework.TestCase;

import com.hinacom.pix.ihe.HL7Actor;
import com.hinacom.pix.ihe.IPdSupplierAdapter;
import com.hinacom.pix.ihe.audit.IheAuditTrail;
import com.hinacom.pix.ihe.configuration.ConfigurationLoader;
import com.hinacom.pix.ihe.log.IMessageStoreLogger;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.ERL;
import ca.uhn.hl7v2.model.v25.group.RSP_K21_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.message.RSP_K21;
import ca.uhn.hl7v2.model.v25.segment.DSC;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;
import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.util.OID;


/**
 * This class is used to testing the PdQueryHandler
 * @author Rasakannu Palaniyandi
 * @version dec 14, 2008
 * 
 
 */

public class TestPdqQuery extends TestCase {
	public static void main(String[] args) {
        TestPdqQuery testRun = new TestPdqQuery();

        testRun.setUp();

        testRun.testUnexpectedRequest();
        testRun.testInvalidPdqQuery();
        testRun.testReceivingApplication();
        testRun.testReceivingFacility();
        testRun.testInvalidRequestedDoamin();
        testRun.testInvalidDataTypeError();
        testRun.testInvalidQueryParameter();
        testRun.testPdQueryHandler();
        testRun.testContinuationPointer();
        testRun.testCancelQuery();
	}


	private IConnectionDescription connection = null;
    private IPdSupplierAdapter pdsAdapter = null;
    private static ConfigurationLoader loader = null;
	private PdSupplier actor = null;

	public void setUp() {
		 
	        try {
	        	loader = ConfigurationLoader.getInstance();
	            URL url = TestPixQuery.class.getResource("/conf/tests/actors/IheActors.xml");
	            File file = new File(url.toURI());
	            loader.loadConfiguration(file, false, null, new OidMock(), null, null, null, new TestLogContext());
	            Collection actors = loader.getActorDescriptions();	            
	            ConfigurationLoader.ActorDescription pixqactor = loader.getDescriptionById("pdsup");
	            connection=pixqactor.getConnection();
	            ConfigurationLoader.ActorDescription auditactor = loader.getDescriptionById("localaudit");
	           
	            IheAuditTrail auditTrail=new IheAuditTrail("pdsup", auditactor.getLogConnection());
	            actor = new PdSupplier(connection, auditTrail);
		    	MockPixAdapter pdAdapter = new MockPixAdapter(); 
	            actor.registerPdSupplierAdapter(pdAdapter);

	            IMessageStoreLogger storeLogger = new MockMessageStoreLogger(); 
	            actor.setStoreLogger(storeLogger);
	        } catch (Exception e) {
	            e.printStackTrace();
	            fail("Cannot load the actor property");
	        }
	}
	
	/**
	 * Test PdQueryHandler: Unexpected request
	 */
	public void testUnexpectedRequest(){
		
	      
		String msh ="MSH|^~\\&|EHR_MISYS|MISYS|PAT_IDENTITY_X_REF_MGR_IBM1|IBM|20060817212747-0400||QBP^Q21^QBP_Q22|PDQ_0|P|2.5\r"+
		"QPD|QRY_PDQ_1001^Query By Name^IHEDEMO|QRY_PDQ_0|@PID.5.1^DEPINTO~@PID.5.2^JOE\r"+
		"RCP|I";
	        PipeParser pipeParser = new PipeParser();
	        try {
	        	Message msgIn = pipeParser.parse(msh);      
	        	PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);
	        	Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	        	fail("processQuery() should've thrown exception");
			} catch (Exception e) {
				 // this is exactly what we were expecting so 
	            // let's just ignore it and let the test pass
		} 
    }
	
	
	/**
	 * Test PdQueryHandler: Invalid PdQueryHandler event.
	 */
	public void testInvalidPdqQuery(){
		
		String msh ="MSH|^~\\&|EHR_MISYS|MISYS|PAT_IDENTITY_X_REF_MGR_IBM1|IBM|20060817212747-0400||QBP^Q21^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|QRY_PDQ_1001^Query By Name^IHEDEMO|QRY_PDQ_0|@PID.5.1^DEPINTO~@PID.5.2^JOE\r"+
		"RCP|I";
	        PipeParser pipeParser = new PipeParser();
	        try {
	        	Message msgIn = pipeParser.parse(msh);
	        	PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	        	Message resposeMsg =pdQueryHandler.processMessage(msgIn);
	        	 RSP_K21 rMessage=getResponseMsg(resposeMsg);
		    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
		    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
		    	  String returnValues=processPixQueryResponse(rMessage);
		    	  String[] errorLog=returnValues.split(",");
		    	  assertEquals("MSH", errorLog[0]);
		    	  assertEquals("1", errorLog[1]);
		    	  assertEquals("9", errorLog[2]);
		    	  assertEquals("1", errorLog[3]);
		    	  assertEquals("2", errorLog[4]);
		    	  assertEquals("201", errorLog[5]);
		    	  assertEquals("Unsupported event code", errorLog[6]);	
			} catch (Exception e) {
				e.printStackTrace();
		} 
    }
	
	/**
	 * Tests PdQueryHandler: ReceivingApplication is not recognized.
	 */
	public void testReceivingApplication(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|HOSPMPI|HOSP|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|QRY_PDQ_1001^Query By Name^IHEDEMO|QRY_PDQ_0|@PID.5.1^DEPINTO~@PID.5.2^JOE\r"+
		"RCP|I";
	       PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);  	
	    	  PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	          Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          RSP_K21 rMessage=getResponseMsg(resposeMsg);
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("MSH", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("5", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  assertEquals("1", errorLog[4]);
	    	  assertEquals("204", errorLog[5]);
	    	  assertEquals("Unknown Receiving Application", errorLog[6]);	  
	    	  
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}

	/**
	 * Tests PdQueryHandler: Receiving Facility is not recognized.
	 */
	public void testReceivingFacility(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|HOSP|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|QRY_PDQ_1001^Query By Name^IHEDEMO|QRY_PDQ_0|@PID.5.1^DEPINTO~@PID.5.2^JOE\r"+
		"RCP|I";
	       PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	          Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          RSP_K21 rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);    	  
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("MSH", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("6", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  assertEquals("1", errorLog[4]);
	    	  assertEquals("204", errorLog[5]);
	    	  assertEquals("Unknown Receiving Facility", errorLog[6]);
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	/**
	 * Tests PdQueryHandler: requested domain is not valid
	 */
	public void testInvalidRequestedDoamin(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|QRY_PDQ_1001^Query By Name^IHEDEMO|QRY_PDQ_0|@PID.5.1^DEPINTO~@PID.5.2^JOE|||||^^^WESTCLINIC~^^^SOUTH LAB\r"+
		"RCP|I";
	       PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	          Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          RSP_K21 rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);    	  
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("QPD", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("8", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  assertEquals("null", errorLog[4]);
	    	  assertEquals("204", errorLog[5]);
	    	  assertEquals("Unknown Key Identifier", errorLog[6]);
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	/**
	 * Tests PdQueryHandler: Data type error for requested domain QPD-8
	 */
	public void testInvalidDataTypeError(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|QRY_PDQ_1001^Query By Name^IHEDEMO|QRY_PDQ_0|@PID.5.1^DEPINTO~@PID.5.2^JOE|||||~!@#$%^%&^^*^*&&(()&|\r"+
		"RCP|I";
	       PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	          Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          RSP_K21 rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);    	  
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("QPD", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("8", errorLog[2]);	   
	    	  assertEquals("null", errorLog[3]);
	    	  assertEquals("null", errorLog[4]);
	    	  assertEquals("102", errorLog[5]);
	    	  assertEquals("Data type error", errorLog[6]);
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	/**
	 * Tests PdQueryHandler: Invalid query parameter QPD-3 
	 */
	public void testInvalidQueryParameter(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|QRY_PDQ_1001^Query By Name^IHEDEMO|QRY_PDQ_0|&PID.5.1&DEPINTO@PID.5.2&JOE|||||^^^CPR^^^\r"+
		"RCP|I";
	    PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	          Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          RSP_K21 rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);    	  
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("QPD", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("3", errorLog[2]);
	    	  assertEquals("null", errorLog[3]);
	    	  assertEquals("null", errorLog[4]);
	    	  assertEquals("102", errorLog[5]);
	    	  assertEquals("Data type error", errorLog[6]);
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	/**
	 * Tests PdQueryHandler: test the continuation pointer  
	 */
	public void testPdQueryHandler(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|Q22^Find Candidates^HL7nnn|111069|@PID.5.1^SMITH~@PID.5.2^JOHN~@PID.8^M|80|MATCHWARE|1.2||^^^CPR^^^|\r"+
		"RCP|I|1^RD";
		 PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	          Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          RSP_K21 rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AA",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("OK",rMessage.getQAK().getQueryResponseStatus().getValue()); 	
	    	  RSP_K21_QUERY_RESPONSE qr = rMessage.getQUERY_RESPONSE(0);
	    	  PID pid = qr.getPID(); 
	    	  assertEquals(pid.getPatientName(0).getPrefixEgDR().getValue(),"M");
	    	  assertEquals(pid.getPatientName(0).getSuffixEgJRorIII().getValue(),"R");
	    	  assertEquals(pid.getPatientName(0).getGivenName().getValue(),"jhon");
	    	  assertEquals(pid.getPatientName(0).getFamilyName().getSurname().getValue(),"smith");
	    	  assertEquals(pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue(),"CPR");
	    	  assertEquals(pid.getPatientIdentifierList(0).getIDNumber().getValue(),"100101");
	    	
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	
	
	/**
	 * Tests PdQueryHandler: Test the continuation pointer if request record is less than retrieved patients
	 */
	public void testContinuationPointer(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|Q22^Find Candidates^HL7nnn|111069|@PID.5.1^SMITH~@PID.5.2^JOHN~@PID.8^M|80|MATCHWARE|1.2||^^^CPR^^^|\r"+
		"RCP|I|1^RD";
		 PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	          Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          RSP_K21 rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AA",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("OK",rMessage.getQAK().getQueryResponseStatus().getValue()); 	
	    	 RSP_K21_QUERY_RESPONSE qr = rMessage.getQUERY_RESPONSE();
	    	  DSC dsc = rMessage.getDSC();
	    	  String continuationPointer = dsc.getContinuationPointer().getValue();
	    	  PID pid = qr.getPID(); 
	    	  assertEquals(pid.getPatientName(0).getPrefixEgDR().getValue(),"M");
	    	  assertEquals(pid.getPatientName(0).getSuffixEgJRorIII().getValue(),"R");
	    	  assertEquals(pid.getPatientName(0).getGivenName().getValue(),"jhon");
	    	  assertEquals(pid.getPatientName(0).getFamilyName().getSurname().getValue(),"smith");
	    	  assertEquals(pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue(),"CPR");
	    	  assertEquals(pid.getPatientIdentifierList(0).getIDNumber().getValue(),"100101");
	    	  
	    	 //next continuation record 
	    	 String msh2 = "MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
	  		"QPD|Q22^Find Candidates^HL7nnn|111069|@PID.5.1^SMITH~@PID.5.2^JOHN~@PID.8^M|80|MATCHWARE|1.2||^^^CPR^^^|\r"+
	  		"RCP|I|1^RD\r"+
	  		"DSC|"+continuationPointer+"|I";
	    	  msgIn = pipeParser.parse(msh2);   
	    	  pdQueryHandler = new PdQueryHandler(actor);	       
	          resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AA",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("OK",rMessage.getQAK().getQueryResponseStatus().getValue()); 	
	    	  qr = rMessage.getQUERY_RESPONSE();
	    	  pid = qr.getPID(); 
	    	  assertEquals(pid.getPatientName(0).getPrefixEgDR().getValue(),"P");
	    	  assertEquals(pid.getPatientName(0).getSuffixEgJRorIII().getValue(),"S");
	    	  assertEquals(pid.getPatientName(0).getGivenName().getValue(),"vijay");
	    	  assertEquals(pid.getPatientName(0).getFamilyName().getSurname().getValue(),"raja");
	    	  assertEquals(pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue(),"CPR");
	    	  assertEquals(pid.getPatientIdentifierList(0).getIDNumber().getValue(),"100101");
	    	 
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	
	/**
	 * Tests PdQueryHandler: test the continuation pointer with cancel query
	 */
	public void testCancelQuery(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
		"QPD|Q22^Find Candidates^HL7nnn|111069|@PID.5.1^SMITH~@PID.5.2^JOHN~@PID.8^M|80|MATCHWARE|1.2||^^^CPR^^^|\r"+
		"RCP|I|1^RD";
		 PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  PdQueryHandler pdQueryHandler = new PdQueryHandler(actor);	       
	          Message resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          RSP_K21 rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AA",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("OK",rMessage.getQAK().getQueryResponseStatus().getValue()); 	
	    	 RSP_K21_QUERY_RESPONSE qr = rMessage.getQUERY_RESPONSE();
	    	  DSC dsc = rMessage.getDSC();
	    	  String continuationPointer = dsc.getContinuationPointer().getValue();
	    	  PID pid = qr.getPID(); 
	    	  assertEquals(pid.getPatientName(0).getPrefixEgDR().getValue(),"M");
	    	  assertEquals(pid.getPatientName(0).getSuffixEgJRorIII().getValue(),"R");
	    	  assertEquals(pid.getPatientName(0).getGivenName().getValue(),"jhon");
	    	  assertEquals(pid.getPatientName(0).getFamilyName().getSurname().getValue(),"smith");
	    	  assertEquals(pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().getValue(),"CPR");
	    	  assertEquals(pid.getPatientIdentifierList(0).getIDNumber().getValue(),"100101");
	    	  
	    	  // cancel query 
	    	String cancelMsh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QCN^J01^QCN_J01|PDQ_0|P|2.5\r"+
	    	"QID|J01^cancel query|111069";		
	    	 msgIn = pipeParser.parse(cancelMsh);   
	    	 pdQueryHandler = new PdQueryHandler(actor);	 
	    	 ACK resposeMsg1 =(ACK)pdQueryHandler.processMessage(msgIn);	        	
	          ACK rMessage1=getCancelResponseMsg(resposeMsg);   	
	    	  assertEquals("AA",rMessage.getMSA().getAcknowledgmentCode().getValue()); 
	    	  
	    	 //test unknown continuation pointer
	    	 String msh2 = "MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060817212747-0400||QBP^Q22^QBP_Q21|PDQ_0|P|2.5\r"+
	  		"QPD|Q22^Find Candidates^HL7nnn|111069|@PID.5.1^SMITH~@PID.5.2^JOHN~@PID.8^M|80|MATCHWARE|1.2||^^^CPR^^^|\r"+
	  		"RCP|I|1^RD\r"+
	  		"DSC|"+continuationPointer+"|I";
	    	  msgIn = pipeParser.parse(msh2);   
	    	  pdQueryHandler = new PdQueryHandler(actor);	       
	          resposeMsg =pdQueryHandler.processMessage(msgIn);	        	
	          rMessage=getResponseMsg(resposeMsg);	    	  
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue()); 	
	    	  String returnValues=processPixQueryResponse(rMessage);    	  
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("DSC", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("1", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  assertEquals("null", errorLog[4]);
	    	  assertEquals("null", errorLog[5]);
	    	  assertEquals("Unknown Continuation Pointer", errorLog[6]);
	    
	    	 
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	private RSP_K21 getResponseMsg(Message response){
		RSP_K21 message = null;
		if (response instanceof RSP_K21) {
			message = (RSP_K21) response;
		} else {
			String error = "Unexpected response from PIX server \"" + connection.getDescription() + "\"";			
		}
		return message;
	}
	private ACK getCancelResponseMsg(Message response){
		ACK message = null;
		if (response instanceof ACK) {
			message = (ACK) response;
		} else {
			String error = "Unexpected response from PIX server \"" + connection.getDescription() + "\"";			
		}
		return message;
		
	}
	private String processPixQueryResponse(RSP_K21 message) throws HL7Exception{
		
		String status = message.getMSA().getAcknowledgmentCode().getValue();
		if ((status == null) || (!status.equalsIgnoreCase("AA") && !status.equalsIgnoreCase("CA"))) {
			return getErrorMsg(message.getERR());
		}
		status = message.getQAK().getQueryResponseStatus().getValue();
		if (status.equalsIgnoreCase("OK")) {
				return processPixQueryIds(message.getQUERY_RESPONSE().getPID());
		} else if (status.equalsIgnoreCase("NF")) {
			return null;
		} else {
			 return getErrorMsg(message.getERR());
		}
	}
	
	private String getErrorMsg(ERR msgIn)throws HL7Exception {
		ERL erl = msgIn.getErrorLocation(0);
		String errorMsg = erl.getSegmentID().getValue()+","+
		erl.getSegmentSequence().getValue()+","+
		erl.getFieldPosition().getValue()+","+
		erl.getFieldRepetition().getValue()+","+
		erl.getComponentNumber().getValue()+","+
		msgIn.getHL7ErrorCode().getIdentifier().getValue()+","+
		msgIn.getHL7ErrorCode().getText().getValue();
		return errorMsg;
	}
	
	private String processPixQueryIds(PID pid)throws HL7Exception {
		String localId = null;
		String localAuthority =null;
		CX[] idList = pid.getPatientIdentifierList();
		if (idList != null) {
			for (int i=0; i<idList.length; i++) {
				// Decode the returned ID
				CX theId = pid.getPatientIdentifierList(i);
				localId = theId.getIDNumber().getValue()+ "," +
				theId.getAssigningAuthority().getNamespaceID().getValue()+","+
				theId.getAssigningAuthority().getUniversalID().getValue()+","+
				theId.getAssigningAuthority().getUniversalIDType().getValue()+","+
			    theId.getIdentifierTypeCode().getValue()+","+
				pid.getPatientName(0).getGivenName().getValue()+","+
				pid.getPatientName(1).getNameTypeCode().getValue();
			}
		}
		return localId;
	}
	public class OidMock implements OID.OidSource {
        public synchronized String generateId() {
            return Long.toString( System.currentTimeMillis() );
        }
    }

}
