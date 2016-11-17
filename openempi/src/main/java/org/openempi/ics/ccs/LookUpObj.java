package org.openempi.ics.ccs;

import java.util.ArrayList;
import java.util.Iterator;

import org.openempi.data.Person;
import org.openempi.ics.db.DatabaseException;
import org.openempi.ics.db.DatabaseServices;
import org.openempi.ics.db.DatabaseServicesFactory;

public class LookUpObj
{
  protected Iterator iter;
  protected ArrayList attrsList;
  protected double confidence;
  protected Person person;
  
  protected LookUpObj(Person person, Iterator iter,
                      double confidence, ArrayList attrsList)
  {
    this.person = person;
    this.iter = iter;
    this.attrsList = attrsList;
    this.confidence = confidence;
  }

  public void finalize()
  {
    if (iter != null) {
      try {
        DatabaseServices dbServices = DatabaseServicesFactory.getInstance();
        dbServices.releaseIterator(iter);
      } catch (DatabaseException e) {
        e.printStackTrace();
      }
    }
  }
  
}
