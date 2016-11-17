/*
 * Title      : AttributeComparator
 * Description: Performs demographic attribute comparisons.
 * Copyright  : (c) 1998-2002
 * Company    : CareScience, Inc.
 *              3600 Market Street
 *              6th Floor
 *              Philadelphia, PA 19104, U.S.A
 */
package org.openempi.ics.ccs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openempi.ics.utility.Profile;

/**
 * This class is used for demographic attribute comparisons such as names,
 * birth places, home addresses, birth dates.  When performing comparisons
 * the AttributeComparator filters out string variations including: keyboarding
 * errors, nicknames, sequence variations, noise and phonetic variations.
 *
 * @author CareScience
 * @version 1.5, 20020324
 */
public class AttributeComparator {

  public static final int MEAN_SCORING = 1;
  public static final int MEDIAN_SCORING = 2;
  public static final int MODE_SCORING = 3;

  private static final String NAME_NAME_SEARCH_SERVICE = "name1   ";
  private static final String STREET_NAME_SEARCH_SERVICE = "street";

  private int scoringMethod = MEDIAN_SCORING;

  /**
   * Compares two street names.
   *
   * @return a value between 0 and 1, where 0 represents no correlation and 1
   * represents a perfect correlation.
   */
  public double compareStreets(String street1, String street2) {
    return compare(street1, street2, STREET_NAME_SEARCH_SERVICE);
  }

  /**
   * Compares two names.
   *
   * @return a value between 0 and 1, where 0 represents no correlation and 1
   * represents a perfect correlation.
   */
  public double compareNames(String name1, String name2) {
    return compare(name1, name2, NAME_NAME_SEARCH_SERVICE);
  }

  /**
   * Compares two phone numbers.
   *
   * @return a value between 0 and 1, where 0 represents no correlation and 1
   * represents a perfect correlation.
   */
  public double comparePhoneNumbers(String phone1, String phone2) {
    return compare(phone1, phone2, STREET_NAME_SEARCH_SERVICE);
  }

  /**
   * Compares two dates strings.
   *
   * @return a value between 0 and 1, where 0 represents no correlation and 1
   * represents a perfect correlation.
   */
  public double compareDates(String date1, String date2) {
    Profile.begin("AttributeComparator.compareDates");
    double correlation = 0.0;
    if (date1.length() > 0 && date2.length() > 0 && date1.equals(date2)) 
        correlation = CorrelationSystem.EXACT_MATCH;
    else {
    	OpenEmpiStringComparator dateCompObj = new OpenEmpiStringComparator(date1,date2);
        correlation = dateCompObj.getScoreInt()/100.0;
    }
    if(correlation < .55 && correlation > .45){
      // get the year off each string
      int year1 = Integer.parseInt(date1.substring(4));
      int year2 = Integer.parseInt(date2.substring(4));

      if (year1 != year2) {
          int difference = Math.abs(year1 - year2);
          // deal with typo in decade
          if (difference == 10) {
            correlation = 0.8;
          }
          // deal with typo in year
          else if (difference <= 2) {
            correlation = 0.8;
          }
          // deal with typo in century
          else if (difference == 100) {
            correlation = 0.8;
          }
          // anything else is assumed to be a different date
      }
    }
    Profile.end("AttributeComparator.compareDates");
    return correlation;
  }

  /**
   * Compares two <code>Date</code> objects.
   *
   * @return a value between 0 and 1, where 0 represents no correlation and 1
   * represents a perfect correlation.
   */
  public double compareDates(Date date1, Date date2) {
    Profile.begin("AttributeComparator.compareDates");
    double correlation = CorrelationSystem.UNKNOWN;
    DateFormat df = new SimpleDateFormat("MMddyyyy");

    if (date1 != null && date2 != null) {
       if (date1.equals(date2)) 
           correlation = CorrelationSystem.EXACT_MATCH;
       else {
           String dateStr1 = df.format(date1);
           String dateStr2 = df.format(date2);
           correlation = compareDates(dateStr1, dateStr2);
       }
    }

    Profile.end("AttributeComparator.compareDates");
    return correlation;
  }

  /**
   * Compares two <code>long</code> dates.
   *
   * @return a value between 0 and 1, where 0 represents no correlation and 1
   * represents a perfect correlation.
   */
  public double compareDates(long date1, long date2) {
    Profile.begin("AttributeComparator.compareDates");
    DateFormat df = new SimpleDateFormat("MMddyyyy");

    String dateStr1 = df.format(new Date(date1));
    String dateStr2 = df.format(new Date(date2));

    double ret = compareDates(dateStr1, dateStr2);
    Profile.end("AttributeComparator.compareDates");
    return ret;
  }

  /**
   * Returns the scoring method used by the comparator.
   */
  public int getScoringMethod() {
    return scoringMethod;
  }

  /**
   * Updates the scoring method that is used in comparisons.
   */
  public void setScoringMethod(int method) throws IllegalArgumentException {
    if ((method != MEDIAN_SCORING) && (method != MEAN_SCORING) &&
        (method != MODE_SCORING)) {
      throw new IllegalArgumentException("Unknown scoring method: " + method);
    }
    scoringMethod = method;
  }

  /**
   * Makes a comparison using NameSearch.  Does not apply name or street
   * matching heuristics.
   */
  public double alfaCompare(String x, String y) {
    Profile.begin("AttributeComparator.alfaCompare");
    double correlation = CorrelationSystem.UNKNOWN;

    if (x != null && y != null) {
      if (x.length() > 0 && y.length() > 0 && x.equals(y)) {
          correlation = CorrelationSystem.EXACT_MATCH;
      } else {
          OpenEmpiStringComparator alfaCompObj = new OpenEmpiStringComparator(x, y);
          
          int[] scores = new int[alfaCompObj.getNumberOfScores()];
     //JWW 04-14-2007 Look at this!!!     
          for (int i = 1; i < scores.length; i++) {
              scores[i] = alfaCompObj.getScoreInt(i);
          }
          
          if (scoringMethod == MEAN_SCORING) {
//              correlation = Stats.mean(scores)/100.0;
          }
          else if (scoringMethod == MEDIAN_SCORING) {
//              correlation = Stats.median(scores)/100.0;
          }
          else if (scoringMethod == MODE_SCORING) {
//              correlation = Stats.mode(scores)/100.0;
          }
      }
    }
    Profile.end("AttributeComparator.alfaCompare");
    return correlation;
  }

  /**
   * Makes a comparison using NameSearch numcomp.  Good for SSN comparisons
   * Does not apply name or street matching heuristics.
   */
  public double numCompare(String x, String y) {
    Profile.begin("AttributeComparator.numCompare");
    double correlation = CorrelationSystem.UNKNOWN;

    if (x != null && y != null) {
    	OpenEmpiStringComparator numCompObj = new OpenEmpiStringComparator(x,y);

      int[] scores = new int[numCompObj.getNumberOfScores()];

      for (int i = 1; i < scores.length; i++) {
        scores[i] = numCompObj.getScoreInt(i);
      }

      if (scoringMethod == MEAN_SCORING) {
//        correlation = Stats.mean(scores)/100.0;
      } else if (scoringMethod == MEDIAN_SCORING) {
//        correlation = Stats.median(scores)/100.0;
      } else if (scoringMethod == MODE_SCORING) {
//        correlation = Stats.mode(scores)/100.0;
      }
    }

    Profile.end("AttributeComparator.numCompare");
    return correlation;
  }

  /**
   * Makes a comparison using NameSearch.
   */
  public double compare(String x, String y, String service) {
    Profile.begin("AttributeComparator.compare");
    double correlation = CorrelationSystem.UNKNOWN;

    if (x != null && y != null && service != null) {
      if (x.length() > 0 && y.length() > 0 && x.equals(y)) 
          correlation = CorrelationSystem.EXACT_MATCH;
      else {
    	  OpenEmpiStringComparator comp2Obj = new OpenEmpiStringComparator(service, x, y);
          
          int[] scores = new int[comp2Obj.getNumberOfScores()];
          
          for (int i = 1; i < scores.length; i++) {
              scores[i] = comp2Obj.getScoreInt(i);
          }
          
          if (scoringMethod == MEAN_SCORING) {
//              correlation = Stats.mean(scores)/100.0;
          } else if (scoringMethod == MEDIAN_SCORING) {
//              correlation = Stats.median(scores)/100.0;
          } else if (scoringMethod == MODE_SCORING) {
//              correlation = Stats.mode(scores)/100.0;
          }
      }
    }

    Profile.end("AttributeComparator.compare");
    return correlation;
  }

}
