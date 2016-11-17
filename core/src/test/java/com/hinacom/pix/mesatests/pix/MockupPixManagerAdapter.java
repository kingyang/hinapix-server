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
package com.hinacom.pix.mesatests.pix;

import java.util.ArrayList;
import java.util.List;

import com.hinacom.pix.data.MessageHeader;
import com.hinacom.pix.data.Patient;
import com.hinacom.pix.data.PatientIdentifier;
import com.hinacom.pix.ihe.IPixManagerAdapter;
import com.hinacom.pix.ihe.PixManagerException;

import com.misyshealthcare.connect.net.Identifier;

/**
 * 
 * @author Wenzhi Li
 * @version 1.0, Dec 24, 2008
 */
public class MockupPixManagerAdapter implements IPixManagerAdapter {

	@Override
    public boolean isValidPatient(PatientIdentifier pid, MessageHeader header) throws PixManagerException {
    	return true;
    }

	@Override
    public List<PatientIdentifier> findPatientIds(PatientIdentifier pid, MessageHeader header) throws PixManagerException {
    	return new ArrayList<PatientIdentifier>();
    }

	@Override
	public List<PatientIdentifier> createPatient(Patient patient, MessageHeader header) throws PixManagerException {
		Identifier id = patient.getPatientIds().get(0).getAssigningAuthority();
		System.out.println("name space=" + id.getNamespaceId());
		System.out.println("universal Id=" + id.getUniversalId());
		System.out.println("univ Id type=" + id.getUniversalIdType());
		return null;
	}

	@Override
	public List<List<PatientIdentifier>> updatePatient(Patient patient, MessageHeader header) throws PixManagerException{
		return null;
	}

	@Override
	public List<List<PatientIdentifier>> mergePatients(Patient patientMain, Patient patientOld, MessageHeader header) throws PixManagerException {
		return null;
	}

}
