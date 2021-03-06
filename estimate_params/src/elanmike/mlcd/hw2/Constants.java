package elanmike.mlcd.hw2;

import java.util.regex.Pattern;

public class Constants {
	/**
	 * Enum for axes
	 */
	public enum AXIS {
		H, V
	}
	/**
	 * Enum for directions. 
	 * @author mcs
	 */
	public enum DIR {
		NORTH("N", "North", AXIS.V), SOUTH("S", "South", AXIS.V), 
			EAST("E","East", AXIS.H), WEST("W","West", AXIS.H);
		private String shortName, longName;
		private AXIS axis;
		DIR(String s, String t, AXIS a) {
			this.shortName = s;
			this.longName = t;
			this.axis = a;
		}
		/**
		 * @return the axis of the direction
		 */
		public AXIS getAxis() {return axis;}
		/** @return the short name of the direction */
		public String toString() {return shortName;}
		/** @return the long name of the direction */
		public String getLongName() {return longName;}
		/**
		 * @return the value of the move action when moving in the direction, eg "MoveNorth"
		 */
		public String getMoveAction() {return "Move"+longName;}
		/**
		 * @return a string '(x|y|...|z)' such that it's a regex for a grouped OR matcher, using short names
		 */
		static String getRegexGroupShort() {
			StringBuilder sb = new StringBuilder("(");
			for(DIR d : DIR.values()) { 
				sb.append(d.toString()).append('|');
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			return sb.toString();
		}
		/**
		 * @return a string '(x|y|...|z)' such that it's a regex for a grouped OR matcher, using long names
		 */
		static String getRegexGroupLong() {
			StringBuilder sb = new StringBuilder("(");
			for(DIR d : DIR.values()) { 
				sb.append(d.getLongName()).append('|');
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			return sb.toString();
		}
		/**
		 * Given a string long name, output the direction value
		 * @param dirLongName
		 * @return dir value, or null if invalid long name specified
		 */
		static DIR getDirValueFromLongName(String dirLongName) {
			for(DIR d : DIR.values()) {
				if(dirLongName.equals(d.longName)) {
					return d;
				}
			}
			return null;
		}
		/**
		 * Given a string long name, output the direction value
		 * @param dirShortName
		 * @return dir value, or null if invalid long name specified
		 */
		static DIR getDirValueFromShortName(String dirShortName) {
			for(DIR d : DIR.values()) {
				if(dirShortName.equals(d.shortName)) {
					return d;
				}
			}
			return null;
		}
	}
	// static final values for row and col
	public static final String ROW = "Row", COL = "Col";
	/** Matcher for position variable name -- 1 group: row or col */
	public static final Pattern _regexPosition = Pattern.compile("Position("+ROW+"|"+COL+")_\\d+");
	/** Matcher for observe wall variable name -- 1 group: direction */
	public static final Pattern _regexObserveWall = Pattern.compile("ObserveWall_"+DIR.getRegexGroupShort()+"_\\d+");
	/** Matcher for observe landmark variable name -- 2 groups: landmark number, direction */
	public static final Pattern _regexObserveLandmark = Pattern.compile("ObserveLandmark(\\d+)_"+DIR.getRegexGroupShort()+"_\\d+");
	/** Matcher for action variable name -- no groups */
	public static final Pattern _regexAction = Pattern.compile("Action_\\d+");
	/** Matcher for action variable value -- 1 group: direction */
	public static final Pattern _regexMove = Pattern.compile("Move"+DIR.getRegexGroupLong());
	/** Matcher for time step in variable name -- 1 group: time step number */
	public static final Pattern _regexVarTimeStep = Pattern.compile(".+_(\\d+)$");
}