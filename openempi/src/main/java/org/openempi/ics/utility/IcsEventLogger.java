/*
 * Title:        IcsEventLogger
 * Description:  Will log events from listeners on ICS classes
 * Copyright:    (c) 2002-2003
 * Company:      CareScience, Inc.
 *               3600 Market Street
 *               6th Floor
 *               Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.utility;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openempi.data.Person;
import org.openempi.ics.db.DatabaseServices;
import org.openempi.ics.db.DatabaseServicesFactory;
import org.openempi.ics.db.DatabaseServicesListener;

/**
 * Responsible for listening to <code>CorrelationSystem</code> and
 * <code>DatabaseServices</code> events, and logging these to a designated target.
 *
 * @author       J. Mangione
 * @author       M. Abundo
 * @version      1.5, 20030214
 */
public class IcsEventLogger implements DatabaseServicesListener
{
  private static IcsEventLogger instance;

  private Logger log = Logger.getLogger("ICS");
  
  static
  {
      instance = new IcsEventLogger();
  }

  /**
   * Obtain single instance of CorrelationSystem and DatabaseServices and
   * register as listeners to them.
   */
  private IcsEventLogger()
  {
      DatabaseServices dbSys = DatabaseServicesFactory.getInstance();

      dbSys.addListener(this);
  }

  /**
   * Returns the single instance of this class.
   */
  public static IcsEventLogger getInstance()
  {
      return instance;
  }

  public void duplicateFoundDuringAdd(Person duplicate, Person person)
  {
      Calendar cal = Calendar.getInstance(); // current date/time

      persistEvent(IcsEvent.EVENT_ONADD_DUPLICATE_FOUND, cal.getTime(),
                   person, duplicate);
  }

  public void similarFoundDuringAdd(Person similar, Person person)
  {
      Calendar cal = Calendar.getInstance(); // current date/time

      persistEvent(IcsEvent.EVENT_ONADD_SIMILAR_FOUND, cal.getTime(),
                   person, similar);
  }

  public void expiredPersonFoundDuringAdd(Person person)
  {
      Calendar cal = Calendar.getInstance(); // current date/time

      persistEvent(IcsEvent.EVENT_ONADD_EXPIRED_FOUND, cal.getTime(),
                   person, null);
  }

  public void personAdded(Person person)
  {
      Profile.begin("IcsEventLogger.personAdded");
      Calendar cal = Calendar.getInstance(); // current date/time
      persistEvent(IcsEvent.EVENT_PERSON_ADDED, cal.getTime(), person, null);
      Profile.end("IcsEventLogger.personAdded");
  }

  public void personUpdated(Person person)
  {
      Profile.begin("IcsEventLogger.personUpdated");
      Calendar cal = Calendar.getInstance(); // current date/time
      persistEvent(IcsEvent.EVENT_PERSON_UPDATED, cal.getTime(), person, null);
      Profile.end("IcsEventLogger.personUpdated");
  }

  public void personsMerged(Person basePerson, Person mergePerson)
  {
      Profile.begin("IcsEventLogger.personMerged");
      Calendar cal = Calendar.getInstance(); // current date/time
      persistEvent(IcsEvent.EVENT_PERSONS_MERGED, cal.getTime(), basePerson, mergePerson);
      Profile.end("IcsEventLogger.personMerged");
  }

  public void personSplit(Person originalPerson, Person newPerson)
  {
      Profile.begin("IcsEventLogger.personSplit");
      Calendar cal = Calendar.getInstance(); // current date/time
      persistEvent(IcsEvent.EVENT_PERSON_SPLIT, cal.getTime(), originalPerson, newPerson);
      Profile.end("IcsEventLogger.personSplit");
  }

  public void patientConsentUpdated(Person person)
  {
      Profile.begin("IcsEventLogger.patientConsentUpdated");
      Calendar cal = Calendar.getInstance(); // current date/time
      persistEvent(IcsEvent.EVENT_CONSENT_UPDATED, cal.getTime(), person, null);
      Profile.end("IcsEventLogger.patientConsentUpdated");
  }

  public void personRemoved(Person person)
  {
      Profile.begin("IcsEventLogger.personRemoved");
      Calendar cal = Calendar.getInstance(); // current date/time
      persistEvent(IcsEvent.EVENT_PERSON_REMOVED, cal.getTime(), person, null);
      Profile.end("IcsEventLogger.personRemoved");
  }

  /**
   * Wrapper method to persist specific ICS events.  This spins a new thread for
   * each event to be persisted so as not to block the main thread while the DB
   * activity is going on.
   */
  private void persistEvent(String eventType,
                            Date eventDate,
                            Person currPerson,
                            Person altPerson )
  {
      IcsEvent icsEvent = new IcsEvent(eventType, eventDate, currPerson, altPerson);
      new Thread(new IcsEventLoggerRunner(icsEvent)).start();
  }

  /**
   * Private class used by persistEvent to persist new ICSEvents in their own
   * thread.
   */
  private class IcsEventLoggerRunner
       implements Runnable
  {
       private IcsEvent event;

       public IcsEventLoggerRunner(IcsEvent event)
       {
           this.event = event;
       }

       public void run()
       {
           if ( !event.persist() )
           {
               log.warn("run(): Cannot persist " + event);
           }
           else
           {
               log.debug("run(): " + event);
           }
       }
   }
}
