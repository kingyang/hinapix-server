/*
 * Generated by XDoclet - Do not edit!
 */
package org.openempi.ils.interfaces;

/**
 * Remote interface for IlsService.
 */
public interface IlsService
   extends javax.ejb.EJBObject
{

   public java.util.HashMap[] queryHashMap(java.util.HashMap[] ilsReqs)
      throws org.openempi.ils.IlsException, java.rmi.RemoteException;

   public org.openempi.ils.IlsMap[] query(org.openempi.ils.IlsMap[] ilsReqs)
      throws org.openempi.ils.IlsException, java.rmi.RemoteException;

   public java.util.HashMap[] queryHashMap(java.util.HashMap ilsReq)
      throws org.openempi.ils.IlsException, java.rmi.RemoteException;

   public org.openempi.ils.IlsMap[] query(org.openempi.ils.IlsMap ilsReq)
      throws org.openempi.ils.IlsException, java.rmi.RemoteException;

   public org.openempi.ils.NodeInfo[] getNodeInfo()
      throws org.openempi.ils.IlsException, java.rmi.RemoteException;

   public org.openempi.ils.NodeInfo[] getNodeInfo(String domain)
      throws org.openempi.ils.IlsException, java.rmi.RemoteException;

   public String[] getDomains()
      throws org.openempi.ils.IlsException, java.rmi.RemoteException;

}
