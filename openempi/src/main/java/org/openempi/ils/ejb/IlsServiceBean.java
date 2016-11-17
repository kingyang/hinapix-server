package org.openempi.ils.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.openempi.ils.IlsException;
import org.openempi.ils.IlsMap;
import org.openempi.ils.IlsServiceImpl;
import org.openempi.ils.NodeInfo;
import org.openempi.ils.NodeInfoList;



/**
 * @ejb.bean
 * 		name="IlsService"
 * 		display-name="IlsService"
 * 		jndi-name="ejb/IlsService"
 * 		type="Stateless"
 * 		view-type="remote"
 * 
 */
public class IlsServiceBean implements SessionBean
{
  private SessionContext ctx;

  public void setSessionContext(SessionContext context)
    throws RemoteException, EJBException
  {
    ctx = context;
  }

  public void ejbActivate()
    throws RemoteException, EJBException
  {
  }

  public void ejbPassivate()
    throws RemoteException, EJBException
  {
  }

  public void ejbRemove()
    throws RemoteException, EJBException
  {
  }
  
  public void ejbCreate()
    throws CreateException, EJBException
  {

  }

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	  
  public HashMap[] queryHashMap(HashMap[] ilsReqs)
    throws IlsException
  {
    IlsServiceImpl ils = new IlsServiceImpl();
    return ils.sendRequestHashMap(ilsReqs);
  }

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
  public IlsMap[] query(IlsMap[] ilsReqs)
    throws IlsException
  {
    IlsServiceImpl ils = new IlsServiceImpl();
    return ils.sendRequest(ilsReqs);
  }

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
  public HashMap[] queryHashMap(HashMap ilsReq)
    throws IlsException
  {
    HashMap[] ilsReqs = { ilsReq };
    IlsServiceImpl ils = new IlsServiceImpl();
    return ils.sendRequestHashMap(ilsReqs);
  }

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
  public IlsMap[] query(IlsMap ilsReq)
    throws IlsException
  {
    IlsMap[] ilsReqs = { ilsReq };
    IlsServiceImpl ils = new IlsServiceImpl();
    return ils.sendRequest(ilsReqs);
  }

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	  
  public NodeInfo[] getNodeInfo()
    throws IlsException
  {
    List nodeInfoList = NodeInfoList.getList();
    return (NodeInfo[]) nodeInfoList.toArray(new NodeInfo[nodeInfoList.size()]);
  }

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */	
  public NodeInfo[] getNodeInfo(String domain)
    throws IlsException
  {
    ArrayList ret = new ArrayList();
    List nodeInfoList = NodeInfoList.getList();
    Iterator iter = nodeInfoList.iterator();
    while(iter.hasNext()){
      NodeInfo dm = (NodeInfo) iter.next();
      if (dm.getDomainId().equals(domain))
        ret.add(dm);
    }
    if (ret.size() == 0)
      throw new IlsException("Domain " + domain + " is not a configured ILS domain");
    return (NodeInfo[]) ret.toArray(new NodeInfo[ret.size()]);
  }

	/**
	 * @ejb.interface-method
	 * 		view-type="remote"
	 */
  public String[] getDomains()
    throws IlsException
  {
    HashSet ret = new HashSet();
    List nodeInfoList = NodeInfoList.getList();
    Iterator iter = nodeInfoList.iterator();
    while(iter.hasNext()){
      NodeInfo dm = (NodeInfo) iter.next();
      ret.add(dm.getDomainId());
    }
    return (String[]) ret.toArray(new String[ret.size()]);
  }

}
