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
package com.hinacom.pix.ihe.log;

import javax.xml.soap.SOAPMessage;

import org.apache.axiom.om.OMElement;

import ca.uhn.hl7v2.model.Message;

/**
 * Interface that enables IHE Actors to write stuff to the
 * MESA test log, when appropriate.
 * 
 * @author Jim Firby
 * @version 1.0 - Nov 2, 2005
 */
public interface IMesaLogger {

	/**
	 * Writes a text string to the MESA log
	 * 
	 * @param message The text string to display
	 */
	public void writeString(String message);

	/**
	 * Writes a SOAP message to the MESA log
	 * 
	 * @param message The SOAP message to display
	 */
	public void writeSoapMessage(SOAPMessage message);

	/**
	 * Writes out an Axiom OMElement message to the logger.
	 * 
	 * @param message the element to display
	 */
	public void writeAxiomElementMessage(OMElement message);

	/**
	 * Writes an HL7 message to the MESA log
	 * 
	 * @param message the HL7 message to display
	 */
	public void writeHL7Message(Message message);
}
