package org.openempi.ils.adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.openempi.ils.utility.pool.Pool;
import org.openempi.ils.utility.pool.Poolable;

public class ConnectionPool
{
  private static Hashtable map = new Hashtable();
  private static Hashtable pools = new Hashtable();
  
  public static Connection getConnection(String name,
                                         String driver, String url,
                                         String user, String pwd,
                                         int min, int max, long shrink)
  {
    ConnectionPoolObject obj;
    
    String key = driver + url + user + pwd;

    Pool pool = (Pool) pools.get(key);
    if (pool == null) {
      PoolParam param = new PoolParam();
      param.key = key;
      param.driver = driver;
      param.url = url;
      param.user = user;
      param.pwd = pwd;
      pool = new Pool(ConnectionPoolObject.class, param, min, max);
      pool.setShrinkAfter(shrink);
      pool.setBlockOnMax(true);
      pool.setName(name);
      pools.put(key, pool);
    }
    obj = (ConnectionPoolObject) pool.get();
    map.put(obj.conn, obj);
    return obj.conn;
  }
  
  public static void releaseConnection(Connection conn)
  {
    releaseConnection(conn, false);
  }
  
  public static void releaseConnection(Connection conn, boolean delete)
  {
    ConnectionPoolObject obj = (ConnectionPoolObject) map.remove(conn);
    Pool pool = (Pool) pools.get(obj.key);
    if (pool != null) 
      pool.release(obj, delete);
  }

  private static class PoolParam
  {
    public String key;
    public String driver;
    public String url;
    public String user;
    public String pwd;
  }
  
  public static class ConnectionPoolObject 
    implements Poolable
  {
    public Connection conn = null;
    public String key = null;
    
    public void poolInit(Object param)
    {
      try {
        PoolParam pparam = (PoolParam) param;
        key = pparam.key;
        Class.forName(pparam.driver);
        conn = DriverManager.getConnection(pparam.url, pparam.user, pparam.pwd);
      } catch (Exception e) {
        Logger log = Logger.getLogger("ILS");
        log.error(e, e);
      }
    }
    
    public void poolDelete()
    {
      try {
        conn.close();
      } catch (SQLException e) {
        Logger log = Logger.getLogger("ILS");
        log.error(e, e);
      }
    }
  }
}
