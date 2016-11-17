/*
 * NDCCdrAdapterSql.java
 *
 * Created on August 9, 2004, 4:21 PM
 */

package org.openempi.ils.adapters;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.openempi.ils.IlsException;

/**
 *
 * @author  kzheng
 */
public class NDCCdrAdapterSql extends CdrAdapterSql {
    
    
    
    protected int bindingValue(String key, Object val, int idx, PreparedStatement ps) throws SQLException, IlsException{
        
        if (val instanceof Date) {
          Date d = (Date) val;
          ps.setTimestamp(++idx, new java.sql.Timestamp(d.getTime()));
        } else if (val instanceof String) {
            if ("patientId".equals(key)) {
                StringTokenizer strTz = new StringTokenizer((String)val, "|");
                if(strTz.countTokens() != 7){
                    throw new IlsException("Invalid patientId value");
                }
                    
                String newVal;
                int count = 0;
                while (strTz.hasMoreTokens()) {
                  newVal = (String) strTz.nextToken();
                  if (count++ == 4 && newVal != null && newVal.length() > 0) {
                      // DOB
                     SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                     try {
                         Date d = formatter.parse(newVal);
                         ps.setTimestamp(++idx, new java.sql.Timestamp(d.getTime()));
                     } catch (java.text.ParseException e) {
                         e.printStackTrace();
                        throw new IlsException("Invalid DOB format");
                     }
                  } else {
                    ps.setString(++idx, newVal);
                  }
                  log.debug("patientID part"+idx+":"+newVal);
                }
            } else {
              ps.setString(++idx, (String) val);
            }
        } else {
          ps.setObject(++idx, val);
        }
        
        return idx;
    }
}