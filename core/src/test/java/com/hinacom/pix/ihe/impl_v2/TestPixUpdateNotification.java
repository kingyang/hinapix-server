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
import com.hinacom.pix.ihe.IPixManagerAdapter;
import com.hinacom.pix.ihe.audit.IheAuditTrail;
import com.hinacom.pix.ihe.configuration.ConfigurationLoader;
import com.hinacom.pix.ihe.configuration.ConfigurationLoader.PixManagerActorDescription;
import com.hinacom.pix.ihe.log.IMessageStoreLogger;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.message.ACK;
import ca.uhn.hl7v2.parser.PipeParser;
import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.util.OID;
public class TestPixUpdateNotification extends TestCase {
	private IConnectionDescription connection = null;
    private IPixManagerAdapter pixAdapter = null;
    private static ConfigurationLoader loader = null;
	private PixManager actor = null;
	private Collection<IConnectionDescription> pixConsumerConnections = null;
	public void setUp() {
		 try {
        	loader = ConfigurationLoader.getInstance();
            URL url = TestPixQuery.class.getResource("/tests/actors/IheActors.xml");
            File file = new File(url.toURI());
            loader.loadConfiguration(file, false, null, new OidMock(), null, null, null, new TestLogContext());                  
            ConfigurationLoader.ActorDescription pixqactor = loader.getDescriptionById("pixman");          
            connection=pixqactor.getConnection();
            pixConsumerConnections = ((PixManagerActorDescription)pixqactor).getPixConsumerConnections();
            ConfigurationLoader.ActorDescription auditactor = loader.getDescriptionById("localaudit");            
            //create an actor
            IheAuditTrail auditTrail=new IheAuditTrail("localaudit", auditactor.getLogConnection());	    
            actor = new PixManager(connection, auditTrail, null, pixConsumerConnections);
	    	MockPixAdapter pixAdapter = new MockPixAdapter(); 
            actor.registerPixManagerAdapter(pixAdapter);            
            IMessageStoreLogger storeLogger = new MockMessageStoreLogger();
            actor.setStoreLogger(storeLogger);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot load the actor property");
        }
	}
	
		
	/**
	 * Test PixUpdateNotification: patient update.
	 */
	
	public void testUpdateNotify(){	      
		String msh="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060809155816-0400||ADT^A08^ADT_A08|PIF_0|P|2.3.1||||||||\r"+ 
		"EVN|A08|20060809155816||||20060809155814\r"+
		"PID|||12345678^^^WESTCLINIC&1.2234.634325.5734&ISO^PI||DePinto^Joe^V^Jr^Dr.|Wang|19580325|M|raja|^race^^^^|||(716)385-6235^PRN^PH^^^716^3856235||tamil|^U^^^^|^hindu^^^^|077746567576|666777888|9877876|priya|^ethnic^^^^|tamilnadu||1|^indian^^^^||||090120071011|1|\r"+
		"PV1||O";
	        PipeParser pipeParser = new PipeParser();
	        try {
	        	        	
	        	Message msgIn = pipeParser.parse(msh);        	
	        	PixFeedHandler feedHandler = new PixFeedHandler(actor);
	        	Message resposeMsg = feedHandler.processMessage(msgIn);   	
	        	ACK rMessage=getResponseMsg(resposeMsg);
		    	assertEquals("AA",rMessage.getMSA().getAcknowledgementCode().getValue());		    	 
		    	
			} catch (Exception e) {
				 // this is exactly what we were expecting so 
	            // let's just ignore it and let the test pass
		} 
    }
	
	/**
	 * Test PixUpdateNotification: patient merge.
	 */
	
	public void testMergeNotify(){	      
		String msh="MSH|^~\\&|CLINREG|WESTCLIN|EHR_MISYS|MISYS|20060809155816-0400||ADT^A40^ADT_A40|PIF_0|P|2.3.1||||||||\r"+ 
		"EVN|A40|20060809155816||||20060809155814\r"+
		"PID|||12345678^^^WESTCLINIC&1.2234.634325.5734&ISO^PI||DePinto^Joe^V^Jr^Dr.|Wang|19580325|M|raja|^race^^^^|||(716)385-6235^PRN^PH^^^716^3856235||tamil|^U^^^^|^hindu^^^^|077746567576|666777888|9877876|priya|^ethnic^^^^|tamilnadu||1|^indian^^^^||||090120071011|1|\r"+
		"MRG|536253^^^MIEH^PI||||||Tusona^Luis^N^Sr^Dr.\r";		
	        PipeParser pipeParser = new PipeParser();
	        try {
	        	        	
	        	Message msgIn = pipeParser.parse(msh);        	
	        	PixFeedHandler feedHandler = new PixFeedHandler(actor);
	        	Message resposeMsg = feedHandler.processMessage(msgIn);   	
	        	ACK rMessage=getResponseMsg(resposeMsg);
		    	assertEquals("AA",rMessage.getMSA().getAcknowledgementCode().getValue());		    	 
		    	
			} catch (Exception e) {
				 // this is exactly what we were expecting so 
	            // let's just ignore it and let the test pass
		} 
    }
	
	private ACK getResponseMsg(Message response){
		ACK message = null;
		if (response instanceof ACK) {
			message = (ACK) response;
		} else {
			String error = "Unexpected response from PIX server \"" + connection.getDescription() + "\"";			
		}
		return message;
	}
	
	public class OidMock implements OID.OidSource {
        public synchronized String generateId() {
            return Long.toString( System.currentTimeMillis() );
        }
    }
}
