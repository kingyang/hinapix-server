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
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionHub;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * The abstract class for all PIX/PDQ test cases. It set up
 * basic testing information such as server name and port.
 * 
 * @author Wenzhi Li
 * @version 1.0, Jan 23, 2009
 */
public class AbstractPixPdqTestCase extends AbstractEJBTestCase {

	//PIX/PDQ server host name and ports
	//private final String HOST_NAME = "198.160.211.53";
	private final String HOST_NAME = "localhost";
	private final int PIX_SEVER_PORT = 3600; 
	private final int PDQ_SEVER_PORT = 3601; 

	protected Connection connection = null;
	
	public AbstractPixPdqTestCase() {
		super();
	}
	public AbstractPixPdqTestCase(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (connection != null) {
			//Find out why this is not working
			//connection.close();						
		}
	}

	protected void createPIXConnection() throws HL7Exception {
		ConnectionHub connectionHub = ConnectionHub.getInstance();
		connection = connectionHub.attach(HOST_NAME, PIX_SEVER_PORT, new HL7Parser(), MinLowerLayerProtocol.class);
	}
	
	protected void createPDQConnection() throws HL7Exception {
		ConnectionHub connectionHub = ConnectionHub.getInstance();
		connection = connectionHub.attach(HOST_NAME, PDQ_SEVER_PORT, new HL7Parser(), MinLowerLayerProtocol.class);
	}	
}
