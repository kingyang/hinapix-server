package org.openempi.ics.db.jdbc;

/**
 * Title:        Sql
 * Description:  Handles all SQL-related functionality
 * Copyright:    Copyright (c) 2002
 * Company:      CareScience
 * @author J. Mangione
 * @version
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openempi.data.SearchRange;
import org.openempi.ics.db.DatabaseException;
import org.openempi.ics.db.QueryParam;
import org.openempi.ics.db.QueryParamList;
import org.openempi.ics.utility.IcsSqlXML;
import org.openempi.ics.utility.Profile;

/**
 * This class will handle all generic SQL methods, such as building dynamic statements and executing queries.
 */
public class Sql {


  /**
     Will build part of the where clause to query based on a particular 
     column / value.
     
     @param colName real database column name retrieved from COLNAME attribute in IcsSql XML
     @param attrColType String, Number or Date as specified in COLTYPE attribute in IcsSql XML
     @param value value this column is being set to.
     @param valueList a List to which specific values are added for
                      and SQL parameters that will later be used
                      to add values to the PreparedStatement.
     @throws DatabaseException
     @return String where clause without the keyword: where (example: pn.last_name = 'Mangione')
  */
  public static String buildWhereClause(String colName, String attrColType, 
                                        Object value, List valueList)
    throws DatabaseException
  {
    return addSqlCondition(colName, attrColType, value,
                           valueList, new StringBuffer()).toString();
  }

  /**
     Will build part of the where clause to query based on a particular 
     column / value.
     
     @param colName real database column name retrieved from COLNAME attribute in IcsSql XML
     @param attrColType String, Number or Date as specified in COLTYPE attribute in IcsSql XML
     @param value value this column is being set to.
     @param valueList a List to which specific values are added for
                      and SQL parameters that will later be used
                      to add values to the PreparedStatement.
     @param buf StringBuffer to append SQL to
     @throws DatabaseException
     @return StringBuffer where clause without 
  */
  public static StringBuffer addSqlCondition(String colName, 
                                             String attrColType, 
                                             Object value, 
                                             List valueList, 
                                             StringBuffer buf)
    throws DatabaseException
  {
    String filterClause; // start with the column name.
    boolean bind = (valueList != null);

    if ((attrColType.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_USTRING) == 0) ||
        (attrColType.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_LIKE_USTRING) == 0)) {
      buf.append(" UPPER(").append(colName).append(")");
    } else {
      buf.append(" ").append(colName);
    }
    
    // Determine if we're dealing with a Range or a single value
    if ( value instanceof SearchRange )
      {
        SearchRange srVal = (SearchRange) value;
        String val1 = valToSql(attrColType, (Object) srVal.getStart(), bind);
        String val2 = valToSql(attrColType, (Object) srVal.getEnd(), bind);

        if (attrColType.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_USTRING) == 0 ) {
          buf.append(" between UPPER(").append(val1).append(") and UPPER(").append(val2).append(")");
          if (bind) {
            valueList.add(srVal.getStart());
            valueList.add(srVal.getEnd());
          }

        } else {
          buf.append(" between ").append(val1).append(" and ").append(val2);
          if (bind) {
            valueList.add(srVal.getStart());
            valueList.add(srVal.getEnd());
          }
        }
      }
    else if ( value instanceof String )
      {
        String sVal = (String) valToSql(attrColType,value,bind);
        if (bind)
          valueList.add(value);
        
        if ( attrColType.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_USTRING) == 0 ) {
          buf.append(" = UPPER(").append(sVal).append(")");
        } else if ( attrColType.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_LIKE_USTRING) == 0 ) {
          buf.append(" like UPPER(").append(sVal).append(")");
        } else if ( attrColType.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_LIKE_STRING) == 0 ) {
          buf.append(" like ").append(sVal);
        } else {
          buf.append(" = ").append(sVal);
        }
      }
    else if (value instanceof java.util.Date) 
      {
        java.sql.Date sDate = new java.sql.Date(((java.util.Date)value).getTime());
        String sVal = (String) valToSql(attrColType,sDate,bind);
        if (bind)
          valueList.add(sDate);
        buf.append(" = ").append(sVal);
      }
    else 
      {
        String sVal = (String) valToSql(attrColType,value,bind);
        if (bind)
          valueList.add(value);
        buf.append(" = ").append(sVal);
      }
    return buf;
  }

  /**
     Builds the SQL representation of the specified QueryParamList. 
     Depends on what QueryParams and nested QueryParamsLists there 
     are, this will build it and return the SQL.
     
     @param icssql the helper class for reading in the IcsSQL XML File
     @param params The QueryParamList to build the SQL for
     @param valueList a List to which specific values are added for
                      and SQL parameters that will later be used
                      to add values to the PreparedStatement.
     @param buf StringBuffer to append SQL to
     @return The SQL generated from the QueryParamList 
     @see QueryParamList
  */
  private static StringBuffer buildSql(IcsSqlXML icssql, QueryParamList params,
                                       List valueList, StringBuffer buf)
    throws DatabaseException
  {
    Element attrElement = null; 
    Iterator iter = params.iterator();
    boolean first = true;  // Set to false after first loop iteration
    String op;             // SQL logical operand.  Either "or" or "and"
    
    switch (params.getType()) {
    case QueryParamList.OR_LIST:
      op = " or ";
      break;
    case QueryParamList.AND_LIST:
      op = " and ";
      break;
    default:
      throw new DatabaseException("Invalid QueryParamList type: " + params.getType()); 
    }
    
    while(iter.hasNext()) {
      if (first) {
        buf.append(" (");
        first = false;
      } else 
        buf.append(op).append(" (");
      
      Object p = iter.next();

      if (p instanceof QueryParamList)
        // If this is a nested param list, recurse to build that list
        buf = buildSql(icssql, (QueryParamList) p, valueList, buf);
      else {
        QueryParam param = (QueryParam) p;
        Integer attributeType = param.getAttributeType();
        Object value = param.getValue();
        
        attrElement = icssql.getElement("QUERY-ATTRIBUTE-TYPES").getChild(IcsSqlXML.ATTR_TAG + attributeType.toString());
        
        buf = addSqlCondition(attrElement.getChildText("COLNAME"), 
                              attrElement.getChildText("COLTYPE"), 
                              value, valueList, buf);
      }
      buf.append(") ");
    }
    return buf;
  }
  
  /**
     Build the entire SQL statement based on the QueryParam array.
     QueryParams are looked up in the IcsSql XML file and a SQL statement 
     is constructed with ORs between each of the parameters in the 
     QueryParams array.  
     
     @param icssql the helper class for reading in the IcsSQL XML File
     @param params The QueryParamsList containing the search parameters 
     to build the where clause
     @param conn The database Connection to use
     @throws DatabaseException
     @return The PreparedStatement ready to be executed.
  */
  public static PreparedStatement buildQuery(IcsSqlXML icssql, 
                                             QueryParamList params, 
                                             Connection conn)
    throws DatabaseException
  {
    Element sqlElement = null;  // xml element containing SQL
    ArrayList valueList = new ArrayList(); // Use to track values for st
    PreparedStatement st;  // Prepapred statement to build and return
    StringBuffer buf = new StringBuffer();  // Used to build SQL
    int i;
    
    try {
      sqlElement =  icssql.getElement("QUERY-GETPERSONS");

      buf.append(sqlElement.getChildText("SQL-SELECT")).append(" ");
      buf.append(sqlElement.getChildText("SQL-FROM")).append(" ");
      buf.append(sqlElement.getChildText("SQL-JOIN")).append(" and ( ");

      buf = buildSql(icssql, params, valueList, buf);
      
      // The sqlJOIN currently has a sub-select with an open left-paren.
      // This is why we have an extraclose right-paren before the order 
      // by. It's a hack, but there you go.
      buf.append(")) order by 1"); // order must be PERSON ID
    } catch ( NullPointerException npe ) {
      throw new DatabaseException("Cannot retrieve QUERY-GETPERSONS or QUERY-ATTRIBUTE-TYPES from SQL XML"); 
    }

    Logger log = Logger.getLogger("ICS");
    try {
      log.debug(buf);
      Profile.begin("Connection.prepareStatement");
      st = conn.prepareStatement(buf.toString());
      Profile.end("Connection.prepareStatement");
      
      int len = valueList.size();
      for(i=0;i<len;i++) {
        st.setObject(i+1, valueList.get(i));
      }
    } catch (SQLException e) {
      log.error(e, e);
      throw new DatabaseException(e);
    }
    return st;
  }

  /**
     Build the entire SQL statement based on the QueryParam array.
     QueryParams are looked up in the IcsSql XML file and a SQL statement 
     is constructed with ORs between each of the parameters in the 
     QueryParams array.  
     
     @param icssql the helper class for reading in the IcsSQL XML File
     @param params The QueryParamsList containing the search parameters 
     to build the where clause
     @param conn The database Connection to use
     @throws DatabaseException
     @return The PreparedStatement ready to be executed.
  */
  public static String buildStatement(IcsSqlXML icssql, 
                                      QueryParamList params, 
                                      Connection conn)
    throws DatabaseException
  {
    Element sqlElement = null;  // xml element containing SQL
    PreparedStatement st;  // Prepapred statement to build and return
    StringBuffer buf = new StringBuffer();  // Used to build SQL
    int i;
    
    try {
      sqlElement =  icssql.getElement("QUERY-GETPERSONS");

      buf.append(sqlElement.getChildText("SQL-SELECT")).append(" ");
      buf.append(sqlElement.getChildText("SQL-FROM")).append(" ");
      buf.append(sqlElement.getChildText("SQL-JOIN")).append(" and ( ");

      buf = buildSql(icssql, params, null, buf);
      
      // The sqlJOIN currently has a sub-select with an open left-paren.
      // This is why we have an extraclose right-paren before the order 
      // by. It's a hack, but there you go.
      buf.append(")) order by 1"); // order must be PERSON ID
    } catch ( NullPointerException npe ) {
      throw new DatabaseException("Cannot retrieve QUERY-GETPERSONS or QUERY-ATTRIBUTE-TYPES from SQL XML"); 
    }

    return buf.toString();
  }

  private static String valToSql(String type, Object val, boolean bind)
  {
    if (bind)
      return "?";

    String quote = "";
    if (type.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_USTRING) == 0 ||
        type.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_LIKE_USTRING) == 0 ||
        type.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_STRING) == 0 ||
        type.compareToIgnoreCase(IcsSqlXML.ATTR_COLTYPE_LIKE_STRING) == 0)
      quote = "'";
    return quote + val.toString() + quote;
  }

  
}
