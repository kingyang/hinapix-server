/* Copyright 2009 Misys PLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License. 
 */
package com.hinacom.pix.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 * This class contains utility methods for date
 * 
 * @author Rasakannu Palaniyandi
 * @version Dec 01, 2008
 */
public class DateUtil {
	
	 private static SimpleDateFormat hl7formatter1 = new SimpleDateFormat("yyyyMMdd");
	 private static SimpleDateFormat hl7formatter2 = new SimpleDateFormat("yyyyMMddHHmm");
	 private static SimpleDateFormat DTMformatter = new SimpleDateFormat("yyyyMMddHHmmssZ");
	 /**
	  * Converts the date String into Calender 
	  * 
	  * @param fromDate as String to be converted from
	  * @return Calender
	  * 
	  */
	 public static Calendar convertHL7DateToCalender (String fromDate) {
        if(fromDate == null)
            return null;
        try {
            Date date = null;
            if(fromDate.length() == 8)
                date = hl7formatter1.parse(fromDate);
            else
                date = hl7formatter2.parse(fromDate);
            Calendar cal=Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch(ParseException pex) {            
            return null;
        }
	 }
 
	 /*** 
	  * Converts the date String into date format 
	  * 
	  * @param fromDate as String to be converted from
      * @return Date
	  * 
	  */
	public static Date convertHL7Date(String fromDate) {
        if(fromDate == null)
        	return null;

        try {
            Date date = null;
            if(fromDate.length() == 8)
                date = hl7formatter1.parse(fromDate);
            else
                date = hl7formatter2.parse(fromDate);
            
            return date;
        } catch(ParseException pex) {            
            return null;
        }
    }
	/**
	 * Formats a date/time according to the HL7 v2.3.1 spec unless a
	 * custom format string is supplied, then use that.
	 * 
	 * @param date the date/time to format
	 * @param formatString a custom format string, or NULL for the default
	 * @return the formatted data as a string
	 */
	public static String formatDateTime(Date date, String formatString) {
		if (formatString == null) {
			return formatDateTime(date);
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat(formatString);
			return formatter.format(date);
		}
	}

	
	/**
	 * Formats a date/time according to the HL7 v2.3.1 spec.
	 * 
	 * @param date the date/time to format
	 * @return the formatted data/time as a string
	 */
	public static String formatDateTime(Date date) {
		return DTMformatter.format(date);
	}


}
