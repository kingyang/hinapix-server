package com.hinacom.pix.integrationtests;

import junit.framework.TestCase;


import java.rmi.RemoteException;
import java.util.Date;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;
import org.openempi.data.Address;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DocumentHeader;
import org.openempi.data.DomainIdentifier;
import org.openempi.data.Gender;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.PersonName;
import org.openempi.data.SocialSecurityNumber;
import org.openempi.ics.pids.IdentityService;
import org.openempi.ics.pids.IdentityServiceHome;
import org.openempi.ics.pids.PersonIdService;
import org.openempi.ics.pids.PersonIdServiceHome;

import junit.framework.TestCase;

public class AbstractEJBTestCase extends TestCase
 {
	private Context context=null;
	protected Logger log = Logger.getLogger(AbstractEJBTestCase.class);
	
	public AbstractEJBTestCase() {
		super();
	}

	public AbstractEJBTestCase(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = getInitialContext();
	}
	
	protected IdentityService getIdentityService() throws NamingException, RemoteException, CreateException {
		Object o = context.lookup("ejb/IdentityService");
		IdentityServiceHome intf = (IdentityServiceHome) PortableRemoteObject
				.narrow(o, IdentityServiceHome.class);
		return intf.create();
	}

	protected PersonIdService getPersonIdService() throws NamingException, RemoteException, CreateException {
		Object o = context.lookup("ejb/PersonIdService");
		PersonIdServiceHome intf = (PersonIdServiceHome) PortableRemoteObject
				.narrow(o, PersonIdServiceHome.class);
		return intf.create();
	}

	/**
	 * Get the initial naming context
	 */
	protected Context getInitialContext() throws NamingException {
		Hashtable<String,String> props = new Hashtable<String,String>();
		props.put(
			Context.INITIAL_CONTEXT_FACTORY,
			"org.jnp.interfaces.NamingContextFactory");
		props.put(
			Context.URL_PKG_PREFIXES,
			"org.jboss.naming:org.jnp.interfaces");
		props.put(Context.PROVIDER_URL, "jnp://127.0.0.1:1099");
		Context ctx = new InitialContext(props);
		return ctx;
	}
	
	/**
    Helper method to create Persons
    
    @param domain Domain for the pid (may be null)
    @param domain Facility for the pid (may be null)
    @param pid The patient id (may be null)
    @param lname Last Name (may be null)
    @param fname First Name (may be null)
    @param gender The gender of the person (may be null)
    @param ssn The social security number (may be null)
    @param dob The Date of birt for the person (may be null)
    @param addr The main address lines (if null, city, state and zip ignored)
    @param city The city for the address
    @param state The State for the address
    @param zip The zip code for the address
    @return new Person
	 */
	@SuppressWarnings("deprecation")
	protected Person createPerson(String domain, String facility, String pid,
		String lname, String fname, String gender, String ssn, Date dob,
		String addr, String city, String state, String zip) {
		    Person person = new Person();
		    DocumentHeader dh = new DocumentHeader();
		    dh.setMessageDate(new Date());
		
		    if (lname != null && lname.length() > 0)
		      person.addName(new PersonName(lname.toUpperCase(), 
		                                    (fname != null?fname.toUpperCase():null),
		                                     null));
		    if (addr != null && addr.length() > 0)
		      person.addAddress(new Address(addr, null, city, state, zip));
		    if (gender != null && gender.length() > 0)
		      person.addGender(new Gender(dh, gender));
		    if (dob != null) 
		      person.addDateOfBirth(new DateOfBirth(dh, dob));
		    if (ssn != null && ssn.length() > 0)
		      person.addSocialSecurityNumber(new SocialSecurityNumber(dh, ssn));
		    if (domain!=null || facility!=null || pid != null) {
		    	person.addPersonIdentifier(createPersonIdentifier(domain, facility, pid));
		    }
		    person.addDocumentHeader(dh);
		    return person;
	}	
	
	/**
    Helper method to create PersonIdentifiers
    
    @param domain Domain for the pid
    @param domain Facility for the pid
    @param pid The actual pid to use
    @return new PersonIdentifier 
	 */
	@SuppressWarnings("deprecation")
	protected PersonIdentifier createPersonIdentifier(String domain, String facility,
		String pid) {
		    return new PersonIdentifier(pid,
		                                new DomainIdentifier(domain, domain, "ISO"),
		                                new DomainIdentifier(facility, facility, "ISO"), 
		                                null);
		  }


}
