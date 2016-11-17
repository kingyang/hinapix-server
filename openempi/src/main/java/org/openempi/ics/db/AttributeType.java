package org.openempi.ics.db;

/**
 * A class of constants used in querying the database.
 * In the objectivity implementation, an AttributeType is associated
 * with an Attribute object, which contains all the information needed
 * to build a query for that AttributeType.  DatabaseServicesObjy selects
 * the correct Attribute object by searching a HashMap using the AttributeType
 * provided as the key.
 */

public interface AttributeType {
    public static final Integer LAST_NAME = new Integer(1);
    public static final Integer SOCIAL_SECURITY_NUMBER = new Integer(2);
    public static final Integer DRIVERS_LICENSE_NUMBER = new Integer(3);
    public static final Integer ADDRESS = new Integer(4);
    public static final Integer EMAIL = new Integer(5);
    public static final Integer LAST_NAME_ALIAS = new Integer(6);
    public static final Integer NAME_SEARCH_KEY = new Integer(7);
    public static final Integer ALIAS_SEARCH_KEY = new Integer(8);
    public static final Integer PERSON_IDENTIFIER = new Integer(9);
    public static final Integer ETHNIC_GROUP = new Integer(10);
    public static final Integer GENDER = new Integer(11);
    public static final Integer MARITAL_STATUS = new Integer(12);
    public static final Integer SOURCE_DOCUMENT = new Integer(13);
    public static final Integer RACE = new Integer(14);
    public static final Integer RELIGION = new Integer(15);
    public static final Integer PHONE_NUMBER = new Integer(16);
    public static final Integer PERSON_OID = new Integer(17);
    public static final Integer FIRST_NAME_ALIAS = new Integer(18);
    public static final Integer AA_NAMESPACE_ID = new Integer(19);
    public static final Integer AF_NAMESPACE_ID = new Integer(20);
    public static final Integer CORPORATE_ID = new Integer(21);
    public static final Integer DOC_HEADER_ID = new Integer(23);
    public static final Integer PERSON_IDENTIFIER_ALIAS = new Integer(24);
    public static final Integer DATE_OF_BIRTH = new Integer(25);
    public static final Integer STATE_PROV = new Integer(26);
    public static final Integer AA_UNIV_ID = new Integer(27);
    public static final Integer AA_UNIV_ID_TYPE_CD = new Integer(28);
    public static final Integer ADDRESS_2 = new Integer(29);
    public static final Integer CITY = new Integer(30);
    public static final Integer ZIP = new Integer(31);
    public static final Integer COUNTRY = new Integer(32);
    public static final Integer PHONE_COUNTRY_CD = new Integer(33);
    public static final Integer PHONE_AREA_CD = new Integer(34);
    public static final Integer PHONE_NUM = new Integer(35);
    public static final Integer PHONE_EXT = new Integer(36);
    public static final Integer TELECOM_USE_CD = new Integer(37);

    public static final Integer AN_IDENTIFIER = new Integer(38);
    public static final Integer AN_AA_NAMESPACE_ID = new Integer(39);
    public static final Integer AN_IDENTIFIER_CODE = new Integer(40);
    public static final Integer AN_AF_UNIV_ID = new Integer(41);

    public static final Integer DRV_LICENSE_NUM = new Integer(42);
    public static final Integer DRV_LICENSE_ISSUING_STATE = new Integer(43);
    public static final Integer DRV_LICENSE_ISSUE_DATE = new Integer(44);
}
