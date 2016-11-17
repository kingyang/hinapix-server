package org.openempi.data;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
  PersonReview encapsulates the object for reviewing correlation errors.
  Each instance of a PersonReview represents a single review request from
  either a user of the system.  Each such request can have multiple
  Persons associated with it for review, plus a description and information
  about who the request is from.

  PersonReview objects are used by the PersonIdService methods
  submitReview(), deleteReview() and getReviews() to manage the reviews.

  The PersonReview only stores the ids of the Person objects used.  When
  you have retrieved PersonReviews from the PersonIdService you will have to
  use the IdentityService method getPersonByOid() to retrieve to actual
  person object from the oid.
*/
public class PersonReview
  implements java.io.Serializable
{
  private int id = 0;
  private String userId = "ICS"; //default to ICS for system inited reviews
  private String domainId;
  private String descr;
  private int[] personIds = null;
  private Date createDt = null;

  /**
     Constructor that takes all paramters to build a PersonReview

     @param persons Array of Persons to review
     @param descr A text description of what to review and why
     @param userId The id of the user requesting the review
     @param domainId The domain (namespace) of the user creating the request
                     or null if not known.
  */
  public PersonReview(Person[] persons, String descr,
                      String userId, String domainId)
  {
    setUserId(userId);
    setDomainId(domainId);
    setDescr(descr);
    setPersons(persons);
  }

  /**
     Constructor that takes all paramters to build a PersonReview

     @param persons Array of int, the person ids to add
     @param descr A text description of what to review and why
     @param userId The id of the user requesting the review
     @param domainId The domain (namespace) of the user creating the request
                     or null if not known.
  */
  public PersonReview(int[] persons, String descr,
                      String userId, String domainId)
  {
    setUserId(userId);
    setDomainId(domainId);
    setDescr(descr);
    setPersons(persons);
  }

  /**
     Default constructor.  Caller should use setter methods to
     fill in fields in PersonReview
  */
  public PersonReview()
  {
    personIds = new int[0];
  }

  /**
     @return The unique Id for this PersonReview
  */
  public int getId()
  {
    return id;
  }

  /**
     Sets the unique Id for this PersonReview.  Should only be used
     by the DB code to generate the unique ids

     @param id The unique id
  */
  public void setId(int id)
  {
    this.id = id;
  }

  /**
     @return The Date this PersonReview was added to the database
  */
  public Date getCreateDate()
  {
    return createDt;
  }

  /*
    Sets the creation date of this review

    @param createDr The creation date
  */
  public void setCreateDate(Date createDt)
  {
    this.createDt = createDt;
  }
  
  /**
     @return Array of person ids to whom this issue relates
  */
  public int[] getPersons()
  {
    return personIds;
  }

  /**
     @return The description of the correlation issue
  */
  public String getDescr()
  {
    return descr;
  }

  /**
     @return The user who initiatated the review, or null is system
  */
  public String getUserId()
  {
    return userId;
  }

  /**
     Sets the Array of persons to be checked for this review

     @param persons Array of persons to be checked
  */
  public void setPersons(Person[] persons)
  {
    personIds = new int[persons.length];

    for(int i=0; i<persons.length; i++)
      personIds[i] = persons[i].getOidInt();
  }

  /**
     Sets the Array of persons id to be checked for this review

     @param persons Array of persons id to be checked
  */
  public void setPersons(int[] persons)
  {
    personIds = persons;
  }

  /**
     Appends a new person to the list of persons for this PersonReview

     @param personId New person id to add
  */
  public void addPerson(int personId)
  {
    int[] l = new int[personIds.length + 1];
    int i;
    
    for(i=0; i<personIds.length; i++)
      l[i] = personIds[i];
    l[i] = personId;
    personIds = l;
  }

  /**
     Appends a new person to the list of persons for this PersonReview

     @param person New person to add
  */
  public void addPerson(Person person)
  {
    addPerson(person.getOidInt());
  }

  /**
     Appends the Array of Person ids to the persons for review

     @param persons Array of person ids to add
  */
  public void addPersons(int[] persons)
  {
    int[] l = new int[personIds.length + persons.length];
    int i;
    
    for(i=0; i<personIds.length; i++)
      l[i] = personIds[i];
    for(int j=0; i<persons.length; j++)
      l[j+i] = persons[j];
    personIds = l;
  }

  /**
     Appends the Collection of Persons to the persons for review

     @param persons Array of person ids to add
  */
  public void addPersons(Collection persons)
  {
    int[] l = new int[personIds.length + persons.size()];
    int i;
    
    for(i=0; i<personIds.length; i++)
      l[i] = personIds[i];

    Iterator iter = persons.iterator();
    while(iter.hasNext()) {
      Person p = (Person) iter.next();
      l[i++] = p.getOidInt();
    }
    personIds = l;
  }

  /**
     @param descr The description of the reason for this review
  */
  public void setDescr(String descr)
  {
    this.descr = descr;
  }

  /**
     @param userId The user id of user initiating this request.
  */
  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  /**
     @param domainId The domain of the user making the request
  */
  public void setDomainId(String domainId)
  {
    this.domainId = domainId;
  }

  /**
     @return The domain of the user making the request
  */
  public String getDomainId()
  {
    return domainId;
  }
}
