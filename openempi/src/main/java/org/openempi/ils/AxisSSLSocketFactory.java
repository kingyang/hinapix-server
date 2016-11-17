/*
 * Title:       SSLFactory
 * Description: Creates an SSLSocketFactory.
 * Copyright:   Copyright (c) 2001
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils;

import java.net.Socket;
import java.security.Security;
import java.util.Hashtable;

import javax.net.ssl.SSLSocketFactory;

import org.apache.axis.components.net.BooleanHolder;
import org.openempi.ils.utility.SSLFactory;


/**
  This is an apache specific implementation.  
*/
public class AxisSSLSocketFactory
    extends SSLFactory
    implements org.apache.axis.components.net.SecureSocketFactory
{
    public static synchronized SSLSocketFactory init()
    {
        SSLSocketFactory factory = SSLFactory.init();
        Security.setProperty("axis.socketSecureFactory", 
                             "com.carescience.ils.AxisSSLSocketFactory");
        return factory;
    }

    public AxisSSLSocketFactory(Hashtable attributes) 
    {
        init();
    }
    
    public Socket create(String host, int port,
                         StringBuffer otherHeaders,
                         BooleanHolder useFullURL)
        throws Exception
    {
        if (port == -1) 
            port = 443;
        return createSocket(host, port);
    }

}
 
