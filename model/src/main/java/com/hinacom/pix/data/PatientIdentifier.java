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
package com.hinacom.pix.data;

import com.misyshealthcare.connect.net.Identifier;


/**
 * This class represents a patient identifier 
 * 
 * @author Wenzhi Li
 * @version 1.0, Nov 14, 2008
 * @see PersonIdentifier
 */
public class PatientIdentifier extends PersonIdentifier {
	
	public PatientIdentifier(){		
		super();
	}
	public PatientIdentifier(String id, Identifier assigningAuthority) {
		super(id, assigningAuthority);
	}
}
