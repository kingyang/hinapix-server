/*
 * Title:       XMLWriter
 * Description: Class to write an XML document to an output stream. Basically
 *              written to bypass the JAXP dependency of being able to print XML
 *              documents to an output stream.
 * Copyright:   (c) 2000-2002
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ils.utility.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class tries to simulate the implementation of the JAXP's
 * XmlWriteContext Class of the JAXP product package. It basically
 * collects the Node information of an input XML Document and adds
 * it to a StringBuffer. The StringBuffer is later written to an
 * Output Stream.
 *
 * @author mnanchal, CareScience
 * @version 1.2, 20020205
 */
public class XMLWriter {

  private Writer outWriter;
  private StringBuffer sBuffer;
  private OutputStream outStream;
  private Logger log = Logger.getLogger("ILS");
  
  /**
   * Null argument constructor
   */
  public XMLWriter() {}

  /**
   * Overloaded constructor which takes in one argument
   *
   * @param ostream - The OutputStream to which the Output is to be
   *         printed
   */
  public XMLWriter(OutputStream ostream) {
      if (ostream != null) {
          outStream = ostream;
      } else {
        log.warn("Null Object input for OutputStream");
      }
      sBuffer = new StringBuffer();
  }

  /**
   * Method to iterate through a list of nodes and send them to the
   *         processing method
   *
   * @param nlist - The List of nodes to be processed
   */
  private void sendNodes(NodeList nlist) {
      if ((nlist != null) && (nlist.getLength() > 0)) {
          for (int i=0; i < nlist.getLength(); i++) {
              processNode(nlist.item(i));
              if ((nlist.item(i)).hasChildNodes()) {
                  sendNodes(getNodes(nlist.item(i)));
              }
              if (nlist.item(i).getNodeName().equals("#text"));
              else {
                  sBuffer.append("</" + nlist.item(i).getNodeName() + ">");
              }
          }
      }
  }

  /**
   * Method to get the nodes in an input XML document
   *
   * @param doc - The XML document from which to get the nodes
   * @return NodeList - The list of nodes
   */
  private NodeList getNodes(Document doc) {
      NodeList childNodes = null;
      if (doc != null) {
          sBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

          DocumentTypeImpl myDocType = (DocumentTypeImpl) doc.getDoctype();
          if (myDocType != null) {
              sBuffer.append("<!DOCTYPE " + myDocType.getName() + " SYSTEM \"" +
                                    myDocType.getSystemId() + "\">");
          }

          childNodes = doc.getChildNodes();
      }
      return childNodes;
  }

  /**
   * Method to get the child nodes from a parent node
   *
   * @param node - The parent Node from which to get the child
   *         nodes
   * @return NodeList - The list of nodes
   */
  private NodeList getNodes(Node node) {
      NodeList childNodes = null;
      if (node != null) {
          childNodes = node.getChildNodes();
      }

      return childNodes;
  }

  /**
   * Method to process a node
   *
   * @param currentNode - The Node to be processed
   */
  private void processNode(Node currentNode) {
      if (currentNode != null) {
          if (currentNode.getNodeName().equals("#text"));
          else {
              sBuffer.append("<" + currentNode.getNodeName());
              if (currentNode.getAttributes() != null) {
                  NamedNodeMap attrMap = processNodeForAttributes(currentNode);
                  for (int i = 0; i < attrMap.getLength(); i++) {
                      sBuffer.append(" " + attrMap.item(i).getNodeName() + "=\"");
                      sBuffer.append(attrMap.item(i).getNodeValue());
                      sBuffer.append("\"");
                  }
              }
              sBuffer.append(">");
          }
          if (currentNode.getNodeValue() != null) {
              sBuffer.append(currentNode.getNodeValue());
          }
      }
  }

  /**
   * Method to check for the presence of Attributes for a Node in
   *         an XML Document
   *
   * @param currentNode - The Node to check Attributes for
   * @return NamedNodeMap - The list of Attributes
   */
  private NamedNodeMap processNodeForAttributes(Node currentNode) {
      NamedNodeMap attrMap = null;
      if (currentNode != null) {
          attrMap = currentNode.getAttributes();
      }
      return attrMap;
  }

  /**
   * Method to write the XML Document input to an Output Stream
   *
   * @param doc - The XML Document to be written to OutputStream
   * @throws IOException
   */
  public void writeXML(Document doc) throws IOException{
      if (doc != null) {
          sendNodes(getNodes(doc));
          sBuffer.append("\n");
          outStream.write(sBuffer.toString().getBytes());
      }
  }
}
