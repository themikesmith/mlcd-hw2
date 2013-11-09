package elanmike.mlcd.hw2;

import elanmike.mlcd.hw2.Constants.AXIS;
import elanmike.mlcd.hw2.Constants.DIR;

public class MotionModel {
	private int[] attemptedMoves, successfulMoves;
	private int numRows, numCols;
	/**
	 * Create a MotionModel.
	 * Instantiates and initializes our counting arrays.
	 * @param numRows
	 * @param numCols
	 */
	public MotionModel(int numRows, int numCols) {
		attemptedMoves = new int[DIR.values().length];
		for(int i = 0; i < attemptedMoves.length; i++) {
			attemptedMoves[i] = 2;
		}
		successfulMoves = new int[DIR.values().length];
		for(int i = 0; i < successfulMoves.length; i++) {
			successfulMoves[i] = 1;
		}
		this.numRows = numRows;
		this.numCols = numCols;
	}
	/**
	 * Increment the count corresponding to a successful move in direction d
	 * @param d
	 */
	private void incrementSuccess(DIR d) {
		successfulMoves[d.ordinal()]++;
	}
	/**
	 * Increment the count corresponding to an attempted move in direction d
	 * @param d direction moved
	 */
	private void incrementAttempt(DIR d) {
		attemptedMoves[d.ordinal()]++;
	}
	/**
	 * Add a successful move to our counts.
	 * @param d direction moved
	 */
	public void addSuccessfulMove(DIR d) {
		incrementSuccess(d);
		incrementAttempt(d);
	}
	/**
	 * Add a failed move to our counts.
	 * @param d direction moved
	 */
	public void addFailedMove(DIR d) {
		incrementAttempt(d);
	}
	/**
	 * Given previous position, current position, and move attempted,
	 * process the (successful or unsuccessful) move by updating the appropriate counts.
	 * @param prevRow
	 * @param prevCol
	 * @param currRow
	 * @param currCol
	 * @param moveAttempted
	 * @return true if successful, false otherwise
	 */
	public boolean processMove(int prevRow, int prevCol, int currRow, int currCol, DIR moveAttempted) {
		if(moveAttempted.equals(DIR.EAST) || moveAttempted.equals(DIR.WEST)) {
			if(prevCol == currCol && prevRow == currRow) { // unsuccessful
				addFailedMove(moveAttempted);
			}
			else if(prevRow == currRow &&
				prevCol == (moveAttempted.equals(DIR.EAST) ? decrementCol(currCol) : incrementCol(currCol))) {
				addSuccessfulMove(moveAttempted);
			}
			else {
				System.err.printf("invalid positions specified for vert movement %s!prev:(%d,%d) curr(%d,%d)" +
						"changed:%d\n",
					moveAttempted, prevRow, prevCol, currRow, currCol, (moveAttempted.equals(DIR.EAST) ? decrementCol(currCol) : incrementCol(currCol)));
				return false;
			}
		}
		else if(moveAttempted.equals(DIR.NORTH) || moveAttempted.equals(DIR.SOUTH)) {
			if(prevRow == currRow && prevCol == currCol) { // unsuccessful
				addFailedMove(moveAttempted);
			}
			else if(prevCol == currCol && 
					prevRow == (moveAttempted.equals(DIR.NORTH) ? decrementRow(currRow) : incrementRow(currRow))) {
				addSuccessfulMove(moveAttempted);
			}
			else {
				System.err.printf("invalid positions specified for horiz movement %s!prev:(%d,%d) curr(%d,%d)" +
						"changed:%d\n",
					moveAttempted, prevRow, prevCol, currRow, currCol, (moveAttempted.equals(DIR.NORTH) ? decrementRow(currRow) : incrementRow(currRow)));
				return false;
			}
		}
		else {
			System.err.printf("invalid direction!:%s\n",moveAttempted);
			return false;
		}
		return true;
	}
	/**
	 * Calculates and returns the probability of currPos,
	 * given previous move action direction d and prevPos.
	 * p(current i,j | prev i,j ^ move)
	 * Let S be the number of successful moves in direction d,
	 * and A be the number of attempted moves in direction d.
	 * If move successful, returns S/A.
	 * if move failed (remained in place) returns 1-(S/A)
	 * else return 0 for impossible move
	 * 
	 * @param currRow
	 * @param currCol
	 * @param prevRow
	 * @param prevCol
	 * @param moveAttempted
	 * @return the probability
	 * @throws IllegalArgumentException
	 */
	public float getProbability(int currPos, int prevPos, AXIS a, DIR moveAttempted) 
			throws IllegalArgumentException {
		float value = (float) successfulMoves[moveAttempted.ordinal()] 
			/ attemptedMoves[moveAttempted.ordinal()];
		if(!moveAttempted.getAxis().equals(a)) {
			// if the axis of movement specified is perpendicular to the movement...
			if(currPos == prevPos) {
				// and we didn't move in the perpendicular axis (read: always will happen)
				return 1;
			}
			else { // and we did move in the perpendicular axis! uh oh
				System.err.println("change in position in axis perpendicular to movement");
				String error = String.format("curr:%d prev:%d axis:%s dir:%s", currPos, prevPos, a, moveAttempted);
				System.err.println(error);
				throw new IllegalArgumentException(error);
			}
		}
		else { // else the axis of movement is parallel to change in position
			if(prevPos == currPos) { // unsuccessful
				return 1 - value;
			}
			else {
				if(moveAttempted.equals(DIR.NORTH) || moveAttempted.equals(DIR.SOUTH)) {
					if(prevPos == (moveAttempted.equals(DIR.NORTH)
							? decrementRow(currPos) : incrementRow(currPos))) {
						return value;
					}
				}
				else if(moveAttempted.equals(DIR.EAST) || moveAttempted.equals(DIR.WEST)) {
					if(prevPos == (moveAttempted.equals(DIR.EAST)
							? decrementCol(currPos) : incrementCol(currPos))) {
						return value;
					}
				}
				else {
					String error = "invalid direction!:"+moveAttempted;
					System.err.println(error);
					throw new IllegalArgumentException(error);
				}
			}
		}
		String error = prevPos+" differs from "+currPos+" by more than 1 on axis:"+a;
		System.err.println(error);
		throw new IllegalArgumentException(error);
	}
	/**
	 * 
	 */
	public void printInfoDebug() {
		System.err.println("attempted moves:");
		for(int i = 0; i < attemptedMoves.length; i++) {
			System.err.printf("i:%s c:%d\n", DIR.values()[i], attemptedMoves[i]);
		}
		System.err.println("successful moves:");
		for(int i = 0; i < successfulMoves.length; i++) {
			System.err.printf("i:%s c:%d\n", DIR.values()[i], successfulMoves[i]);
		}
	}
	/**
	 * Calculate and assemble the line for the CPD given the following:
	 * @param t time step value
	 * @param currPos current position (i or j depending on axis specified)
	 * @param prevPos previous position (i or j, depending on axis specified)
	 * @param a axis specified
	 * @param d previous action specified by movement
	 * @return the string formatted as required for the CPD
	 */
	public String calculateAssembleCPDEntry(int t, int currPos, int prevPos, AXIS a, DIR d) {
		float f = getProbability(currPos, prevPos, a, d);
		return String.format("PositionRow_%d=%d PositionRow_%d=%d,Action_%d=Move%s %.13e",
			t, currPos, t, prevPos, t, d.getLongName(), f);
	}
	/**
	 * 
	 * @param row
	 * @return row+1 % numRows
	 */
	public int incrementRow(int row) { 
		return (row + 1) % numRows;
	}
	/**
	 * 
	 * @param row
	 * @return row-1 % numRows
	 */
	public int decrementRow(int row) { 
		return (row - 1 + numRows) % numRows;
	}
	/**
	 * 
	 * @param col
	 * @return col+1 % numCols
	 */
	public int incrementCol(int col) { 
		return (col + 1) % numCols;
	}
	/**
	 * 
	 * @param col
	 * @return col-1 % numCols
	 */
	public int decrementCol(int col) { 
		return ((col - 1 + numCols) % numCols);
	}
}
