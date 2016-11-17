package org.openempi.ics.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.log4j.Logger;
import org.openempi.data.Person;
import org.openempi.ics.pids.PersonIdService;
import org.openempi.ics.pids.PersonIdServiceHome;

public abstract class AbstractFileLoader implements FileLoader
{
	protected Logger log = Logger.getLogger(AbstractFileLoader.class);
	private PersonIdServiceHome personIdServiceHome;
	private PersonIdService personIdService;
	
	public void loadPerson(Person person) {
		personIdService = getPersonIdService();
		log.debug("Attempting to load person entry " + person);
		try {
			personIdService.addPerson(person);
		} catch (Exception e) {
			log.error("Failed while adding person entry to the system. Error: " + e, e);
			throw new RuntimeException("Failed while adding person entry to the system.");
		}
	}

	public void parseFile(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			log.error("Unable to read the input file. Error: " + e);
			throw new RuntimeException("Unable to read the input file.");
		}
		
		try {
			boolean done = false;
			int lineIndex=0;
			while (!done) {
				String line = reader.readLine();
				if (line == null) {
					done = true;
					continue;
				}
				Person person = processLine(line, lineIndex++);
				if (person == null) {
					continue;
				}
				loadPerson(person);
			}
		} catch (IOException e) {
			log.error("Failed while loading the input file. Error: " + e);
			throw new RuntimeException("Failed while loading the input file.");
		}
	}

	protected abstract Person processLine(String line, int lineIndex);

	private PersonIdService getPersonIdService() {
		try {
			if (personIdService == null) {
				personIdService = getPersonIdServiceHome().create();
			}
		} catch (Exception e) {
			log.error("Failed while attempting to retrieve reference to the PersonIdService. Error: " + e);
			throw new RuntimeException("Failed while obtaining reference to the Person Id Service.");
		}
		return personIdService;
	}


	private PersonIdServiceHome getPersonIdServiceHome() throws NamingException {
		if (personIdServiceHome == null) {
			personIdServiceHome = getHome();
		}
		return personIdServiceHome;
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
		props.put(Context.PROVIDER_URL, "jnp://localhost:1199");
		Context ctx = new InitialContext(props);
		return ctx;
	}

	/**
	 * Get the home interface
	 */
	protected PersonIdServiceHome getHome()
		throws NamingException {
		Context ctx = this.getInitialContext();
		Object o = ctx.lookup("ejb/PersonIdService");
		PersonIdServiceHome intf = (PersonIdServiceHome) PortableRemoteObject
			.narrow(o, PersonIdServiceHome.class);
		return intf;
	}	
}
