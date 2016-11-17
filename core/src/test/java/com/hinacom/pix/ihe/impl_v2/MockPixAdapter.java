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
package com.hinacom.pix.ihe.impl_v2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.hinacom.pix.data.MessageHeader;
import com.hinacom.pix.data.Patient;
import com.hinacom.pix.data.PatientIdentifier;
import com.hinacom.pix.data.PersonName;
import com.hinacom.pix.ihe.IPdSupplierAdapter;
import com.hinacom.pix.ihe.IPixManagerAdapter;
import com.hinacom.pix.ihe.PdSupplierException;
import com.hinacom.pix.ihe.PixManagerException;
import com.hinacom.pix.ihe.pdq.PdqQuery;
import com.hinacom.pix.ihe.pdq.PdqResult;

import com.misyshealthcare.connect.base.SharedEnums.SexType;
import com.misyshealthcare.connect.net.Identifier;

/**
 * Title				:	
 * Description			:	This is the mock implementation class for Pix query class
 * Copyright			:	Copyright (c) 2008
 * Company				:	Misys Healthcare Systems
 * Created By			:	Rasakannu Palaniyandi 	
 * Created Date			:	Nov 21, 2008
 * Last Modified By		:	
 * Last Modified Date	:
 */

public class MockPixAdapter implements IPixManagerAdapter,IPdSupplierAdapter{
	PatientIdentifier patientIdentifier=null;

	@Override
	public List<PatientIdentifier> createPatient(Patient patient, MessageHeader header)
			throws PixManagerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PatientIdentifier> findPatientIds(PatientIdentifier pid, MessageHeader header)
			throws PixManagerException {
		Boolean flag=false;
		List<PatientIdentifier> returnPat= new ArrayList<PatientIdentifier>();
		List<PatientIdentifier> pList= getIdentifier();
		for(PatientIdentifier pd : pList){
		  if(pd.getId().equals(pid.getId()) && pd.getAssigningAuthority().equals(pid.getAssigningAuthority())){ 	
              flag=true;
		  } 
		}
		if(flag){
		if(pid.getId().equals("112234")){
			return returnPat; 
		}else{
			for(PatientIdentifier pd : pList){
				  if(pd.getId().equals(pid.getId())){ 	
					  
				  } 
				  else{
					  returnPat.add(pd);
				  }
			}
		}
		}
		return returnPat;
	}

	@Override
	public boolean isValidPatient(PatientIdentifier pid, MessageHeader header) {
		List<PatientIdentifier> pList= getIdentifier();
		for(PatientIdentifier pd : pList){
		  if(pd.getId().equals(pid.getId()) && pd.getAssigningAuthority().equals(pid.getAssigningAuthority())){ 	
             return true;			  
		  }
		}
		return false;
	}

	@Override
	public List<List<PatientIdentifier>> mergePatients(Patient patientMain,
			Patient patientOld, MessageHeader header) throws PixManagerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<List<PatientIdentifier>> updatePatient(Patient patient, MessageHeader header)
			throws PixManagerException {
		// TODO Auto-generated method stub
		return null;
	}
	private List<PatientIdentifier> getIdentifier(){
		List<PatientIdentifier> pList = new ArrayList<PatientIdentifier>();
		PatientIdentifier identifier1 = new PatientIdentifier();
		Identifier ident1=new Identifier("CPR","null","null");
		identifier1.setId("112234");
		identifier1.setAssigningAuthority(ident1);
		pList.add(identifier1);
		PatientIdentifier identifier2 = new PatientIdentifier();
		Identifier ident2=new Identifier("HomeCare","null","null");
		identifier2.setId("112235");
		identifier2.setAssigningAuthority(ident2);
		pList.add(identifier2);
		PatientIdentifier identifier3 = new PatientIdentifier();
		Identifier ident3=new Identifier("WESTCLINIC","1.2234.634325.5734","ISO");
		identifier3.setId("12345678");
		identifier3.setAssigningAuthority(ident3);
		pList.add(identifier3);
		PatientIdentifier identifier4 = new PatientIdentifier();
		Identifier ident4=new Identifier("MIEH","1.2234.634325.5746","ISO");
		identifier4.setId("536253");
		identifier4.setAssigningAuthority(ident4);
		pList.add(identifier4);
		return pList;
	}

	@Override
	public void cancelQuery(String queryTag, String messageQueryName)
			throws PdSupplierException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PdqResult findPatients(PdqQuery query, MessageHeader header)
			throws PdSupplierException {
		List<List<Patient>> pList =new ArrayList<List<Patient>>();
		List<Patient> patientList1 = new ArrayList<Patient>();
		List<Patient> patientList2 = new ArrayList<Patient>();
		Patient patient1 =new Patient();
		Patient patient2 = new Patient();
		PersonName personName = query.getPersonName();
		String prefix =query.getPrefix();
		String suffix = query.getSuffix();
		SexType sexType =query.getSex();
		String ssn = query.getSsn();
		String pointer = query.getContinuationPointer();
		int requestNo =query.getHowMany();
		Calendar birthDate =query.getBirthDate();
		if(personName != null){
			patient1.setPatientName(getPersonName("jhon","smith","M","R"));			
			patient1.addPatientId(getPatientIdentifier());
			patient2.setPatientName(getPersonName("vijay","raja","P","S"));
			patient2.addPatientId(getPatientIdentifier());
		}
		patientList1.add(patient1);
		patientList2.add(patient2);
		pList.add(patientList1);
		pList.add(patientList2);
		PdqResult pdqResult =new PdqResult(pList);
		return pdqResult;
	}
   private PersonName getPersonName(String fName,String lName,String Px,String Sx){
	   PersonName personName =new PersonName();
	   personName.setFirstName(fName);
	   personName.setLastName(lName);
	   personName.setPrefix(Px);
	   personName.setSuffix(Sx);
	   return  personName;
   }
   private PatientIdentifier getPatientIdentifier(){
	   PatientIdentifier patientIdentifier =new PatientIdentifier();
	   patientIdentifier.setAssigningAuthority(new Identifier("CPR","sd",""));	   
	   patientIdentifier.setId("100101");
	   return patientIdentifier;
   }
   
}
