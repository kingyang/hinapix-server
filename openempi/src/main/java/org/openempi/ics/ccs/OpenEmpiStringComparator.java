package org.openempi.ics.ccs;

public class OpenEmpiStringComparator
{
	private String x = "" ;
	private String y = "" ;
	private double score = 0.0 ;
	
	public OpenEmpiStringComparator(String x, String y) {
		this.x = x ;
		this.y = y ;
		score = OpenEmpiExactMatchComparator.compare(x,y);
	}

	public OpenEmpiStringComparator(String service, String x, String y) {
		// note - this ignores service.  implement later
		this.x = x ;
		this.y = y ;
		score = OpenEmpiExactMatchComparator.compare(x,y);
	}

	// since lists are not part of this implementation, only one score exists
	public int getNumberOfScores() {
		return 1;
	}

	// returns score
	public int getScoreInt(int i) {
		if (score == 1.0) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getScoreInt() {
		// TODO Auto-generated method stub
		return 0.99;
	}

	public String getKey(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStartRange(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEndRange(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getKeyCountInt() {
		// TODO Auto-generated method stub
		return 1;
	}

	public String getRangeEntry(int i) {
		// TODO Auto-generated method stub
		return null;
	}

}

class OpenEmpiExactMatchComparator 
{
  public static double compare(String x, String y)
  {
	  	  if (x.equalsIgnoreCase(y))
		  return 1.0 ;
	  return 0.0 ;
  }
}