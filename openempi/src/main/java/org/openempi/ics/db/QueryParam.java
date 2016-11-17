package org.openempi.ics.db;

import org.openempi.ics.utility.IcsSqlXML;

public class QueryParam
{
  /**
     The attribute type to query against,
     which should be a type in the AttributeType interface.
  */
  private Integer attributeType;

  /**
       An object representing the value for the attribute type
    */
  private Object value;

  /** 
        Constructs a new QueryParam
        
        @param attributeType The attribute type to query against,
                             which should be a type in the AttributeType 
                             interface.
        @param value The value represented as an Object 
    */
  public QueryParam(Integer attributeType, Object value)
  {
    this.attributeType = attributeType;
    this.value = value;
  }

  /** 
        Constructs a new QueryParam
        
        @param attributeType The attribute type to query against,
                             which should be a type in the AttributeType 
                             interface.
        @param value The value represented as an String
    */
  public QueryParam(Integer attributeType, String value)
  {
    this.attributeType = attributeType;
    this.value = (Object) value;
  }

  /** 
        Constructs a new QueryParam
        
        @param attributeType The attribute type to query against,
                             which should be a type in the AttributeType 
                             interface.
        @param value The value represented as an int
    */
  public QueryParam(Integer attributeType, int value)
  {
    this.attributeType = attributeType;
    this.value = new Integer(value);
  }

  /** 
        Constructs a new QueryParam
        
        @param attributeType The attribute type to query against,
                             which should be a type in the AttributeType 
                             interface.
        @param value The value represented as an int
    */
  public QueryParam(Integer attributeType, long value)
  {
    this.attributeType = attributeType;
    this.value = new Long(value);
  }

  /**
       @return The attributeType for this QueryParam
    */
  public Integer getAttributeType()
  {
    return attributeType;
  }

  /**
       @return The value of this QueryParam as an Object.
    */
  public Object getValue()
  {
    return value;
  }


  public String toString()
  {
    IcsSqlXML icssql = IcsSqlXML.getInstance(); // contains all SQL needed from xml file
    String col = icssql.getElement("QUERY-ATTRIBUTE-TYPES").getChild(IcsSqlXML.ATTR_TAG + attributeType.toString()).getChildText("COLNAME");
    return col + "=" + value;
  }
}
