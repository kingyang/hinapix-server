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


package com.hinacom.pix.ihe.impl_v2.hl7;

import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.llp.LowerLayerProtocol;
import ca.uhn.hl7v2.parser.Parser;

import com.misyshealthcare.connect.net.ConnectionFactory;
import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.net.IServerConnection;

import javax.net.ssl.SSLServerSocket;

/**
 * The class extends HAPI HL7Service interface. It is the HL7 server engine
 * to start and stop an HL7 service.
 *
 * @author Wenzhi Li
 * @version 1.0, Mar 2, 2007
 */
public class HL7Server extends HL7Service {

    private static Logger log = Logger.getLogger(HL7Server.class);
    private IConnectionDescription connection;

    /**
     * Constructor. Creates a new instance of SimpleServer that listens
     * on the given port.
     */
    public HL7Server(IConnectionDescription conn, LowerLayerProtocol llp, Parser parser) {
        super(parser, llp);
        this.connection = conn;
    }

    /**
     * Loop that waits for a connection and starts a ConnectionManager
     * when it gets one.
     */
    public void run() {
        try {
            IServerConnection serverConn = ConnectionFactory.getServerConnection(connection);
            ServerSocket ss = serverConn.getServerSocket();
            if(ss instanceof SSLServerSocket)
            {
                SSLServerSocket ssl = (SSLServerSocket)ss;
                ssl.setNeedClientAuth(true);
                String[] e = new String[]{"SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA", "SSL_RSA_WITH_DES_CBC_SHA"};
                ssl.setEnabledCipherSuites(e);
            }

            ss.setSoTimeout(10000);

            log.info(connection.getDescription() + " is running on port " + ss.getLocalPort());
            while (keepRunning()) {
                try {
                    Socket newSocket = ss.accept();
                    log.info("Accepted connection from " + newSocket.getInetAddress().getHostAddress());
                    Connection conn = new Connection(parser, this.llp, newSocket);
                    newConnection(conn);
                } catch (InterruptedIOException ie) {
                    //ignore - just timed out waiting for connection
                } catch (Exception e) {
                    log.error("Error accepting HL7 connections: ", e);
                }
            }

            ss.close();
        } catch (Exception e) {
            log.error(e);
        } finally {
            this.stop();
        }
    }

}
