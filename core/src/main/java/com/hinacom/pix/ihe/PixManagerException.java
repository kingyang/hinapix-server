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
package com.hinacom.pix.ihe;

/**
 * This exception is generated when there is a problem
 * with PixManager operations
 * 
 * @author Wenzhi Li
 * @version 1.0 - Oct 21, 2008
 */
public class PixManagerException extends Exception {

	private static final long serialVersionUID = 4496513837046385313L;
	/**
	 * Create a new PixManagerException.
	 * 
	 * @param string A description of the problem
	 */
	public PixManagerException(String string) {
		super(string);
	}

    public PixManagerException(String msg, Throwable cause){
        super(msg, cause);
    }

    public PixManagerException(Throwable cause) {
        super(cause);
    }
}
