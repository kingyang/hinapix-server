/*
 * Title:       SSLFactory
 * Description: Creates an SSLSocketFactory.
 * Copyright:   Copyright (c) 2001
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;


/**
 This class is used to create an SSLSocketFactory.  The factory will contain the
 nessesary information for validation by an external system.  This information includes:
 <ul>
  <li>The <code>certificate</code> is a digitally signed statement vouching
      for the identity and public key of an entity (person, company, etc.). Certificates
      can either be self-signed or issued by a Certification Authority (CA).
      Certification Authorities are entities that are trusted to issue valid certificates
      for other entities. Well-known CAs include VeriSign, Entrust, and GTE CyberTrust.</li><br><br>

  <li>The <code>keystore</code> is where your public and private keys are stored.
      A keystore is a database of key material. Key material is used for a variety of purposes,
      including authentication and data integrity.<br><br>

      Generally speaking, keystore information can be grouped into two different categories:
      key entries and trusted certificate entries. A key entry consists of an entity's identity
      and its private key, and can be used for a variety of cryptographic purposes. In contrast,
      a trusted certificate entry only contains a public key in addition to the entity's identity.
      <br><br>

   <li>The <code>truststore</code> is a keystore which is used when making decisions about what to trust.
      If you receive some data from an entity that you already trust, and if you can verify
      that the entity is the one it claims to be, then you can assume that the data really
      came from that entity. </li><br><Br>

      This class uses two different keystore files: one containing just your key entries,
      and the other containing your trusted certificate entries, including Certification Authority (CA)
      certificates. The former contains private information, while the latter does not.
      Using two different files instead of a single keystore file provides for a cleaner
      separation of the logical distinction between your own certificates (and corresponding
      private keys) and others' certificates. You could provide more protection for your private
      keys if you store them in a keystore with restricted access, while providing the trusted
      certificates in a more publicly accessible keystore if needed.<br><br>
   </ul><br>

 This class refers to the <tt>cacerts</tt> keystore file which the jdk uses to store CA entries.
 It is located in <tt>jdk1.3/jre/lib/security/</tt>.  In order to perform bi-directional authentication
 with a remote host you must have the CA cert of that host stored in the <tt>cacerts</tt> file.<br><br>

 Refer to sun's documentation on <a href="http://java.sun.com/j2se/1.3/docs/tooldocs/win32/keytool.html" target="blank">
 keytool</a> for more information on how to do this.

 <b>Note</b>: This class does two things, it sets up the default SSLSocketFactory
 for both https connections and normal ssl socket connections.  This is why this
 class extends SSLSocketFactory, it must do this to provide the correct 
 key and trust stores for the default SSLSocketFactory it implements.

 @author CareScience
 @version 1.7, 20011017
 @since CDE 1.1
*/
public class SSLFactory extends SSLSocketFactory
{
  private static final String KEYSTORE_LOC = IlsProps.getKeyStoreLoc();
  private static final String KEYSTORE_PWD = IlsProps.getKeyStorePwd();
  private static final String KEY_PWD = IlsProps.getKeyPwd();
  
  private static SSLSocketFactory factory = null;
  
  private static SSLFactory instance = new SSLFactory();
  
  public SSLFactory() 
  {
    init();
  }
  
  /**
     Initializes the default SSLSocketFactory for all HttpsUrlConnections.
     This call is required to be make possible the use of https urls.  You
     may call this as many times as you wish with no additonal overhead, or
     to return the SSLSocketFactory.
     
     @return SSLSocketFactory The factory instance created, if you need to
     do your own SSL stuff outside of a URLConnection
  */
  public static synchronized SSLSocketFactory init()
  {
    if (factory != null) 
      return factory;
    
    return getSSLSocketFactory(KEYSTORE_LOC,KEYSTORE_PWD,KEY_PWD,null,null);
  }
  
  /**
      Creates an SSLSocketFactory to use for authentication of the client.
      @param keyStoreLoc the location of the <code>keystore</code> which holds
                         the public <code>key</code>
      @param keyStorePass the password of the <code>keystore</code>
      @param keyPass the password to the public <code>key</code> held within
                     the <code>keystore</code>
      @param trustStoreLoc location of the truststore which holds the CA certs.
                           If null, the default trust store is used.  
                           This parameter is currently ignored.
      @param trustStorePass If trustStoreLoc is not null, the password for
                            the specified trust store.
                            This parameter is currently ignored.
      @return SSLSocketFactory
  */
  private static SSLSocketFactory getSSLSocketFactory(String keyStoreLoc,
                                                      String keyStorePass,
                                                      String keyPass,
                                                      String trustStoreLoc,
                                                      String trustStorePass)
  {
    Logger log = Logger.getLogger("ILS");
    try {
      System.setProperty("java.protocol.handler.pkgs",
                         "com.sun.net.ssl.internal.www.protocol");
      /*
        System.setProperty("java.protocol.handler.pkgs", 
        "sun.net.www.protocol.http");
      */
      Provider p = new com.sun.net.ssl.internal.ssl.Provider();
      Security.insertProviderAt(p, 1);
      
      try {
        SSLContext ctx = SSLContext.getInstance("SSL", p);
        if (keyStoreLoc != null && keyStorePass != null && keyPass != null) {
          KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", p);
          KeyStore ks = KeyStore.getInstance("JKS");
          
          ks.load(new FileInputStream(keyStoreLoc), keyStorePass.toCharArray());
          kmf.init(ks, keyPass.toCharArray());
          
          TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", p);
          tmf.init(ks);
          ctx.init(kmf.getKeyManagers(), 
                   tmf.getTrustManagers(), 
                   null);
        } else {
          log.warn("No Keystore settings defined, outbound SSL client certs will be disabled.");
        }
        factory = ctx.getSocketFactory();
      }
      catch (SSLException se) {
        log.error(se, se);
      }
    }
    catch (java.security.GeneralSecurityException gse) {
      log.error(gse, gse);
    }
    catch (IOException ioe) {
      log.error(ioe, ioe);
    }
    
    /** This sets up the SSLFactory class as the default SSLSocketFactory
        for all non-https/URL based SSL stuff.  This is required to 
        work with the SOAP/Axis implementation. */
    Security.setProperty("ssl.SocketFactory.provider", 
                         "com.carescience.utility.SSLFactory");
    
    /** This makes the factory the default for all https url connections */
    HttpsURLConnection.setDefaultSSLSocketFactory(factory);
    
    return factory;
  }
  
  
  /* The following are for the SSLSocketFactory implementation */
  
  public static SocketFactory getDefault() 
  {
    return new SSLFactory();
  }
  
  public Socket createSocket(InetAddress host, int port)
    throws IOException
  {
    return factory.createSocket(host, port);
  }
  
  public Socket createSocket(InetAddress address, int port, 
                             InetAddress localAddress, int localPort) 
    throws IOException
  {
    return factory.createSocket(address, port, localAddress, localPort);
  }
  
  public Socket createSocket(String host, int port)
    throws IOException, UnknownHostException
  {
    return factory.createSocket(host, port);
  }
  
  public Socket createSocket(String host, int port, InetAddress localHost, 
                             int localPort) 
    throws IOException, UnknownHostException
  {
    return factory.createSocket(host, port, localHost, localPort);
  }
  
  public Socket createSocket(Socket s, String host, int port, 
                             boolean autoClose) 
    throws IOException, UnknownHostException
  {
    return factory.createSocket(s, host, port, autoClose);
  }
  
  public String[] getDefaultCipherSuites() 
  {
    return factory.getSupportedCipherSuites();
  }
  
  public String[] getSupportedCipherSuites() 
  {
    return factory.getSupportedCipherSuites();
  }
  
}
 
