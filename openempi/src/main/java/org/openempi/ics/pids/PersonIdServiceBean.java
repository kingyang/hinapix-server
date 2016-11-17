/*
 * Title:       PersonIdServiceBean
 * Description: Implementation of the PersonIdService interface.
 * Copyright:   (c) 2001-2003
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.pids;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.openempi.data.DocumentHeader;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.PersonName;
import org.openempi.data.PersonReview;
import org.openempi.ics.ccs.CorrelationSystem;
import org.openempi.ics.db.AttributeType;
import org.openempi.ics.db.DatabaseException;
import org.openempi.ics.db.DatabaseServices;
import org.openempi.ics.db.DatabaseServicesFactory;
import org.openempi.ics.db.QueryParamList;
import org.openempi.ics.utility.ICSProperties;
import org.openempi.ics.utility.IcsTrace;
import org.openempi.ics.utility.JndiHelper;
import org.openempi.ics.utility.Profile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ejb.bean
 * 		name="PersonIdService"
 * 		display-name="PersonIdService"
 * 		description="Patient identification service used to manage patient data in the database."
 * 		jndi-name="ejb/PersonIdService"
 * 		type="Stateless"
 * 		view-type="remote"
 * 
 */
public class PersonIdServiceBean implements SessionBean {

	private Logger log = Logger.getLogger(IdentityServiceBean.class);

	private SessionContext ctx;

	/**
	 * The Cache of Person objects used to improve certain lookup performance
	 */
	private PersonCache idCache = PersonCache.getInstance();

	private IdentityServiceHome _idshome;

	// private CorrelationHome _corhome;
	private String _eidDomainProperty = null;

	private List _eidDomains = new ArrayList();

	public void setSessionContext(SessionContext context)
			throws RemoteException, EJBException {
		ctx = context;
	}

	public void ejbActivate() throws RemoteException, EJBException {
		// log = Logger.getLogger("ICS");
		// idCache = PersonCache.getInstance();
		try {
			// really should use a utility class that caches these EJBHomes. Use
			// xdoclet to create when I have more than a few hrs to impl.
			// TODO Tech Upgrade!!
			_idshome = (IdentityServiceHome) JndiHelper.getObject(
					"com.carescience.ics.pids.IdentityServiceHome",
					IdentityServiceHome.class);
			// _corhome = (CorrelationHome)
			// JndiHelper.getObject("CorrelationUtils", CorrelationHome.class);
		} catch (NamingException e) {
			EJBException ejbx = new EJBException(e.getExplanation(), e);
			throw ejbx;
		}
	}

	public void ejbPassivate() throws RemoteException, EJBException {
		log = null;
		idCache = null;
	}

	public void ejbRemove() throws RemoteException, EJBException {
	}

	public void ejbCreate() throws CreateException, EJBException,
			RemoteException {
		log = Logger.getLogger("ICS");
		idCache = PersonCache.getInstance();
		try {
			_idshome = (IdentityServiceHome) JndiHelper.getObject(
					"ejb/IdentityService",
					IdentityServiceHome.class);
		} catch (NamingException e) {
			EJBException ejbx = new EJBException(e.getExplanation(), e);
			throw ejbx;
		}
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
	public Person addPerson(Person person) throws EJBException,
			PersonIdServiceException {
		return addPerson(person, false);
	}

	private Person addPerson(Person person, boolean eidcheck)
			throws EJBException, PersonIdServiceException {
		try {
			person.isValid();
			IcsTrace trace = new IcsTrace(IcsTrace.ADD_PERSON, person);
			Person toreturn = savePerson(person, trace);
			trace.save();
			if (eidcheck) {
				checkforEIDMismatch(toreturn);
			}
			return toreturn;
		} catch (Exception e) {
			log.error(e, e);
			throw new PersonIdServiceException(e);
		}
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
	public boolean removePerson(Person person) throws EJBException,
			PersonIdServiceException {
		Profile.begin("PersonIdServiceBean.removePerson");

		boolean success = true;
		DatabaseServices dbServices = DatabaseServicesFactory.getInstance();

		try {

			List matches = new ArrayList();

			Iterator ids = person.getPersonIdentifiers().iterator();

			QueryParamList params = new QueryParamList(QueryParamList.OR_LIST);
			while (ids.hasNext()) {
				QueryParamList inner = new QueryParamList(
						QueryParamList.AND_LIST);
				PersonIdentifier pid = (PersonIdentifier) ids.next();
				inner.add(AttributeType.PERSON_IDENTIFIER, pid.getId());
				inner.add(AttributeType.AA_NAMESPACE_ID, pid
						.getAssigningAuthority().getNameSpaceID());
				inner.add(AttributeType.AF_NAMESPACE_ID, pid
						.getAssigningFacility().getNameSpaceID());
				if (log.isDebugEnabled()) {
					log.debug("PID=" + pid.getId() + ",AA="
							+ pid.getAssigningAuthority().getNameSpaceID()
							+ ",AF="
							+ pid.getAssigningFacility().getNameSpaceID());
				}

				params.add(inner);
			}

			matches = dbServices.query(params);

			Iterator matchIter = matches.iterator();
			while (matchIter.hasNext()) {
				Person match = (Person) matchIter.next();

				log.debug("Checking person: " + match.getOid());
				HashSet set = new HashSet();
				Iterator itr = match.getPersonIdentifiers().iterator();
				while (itr.hasNext()) {
					PersonIdentifier pid = (PersonIdentifier) itr.next();

					Iterator itr2 = person.getPersonIdentifiers().iterator();
					while (itr2.hasNext()) {
						PersonIdentifier pid2 = (PersonIdentifier) itr2.next();
						if (pid.equals(pid2)) {
							set.add(pid.getDocumentHeader().getOid());
							if (log.isDebugEnabled())
								log.debug("Removing DocumentHeader: "
										+ pid.getDocumentHeader().getOid());
						}
					}
				}
				if (match.getDocumentHeaders().size() == set.size()) {
					if (log.isDebugEnabled())
						log
								.debug("All DocumentHeaders removed, removing Person instead");
					success = dbServices.removeObject(match.getOid());
				} else
					success = dbServices.removeObject(new ArrayList(set));
				idCache.remove(match);
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new PersonIdServiceException(e);
		} finally {
			Profile.end("PersonIdServiceBean.removePerson");
		}
		return success;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */		
	public Person updatePerson(Person person) throws RemoteException,
			PersonIdServiceException {
		try {
			person.isValid();
			Person toreturn;
			IcsTrace trace = new IcsTrace(IcsTrace.UPDATE_PERSON, person);
			// boolean success = savePerson(person, trace);

			toreturn = updatePerson(person, trace);
			trace.save();
			return toreturn;
		} catch (Exception e) {
			log.error(e, e);
			throw new PersonIdServiceException(e);
		}
	}

    // @Autowired
	// private IdentityService identityService;

	private Person savePerson(Person person, IcsTrace trace)
			throws RemoteException, PersonIdServiceException {
		Profile.begin("PersonIdServiceBean.savePerson");

		int personId;
		Person toreturn = null;
		// boolean success = true;
		DatabaseServices dbServices = DatabaseServicesFactory.getInstance();

		// IdentityService IDS;
		try {
			// IDS = _idshome.create();
            // IDS = new IdentityServiceImpl();

            Person foundPerson = checkForExactPIDMatch(person, trace);
			if (foundPerson != null) {
				return foundPerson;
			}

			Collection matches = null;
			SortedMap matchMap = null;

			// Query for Correlation match
			trace.add("Checking for Correlation match");
			CorrelationSystem correlationSystem = new CorrelationSystem(trace);
			matches = correlationSystem.query(person);

			if ((matches != null) && (matches.size() > 0)) {

				for (Iterator iter = matches.iterator(); iter.hasNext();) {

					Person dupe = (Person) iter.next();
					if (trace.isEnabled()) {
						trace.add("Possible Match found with Person:");
						trace.add(dupe, true);
					}

					// If there is a match based on the demographics based on the
					// matching algorithm used, then we need to add the patient to the database in
					// association with the matching patient.
					//
					if (correlationSystem.match(person, dupe)) {
						log.debug("Need to build an association between " + person + " and " + dupe);
						DocumentHeader dupeDocHeader = (DocumentHeader) dupe.getDocumentHeaders().get(0);
						log.debug("The DH ID of the dupe is " + dupeDocHeader.getOid());
						DocumentHeader personDocHeader = (DocumentHeader) person.getDocumentHeaders().get(0);
						log.debug("The DH ID of the person is " + personDocHeader.getOid());
						personDocHeader.setOid(dupeDocHeader.getOid());
						dbServices.addPersonInfo(person);
						break;
					}
				}
			} else {
				// Here we need to add the person in the database since the
				// patient is not in the system yet.
				dbServices.addPerson(person);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e, e);
			throw new PersonIdServiceException(e.getMessage());
		} finally {
			Profile.end("PersonIdServiceBean.savePerson");
		}
		// return success;
		return toreturn;
	}

	private Person updatePerson(Person person, IcsTrace trace) throws RemoteException,
			PersonIdServiceException {
		Profile.begin("PersonIdServiceBean.savePerson");

		int personId;
		Person toreturn = null;
		// boolean success = true;
		DatabaseServices dbServices = DatabaseServicesFactory.getInstance();

		// IdentityService IDS;
		try {
			// IDS = _idshome.create();
			Person foundPerson = checkForExactPIDMatch(person, trace);
			if (foundPerson == null) {
				return person;
			}
			
			if (foundPerson.getPersonIdentifiers().size() > 0) {
				PersonIdentifier key = (PersonIdentifier) person.getPersonIdentifiers().get(0);
				for (Iterator<PersonIdentifier> iter = foundPerson.getPersonIdentifiers().iterator(); iter.hasNext(); ) {
					PersonIdentifier match = iter.next();
					if (key.getId().equalsIgnoreCase(match.getId())) {
						dbServices.removeObject(new Integer(match.getOidInt()));
					}
				}
			} else {
				dbServices.removeObject(foundPerson.getOid());
			}

			Collection matches = null;
			SortedMap matchMap = null;

			// Query for Correlation match
			trace.add("Checking for Correlation match");
			CorrelationSystem correlationSystem = new CorrelationSystem(trace);
			matches = correlationSystem.query(person);

			if ((matches != null) && (matches.size() > 0)) {

				for (Iterator iter = matches.iterator(); iter.hasNext();) {

					Person dupe = (Person) iter.next();
					if (trace.isEnabled()) {
						trace.add("Possible Match found with Person:");
						trace.add(dupe, true);
					}

					// If there is a match based on the demographics based on the
					// matching algorithm used, then we need to add the patient to the database in
					// association with the matching patient.
					//
					if (correlationSystem.match(person, dupe)) {
						log.debug("Need to build an association between " + person + " and " + dupe);
						DocumentHeader dupeDocHeader = (DocumentHeader) dupe.getDocumentHeaders().get(0);
						log.debug("The DH ID of the dupe is " + dupeDocHeader.getOid());
						DocumentHeader personDocHeader = (DocumentHeader) person.getDocumentHeaders().get(0);
						log.debug("The DH ID of the person is " + personDocHeader.getOid());
						personDocHeader.setOid(dupeDocHeader.getOid());
						dbServices.addPersonInfo(person);
						break;
					}
				}
			} else {
				// Here we need to add the person in the database since the
				// patient is not in the system yet.
				dbServices.addPerson(person);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e, e);
			throw new PersonIdServiceException(e.getMessage());
		} finally {
			Profile.end("PersonIdServiceBean.savePerson");
		}
		// return success;
		return toreturn;
	}

	private Person checkForExactPIDMatch(Person person, IcsTrace trace) {
		Collection matches;
		SortedMap matchMap;
		// Step 1: Check for PID match
		trace.add("Checking for PID match");
		matchMap = searchByPID(person, trace);
		matches = matchMap.values();
		
		// If we get an exact match based on the PID, then the patient
		// is already in the system. The add operation is not used to update attributes
		// so we are done.
		if (matches.size() > 0) {
			// Special logic to merge with best pid match
			if (trace.isEnabled()) {
				trace.add("PID matches found: " + matches.size());
				Iterator vals = matchMap.values().iterator();
				while (vals.hasNext()) {
					Person p = (Person) vals.next();
					trace.add(p, true);
				}
			}
			trace.save();
			return (Person) matches.iterator().next();
		}
		return null;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */		
	public Person mergePersons(Person[] persons) throws EJBException,
			PersonIdServiceException {

		try {
			DatabaseServices dbServices = DatabaseServicesFactory.getInstance();
			idCache.remove(persons);
			return dbServices.mergePersons(persons);
		} catch (DatabaseException dbe) {
			// Log the exception.
			log.error(dbe, dbe);

			// Wrap the exception.
			throw new PersonIdServiceException(dbe.getMessage());
		}
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */		
	public int splitPerson(Person person, DocumentHeader[] docHeaders)
			throws RemoteException, PersonIdServiceException {

		try {
			DatabaseServices dbServices = DatabaseServicesFactory.getInstance();
			idCache.remove(person);
			return dbServices.splitPerson(person, docHeaders);
		} catch (DatabaseException dbe) {
			// Log the exception.
			log.error(dbe, dbe);

			// Wrap the exception.
			throw new PersonIdServiceException(dbe);
		}
	}

	/**
	 * Searches the database for a <code>Person</code> with the same
	 * <code>PersonIdentifier</code> as the one in an ADT request.
	 * 
	 * @param person
	 *            the <code>Person</code> coming in as part of the ADT request
	 * @return Person the <code>Person</code> in the database which has the
	 *         same <code>PersonIdentifier</code> as the input Object
	 */
	private SortedMap searchByPID(Person person, IcsTrace trace) {
		Profile.begin("PersonIdServiceBean.searchByPID");

		TreeMap ret = new TreeMap();
		DatabaseServices dbServices = DatabaseServicesFactory.getInstance();

		try {
			List matches = null;

			Iterator ids = person.getPersonIdentifiers().iterator();

			QueryParamList params = new QueryParamList(QueryParamList.OR_LIST);
			while (ids.hasNext()) {
				QueryParamList inner = new QueryParamList(
						QueryParamList.AND_LIST);
				PersonIdentifier pid = (PersonIdentifier) ids.next();
				inner.add(AttributeType.PERSON_IDENTIFIER, pid.getId());
				inner.add(AttributeType.AA_NAMESPACE_ID, pid
						.getAssigningAuthority().getNameSpaceID());
				inner.add(AttributeType.AF_NAMESPACE_ID, pid
						.getAssigningFacility().getNameSpaceID());
				params.add(inner);
			}
			matches = dbServices.query(params);

			if (trace.isEnabled()) {
				trace.add("Persons that match PIDS:");
				Iterator i = matches.iterator();
				while (i.hasNext())
					trace.add((Person) i.next());
			}

			Iterator i = matches.iterator();
			while (i.hasNext()) {
				Person match = (Person) i.next();
				ret.put(new Double(1.0), match);
			}
		} catch (DatabaseException dbEx) {
			log.error(dbEx, dbEx);
		} finally {
			Profile.end("PersonIdServiceBean.searchByPID");
		}

		return ret;
	}

	private Person getPersonByPID(Person person, String oldPid) {
		Profile.begin("PersonIdServiceBean.getPersonByPID");

		TreeMap ret = new TreeMap();
		DatabaseServices dbServices = DatabaseServicesFactory.getInstance();

		try {
			List matches = null;

			Iterator ids = person.getPersonIdentifiers().iterator();

			QueryParamList params = new QueryParamList(QueryParamList.OR_LIST);
			if (ids.hasNext()) {
				QueryParamList inner = new QueryParamList(
						QueryParamList.AND_LIST);
				PersonIdentifier pid = (PersonIdentifier) ids.next();
				inner.add(AttributeType.PERSON_IDENTIFIER, oldPid);
				inner.add(AttributeType.AA_NAMESPACE_ID, pid
						.getAssigningAuthority().getNameSpaceID());
				inner.add(AttributeType.AF_NAMESPACE_ID, pid
						.getAssigningFacility().getNameSpaceID());
				params.add(inner);
			} else {
				return null;
			}

			matches = dbServices.query(params);

			Iterator i = matches.iterator();
			if (i.hasNext()) {
				Person match = (Person) i.next();
				return match;
			}

		} catch (DatabaseException dbEx) {
			log.error(dbEx, dbEx);
		} finally {
			Profile.end("PersonIdServiceBean.searchByPID");
		}

		return null;
	}
	
	private List getLinkedPersons(Person person) {
		Profile.begin("PersonIdServiceBean.getLinkedPersons");

		List matches = new ArrayList();
		DatabaseServices dbServices = DatabaseServicesFactory.getInstance();

		try {

			QueryParamList params = new QueryParamList(QueryParamList.AND_LIST);
			DocumentHeader docHeader = (DocumentHeader) person.getDocumentHeaders().get(0);
			params.add(AttributeType.DOC_HEADER_ID, docHeader.getOidInt());

			matches = dbServices.query(params);
		} catch (DatabaseException dbEx) {
			log.error(dbEx, dbEx);
		} finally {
			Profile.end("PersonIdServiceBean.searchByPID");
		}

		return matches;
	}	

	/**
	 * Validates if the <code>Person</code> received with request is an exact
	 * match of the <code>Object</code> retrieved from the database.
	 * 
	 * @param person
	 *            the <code>Person</code> being input
	 * @param match
	 *            the <code>Person</code> from the database
	 * @param checkOnly
	 *            If true, this only performs exact match check, if false, also
	 *            updates Person.
	 * @return true if there is an exact match, false otherwise.
	 * @throws RemoteException
	 * @throws org.openempi.ics.pids.PersonIdServiceException
	 */
	private boolean validateExactMatch(Person person, Person match,
			boolean checkOnly) {
		Profile.begin("PersonIdServiceBean.validExactMatch");
		boolean exactMatch = true;

		if ((person != null) && (match != null)) {

			/*
			 * REMOVE single-valued attributes from the incoming Person in the
			 * event of a) The domains of the incoming Person and the matching
			 * Person from the DB do not match b) The domains of the incoming
			 * Person and the matching Person from the DB match, but the
			 * attribute values are the same KEEP single-valued attributes in
			 * the incoming Person in the event of a) The domains of the
			 * incoming Person and the matching Person from the DB match but the
			 * attribute values are different REMOVE multi-valued attributes
			 * from the incoming Person if there are no updates in the record
			 * NOTE: The 'Notes' attribute is not ICS related. So, we don't
			 * impose the domainMatch rule on that.
			 */

			/* check for domains */
			boolean domainsMatch = person.checkDomain(match);

			/* has new data? */
			boolean hasNewData = false;

			/* Used for comparing single-valued attributes */
			boolean attributesMatch = false;

			/* Used for comparing multi-valued attributes */
			boolean hasUpdates = false;

			/* Person Name for logging, using first */
			PersonName nombre = (PersonName) person.getNames().get(0);
			String pName = nombre.getName();

			/* domain for logging, using first */
			DocumentHeader header = (DocumentHeader) person
					.getDocumentHeaders().get(0);
			String domain = header.getSendingFacility();

			log.debug("Received updates for " + pName + " from " + domain);

			/* Set the OID to the one found in the DB */
			if (!checkOnly)
				person.setOid(match.getOid());

			/* is the update from same domain? */
			if (domainsMatch) {

				/* Check for the presence of new Expired flag */
				attributesMatch = (person.isExpired() == match.isExpired());

				if (attributesMatch) { /* do nothing */
				} else {
					/* do nothing, will update DB */
					log.debug("Updating Expired flag" + " for " + pName);
				}

				/* Check for the presence of new Nationality */
				if ((match.getNationality() != null)
						&& (person.getNationality() != null)) {

					attributesMatch = (match.getNationality().equals(person
							.getNationality()));

					if (attributesMatch) {
						if (!checkOnly)
							person.setNationality(null);
					} else {
						exactMatch = false;
						log.debug("Updating Nationality" + " for " + pName);
					}
				}

				/* Check for the presence of new PrimaryLanguage */
				if ((match.getPrimaryLanguage() != null)
						&& (person.getPrimaryLanguage() != null)) {

					attributesMatch = (match.getPrimaryLanguage().equals(person
							.getPrimaryLanguage()));

					if (attributesMatch) {
						if (!checkOnly)
							person.setPrimaryLanguage(null);
					} else {
						exactMatch = false;
						log.debug("Updating PrimaryLanguage" + " for " + pName);
					}
				}

				/* Check for the presence of new BirthPlace */
				if ((match.getBirthPlace() != null)
						&& (person.getBirthPlace() != null)) {

					attributesMatch = (match.getBirthPlace().equals(person
							.getBirthPlace()));

					if (attributesMatch) {
						if (!checkOnly)
							person.setBirthPlace(null);
					} else {
						exactMatch = false;
						log.debug("Updating BirthPlace" + " for " + pName);
					}
				}

				/* Check for the presence of new MaidenName */
				if ((match.getMaidenName() != null)
						&& (person.getMaidenName() != null)) {

					attributesMatch = (match.getMaidenName().equals(person
							.getMaidenName()));

					if (attributesMatch) {
						if (!checkOnly)
							person.setMaidenName(null);
					} else {
						exactMatch = false;
						log.debug("Updating MaidenName" + " for " + pName);
					}
				}
			} else { /* Domains do not match */
				exactMatch = false;

				/*
				 * CLEAR the single valued attributes since we won't update
				 * records from a different domain than the first
				 */
				if (!checkOnly) {
					person.setBirthPlace(null);
					person.setMaidenName(null);
					person.setNationality(null);
					person.setPrimaryLanguage(null);
				}
			}

			/* check for the presence of new SSNs */
			hasUpdates = checkForUpdates(match.getSocialSecurityNumbers(),
					person.getSocialSecurityNumbers());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received SSN updates for " + pName + " from "
						+ domain);
			} else {
				log.debug("No new SSNs in update request for " + pName
						+ " from " + domain);
			}

			/* check for the presence of new DOBs */
			hasUpdates = checkForUpdates(match.getDatesOfBirth(), person
					.getDatesOfBirth());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received DOB updates for " + pName + " from "
						+ domain);
			} else {
				log.debug("No new DOBs in update request for " + pName
						+ " from " + domain);
			}

			/* check for the presence of new Addresses */
			hasUpdates = checkForUpdates(match.getAddresses(), person
					.getAddresses());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received Address updates for " + pName + " from "
						+ domain);
			} else {
				log.debug("No new Addresses in update request for " + pName
						+ " from " + domain);
			}

			/* check for the presence of new DriversLicenses */
			hasUpdates = checkForUpdates(match.getDriversLicenses(), person
					.getDriversLicenses());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received DriversLicense updates for " + pName
						+ " from " + domain);
			} else {
				log.debug("No new DriversLicenses in update request " + " for "
						+ pName + " from " + domain);
			}

			/* check for the presence of new EmailAddresses */
			hasUpdates = checkForUpdates(match.getEmailAddresses(), person
					.getEmailAddresses());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received EmailAddress updates for " + pName
						+ " from " + domain);
			} else {
				log.debug("No new EmailAddresses in update request " + " for "
						+ pName + " from " + domain);
			}

			/* check for the presence of new EthnicGroups */
			hasUpdates = checkForUpdates(match.getEthnicGroups(), person
					.getEthnicGroups());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received EthnicGroup updates for " + pName
						+ " from " + domain);
			} else {
				log.debug("No new EthnicGroups in update request " + " for "
						+ pName + " from " + domain);
			}

			/* check for the presence of new Genders */
			hasUpdates = checkForUpdates(match.getGenders(), person
					.getGenders());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received Gender updates for " + pName + " from "
						+ domain);
			} else {
				log.debug("No new Genders in update request " + " for " + pName
						+ " from " + domain);
			}

			/* check for the presence of new MaritalStatii */
			hasUpdates = checkForUpdates(match.getMaritalStatii(), person
					.getMaritalStatii());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received MaritalStatus updates for " + pName
						+ " from " + domain);
			} else {
				log.debug("No new MaritalStatii in update request " + " for "
						+ pName + " from " + domain);
			}

			/* check for the presence of new Names */
			hasUpdates = checkForUpdates(match.getNames(), person.getNames());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received Name updates for " + pName + " from "
						+ domain);
			} else {
				log.debug("No new Names in update request " + " for " + pName
						+ " from " + domain);
			}

			/* check for the presence of new PersonIdentifiers */
			hasUpdates = checkForUpdates(match.getPersonIdentifiers(), person
					.getPersonIdentifiers());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received PersonIdentifier updates for " + pName
						+ " from " + domain);
			} else {
				log.debug("No new PersonIdentifiers in update request "
						+ " for " + pName + " from " + domain);
			}

			/* check for the presence of new Races */
			hasUpdates = checkForUpdates(match.getRaces(), person.getRaces());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received Race updates for " + pName + " from "
						+ domain);
			} else {
				log.debug("No new Races in update request " + " for " + pName
						+ " from " + domain);
			}

			/* check for the presence of new Religions */
			hasUpdates = checkForUpdates(match.getReligions(), person
					.getReligions());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received Religion updates for " + pName + " from "
						+ domain);
			} else {
				log.debug("No new Religions in update request " + " for "
						+ pName + " from " + domain);
			}

			/* check for the presence of new TelephoneNumbers */
			hasUpdates = checkForUpdates(match.getTelephoneNumbers(), person
					.getTelephoneNumbers());

			if (hasUpdates) {
				hasNewData = true;
				log.debug("Received TelephoneNumbers updates for " + pName
						+ " from " + domain);
			} else {
				log.debug("No new TelephoneNumbers in update "
						+ " request for " + pName + " from " + domain);
			}

            /* check for the presence of new TelephoneNumbers */
			hasUpdates = checkForUpdates(match.getAccountNumbers(), person.getAccountNumbers());
            if (hasUpdates) {
                hasNewData = true;
                log.debug("Received AccountNumbers updates for " + pName
                        + " from " + domain);
            } else {
                log.debug("No new AccountNumbers in update "
                        + " request for " + pName + " from " + domain);
            }

			/* does the input record have new data? */
			if (!hasNewData) {
				/* no updates, clear Lists */
				if (!checkOnly) {
					person.clearAddresses();
					person.clearDriversLicenses();
					person.clearEmailAddresses();
					person.clearEthnicGroups();
					person.clearGenders();
					person.clearMaritalStatii();
					person.clearNames();
					person.clearPersonIdentifiers();
					person.clearRaces();
					person.clearReligions();
					person.clearTelephoneNumbers();
					person.clearDocumentHeaders();
                    person.clearAccountNumbers();
				}

				log.debug("No new attributes for " + "update in request from "
						+ domain + " for " + pName);
			} else
				exactMatch = false;
		}

		Profile.end("PersonIdServiceBean.validExactMatch");

		return exactMatch;
	}

	/**
	 * method to check for actual updates in the incoming record
	 * 
	 * NOTE: this method assumes that there will be one and only one alias per
	 * update in an incoming record. If there are any more than one, this method
	 * may fail.
	 * 
	 * @param oldList
	 *            the existing list of Objects
	 * @param newList
	 *            the new list being input
	 * @return boolean - true if has updates, false otherwise
	 */
	private boolean checkForUpdates(List oldList, List newList) {
		Profile.begin("PersonIdServiceBean.checkForUpdates");
		boolean hasUpdates = false;

		if ((oldList == null || oldList.size() == 0) && newList != null
				&& newList.size() > 0)
			return true;

		if (oldList != null && oldList.size() > 0 && newList != null
				&& newList.size() > 0) {

			int count = 0; /* counter for available aliases in the DB */

			for (int j = 0; j < newList.size(); j++) {
				count = 0;
				for (int i = 0; i < oldList.size(); i++) {
					if (newList.get(j).equals(oldList.get(i))) {
						break; /* have the alias in the DB, look no further */
					} else {
						count++;
					}
				}

				/* have we checked agianst all the aliases in the DB? */
				if (count == oldList.size()) {
					hasUpdates = true;
					break; /*
							 * assuming that the incoming record has one and
							 * only alias
							 */
				}
			}
		}
		Profile.end("PersonIdServiceBean.checkForUpdates");
		return hasUpdates;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 * 
	 * Submits a potential correlation problem for review. This will add to the
	 * Correlation Error Queue the information specified.
	 * 
	 * @param personReview
	 *            The PersonReview object to submit
	 * 
	 * @return The id assigned to the PersonReview object
	 */
	public int submitReview(PersonReview personReview) throws RemoteException,
			PersonIdServiceException {
		ReviewQueue q = new ReviewQueue();
		q.submit(personReview);
		return personReview.getId();
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 *	
	 * Submits a potential correlation problem for review. This will add to the
	 * Correlation Error Queue the information specified.
	 * 
	 * @param id
	 *            The id of the PersonReview object to delete
	 */
	public void deleteReview(int id) throws EJBException,
			PersonIdServiceException {
		ReviewQueue q = new ReviewQueue();
		q.delete(id);
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 *
	 * Returns a list of PersonReview objects representing the current queue in
	 * the database for the domain specified only.
	 * 
	 * @param domain
	 *            The domain to match against. The domain is determined by
	 *            matching against the domain for the physicians who created the
	 *            review request. If domain is an empty string (zero length)
	 *            this will return all system reviews or those for physicians
	 *            not associated with a domain. If domain is null, all reviews
	 *            are returned.
	 * @return List of PersonReview objects
	 */
	public List getReviews(String domain) throws EJBException,
			PersonIdServiceException {
		ReviewQueue q = new ReviewQueue();
		return q.get(domain);
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 *
	 * Returns a list of all PersonReview objects representing the current queue
	 * in the database. This includes all reviews for all domains and for system
	 * generated review requests.
	 * 
	 * @return List of PersonReview objects
	 */
	public List getAllReviews() throws EJBException,
			PersonIdServiceException {
		return getReviews(null);
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 *
	 * Returns a list of all PersonReview objects generated by the system (those
	 * not associated with a specific domain).
	 * 
	 * @return List of PersonReview objects
	 */
	public List getSystemReviews() throws EJBException,
			PersonIdServiceException {
		return getReviews("");
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 *
	 * Checks if reviews are pending for the domain.
	 * 
	 * @param domain
	 *            The domain to match against. The domain is determined by
	 *            matching against the domain for the physicians who created the
	 *            review request. If domain is null this will return all system
	 *            reviews or those for physicians not associated with a domain.
	 * @return true if reviews are pending, false otherwise.
	 */
	public boolean hasReviews(String domain) throws EJBException,
			PersonIdServiceException {
		return true;
	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */		
	public Person addPersonWithCDECascade(Person newperson, String oldPID)
			throws EJBException, PersonIdServiceException {
		Person person = addPerson(newperson);
		// CorrelationRemote CDECorrelationSystem;
		// try
		// {
		// CDECorrelationSystem = _corhome.create();
		// }
		// catch(CreateException e)
		// {
		// throw new PersonIdServiceException(e);
		// }
		//        
		// Person oldP = getPersonByPID(person, oldPID);
		//        
		// if (oldP != null)
		// {
		// CDECorrelationSystem.updateCDEforA35(oldP.getOid(), person);
		// }
		return person;

	}

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */		
	public Person mergePersonsWithCDECascade(Person newperson, String oldPID,
			String oldEID) throws EJBException, PersonIdServiceException {
		// CorrelationRemote CDECorrelationSystem;
		// try
		// {
		// CDECorrelationSystem = _corhome.create();
		// }
		// catch(CreateException e)
		// {
		// throw new PersonIdServiceException(e);
		// }

		PersonIdentifier person_info = (PersonIdentifier) newperson
				.getPersonIdentifiers().get(0);
		String domain = person_info.getAssigningAuthority().getNameSpaceID();
		String facility = person_info.getAssigningFacility().getNameSpaceID();
		String newPID = person_info.getId();
		String newEID = person_info.getCorpId();

		Person oldPerson = getPersonByPID(newperson, oldPID);
		if (oldPerson == null) {// old PID doesn't exist
			Iterator ids = newperson.getPersonIdentifiers().iterator();
			if (ids.hasNext()) {
				PersonIdentifier pid = (PersonIdentifier) ids.next();
				PersonIdentifier oldPid = new PersonIdentifier();
				oldPid.setDocumentHeader(pid.getDocumentHeader());
				oldPid.setId(oldPID);
				oldPid.setAssigningAuthority(pid.getAssigningAuthority());
				oldPid.setAssigningFacility(pid.getAssigningFacility());
				oldPid.setIdentifierTypeCode(pid.getIdentifierTypeCode());
				oldPid.setCorpId(pid.getCorpId());
				oldPid.setUpdatedCorpId(pid.getUpdatedCorpId());

				newperson.addPersonIdentifier(oldPid);
			} else {
				throw new PersonIdServiceException(
						"Newperson doesn't contain any PersonIdentifiers");
			}
		}

		newperson = addPerson(newperson, false);
		if (newperson == null)
			throw new PersonIdServiceException(
					"Invalid incoming new person.  Check critical fields, like LNAME.");

		// before anything can go wrong, version our EID
		// newperson memory object already in correct state
		// DB update will populate the retrieved merge people with updated state
		// TODO

		// update DB

		try {
			if (newEID != null) {
				// update memory object
				updateEID(newperson, domain, facility, oldPID, oldEID, newEID);

				// update DB
				DatabaseServices dbServices = DatabaseServicesFactory
						.getInstance();
				dbServices.updateEID(domain, facility, oldPID, newPID, oldEID,
						newEID);
				idCache.clear();
			}
		} catch (DatabaseException de) {
			log.error("Unable to update EID (" + domain + ", " + facility
					+ ", " + oldPID + ", " + newPID + ", " + oldEID + ", "
					+ newEID + ")", de);
			throw new PersonIdServiceException("Unable to update EID ("
					+ domain + ", " + facility + ", " + oldPID + ", " + newPID
					+ ", " + oldEID + ", " + newEID + ")");
		}

		// if oldperson == null then there's nobody to merge. The new alias with
		// old PID has already been created.
		// don't try and perform a merge that will fail
		if (oldPerson == null) {
			// check for EID mismatch, possible Admin Queue message...
			checkforEIDMismatch(newperson);
			return newperson;
		}

		// oldPerson = newperson; //demographics from HL7message only
		// doing a new DB search so should have proper updated_corpId values
		Collection folksToMerge = searchByPID(oldPerson,
				new IcsTrace(IcsTrace.MERGE_PERSON, oldPerson)).values();
		if (folksToMerge.size() == 0) {
			// addPerson(oldPerson);
			// check for EID mismatch, possible Admin Queue message...
			checkforEIDMismatch(newperson);
			return newperson;
		}
		List filtedFolksToMerge = new ArrayList();
		filtedFolksToMerge.add(newperson);
		// don't try to merge a person with itself, ICS doesn't like it.
		for (Iterator i = folksToMerge.iterator(); i.hasNext();) {
			Person currentPerson = (Person) i.next();
			if (currentPerson.getOid().compareTo(newperson.getOid()) != 0) {
				filtedFolksToMerge.add(currentPerson);
			}
		}
		Person[] personarray = new Person[1];
		Person mergedPerson = mergePersons((Person[]) filtedFolksToMerge
				.toArray(personarray));
		if (mergedPerson == null) {
			log
					.info("mergePersonsWithCDECascade(): Didn't find anyone to merge");
			// check for EID mismatch, possible Admin Queue message...
			checkforEIDMismatch(newperson);
			return newperson;
		}
		// toss our merged person, CDE call doesn't use it here
		filtedFolksToMerge.remove(0);
		// if(!filtedFolksToMerge.isEmpty())
		// {
		// personarray = new Person[1];
		// CDECorrelationSystem.upDateMergedPatients(mergedPerson, (Person[])
		// filtedFolksToMerge.toArray(personarray));
		// }
		// check for EID mismatch, possible Admin Queue message...
		checkforEIDMismatch(mergedPerson);
		return mergedPerson;
	}

	private Person updateEID(Person oldPerson, String domain, String facility,
			String MRN, String oldEID, String newEID) {
		// foreach alias in the Person, if the 4 part key matches, set the
		// updated_corpId field
		// Damn, where are you Java 5 enhanced for loop
		for (Iterator i = oldPerson.getPersonIdentifiers().iterator(); i
				.hasNext();) {
			PersonIdentifier pi = (PersonIdentifier) i.next();
			String _domain = pi.getAssigningAuthority().getNameSpaceID();
			String _facility = pi.getAssigningFacility().getNameSpaceID();
			String _MRN = pi.getId();
			String _oldEID = pi.getCorpId();

			if ((_domain != null && _domain.equals(domain))
					|| (_domain == null && domain == null)) {
				if (_facility != null && _facility.equals(facility)
						|| (_facility == null && facility == null)) {
					if (_MRN != null && _MRN.equals(MRN)
							|| (_MRN == null && MRN == null)) {
						if (_oldEID != null && _oldEID.equals(oldEID)
								|| (_oldEID == null && oldEID == null)) {
							pi.setUpdatedCorpId(newEID);
						}
					}
				}
			}
		}
		return oldPerson;
	}

	private List getEIDDomains() {
		String eidProperty = ICSProperties.getProperty("EID_DOMAINS", "");

		// Check to see if we really need to reparse, if so, do it
		if (_eidDomainProperty != eidProperty) {
			_eidDomainProperty = eidProperty;
			StringTokenizer st = new StringTokenizer(_eidDomainProperty, ",");
			_eidDomains.clear();
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				_eidDomains.add(token);
			}
		}
		return _eidDomains;
	}

	private void checkforEIDMismatch(Person person)
			throws PersonIdServiceException {
		// foreach domain in this person ensure it only has one unique "virtual
		// EID" (i.e. updated or original)
		// so CCOW doesn't have a fit
		log.debug("Checking for EID mismatch...");
		try {
			Map eidmap = new HashMap();
			String problemDomain = null;
			for (Iterator i = person.getPersonIdentifiers().iterator(); i
					.hasNext();) {
				PersonIdentifier pi = (PersonIdentifier) i.next();
				String domain = pi.getAssigningAuthority().getNameSpaceID();
				String corpId = pi.getCorpId();
				String updatedCorpId = pi.getUpdatedCorpId();

				if (eidmap.containsKey(domain)) {
					String eid = (String) eidmap.get(domain);
					if (updatedCorpId != null) {
						if (!eid.equals(updatedCorpId)) {
							log.debug("EID mismatch found for:\n" + "Domain: "
									+ domain + "\n" + "EID1: " + eid + "\n"
									+ "EID2: " + updatedCorpId);
							problemDomain = domain;
							break;
						}
					} else {
						if (!eid.equals(corpId)) {
							log.debug("EID mismatch found for:\n" + "Domain: "
									+ domain + "\n" + "EID1: " + eid + "\n"
									+ "EID2: " + corpId);
							problemDomain = domain;
							break;
						}
					}
				} else {
					String veid = (updatedCorpId != null) ? updatedCorpId
							: corpId;
					if (veid != null) {
						eidmap.put(domain, veid);
					}
				}
			}

			if (problemDomain != null) {
				// check to see if we care about this problemDomain
				if (!getEIDDomains().contains(problemDomain)) {
					log
							.debug("EID mismatch exists but alerting is not configured for domain: "
									+ problemDomain);
				} else {
					String description = "Multiple EID attributes exist for single CDE person";
					if (!new ReviewQueue().exists(description, person)) {
						PersonReview r = new PersonReview();
						r.setDescr(description);
						r.setDomainId(problemDomain);
						r.setUserId("System");
						r.addPerson(person);
						submitReview(r);
						log.debug("EID mismatch PersonReview sent.  Domain: "
								+ problemDomain);
					} else {
						log
								.debug("EID mismatch exists but is already recorded in Review Queue");
					}
				}
			} else {
				log.debug("No EID mismatch found");
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e, e);
			throw new PersonIdServiceException(e.getMessage());
		}
	}
}
