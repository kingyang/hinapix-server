/*
 * Title      : DocumentHeader
 * Description:
 * Copyright  : (c) 1998-2003
 * Company    : CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

import java.util.Date;

/**
 * The <code>DocumentHeader</code> transient object. Each transient object is
 * mirrored by a persistent object in Objectivity.
 *
 * @author CareScience
 * @version 1.3, 20030203
 */
public class DocumentHeader extends TransientObject {

  private Date msgDate;
  private String messageControlID;
  private String sequenceNumber;
  private String continuationPtr;
  private String ackType;
  private String appAckType;
  private String msgSecurity;
  private String receivingApplication_;
  private String receivingFacility_;
  private String sendingApplication_;
  private String sendingFacility_;
  private String eventCode_;
  private String messageType_;
  private String triggerEvent_;
  private String messageStructure_;

  public DocumentHeader() {}

  /**
   *  Sets the receiving application
   *  @param String the receiving application
   */
  public void setReceivingApplication(String receivingApp) {
    receivingApplication_ = receivingApp;
  }

  /**
   * Sets the receiving facility
   * @param String the receiving facility name
   */
  public void setReceivingFacility(String receivingFacility) {
    receivingFacility_ = receivingFacility;
  }

  /**
   * Sets the sending application
   * @param String the sending application
   */
  public void setSendingApplication(String sendingApp) {
    sendingApplication_ = sendingApp;
  }

  /**
   * Sets the event code
   * @param String the event code
   */
  public void setEventCode(String eventCode) {
    eventCode_ = eventCode;
  }

  /**
   * Gets the event code
   * @return String the event code
   */
  public String getEventCode() {
    return eventCode_;
  }

  /**
   * Sets the sending facility
   * @param String the sending facility name
   */
  public void setSendingFacility(String sendingFacility) {
    sendingFacility_ = sendingFacility;
  }

  /**
   * Sets the message date
   * @param aDate the message date
   */
  public void setMessageDate(Date aDate) {
    msgDate = aDate;
  }

  /**
   * Sets the message control id
   * @param String the message control id
   */
  public void setMessageControlID(String ID) {
    messageControlID = ID;
  }

  /**
   * Sets the continuation pointer
   * @param String the continuation pointer
   */
  public void setContinuationPointer(String ptr_Val) {
    continuationPtr = ptr_Val;
  }

  /**
   * Sets the message security
   * @param String the message security
   */
  public void setMessageSecurity(String security) {
    msgSecurity = security;
  }

  /**
   * Sets the accepted acknowledgement type
   * @param String the acknowledgement type
   */
  public void setAcceptAckType(String ack) {
    ackType = ack;
  }

  /**
   * Sets the application acknowledgement type
   * @param String the application acknowledgement type
   */
  public void setApplicationAckType(String app_Ack) {
    appAckType = app_Ack;
  }

  /**
   * Gets the message security
   * @return String the message security values
   */
  public String getMessageSecurity() {
    return msgSecurity;
  }

  /**
   * Gets the application acknowledgement type
   * @return String the application acknowledgement type
   */
  public String getApplicationAckType() {
    return appAckType;
  }

  /**
   * Gets the accepted acknowledgement type
   * @return String the accepted acknowledgement type
   */
  public String getAcceptAckType() {
    return ackType;
  }

  /**
   * Gets the continuation pointer type
   * @return String the continuation pointer type
   */
  public String getContinuationPointer() {
    return continuationPtr;
  }

  /**
   * Gets the message control id
   * @return String the message control id
   */
  public String getMessageControlID() {
    return messageControlID;
  }

  /**
   * Sets the sequence number for the message
   * @return String the sequence number for the message
   */
  public void setSequenceNumber(String seq_Num) {
    sequenceNumber = seq_Num;
  }

  /**
   * Gets the sequence number for the message
   * @return String the sequence number for the message
   */
  public String getSequenceNumber() {
    return sequenceNumber;
  }

  /**
   * Gets the date the message was generated
   * @return String the date for the message
   */
  public Date getMessageDate() {
    return msgDate;
  }

  /**
   * Gets the message type
   * @return String the message type
   */
  public String getMessageType() {
    return messageType_;
  }

  /**
   * Sets the message type
   * @param String the message type
   */
  public void setMessageType(String type_Data) {
    messageType_ = type_Data;
  }

  /**
   * Gets the event that triggered the message
   * @return String the trigger event
   */
  public String getTriggerEvent() {
    return triggerEvent_;
  }

  /**
   * Sets the trigger event
   * @param String the trigger event
   */
  public void setTriggerEvent(String event_Data) {
    triggerEvent_ = event_Data;
  }

  /**
   * Gets the receiving application name
   * @return String the receiving application
   */
  public String getReceivingApplication() {
    return receivingApplication_;
  }

  /**
   * Gets the receiving facility
   * @return String the receiving facility
   */
  public String getReceivingFacility() {
    return receivingFacility_;
  }

  /**
   * Gets the sending application
   * @return String the sending application
   */
  public String getSendingApplication() {
    return sendingApplication_;
  }

  /**
   * Gets the sending facility
   * @param String the sending facility
   */
  public String getSendingFacility() {
    return sendingFacility_;
  }

  /**
   * Gets the message structure
   * @param String the continuation pointer type
   */
  public String getMessageStructure() {
    return messageStructure_;
  }

  /**
   * Sets the message structure
   * @param String the message structure
   */
  public void setMessageStructure(String message_Struct_data) {
    messageStructure_ = message_Struct_data;
  }

  /**
   * Checks for validity of this <code>DocumentHeader</code>. For now, checks
   * that the message date is a valid (non-null) <code>Date</code>.
   *
   * @throws IllegalArgumentException if anything is bad
   */
  public void isValid() {
    if (msgDate == null) 
      throw new IllegalArgumentException("DocumentHeader msgDate may not be null");
  }

    /**
     * Method to check for equality with another Object
     *
     * @param obj - The Object to be compared to
     * @return boolean - true if equal, false otherwise
     */
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (obj != null) {
            String oid1 = this.getOid();
            String oid2 = ((DocumentHeader)obj).getOid();
            if (oid1 != null && oid2 != null) {
                isEqual = oid1.equals(oid2);
	    } else if (getClass() == obj.getClass()) {
		DocumentHeader header = (DocumentHeader)obj;
		isEqual = true;

		if ((ackType == null)
		     ? (header.ackType != null)
		     : (!ackType.equals(header.ackType))) {
		    isEqual = false;
		} else if ((appAckType == null)
			    ? (header.appAckType != null)
			    : (!appAckType.equals(header.appAckType))) {
		    isEqual = false;
		} else if ((continuationPtr == null)
			    ? (header.continuationPtr != null)
			    : (!continuationPtr.equals(header.continuationPtr))) {
		    isEqual = false;
		} else if ((eventCode_ == null)
			    ? (header.eventCode_ != null)
			    : (!eventCode_.equals(header.eventCode_))) {
		    isEqual = false;
		} else if ((messageControlID == null)
			    ? (header.messageControlID != null)
			    : (!messageControlID.equals(header.messageControlID))) {
		    isEqual = false;
		} else if ((messageStructure_ == null)
			    ? (header.messageStructure_ != null)
			    : (!messageStructure_.equals(header.messageStructure_))) {
		    isEqual = false;
		} else if ((messageType_ == null)
			    ? (header.messageType_ != null)
			    : (!messageType_.equals(header.messageType_))) {
		    isEqual = false;
		} else if ((msgSecurity == null)
			    ? (header.msgSecurity != null)
			    : (!msgSecurity.equals(header.msgSecurity))) {
		    isEqual = false;
		} else if ((receivingApplication_ == null)
			    ? (header.receivingApplication_ != null)
			    : (!receivingApplication_.equals(header.receivingApplication_))) {
		    isEqual = false;
		} else if ((receivingFacility_ == null)
			    ? (header.receivingFacility_ != null)
			    : (!receivingFacility_.equals(header.receivingFacility_))) {
		    isEqual = false;
		} else if ((sendingApplication_ == null)
			    ? (header.sendingApplication_ != null)
			    : (!sendingApplication_.equals(header.sendingApplication_))) {
		    isEqual = false;
		} else if ((sendingFacility_ == null)
			    ? (header.sendingFacility_ != null)
			    : (!sendingFacility_.equals(header.sendingFacility_))) {
		    isEqual = false;
		} else if ((sequenceNumber == null)
			    ? (header.sequenceNumber != null)
			    : (!sequenceNumber.equals(header.sequenceNumber))) {
		    isEqual = false;
		} else if ((triggerEvent_ == null)
			    ? (header.triggerEvent_ != null)
			    : (!triggerEvent_.equals(header.triggerEvent_))) {
		    isEqual = false;
		} else if ((msgDate == null)
		            ? (header.msgDate != null)
			    : (!msgDate.equals(header.msgDate))){
		    isEqual = false;
		}
	    }
        }
	return isEqual;
    }
}
