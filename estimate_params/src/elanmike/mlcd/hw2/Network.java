package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	// static final values
	private static final String ROW = "Row", COL = "Col"; 
	/**
	 * Enum for directions.
	 * @author mcs
	 *
	 */
	enum DIR {
		NORTH("N"), SOUTH("S"), EAST("E"), WEST("W");
		private String s;
		DIR(String s) {
			this.s = s;
		}
		public String toString() {
			return s;
		}
		static String getRegexGroup() {
			StringBuilder sb = new StringBuilder("(");
			for(DIR d : DIR.values()) { 
				sb.append(d.toString()).append('|');
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
			return sb.toString();
		}
	}
	/** Matcher for position variable name -- 1 group: row or col */
	private static final Pattern _regexPosition = Pattern.compile("Position(Row|Col)_\\d+");
	/** Matcher for observe wall variable name -- 1 group: direction */
	private static final Pattern _regexObserveWall = Pattern.compile("ObserveWall_"+DIR.getRegexGroup()+"_\\d+");
	/** Matcher for observe landmark variable name -- 2 groups: landmark number, direction */
	private static final Pattern _regexObserveLandmark = Pattern.compile("ObserveLandmark(\\d+)_"+DIR.getRegexGroup()+"_\\d+");
	/** Matcher for time step in variable name -- 1 group: time step number */
	private static final Pattern _regexVarTimeStep = Pattern.compile(".+_(\\d+)");
	private int _biggestRow, _biggestCol, _biggestTimeStep, _numLandmarks;
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
				numVariables = new Integer(line); // throws number format exception
			}
			else if(numVariables > 0) {
				// read variable
				String[] varInfo = line.split("\\s");
				String varName = varInfo[0];
				String[] varValues = varInfo[1].split(",");
				// if it's a position variable, take the max value for I or J
				Matcher m = _regexPosition.matcher(varName);
				if(m.matches()) {
					if(m.group(1).equals(ROW)) {
						int currBiggestRow = new Integer(varValues[varValues.length-1]);
						if(currBiggestRow > _biggestRow) {
							_biggestRow = currBiggestRow;
						}
					}
					else if(m.group(1).equals(COL)) {
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
				m = _regexObserveLandmark.matcher(varName);
				if(m.matches()) {
					int landmarkNum = new Integer(m.group(1));
					if(landmarkNum > _numLandmarks) {
						_numLandmarks = landmarkNum;
					}
				}
				// and finally get the time step, and increment our max value
				m = _regexVarTimeStep.matcher(varName);
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
	 * Reads the training file, and maintain counts.
	 * When finished with training data, smooth with add-1 smoothing.
	 * 
	 * @param trainingFilename training file name
	 * @throws IOException 
	 */
	public void train(String trainingFilename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(trainingFilename));
		br.close();
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
			// compute observation model
			// compute motion model
		}
	}

}
