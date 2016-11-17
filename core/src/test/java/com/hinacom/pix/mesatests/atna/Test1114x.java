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

import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.hinacom.pix.ihe.audit.IheAuditTrail;
import com.hinacom.pix.mesatests.MesaTestLogger;

import com.misyshealthcare.connect.base.AuditBroker;
import com.misyshealthcare.connect.net.ConnectionFactory;
import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.net.IServerConnection;

/**
 * ANTA 11141 (the same as 1221)
 * 
 * @author Wenzhi Li
 * @version 1.0,  Jan 7, 2009
 */
public class Test1114x {
	/**
	 * Note: This Test Case can be used to run ATNA Server Node Mesa Test #11141 (or 1221),
	 *  #11142 (or 1222) and #11143 (or 1223).
	 * 
	 * Setup Instructions:
	 *   Before running this test, make sure to config Mesa Client (the secure_test.cfg file) as follows:
	 *   TEST_SECURE_SERVER_HOST = localhost (or the hostname where your Mesa Software is installed) 
     *   TEST_SECURE_SERVER_PORT = 4300 (this port must be the same as the one used in ALLSCRIPTS-SECURE)  
     *
	 */
	public static void main(String[] args) {
		String test = "1114x";

		MesaTestLogger logger = new MesaTestLogger(System.out);
		logger.writeTestBegin(test);

		ConnectionFactory.loadConnectionDescriptionsFromFile("C:\\src2\\hinapix\\core\\src\\test\\resources\\conf\\mesatests\\actors\\AuditRepositoryConnections.xml");
		// Set up audit trail.
		ArrayList<IConnectionDescription> repositories = new ArrayList<IConnectionDescription>();
		repositories.add(ConnectionFactory.getConnectionDescription("log4j_audittrail"));
		repositories.add(ConnectionFactory.getConnectionDescription("mesa_arr_bsd"));
		AuditBroker broker = AuditBroker.getInstance();
		broker.registerAuditSource(new IheAuditTrail("SecureNode", repositories));
		
		IConnectionDescription connection = ConnectionFactory.getConnectionDescription("ALLSCRIPTS-SECURE");
		
		IServerConnection serverConn = ConnectionFactory.getServerConnection(connection);
		ServerSocket ss = serverConn.getServerSocket();
        logger.writeString("HL7Server running on port " + ss.getLocalPort());
        try {
	        	ss.setSoTimeout(60000);
	        	Socket newSocket = ss.accept();
                logger.writeString("Accepted connection from " + newSocket.getInetAddress().getHostAddress());

         
        		PrintStream writer = new PrintStream(newSocket.getOutputStream());
        		writer.println("Hello world");
        		writer.flush();
        	    if (writer.checkError()) System.out.println("SSLSocketServer: java.io.PrintWriter error");
 
        }
        catch (InterruptedIOException ie) {
             //ignore - just timed out waiting for connection
         }
        catch (Exception e) {
        	 e.printStackTrace();
             logger.writeString( "Error accepting HL7 connections: "+ e.getMessage());
        } finally {	
        	serverConn.closeServerConnection();
        }
    	logger.writeTestEnd(test);
	}

}
