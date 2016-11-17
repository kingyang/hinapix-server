/*
 * Title:       CdrAdapterSql
 * Description: Implements the ExternalLocatorAdapter interface.
 * Copyright:   (c) 2001-2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.openempi.ils.ExternalLocatorAdapter;
import org.openempi.ils.IlsException;
import org.openempi.ils.IlsMap;
import org.openempi.ils.utility.IlsProps;
import org.openempi.ils.utility.IlsSystemProps;
import org.openempi.ils.utility.Profile;


/**
   Implements the <code>ExternalLocatorAdapter</code> interface on an external system
   for integration with the locator service.

   @author CareScience
   @version 1.2, 20020627
*/
public class CdrAdapterSql
  implements ExternalLocatorAdapter
{
  protected Logger log;
  private String dataSource = null;
  private IlsSystemProps prop = null;
  private IlsMap req = null;
  private Map paramMap = null;
  private Map lengthMap = null;

  private static final String SQL_PARAM_DELIME = ",";
  private static final char PREPEND_CHAR = '0';


  public IlsMap[] findLocators(IlsMap map)
    throws IlsException
  {
    Profile.begin("CdrAdapterSql.findLocators");

    req = map;
    log = Logger.getLogger("ILS");
    IlsMap[] locators;

    String type = req.getTypeCode();
    prop = IlsProps.getSystemPropsForType(req.getDomainId(), type);

    List list = query();

    // Convert the list to an array
    locators = (IlsMap[])list.toArray(new IlsMap[0]);

    Profile.end("CdrAdapterSql.findLocators");
    return locators;
  }

  /**
     Retrieves a DB Connection to use.  This will check to see if
     a DataSource is specified in the configuration and, if so,
     it will do a jndi lookup and get a connection from that.
     If no DataSource is specified, The JdbcDriver, JdbcUrl,
     JdbcUser and JdbcPwd must be specified.  In this case,
     a Connection is allocated from a local connection pool.

     @return The Connection object
  */
  private Connection getConnection()
  {
    Profile.begin("CdrAdapterSql.getConnection");
    Connection conn = null;
    dataSource = prop.getInterfaceProperty("DataSource");
    if (dataSource == null) {
      String dbDriver = prop.getInterfaceProperty("JdbcDriver");
      String dbURL = prop.getInterfaceProperty("JdbcUrl");
      String dbName = prop.getInterfaceProperty("JdbcUser");
      String dbPwd = prop.getInterfaceProperty("JdbcPwd");

      int min = prop.getInterfacePropertyInt("PoolMin", 0);
      int max = prop.getInterfacePropertyInt("PoolMax", 25);
      long shrink = prop.getInterfacePropertyLong("PoolShrink", 1800) * 1000;
      conn = ConnectionPool.getConnection(prop.getSystemName(),
                                          dbDriver, dbURL,
                                          dbName, dbPwd,
                                          min, max, shrink);
    } else {
      try {
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup(dataSource);
        conn = ds.getConnection();
      } catch (Exception e) {
        log.error(e, e);
      }
    }
    Profile.end("CdrAdapterSql.getConnection");
    return conn;
  }

  /**
     Release the connection allocated by getConnection().  If the
     Connection was from the DataSource, the Connection is closed.
     If the Connection was from the local pool, it is released
     back to the pool.

     @param conn The Connection object to release
     @param error Hint to indicate an error on the Connection.  Used
                  by the local pool to clean up bad connections.
  */
  private void releaseConnection(Connection conn, boolean error)
  {
    Profile.begin("CdrAdapterSql.releaseConnection");
    if (dataSource != null) {
      try {
        conn.close();
      } catch (Exception e) {}
    } else
      ConnectionPool.releaseConnection(conn, error);
    Profile.end("CdrAdapterSql.releaseConnection");
  }

  protected int bindingValue(String key, Object val, int idx, PreparedStatement ps)
    throws SQLException, IlsException {

    if (val instanceof java.util.Date) {
      java.util.Date d = (java.util.Date) val;
      ps.setTimestamp(++idx, new java.sql.Timestamp(d.getTime()));
    } else if (val instanceof String) {
      ps.setString(++idx, (String) val);
    } else {
      ps.setObject(++idx, val);
    }

    return idx;

  }

  /**
     Performs the database query based on the SQL, etc. from the
     configuration.  This is described fully in the ILS documentation.

     @return List of IlsMaps representing the locators found from the query.
   */
  private List query() throws IlsException
  {
    boolean deleteConn = false;
    Profile.begin("CdrAdapterSql.query");

    Connection conn = getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;

    List locatorList = null;

    List sqlParams = getSqlParams();
    // load parameter mapping
    loadParamMaps();

    try {
      String sql = prop.getInterfaceProperty("Sql", req.getMap());
      log.debug("SQL= " + sql);
      // Set parameters to the PreparedStatement.
      Profile.begin("CdrAdapterSql.prepareStatement");
      ps = conn.prepareStatement(sql);
      Profile.end("CdrAdapterSql.prepareStatement");

      String paramIdx;
      Map map = req.getMap();

      int cnt = sqlParams.size();
      int idx_bind = 0;
      for(int idx = 0; idx < cnt; idx++) {
        String key = (String) sqlParams.get(idx);

        Object val = getValue(map, key);
        idx_bind = bindingValue(key, val, idx_bind, ps);
        log.debug(key + " = " + val);

      }

      // Execute the query.
      Profile.begin("CdrAdapterSql.executeQuery");
      rs = ps.executeQuery();
      Profile.end("CdrAdapterSql.executeQuery");

      // Parse the ResultSet.
      locatorList = parseResultSet(rs);

      rs.close();
    }
    catch (SQLException e) {
      log.error(e, e);
      deleteConn = true;
    } finally {

      if (rs != null)
        try {
          rs.close();
        } catch (SQLException e) {
          log.error(e, e);
        }
      if (ps != null)
        try {
          ps.close();
        } catch (SQLException e) {
          log.error(e, e);
        }
      if (conn != null)
        releaseConnection(conn, deleteConn);
    }
    Profile.end("CdrAdapterSql.query");
    return locatorList;
  }

  /**
   * Parses the <code>ResultSet</code>. Creates a new <code>HashMap</code> and
   * assigns values to the <code>HashMap</code>.
   *
   * @param rs the <code>ResultSet</code>.
   * @return List containing <code>HashMaps</code>. Each <code>HashMap</code>
   * contains record locator information.
   * @throws SQLException if a database access error occurs
   */
  private List parseResultSet(ResultSet rs)
    throws SQLException
  {
    Profile.begin("CdrAdapterSql.parseResultSet");
    List locatorList = new ArrayList();
    IlsMap locator = null;
    boolean na;
    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");

	java.sql.Timestamp tmpDate=null;
	ResultSetMetaData meta = rs.getMetaData();
    int cnt = meta.getColumnCount();

    Map sqlMap = getSqlMap();

    while (rs.next()) {
      locator = new IlsMap(req);

      try {

        for(int i=1;i<=cnt;i++) {
          String col = meta.getColumnName(i);
          String key = (String) sqlMap.get(col.toUpperCase());
          if (key == null) {
            log.warn("Column " + col +
                     " is not mapped to any key in the IlsProps.xml SqlResults");
          } else {
            Object obj = rs.getObject(i);
            if (obj != null) {
              switch (meta.getColumnType(i)) {
              case Types.DATE:
              case Types.TIME:
              case Types.TIMESTAMP:
                tmpDate = rs.getTimestamp(i);
				java.util.Date dayTime = new java.util.Date();
				dayTime.setTime(tmpDate.getTime());
                locator.put(key, dayTime);
                break;

              default:
                locator.put(key, obj.toString());
                break;
              }
            }
          }
        }

        locatorList.add(locator);

      } catch (Exception e) {
        log.error(e, e);
      }
    }

    Profile.end("CdrAdapterSql.parseResultSet");
    return locatorList;
  }


  /**
     Parses the SqlResults from the IlsProps.xml and returns a Map object
     with COLUMN,TO as the map key,value pairs.

     return Map containing the SqlResults values.
  */
  private Map getSqlMap()
  {
    HashMap map = new HashMap();
    Element params = prop.getInterfaceElement("SqlResults", req.getMap());
    List nList = params.getChildren("Param");

    for (int i = 0; i < nList.size(); i++) {
      Element node = (Element) nList.get(i);
      map.put(node.getAttributeValue("Column").toUpperCase(),
              node.getTextTrim());
    }

    return map;
  }

  /**
     Parses the SqlParams from the IlsProps.xml and returns a Map object
     with PARAM,INDEX as the map key,value pairs.  The PARAMs are as
     defined in the PARAMS array.  INDEX will be the 1-based index used
     by the PreparedStatement.set*() methods.

     return Map containing the SQL_PARAM values.
  */
  private List getSqlParams()
  {
    ArrayList list = new ArrayList();

    Element params = prop.getInterfaceElement("SqlParams", req.getMap());
    List nList = params.getChildren("Param");

    for (int i = 0; i < nList.size(); i++) {
      Element node = (Element) nList.get(i);
      list.add(node.getTextTrim());
    }

    return list;
  }

  /**
     Parses the ParamMaps from the IlsProps.xml and load the paramMap object
     with Parameter Name, Parameter value Map as the map key,value pairs.

  */
  private void loadParamMaps()
  {
    log.debug("loadParamMaps");
    if (paramMap != null ) {
       // already loaded
        return;
    }
    paramMap = new HashMap();
    ArrayList list = new ArrayList();

    Element mappings = prop.getInterfaceElement("ParamMaps", req.getMap());
    if (mappings == null ) {
        // no parameter mapping for this interface
        return;
    }
    // build Parameter Mapping
    List nList = mappings.getChildren("Map");

    Attribute attr;
    Element node;
    for (int i = 0; nList != null && i < nList.size(); i++) {
      node = (Element) nList.get(i);
      attr = node.getAttribute("Name");
      if (attr == null) {
         log.warn("Name attribute is missing in <Map> tag.");
         continue;
      }
      addToParamMap(attr.getValue(), node.getChildTextTrim("Key"), node.getChildTextTrim("Value"));
    }
    log.debug("ParamMaps size "+ paramMap.size());

    // build Parameter value length
    List lengthList = mappings.getChildren("ParamLength");

    for (int i = 0; lengthList != null && i < lengthList.size(); i++) {
      node = (Element) lengthList.get(i);
      attr = node.getAttribute("Name");
      if (attr == null) {
         log.warn("Name attribute is missing in <ParamLength> tag.");
         continue;
      }
   	  if (lengthMap ==  null) {
		lengthMap = new HashMap();
	  }
	  lengthMap.put(attr.getValue(), node.getTextTrim());
    }
    log.debug("ParamLength size "+ lengthMap.size());

  }

  private void addToParamMap(String name, String key, String val) {
    if (name == null || name.length() == 0) {
        log.warn("Name attribute in <Map> tag has no value.");
        return;
    }

    if (key == null || val == null ) {
        log.warn("Couldn't find <Key> or <Value> under <Map>.");
        return;
    }

    Map keyMap = (Map) paramMap.get(name);
    if (keyMap ==  null) {
        keyMap = new HashMap();
    }
    keyMap.put(key, val);
    paramMap.put(name, keyMap);
  }

  // Handles composite String typed paramters
  private Object getValue(Map map, String key) throws IlsException {
      if (key.indexOf(SQL_PARAM_DELIME) != -1) {
        // composite paramater
        StringBuffer value = new StringBuffer();
        StringTokenizer keys = new StringTokenizer(key, SQL_PARAM_DELIME);
        String nextKey, paramLength;
        Object val;
        Map keyMap;
        while (keys.hasMoreTokens()) {
            nextKey = keys.nextToken();
            keyMap = (Map) paramMap.get(nextKey);
            val = map.get(nextKey);

            // check for length
            if (lengthMap != null) {
	            paramLength = (String) lengthMap.get(nextKey);
				if (paramLength != null && paramLength.length() > 0 && val != null) {
					int len = Integer.valueOf(paramLength).intValue();
					int valLen = val.toString().length();
					if (valLen > len) {
						throw new IlsException(nextKey+" is longer than "+paramLength);
					} else if (valLen < len) {
						char[] prepend = new char[len - valLen];
						Arrays.fill(prepend, PREPEND_CHAR);
						// prepend the characters
						value.append(prepend);
					}
				}
			}


            if ( keyMap == null) {
                value.append(val);
            } else {
                if (keyMap.containsKey(val)) {
                    value.append(keyMap.get(val));
                } else {
					throw new IlsException(val +" is missing in <Key> for parameter "+nextKey);
				}
            }
        }
        return value.toString();
      } else {
          // single paramater
          return map.get(key);
      }
  }
}
