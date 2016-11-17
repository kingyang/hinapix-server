/*
 * Title:       MessageHeaderInfo
 * Description: The class is used to collect the data in the ADT message header
 * Copyright  : (c) 1998-2001
 *              CareScience,
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A <code>HashMap</code> which stores the Message Header Information of a
 * request and the Document Header Information for Attributes of a Person Object.
 *
 * @author CareScience
 * @version 1.4, 20011025
 */
public class MessageHeaderInfo extends HashMap implements Externalizable {

  public static final String MESSAGE_TYPE = "CM_MSG_TYPE.1";

  public static final String TRIGGER_EVENT = "CM_MSG_TYPE.2";

  public static final String MESSAGE_STRUCTURE = "CM_MSG_TYPE.3";

  private Document headerDoc;
  private Date msgDate_;
  private String msgControlID_;
  private String sequenceNumber_;
  private String continuationPtr_;
  private String ackType_;
  private String appAckType_;
  private String msgSecurity_;
  private String receivingApplication_;
  private String receivingFacility_;
  private String sendingApplication_;
  private String sendingFacility_;
  private String eventCode_;

  /** Default constructor */
  public MessageHeaderInfo() {}

  /**
   * Sets the receiving application
   *
   * @param String the receiving application
   */
  public void setReceivingApplication(String receivingApp) {
    receivingApplication_ = receivingApp;
  }

  /**
   * Sets the receiving facility.
   *
   * @param String the receiving facility name
   */
  public void setReceivingFacility(String receivingFacility) {
    receivingFacility_ = receivingFacility;
  }

  /**
   * Sets the sending application.
   *
   * @param String the sending application
   */
  public void setSendingApplication(String sendingApp) {
    sendingApplication_ = sendingApp;
  }

  /**
   * Sets the event code.
   *
   * @param String the event code
   */
  public void setEventCode(String eventCode) {
    eventCode_ = eventCode;
  }

  /**
   * Returns the event code.
   *
   * @return String the event code
   */
  public String getEventCode() {
    return eventCode_;
  }

  /**
   * Sets the sending facility.
   *
   * @param String the sending facility name
   */
  public void setSendingFacility(String sendingFacility) {
    sendingFacility_ = sendingFacility;
  }

  /**
   * Sets the message date.
   *
   * @param Date the message date
   */
  public void setMessageDate(Date aDate) {
    msgDate_ = aDate;
  }

  /**
   * Sets the message control id.
   *
   * @param String the message control id
   */
  public void setMessageControlID(String ID) {
    msgControlID_ = ID;
  }

  /**
   * Sets the continuation pointer.
   *
   * @param String the continuation pointer
   */
  public void setContinuationPointer(String ptr_Val) {
    continuationPtr_ = ptr_Val;
  }

  /**
   * Sets the message security.
   *
   * @param String the message security
   */
  public void setMessageSecurity(String security) {
    msgSecurity_ = security;
  }

  /**
   * Sets the accepted acknowledgement type.
   *
   * @param String the acknowledgement type
   */
  public void setAcceptAckType(String ack) {
    ackType_ = ack;
  }

  /**
   * Sets the application acknowledgement type.
   *
   * @param String the application acknowledgement type
   */
  public void setApplicationAckType(String app_Ack) {
    appAckType_ = app_Ack;
  }

  /**
   * Returns the message security.
   *
   * @return String the message security values
   */
  public String getMessageSecurity() {
    return msgSecurity_;
  }

  /**
   * Returns the application acknowledgement type.
   *
   * @return String the application acknowledgement type
   */
  public String getApplicationAckType() {
    return appAckType_;
  }

  /**
   * Returns the accepted acknowledgement type.
   *
   * @return String the accepted acknowledgement type
   */
  public String getAcceptAckType() {
    return ackType_;
  }

  /**
   * Returns the continuation pointer type.
   *
   * @return String the continuation pointer type
   */
  public String getContinuationPointer() {
    return continuationPtr_;
  }

  /**
   * Returns the message control id.
   *
   * @return String the message control id
   */
  public String getMessageControlID() {
    return msgControlID_;
  }

  /**
   * Sets the sequence number for the message.
   *
   * @return String the sequence number for the message
   */
  public void setSequenceNumber(String seq_Num) {
    sequenceNumber_ = seq_Num;
  }

  /**
   * Returns the sequence number for the message.
   *
   * @return String the sequence number for the message
   */
  public String getSequenceNumber() {
    return sequenceNumber_;
  }

  /**
   * Returns the date the message was generated.
   *
   * @return String the date for the message
   */
  public Date getMessageDate() {
    return msgDate_;
  }

  /**
   * Returns the message type.
   *
   * @return String the message type
   */
  public String getMessageType() {
    String messageType = null;

    if (containsKey(MESSAGE_TYPE)) {
      messageType = (String)get(MESSAGE_TYPE);
    }

    return messageType;
  }

  /**
   * Sets the message type.
   *
   * @param String the message type
   */
  public void setMessageType(String type_Data) {
    put(MESSAGE_TYPE, type_Data);
  }

  /**
   * Returns the event that triggered the message.
   *
   * @return String the trigger event
   */
  public String getTriggerEvent() {
    String triggerEvent = null;

    if (containsKey(TRIGGER_EVENT)) {
      triggerEvent = (String) get(TRIGGER_EVENT);
    }

    return triggerEvent;
  }

  /**
   * Sets the trigger event.
   *
   * @param String the trigger event
   */
  public void setTriggerEvent(String event_Data) {
    put(TRIGGER_EVENT, event_Data);
  }

  /**
   * Returns the receiving application name.
   *
   * @return String the receiving application
   */
  public String getReceivingApplication() {
    return receivingApplication_;
  }

  /**
   * Returns the receiving facility.
   *
   * @return String the receiving facility
   */
  public String getReceivingFacility() {
    return receivingFacility_;
  }

  /**
   * Returns the sending application.
   *
   * @return String the sending application
   */
  public String getSendingApplication() {
    return sendingApplication_;
  }

  /**
   * Returns the sending facility.
   *
   * @param String the sending facility
   */
  public String getSendingFacility() {
    return sendingFacility_;
  }

  /**
   * Returns the message structure.
   *
   * @param String the continuation pointer type
   */
  public String getMessageStructure() {
    String messageStructure = null;

    if (containsKey(MESSAGE_STRUCTURE)) {
      messageStructure = (String)get(MESSAGE_STRUCTURE);
    }

    return messageStructure;
  }

  /**
   * Sets the message structure.
   *
   * @param String the message structure
   */
  public void setMessageStructure(String message_Struct_data) {
    put(MESSAGE_STRUCTURE, message_Struct_data);
  }

  /**
   * Sets the data to the stream provided.
   *
   * @param ObjectOutput the stream to write the object to
   * @throws IOException thrown when the data cannot be written to
   * the underlying stream
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(msgDate_);
    out.writeObject(msgControlID_);
    out.writeObject(sequenceNumber_);
    out.writeObject(continuationPtr_);
    out.writeObject(ackType_);
    out.writeObject(appAckType_);
    out.writeObject(msgSecurity_);
    out.writeObject(receivingApplication_);
    out.writeObject(receivingFacility_);
    out.writeObject(sendingFacility_);
    out.writeObject(sendingApplication_);
  }

  /**
   * Sets the serialized data from the stream provided.
   *
   * @param ObjectInput the stream to read the object from
   * @throws IOException thrown when the data cannot be read from
   * the underlying stream
   * @throws ClassNotFoundException thrown when the class information of
   * data read is not found
   */
  public void readExternal(ObjectInput in) throws IOException,
    ClassNotFoundException {

    msgDate_ = (Date) in.readObject();
    msgControlID_ = (String) in.readObject();
    sequenceNumber_ = (String) in.readObject();
    continuationPtr_ = (String) in.readObject();
    ackType_ = (String) in.readObject();
    appAckType_ = (String) in.readObject();
    msgSecurity_ = (String) in.readObject();
    receivingApplication_ = (String) in.readObject();
    receivingFacility_ = (String) in.readObject();
    sendingFacility_ = (String) in.readObject();
    sendingApplication_ = (String) in.readObject();
  }

  /**
   * Returns the XML Header Document created.
   *
   * @return Document - The XML Header Document created
   */
  public Document getXMLHeader() {
    return headerDoc;
  }

  /** Sets the XML Header Document. */
  public void setXMLHeader() {
    headerDoc = createHeaderDocument();
  }

  /**
   * Creates the XML Document to be stored in the Database.
   *
   * @return headerDoc The XML Header Document Created
   * @throws org.apache.xerces.dom.ParserConfigurationException
   */
  private Document createHeaderDocument() {
    /* create the Document Type */
    DocumentType docType = new DocumentTypeImpl(null, "MSH", null, "MSH.dtd");

    /* create the Document */
    Document header = new DocumentImpl(docType);

    /* create the root element */
    Element msh = header.createElement("MSH");
    header.appendChild(msh);

    /* create and append MSH.3 */
    Element msh3 = header.createElement("MSH.3");
    msh.appendChild(msh3);
    msh3.appendChild(createTextNode(header, "HD.1", sendingApplication_));
    msh3.appendChild(createTextNode(header, "HD.2", null));
    msh3.appendChild(createTextNode(header, "HD.3", null));

    /* create and append MSH.4 */
    Element msh4 = header.createElement("MSH.4");
    msh.appendChild(msh4);
    msh4.appendChild(createTextNode(header, "HD.1", sendingFacility_));
    msh4.appendChild(createTextNode(header, "HD.2", null));
    msh4.appendChild(createTextNode(header, "HD.3", null));

    /* create and append MSH.5 */
    Element msh5 = header.createElement("MSH.5");
    msh.appendChild(msh5);
    msh5.appendChild(createTextNode(header, "HD.1", receivingApplication_));
    msh5.appendChild(createTextNode(header, "HD.2", null));
    msh5.appendChild(createTextNode(header, "HD.3", null));

    /* create and append MSH.6 */
    Element msh6 = header.createElement("MSH.6");
    msh.appendChild(msh6);
    msh6.appendChild(createTextNode(header, "HD.1", receivingFacility_));
    msh6.appendChild(createTextNode(header, "HD.2", null));
    msh6.appendChild(createTextNode(header, "HD.3", null));

    DateFormat df = new SimpleDateFormat("yyyyMMdd");

    /* create and append MSH.7 */
    msh.appendChild(createTextNode(header, "MSH.7", df.format(msgDate_)));

    /* create and append MSH.8 */
    msh.appendChild(createTextNode(header, "MSH.8", msgSecurity_));

    /* create and append MSH.9 */
    Element msh9 = header.createElement("MSH.9");
    msh.appendChild(msh9);
    msh9.appendChild(createTextNode(header, MESSAGE_TYPE, null));
    msh9.appendChild(createTextNode(header, TRIGGER_EVENT, null));

    /* create and append MSH.10 */
    msh.appendChild(createTextNode(header, "MSH.10", msgControlID_));

    /* create and append MSH.13 */
    msh.appendChild(createTextNode(header, "MSH.13", sequenceNumber_));

    /* create and append MSH.14 */
    msh.appendChild(createTextNode(header, "MSH.14", continuationPtr_));

    /* create and append MSH.15 */
    msh.appendChild(createTextNode(header, "MSH.15", ackType_));

    /* create and append MSH.16 */
    msh.appendChild(createTextNode(header, "MSH.16", appAckType_));

    return header;
  }

  /**
   * Creates a Text Node in the XML Document.
   *
   * @param theDoc The Document in which the Node should be created
   * @param nodeName The name of the Node
   * @param nodeValue The node value
   * @return Node
   */
  private Node createTextNode(Document theDoc, String nodeName, String nodeValue) {
    Element docEl = theDoc.createElement(nodeName);
    Node nValue = theDoc.createTextNode(nodeValue);
    docEl.appendChild(nValue);
    return  docEl;
  }
}
