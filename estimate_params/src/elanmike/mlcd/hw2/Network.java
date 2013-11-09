package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;

import elanmike.mlcd.hw2.Constants.DIR;
import elanmike.mlcd.hw2.Constants.VARTYPES;
import elanmike.mlcd.hw2.Constants.VariablePair;

/**
 * This class, a singleton in our Estimate Parameters framework, 
 * reads a network from a network file, 
 * trains on data given in a training file,
 * and writes output to a given output file.
 * 
 * Some assumptions to note:
 * The map layout is IxJ grid of discrete cells, each denoted (i,j).
 * Our grid wraps around.
 * Our robot can take 4 possible actions: move north, south, east, or west.  
 * With each action, there is some probability that our robot will remain in-place.
 * Our robot can observe objects (walls or landmarks) in a given direction, but 
 * there is some probability of error in its sensors. 
 * 
 * We train by computing the MAP estimate.  We smooth using add-1 smoothing.
 * All prior probabilities are assumed to be the uniform distribution.
 * 
 * @author mcs
 *
 */
public class Network {
	private static int _biggestRow, _biggestCol, _biggestTimeStep, _numLandmarks;
	/**
	 * Given a 'network-gridAxB-tC.txt' input file,
	 * where A indicates the number of rows, B indicates the number of columns, 
	 * and C indicates the time step, reads in the network.
	 * 
	 * Note that because of our domain and model, we do not actually need to maintain
	 * the network in memory.
	 * 
	 * We only need to store the following, so that we can iterate over each position 
	 * at each time step when computing the CPD:
	 * - the number of rows I and number of columns J in the grid 
	 * (so we can iterate over every position)
	 * - the number of time steps
	 * - the number of landmarks
	 * - ?? TODO what else ??
	 * 
	 * @param networkFilename the name of the network file
	 * @throws IOException if cannot find the network file, or can't read a line in the file
	 * @throws NumberFormatException if cannot format a number
	 */
	public void read(String networkFilename) throws IOException {
		// variables to store info about the network
		BufferedReader br = new BufferedReader(new FileReader(networkFilename));
		String line;
		// on the first line is the number of following lines that describe variables
		int numVariables = -1;
		// after the first line, we either are reading variables, or edges. check if we have any variables left
		// init our known values
		_biggestRow = -1;
		_biggestCol = -1;
		_biggestTimeStep = -1;
		_numLandmarks = -1;
		while ((line = br.readLine()) != null) {
			if(numVariables == -1) { // set the number of variables
				numVariables = new Integer(line); // throws number format exceptioncompute 
			}
			else if(numVariables > 0) {
				// read variable
				String[] varInfo = line.split("\\s");
				String varName = varInfo[0];
				String[] varValues = varInfo[1].split(",");
				// if it's a position variable, take the max value for I or J
				Matcher m = Constants._regexPosition.matcher(varName);
				if(m.matches()) {
					if(m.group(1).equals(Constants.ROW)) {
						int currBiggestRow = new Integer(varValues[varValues.length-1]);
						if(currBiggestRow > _biggestRow) {
							_biggestRow = currBiggestRow;
						}
					}
					else if(m.group(1).equals(Constants.COL)) {
						int currBiggestCol = new Integer(varValues[varValues.length-1]);
						if(currBiggestCol > _biggestCol) {
							_biggestCol = currBiggestCol;
						}
					}
					else {
						br.close();
						throw new IOException("error parsing position variable name! fix me");
					}
				}
				// if it's observe landmark, take max value for N
				m = Constants._regexObserveLandmark.matcher(varName);
				if(m.matches()) {
					int landmarkNum = new Integer(m.group(1));
					if(landmarkNum > _numLandmarks) {
						_numLandmarks = landmarkNum;
					}
				}
				// and finally get the time step, and increment our max value
				m = Constants._regexVarTimeStep.matcher(varName);
				if(m.matches()) {
					int timeStep = new Integer(m.group(1));
					if(timeStep > _biggestTimeStep) {
						_biggestTimeStep = timeStep;
					}
				}
				numVariables--; // and decrement our number left to read
			}
			else { // reading edges
				
			}
		}
		br.close();
		// check we have valid values for our network parameters
		if(_biggestRow == -1 || _biggestCol == -1 || _biggestTimeStep == -1
				|| _numLandmarks == -1) {
			throw new IOException("error reading network!"
				+_biggestRow+'_'+_biggestCol+'_'+_biggestTimeStep+'_'+_numLandmarks);
		}
	}

	/**
	 * Reads the training file, and maintain counts of events.
	 * When finished with training data, smooth with add-1 smoothing.
	 * 
	 * Each line:
	 * TrajectoryNumber TimeStep Variable1=Value1 Variable2=Value2 ...
	 * 
	 * (The position and action variables are always given.)
	 * 
	 * @param trainingFilename training file name
	 * @throws IOException 
	 */
	public void train(String trainingFilename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(trainingFilename));
		MotionModel motion = new MotionModel();
		int prevRow = -1, prevCol = -1, currRow = -1, currCol = -1;
		DIR currAction = null, prevAction = null; // we can represent action by direction of move
		String line;
		while ((line = br.readLine()) != null) {
			String[] data = line.split(" ");
			// data[0] is trajectory number, meaningless
			// data[1] is time step, meaningless
			// assume first two variables data[2] and data[3] are positions, i, j
			// assume data[4] is action variable
			if(data.length <= 5) {
				System.err.printf("error parsing training line - too short:%s\n",line);
				continue; // skip line and continue counting
			}
			else {
				// get row value
				String[] rowValue = data[2].split("=");
				Matcher m = Constants._regexPosition.matcher(rowValue[0]);
				if(!m.matches() && !m.group(1).equals(Constants.ROW)) {
					System.err.printf("error parsing row position:%s\n",data[2]);
					continue; // skip line and continue counting
				}
				prevRow = currRow;
				currRow = Integer.parseInt(rowValue[1]);
				// get col value
				String[] colValue = data[3].split("=");
				m = Constants._regexPosition.matcher(colValue[0]);
				if(!m.matches() && !m.group(1).equals(Constants.COL)) {
					System.err.printf("error parsing col position:%s\n",data[3]);
					continue; // skip line and continue counting
				}
				prevCol = currCol;
				currCol = Integer.parseInt(colValue[1]);
				// get action value-
				String[] actionValue = data[4].split("=");
				m = Constants._regexAction.matcher(actionValue[0]);
				Matcher n = Constants._regexMove.matcher(actionValue[1]);
				if(!m.matches() && !n.matches()) {
					System.err.printf("error parsing action:%s\n",data[4]);
					continue; // skip line and continue counting
				}
				prevAction = currAction; // could be null if first line
				currAction = DIR.getDirValue(n.group(1)); // could be null if error in dir
				// for each (i,j) given: (for each data point)
				// remember previous (i,j) and previous action
				// TODO add 1 to count of the applicable motion parameters, if prevAction not null
				if(prevAction != null) {
					if(currAction == null) {
						System.err.printf("error parsing action:%s\n",data[4]);
						continue; // skip line and continue counting
					}
					// p(row i | row i-1, prev action moving in direction d)
					// p(row i | row i+1, prev action moving in direction d)
					// p(row i | row i, prev action moving in direction d)
					// p(col j | row j-1, prev action moving in direction d)
					// p(col j | row j+1, prev action moving in direction d)
					// p(col j | row j, prev action moving in direction d)
				}
				// all subsequent values are observation variable 'yes' values
				// go through all subsequent variables
				for(int v = 5; v < data.length; v++) {
					// TODO add 1 to count of observation_x at (i,j)	
				}
			}
		}
		br.close();
		// smooth
		smoothCounts();
	}
	/**
	 * Compute our smoothed counts
	 */
	private void smoothCounts() {
		// TODO implement smoothing counts
	}
	/**
	 * Gets our probability of X=x given context variables Y1=y1, Y2=y2, ... Yn=yn
	 * aka P(X=x | Y1=y1, Y2=y2, ... Yn=yn)
	 * One may pass no contexts.
	 * 
	 * @param lhsVar left hand side of query, X=x
	 * @param contextVars list of context variables, Yi=yi
	 * @return float probability
	 */
	private float getProbability(VariablePair lhsVar, VariablePair... contextVars) {
		// TODO implement
		return -1;
	}

	/**
	 * Writing the CPD:
	 * 
	 * We share parameters across time steps.
	 * 
	 * For each time point we compute:
	 * 
	 * (for the observation model)
	 * 
	 * P(ObserveWall_dir_t | PositionRow_t = i; PositionCol_t = j)
	 * P(ObserveLandmarkX_dir_t | PositionRow_t = i; PositionCol_t = j)
	 * 
	 * where dir \in {N,S,E,W} and X \in 1...N where N is the number of landmarks.
	 * We note that the values of our observation model are binary yes/no answers, 
	 * so we compute 'yes' and subtract from 1 for 'no'.
	 * We ignore all time values.
	 * 
	 * (for the motion model)
	 * 
	 * P(PositionRow_t = i | PositionRow t-1 = i-1; Action t-1)
	 * P(PositionRow_t = i | PositionRow t-1 = i+1; Action t-1)
	 * P(PositionRow_t = i | PositionRow t-1 = i; Action t-1)
	 * P(PositionCol_t = j | PositionCol t-1 = j-1; Action t-1)
	 * P(PositionCol_t = j | PositionCol t-1 = j+1; Action t-1)
	 * P(PositionCol_t = j | PositionCol t-1 = j; Action t-1)
	 * 
	 * where (i,j) is in our grid
	 * 
	 * Note we need to print with 13 sig figs
	 * double x = 1.0 / 7.0
	 * System.out.printf("%.13e", x);
	 * 
	 * @param cpdOutputFilename the cpd filename (to be created / overwritten)
	 * @throws IOException if cannot create file, delete file, write to file
	 */
	public void writeCPD(String cpdOutputFilename) throws IOException {
		// print out our read network parameters
		System.out.printf("I:%d J:%d T:%d L:%d\n", _biggestRow, _biggestCol, _biggestTimeStep, _numLandmarks);
		// create and open the cpd file
		File outfile = new File(cpdOutputFilename);
		if(outfile.exists()) {
			outfile.delete();
		}
		outfile.createNewFile();
		for(int t = 1; t <= _biggestTimeStep; t++) { // for each time point...
			// compute motion model, and observation model at the same time
			for(int i = 1; i <= _biggestRow; i++) {
				for(int j = 1; j <= _biggestCol; j++) {
					// for each i,j cell
					// compute observation model and motion model at each point
					for(DIR d : DIR.values()) {
						// motion model:
						// TODO 6 functions
						// compute p(row i | row i-1, prev action moving in direction d)
						// compute p(row i | row i+1, prev action moving in direction d)
						// compute p(row i | row i, prev action moving in direction d)
						// compute p(col j | row j-1, prev action moving in direction d)
						// compute p(col j | row j+1, prev action moving in direction d)
						// compute p(col j | row j, prev action moving in direction d)
						
						// observation model:
						// compute p(observe wall in that direction | current position)
						String varName = VARTYPES.OBSERVE_WALL.makeVarName("",d.toString(),Integer.toString(t));
						// TODO compute 'yes' | i,j
						// TODO and compute 'no' = 1-'yes' | i,j
						for(int l = 1; l <= _numLandmarks; l++) {
							// compute p(observe landmark L in that direction | current position)
							varName = VARTYPES.OBSERVE_LANDMARK.makeVarName(Integer.toString(l),d.toString(),Integer.toString(t));
							// TODO compute 'yes' | i,j
							// TODO and compute 'no' = 1-'yes' | i,j
						}
					}
				}
			}
		}
	}
}
