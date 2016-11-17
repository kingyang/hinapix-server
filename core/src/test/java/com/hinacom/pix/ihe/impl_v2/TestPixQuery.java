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
import com.hinacom.pix.ihe.IPixManagerAdapter;
import com.hinacom.pix.ihe.audit.IheAuditTrail;
import com.hinacom.pix.ihe.configuration.ConfigurationLoader;
import com.hinacom.pix.ihe.log.IMessageStoreLogger;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.ERL;
import ca.uhn.hl7v2.model.v25.message.QBP_Q21;
import ca.uhn.hl7v2.model.v25.message.RSP_K23;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;

import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.util.OID;

/**
 * Title				:	
 * Description			:	This is the mock implementation class for Pix query class
 * Created By			:	Rasakannu Palaniyandi 	
 * Created Date			:	Nov 21, 2008
 * Last Modified By		:	
 * Last Modified Date	:
 */
public class TestPixQuery extends TestCase {
	private IConnectionDescription connection = null;
    private IPixManagerAdapter pixAdapter = null;
    private static ConfigurationLoader loader = null;
	private PixManager actor = null;


	public static void main(String[] args) {
        TestPixQuery testQuery = new TestPixQuery();
        testQuery.setUp();

        testQuery.testReceivingApplication();
	}

	public void setUp() {
		 
	        try {
	        	loader = ConfigurationLoader.getInstance();
	            URL url = TestPixQuery.class.getResource("/conf/tests/actors/IheActors.xml");
	            File file = new File(url.toURI());
	            loader.loadConfiguration(file, false, null, new OidMock(), null, null, null, new TestLogContext());
	            Collection actors = loader.getActorDescriptions();	            
	            ConfigurationLoader.ActorDescription pixqactor = loader.getDescriptionById("pixman");
	            connection=pixqactor.getConnection();
	            ConfigurationLoader.ActorDescription auditactor = loader.getDescriptionById("localaudit");
	            
	            IheAuditTrail auditTrail=new IheAuditTrail("pixman", auditactor.getLogConnection());
	            actor = new PixManager(connection, auditTrail, null, null);
		    	MockPixAdapter pixAdapter = new MockPixAdapter();
	            actor.registerPixManagerAdapter(pixAdapter);
	            //Add MessageStore log
	            IMessageStoreLogger storeLogger = new MockMessageStoreLogger();
	            actor.setStoreLogger(storeLogger);
	        } catch (Exception e) {
	            e.printStackTrace();
	            fail("Cannot load the actor property");
	        }
	}
	
	/**
	 * Test PixQueryHandler: Invalid PixQueryHandler event.
	 */
	public void testInvalidPixQuery(){
				      
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|HOSPMPI|HOSP|200701181712||QBP^Q24^QBP_Q21|1|D|2.5||||||||\r"+ 
		"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112234^^^METRO HOSPITAL|^^^WESTCLINIC~^^^SOUTH LAB|\r"+
		"RCP||+|\r"+
		"SEC|0614";
	        PipeParser pipeParser = new PipeParser();
	        try {
	        	Message msgIn = pipeParser.parse(msh);   
	        	QBP_Q21 message = (QBP_Q21)msgIn;
	        	Message responseMsg= new PixQueryHandler(actor).processMessage(message);	
	        	fail("processQuery() should've thrown exception");
			} catch (Exception e) {
				 // this is exactly what we were expecting so 
	            // let's just ignore it and let the test pass
		} 
    }
	
	/**
	 * Tests PixQueryHandler: ReceivingApplication is not recognized.
	 */
	public void testReceivingApplication(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|HOSPMPI|HOSP|200701181712||QBP^Q23^QBP_Q21|1|D|2.5||||||||\r"+ 
		"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112234^^^METRO HOSPITAL|^^^WESTCLINIC~^^^SOUTH LAB|\r"+
		"RCP||+|\r"+
		"SEC|0614";
	       PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  QBP_Q21 message = (QBP_Q21)msgIn;
	    	  Message resposeMsg= new PixQueryHandler(actor).processMessage(message);
	    	  RSP_K23 rMessage=getResponseMsg(resposeMsg);
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("MSH", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("5", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  assertEquals("1", errorLog[4]);
	    	  assertEquals("null", errorLog[5]);
	    	  assertEquals("Unknown Receiving Application", errorLog[6]);	  
	    	  
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}

	/**
	 * Tests PixQueryHandler: Receiving Facility is not recognized.
	 */
	public void testReceivingFacility(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|HOSP|200701181712||QBP^Q23^QBP_Q21|1|D|2.5||||||||\r"+ 
		"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112234^^^METRO HOSPITAL|^^^WESTCLINIC~^^^SOUTH LAB|\r"+
		"RCP||+|\r"+
		"SEC|0614";
	       PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  QBP_Q21 message = (QBP_Q21)msgIn;
	    	  Message resposeMsg= new PixQueryHandler(actor).processMessage(message);	
	    	  RSP_K23 rMessage=getResponseMsg(resposeMsg);
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);    	  
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("MSH", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("6", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  assertEquals("1", errorLog[4]);
	    	  assertEquals("null", errorLog[5]);
	    	  assertEquals("Unknown Receiving Facility", errorLog[6]);
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	/**
	 * Tests PixQueryHandler: the request domain is not valid.
	 */
	public void testRequestDomainId(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|200701181712||QBP^Q23^QBP_Q21|1|D|2.5||||||||\r"+ 
		"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112234^^^METRO HOSPITAL|^^^WESTCLINIC~^^^SOUTH LAB|\r"+
		"RCP||+|\r"+
		"SEC|0614";
	       PipeParser pipeParser = new PipeParser();
	       try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  
	    	  QBP_Q21 message = (QBP_Q21)msgIn;
	    	  Message resposeMsg= new PixQueryHandler(actor).processMessage(message);
	    	  RSP_K23 rMessage=getResponseMsg(resposeMsg);
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);    	  
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("QPD", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("3", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  assertEquals("4", errorLog[4]);
	    	  assertEquals("204", errorLog[5]);
	    	  assertEquals("Unknown Key Identifier", errorLog[6]);
	    	  
	       } catch (Exception e) {
			e.printStackTrace();
	} 
	}
	
	/**
	 * Tests PixQueryHandler: The patient request Id is not valid.
	 */
 	public void testRequestId(){
 		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|200701181712||QBP^Q23^QBP_Q21|1|D|2.5||||||||\r"+ 
 		"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112254^^^CPR|^^^WESTCLINIC~^^^SOUTH LAB|\r"+
		"RCP||+|\r"+
		"SEC|0614";
	      PipeParser pipeParser = new PipeParser();
	      try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  QBP_Q21 message = (QBP_Q21)msgIn;
	    	  Message resposeMsg= new PixQueryHandler(actor).processMessage(message);	
	    	  RSP_K23 rMessage=getResponseMsg(resposeMsg);
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("QPD", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("3", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  assertEquals("1", errorLog[4]);
	    	  assertEquals("204", errorLog[5]);
	    	  assertEquals("Unknown Key Identifier", errorLog[6]);
			} catch (Exception e) {
				e.printStackTrace();
		}
	}		
 
 	/**
 	 * Tests PixQueryHandler when at least one of the return domains is not valid. 
 	 */
 	public void testRequestNDomainId(){
 		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|200701181712||QBP^Q23^QBP_Q21|1|D|2.5||||||||\r"+ 
 		"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112234^^^CPR|^^^WRONGDOMAIN~^^^SOUTH LAB|\r"+
		"RCP||+|\r"+ 
		"SEC|0614";
 		
// 		String msh ="MSH|^&~\\|CLINREG|WESTCLIN|HOSPMPI|HOSP|||QBP^Q23^QBP_Q21|1|D|2.5\r"+
//		      	"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112234^^^METRO HOSPITAL|^^^WEST\r"+
//		      	"CLINIC~^^^SOUTH LAB|\r"+
//		      	"RCP||I|\r"+
//		      	"SEC|0614";
	      PipeParser pipeParser = new PipeParser();
	      try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  QBP_Q21 message = (QBP_Q21)msgIn;
	    	  Message resposeMsg= new PixQueryHandler(actor).processMessage(message);	
	    	  RSP_K23 rMessage=getResponseMsg(resposeMsg);
	    	  assertEquals("AE",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("AE",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);
	    	  String[] errorLog=returnValues.split(",");
	    	  assertEquals("QPD", errorLog[0]);
	    	  assertEquals("1", errorLog[1]);
	    	  assertEquals("4", errorLog[2]);
	    	  assertEquals("1", errorLog[3]);
	    	  //assertNull(errorLog[4]);
	    	  assertEquals("204", errorLog[5]);
	    	  assertEquals("Unknown Key Identifier", errorLog[6]);
	      } catch (Exception e) {
			e.printStackTrace();
	      }
	}	 
 	
 	/**
 	 * Test PixQueryHandler: At least one Patient ID is found.
 	 */
	public void testIdFoundExRequestId(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|200701181712||QBP^Q23^QBP_Q21|1|D|2.5||||||||\r"+ 
 		"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112235^^^HomeCare|^^^WESTCLINIC~^^^SOUTH LAB|\r"+
		"RCP||+|\r"+
		"SEC|0614";
		  PipeParser pipeParser = new PipeParser();
	      try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  QBP_Q21 message = (QBP_Q21)msgIn;
	    	  Message resposeMsg= new PixQueryHandler(actor).processMessage(message);
	    	  RSP_K23 rMessage=getResponseMsg(resposeMsg);
	    	  assertEquals("AA",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("OK",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  String returnValues=processPixQueryResponse(rMessage);
	    	  String[] errorLog=returnValues.split(",");	    	   	  
	    	  assertEquals("12345678", errorLog[0]);
	    	  assertEquals("WESTCLINIC", errorLog[1]);
	    	  assertEquals("1.2234.634325.5734", errorLog[2]);
	    	  assertEquals("ISO", errorLog[3]);
	    	  assertEquals("PI", errorLog[4]);
	    	  assertEquals("", errorLog[5]);  
	    	 } catch (Exception e) {
				e.printStackTrace();
		}
	}	
	/**
	 * Tests PixQueryHandler when no patient id is found, but the request message is correct.
	 */
	public void testNoIdFoundExRequestId(){
		String msh ="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|200701181712||QBP^Q23^QBP_Q21|1|D|2.5||||||||\r"+ 
 		"QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112234^^^CPR|^^^WESTCLINIC~^^^SOUTH LAB|\r"+
		"RCP||+|\r"+
		"SEC|0614";
		  PipeParser pipeParser = new PipeParser();
	      try {
	    	  Message msgIn = pipeParser.parse(msh);   
	    	  QBP_Q21 message = (QBP_Q21)msgIn;
	    	  Message resposeMsg= new PixQueryHandler(actor).processMessage(message);
	    	  RSP_K23 rMessage=getResponseMsg(resposeMsg);
	    	  assertEquals("AA",rMessage.getMSA().getAcknowledgmentCode().getValue());
	    	  assertEquals("NF",rMessage.getQAK().getQueryResponseStatus().getValue());
	    	  } catch (Exception e) {
				e.printStackTrace();
		}
	}				
	private RSP_K23 getResponseMsg(Message response){
		RSP_K23 message = null;
		if (response instanceof RSP_K23) {
			message = (RSP_K23) response;
		} else {
			String error = "Unexpected response from PIX server \"" + connection.getDescription() + "\"";			
		}
		return message;
	}
	
	private String processPixQueryResponse(RSP_K23 message) throws HL7Exception{
		
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
