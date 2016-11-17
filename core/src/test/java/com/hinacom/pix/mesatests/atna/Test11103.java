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

import com.hinacom.pix.data.Patient;
import com.hinacom.pix.data.PatientIdentifier;
import com.hinacom.pix.data.PersonName;
import com.hinacom.pix.ihe.audit.IheAuditTrail;
import com.hinacom.pix.ihe.audit.ParticipantObject;
import com.hinacom.pix.ihe.registry.HL7;
import com.hinacom.pix.mesatests.MesaTestLogger;

import com.misyshealthcare.connect.base.audit.ActiveParticipant;
import com.misyshealthcare.connect.base.audit.AuditCodeMappings.EventActionCode;
import com.misyshealthcare.connect.net.ConnectionFactory;
import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.net.Identifier;

/** Test rig for MESA test 11103.
 * 
 * This tests alternate logging capabilities.
 * 
 * @see ConnectionFactory
 * @author Wenzhi Li
 * @version 1.0 - Dec 30, 2008
 */

public class Test11103 {
	/**
	 * Run MESA Test 11103
	 */
	public static void main(String[] args) {


		String test = "11103";
		
		ConnectionFactory.loadConnectionDescriptionsFromFile("C:\\src2\\hinapix\\core\\src\\test\\resources\\conf\\mesatests\\actors\\AuditRepositoryConnections.xml");
		ArrayList<IConnectionDescription> repositories = new ArrayList<IConnectionDescription>();
		repositories.add(ConnectionFactory.getConnectionDescription("log4j_audittrail"));
		repositories.add(ConnectionFactory.getConnectionDescription("mesa_arr_bsd"));

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

		MesaTestLogger logger = new MesaTestLogger(System.out);
		logger.writeTestBegin(test);
		IheAuditTrail dcat = new IheAuditTrail("PixManager", repositories);
		// dcat.start();
		//PIX Feed Audit Log
		dcat.logPixFeed(source, po, EventActionCode.Create );
		
		//PIX Query Audit Log
		ParticipantObject patientObj = new ParticipantObject();
		patientObj.addId(pid);
		ParticipantObject queryObj = new ParticipantObject(null, "111069");
		queryObj.setQuery("QPD|Q23^Get Corresponding IDs^HL7nnnn|111069|112234^^^METRO HOSPITAL|^^^WESTCLINIC~^^^SOUTH LAB");
		queryObj.setDetail("Message id");		
		dcat.logPixQuery(source, patientObj, queryObj);
		
		//PDQ Query Audit Log
		Collection<ParticipantObject> pos2 = new ArrayList<ParticipantObject>();
		ParticipantObject patientObj2 = new ParticipantObject(patient);
		pos2.add(patientObj2);
		ParticipantObject queryObj2 = new ParticipantObject(null, "111069");
		queryObj2.setQuery("QPD|Q22^Find Candidates^HL7nnn|111069|@PID.5.1^SMITH~@PID.5.2^JOHN~@PID.8^M|80|MATCHWARE|1.2||^^^CPR^^^|");
		queryObj2.setDetail("Message id");		
		dcat.logPdqQuery(source, pos2, queryObj2);

		//PIX Update Notification Log
		ActiveParticipant destination = new ActiveParticipant();
		destination.setUserId("ALLSCRIPTS|EHR_ALLSCRIPTS_CONNECT");
		destination.setAccessPointId("127.0.0.1");
		ParticipantObject patientObject = new ParticipantObject(patient);
		patientObject.setDetail("Message id");
		dcat.logPixUpdateNotification(destination, patientObject);		

		logger.writeTestEnd(test);
	}
}