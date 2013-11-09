package elanmike.mlcd.hw2;

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
	 */
	public void processMove(int prevRow, int prevCol, int currRow, int currCol, DIR moveAttempted) {
		if(moveAttempted.equals(DIR.EAST) || moveAttempted.equals(DIR.WEST)) {
			if(prevCol == currCol && prevRow == currRow) { // unsuccessful
				addFailedMove(moveAttempted);
			}
			else if(prevRow == currRow && 
				prevCol == (currCol + (moveAttempted.equals(DIR.EAST) ? -1 : 1) % numCols)) {
				addSuccessfulMove(moveAttempted);
			}
			else {
				System.err.printf("invalid positions specified for vert movement %s!prev:(%d,%d) curr(%d,%d)\n",
					moveAttempted, prevRow, prevCol, currRow, currCol);
			}
		}
		else if(moveAttempted.equals(DIR.NORTH) || moveAttempted.equals(DIR.SOUTH)) {
			if(prevRow == currRow && prevCol == currCol) { // unsuccessful
				addFailedMove(moveAttempted);
			}
			else if(prevCol == currCol && 
				prevRow == currRow + (moveAttempted.equals(DIR.NORTH) ? -1 : 1) % numRows) {
				addSuccessfulMove(moveAttempted);
			}
			else {
				System.err.printf("invalid positions specified for horiz movement %s!prev:(%d,%d) curr(%d,%d)\n",
					moveAttempted, prevRow, prevCol, currRow, currCol);
			}
		}
		else {
			System.err.printf("invalid direction!:%s\n",moveAttempted);
		}
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
	public float getProbability(int currRow, int currCol, int prevRow, int prevCol, 
			DIR moveAttempted) throws IllegalArgumentException {
		float value = (float) successfulMoves[moveAttempted.ordinal()] 
			/ attemptedMoves[moveAttempted.ordinal()];
		if(moveAttempted.equals(DIR.NORTH) || moveAttempted.equals(DIR.SOUTH)) {
			if(prevCol == currCol && prevRow == currRow) { // unsuccessful
				return 1 - value;
			}
			else if(prevRow == currRow && // successful
				prevCol == currCol + (moveAttempted.equals(DIR.NORTH) ? -1 : 1)) {
				return value;
			}
			else { // impossible
				return 0;
			}
		}
		else if(moveAttempted.equals(DIR.EAST) || moveAttempted.equals(DIR.WEST)) {
			if(prevRow == currRow && prevCol == currCol) { // unsuccessful
				return 1 - value;
			}
			else if(prevCol == currCol && // successful
				prevRow == currRow + (moveAttempted.equals(DIR.EAST) ? -1 : 1)) {
				return value;
			}
			else { // impossible
				return 0;
			}
		}
		else {
			String error = "invalid direction!:"+moveAttempted;
			System.err.println(error);
			throw new IllegalArgumentException(error);
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
	//public void 
}
