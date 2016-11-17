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
package com.hinacom.pix.mesatests.pdq;

import java.util.ArrayList;
import java.util.List;

import com.hinacom.pix.data.MessageHeader;
import com.hinacom.pix.data.Patient;
import com.hinacom.pix.ihe.IPdSupplierAdapter;
import com.hinacom.pix.ihe.PdSupplierException;
import com.hinacom.pix.ihe.pdq.PdqQuery;
import com.hinacom.pix.ihe.pdq.PdqResult;

/**
 * @author Wenzhi Li
 * @version 1.0, Dec 26, 2008
 */
public class MockupPdSupplierAdapter implements IPdSupplierAdapter {
    /**
     * Finds a list of matched patients based on PDQ query parameters.
     *
     * @param query the <code>PdqQuery</code>
     * @param header the <code>MessageHeader</code>
     * @throws PdSupplierException when there is trouble finding the patients
     * @return a <code>PdqResult</code> which contains a list of list 
     *         of <code>Patient</code  The first list is a list 
     *         of different logic patients, while the second list is a list of 
     *         the same patient in different domain systems. PdqResult also 
     *         contains a continuation reference number.
     * @see PdqResult        
     */
    public PdqResult findPatients(PdqQuery query, MessageHeader header) throws PdSupplierException {
    	PdqResult ret = new PdqResult(new ArrayList<List<Patient>>());
    	return ret;
    }
 
    /**
     * Cancels the existing PDQ Query whose reference id is given by pointer.
     * 
     * @param String the tag of query to be canceled.
     * @param messageQueryName the messageQueryName 
     * @throws PdSupplierException when there is trouble canceling the query.
     */
    public void cancelQuery(String queryTag, String messageQueryName) throws PdSupplierException{
    	
    }

}
