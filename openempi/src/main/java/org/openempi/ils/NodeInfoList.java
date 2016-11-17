package org.openempi.ils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openempi.ils.admin.AdminConnector;
import org.openempi.ils.admin.AdminMap;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.IlsSystemProps;

/**
   Class for managing the list of know Ils Nodes and keeping that list
   up-to-date.
*/
public class NodeInfoList
{
  /**** Everything from here down supports the NodeInfo status stuff ****/
  
  /**
     The current list of known ILS NodeInfos which maintains the
     current state of each node
  */
  private static List nodeInfoList = null;

  /**
     Keeps track of the threads spun to check remote node status.
     The Map consists of host/thread pairs.  This prevents us from
     spinning a new thread for host status when one is currently running.
  */
  private static HashMap nodeThreads = new HashMap();

  private static NodeInfoList instance = new NodeInfoList();

  private NodeInfoList()
  {
  }

  /**
     @return The Node info list.  Information about all known nodes
  */
  public static List getList()
  {
    return nodeInfoList;
  }
  
  /**
     Performs a full refresh of the nodeInfoList.  The nodeInfoList is
     constructed initially from node information from the IlsProps.xml
     and subsequently updated with status information from the nodes.
     A new thread is started for each distinct host on which a node exists
     which uses the AdminConnector to get remote status.

     @param rebuild If true, this will for a rebuild of the nodeInfoList.
                    This may be called if the IlsProps.xml has changed.
  */
  public static void refreshNodes(boolean rebuild)
  {
    Iterator iter = null;
    
    synchronized (NodeInfoList.class) {
      if (nodeInfoList == null || rebuild) {
        nodeInfoList = new ArrayList();
        Set domains = IlsProps.getSystemPropsDomains();
        iter = domains.iterator();
        while(iter.hasNext()) {
          String domain = (String) iter.next();
          List props = IlsProps.getSystemProps(domain);
          Iterator propsIter = props.iterator();
          while(propsIter.hasNext()){
            IlsSystemProps prop = (IlsSystemProps) propsIter.next();
            Set types = prop.getTypes();
            NodeInfo node = new NodeInfo();
            node.setDomainId(domain);
            node.setTypeCodes(types);
            node.setSystemName(prop.getSystemName());
            node.setDescription(prop.getDescription());
            
            String nodeStr = prop.getInterfaceHost();
            if (nodeStr != null) 
              node.setNode(nodeStr);
            else {
              node.setNode(IlsProps.getHost());
              node.setIsRunning(true);
            }
            
            nodeInfoList.add(node);
          }
        }
      }
      
      iter = nodeInfoList.iterator();
      while(iter.hasNext()) {
        boolean startThread = false;
        NodeInfo node = (NodeInfo) iter.next();
        String host = node.getNode();
        if (host.equals(IlsProps.getHost()))
          continue;
        if (nodeThreads.get(host) == null) {
          Date d = node.getLastUpdate();
          if (d == null ||
              System.currentTimeMillis() - d.getTime() > IlsProps.getNodeStatusRefresh()) 
            startThread = true;
          else
            startThread = false;
          
          if (startThread) {
            RefreshNodeRunner runner = new RefreshNodeRunner(host);
            Thread t = new Thread(runner);
            t.setContextClassLoader(Thread.currentThread().getContextClassLoader());
            nodeThreads.put(host, t);
            t.start();
          }
        }
      }
    }
  }

  /**
     A new RefreshNodeRunning is started in a thread for each host to
     check status of.  
  */
  private static class RefreshNodeRunner
    implements Runnable
  {
    String host;
    
    public RefreshNodeRunner(String host)
    {
      this.host = host;
    }

    public void run()
    {
      Logger log = Logger.getLogger("ILS");
      AdminConnector ac = new AdminConnector(host);
      AdminMap m = ac.callForStatus();

      Iterator iter = nodeInfoList.iterator();
      while(iter.hasNext()) {
        NodeInfo node = (NodeInfo) iter.next();
        if (node.getNode().equals(host)) {
          if (node.getIsRunning() && !m.getIsRunning())
            log.info("Domain " + node.getDomainId() + " is down!");
          else if (! node.getIsRunning() && m.getIsRunning())
            log.info("Domain " + node.getDomainId() + " is up!");
          node.setIsRunning(m.getIsRunning());
          node.setVersion(m.getVersion());
          node.setLastUpdate(new Date(System.currentTimeMillis()));
        }
      }
      nodeThreads.remove(host);
    }
  }

}
