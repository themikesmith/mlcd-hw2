package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;

import elanmike.mlcd.hw2.Constants.AXIS;
import elanmike.mlcd.hw2.Constants.DIR;

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
	private static MotionModel _motion;
	private static int _biggestRow, _biggestCol, _biggestTimeStep, _numLandmarks;
	private static ObservationModel _obsMod;
	
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
			} // we don't need to read the edges. we have sufficient statistics
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
		// initialize our models
		_motion = new MotionModel(_biggestRow, _biggestCol);
		_obsMod = new ObservationModel(_biggestRow,_biggestCol,_numLandmarks);
		BufferedReader br = new BufferedReader(new FileReader(trainingFilename));
		int prevRow = -1, prevCol = -1, currRow = -1, currCol = -1,
			currTrajectory = -1, prevTrajectory = -1;
		// we can represent action by the direction of move
		DIR currAction = null, prevAction = null;
		String line, prevLine = "";
		while ((line = br.readLine()) != null) {
			String[] data = line.split(" ");
			// data[0] is trajectory number
			prevTrajectory = currTrajectory;
			currTrajectory = Integer.parseInt(data[0]);
			if(currTrajectory != prevTrajectory) {
				// begin new trajectory
				prevRow = -1;
				prevCol = -1;
				currRow = -1;
				currCol = -1;
				currAction = null;
				prevAction = null;
			} // else: curr trajectory = prev, continue as usual 
			// data[1] is time step, meaningless
			// assume first two variables data[2] and data[3] are positions, i, j
			// assume data[4] is action variable
			if(data.length < 5) {
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
				currRow = Integer.parseInt(rowValue[1]) - 1;
				// get col value
				String[] colValue = data[3].split("=");
				m = Constants._regexPosition.matcher(colValue[0]);
				if(!m.matches() && !m.group(1).equals(Constants.COL)) {
					System.err.printf("error parsing col position:%s\n",data[3]);
					continue; // skip line and continue counting
				}
				prevCol = currCol;
				currCol = Integer.parseInt(colValue[1]) - 1;
				// get action value
				String[] actionValue = data[4].split("=");
				m = Constants._regexAction.matcher(actionValue[0]);
				Matcher n = Constants._regexMove.matcher(actionValue[1]);
				if(!m.matches() && !n.matches()) {
					System.err.printf("error parsing action:%s\n",data[4]);
					continue; // skip line and continue counting
				}
				prevAction = currAction; // could be null if first item of trajectory
				try {
					n.matches();
					currAction = DIR.getDirValueFromLongName(n.group(1)); // could be null if error in dir	
				}
				catch(IllegalStateException ex) {
					ex.printStackTrace();
				}
				// for each (i,j) given: (for each data point)
				// remember previous (i,j) and previous action
				if(prevAction != null) {
					if(currAction == null) {
						System.err.printf("error parsing action:%s\n",data[4]);
						continue; // skip line and continue counting
					}
					if(!_motion.processMove(prevRow, prevCol, currRow, currCol, prevAction)) {
						System.err.println("p:"+prevLine);
						System.err.println("l:"+line);
					}
				}
				// all subsequent values are observation variable 'yes' values
				// go through all subsequent variables
				boolean[] obsers  = new boolean[4*(1+_numLandmarks)];
				for(int v = 5; v < data.length; v++) {
					// TODO add 1 to count of observation_x at (i,j)
					
					String[] observationValue = data[v].split("=");
					observationValue = observationValue[0].split("_");
					
					//for(String s:observationValue)
					//	System.out.println(s);
					
					
					int dir = Constants.DIR.getDirValueFromShortName(observationValue[1]).ordinal();
					if(observationValue[0].contains("Wall")){
						//System.out.println("Wall - " + observationValue[1] + "("+dir+")");
						obsers[dir] = true;
					}else if(observationValue[0].contains("Landmark")){
						int landmark_val = Integer.parseInt(observationValue[0].substring(observationValue[0].length()-1));
						obsers[dir+4*landmark_val] = true;
						//System.out.println("Landmark("+landmark_val+") - " + observationValue[1] + "("+dir+")");
					}else{
						//Hmm...
					}
					

				}
				_obsMod.addObservation(currRow, currCol, obsers);
				
				
			}
			prevLine = line;
		}
		br.close();
		//_motion.printInfoDebug();
		//_obsMod.printWallDebug();
		//_obsMod.printLandmarkDebug();
		//System.out.println(_obsMod.formattedCpdOutput(0));
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
		// create and open the cpd file
		File outfile = new File(cpdOutputFilename);
		if(outfile.exists()) {
			outfile.delete();
		}
		outfile.createNewFile();
		PrintWriter out = new PrintWriter(outfile);
		for(int t = 0; t <= _biggestTimeStep; t++) { // for each time point...
			// motion model:
			if(t + 1 <= _biggestTimeStep) { // only compute if we have data
				// for every prev row value i
				for(int i = 0; i < _biggestRow; i++) {
					// for every prev action
					for(DIR prevAction : DIR.values()) {
						// print p(success)
						int currPos = _motion.getPositionOfSuccessfulMove(prevAction, i, MotionModel.ROW_AXIS);
						float f = _motion.getProbability(currPos, i, MotionModel.ROW_AXIS, prevAction);
						if(f != 0) { // check superfluous, but just in case we don't smooth
							out.println(_motion.assembleCPDEntry(t, currPos, i, MotionModel.ROW_AXIS, prevAction, f));
						}
						// print p(fail) where applicable (if axis is parallel)
						if(prevAction.getAxis().equals(MotionModel.ROW_AXIS)) {
							currPos = i;
							f = _motion.getProbability(currPos, i, AXIS.V, prevAction);
							if(f != 0) { // only print if scenario is possible.
								out.println(_motion.assembleCPDEntry(t, currPos, i, MotionModel.ROW_AXIS, prevAction, f));
							}
						}
					}
				}
				// for every prev col value j
				for(int j = 0; j < _biggestCol; j++) {
					// for every prev action
					for(DIR prevAction : DIR.values()) {
						// print p(success)
						int currPos = _motion.getPositionOfSuccessfulMove(prevAction, j, MotionModel.COL_AXIS);
						float f = _motion.getProbability(currPos, j, MotionModel.COL_AXIS, prevAction);
						if(f != 0) { // check superfluous, but just in case we don't smooth
							out.println(_motion.assembleCPDEntry(t, currPos, j, MotionModel.COL_AXIS, prevAction, f));
						}
						// print p(fail) where applicable (if axis is parallel)
						if(prevAction.getAxis().equals(MotionModel.COL_AXIS)) {
							currPos = j;
							f = _motion.getProbability(currPos, j, AXIS.V, prevAction);
							if(f != 0) { // only print if scenario is possible.
								out.println(_motion.assembleCPDEntry(t, currPos, j, MotionModel.COL_AXIS, prevAction, f));
							}
						}
					}
				}
			}
			// observation model:
			for(int i = 0; i < _biggestRow; i++) {
				for(int j = 0; j < _biggestCol; j++) {
					for(DIR d : DIR.values()) {
						// compute p(observe wall in that direction | current position)
						//walls
						//e.g. ObserveWall_N_8=Yes PositionRow_8=9,PositionCol_8=2 0.866634
						out.printf("ObserveWall_%s_%d=Yes PositionRow_%d=%d,PositionCol_%d=%d %.13e\n",
								d.toString(),t,t,i+1,t,j+1,_obsMod.getWallObservation(i, j, d.ordinal()));
						out.printf("ObserveWall_%s_%d=No PositionRow_%d=%d,PositionCol_%d=%d %.13e\n",
								d.toString(),t,t,i+1,t,j+1,(1-_obsMod.getWallObservation(i, j, d.ordinal())));
						for(int lmk = 1; lmk <= _numLandmarks; lmk++) {
							// compute p(observe landmark L in that direction | current position)
							out.printf("ObserveLandmark%d_%s_%d=Yes PositionRow_%d=%d,PositionCol_%d=%d %.13e\n",
									lmk,d.toString(),t,t,i+1,t,j+1,_obsMod.getLandmarkObservation(i, j, lmk, d.ordinal()));
							out.printf("ObserveLandmark%d_%s_%d=No PositionRow_%d=%d,PositionCol_%d=%d %.13e\n",
									lmk,d.toString(),t,t,i+1,t,j+1,(1-_obsMod.getLandmarkObservation(i, j, lmk, d.ordinal())));
						}
					}
				}
			}
		}
		out.close();
	}
}
