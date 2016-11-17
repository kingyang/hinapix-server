package org.openempi.ils.utility;

/**
 * Title:        DataEncryptor
 * Description:  Utility class that encrypts/decrypts String data.
 * Copyright:    Copyright (c) 2002
 * Company:      CareScience, Inc.
 */

import java.security.Key;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Utility class that encrypts/decrypts String data.
 *
 * @author apope, CareScience
 * @version 1.3, 20020521
 */
public class DataEncryptor {
  /* Default key */
  private static byte[] defaultKey =
  {0x31,0x1f,0xffffffa7,0xffffffd6,0xffffffc1,0xfffffff8,0xffffffae,0xffffffb0};

  private static Key _key;

  /** Hopefully prevents class garbage collection */
  private static DataEncryptor _instance = new DataEncryptor();

  static {
    createKey();
  }

  private DataEncryptor() {}

  /**
   * Sets a new Key.
   *
   * @param Key key - The private key used to encryt/decrypt.
   */
  public static void setKey(byte[] key) {
    if (key.length == 0) {
      defaultKey = key;
    } else {
      System.out.println("Invalid Key! ....Using default key.");
      createKey();
    }
  }

  /**
   * Gets the Key.
   *
   * @param Key key - The private key used to encryt/decrypt.
   */
  public static Key getKey() {
    return _key;
  }

  /*
   * Creates the Key.
   *
   * @param Key key - The private key used to encryt/decrypt.
   */
  private static void createKey() {
    try {
      Security.addProvider(new com.sun.crypto.provider.SunJCE());
      _key = (Key) new SecretKeySpec(defaultKey, "DES");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Encypts the String data
   * @param data - The string to be encrypted.
   * @return encrypted string or null on error
   */
  public static String encrypt(String data) {
    try {
      Security.addProvider(new com.sun.crypto.provider.SunJCE());
      Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, _key);
            
      byte[] inputBytes = data.getBytes("UTF8");
      byte[] outputBytes = cipher.doFinal(inputBytes);
            
      BASE64Encoder encoder = new BASE64Encoder();

      return encoder.encode(outputBytes);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Decrypts the encoded data
   * @param encodedData - The string to be decrypted.
   * @return decrypted string or null on error
   */
  public static String decrypt(String encodedData) {
    try {
      Security.addProvider(new com.sun.crypto.provider.SunJCE());
      Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, _key);
            
      BASE64Decoder decoder = new BASE64Decoder();
            
      byte[] inputBytes = decoder.decodeBuffer(encodedData);
      byte[] outputBytes = cipher.doFinal(inputBytes);
            
      return new String(outputBytes, "UTF8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Main method used for testing
   */
  public static void main(String[] args) {
    try {
      String data = "password";

      DataEncryptor de = new DataEncryptor();

      System.out.println("\nNow encrypting data: " + data);
      String eData = de.encrypt(data);

      System.out.println("\nNow decrypting data: " + eData);
      String dData = de.decrypt(eData);

      System.out.println("\nThe decrypted data is: " + dData);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
