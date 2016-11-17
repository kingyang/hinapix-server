package org.openempi.ics.db;

import java.util.ArrayList;

/**
   This is an extenstion of the ArrayList class to support lists of
   QueryParams for use by the DatabaseServices class.  The intent
   is that you create QueryParamLists to represent to values to 
   be used in a where clause in SQL that will be eventually generated.
   
   QueryParamLists have (currently) two basic types: OR_LIST and AND_LIST.
   An OR_LIST indicates that all the QueryParams in the list will be 
   OR'd together in the eventual SQL.  This is also called DISJUNTION_LIST.
   An AND_LIST indicates that all the QueryParams in the list will be
   AND'd together in the eventual SQL.  This is also called a CONJUNCTION_LIST.

   A QueryParamList may contain two classes of elements.  It may contain
   QueryParams or other QueryParamLists.  This allows you to mix ANDs and
   ORs together in a single SQL statement by nesting QueryParamLists
   of different types.  For example, the following:
   <code>
   QueryParamList params = new QueryParamList(QueryParamList.OR_LIST);
   params.add(att1, val1);
   params.add(att2, val2);

   QueryParamList params2 = new QueryParamList(QueryParamList.AND_LIST);
   params2.add(att3, val3);
   params2.add(att4, val4);
   
   param.add(param2);

   </code>

   would create a where clause that looks something like:

   <code>
   (att1 = val1) OR (att2 = val2) OR ( (att3 = val3) AND (att4 = val4) )
   </code>

   There is no limit to the nesting or combination you can make.
*/
public class QueryParamList
    extends ArrayList
{
  /**
     Specifies this is a SQL OR list
  */
  public static final int OR_LIST = 0;
  public static final int DISJUNCTION_LIST = 0;
  
  /**
     Specified this is a SQL AND list
  */
  public static final int AND_LIST = 1;
  public static final int CONJUNCTION_LIST = 1;
  
  /**
     The type of this list
  */
  private int _type;
  
  /**
     Prevent Construction of untyped lists
  */
  private QueryParamList()
  {
    super();
  }
  
  /**
     Construct a new list of the specified type
  */
  public QueryParamList(int type)
  {
    super();
    _type = type;
  }
  
  /**
     Adds a new paramters to the list by specifying the attribute type
     and value
     
     @param attributeType The attribute type to query against,
                          which should be a type in the AttributeType 
                          interface.
     @param value The value represented as an String
  */
  public void add(Integer attributeType, Object value)
  {
    QueryParam param = new QueryParam(attributeType, value);
    add(param);
  }
  
  /**
     Adds a new paramters to the list by specifying the attribute type
     and value
     
     @param attributeType The attribute type to query against,
                             which should be a type in the AttributeType 
                             interface.
     @param value The value represented as an int
  */
  public void add(Integer attributeType, int value)
  {
    QueryParam param = new QueryParam(attributeType, value);
    add(param);
  }
  
  /**
     Adds a new paramters to the list by specifying the attribute type
     and value
       
     @param attributeType The attribute type to query against,
                             which should be a type in the AttributeType 
                             interface.
     @param value The value represented as a long
  */
  public void add(Integer attributeType, long value)
  {
    QueryParam param = new QueryParam(attributeType, value);
    add(param);
  }
  
  /**
     Adds a new paramter to the list by specifying the QueryParam
     
     @param param A QueryParam object containing the attribute type and value
  */
  public void add(QueryParam param)
  {
    add((Object) param);
  }
  
  /**
     Adds a nested QueryParamList to the current list.
     
     @param params A QueryParamList object to nest
  */
  public void add(QueryParamList params)
  {
    add((Object) params);
  }
  
  /**
     Returns the type of this list
     
     @return The type.  One of the following: OR_LIST, AND_LIST
  */
  public int getType()
  {
    return _type;
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    int cnt = size();
    for(int i=0; i<cnt; i++) {
      if (i>0) {
        switch(_type) {
        case OR_LIST:
          buf.append(" OR ");
          break;
          
        case AND_LIST:
          buf.append(" AND ");
          break;
        }
      }
      buf.append(get(i).toString());
    }
    return buf.toString();
  }
}
