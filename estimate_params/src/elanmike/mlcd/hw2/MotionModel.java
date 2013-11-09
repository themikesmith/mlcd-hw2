package elanmike.mlcd.hw2;

import elanmike.mlcd.hw2.Constants.AXIS;
import elanmike.mlcd.hw2.Constants.DIR;

public class MotionModel {
	/** Row axis is vertical */
	public static final AXIS ROW_AXIS = AXIS.V;
	/** Col axis is horizontal */
	public static final AXIS COL_AXIS = AXIS.H;
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
	 * p(current i|j | prev i|j , prev move)
	 * Let S be the number of successful moves in direction d,
	 * and A be the number of attempted moves in direction d.
	 * if axis parallel to move and move succeeded, return S/A.
	 * else if axis parallel and move failed (remained in place), return 1-(S/A)
	 * else if remained in place along perpendicular axis, return 1
	 * else return 0 for impossible move
	 * 
	 * @param currPos current position
	 * @param prevPos previous position
	 * @param a the axis along which the positions exist
	 * 		(may not be the same as the move's axis)
	 * @param moveAttempted
	 * @return
	 * @throws IllegalArgumentException
	 */
	public float getProbability(int currPos, int prevPos, AXIS a, DIR moveAttempted) 
			throws IllegalArgumentException {
		if(a.equals(moveAttempted.getAxis())) { // parallel
			float value = (float) successfulMoves[moveAttempted.ordinal()] 
					/ attemptedMoves[moveAttempted.ordinal()];
			if(currPos == getPositionOfSuccessfulMove(moveAttempted, prevPos, a)) {
				return value; // move successful
			}
			else {// move failed, we remained in the same position
				return 1 - value;
			}
		}
		else { // perpendicular
			if(currPos == prevPos) {
				// remained in position along a perpendicular axis - always!
				return 1;
			}
			else {
				// moved along a perpendicular axis - impossible!
				return 0;
			}
		}
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
	 * Assemble the line for the CPD given the following:
	 * @param t time step value
	 * @param currPos current position (i or j depending on axis specified)
	 * @param prevPos previous position (i or j, depending on axis specified)
	 * @param a the axis specified by positions (row or col)
	 * @param d previous action specified by movement
	 * @param f the probability result
	 * @return the string formatted as required for the CPD
	 */
	public String assembleCPDEntry(int t, int currPos, int prevPos, AXIS a, DIR d, float f) {
		String axis = Constants.COL;
		if(a.equals(ROW_AXIS)) {
			axis = Constants.ROW;	
		}
		return String.format("Position%s_%d=%d Position%s_%d=%d,Action_%d=Move%s %.13e",
			axis, t, currPos+1, axis, t, prevPos+1, t, d.getLongName(), f);
	}
	/**
	 * Given a direction of a move, a previous position, 
	 * and an axis we're considering relative to the position,
	 * output what would be the current position if the move succeeded.
	 * @param move the move direction
	 * @param prevPos the previous position
	 * @param a the axis to consider when computing the position,
	 * 		which is not necessarily along the same axis as the move
	 * @return the current position along the axis a if the move succeeded
	 */
	public int getPositionOfSuccessfulMove(DIR move, int prevPos, AXIS a) {
		if(!move.getAxis().equals(a)) {// if the move's axis is perpendicular to a
			return prevPos; // we wouldn't have moved along the axis
		}
		else { // increment or decrement row or col
			if(a.equals(AXIS.H)) { // horizontal
				if(move.equals(DIR.EAST)) {
					return incrementRow(prevPos);
				}
				else { // west
					return decrementRow(prevPos);
				}
			}
			else { // vertical
				if(move.equals(DIR.NORTH)) {
					return incrementCol(prevPos);
				}
				else { // south
					return decrementCol(prevPos);
				}
			}
		}
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
