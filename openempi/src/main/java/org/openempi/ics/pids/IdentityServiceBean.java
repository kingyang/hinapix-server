package org.openempi.ics.pids;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;
import org.openempi.data.DomainIdentifier;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.ics.ccs.CorrelationSystem;
import org.openempi.ics.ccs.LookUpObj;
import org.openempi.ics.db.AttributeType;
import org.openempi.ics.db.DatabaseServices;
import org.openempi.ics.db.DatabaseServicesFactory;
import org.openempi.ics.db.QueryParamList;
import org.openempi.ics.utility.ICSProperties;
import org.openempi.ics.utility.Profile;


/**
 * @ejb.bean
 * 		name="IdentityService"
 * 		display-name="IdentityService"
 * 		description="Identity Service used for retrieving patient matches."
 * 		jndi-name="ejb/IdentityService"
 * 		type="Stateful"
 * 		view-type="remote"
 */
public class IdentityServiceBean implements SessionBean {
	/**
	 * Number of millis of inactivity of a query() call before closing the
	 * database connection. The timer starts after each call to queryNext().
	 * This prevents orphaned query calls from holding open db connections.
	 */
	private final static long QUERY_SESSION_TIMEOUT = 300000;

	/**
	 * A Map containing the LookUpObjs initiated by this session. This is
	 * transient because the objects contain DB state information which is not
	 * serializable.
	 */
	private HashMap queryMap = new HashMap();

	/**
	 * A Map containing the Timeout objects associated with each query in the
	 * queryMap. Also transient because of relationship to queryMap.
	 */
	private HashMap timeoutMap = new HashMap();

	/**
	 * The Cache of Person objects used to improve certain lookup performance
	 */
	private PersonCache idCache = PersonCache.getInstance();

	private Logger log = Logger.getLogger(IdentityServiceBean.class);

	private CorrelationSystem corrSys = new CorrelationSystem();

	private SessionContext ctx;
    
	public IdentityServiceBean() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
	public List getProfile(PersonIdentifier pid) throws EJBException,
			IdentityServiceException {
		Profile.begin("IdentityServiceBean.getProfile");
		DomainIdentifier did;
		String pidStr = pid.getId();
		String domain = null;
		String facility = null;

		if ((did = pid.getAssigningAuthority()) != null)
			domain = did.getNameSpaceID();
		if ((did = pid.getAssigningFacility()) != null)
			facility = did.getNameSpaceID();

		List matchList = null;

		matchList = (List) idCache.getPid(pid);
		if (matchList != null)
			return matchList;

		try {

			log.debug("getProfile for " + pidStr + ", " + domain + ", "
					+ facility);
			/*
			 * This only returns a list of Person objects with the matching ID
			 */
			QueryParamList params = new QueryParamList(QueryParamList.AND_LIST);
			params.add(AttributeType.PERSON_IDENTIFIER, pidStr);

			if (domain != null)
				params.add(AttributeType.AA_NAMESPACE_ID, domain);

			if (facility != null)
				params.add(AttributeType.AF_NAMESPACE_ID, facility);

			DatabaseServices dbServices = DatabaseServicesFactory.getInstance();
			matchList = dbServices.query(params);

			if (matchList.size() == 0)
				log.debug("Identifier was not found in the Database !!");

			idCache.put(pid, matchList);
		} catch (org.openempi.ics.db.DatabaseException dbe) {
			log.error(dbe, dbe);
			throw new IdentityServiceException(dbe);
		}

		Profile.end("IdentityServiceBean.getProfile");
		return matchList;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */
	public List findCandidates(Person person) throws EJBException,
			IdentityServiceException {
		return findCandidates(person, ICSProperties.getDouble(
				"ICS_SEARCH_QUALITY", 0.8), 0);
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
	public List findCandidates(Person person, double confidence)
			throws EJBException, IdentityServiceException {
		return findCandidates(person, confidence, 0);
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
	public List findCandidates(Person person, int max) throws EJBException,
			IdentityServiceException {
		return findCandidates(person, ICSProperties.getDouble(
				"ICS_SEARCH_QUALITY", 0.8), max);
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
	public List findCandidates(Person person, double confidence, int max)
			throws EJBException, IdentityServiceException {
		Profile.begin("IdentityServiceBean.findCandidates");
		log.debug("findCandidates(): person = " + person + ", confidence = "
				+ confidence);
		List candidates = null;

		try {
			List matches = corrSys.lookUp(person, confidence, max);

			if (matches.size() > 0)
				candidates = matches;
			else
				candidates = new ArrayList();
		} catch (java.security.AccessControlException acc) {
			throw acc;
		} catch (Exception e) {
			log.error(e, e);
			throw new IdentityServiceException(e);
		} finally {
			Profile.end("IdentityServiceBean.findCandidates");
		}

		log.debug("findCandidates(): candidates.size() = " + candidates.size());
		return candidates;
	}
	
	public List findPersons(Person person) throws EJBException, IdentityServiceException {
		Profile.begin("IdentityServiceBean.findPersons");
		log.debug("findPersons(): person = " + person);
		List candidates = null;

		try {
			List matches = corrSys.lookUp(person);

			if (matches.size() > 0)
				candidates = matches;
			else
				candidates = new ArrayList();
		} catch (java.security.AccessControlException acc) {
			throw acc;
		} catch (Exception e) {
			log.error(e, e);
			throw new IdentityServiceException(e);
		} finally {
			Profile.end("IdentityServiceBean.findCandidates");
		}

		log.debug("findPersons(): persons.size() = " + candidates.size());
		return candidates;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
	public List getProfileList(List identifiers) throws EJBException,
			IdentityServiceException {
		Profile.begin("IdentityServiceBean.getProfileList");
		List profiles = new ArrayList();
		PersonIdentifier pid = null;
		List personList = null;

		// check if the input List is valid
		if ((identifiers != null) && (identifiers.size() > 0)) {
			Iterator itr = identifiers.iterator();

			// check if the iterator has any elements and loop through them
			while (itr.hasNext()) {

				// get the PersonIdentifier
				pid = (PersonIdentifier) itr.next();

				// get the profiles
				personList = getProfile(pid);

				if ((personList != null) && (personList.size() > 0)) {
					profiles.addAll(personList);
				} else {
					log.debug("getProfileList(): match not found for "
							+ pid.getId());
				}
			}
		} else {
			log.warn("getProfileList(): Input List is either null or empty");
		}

		Profile.end("IdentityServiceBean.getProfileList");
		return profiles;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 * 
	 * Method which takes in an Object Identifier and returns the Person Object
	 * corresponding to this identifier.
	 * 
	 * Note: This Object Identifier, in most cases will be the primary key of
	 * the row of data for the Person Object required
	 * 
	 * @param oid
	 *            The Object Id of the <code>Person</code> Object in the
	 *            Database
	 * @return Person - The <code>Person</code> Object requested
	 * @throws RemoteException
	 * @throws org.openempi.ics.pids.IdentityServiceException
	 * @deprecated Use the int oid method instead
	 * 
	 */
	public Person getPersonByOid(String oid) throws EJBException,
			IdentityServiceException {
		return getPersonByOid(Integer.valueOf(oid).intValue());
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 * 
	 * Method which takes in an Object Identifier and returns the Person Object
	 * corresponding to this identifier.
	 * 
	 * Note: This Object Identifier, in most cases will be the primary key of
	 * the row of data for the Person Object required
	 * 
	 * @param oid
	 *            The Object Id of the <code>Person</code> Object in the
	 *            Database
	 * @return Person - The <code>Person</code> Object requested
	 * @throws RemoteException
	 * @throws org.openempi.ics.pids.IdentityServiceException
	 */
	public Person getPersonByOid(int oid) throws EJBException,
			IdentityServiceException {
		Profile.begin("IdentityServiceBean.getPersonByOid");
		List results = null;
		Person person = null;

		if (oid != 0) {
			if ((person = idCache.getOid(oid)) == null) {

				try {
					QueryParamList params = new QueryParamList(
							QueryParamList.OR_LIST);
					params.add(AttributeType.PERSON_OID, oid);
					DatabaseServices dbServices = DatabaseServicesFactory
							.getInstance();
					results = dbServices.query(params);
				} catch (org.openempi.ics.db.DatabaseException dbEx) {
					log.error(dbEx, dbEx);
					throw new IdentityServiceException(dbEx);
				}

				if ((results != null) && (results.size() > 0)) {
					/*
					 * Since this particular query returns only one Person
					 * Object, we pick the first Object from the list returned
					 */
					person = (Person) results.get(0);
					idCache.put(oid, person);
				} else {
					log
							.debug("No Person found in the Database corresponding to the Oid "
									+ oid);
				}
			}
		} else {
			log.warn("Empty Object Id passed for querying - " + oid);
		}

		Profile.end("IdentityServiceBean.getPersonByOid");
		return person;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 * 
	 * Begins a new query for Persons that match the given Person. You should
	 * call queryNext() to retrieve the matching Persons. You MUST call
	 * queryEnd() when you have retreived all results.
	 * 
	 * @param person
	 *            The Person to search for
	 * @param confidence
	 *            the confidence level to be used in the search.
	 * @return A reference integer to be used in calls to queryNext() and
	 *         queryEnd();
	 */
	public int queryStart(Person person, double confidence)
			throws EJBException, IdentityServiceException {
		LookUpObj queryObj = (LookUpObj) queryMap.get(person);
		int ref = person.hashCode();

		if (queryObj != null)
			corrSys.lookUpEnd(queryObj);

		queryObj = corrSys.lookUpStart(person, confidence);
		Integer refInt = new Integer(ref);
		queryMap.put(refInt, queryObj);
		Timeout timeout = new Timeout(ref, QUERY_SESSION_TIMEOUT);
		timeoutMap.put(refInt, timeout);
		return ref;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 * 
	 * Returns the next set of Persons for a query started with queryStart().
	 * The person object passed in should be the same Person object passed in
	 * from queryStart(). You can control how many results to return in each
	 * call to queryNext() by setting the howMany attribute. The returned List
	 * will contain at most howMany Persons but may contain fewer if there are
	 * fewer left that match. There is no guarenteed order to the results.
	 * 
	 * You should always call queryEnd() when you have retrieved all results you
	 * need from the most recent query.
	 * 
	 * @param ref
	 *            The reference integer returned by queryStart()
	 * @param howMany
	 *            The number of matches to return in this batch
	 * 
	 * @return A List of Person objects that match or null if there are no
	 *         further matches.
	 */
	public List queryNext(int ref, int howMany) throws EJBException,
			IdentityServiceException {
		List ret = null;
		Integer refInt = new Integer(ref);
		LookUpObj queryObj = (LookUpObj) queryMap.get(refInt);
		Timeout timeout = (Timeout) timeoutMap.get(refInt);

		if (queryObj == null)
			throw new IdentityServiceException("Query abnormally terminate");

		timeout.reset();
		ret = corrSys.lookUpNext(queryObj, howMany);
		return ret.size() == 0 ? null : ret;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 * 
	 * Called to indicate you are done with the most recent query being
	 * executed. This tells the ICS that it can clean up and close all database
	 * connections, etc.
	 * 
	 * @param ref
	 *            The reference integer returned by queryStart()
	 */
	public void queryEnd(int ref) throws EJBException,
			IdentityServiceException {
		Integer refInt = new Integer(ref);
		LookUpObj queryObj = (LookUpObj) queryMap.remove(refInt);
		if (queryObj != null)
			corrSys.lookUpEnd(queryObj);
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */
	public Person getPersonByCorpId(String clientId, String corpId)
			throws EJBException, IdentityServiceException {

		Profile.begin("IdentityServiceBean.getPersonByCorpId");
		List results = null;
		Person person = null;

		if (corpId != null) {

			try {
				QueryParamList params = new QueryParamList(
						QueryParamList.AND_LIST);
				params.add(AttributeType.AA_NAMESPACE_ID, clientId);
				// remove facId as part of query because the cropId should work
				// accross
				// facilities.
				// This is for 4.5 patch.
				// params.add(AttributeType.AF_NAMESPACE_ID, facilityId);
				params.add(AttributeType.CORPORATE_ID, corpId);
				DatabaseServices dbServices = DatabaseServicesFactory
						.getInstance();
				results = dbServices.query(params);
			} catch (org.openempi.ics.db.DatabaseException dbEx) {
				log.error(dbEx, dbEx);
				throw new IdentityServiceException(dbEx);
			}

			if ((results != null) && (results.size() > 0)) {
				/*
				 * Since this particular query returns only one Person Object,
				 * we pick the first Object from the list returned
				 */
				person = (Person) results.get(0);
			} else {
				log
						.debug("No Person found in the Database corresponding to the corporate id "
								+ corpId);
			}

		} else {
			log.warn("Empty Object Id passed for querying - " + corpId);
		}

		Profile.end("IdentityServiceBean.getPersonByCorpId");
		return person;

	}

	/**
	 * Private class to handle timeouts for the query methods to prevent a db
	 * connection from getting stranded. NOTE: This thread is only to clean up
	 * the DB connection on inactivity. It should NOT be used to modify any
	 * persistent state of the object. This will fail if the object had been
	 * passivated previously.
	 */
	private class Timeout implements Runnable {
		/**
		 * Number of millis before timeout
		 */
		private long timeout = 0;

		/**
		 * The thread running the timeout check
		 */
		private Thread myThread = null;

		/**
		 * The reference int returned from queryStart()
		 */
		private int ref = 0;

		/**
		 * Creates the timeout object and start the timer thread. After timeout
		 * milliseconds, the database connection will be closed
		 */
		public Timeout(int ref, long timeout) {
			this.timeout = timeout;
			this.ref = ref;
			myThread = new Thread(this);
			myThread.start();
		}

		/**
		 * Resets the timeout object such that it will take another timeout
		 * milliseconds before the db connection is closed. Interrupts the timer
		 * thread which causes it to reset.
		 */
		public void reset() {
			if (myThread != null)
				myThread.interrupt();
		}

		/**
		 * Runs until the timeout period is reached. When the timeout is hit,
		 * calls queryEnd() to close the DB connections. If the thread gets an
		 * InterruptedException, the timer is restarted.
		 */
		public void run() {
			while (true) {
				try {
					Thread.sleep(timeout);
					queryEnd(ref);
					return;
				} catch (InterruptedException e) {
				} catch (Exception e) {
					log.error(e, e);
					return;
				}
			}
		}

	}

	public void ejbActivate() throws EJBException, RemoteException {
		log = Logger.getLogger("ICS");
		idCache = PersonCache.getInstance();
		corrSys = new CorrelationSystem();
	}

	public void ejbCreate() throws CreateException, EJBException,
			RemoteException {
		log = Logger.getLogger("ICS");
		idCache = PersonCache.getInstance();
		corrSys = new CorrelationSystem();
	}

	public void ejbPassivate() throws EJBException, RemoteException {
		log = null;
		idCache = null;
		corrSys = null;
	}

	public void ejbRemove() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	public void setSessionContext(SessionContext context) throws EJBException,
			RemoteException {
		ctx = context;
	}

}
