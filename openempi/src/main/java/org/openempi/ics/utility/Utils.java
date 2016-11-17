/*
 * Title:       Utils
 * Description: A collection of utility methods.
 * Copyright:   (c) 1998-2002
 * Company:     CareScience
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openempi.data.DocumentHeader;
import org.openempi.data.Person;


/**
 * A collection of static utility methods.
 *
 * @author mabundo, CareScience
 * @version 1.15, 20020318
 */
public class Utils {
  /** The message date format, <code>yyyyMMddhhmmss</code> */
  public static final DateFormat MESSAGE_DATE = new SimpleDateFormat("yyyyMMddhhmmss");

  /** The birth date format, <code>yyyyMMdd</code> */
  public static final DateFormat BIRTH_DATE = new SimpleDateFormat("yyyyMMdd");

  /** This class should never be instantiated, all methods are static. */
  private Utils() {}

  /**
   * Returns the namespace ID of a hostname.
   *
   * @param hostname the fully-qualified hostname, e.g. "medtec.sansum.com"
   * @return the namespace ID, e.g. "com.sansum.medtec"
   */
  public static String getNamespaceIdFor(String hostname) {
    StringBuffer sb = new StringBuffer();

    if ((hostname != null) && (hostname.length() > 0)) {
      List list = toReversedList(hostname);

      int tokenCount = list.size();

      Iterator it = list.iterator();

      while (it.hasNext()) {
        sb.append(it.next());
        sb.append(".");
      }

      // Delete the last '.'
      sb.deleteCharAt(sb.length()-1);
    }
    return sb.toString();
  }

  /**
   * Given a hostname, returns a reversed list.
   *
   * @param hostname a fully-qualified hostname, e.g. "medtec.sansum.com"
   * @return a reversed <code>List</code>
   */
  private static synchronized List toReversedList(String hostname) {
    List list = new ArrayList();

    if ((hostname != null) && (hostname.length() > 0)) {
      StringTokenizer st = new StringTokenizer(hostname, ".");

      while (st.hasMoreTokens()) {
        list.add(st.nextToken());
      }
    }

    // Reverse the list
    Collections.reverse(list);

    return list;
  }

  /**
   * Writes a stream to a file.
   *
   * @param ostream the stream to write
   * @param filename name of output file
   * @throws IOException
   */
  public static void writeToFile(ByteArrayOutputStream ostream, String filename)
    throws IOException {

    FileOutputStream outStream = new FileOutputStream(filename);
    outStream.write(ostream.toByteArray());
    outStream.close();
  }

  /**
   * Filters a list of potential matches to form a list of unique objects.
   *
   * @param people the list of people to be filtered
   * @return List the filtered list of unique people
   */
    public static List filterForUnique(List people) 
    {
        Profile.begin("Utils.filterForUnique");
        int size = people.size();
        if (people != null) {
            for (int i = 0; i < size; i++) {
                Person curPerson = (Person)people.get(i);
                
                for (int j = i+1; j < size;j++) {
                    /* Didn't want to change the Person.equals() method at 
                       this time, it would involve rebuilding CIA... but this
                       oid check should be done by equals() */
                    Person p = (Person) people.get(j);
                    String oid1 = curPerson.getOid();
                    String oid2 = p.getOid();
                    if (oid1 != null && oid2 != null) {
                        if (oid1.equals(oid2)) {
                            people.remove(j);
                            size--;
                            j--;
                        }
                    } else if (curPerson.equals(p)) {
                        people.remove(j);
                        size--;
                        j--;
                    }
                }
            }
        }
        Profile.end("Utils.filterForUnique");
        return people;
    }

  /**
   * Method to get the first DocumentHeader from a List of Headers for a
   * <code>Person</code> Object
   *
   * @param headers - The list of <code>DocumentHeader</code>s of which, the
   * first DocumentHeader is required
   * @DocumentHeader - The first DocumentHeader
   */
  public static DocumentHeader getFirstHeader(List headers) {
    DocumentHeader firstHeader = null;
    if ((headers != null) && (headers.size() > 0)) {
      firstHeader = (DocumentHeader) headers.get(0);
      for (int i = 0; i < headers.size(); i++) {
        DocumentHeader h = (DocumentHeader) headers.get(i);
        if (h.getMessageDate().before(firstHeader.getMessageDate())) {
          firstHeader = h;
        }
      }
    }

    return firstHeader;
  }

  /**
   * Method to get the last DocumentHeader from a List of Headers for a
   * <code>Person</code> Object
   *
   * @param headers - The list of <code>DocumentHader</code>s of
   *         which, the last DocumentHeader is required
   * @DocumentHeader - The last DocumentHeader
   */
  public static DocumentHeader getLastHeader(List headers) {
    DocumentHeader lastHeader = null;
    if ((headers != null) && (headers.size() > 0)) {
      lastHeader = (DocumentHeader) headers.get(0);
      for (int i = 0; i < headers.size(); i++) {
        DocumentHeader h = (DocumentHeader) headers.get(i);
        if (h.getMessageDate().after(lastHeader.getMessageDate())) {
          lastHeader = h;
        }
      }
    }

    return lastHeader;
  }

    /**
       Given a config file, will return the full, absolute path
       to the file.  If the file name is already an absolute path, 
       that same path is returned.  If the file is not absolute,
       the full path to the config is returned. 
       
       @param file The file name or full path to the config file
       @return The canonical absolute path to the config file
    */
    public static String configDir(String file)
    {
        try {
            if (file != null) {
                File f = new File(file);
                // if a file is not existed, try to find the file in the class path
                if(f.exists() == false)
                {
                    if(file.startsWith("/") == false )
                    {
                        file = "/".concat(file);
                        file = Utils.class.getResource(file).getPath().toString();
                        f = new File(file);
                    }
                }

                if (! f.isAbsolute()) {
                    // This finds to domain root for weblogic 6/7
                    String domain = System.getProperty("weblogic.Domain");
                    if (domain != null) 
                        f = new File("config/" + domain + "/" + file);
                }
                return f.getCanonicalPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
