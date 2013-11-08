package elanmike.mlcd.hw2;

import java.util.regex.Pattern;

public class Constants {
	/**
	 * Enum for directions.
	 * @author mcs
	 */
	public enum DIR {
		NORTH("N", "North"), SOUTH("S", "South"), EAST("E","East"), WEST("W","West");
		private String shortName, longName;
		DIR(String s, String t) {
			this.shortName = s;
			this.longName = t;
		}
		/** @return the short name of the direction */
		public String toString() {return shortName;}
		/** @return the long name of the direction */
		public String getLongName() {return longName;}
		/**
		 * @return the value of the move action when moving in the direction, eg "MoveNorth"
		 */
		public String getMoveAction() {return "Move"+longName;}
		/**
		 * @return a string '(x|y|...|z)' such that it's a regex for a grouped OR matcher
		 */
		static String getRegexGroup() {
			StringBuilder sb = new StringBuilder("(");
			for(DIR d : DIR.values()) { 
				sb.append(d.toString()).append('|');
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
		static DIR getDirValue(String dirLongName) {
			for(DIR d : DIR.values()) {
				if(dirLongName.equals(d.longName)) {
					return d;
				}
			}
			return null;
		}
	}
	/**
	 * Enum for action types.
	 * @author mcs
	 */
	public enum VARTYPES {
		POSITION("Position"), OBSERVE_WALL("ObserveWall"), 
			OBSERVE_LANDMARK("ObserveLandmark"), ACTION("Action");
		private String s;
		VARTYPES(String s) {
			this.s = s;
		}
		/**
		 * Creates a variable name given the parameters.
		 * Eg, passing '1','N','1' would add '1_N_1' to the end of the variable name.
		 * 
		 * @param params the list of parameters
		 * @return the string variable name
		 */
		public String makeVarName(String... params) {
			StringBuilder sb = new StringBuilder(s);
			for(String p : params) {
				sb.append(p).append('_');
			}
			sb.deleteCharAt(sb.length()-1);
			return sb.toString();
		}
	}
	// static final values for row and col
	public static final String ROW = "Row", COL = "Col";
	/** Matcher for position variable name -- 1 group: row or col */
	public static final Pattern _regexPosition = Pattern.compile("Position("+ROW+"|"+COL+")_\\d+");
	/** Matcher for observe wall variable name -- 1 group: direction */
	public static final Pattern _regexObserveWall = Pattern.compile("ObserveWall_"+DIR.getRegexGroup()+"_\\d+");
	/** Matcher for observe landmark variable name -- 2 groups: landmark number, direction */
	public static final Pattern _regexObserveLandmark = Pattern.compile("ObserveLandmark(\\d+)_"+DIR.getRegexGroup()+"_\\d+");
	/** Matcher for action variable name -- no groups */
	public static final Pattern _regexAction = Pattern.compile("Action_\\d+");
	/** Matcher for action variable value -- 1 group: direction */
	public static final Pattern _regexMove = Pattern.compile("Move"+DIR.getRegexGroup());
	/** Matcher for time step in variable name -- 1 group: time step number */
	public static final Pattern _regexVarTimeStep = Pattern.compile(".+_(\\d+)");
	/**
	 * Small class to hold variable name / value pairs
	 * 
	 * @author mcs
	 *
	 */
	class VariablePair {
		private String name, value;
		VariablePair(String name, String value) {
			this.name = name;
			this.value = value;
		}
		/** @return the name */
		String getName() {return name;}
		/** @return the value */
		String getValue() {return value;}
		/** @return a string representation "name=value" */
		public String toString() {return name + "=" +value;}
	}
}