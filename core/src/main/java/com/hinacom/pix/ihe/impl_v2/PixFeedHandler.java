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
import java.util.List;

import org.apache.log4j.Logger;
import com.hinacom.pix.data.MessageHeader;
import com.hinacom.pix.data.Patient;
import com.hinacom.pix.data.PatientIdentifier;
import com.hinacom.pix.ihe.IPixManagerAdapter;
import com.hinacom.pix.ihe.IPixUpdateNotificationRequest;
import com.hinacom.pix.ihe.PixManagerException;
import com.hinacom.pix.ihe.PixUpdateNotifier;
import com.hinacom.pix.ihe.audit.ParticipantObject;
import com.hinacom.pix.ihe.configuration.IheConfigurationException;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7Channel;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7Header;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7v231;
import com.hinacom.pix.ihe.impl_v2.hl7.HL7v231ToBaseConvertor;
import com.hinacom.pix.ihe.log.MessageStore;
import com.hinacom.pix.util.AssigningAuthorityUtil;
import com.hinacom.pix.util.ExceptionUtil;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v231.datatype.CE;
import ca.uhn.hl7v2.model.v231.datatype.CX;
import ca.uhn.hl7v2.model.v231.datatype.ELD;
import ca.uhn.hl7v2.model.v231.datatype.ST;
import ca.uhn.hl7v2.model.v231.group.ADT_A39_PIDPD1MRGPV1;
import ca.uhn.hl7v2.model.v231.message.ACK;
import ca.uhn.hl7v2.model.v231.message.ADT_A01;
import ca.uhn.hl7v2.model.v231.message.ADT_A04;
import ca.uhn.hl7v2.model.v231.message.ADT_A05;
import ca.uhn.hl7v2.model.v231.message.ADT_A08;
import ca.uhn.hl7v2.model.v231.message.ADT_A39;
import ca.uhn.hl7v2.model.v231.segment.MRG;
import ca.uhn.hl7v2.model.v231.segment.PID;

import com.misyshealthcare.connect.base.audit.ActiveParticipant;
import com.misyshealthcare.connect.base.audit.AuditCodeMappings;
import com.misyshealthcare.connect.base.audit.AuditCodeMappings.EventActionCode;
import com.misyshealthcare.connect.net.IConnectionDescription;
import com.misyshealthcare.connect.net.Identifier;

/**
 * This class processes PIX Feed message in HL7 v2.3.1 format. It 
 * handles the PIX Feed transaction (including also PIX Update 
 * and PIX Merge transactions) of the PIX profile.  
 * The supported message type includes ADT^A01, ADT^A04, ADT^A05, 
 * ADT^A08 and ADT^A40.
 *
 * @author Wenzhi Li 
 * @version 1.0 Oct 22, 2008
 * 
 * @see com.hinacom.pix.ihe.impl_v2.PixQueryHandler
 *
 */
public class PixFeedHandler extends BaseHandler implements Application {

    private static Logger log = Logger.getLogger(PixFeedHandler.class);
	private PixManager actor = null;
	private IPixManagerAdapter pixAdapter = null;
	/** Keep an instance of v25 handler for message redirection */
	private PixFeedHandlerV25 handlerV25 = null;
	
	/**
	 * Constructor
	 * 
	 * @param actor the {@link PixManager} actor
	 */
	public PixFeedHandler(PixManager actor) {
		super(actor.getConnection());
		this.actor = actor;
		this.pixAdapter = actor.getPixManagerAdapter();
		this.handlerV25 = new PixFeedHandlerV25(actor);
		assert this.connection != null;
		assert this.pixAdapter != null;
	}

    /**
     * Whether a incoming message can be processed by this handler.
     * 
     * @return <code>true</code> if the incoming message can be processed;
     * otherwise <code>false</code>.
     */
	public boolean canProcess(Message theIn) {
		if (theIn instanceof ADT_A01 || theIn instanceof ADT_A04 ||
		    theIn instanceof ADT_A05 || theIn instanceof ADT_A08 ||
		    theIn instanceof ADT_A39 ||
		    theIn instanceof ca.uhn.hl7v2.model.v25.message.ADT_A01 ||
		    theIn instanceof ca.uhn.hl7v2.model.v25.message.ADT_A05)
			return true;
		else
			return false;
	}
	 
    /**
     * Processes the incoming PIX Feed Message. Valid messages 
     * are ADT^A01, ADT^A04, ADT^A05, ADT^A08 and ADT^A40.
     * 
     * @param msgIn the incoming message
     */
	public Message processMessage(Message msgIn) throws ApplicationException,
			HL7Exception {		
		Message retMessage = null;
		MessageStore store = actor.initMessageStore(msgIn, true);
		//String encodedMessage = HapiUtil.encodeMessage(msgIn);
		//log.info("Received message:\n" + encodedMessage + "\n\n");
		try {
			HL7Header hl7Header = new HL7Header(msgIn);			

			//Populate MessageStore to persist the message
			hl7Header.populateMessageStore(store);

			if (msgIn instanceof ADT_A01 || //Admission of in-patient into a facility
				msgIn instanceof ADT_A04 || //Registration of an outpatient for a visit of the facility
				msgIn instanceof ADT_A05) { //Pre-admission of an in-patient 
				retMessage = processCreate(msgIn);
			} else if (msgIn instanceof ADT_A08) { //Update patient information   
				retMessage = processUpdate(msgIn);
			} else if (msgIn instanceof ADT_A39) { //Merge Patients
				retMessage = processMerge(msgIn);
			} else if (msgIn instanceof ca.uhn.hl7v2.model.v25.message.ADT_A01 || //Admission of in-patient into a facility
					msgIn instanceof ca.uhn.hl7v2.model.v25.message.ADT_A05) { //Pre-admission of an in-patient 
					retMessage = handlerV25.processCreate(msgIn);
			} else {
				String errorMsg = "Unexpected request to PIX Manager server. " 
					+ "Valid message types are ADT^A01, ADT^A04, ADT^A05, ADT^A08 and ADT^A40";

				throw new ApplicationException(errorMsg);
			}
		} catch (ApplicationException e) {
			if (store !=null) { 
				store.setErrorMessage( e.getMessage() );
			}
			throw new ApplicationException(ExceptionUtil.strip(e.getMessage()), e);		
		} catch (HL7Exception e) {
			if (store !=null) {
				store.setErrorMessage( e.getMessage() );
			}			
			throw new HL7Exception(ExceptionUtil.strip(e.getMessage()), e);
		} finally {
			//Persist the message
			if (store !=null) { 
				actor.saveMessageStore(retMessage, false, store);			
			}						
		}

		return retMessage;
	}

	/**
	 * Processes PIX Feed Create Patient message in HL72.3.1.
	 * 
	 * @param msgIn the PIX Feed request message
	 * @return a response message for PIX Feed
	 * @throws ApplicationException If Application has trouble
	 * @throws HL7Exception if something is wrong with HL7 message 
	 */
	private Message processCreate(Message msgIn) 
	throws ApplicationException, HL7Exception {

		assert msgIn instanceof ADT_A01 || 
			   msgIn instanceof ADT_A04 || 
			   msgIn instanceof ADT_A05;

		HL7Header hl7Header = new HL7Header(msgIn);
		
		//If it is for A08, we redirect it to processUpdate.
		if(hl7Header.getTriggerEvent().equals("A08")) {
			return processUpdate(msgIn);
		}
			
		//Create Acknowledgment and its Header
		ACK reply = initAcknowledgment(hl7Header);

		//Validate incoming message first
		PID pid = (PID)msgIn.get("PID");
		PatientIdentifier patientId = getPatientIdentifiers(pid);				
		boolean isValidMessage = validateMessage(reply, hl7Header, patientId, null, true);
		if (!isValidMessage) return reply;
		
		//Invoke eMPI function
		MessageHeader header = hl7Header.toMessageHeader();
		Patient patient = getPatient(msgIn);
		try {
			List<PatientIdentifier> matching = pixAdapter.createPatient(patient, header);
			
			//Send PIX Update Notification
			if (matching != null && matching.size() > 0) {
				IPixUpdateNotificationRequest request = 
					new PixUpdateNotificationRequest(actor, matching);
				PixUpdateNotifier.getInstance().accept(request);
			}			
		}catch (PixManagerException e) {
			throw new ApplicationException(e);
		} 
		
		//Forward this PIX Feed message to the XDS Registry
		forwardToXdsRegistry(msgIn, patientId);
		
		HL7v231.populateMSA(reply.getMSA(), "AA", hl7Header.getMessageControlId());

		//Finally, Audit Log PIX Feed Success 
	    auditLog(hl7Header, patient, AuditCodeMappings.EventActionCode.Create);

	    return reply; 
	}
	
	
	/**
	 * Processes PIX Feed Update Patient message.
	 * 
	 * @param msgIn the PIX Feed request message
	 * @return a response message for PIX Feed
	 * @throws ApplicationException If Application has trouble
	 * @throws HL7Exception if something is wrong with HL7 message 
	 */
	private Message processUpdate(Message msgIn) throws ApplicationException,
			HL7Exception {
		assert msgIn instanceof ADT_A01 ||
			   msgIn instanceof ADT_A08 ;
		
		HL7Header hl7Header = new HL7Header(msgIn);
		
		//Create Acknowledgment and its Header
		ACK reply = initAcknowledgment(hl7Header);

		//Validate incoming message first
		PID pid = (PID)msgIn.get("PID");
		PatientIdentifier patientId = getPatientIdentifiers(pid);				
		boolean isValidMessage = validateMessage(reply, hl7Header, patientId, null, false);
		if (!isValidMessage) return reply;
		
		//Invoke eMPI function
		MessageHeader header = hl7Header.toMessageHeader();
		Patient patient = getPatient(msgIn);
		try {
			//Update Patient
			List<List<PatientIdentifier>> matchingList = pixAdapter.updatePatient(patient, header);			
			
			//PIX Update Notification to PIX consumers
			if (matchingList != null) {
				for (List<PatientIdentifier> matching : matchingList) {
					IPixUpdateNotificationRequest matchingRequest = 
						new PixUpdateNotificationRequest(actor, matching);
					PixUpdateNotifier.getInstance().accept(matchingRequest);					
				}
			}
		} catch (PixManagerException e) {
			throw new ApplicationException(e);
		}
    	
		HL7v231.populateMSA(reply.getMSA(), "AA", hl7Header.getMessageControlId());
    	
		//Finally, Audit Log PIX Feed Success 
	    auditLog(hl7Header, patient, AuditCodeMappings.EventActionCode.Update);

    	return reply;
	}

	/**
	 * Processes PIX Feed Merge Patient message.
	 * 
	 * @param msgIn the PIX Feed request message
	 * @return a response message for PIX Feed
	 * @throws ApplicationException If Application has trouble
	 * @throws HL7Exception if something is wrong with HL7 message 
	 */
	private Message processMerge(Message msgIn) 
	throws ApplicationException, HL7Exception {

		assert msgIn instanceof ADT_A39;
		
		HL7Header hl7Header = new HL7Header(msgIn);

		//Create Acknowledgment and its Header
		ACK reply = initAcknowledgment(hl7Header);

		//Validate incoming message first
		ADT_A39_PIDPD1MRGPV1 requestId = ((ADT_A39)msgIn).getPIDPD1MRGPV1();
		PatientIdentifier patientId = getPatientIdentifiers(requestId.getPID());
		PatientIdentifier mrgPatientId = getMrgPatientIdentifiers(requestId.getMRG());
		boolean isValidMessage = validateMessage(reply, hl7Header, patientId, mrgPatientId, false);
		if (!isValidMessage) return reply;

		//Invoke eMPI function
		MessageHeader header = hl7Header.toMessageHeader();
		Patient patient = getPatient(msgIn);
		Patient mrgPatient = getMrgPatient(msgIn);
		try {
			//Merge Patients
			List<List<PatientIdentifier>> matchingList = pixAdapter.mergePatients(patient, mrgPatient, header);

			//PIX Update Notification to PIX consumers
			if (matchingList != null) {
				for (List<PatientIdentifier> matching : matchingList) {
					IPixUpdateNotificationRequest matchingRequest = 
						new PixUpdateNotificationRequest(actor, matching);
					PixUpdateNotifier.getInstance().accept(matchingRequest);					
				}
			}
		}catch (PixManagerException e) {
			throw new ApplicationException(e);
		}
		
		//Forward this PIX Feed (Merge) message to the XDS Registry
		forwardToXdsRegistry(msgIn, patientId);

		HL7v231.populateMSA(reply.getMSA(), "AA", hl7Header.getMessageControlId());

		//Finally, Audit Log PIX Feed Success 
	    auditLog(hl7Header, patient, AuditCodeMappings.EventActionCode.Update);
	    auditLog(hl7Header, mrgPatient, AuditCodeMappings.EventActionCode.Delete);

		return reply;
	}

	/**
	 * Audit Logging of PIX Feed message.
	 * 
	 * @param hl7Header the header message from the source application
	 * @param patient the patient to create, update or merged
	 * @param eventActionCode the {@link EventActionCode}
	 */
	private void auditLog(HL7Header hl7Header, Patient patient, AuditCodeMappings.EventActionCode eventActionCode) {
		if (actor.getAuditTrail() == null)
			return;
		
		String userId = hl7Header.getSendingFacility().getNamespaceId() + "|" +
						hl7Header.getSendingApplication().getNamespaceId();
		String messageId = hl7Header.getMessageControlId();
		//TODO: Get the ip address of the source application
		String sourceIp = "127.0.0.1";

		ActiveParticipant source = new ActiveParticipant(userId, messageId, sourceIp);
		
		ParticipantObject patientObj = new ParticipantObject(patient);
		patientObj.setDetail(hl7Header.getMessageControlId());

		actor.getAuditTrail().logPixFeed(source, patientObj, eventActionCode);		
	}
	
	/**
	 * Initiates an acknowledgment instance for the incoming message.
	 * 
	 * @param hl7Header the message header of the incoming message
	 * @return an {@link ACK} instance
	 * @throws HL7Exception if something is wrong with HL7 message 
	 * @throws ApplicationException If Application has trouble
	 */
	private ACK initAcknowledgment(HL7Header hl7Header) throws HL7Exception, ApplicationException {
		//Send Response
		ACK reply = new ACK();
		
		//For the response message, the ReceivingApplication and ReceivingFacility 
		//will become the sendingApplication and sendingFacility;
		//Also the sendingApplication and sendingFacility will become the 
		//receivingApplication and receivingFacility.
		Identifier serverApplication = getServerApplication();
		Identifier serverFacility = getServerFacility();
		Identifier sendingApplication = hl7Header.getSendingApplication();
		Identifier sendingFacility = hl7Header.getSendingFacility();
		try {
			String event = hl7Header.getTriggerEvent();
			HL7v231.populateMSH(reply.getMSH(), "ACK", event, getMessageControlId(), 
				serverApplication, serverFacility, sendingApplication, sendingFacility);
		} catch (IheConfigurationException e) {
			throw new ApplicationException("Error populate message header", e);
		}
		
		return reply;
	}
		
	/**
	 * Validates a patient identifier domain, namely, assigning authority.
	 * 
	 * @param reply the reply message to be populated if the validation fails
	 * @param patientId the patient id
	 * @param incomingMessageId the incoming message id
	 * @return <code>true</code> if the patient domain is validated successfully;
	 *         otherwise <code>false</code>.
	 * @throws HL7Exception if something is wrong with HL7 message 
	 */
	private boolean validateDomain(ACK reply, PatientIdentifier patientId, String incomingMessageId) 
	throws HL7Exception {
		Identifier domain = patientId.getAssigningAuthority();
		boolean domainOk = AssigningAuthorityUtil.validateDomain(
				domain, connection);
		if (!domainOk) {
			HL7v231.populateMSA(reply.getMSA(), "AE", incomingMessageId);
			//segmentId=PID, sequence=1, fieldPosition=3, fieldRepetition=1,componentNubmer=4
			HL7v231.populateERR(reply.getERR(), "PID", "1", "3", "1", "4",
					"204", "Unknown Key Identifier");
			return false;
		}
		return true;
	}
	
	/**
     * Validates the receiving facility and receiving application of an incoming message.
	 * 
     * @param reply the reply message to be populated if any validation is failed
	 * @param receivingApplication the receiving application of the incoming message
	 * @param receivingFacility the receiving facility of the incoming message
	 * @param expectedApplication the expected receiving application
	 * @param expectedFacility the expected receiving facility
	 * @param incomingMessageId the incoming message
	 * @return <code>true</code> if validation is passed;
	 *         otherwise <code>false</code>.
	 * @throws HL7Exception if something is wrong with HL7 message 
	 * @throws ApplicationException if something is wrong with the application
	 */
	private boolean validateReceivingFacilityApplication(ACK reply, Identifier receivingApplication,
			Identifier receivingFacility, Identifier expectedApplication, Identifier expectedFacility,
			String incomingMessageId) 
		    throws HL7Exception, ApplicationException
	{
		//In case of tests, don't validate receiving application and facility,
		//It is not easy to switch to different receiving applications and facilities
		boolean  isTest = Boolean.parseBoolean(connection.getProperty("test"));
		if (isTest) return true;
	
		//We first need to validate ReceivingApplication and ReceivingFacility.
		//Currently we are not validating SendingApplication and SendingFacility
		if (!receivingApplication.equals(expectedApplication)) {
			HL7v231.populateMSA(reply.getMSA(), "AE", incomingMessageId);
			//segmentId=MSH, sequence=1, fieldPosition=5, fieldRepetition=1, componentNubmer=1
			HL7v231.populateERR(reply.getERR(), "MSH", "1", "5", "1", "1",
					null, "Unknown Receiving Application");
			return false;
		}
		if (!receivingFacility.equals(expectedFacility)) {
			HL7v231.populateMSA(reply.getMSA(), "AE", incomingMessageId);
			//segmentId=MSH, sequence=1, fieldPosition=6, fieldRepetition=1, componentNubmer=1
			HL7v231.populateERR(reply.getERR(), "MSH", "1", "6", "1", "1",
					null, "Unknown Receiving Facility");
			return false;
		}
		
		return true;
	}

	/**
	 * Validates the incoming Message in this order:
	 * 
	 * <ul>
	 * <li> Validate Receiving Facility and Receiving Application</li>
	 * <li> Validate Domain </li>
	 * <li> Validate patient Id <li>		 
	 * <li> Validate merge patient Id if applicable<li> 
	 * </ul>
	 * 
     * @param reply the reply message to be populated if any validation is failed
	 * @param hl7Header the message header of the incoming message
	 * @param patientId the id of the patient to be validated
	 * @param mrgPatientId the id of the patient to be merged
	 * @param isPixCreate Whether this validation is for PIX patient creation
	 * @return <code>true</code> if the message is correct; <code>false</code>otherwise.
	 * @throws HL7Exception if something is wrong with HL7 message 
	 * @throws ApplicationException if something is wrong with the application
	 */
	private boolean validateMessage(ACK reply, HL7Header hl7Header, PatientIdentifier patientId, PatientIdentifier mrgPatientId, boolean isPixCreate) 
	throws HL7Exception, ApplicationException {
		Identifier serverApplication = getServerApplication();
		Identifier serverFacility = getServerFacility();
		Identifier receivingApplication = hl7Header.getReceivingApplication();
		Identifier receivingFacility = hl7Header.getReceivingFacility();
		String incomingMessageId = hl7Header.getMessageControlId();
		//1. validate receiving facility and receiving application
		boolean isValidFacilityApplication = validateReceivingFacilityApplication(reply, 
				receivingApplication, receivingFacility, 
				serverApplication, serverFacility, incomingMessageId);
		if (!isValidFacilityApplication) return false;		
		
		//2.validate the domain
		boolean isValidDomain = validateDomain(reply, patientId, incomingMessageId);
		if (!isValidDomain) return false;
		
		//3. validate ID itself 
		if (!isPixCreate) { 
			//Do not valid patient id for PIX patient creation
			boolean isValidPid = validatePatientId(reply, patientId, hl7Header.toMessageHeader(), false, incomingMessageId);
			if (!isValidPid) return false;
		}
		
		//4. validate mrgPatientId
		if (mrgPatientId != null) {
			boolean isValidMrgPid = validatePatientId(reply, mrgPatientId, hl7Header.toMessageHeader(), true, incomingMessageId);
			if (!isValidMrgPid) return false;
		}
		
		//Finally, it must be true when it reaches here
		return true;
	}

	/**
	 * Checks the given whether the given patient id is a valid patient id.
	 * 
     * @param reply the reply message to be populated if any validation is failed
	 * @param patientId the patient id to be checked
	 * @param header the incoming message header 
	 * @param isMrgPatientId whether the patient id to be checked is a merge patient id.
	 * @param incomingMessageId the incoming message id.
	 * @return <code>true</code> if the patientId is valid; otherwise <code>false</code>.
	 * @throws HL7Exception if something is wrong with HL7 message 
	 * @throws ApplicationException if something is wrong with the application
	 */
	private boolean validatePatientId(ACK reply, PatientIdentifier patientId, 
			MessageHeader header, boolean isMrgPatientId, String incomingMessageId)
	throws HL7Exception, ApplicationException{
		boolean validPatient;
		try {
			validPatient = pixAdapter.isValidPatient(patientId, header);
		} catch (PixManagerException e) {
			throw new ApplicationException(e);
		}
		if (!validPatient) {
			HL7v231.populateMSA(reply.getMSA(), "AE", incomingMessageId);
			if (isMrgPatientId){
				//segmentId=MRG, sequence=1, fieldPosition=1, fieldRepetition=1, componentNubmer=1
				HL7v231.populateERR(reply.getERR(), "MRG", "1", "1", "1", "1",
						"204", "Unknown Key Identifier");
			} else {
				//segmentId=PID, sequence=1, fieldPosition=3, fieldRepetition=1, componentNubmer=1
				HL7v231.populateERR(reply.getERR(), "PID", "1", "3", "1", "1",
						"204", "Unknown Key Identifier");
			}
		}
		return validPatient;
	}
	
	/**
	 * Converts a PIX Feed Patient message to a {@link Patient} object.
	 * 
	 * @param msgIn the incoming PIX Feed message
	 * @return a {@link Patient} object
	 * @throws ApplicationException if something is wrong with the application
	 */
	private Patient getPatient(Message msgIn) throws ApplicationException,HL7Exception {
		HL7v231ToBaseConvertor convertor = null;
		if (msgIn.getVersion().equals("2.3.1")) {
			convertor = new HL7v231ToBaseConvertor(msgIn, connection);
		} else {
			throw new ApplicationException("Unexpected HL7 version");
		}
		Patient patientDesc = new Patient();
		patientDesc.setPatientIds(convertor.getPatientIds());
		patientDesc.setPatientName(convertor.getPatientName());
		patientDesc.setMonthersMaidenName(convertor.getMotherMaidenName());
		patientDesc.setBirthDateTime(convertor.getBirthDate());
		patientDesc.setAdministrativeSex(convertor.getSexType());
		patientDesc.setPatientAlias(convertor.getPatientAliasName());
		patientDesc.setRace(convertor.getRace());
		patientDesc.setPrimaryLanguage(convertor.getPrimaryLanguage());
		patientDesc.setMaritalStatus(convertor.getMartialStatus());
		patientDesc.setReligion(convertor.getReligion());
		patientDesc.setPatientAccountNumber(convertor.getpatientAccountNumber());
		patientDesc.setSsn(convertor.getSsn());
		patientDesc.setDriversLicense(convertor.getDriversLicense());
		patientDesc.setMonthersId(convertor.getMonthersId());
		patientDesc.setEthnicGroup(convertor.getEthnicGroup());
		patientDesc.setBirthPlace(convertor.getBirthPlace());
		patientDesc.setBirthOrder(convertor.getBirthOrder());
		patientDesc.setCitizenship(convertor.getCitizenShip());
		patientDesc.setDeathDate(convertor.getDeathDate());
		patientDesc.setDeathIndicator(convertor.getDeathIndicator());
		patientDesc.setPhoneNumbers(convertor.getPhoneList());
		patientDesc.setAddresses(convertor.getAddressList());
		patientDesc.setVisits(convertor.getVisitList());
		patientDesc.setAccountNumbers(convertor.getAccountNumber());
		return patientDesc;
	}
	
	

	/**
	 * Extracts the merge patient out of a PIX Merge Patient message.
	 * 
	 * @param msgIn the incoming PIX Merge message
	 * @return a {@link Patient} object that represents the merge patient
	 * @throws ApplicationException if something is wrong with the application
	 */
	private Patient getMrgPatient(Message msgIn) throws ApplicationException, HL7Exception {
		HL7v231ToBaseConvertor convertor = null;		
		convertor = new HL7v231ToBaseConvertor(msgIn, connection);
		Patient patientDesc = new Patient();
		patientDesc.setPatientIds(convertor.getMrgPatientIds());
		patientDesc.setPatientName(convertor.getMrgPatientName());
		patientDesc.setPatientAccountNumber(convertor
				.getMrgpatientAccountNumber());
		patientDesc.setVisits(convertor.getMrgVisitList());
		return patientDesc;
	}

	/**
	 * Gets the patient identifier from a Patient PID segment.
	 * 
	 * @param pid the PID segment
	 * @return a {@link PatientIdentifier}
	 */
	private PatientIdentifier getPatientIdentifiers(PID pid) {
		PatientIdentifier identifier = new PatientIdentifier();
		CX[] cxs = pid.getPatientIdentifierList();
		for (CX cx : cxs) {
			Identifier assignAuth = new Identifier(cx.getAssigningAuthority()
					.getNamespaceID().getValue(), cx.getAssigningAuthority()
					.getUniversalID().getValue(), cx.getAssigningAuthority()
					.getUniversalIDType().getValue());
			Identifier assignFac = new Identifier(cx.getAssigningFacility()
					.getNamespaceID().getValue(), cx.getAssigningFacility()
					.getUniversalID().getValue(), cx.getAssigningFacility()
					.getUniversalIDType().getValue());
			identifier.setAssigningAuthority(AssigningAuthorityUtil.reconcileIdentifier(assignAuth, connection));
			identifier.setAssigningFacility(assignFac);
			identifier.setId(cx.getID().getValue());
			identifier.setIdentifierTypeCode(cx.getIdentifierTypeCode()
					.getValue());
		}
		return identifier;
	}

	/**
	 * Gets the merge patient identifier out of a MRG segment.
	 * 
	 * @param
	 * @return a {@link PatientIdentifier} 
	 */
	private PatientIdentifier getMrgPatientIdentifiers(MRG mrg) {
		PatientIdentifier identifier = new PatientIdentifier();
		CX[] cxs = mrg.getPriorPatientIdentifierList();
		for (CX cx : cxs) {
			Identifier assignAuth = new Identifier(cx.getAssigningAuthority()
					.getNamespaceID().getValue(), cx.getAssigningAuthority()
					.getUniversalID().getValue(), cx.getAssigningAuthority()
					.getUniversalIDType().getValue());
			Identifier assignFac = new Identifier(cx.getAssigningFacility()
					.getNamespaceID().getValue(), cx.getAssigningFacility()
					.getUniversalID().getValue(), cx.getAssigningFacility()
					.getUniversalIDType().getValue());
			identifier.setAssigningAuthority(AssigningAuthorityUtil.reconcileIdentifier(assignAuth, connection));
			identifier.setAssigningFacility(assignFac);
			identifier.setId(cx.getID().getValue());
			identifier.setIdentifierTypeCode(cx.getIdentifierTypeCode()
					.getValue());
		}
		return identifier;
	}
		
	/**
	 * Forwards this PIX Feed message to the XDS Registry in the affinity domain. The XDS registry
	 * in the affinity domain is interested in patient IDs in only the global (master) assigning
	 * authority (domain). So messages for non-global patient IDs are filtered out. Also, be sure
	 * to configure XDS Registry connection in the Actor configuration. See the relevant actor 
	 * configuration documentation.
	 * 
	 * @param msgIn the incoming PIX Feed message to be forwarded to the XDS Registry 
	 * @param patientId the ID of the patient of PIX Feed. For patient creation
	 * and update, it is the main patient ID; for patient merge, it is the surviving
	 * patient ID.
	 */
	private void forwardToXdsRegistry(Message msgIn, PatientIdentifier patientId) {
		//Ignore it if XDS registry is not configured
		IConnectionDescription registryConnection = actor.getXdsRegistryConnection();
		if (registryConnection == null) 
			return ;
		
		//Forward to XDS Registry only those messages associated with global patients
		Identifier globalAuthority = connection.getIdentifier("GLOBAL");			
		if ( !patientId.getAssigningAuthority().equals(globalAuthority) ) 
			return ;
			
		MessageStore store = null;
		try {
			log.info("Forward the PIX Feed to the XDS Registry"+ registryConnection);		            

			store = actor.initMessageStore(msgIn, false);
			HL7Header header = new HL7Header(msgIn);
			header.populateMessageStore(store);

			HL7Channel channel = new HL7Channel(registryConnection);
			Message ack = channel.sendMessage(msgIn);
		    boolean ok = processPixFeedResponse(ack, registryConnection);
		    
		} catch(Exception e) {
			String errorMsg = "Cannot send PIX Feed to XDS Registry: " + registryConnection.getDescription();
			errorMsg += " Error Message:"+e.getMessage();
			log.error(errorMsg);
			if (store != null) {
				store.setErrorMessage( errorMsg );
			}							
		} finally {
			//Persist the message
			if (actor.getStoreLogger() != null && store != null) {  
				actor.getStoreLogger().saveLog(store);			
			}						
		}
	}
	
	
  /**
   * Checks the response to the patient identity feed to ensure that it was
   * a success.
   * 
   * @param response the response from the patient identity feed consumer
   * @param connection the connection from which the response is from
   * @return <code>true</code> if the PIX Feed message was accepted
   */
   private boolean processPixFeedResponse(Message response, IConnectionDescription connection) throws PixManagerException {
		// Make sure the response is the right type of message
		ACK message = null;
		if (response instanceof ACK) {
			message = (ACK) response;
		} else {
			actor.logHL7MessageError(log, message, "Unexpected response");
			throw new PixManagerException("Unexpected response from \"" + connection.getDescription());
		}
		// Check the MSA segment ...
		String status = message.getMSA().getAcknowledgementCode().getValue();
		if ((status == null) || (!status.equalsIgnoreCase("AA") && !status.equalsIgnoreCase("CA"))) {
			// The server has rejected our request, or generated an error
			String mtext = message.getMSA().getTextMessage().getValue();
			String code = message.getMSA().getErrorCondition().getIdentifier().getValue();
			String etext = message.getMSA().getErrorCondition().getText().getValue();
			String error = null;
			if (code != null) error = "(" + code + ") " + HL7v231.getErrorString(code);
			if (mtext != null) error = error + " - " + mtext;
			if (etext != null) error = " [" + etext + "]";
            if (error == null) {
            	message.getERR();
            	ca.uhn.hl7v2.model.v231.segment.ERR err = message.getERR();
              if (err != null) {
                  // Message = err.getMessage().get;
                   try {
                       ELD eld = err.getErrorCodeAndLocation(0);
                       if (eld != null) {
                           CE ce = eld.getCodeIdentifyingError();
                           if (ce != null) {
                               ST errorcode = ce.getIdentifier();
                               if (errorcode != null) {
                                   error = "(" + errorcode.getValue() + ") " + HL7v231.getErrorString(errorcode.getValue());
                               }
                               ST text = ce.getText();
                               if (text != null) error = error + "-" + text.getValue();
                           }
                       }
                   } catch (HL7Exception e) { //do nothing if we cannot get anything from ERR.
                   }
               }
            }
            if (error == null) error ="Unspecified error";
            
			error = "Error response from \"" + connection.getDescription() + "\": " + error; 
			actor.logHL7MessageError(log, message, error);
			throw new PixManagerException(error);
		}
		// Okay, we're good
		return true;
	}
	
}
