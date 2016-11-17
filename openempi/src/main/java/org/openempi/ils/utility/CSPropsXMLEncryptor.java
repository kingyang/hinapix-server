package org.openempi.ils.utility;

/**
 * Title:        CSPropsXMLEncryptor
 * Description:  Encrypts the KEYSTORE_PWD and KEY_PWD Attribute valuess in CDEProps.xml
 * Copyright:    Copyright (c) 2002
 * Company:      CareScience, Inc.
 */
import java.io.File;
import java.io.FileOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Encrypts the KEYSTORE_PWD and KEY_PWD Attribute valuess in CDEProps.xml
 *
 * @author apope, CareScience
 * @version 1.2, 20020531
 */
public class CSPropsXMLEncryptor
{
  private Document propsDoc;
  private String propFile;
  
  /*
   * Public constructor.
   */
  public CSPropsXMLEncryptor (String propFile) {
    try {
      this.propFile = propFile;
      propsDoc = (new SAXBuilder()).build(new File(propFile));
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Encrypts the Attributes Values
   *
   * @param rootElmnt - The root Element of CDEprops.xml
   */
  public static Element doEncrypt (Element docRoot, String node)
  {
    Element el = docRoot.getChild(node);
    
    try {
      String enc = "{encrypt}" + DataEncryptor.encrypt(el.getTextTrim());
      el.setText(enc);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return docRoot;
  }
  
  /**
   * Write out the contents of the JDOM Document object to XML in the given
   * path & filename
   */
  public void writeFile(Document inDocument)
    throws Exception
  {
    FileOutputStream fileOut = new FileOutputStream(propFile);
    XMLOutputter outputter = new XMLOutputter();
    outputter.output(inDocument, fileOut);
    fileOut.flush();
    fileOut.close();
  }
  
}
