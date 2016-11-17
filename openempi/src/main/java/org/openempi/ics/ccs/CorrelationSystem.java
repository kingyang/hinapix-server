/*
 * Any use of the Material is governed by the terms of the actual license
 * agreement between CareScience and the user. Any reproduction or
 * redistribution, by any means - whether mechanical or electronic -
 * without the express written permission of CareScience is strictly
 * prohibited. The Material includes information protected by copyrights
 * and/or patents held by CareScience and/or the University of
 * Pennsylvania.
 *
 * It is understood by users of this Material that the information
 * contained herein is intended to serve as a guide and basis for general
 * comparisons only, and NOT as the sole basis upon which any specific
 * action is to be recommended or undertaken. All users of this site and
 * its data agree to hold CareScience harmless from any and all claims,
 * losses, damages, obligations or liabilities, directly or indirectly
 * relating to these materials, caused thereby or arising therefrom.
 *
 * CareScience assumes no responsibility for errors or omission in these
 * materials. CareScience does not warrant the accuracy or completeness of
 * the information, text, graphics, links or other items contained within
 * these materials. CareScience shall not be liable for any special,
 * indirect, incidental, or consequential damages, including without
 * limitation, lost revenues or lost profits, which may result from the use
 * of these materials. CareScience may make changes to these materials, or
 * to the products described therein, at any time without notice.
 *
 * THESE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT.
 *
 * THE DOCUMENTS AND RELATED GRAPHICS PUBLISHED IN THIS DOCUMENT COULD
 * INCLUDE TECHNICAL INACCURACIES OR TYPOGRAPHICAL ERRORS. CHANGES ARE
 * PERIODICALLY ADDED TO THE MATERIAL HEREIN. CareScience AND/OR ITS
 * SUPPLIERS MAY MAKE IMPROVEMENTS AND/OR CHANGES IN THE PRODUCT(S) AND/OR
 * THE PROGRAM(S) DESCRIBED HEREIN AT ANY TIME.
 *
 * � Copyright 1998-2002 CareScience, 3600 Market Street, 6th Floor
 * Philadelphia, PA 19104 U.S.A.
 *
 * All rights reserved. Any rights not expressly granted herein are reserved.
 */
package org.openempi.ics.ccs;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openempi.data.*;
import org.openempi.ics.db.AttributeType;
import org.openempi.ics.db.DatabaseException;
import org.openempi.ics.db.DatabaseServices;
import org.openempi.ics.db.DatabaseServicesFactory;
import org.openempi.ics.db.QueryParamList;
import org.openempi.ics.utility.IcsTrace;
import org.openempi.ics.utility.Profile;

/**
 * This class is responsible for making correlations between new and existing
 * patient demographic data. Using probabilistic and neural network algorithms,
 * the correlation system analyzes existing master person index data to
 * determine potential duplicate records. The system identifies persons with
 * multiple or potential duplicate records in the data store.
 *
 * @author CareScience
 * @author Bryan TerBush
 * @author Feijian Sun
 * @author Karl Fankhauser
 * @version 1.43, 20020528
 */
public class CorrelationSystem implements Serializable {
    public static final String dateFormat = "yyyyMMdd";
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Almost exact match confidence level.
     */
    public static final double ALMOST_EXACT_MATCH = 0.99;

    /**
     * Approximate match confidence level.
     */
    public static final double APPROXIMATE = 0.89;

    /**
     * Exact match confidence level.
     */
    public static final double EXACT_MATCH = 1.0;

    /**
     * Similar match confidence level.
     */
    public static final double SIMILAR = 0.6;

    /**
     * Unknown match confidence level.
     */
    public static final double UNKNOWN = 0.5;

    /**
     * No match confidence level.
     */
    public static final double NO_MATCH = 0.0;

    public static final int MAX_MATCHES = 1000000;

    // Cache an empty Person for comparison purposes
    private static final Person EMPTY_PERSON = new Person();
    private static final int TRAIT_COUNT = 9;

    // The following are used in the attrs parameters of the matchByAttributes
    // method
    private static final Integer ATTR_NAME = new Integer(0);
    private static final Integer ATTR_SSN = new Integer(1);
    private static final Integer ATTR_GENDER = new Integer(2);
    private static final Integer ATTR_DOB = new Integer(3);
    private static final Integer ATTR_ADDRESS = new Integer(4);
    private static Logger log = Logger.getLogger("ICS");
    private AttributeComparator comparator = new AttributeComparator();
    private List listeners = new ArrayList();
    private SearchKeyGenerator searchKeyGen = SearchKeyGenerator.getInstance();
    private IcsTrace trace = null;

    /**
     * Constructs a CorrelationSystem.
     *
     * @throws org.openempi.ics.ccs.CorrelationSystemException
     */
    public CorrelationSystem(IcsTrace trace) {
        this.trace = trace;
    }

    /**
     * Constructs a CorrelationSystem.
     *
     * @throws org.openempi.ics.ccs.CorrelationSystemException
     */
    public CorrelationSystem() {
        this(new IcsTrace(0, null));
    }

    /**
     * Adds a <code>CorrelationSystemListener</code>
     */
    public void addListener(CorrelationSystemListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a <code>CorrelationSystemListener</code>
     */
    public boolean removeListener(CorrelationSystemListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Compares the search criteria specified in the person paramter
     * against the given candidate to see if the candidate would qualify
     * to return in a search.  The match must be equal to or above
     * the given accuracy to match.  The List of attr specifies which attribute
     * of the person will be compared.  Only those specified in the attrs
     * List are compared, all others are ignored.
     * <p/>
     * This method is meant for UI searching only and is not meant for
     * finding appropriate candidates for correlation.
     *
     * @param person    the Person containing the search criteria
     * @param candidate a Person to check the search criteria against
     * @param accuracy  A value bewteen 0 and 1 indicate the confidence level
     * @param attrs     List of attributes to match against.
     *                  These are the ATTR_* defined above.
     * @return true if the candidate matches, false otherwise.
     */
    private boolean matchByAttributes(Person person, Person candidate,
                                      double accuracy, List attrs) {
        double SSN = 0.0, FN = 0.0, LN = 0.0, MB = 0.0, DB = 0.0, YB = 0.0, ZIP = 0.0, IDMATCH = 0.0;

        if (person == null || candidate == null)
            return false;

        // If we don't care about the match algorithm (used for partial search queries) then the candidates are fine.
        if (accuracy == 0) {
            return true;
        }

        // ID Match
        if (person.getPersonIdentifiers().size() > 0 && candidate.getPersonIdentifiers().size() > 0) {
            Iterator ids = person.getPersonIdentifiers().iterator();
            boolean notFound = true;
            while (ids.hasNext() && notFound) {
                PersonIdentifier id = (PersonIdentifier) ids.next();
                Iterator innerIds = candidate.getPersonIdentifiers().iterator();
                while (innerIds.hasNext()) {
                    PersonIdentifier candidateId = (PersonIdentifier) innerIds.next();
                    if (id.getId().equalsIgnoreCase(candidateId.getId()) &&
                            id.getAssigningAuthority().getNameSpaceID().equalsIgnoreCase(candidateId.getAssigningAuthority().getNameSpaceID())) {
                        notFound = false;
                        IDMATCH = 1.0;
                        break;
                    }
                }

            }
            if (IDMATCH == 1.0) {
                return true;
            }
        }

        // SSN + FN + LN
        if (person.getSocialSecurityNumbers().size() > 0 && candidate.getSocialSecurityNumbers().size() > 0) {
            Iterator ssns1 = person.getSocialSecurityNumbers().iterator();
            if (ssns1.hasNext()) {
                SocialSecurityNumber ssn1 = (SocialSecurityNumber) ssns1.next();

                Iterator ssns2 = candidate.getSocialSecurityNumbers().iterator();
                if (ssns2.hasNext()) {
                    SSN = comparator.compare(ssn1.getSSN(), ((SocialSecurityNumber) ssns2.next()).getSSN(), "");  // last string is service
                }
            }
        }

        if (person.getNames().size() > 0 && candidate.getNames().size() > 0) {
            Iterator names1 = person.getNames().iterator();

            if (names1.hasNext()) {
                PersonName name1 = (PersonName) names1.next();

                Iterator names2 = candidate.getNames().iterator();
                if (names2.hasNext()) {
                    PersonName name2 = (PersonName) names2.next();
                    FN = comparator.compare(name1.getFirstName(), name2.getFirstName(), ""); // last string is service
                    LN = comparator.compare(name1.getLastName(), name2.getLastName(), ""); // last string is service
                }
            }
        }

        if (SSN == 1.0 && FN == 1.0 && LN == 1.0)
            return true;

        // SSN + YB + MB + DB
        if (person.getDatesOfBirth().size() > 0 && candidate.getDatesOfBirth().size() > 0) {
            Iterator dobs1 = person.getDatesOfBirth().iterator();

            if (dobs1.hasNext()) {
                DateOfBirth dob1 = (DateOfBirth) dobs1.next();
                Iterator dobs2 = candidate.getDatesOfBirth().iterator();

                if (dobs2.hasNext()) {
                    DateOfBirth dob2 = (DateOfBirth) dobs2.next();

                    GregorianCalendar c1 = new GregorianCalendar();
                    GregorianCalendar c2 = new GregorianCalendar();

                    c1.setTime(dob1.getDOB());
                    c2.setTime(dob2.getDOB());

                    if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        YB = 1.0;
                    if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH))
                        MB = 1.0;
                    if (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH))
                        DB = 1.0;
                }
            }
        }
        if (SSN == 1.0 && YB == 1.0 && MB == 1.0 && DB == 1.0)
            return true;

        // SSN + FN + YB + ZIP
        if (person.getAddresses().size() > 0 && candidate.getAddresses().size() > 0) {
            Iterator addresses1 = person.getAddresses().iterator();

            if (addresses1.hasNext()) {
                Address address1 = (Address) addresses1.next();
                Iterator addresses2 = candidate.getAddresses().iterator();

                if (addresses2.hasNext()) {
                    Address address2 = (Address) addresses2.next();
                    ZIP = comparator.compare(address1.getZipCode(), address2.getZipCode(), "");
                }
            }
        }
        if (SSN == 1.0 && FN == 1.0 && YB == 1.0 && ZIP == 1.0)
            return true;

        // FN + LN + YB + MB + DB
        if (FN == 1.0 && LN == 1.0 && YB == 1.0 && MB == 1.0 && DB == 1.0)
            return true;

        return false;
    /*
    Profile.begin("CorrelationSystem.matchByAttribute");
    boolean ret = true;
    Integer attr;
    
    if ((person != null) && (candidate != null) && attrs != null) {
      Iterator iter = attrs.iterator();
      
      while(iter.hasNext()) {
        attr = (Integer) iter.next();
        
        // get the attribute type
        if (attr == ATTR_NAME) {
          // check for the name correlation factor
          if (calculateNamesCor(person, candidate, true) < accuracy)
            ret = false;
        } else if (attr == ATTR_SSN) {
          // check for the name correlation factor
          if (calculateSSNsCor(person, candidate) < accuracy)
            ret = false;
        } else if (attr == ATTR_DOB) {
          // check for the DOB correlation factor
          if (calculateDOBsCor(person, candidate) < accuracy)
            ret = false;
        } else if (attr == ATTR_ADDRESS) {
          // check for the Address correlation factor
          if (calculateAddressesCor(person, candidate) < accuracy)
            ret = false;
        } else if (attr == ATTR_GENDER) {
          // check for the Gender correlation factor
          if (calculateGendersCor(person, candidate) < accuracy)
            ret = false;
        } else {
          log.warn("matchByAttribute(): Invalid argument passed to the " + "switch satement");
        }
      }
    }
    Profile.end("CorrelationSystem.matchByAttribute");
    return ret;
    */
    }

    /**
     * This method is used for perform searches for Persons in the database.
     * The person parameters is treated as seasrch criteria and only
     * attributes filled in for the person are used to perform the search.
     * The confidence indicated the minimum level of the match that all
     * returned persons must meet.
     * <p/>
     * This method is NOT appropriate for use in correlation, only UI searching.
     *
     * @param person     the person to search for
     * @param confidence the confidence level (0 - 1)
     * @return Iterator over matches
     */
    public LookUpObj lookUpStart(Person person, double confidence) {
        Profile.begin("CorrelationSystem.lookupStart");
        Iterator iter = null;
        LookUpObj ret = null;
        ArrayList attrsList = new ArrayList();

        try {
            if (person != null && !person.equals(EMPTY_PERSON)) {

                if (person.getSocialSecurityNumbers().size() > 0) {
                    attrsList.add(ATTR_SSN);
                }

                if (person.getNames().size() > 0) {
                    attrsList.add(ATTR_NAME);
                }

                if (person.getDatesOfBirth().size() > 0) {
                    attrsList.add(ATTR_DOB);
                }

                if (person.getAddresses().size() > 0) {
                    attrsList.add(ATTR_ADDRESS);
                }

                if (person.getGenders().size() > 0) {
                    attrsList.add(ATTR_GENDER);
                }

                iter = queryPerson(person, true);

                ret = new LookUpObj(person, iter, confidence, attrsList);
            }
        } catch (RuntimeException e) {
            throw e;
        } finally {
        }

        Profile.end("CorrelationSystem.lookupStart");
        return ret;
    }

    public LookUpObj lookUpStart(Person person) {
        Profile.begin("CorrelationSystem.lookupStart");
        Iterator iter = null;
        LookUpObj ret = null;
        ArrayList attrsList = new ArrayList();

        try {
            if (person != null && !person.equals(EMPTY_PERSON)) {

                if (person.getSocialSecurityNumbers().size() > 0) {
                    attrsList.add(ATTR_SSN);
                }

                if (person.getNames().size() > 0) {
                    attrsList.add(ATTR_NAME);
                }

                if (person.getDatesOfBirth().size() > 0) {
                    attrsList.add(ATTR_DOB);
                }

                if (person.getAddresses().size() > 0) {
                    attrsList.add(ATTR_ADDRESS);
                }

                if (person.getGenders().size() > 0) {
                    attrsList.add(ATTR_GENDER);
                }

                iter = queryPerson(person, true);

                ret = new LookUpObj(person, iter, 1.0, attrsList);
            }
        } catch (RuntimeException e) {
            throw e;
        } finally {
        }

        Profile.end("CorrelationSystem.lookupStart");
        return ret;
    }

    /**
     * This method is used for perform searches for Persons in the database.
     * The person parameters is treated as seasrch criteria and only
     * attributes filled in for the person are used to perform the search.
     * The confidence indicated the minimum level of the match that all
     * returned persons must meet.
     * <p/>
     * This method is NOT appropriate for use in correlation, only UI searching.
     *
     * @param obj        The LookUpObj returned by lookUpStart
     * @param maxMatches Maximum number of returned Persons, or 0 for no limit
     * @return List of matches
     */
    public List lookUpNext(LookUpObj obj, int maxMatches) {

        Profile.begin("CorrelationSystem.lookupNext");

        List ret = findMatches(obj.person, obj.iter, obj.confidence,
                maxMatches, obj.attrsList);
        Profile.end("CorrelationSystem.lookupNext");
        return ret;
    }

    /**
     * This method is used for perform searches for Persons in the database.
     * The person parameters is treated as seasrch criteria and only
     * attributes filled in for the person are used to perform the search.
     * The confidence indicated the minimum level of the match that all
     * returned persons must meet.
     * <p/>
     * This method is NOT appropriate for use in correlation, only UI searching.
     *
     * @param obj The LookUpObj returned by lookUpStart
     * @param
     * @return List of matches
     */
    public void lookUpEnd(LookUpObj obj) {

        Profile.begin("CorrelationSystem.lookupEnd");
        queryDone(obj.iter);
        obj.iter = null;
        Profile.end("CorrelationSystem.lookupEnd");
    }

    /**
     * This method is used for perform searches for Persons in the database.
     * The person parameters is treated as search criteria and only
     * attributes filled in for the person are used to perform the search.
     * The confidence indicated the minimum level of the match that all
     * returned persons must meet.
     * <p/>
     * This method is NOT appropriate for use in correlation, only UI searching.
     *
     * @param person     the person to search for
     * @param confidence the confidence level (0 - 1)
     * @param maxMatches Maximum number of returned Persons, or 0 for no limit
     * @return List of matches
     */
    public List lookUp(Person person, double confidence, int maxMatches) {

        Profile.begin("CorrelationSystem.lookup");
        LookUpObj obj = null;
        List matches = null;

        try {
            obj = lookUpStart(person, confidence);
            matches = lookUpNext(obj, maxMatches);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            if (obj != null)
                lookUpEnd(obj);
        }

        Profile.end("CorrelationSystem.lookup");
        return matches;
    }

    public List lookUp(Person person) {

        Profile.begin("CorrelationSystem.lookup");
        LookUpObj obj = null;
        List matches = null;

        try {
            obj = lookUpStart(person);
            matches = lookUpNext(obj, MAX_MATCHES);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            if (obj != null)
                lookUpEnd(obj);
        }

        Profile.end("CorrelationSystem.lookup");
        return matches;
    }

    /**
     * Compares two <code>Person</code> using their demographic data to obtain a
     * correlation factor.  This is use by the correlation system to determine
     * if the match person qualified for correlation.  This is NOT appropriate
     * for use in general UI searching.
     *
     * @param person the <code>Person</code> to be compared
     * @param match  the <code>Person</code> to be compared against
     * @return double the output correlation factor
     */
    public double comparePersons(Person person, Person match) {
        double cor = CorrelationSystem.UNKNOWN;

        try {
            double[] correlations = buildCorrelationVector(person, match);
//      double[] output = neuralNetwork.calculateOutputs(correlations);
            double[] output = {};
            cor = output[0];

            if (IcsTrace.isEnabled()) {
                trace.add("Checking correlation with:");
                trace.add(match, true);
                trace.add("names = " + correlations[0]);
                trace.add("SSNs = " + correlations[1]);
                trace.add("genders = " + correlations[2]);
                trace.add("birthdates = " + correlations[3]);
                trace.add("addresses = " + correlations[4]);
                trace.add("birthplaces = " + correlations[5]);
                trace.add("phone numbers = " + correlations[7]);
                trace.add("Correlation = " + cor);
            }
        } catch (CorrelationSystemException csEx) {
            log.error(csEx, csEx);
        }

        return cor;
    }

    /**
     * Finds potential Person matches based on demographic data.  Given the
     * Person to match against it will compare this Person to each Person
     * in the people Iterator.  Matches will be returned in the List up
     * to maxMatches.  Only attributes specified in the attrs list will
     * be matches against, all others ignored.
     * <p/>
     * This method is used for ui searching only and is not appropriate for
     * use in correlation.
     *
     * @param person     the <code>Person</code> to search for
     * @param people     Iterator of Persons to match
     * @param accuracy   accuracy (0 - 1) for neural net matching
     * @param maxMatches The maximum number of matches to be returned in the
     *                   list, or 0 for unlimited.
     * @param attrs      List of ATTR_* to match in the person object
     * @return List of Person matches
     */
    private List findMatches(Person person, Iterator people, double accuracy,
                             int maxMatches, List attrs) {
        Profile.begin("CorrelationSystem.findMatches");
        List matches = new ArrayList();

        if (person != null && people != null) {
            Person candidate = null;

            Iterator itr = people;

            while (itr.hasNext()) {
                candidate = (Person) itr.next();

                if (matchByAttributes(person, candidate, accuracy, attrs))
                    matches.add(candidate);

                if (maxMatches > 0 && matches.size() >= maxMatches)
                    break;
            }
        }

        Profile.end("CorrelationSystem.findMatches");

        return matches;
    }

    /**
     * Builds correlations between two <code>Person</code>s' demographic attributes.
     * <p/>
     * <code> correlations[] </code> holds a vector of each of the elements that
     * will have a correlation value calculated.  In order these elements are:
     * correlations[0] = persons names
     * correlations[1] = social security number
     * correlations[2] = gender
     * correlations[3] = date of birth
     * correlations[4] = addresses
     * correlations[5] = birth place
     * correlations[6] = maiden name
     * correlations[7] = telephone numbers
     * correlations[8] = driver's licenses
     * <p/>
     * This represents a trait count of 9. The number of traits compared will
     * impact the number of nodes in the neural network and the training of the
     * neural network as well.
     *
     * @param person
     * @param candidate
     * @return double[] the correlation vector
     * @throws org.openempi.ics.ccs.CorrelationSystemException
     */
    public double[] buildCorrelationVector(Person person, Person candidate)
            throws CorrelationSystemException {
        Profile.begin("CorrelationSystem.buildCorrelationVector");
        double[] correlations = null;

        if ((person != null) && (candidate != null)) {
            correlations = new double[TRAIT_COUNT];

            // Correlate names (whether alias or primary); names with maiden names as well
            correlations[0] = calculateNamesCor(person, candidate, false);

            // Correlate SSNs.
            correlations[1] = calculateSSNsCor(person, candidate);

            // Correlate genders.
            correlations[2] = calculateGendersCor(person, candidate);

            // Correlate birthdates.

            correlations[3] = calculateDOBsCor(person, candidate);

            // Correlate addresses.
            correlations[4] = calculateAddressesCor(person, candidate);

            // Correlate places of birth.
            correlations[5] = calculateBirthPlaceCor(person.getBirthPlace(),
                    candidate.getBirthPlace());

            // Correlate maiden name with maiden names, then maiden name with other names.
            correlations[6] = calculateNameCor(person.getMaidenName(),
                    candidate.getMaidenName(), false);

            // Correlate telephone numbers.
            correlations[7] = calculateTelephonesCor(person, candidate);

            // Correlate drivers licenses.
            correlations[8] = calculateDriverLicensesCor(person, candidate);
        }
        Profile.end("CorrelationSystem.buildCorrelationVector");
        return correlations;
    }

    /**
     * Checks two strings for exact matches.
     *
     * @return correlation between the two <code>string</code>s.
     */
    private double calculateExactStringCor(String s1, String s2) {
        Profile.begin("CorrelationSystem.calculateExactStringCor");
        double match = UNKNOWN;

        if ((s1 != null) && (s2 != null)) {
            if (s1.trim().equalsIgnoreCase(s2.trim())) {
                match = EXACT_MATCH;
            }
        }
        Profile.end("CorrelationSystem.calculateExactStringCor");
        return match;
    }

    /**
     * Calculates the correlation between two <code>Person</code>s' names.
     * If the search parameter is true, additional behavior for comparing
     * persons names is invoked.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param person
     * @param candidate
     * @param search    If true, special name searching logic is invoked.
     * @return correlation between two <code> Persons</code>s' <code> names
     */
    private double calculateNamesCor(Person person, Person candidate,
                                     boolean search) {
        Profile.begin("CorrelationSystem.calculateNamesCor");
        double maxCorrelation = 0.0;

        if (person != null && candidate != null &&
                person.getNames().size() > 0 &&
                candidate.getNames().size() > 0) {
            Iterator names1 = person.getNames().iterator();

            while (names1.hasNext()) {
                PersonName name = (PersonName) names1.next();
                Iterator names2 = candidate.getNames().iterator();

                while (names2.hasNext()) {
                    double cor = calculateNameCor(name,
                            (PersonName) names2.next(),
                            search);

                    if (cor > maxCorrelation) {
                        maxCorrelation = cor;
                    }
                }
            }
        } else
            maxCorrelation = UNKNOWN;

        Profile.end("CorrelationSystem.calculateNamesCor");
        return maxCorrelation;
    }

    /**
     * Calculates the correlation between two names.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param name1  Name to compare to name2
     * @param name2  Name to compare to name1
     * @param search If true, it is assumed this is used for searching
     *               and name1 matches name2 at 1.0 confidence if name2
     *               starts with name1
     * @return correlation between the two names
     */
    private double calculateNameCor(PersonName name1, PersonName name2,
                                    boolean search) {
        Profile.begin("CorrelationSystem.calculateNameCor");
        double correlation = 0;

        if ((name1 != null) && (name2 != null)) {
            if ((name1.getLastName() != null) && (name2.getLastName() != null)) {
                if (search) {
                    // Checks if lastName of name2 starts with lastName of name1
                    if (name2.getLastName().startsWith(name1.getLastName().toUpperCase())) {
                        // Checks if firstName of name2 starts with firstName of name1
                        if ((name1.getFirstName() != null) && (name2.getFirstName() != null)) {
                            if (name2.getFirstName().startsWith(name1.getFirstName().toUpperCase())) {
                                Profile.end("CorrelationSystem.calculateNameCor");
                                return 1.0;
                            }
                        } else {
                            Profile.end("CorrelationSystem.calculateNameCor");
                            return 1.0;
                        }
                    }
                }

                String str1 = name1.getFirstName() + " " + name1.getSecondName() + " " +
                        name1.getLastName();
                String str2 = name2.getFirstName() + " " + name2.getSecondName() + " " +
                        name2.getLastName();

                // add suffix - useful to determine father/son relationships
                if (name1.getSuffix() != null) {
                    str1 = str1 + " " + name1.getSuffix();
                }

                if (name2.getSuffix() != null) {
                    str2 = str2 + " " + name2.getSuffix();
                }
                correlation = comparator.compareNames(str1, str2);
            } else {
                log.error(" - error in Person Name;No last Name");
            }
        } else
            correlation = UNKNOWN;
        Profile.end("CorrelationSystem.calculateNameCor");
        return correlation;
    }

    /**
     * Calculates the correlation between two <code>Person</code>s' addresses.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param person
     * @param candidate
     * @return correlation between two addresses
     */
    private double calculateAddressesCor(Person person, Person candidate) {
        Profile.begin("CorrelationSystem.calculateAddressesCor");
        double maxCorrelation = 0;

        if (person != null && candidate != null &&
                person.getAddresses().size() > 0 &&
                candidate.getAddresses().size() > 0) {
            Iterator addresses1 = person.getAddresses().iterator();

            while (addresses1.hasNext()) {
                Address address1 = (Address) addresses1.next();
                Iterator addresses2 = candidate.getAddresses().iterator();

                while (addresses2.hasNext()) {
                    double cor = calculateAddressCor(address1, (Address) addresses2.next());

                    if (cor > maxCorrelation) {
                        maxCorrelation = cor;
                    }
                }
            }
        } else
            maxCorrelation = UNKNOWN;

        Profile.end("CorrelationSystem.calculateAddressesCor");
        return maxCorrelation;
    }

    private double calculateSSNsCor(Person person, Person candidate) {
        Profile.begin("CorrelationSystem.calculateSSNsCor");
        double maxCorrelation = 0;

        if (person != null && candidate != null &&
                person.getSocialSecurityNumbers().size() > 0 &&
                candidate.getSocialSecurityNumbers().size() > 0) {
            Iterator ssns1 = person.getSocialSecurityNumbers().iterator();

            while (ssns1.hasNext()) {
                SocialSecurityNumber ssn1 = (SocialSecurityNumber) ssns1.next();
                Iterator ssns2 = candidate.getSocialSecurityNumbers().iterator();

                while (ssns2.hasNext()) {
                    double cor = calculateSSNCor(ssn1, (SocialSecurityNumber) ssns2.next());

                    if (cor > maxCorrelation) {
                        maxCorrelation = cor;
                    }
                }
            }
        } else
            maxCorrelation = UNKNOWN;

        Profile.end("CorrelationSystem.calculateSSNsCor");
        return maxCorrelation;
    }

    private double calculateDOBsCor(Person person, Person candidate) {
        Profile.begin("CorrelationSystem.calculateDOBsCor");
        double maxCorrelation = 0;

        if (person != null && candidate != null &&
                person.getDatesOfBirth().size() > 0 &&
                candidate.getDatesOfBirth().size() > 0) {
            Iterator dobs1 = person.getDatesOfBirth().iterator();

            while (dobs1.hasNext()) {
                DateOfBirth dob1 = (DateOfBirth) dobs1.next();
                Iterator dobs2 = candidate.getDatesOfBirth().iterator();

                while (dobs2.hasNext()) {
                    double cor = calculateDOBCor(dob1, (DateOfBirth) dobs2.next());

                    if (cor > maxCorrelation) {
                        maxCorrelation = cor;
                    }
                }
            }
        } else
            maxCorrelation = UNKNOWN;

        Profile.end("CorrelationSystem.calculateDOBsCor");
        return maxCorrelation;
    }

    /**
     * Calculates the correlation between two addresses.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param address1 the first <code>Address</code>
     * @param address2 the second <code>Address</code>
     * @return correlation between the two <code>Address</code> objects
     */
    private double calculateAddressCor(Address address1, Address address2) {
        Profile.begin("CorrelationSystem.calculateAddressesCor");
        double correlation = UNKNOWN;

        if ((address1 != null) && (address2 != null)) {
            String str1 = getNonNullString(address1.getAddress1()) + " " + getNonNullString(address1.getAddress2());
            String str2 = getNonNullString(address2.getAddress1()) + " " + getNonNullString(address2.getAddress2());

            // Check if street is input.
            boolean streetPres = false;

            if (!str1.equals(" ")) {
                streetPres = true;
            }

            double streetCor = comparator.compareStreets(str1.trim(), str2.trim());
            str1 = getNonNullString(address1.getCity());
            str2 = getNonNullString(address2.getCity());

            // Check if City is input.
            boolean cityPres = false;

            if ((address1.getCity() != null) && (!address1.getCity().equals(""))) {
                cityPres = true;
            }
            double cityCor = comparator.alfaCompare(str1.trim(), str2.trim());
            str1 = getNonNullString(address1.getState());
            str2 = getNonNullString(address2.getState());

            // Check if State is input.
            boolean statePres = false;

            if ((address1.getState() != null) && (!address1.getState().equals("default")
                    && (!address1.getState().equals("")))) {
                statePres = true;
            }

      /* In order to provide for a better search capability from the
         CDE GUI point, the state correlation is being removed from
         the Intelligent Search routine
         A simple string match is now used to return a correlated
         (1.0) or a non-correlated (0.0) value
      */
            //double stateCor = comparator.alfaCompare(str1.trim(), str2.trim());
            double stateCor = 0.0;
            if (str1 != null && str1.equalsIgnoreCase(str2)) {
                stateCor = 1.0;
            }

            str1 = getNonNullString(address1.getZipCode());

            // We only compare the first 5 digits of a zip code.
            if (str1.length() > 5) {
                str1 = str1.substring(0, 6);
            }

            str2 = getNonNullString(address2.getZipCode());

            if (str2.length() > 5) {
                str2 = str2.substring(0, 6);
            }

            // Check if Zip is input.
            boolean zipPres = false;

            if ((address1.getZipCode() != null) && (!address1.getZipCode().equals(""))) {
                zipPres = true;
            }

            double zipCor = comparator.numCompare(str1, str2);

            // All the four parts of an Address are present.
            if (streetPres && cityPres && statePres && zipPres) {
                correlation = (streetCor + cityCor + stateCor + zipCor) / 4.0;
            }

            // Only three of the four parts of an Address are present
            else if (streetPres && cityPres && statePres && !zipPres) {
                correlation = (streetCor + cityCor + stateCor) / 3.0;
            } else if (!streetPres && cityPres && statePres && zipPres) {
                correlation = (cityCor + stateCor + zipCor) / 3.0;
            } else if (streetPres && !cityPres && statePres && zipPres) {
                correlation = (streetCor + stateCor + zipCor) / 3.0;
            } else if (streetPres && cityPres && !statePres && zipPres) {
                correlation = (streetCor + cityCor + zipCor) / 3.0;
            }

            // only one of the four parts of an Address is present
            else if (streetPres && !cityPres && !statePres && !zipPres) {
                correlation = streetCor / EXACT_MATCH;
            } else if (!streetPres && cityPres && !statePres && !zipPres) {
                correlation = cityCor / EXACT_MATCH;
            } else if (!streetPres && !cityPres && statePres && !zipPres) {
                correlation = stateCor / EXACT_MATCH;
            } else if (!streetPres && !cityPres && !statePres && zipPres) {
                correlation = zipCor / EXACT_MATCH;
            }

            // only two of the four parts of an Address are present
            else if (streetPres) {
                if (cityPres) {
                    correlation = (streetCor + cityCor) / 2.0;
                } else if (statePres) {
                    correlation = (streetCor + stateCor) / 2.0;
                } else if (zipPres) {
                    correlation = (streetCor + zipCor) / 2.0;
                }
            } else if (cityPres) {
                if (statePres) {
                    correlation = (cityCor + stateCor) / 2.0;
                } else if (zipPres) {
                    correlation = (cityCor + zipCor) / 2.0;
                }
            } else if (statePres) {
                if (zipPres) {
                    correlation = (stateCor + zipCor) / 2.0;
                }
            }
        } else
            correlation = UNKNOWN;
        Profile.end("CorrelationSystem.calculateAddressesCor");
        return correlation;
    }

    private double calculateSSNCor(SocialSecurityNumber ssn1,
                                   SocialSecurityNumber ssn2) {
        Profile.begin("CorrelationSystem.calculateSSNCor");
        double correlation = UNKNOWN;

        if ((ssn1 != null) && (ssn2 != null)) {
            if (ssn1.getSSN() != null && ssn2.getSSN() != null)
                correlation = comparator.numCompare(ssn1.getSSN(), ssn2.getSSN());
        }
        Profile.end("CorrelationSystem.calculateSSNCor");
        return correlation;
    }

    private double calculateDOBCor(DateOfBirth dob1,
                                   DateOfBirth dob2) {
        Profile.begin("CorrelationSystem.calculateDOBCor");
        double correlation = UNKNOWN;

        if ((dob1 != null) && (dob2 != null)) {
            if (dob1.getDOB() != null && dob2.getDOB() != null)
                correlation = comparator.compareDates(dob1.getDOB(), dob2.getDOB());
        }
        Profile.end("CorrelationSystem.calculateDOBCor");
        return correlation;
    }

    /**
     * Calculates the correlation between two BirthPlace addresses.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param address1 the first address
     * @param address2 the second address
     * @return correlation between the two addresses
     */
    private double calculateBirthPlaceCor(String address1, String address2) {
        double correlation = UNKNOWN;

        if ((address1 != null) && (address2 != null)) {
            correlation = comparator.alfaCompare(address1.trim(), address2.trim());
        }

        return correlation;
    }

    /**
     * Calculates the correlation between two <code>Person</code>s' phone numbers.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param person
     * @param candidate
     * @return correlation between two phone numbers
     */
    private double calculateTelephonesCor(Person person, Person candidate) {
        double maxCorrelation = 0;

        if (person != null && candidate != null &&
                person.getTelephoneNumbers().size() > 0 &&
                candidate.getTelephoneNumbers().size() > 0) {
            Iterator phones1 = person.getTelephoneNumbers().iterator();

            while (phones1.hasNext()) {
                TelephoneNumber tn = (TelephoneNumber) phones1.next();
                Iterator phones2 = candidate.getTelephoneNumbers().iterator();

                while (phones2.hasNext()) {
                    double cor = calculateTelephoneCor(tn, (TelephoneNumber) phones2.next());

                    if (cor > maxCorrelation) {
                        maxCorrelation = cor;
                    }
                }
            }
        } else
            maxCorrelation = UNKNOWN;

        return maxCorrelation;
    }

    /**
     * Calculates the correlation between two phone numbers.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param phone1 the first <code>TelephoneNumber</code>
     * @param phone2 the second <code>TelephoneNumber</code>
     * @return correlation between the two <code>TelephoneNumber</code> objects
     */
    private double calculateTelephoneCor(TelephoneNumber phone1, TelephoneNumber phone2) {
        double correlation = UNKNOWN;

        if ((phone1 != null) && (phone2 != null)) {
            int counter = 1;
            double pnCor = NO_MATCH;
            double areaCor = NO_MATCH;
            double extCor = NO_MATCH;

            // todo: logic to look at the phone types...
            if (phone1.getAreaCode() != null && phone2.getAreaCode() != null) {
                areaCor = calculateExactStringCor(getNonNullString(phone1.getAreaCode()),
                        getNonNullString(phone2.getAreaCode()));
                counter++;
            }

            if (phone1.getPhoneNumber() == null || phone2.getPhoneNumber() == null) {
                pnCor = UNKNOWN;
            } else {
                // use alfa compare on this one just in case the phone string is all
                // merged together.
                pnCor = comparator.alfaCompare(getNonNullString(phone1.getPhoneNumber()),
                        getNonNullString(phone2.getPhoneNumber()));
            }

            if (phone1.getExtension() != null && phone2.getExtension() != null) {
                extCor = calculateExactStringCor(getNonNullString(phone1.getExtension()),
                        getNonNullString(phone2.getExtension()));
                counter++;
            }
            correlation = (areaCor + pnCor + extCor) / counter;
        }
        return correlation;
    }

    /**
     * Calculates the correlation between two <code>Person</code>s' drivers licenses.
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param person
     * @param candidate
     * @return correlation between two drivers licenses
     */
    private double calculateDriverLicensesCor(Person person, Person candidate) {
        double correlation = 0;

        if (person != null && candidate != null &&
                person.getDriversLicenses().size() > 0 &&
                candidate.getDriversLicenses().size() > 0) {
            Iterator dln1 = person.getDriversLicenses().iterator();

            while (dln1.hasNext()) {
                DriversLicense dl = (DriversLicense) dln1.next();
                Iterator dln2 = candidate.getDriversLicenses().iterator();

                while (dln2.hasNext()) {
                    double cor = calculateDriverLicenseCor(dl, (DriversLicense) dln2.next());

                    if (cor > correlation) {
                        correlation = cor;
                    }
                }
            }
        } else
            correlation = UNKNOWN;
        return correlation;
    }

    /**
     * Calculates the correlation between two <code>DriverLicense</code> objects.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param dln1 the first <code>DriverLicense</code>
     * @param dln1 the second <code>DriverLicense</code>
     * @return correlation between the two <code>DriverLicense</code> objects
     * @todo should be a mechanism to look at the issue and expiration
     * date as well. Need to figure out logic for this.
     */
    private double calculateDriverLicenseCor(DriversLicense dln1, DriversLicense dln2) {
        double correlation = UNKNOWN;

        if ((dln1 != null) && (dln2 != null)) {
            if (dln1.getState() != null && dln2.getState() != null) {
                String dl1 = getNonNullString(dln1.getState()) + " " + getNonNullString(dln1.getNumber());
                String dl2 = getNonNullString(dln2.getState()) + " " + getNonNullString(dln2.getNumber());
                correlation = comparator.alfaCompare(dl1, dl2);
            } else if (dln1.getNumber() != null && dln2.getNumber() != null) {
                correlation = comparator.alfaCompare(getNonNullString(dln1.getNumber()),
                        getNonNullString(dln2.getNumber()));
            }
        }

        return correlation;
    }

    /**
     * Calculates the correlation between two <code>Person</code>s' genders.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param person
     * @param candidate
     * @return correlation between two genders
     */
    private double calculateGendersCor(Person person, Person candidate) {
        double maxCorrelation = 0;

        if (person != null && candidate != null &&
                person.getGenders().size() > 0 &&
                candidate.getGenders().size() > 0) {
            Iterator genders1 = person.getGenders().iterator();

            while (genders1.hasNext()) {
                Gender gender = (Gender) genders1.next();
                Iterator genders2 = candidate.getGenders().iterator();

                while (genders2.hasNext()) {
                    double cor = calculateGenderCor(gender, (Gender) genders2.next());

                    if (cor > maxCorrelation) {
                        maxCorrelation = cor;
                    }
                }
            }
        } else
            maxCorrelation = UNKNOWN;
        return maxCorrelation;
    }

    /**
     * Calculates the correlation between two <code>Gender</code> objects.
     * <p/>
     * This method is used for both search and correlation so be wary
     * when changing it.
     *
     * @param g1 the first <code>Gender</code>
     * @param g2 the second <code>Gender</code>
     * @return correlation between the two <code>Gender</code> objects
     */
    private double calculateGenderCor(Gender g1, Gender g2) {
        double correlation = UNKNOWN;

        if ((g1 != null) && (g2 != null)) {
            if (g1.getValue() != null && g2.getValue() != null) {
                correlation = calculateExactStringCor(getNonNullString(g1.getValue()), getNonNullString(g2.getValue()));
            }
        }

        return correlation;
    }

    /**
     * Queries the database for a <code>Person</code> and returns an
     * Iterator over the Person objects.  This will query the DB using
     * the SSN and the name (search keys and first/last name matching)
     * for all of the name aliases for the Person specified.
     *
     * @param person The Person to search for in the Database.
     * @param search If true, this query is for UI searching.  If false,
     *               this query is for correlation.
     * @return Iterator of Person object or null if nothing matched.  The caller
     * must call queryDone() with the iterator when they are finished
     * using it!
     */
    private Iterator queryPerson(Person person, boolean search) {
        Iterator queryResults = null;
        String nameStr;

        if (person != null) {
            // Build a QueryParamList from the relevant fields
            // This is an OR_LIST because we want to match SSNs, names, etc.
            QueryParamList params = new QueryParamList(QueryParamList.AND_LIST);

            // PID.19 SSN
            // For each alias, add a parameter to the query
            Iterator ssns = person.getSocialSecurityNumbers().iterator();
            while (ssns.hasNext()) {
                SocialSecurityNumber ssn = (SocialSecurityNumber) ssns.next();
                params.add(AttributeType.SOCIAL_SECURITY_NUMBER, ssn.getSSN());
            }

            // PID.7 Date/Time of Birth
            if (person.getDatesOfBirth().size() > 0) {
                Iterator dobs = person.getDatesOfBirth().iterator();
                while (dobs.hasNext()) {
                    DateOfBirth dob = (DateOfBirth) dobs.next();
                    params.add(AttributeType.DATE_OF_BIRTH, dob.getDOB());
                }
            }

            // PID.11 Patient Address
            if (person.getAddresses().size() > 0) {
                for (Iterator iter = person.getAddresses().iterator(); iter.hasNext(); ) {
                    Address a = (Address) iter.next();
                    if (a.getAddress1() != null && a.getAddress1().length() > 0) {
                        params.add(AttributeType.ADDRESS, a.getAddress1());
                    }

                    if (StringUtils.isNotEmpty(a.getAddress2())) {
                        params.add(AttributeType.ADDRESS_2, a.getAddress2());
                    }

                    if (StringUtils.isNotEmpty(a.getCity())) {
                        params.add(AttributeType.CITY, a.getCity());
                    }

                    if (a.getState() != null && a.getState().length() > 0) {
                        params.add(AttributeType.STATE_PROV, a.getState());
                    }

                    if (StringUtils.isNotEmpty(a.getZipCode())) {
                        params.add(AttributeType.ZIP, a.getZipCode());
                    }

                    if (StringUtils.isNotEmpty(a.getCountry())) {
                        params.add(AttributeType.COUNTRY, a.getCountry());
                    }
                }
            }

            // PID.8 Administrative Sex
            if (person.getGenders().size() > 0) {
                for (Iterator iter = person.getGenders().iterator(); iter.hasNext(); ) {
                    Gender g = (Gender) iter.next();
                    if (g.getValue() != null && g.getValue().length() > 0) {
                        params.add(AttributeType.GENDER, g.getValue());
                    }
                }
            }


            // PID.5 Patient Name and PID.6 Mother Name
            // For each alias, add a parameter to the query
            Iterator names = person.getNames().iterator();
            while (names.hasNext()) {
                PersonName name = (PersonName) names.next();
                nameStr = name.getLastName();
                if (nameStr == null) {
                    nameStr = name.getFirstName();
                }

                if ((name != null) && (nameStr != null)) {
                    SearchRange range = searchKeyGen.generateSearchRange(name.getCleanName());

                    if ((range.getStart() != null) || (range.getEnd() != null)) {
                        if (range.getStart().equals(range.getEnd()))
                            params.add(AttributeType.NAME_SEARCH_KEY, range.getStart());
                        else
                            params.add(AttributeType.NAME_SEARCH_KEY, range);
                    }
                    if (search) {
                        // Special name matching logic: If last name is specified we will
                        // match anything beginning with the specified name.  If
                        // both last and first names specified, only match names that
                        // begin with both the last name AND the first name
                        QueryParamList nameParam = new QueryParamList(QueryParamList.AND_LIST);
                        nameStr = name.getLastName();
                        if (nameStr != null && nameStr.length() > 0)
                            nameParam.add(AttributeType.LAST_NAME_ALIAS,
                                    nameStr.toUpperCase() + "%");

                        nameStr = name.getFirstName();
                        if (nameStr != null && nameStr.length() > 0)
                            nameParam.add(AttributeType.FIRST_NAME_ALIAS,
                                    nameStr.toUpperCase() + "%");

                        Iterator ids = person.getPersonIdentifiers().iterator();
                        while (ids.hasNext()) {
                            PersonIdentifier pi = (PersonIdentifier) ids.next();
                            if (pi == null) continue;

                            nameParam.add(AttributeType.PERSON_IDENTIFIER, pi.getId());

                            if (pi.getAssigningAuthority() != null && pi.getAssigningAuthority().getNameSpaceID() != null) {
                                nameParam.add(AttributeType.AA_NAMESPACE_ID, pi.getAssigningAuthority().getNameSpaceID());
                            }
                        }

                        params.add(nameParam);
                    }
                }
            }

            // PID.13 Phone Number - Home
            Iterator phones = person.getTelephoneNumbers().iterator();
            while (phones.hasNext()) {
                TelephoneNumber telephoneNumber = (TelephoneNumber) phones.next();
                if (StringUtils.isNotEmpty(telephoneNumber.getTelecomUseCode())) {
                    if (telephoneNumber.getTelecomUseCode().equalsIgnoreCase("HP")) {
                        params.add(AttributeType.TELECOM_USE_CD, "HOME");
                    } else {
                        params.add(AttributeType.TELECOM_USE_CD, "WORK");
                    }

                    if (StringUtils.isNotEmpty(telephoneNumber.getCountryCode())) {
                        params.add(AttributeType.PHONE_COUNTRY_CD, telephoneNumber.getCountryCode());
                    }

                    if (StringUtils.isNotEmpty(telephoneNumber.getAreaCode())) {
                        params.add(AttributeType.PHONE_AREA_CD, telephoneNumber.getAreaCode());
                    }

                    if (StringUtils.isNotEmpty(telephoneNumber.getPhoneNumber())) {
                        params.add(AttributeType.PHONE_NUM, telephoneNumber.getPhoneNumber());
                    }

                    if (StringUtils.isNotEmpty(telephoneNumber.getExtension())) {
                        params.add(AttributeType.PHONE_EXT, telephoneNumber.getExtension());
                    }
                }
            }

            // PID.18 Patient Account Number
            Iterator accountNumbers = person.getAccountNumbers().iterator();
            while (accountNumbers.hasNext()) {
                PersonIdentifier accountNumber = (PersonIdentifier) accountNumbers.next();
                if (accountNumber != null) {
                    // @PID.18.1
                    if (StringUtils.isNotEmpty(accountNumber.getId())) {
                        params.add(AttributeType.AN_IDENTIFIER, accountNumber.getId());
                    }

                    // @PID.18.4
                    DomainIdentifier assigningAuthority = accountNumber.getAssigningAuthority();
                    if (assigningAuthority != null) {
                        if (StringUtils.isNotEmpty(assigningAuthority.getNameSpaceID())) {
                            params.add(AttributeType.AN_AA_NAMESPACE_ID, assigningAuthority.getNameSpaceID());
                        }
                    }else
                    {
                        // @PID.18.6
                        DomainIdentifier assigningFacility = accountNumber.getAssigningFacility();
                        if (assigningFacility != null) {
                            if (StringUtils.isNotEmpty(assigningFacility.getNameSpaceID())) {
                                params.add(AttributeType.AN_AF_UNIV_ID, assigningFacility.getNameSpaceID());
                            }
                        }
                    }

                    // @PID.18.5
                    if (StringUtils.isNotEmpty(accountNumber.getIdentifierTypeCode())) {
                        params.add(AttributeType.AN_IDENTIFIER_CODE, accountNumber.getIdentifierTypeCode());
                    }


                }
            }

            // PID.20 Driver License
            Iterator driversLicenses = person.getDriversLicenses().iterator();
            while (driversLicenses.hasNext()) {
                // @PID.20.1
                DriversLicense driverLicense = (DriversLicense) driversLicenses.next();
                ;
                if (StringUtils.isNotEmpty(driverLicense.getNumber())) {
                    params.add(AttributeType.DRV_LICENSE_NUM, driverLicense.getNumber());
                }

                // @PID.20.2
                if (StringUtils.isNotEmpty(driverLicense.getState())) {
                    params.add(AttributeType.DRV_LICENSE_ISSUING_STATE, driverLicense.getState());
                }

                // @PID.20.2
                if (driverLicense.getIssueDate() != null) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String issueDate = df.format(driverLicense.getIssueDate());
                    params.add(AttributeType.DRV_LICENSE_ISSUE_DATE, issueDate);
                }

            }

            // PID.3
            if (person.getNames().size() == 0 && person.getPersonIdentifiers().size() > 0) {
                QueryParamList nameParam = new QueryParamList(QueryParamList.AND_LIST);
                Iterator ids = person.getPersonIdentifiers().iterator();
                while (ids.hasNext()) {
                    PersonIdentifier pi = (PersonIdentifier) ids.next();
                    if (pi == null) continue;

                    if (pi.getId() != null) {
                        nameParam.add(AttributeType.PERSON_IDENTIFIER_ALIAS, pi.getId());
                    }
                    if (pi.getAssigningAuthority().getNameSpaceID() != null) {
                        nameParam.add(AttributeType.AA_NAMESPACE_ID, pi.getAssigningAuthority().getNameSpaceID());
                    }
                    if (pi.getAssigningAuthority().getUniversalID() != null) {
                        nameParam.add(AttributeType.AA_UNIV_ID, pi.getAssigningAuthority().getUniversalID());
                    }
                    if (pi.getAssigningAuthority().getUniversalIDType() != null) {
                        nameParam.add(AttributeType.AA_UNIV_ID_TYPE_CD, pi.getAssigningAuthority().getUniversalIDType());
                    }
                    params.add(nameParam);
                }
            }


            if (IcsTrace.isEnabled())
                trace.add("Querying for: " + params.toString());

            try {
                DatabaseServices dbServices = DatabaseServicesFactory.getInstance();
                queryResults = dbServices.queryIterator(params, false);
            } catch (DatabaseException e) {
                log.error(e, e);
            }
        }

        return queryResults;
    }

    public List query(Person person) {
        List queryResults = new ArrayList();
        String nameStr;

        if (person == null) {
            return queryResults;
        }

        // Build a QueryParamList from the relevant fields
        // This is an OR_LIST because we want to match SSNs, names, etc.
        QueryParamList params = new QueryParamList(QueryParamList.OR_LIST);

        // For each alias, add a parameter to the query
        Iterator names = person.getNames().iterator();
        while (names.hasNext()) {
            PersonName name = (PersonName) names.next();
            QueryParamList nameParam = new QueryParamList(
                    QueryParamList.AND_LIST);
            nameStr = name.getLastName();

            if ((name != null) && (nameStr != null)) {
                nameParam.add(AttributeType.LAST_NAME_ALIAS, nameStr
                        .toUpperCase()
                        + "%");
            }

            nameStr = name.getFirstName();
            if (nameStr != null && nameStr.length() > 0) {
                nameParam.add(AttributeType.FIRST_NAME_ALIAS, nameStr
                        .toUpperCase()
                        + "%");
            }

            if (nameParam.size() > 0) {
                params.add(nameParam);
            }
        }

        if (IcsTrace.isEnabled())
            trace.add("Querying for: " + params.toString());

        try {
            DatabaseServices dbServices = DatabaseServicesFactory.getInstance();
            queryResults = dbServices.query(params);
        } catch (DatabaseException e) {
            log.error(e, e);
        }

        return queryResults;
    }

    /**
     * Releases the iterator return from the query() method.
     *
     * @param iter The iterator to release.
     */
    private void queryDone(Iterator iter) {
        try {
            DatabaseServices dbServices = DatabaseServicesFactory.getInstance();
            dbServices.releaseIterator(iter);
        } catch (DatabaseException e) {
            log.error(e, e);
        }
    }

    /**
     * returns a Non Null String
     */
    private String getNonNullString(String s) {
        return (s == null) ? "" : s;
    }

    // This method is invoked when two patients are not found to match based on the identifier from any domain so
    // we need to assess whether there is a match based on the demographics.
    public boolean match(Person person, Person candidate) {
        double SSN = 0.0, FN = 0.0, LN = 0.0, MB = 0.0, DB = 0.0, YB = 0.0, ZIP = 0.0;

        if (person == null || candidate == null)
            return false;

        // SSN + FN + LN
        if (person.getSocialSecurityNumbers().size() > 0 && candidate.getSocialSecurityNumbers().size() > 0) {
            Iterator ssns1 = person.getSocialSecurityNumbers().iterator();
            if (ssns1.hasNext()) {
                SocialSecurityNumber ssn1 = (SocialSecurityNumber) ssns1.next();

                Iterator ssns2 = candidate.getSocialSecurityNumbers().iterator();
                if (ssns2.hasNext()) {
                    SSN = comparator.compare(ssn1.getSSN(), ((SocialSecurityNumber) ssns2.next()).getSSN(), "");  // last string is service
                }
            }
        }

        if (person.getNames().size() > 0 && candidate.getNames().size() > 0) {
            Iterator names1 = person.getNames().iterator();

            if (names1.hasNext()) {
                PersonName name1 = (PersonName) names1.next();

                Iterator names2 = candidate.getNames().iterator();
                if (names2.hasNext()) {
                    PersonName name2 = (PersonName) names2.next();
                    FN = comparator.compare(name1.getFirstName(), name2.getFirstName(), ""); // last string is service
                    LN = comparator.compare(name1.getLastName(), name2.getLastName(), ""); // last string is service
                }
            }
        }

        if (SSN == 1.0 && FN == 1.0 && LN == 1.0)
            return true;

        // SSN + YB + MB + DB
        if (person.getDatesOfBirth().size() > 0 && candidate.getDatesOfBirth().size() > 0) {
            Iterator dobs1 = person.getDatesOfBirth().iterator();

            if (dobs1.hasNext()) {
                DateOfBirth dob1 = (DateOfBirth) dobs1.next();
                Iterator dobs2 = candidate.getDatesOfBirth().iterator();

                if (dobs2.hasNext()) {
                    DateOfBirth dob2 = (DateOfBirth) dobs2.next();

                    GregorianCalendar c1 = new GregorianCalendar();
                    GregorianCalendar c2 = new GregorianCalendar();

                    c1.setTime(dob1.getDOB());
                    c2.setTime(dob2.getDOB());

                    if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        YB = 1.0;
                    if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH))
                        MB = 1.0;
                    if (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH))
                        DB = 1.0;
                }
            }
        }
        if (SSN == 1.0 && YB == 1.0 && MB == 1.0 && DB == 1.0)
            return true;

        // SSN + FN + YB + ZIP
        if (person.getAddresses().size() > 0 && candidate.getAddresses().size() > 0) {
            Iterator addresses1 = person.getAddresses().iterator();

            if (addresses1.hasNext()) {
                Address address1 = (Address) addresses1.next();
                Iterator addresses2 = candidate.getAddresses().iterator();

                if (addresses2.hasNext()) {
                    Address address2 = (Address) addresses2.next();
                    ZIP = comparator.compare(address1.getZipCode(), address2.getZipCode(), "");
                }
            }
        }
        if (SSN == 1.0 && FN == 1.0 && YB == 1.0 && ZIP == 1.0)
            return true;

        // FN + LN + YB + MB + DB
        if (FN == 1.0 && LN == 1.0 && YB == 1.0 && MB == 1.0 && DB == 1.0)
            return true;

        return false;
    }

}
