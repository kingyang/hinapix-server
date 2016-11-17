package org.openempi.ils;


/**
   Interface definition for the various Connector classes
 */
public interface Connector
  extends Runnable
{
  /**
     Sets up the Connector by giving the the IlsTask and IlsMap
     for this Connector to handle.

     @param aTask the task object
     @param aRequest the request to be processed
   */
  public void setup(IlsTask aTask, IlsMap aRequest);
  
}
