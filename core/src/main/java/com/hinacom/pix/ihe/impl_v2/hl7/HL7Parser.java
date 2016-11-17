package com.hinacom.pix.ihe.impl_v2.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Created by fyu on 2016/10/27.
 */
public class HL7Parser extends PipeParser {
    @Override
    public Message doParse(String message, String version) throws HL7Exception, EncodingNotSupportedException {
        String newMessage = this.formatMessage(message);

        // only support 2.3.1 and 2.5
        if(version.startsWith("2.3"))
        {
            version="2.3.1";
        }

        if(version.startsWith("2.5"))
        {
            version="2.5";
        }

        Message messageEntity = super.doParse(newMessage, version);
        return messageEntity;
    }

    /*
    * re-format \n and \r\n
    * */
    private String formatMessage(String message) {
        String newMessage = message.replace("\r\n", "\r").replace("\n","\r");
        return newMessage;
    }
}
