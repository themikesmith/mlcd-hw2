package elanmike.mlcd.hw2;

import elanmike.mlcd.hw2.Constants.AXIS;
import elanmike.mlcd.hw2.Constants.DIR;

public class MotionModel {
	private int[] attemptedMoves, successfulMoves;
	private boolean smoothed;
	/**
	 * Create a MotionModel.
	 * Instantiates and initializes our counting arrays.
	 * Sets smoothed to false.
	 */
	public MotionModel() {
		attemptedMoves = new int[DIR.values().length];
		for(int i = 0; i < attemptedMoves.length; i++) {
			attemptedMoves[i] = 0;
		}
		successfulMoves = new int[DIR.values().length];
		for(int i = 0; i < successfulMoves.length; i++) {
			successfulMoves[i] = 0;
		}
		smoothed = false;
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
		if(moveAttempted.equals(DIR.NORTH) || moveAttempted.equals(DIR.SOUTH)) {
			if(prevCol == currCol && prevRow == currRow) { // unsuccessful
				addFailedMove(moveAttempted);
			}
			else if(prevRow == currRow && 
				prevCol == currCol + (moveAttempted.equals(DIR.NORTH) ? -1 : 1)) {
				addSuccessfulMove(moveAttempted);
			}
			else {
				System.err.printf("invalid positions specified for vert movement %s!prev:(%d,%d) curr(%d,%d)\n",
					moveAttempted, prevRow, prevCol, currRow, currCol);
			}
		}
		else if(moveAttempted.equals(DIR.EAST) || moveAttempted.equals(DIR.WEST)) {
			if(prevRow == currRow && prevCol == currCol) { // unsuccessful
				addFailedMove(moveAttempted);
			}
			else if(prevCol == currCol && 
				prevRow == currRow + (moveAttempted.equals(DIR.EAST) ? -1 : 1)) {
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
	 * Given our current counts, smooth by add-1 smoothing.
	 */
	public void smooth() {
		if(!smoothed) {
			// add 1 to every bucket of successful moves
			for(int i = 0; i < attemptedMoves.length; i++) {
				attemptedMoves[i]++;
			}
			// add 2 to every bucked of attempted moves
			for(int i = 0; i < successfulMoves.length; i++) {
				successfulMoves[i] += 2;
			}
			smoothed = true; // only smooth once
		}
		else {
			System.err.println("already smoothed motion model! don't call me twice!");
		}
	}
	/**
	 * Calculates and returns the probability of currPos,
	 * given previous move action direction d and prevPos.
	 * Let S be the number of successful moves in direction d,
	 * and A be the number of attempted moves in direction d.
	 * If move successful, returns S/A.
	 * if move failed (remained in place) returns 1-(S/A)
	 * else return 0 for impossible move
	 * 
	 * @param prevRow
	 * @param prevCol
	 * @param currRow
	 * @param currCol
	 * @param moveAttempted
	 * @return the probability
	 * @throws IllegalArgumentException
	 */
	public float getProbability(int prevRow, int prevCol, int currRow, int currCol, 
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
}
