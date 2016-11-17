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
package com.hinacom.pix.mesatests.atna;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRegistry;

import com.hinacom.pix.data.Patient;
import com.hinacom.pix.data.PatientIdentifier;
import com.hinacom.pix.data.PersonName;
import com.hinacom.pix.ihe.audit.IheAuditTrail;
import com.hinacom.pix.ihe.audit.ParticipantObject;
import com.hinacom.pix.mesatests.MesaTestLogger;

import com.misyshealthcare.connect.base.AuditBroker;
import com.misyshealthcare.connect.base.DocumentQuery;
import com.misyshealthcare.connect.base.audit.ActiveParticipant;
import com.misyshealthcare.connect.net.ConnectionFactory;
import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.net.Identifier;
import com.misyshealthcare.connect.util.Pair;

/**
 * 
 * @author Wenzhi Li
 * @version 1.0,  Jan 7, 2009
 */
public class Test11180 {
	/**
	 * Run MESA Test 11180
	 */
	public static void main(String[] args) {
		String test = "11180";
		MesaTestLogger logger = new MesaTestLogger(System.out);
		logger.writeTestBegin(test);

		// -------  TEST SETUP  --------
		ConnectionFactory.loadConnectionDescriptionsFromFile("conf/mesatests/actors/AuditRepositoryConnections.xml");
		// Set up audit trail.
		ArrayList<IConnectionDescription> repositories = new ArrayList<IConnectionDescription>();
		repositories.add(ConnectionFactory.getConnectionDescription("log4j_audittrail"));
		repositories.add(ConnectionFactory.getConnectionDescription("mesa_arr_bsd"));
		AuditBroker broker = AuditBroker.getInstance();
		broker.registerAuditSource(new IheAuditTrail("SecureNode", repositories));
		IheAuditTrail pixMan = new IheAuditTrail("PixManager", repositories);

		// Patient
		Patient patient = new Patient();
		PatientIdentifier pid = new PatientIdentifier();
		pid.setId("123212");
		pid.setAssigningAuthority(new Identifier("ALEH", "1.3.6.1.4.1.21367.2009.1.2.335", "ISO"));
		patient.addPatientId(pid);
		PersonName name = new PersonName();
		name.setFirstName("Susan");
		name.setLastName("Formaldehyde");
		patient.setPatientName(name);
		ParticipantObject po = new ParticipantObject(patient);
        po.setDetail("143546"); 
		
		ActiveParticipant source = new ActiveParticipant();
		source.setUserId("ALLSCRIPTS|EHR_ALLSCRIPTS_CONNECT");
		source.setAccessPointId("127.0.0.1");
		
		Collection<ParticipantObject> pos2 = new ArrayList<ParticipantObject>();
		ParticipantObject patientObj2 = new ParticipantObject(patient);
		pos2.add(patientObj2);
		ParticipantObject queryObj2 = new ParticipantObject(null, "111069");
		queryObj2.setQuery("QPD|Q22^Find Candidates^HL7nnn|111069|@PID.5.1^SMITH~@PID.5.2^JOHN~@PID.8^M|80|MATCHWARE|1.2||^^^CPR^^^|");
		queryObj2.setDetail("Message id");		

    	// -------  TEST  --------		
//		broker.userLogin();
    	//11181
//		pixMan.start();
    	
    	//11190
//		broker.nodeAuthenticationFailure(ConnectionFactory.getConnectionDescription("ALLSCRIPTS-SECURE"));

    	//11199
		pixMan.logPdqQuery(source, pos2, queryObj2); 

    	//11195  Patient Record
//		ActiveParticipant destination = source;
//    	pixMan.logPixUpdateNotification(destination, po);
    	       
    	//11182
//		pixMan.stop();  
//		broker.userLogout();
		
		// -------  END TEST  --------
		
		logger.writeTestEnd(test);
	}

}
