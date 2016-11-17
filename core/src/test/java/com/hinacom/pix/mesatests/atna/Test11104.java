/*
 * Copyright 2005 Misys Healthcare Systems. All rights reserved.
 * This software is protected by international copyright laws and
 * treaties, and may be protected by other law. Violation of copyright
 * laws may result in civil liability and criminal penalties.
 */

package com.hinacom.pix.mesatests.atna;

import java.util.ArrayList;

import com.hinacom.pix.ihe.audit.IheAuditTrail;
import com.hinacom.pix.ihe.impl_v2.TestLogContext;
import com.hinacom.pix.mesatests.MesaTestLogger;

import com.misyshealthcare.connect.base.audit.AuditCodeMappings.SuccessCode;
import com.misyshealthcare.connect.net.ConnectionFactory;
import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.util.LibraryConfig;

/** Test rig for MESA test 11104.
 * 
 * This tests the logging of user authentication.
 * 
 * @see ConnectionFactory
 * @author Josh Flachsbart
 * @version 2.0 - Nov 10, 2005
 */
public class Test11104 {
	
	/**
	 * Run MESA Test 11104
	 */
	public static void main(String[] args) {
		String test = "11104";
		
		
		ConnectionFactory.loadConnectionDescriptionsFromFile("conf/mesatests/actors/AuditRepositoryConnections.xml");
		ArrayList<IConnectionDescription> repositories = new ArrayList<IConnectionDescription>();
		repositories.add(ConnectionFactory.getConnectionDescription("log4j_audittrail"));
		repositories.add(ConnectionFactory.getConnectionDescription("mesa_arr_bsd"));

		// Doctor
//		LogContext context = new LogContext();
//		context.setClientAddress("10.0.1.101");
//		context.setUserId("jones@sunroom.hosp.org");
//		context.setUserName("Dr. Jones");
//		LogManager.setLogContext(context);
		LibraryConfig.getInstance().setLogContext(new TestLogContext());

		MesaTestLogger logger = new MesaTestLogger(System.out);
		logger.writeTestBegin(test);
		IheAuditTrail snat = new IheAuditTrail("SecureNode", repositories);
		snat.start();
		snat.userLogin(SuccessCode.Success, null);
		logger.writeTestEnd(test);
	}

}
