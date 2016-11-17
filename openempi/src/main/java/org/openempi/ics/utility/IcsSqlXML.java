/*
 * Title:       IcsSqlXML
 * Description: Reads the xml file used for generating ALL SQL to be executed by
 *              <code>DatabaseServicesJdbc</code>, and contains all supporting
 *              type elements.
 * Copyright:   (c) 2002-2003
 * Company:     CareScience, Inc.
 *              3600 Market Street
 *              7th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.utility;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * The class will read the xml file used for generating ALL SQL to be executed by
 * <code>DatabaseServicesJdbc</code>, and contains all supporting type elements.
 *
 * @author CareScience
 * @version 1.3, 20030127
 */
public class IcsSqlXML {

    /**
     * Start of each QUERY-ATTRIBUTE-TYPES tag in the sql xml file. Must be directly related to
     * <code>com.carescience.db.AttributeType</code>.
     * <b>
     * Example use for sql xml file is: ATTR-1, which refers to AttributeType.LAST_NAME.
     */
    public static final String ATTR_TAG = "ATTR-";

    /**
     * COLTYPE tag in sql xml file for STRING.
     */
    public static final String ATTR_COLTYPE_STRING = "String";

    /**
     * COLTYPE tag in sql xml file for STRING with SQL LIKE
     */
    public static final String ATTR_COLTYPE_LIKE_STRING = "LikeString";

    /**
     * COLTYPE tag in sql xml file for USTRING
     * (Upcase STRING, effectively case-insensitive).
     */
    public static final String ATTR_COLTYPE_USTRING = "UString";

    /**
     * COLTYPE tag in sql xml file for USTRING with SQL LIKE
     * (Upcase STRING, effectively case-insensitive).
     */
    public static final String ATTR_COLTYPE_LIKE_USTRING = "LikeUString";

    /**
     * COLTYPE tag in sql xml file for NUMBER.
     */
    public static final String ATTR_COLTYPE_NUMBER = "Number";

    /**
     * COLTYPE tag in sql xml file for STRING.
     */
    public static final String ATTR_COLTYPE_DATE = "Date";

    /**
     * Location of IcsSql.xml file defining all SQL and Database Elements, obtained from
     * <code>com.carescience.utility.CSProperties</code> with same name.
     */
    public static final String ICS_SQL_FILE = ICSProperties.getProperty("ICS_SQL_FILE", "IcsSql.xml");

    // singleton
    private static final IcsSqlXML instance = new IcsSqlXML();

    private Logger log = Logger.getLogger("ICS");

    public Properties props;
    public Element rootNode;

    /**
     * Private constructor supresses default public constructor
     */
    private IcsSqlXML() {

        String filePath;
        if (ICS_SQL_FILE == null)
            filePath = "IcsSql.xml";
        else
            filePath = ICS_SQL_FILE;
        try {
            // file = Utils.configDir(file);
            filePath = IcsSqlXML.class.getResource("/IcsSql.xml").getPath();


            props = new Properties();

            File file = new File(filePath);
            SAXBuilder saxBuilder = new SAXBuilder();
            Document docNode = saxBuilder.build(file);

            rootNode = docNode.getRootElement();
            buildProps(rootNode);
            log.debug("Loaded " + file);
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    public void buildProps(Element e) throws Exception {
        if (e != null && e.getChildren() != null && e.getChildren().size() != 0) {
            Iterator listOfChildren = e.getChildren().iterator();

            while (listOfChildren.hasNext()) {
                Element elmnt = (Element) listOfChildren.next();

                String eText = elmnt.getTextTrim();

                if (eText != null && eText.length() != 0) {
                    String eName = elmnt.getName();
                    Attribute attrID = elmnt.getAttribute("ID_SUFFIX");

                    if (attrID != null)
                        props.setProperty(eName + "_" + attrID.getValue().trim(), eText);
                    else
                        props.setProperty(eName, eText);
                }

                Iterator listOfAttr = elmnt.getAttributes().iterator();

                while (listOfAttr.hasNext()) {
                    Attribute attr = (Attribute) listOfAttr.next();

                    if (!attr.getName().equalsIgnoreCase("ID_SUFFIX"))
                        props.setProperty(attr.getName(), attr.getValue());
                }

                buildProps(elmnt);
            }
        }
    }

    public Element searchNode(Element e, String eName) {
        Element element = null;

        if (e.getName().equalsIgnoreCase(eName)) return e;

        if (e.getChildren() != null && e.getChildren().size() != 0) {
            Iterator listOfChildren = e.getChildren().iterator();

            while (listOfChildren.hasNext()) {
                Element elmnt = this.searchNode((Element) listOfChildren.next(), eName);

                if (elmnt != null) return elmnt;
            }
        }

        return null;
    }

    public static final IcsSqlXML getInstance() {
        return instance;
    }

    public static final String getPath() {
        return instance.ICS_SQL_FILE;
    }

    public static final Properties getProperties() {
        return instance.props;
    }

    public static final String getProperty(String propName) {
        return instance.props.getProperty(propName);
    }

    public static final Element getElement(String eName) {
        return instance.searchNode(instance.rootNode, eName);
    }
}
