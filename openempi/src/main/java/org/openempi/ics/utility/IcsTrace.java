package org.openempi.ics.utility;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openempi.data.Address;
import org.openempi.data.DateOfBirth;
import org.openempi.data.DocumentHeader;
import org.openempi.data.Gender;
import org.openempi.data.Person;
import org.openempi.data.PersonIdentifier;
import org.openempi.data.PersonName;
import org.openempi.data.SocialSecurityNumber;
import org.openempi.data.TelephoneNumber;

public class IcsTrace
{
  public static final int UPDATE_PERSON = 1;
  public static final int ADD_PERSON = 2;
  public static final int MERGE_PERSON = 3;
  public static final int SPLIT_PERSON = 4;
  
  private static boolean enabled = false;
  private int type;
  private StringBuffer log = new StringBuffer();
  
  public IcsTrace(int type, Person p)
  {
    enabled = ICSProperties.getBoolean("TRACE", false);
    if (enabled) {
      this.type = type;
      
      switch (type) {
      case UPDATE_PERSON:
        log.append("Update request for person:\n");
        break;
      case ADD_PERSON:
        log.append("Add request for person:\n");
        break;
      }      
      if (p != null)
        add(p);
    }
  }

  public static boolean isEnabled()
  {
    return enabled;
  }
  
  public static void enable()
  {
    enabled = true;
  }
  
  public static void disable()
  {
    enabled = false;
  }
  
  public void add(String msg)
  {
    if (enabled)
      log.append(msg).append("\n");
  }
  
  public void add(Person p)
  {
    if (enabled)
      logPerson(p, false);
  }
  
  public void add(Person p, boolean brief)
  {
    if (enabled) 
      logPerson(p, brief);
  }
  
  public void save()
  {
    if (enabled) {
      Logger trace = Logger.getLogger("ICS_TRACE");
      trace.info(log.toString());
    }
  }
  

  private void logPerson(Person p, boolean brief)
  {
    String id = p.getOid();

    if (id != null)
      log.append("  PERSON_ID: ").append(id).append("\n");

    if (! brief) {
      Iterator iter = p.getDocumentHeaders().iterator();
      
      while(iter.hasNext()) {
        DocumentHeader d = (DocumentHeader) iter.next();
        Map map = p.getAttributesByDocumentHeader(d);
        List list;
        Iterator attrIter;
        
        attrIter = ((List) map.get("ssns")).iterator();
        while(attrIter.hasNext()) {
          SocialSecurityNumber obj = (SocialSecurityNumber) attrIter.next();
          log.append("    SSN: ").append(obj.getSSN()).append("\n");
        }
        
        attrIter = ((List) map.get("dobs")).iterator();
        while(attrIter.hasNext()) {
          DateOfBirth obj = (DateOfBirth) attrIter.next();
          log.append("    DOB: ").append(obj.getDOB()).append("\n");
        }
        
        attrIter = ((List) map.get("names")).iterator();
        while(attrIter.hasNext()) {
          PersonName obj = (PersonName) attrIter.next();
          log.append("    Name: ").append(obj.getName()).append("\n");
        }
        
        attrIter = ((List) map.get("genders")).iterator();
        while(attrIter.hasNext()) {
          Gender obj = (Gender) attrIter.next();
          log.append("    Gender: ").append(obj.getValue()).append("\n");
        }
        
        attrIter = ((List) map.get("addresses")).iterator();
        while(attrIter.hasNext()) {
          Address obj = (Address) attrIter.next();
          log.append("    Addr1: ").append(obj.getAddress1()).append("\n");
          log.append("    Addr2: ").append(obj.getAddress2()).append("\n");
          log.append("    City: ").append(obj.getCity()).append("\n");
          log.append("    State: ").append(obj.getState()).append("\n");
          log.append("    Zip: ").append(obj.getZipCode()).append("\n");
        }
        
        attrIter = ((List) map.get("telephoneNumbers")).iterator();
        while(attrIter.hasNext()) {
          TelephoneNumber obj = (TelephoneNumber) attrIter.next();
          log.append("    Area Code: ").append(obj.getAreaCode()).append("\n");
          log.append("    Phone: ").append(obj.getPhoneNumber()).append("\n");
        }
        
        attrIter = ((List) map.get("personIdentifiers")).iterator();
        while(attrIter.hasNext()) {
          PersonIdentifier obj = (PersonIdentifier) attrIter.next();
          log.append("    PID: ").append(obj.getId()).append("@").append(obj.getAssigningAuthority().getNameSpaceID()).append(".").append(obj.getAssigningFacility().getNameSpaceID()).append("\n");
        }
        log.append("\n");
      }
    }
  }
}
