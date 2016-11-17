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

package com.hinacom.pix.mesatests.atna;

import java.util.ArrayList;

import com.hinacom.pix.ihe.audit.IheAuditTrail;
import com.hinacom.pix.mesatests.MesaTestLogger;

import com.misyshealthcare.connect.net.ConnectionFactory;
import com.misyshealthcare.connect.net.IConnectionDescription;

/** Test rig for MESA test 11101.
 * 
 * This tests the basic BSD logging capabilities.
 * 
 * @see ConnectionFactory
 * @author Josh Flachsbart
 * @version 2.0 - Nov 10, 2005
 */
public class Test11101 {
	
	/**
	 * Run MESA Test 11101
	 */
	public static void main(String[] args) {
		String test = "11101";

		ConnectionFactory.loadConnectionDescriptionsFromFile("conf/mesatests/actors/AuditRepositoryConnections.xml");
		ArrayList<IConnectionDescription> repositories = new ArrayList<IConnectionDescription>();
		repositories.add(ConnectionFactory.getConnectionDescription("log4j_audittrail"));
		repositories.add(ConnectionFactory.getConnectionDescription("mesa_arr_bsd"));

		MesaTestLogger logger = new MesaTestLogger(System.out);
		logger.writeTestBegin(test);
		IheAuditTrail snat = new IheAuditTrail("SecureNode", repositories);
		snat.start();
		logger.writeTestEnd(test);
	}

}
