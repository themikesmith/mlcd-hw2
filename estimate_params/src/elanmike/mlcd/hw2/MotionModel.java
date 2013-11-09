package elanmike.mlcd.hw2;

import elanmike.mlcd.hw2.Constants.DIR;

public class MotionModel {
	/**
	 * Represents a kind of probability we need to maintain under this model.
	 * @author mcs
	 */
	public enum PROBS { 
		rowEast,rowWest,sameRow,sameCol,colNorth,colSouth
	}
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
		smoothed = false;
	}
	/**
	 * Increment the count corresponding to a successful move in direction d
	 * @param d
	 */
	public void incrementSuccess(DIR d) {
		successfulMoves[d.ordinal()]++;
	}
	/**
	 * Increment the count corresponding to an attempted move in direction d
	 * @param d
	 */
	public void incrementAttempt(DIR d) {
		attemptedMoves[d.ordinal()]++;
	}
	/**
	 * Given our current counts, smooth by add-1 smoothing.
	 */
	public void smooth() {
		if(!smoothed) {
			// TODO do smoothing
			smoothed = true;
		}
	}
	/**
	 * Calculates and returns the probability of currPos,
	 * given previous move action direction d and prevPos.
	 * Let S be the number of successful moves in direction d,
	 * and A be the number of attempted moves in direction d.
	 * If currPos != prevPos, returns S/A.
	 * Else returns 1-(S/A) // if currPos == prevPos
	 * @param currPos current position
	 * @param d previous move action direction
	 * @param prevPos previous position
	 * @return the probability
	 */
	public float getProbability(PROBS currPos, DIR d, PROBS prevPos) {
		// TODO do calculations
		return -1;
	}
}
