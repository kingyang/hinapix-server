/*
 * Title:       EventData
 * Description: Collects data in the event element.
 * Copyright  : (c) 1998-2002
 *              CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Collects data in the event element.
 *
 * @author CareScience
 * @version 1.4, 20020205
 */
public class EventData {

  private Date eventDate;
  private String typeCode;
  private String reasonCode;
  private String eventFacility;

  /**
   * Gets the type code
   *
   * @return String the type code
   */
  public String getTypeCode() {
    return typeCode;
  }

  /**
   * Gets the event date
   *
   * @return Date the date the event was generated
   */
  public Date getEventDate() {
    return eventDate;
  }

  /**
   * Gets the reason code
   *
   * @return String the reason code for the event to be generated
   */
  public String getReasonCode() {
    return reasonCode;
  }

  /**
   * Gets the facility which generated the event
   *
   * @return String the facility which generated the event
   */
  public String getEventFacility() {
    return eventFacility;
  }

  /**
   * Sets the type code
   *
   * @param typeCode the type code for the event type
   */
  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }

  /**
   * Sets the event date
   *
   * @param dt the date the event has to be generated
   */
  public void setEventDate(String dt) throws ParseException
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
    eventDate = df.parse(dt);
  }

  /**
   * Sets the reason code for the event
   *
   * @param reasonCode the reason code
   */
  public void setReasonCode(String reasonCode) {
    this.reasonCode = reasonCode;
  }

  /**
   * Sets the event facility name
   *
   * @param eventFacility the event facility name
   */
  public void setEventFacility(String eventFacility) {
    this.eventFacility = eventFacility;
  }
}
