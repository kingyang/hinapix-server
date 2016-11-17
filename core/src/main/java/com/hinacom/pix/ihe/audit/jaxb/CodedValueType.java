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


package com.hinacom.pix.ihe.audit.jaxb;


/**
 * Java content class for CodedValueType complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/J:/workspace/connect_refactor_ihe/src/com/misyshealthcare/connect/ihe/audit/AuditMessage.xsd line 536)
 * <p>
 * <pre>
 * &lt;complexType name="CodedValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{}CodeSystem"/>
 *       &lt;attribute name="code" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="originalText" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface CodedValueType {


    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String}
     */
    String getDisplayName();

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String}
     */
    void setDisplayName(String value);

    /**
     * Gets the value of the originalText property.
     * 
     * @return
     *     possible object is
     *     {@link String}
     */
    String getOriginalText();

    /**
     * Sets the value of the originalText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String}
     */
    void setOriginalText(String value);

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String}
     */
    String getCode();

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String}
     */
    void setCode(String value);

    /**
     * Gets the value of the codeSystemName property.
     * 
     * @return
     *     possible object is
     *     {@link String}
     */
    String getCodeSystemName();

    /**
     * Sets the value of the codeSystemName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String}
     */
    void setCodeSystemName(String value);

    /**
     * Gets the value of the codeSystem property.
     * 
     * @return
     *     possible object is
     *     {@link String}
     */
    String getCodeSystem();

    /**
     * Sets the value of the codeSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link String}
     */
    void setCodeSystem(String value);

}
