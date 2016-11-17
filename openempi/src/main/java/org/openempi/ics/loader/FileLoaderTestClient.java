package org.openempi.ics.loader;

import junit.framework.TestCase;

import java.util.Hashtable;
import javax.rmi.PortableRemoteObject;
import javax.naming.Context;
import javax.naming.InitialContext;
/**
 * EJB Test Client
 */
public class FileLoaderTestClient extends TestCase {

	public FileLoaderTestClient() {
		// TODO Auto-generated constructor stub
	}

	public FileLoaderTestClient(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/** Home interface */
	protected org.openempi.ics.pids.PersonIdServiceHome home;
	/**
	 * Get the initial naming context
	 */
	protected Context getInitialContext() throws Exception {
		Hashtable props = new Hashtable();
		props.put(
			Context.INITIAL_CONTEXT_FACTORY,
			"org.jnp.interfaces.NamingContextFactory");
		props.put(
			Context.URL_PKG_PREFIXES,
			"org.jboss.naming:org.jnp.interfaces");
		props.put(Context.PROVIDER_URL, "jnp://localhost:1099");
		Context ctx = new InitialContext(props);
		return ctx;
	}

	/**
	 * Get the home interface
	 */
	protected org.openempi.ics.pids.PersonIdServiceHome getHome()
		throws Exception {
		Context ctx = this.getInitialContext();
		Object o = ctx.lookup("ejb/PersonIdService");
		org.openempi.ics.pids.PersonIdServiceHome intf = (org.openempi.ics.pids.PersonIdServiceHome) PortableRemoteObject
			.narrow(o, org.openempi.ics.pids.PersonIdServiceHome.class);
		return intf;
	}

	/**
	 * Set up the test case
	 */
	protected void setUp() throws Exception {
		this.home = this.getHome();
	}

	/**
	 * Test for org.openempi.ics.pids.PersonIdService.addPerson(org.openempi.data.Person person)
	 */
	public void testAddPerson() throws Exception {
		org.openempi.ics.pids.PersonIdService instance;
		org.openempi.data.Person result = null;

		// Parameters
		org.openempi.data.Person param0 = new org.openempi.data.Person();

		try {
			// Instance creation
			instance = this.home.create();
	
			// Method call
			result = instance.addPerson(param0);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		// Various assertions
		// assertNotNull(result);
	}
}
