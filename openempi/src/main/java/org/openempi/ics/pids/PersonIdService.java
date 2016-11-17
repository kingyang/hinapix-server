/*
 * Generated by XDoclet - Do not edit!
 */
package org.openempi.ics.pids;

/**
 * Remote interface for PersonIdService.
 */
public interface PersonIdService
   extends javax.ejb.EJBObject
{

   public org.openempi.data.Person addPerson(org.openempi.data.Person person)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public boolean removePerson(org.openempi.data.Person person)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public org.openempi.data.Person updatePerson(org.openempi.data.Person person)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public org.openempi.data.Person mergePersons(org.openempi.data.Person[] persons)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public int splitPerson(org.openempi.data.Person person, org.openempi.data.DocumentHeader[] docHeaders)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public int submitReview(org.openempi.data.PersonReview personReview)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public void deleteReview(int id)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public java.util.List getReviews(String domain)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public java.util.List getAllReviews()
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public java.util.List getSystemReviews()
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public boolean hasReviews(String domain)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public org.openempi.data.Person addPersonWithCDECascade(org.openempi.data.Person newperson, String oldPID)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

   public org.openempi.data.Person mergePersonsWithCDECascade(org.openempi.data.Person newperson, String oldPID, String oldEID)
      throws org.openempi.ics.pids.PersonIdServiceException, java.rmi.RemoteException;

}
