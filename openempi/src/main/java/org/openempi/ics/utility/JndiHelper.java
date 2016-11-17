/*
 * Title:       JndiHelper
 * Description: Class to facilitate JNDI bindings and lookups
 * Copyright:   (c) 2001-2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.utility;

import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;

/**
 * A utility class to facilitate JNDI bindings and lookups.
 *
 * @author CareScience
 * @version 1.7, 20020327
 */
public class JndiHelper 
{
  /** IIOP URL, obtained from <code>com.carescience.utility.CSProperties</code>. */
  public static final String IIOP_URL = ICSProperties.getProperty("IIOP_URL");
    
  /** This class should never be instantiated, all methods are static. */
  private JndiHelper() {}
    
  /**
     * Binds an object to a name in the JNDI tree.
     *
     * @param objectToBind Object to bind in JNDI
     * @param jndiName name of the bound object
     * @throws NamingException
     */
  public static void bindObject(String jndiName, Object objectToBind)
    throws NamingException 
  {
    InitialContext ctx = getContext();
        
    Logger log = Logger.getLogger("ICS");
  
    log.debug("bindObject(): Attempting to bind " + jndiName);
        
    // loop through package and create JNDI subcontexts
    StringTokenizer st = new StringTokenizer(jndiName, ".", false);
        
    int elementCount = st.countTokens() - 1;
        
    String element = "";
        
    while (st.hasMoreElements() && elementCount > 0) {
      element = element + (String)st.nextElement();
      ctx.createSubcontext(element);
      element += ".";
      elementCount--;
    }
        
    // bind the object
    ctx.rebind(jndiName, objectToBind);
  }
    
  /**
     * Retrieves the specified object from the JNDI tree, using the name provided.
     * Will call use PortableRemoteObject if not a DataSource class.
     *
     * @param lookupName the name of the object to be retrieved
     * @param classToCreate the Class to be created from the retrieved object
     * @return Object
     * @throws NamingException
     */
  public static Object getObject(String lookupName, Class classToCreate)
    throws NamingException 
  {
    InitialContext ctx = getContext();
        
    Object obj = null;
        
    if ( classToCreate.getName().endsWith("DataSource") ) {
      obj = ctx.lookup(lookupName); 
    } else {
      obj = PortableRemoteObject.narrow(ctx.lookup(lookupName), classToCreate); 
    }
        
    return obj;
  }
    
  /** Create the initial context to be used by all subsequent JNDI actions. */
  private static InitialContext getContext() 
    throws NamingException 
  {
    return new InitialContext();
  }
}
