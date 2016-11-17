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

/**
 * A utility class to help manipulate the Exception class.
 * 
 * @author Wenzhi Li
 * @version 1.0, Feb 26, 2009
 */
public class ExceptionUtil {

	/**
	 * Strips away the exception class path preceding the exception message,
	 * so the message length would be shortened.
	 * 
	 * @param exceptionMessage
	 * @return a shortened exception message without preceding class path
	 */
	public static String strip(String exceptionMessage) {
		int index = exceptionMessage.lastIndexOf("Exception:");
		if (index == -1) 
			return exceptionMessage;
		else
			return exceptionMessage.substring(index+10);
	}


  public static void main(String[] args){
	 String msg = "PIXManager Exception: ApplicatioException: Both first name and last name cannot be NULL";
     String stripped = strip(msg);
     System.out.println(stripped);
  }	  
}