package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
	// 1 group: row or col
	private static final String _regexPosition = "Position(Row|Col)\\d+";
	// 1 group: direction
	private static final String _regexObserveWall = "ObserveWall_(N|S|E|W)_\\d+";
	// 2 groups: landmark number, direction 
	private static final String _regexObserveLandmark = "ObserveLandmark(\\d+)_(N|S|E|W)_\\d+";
	private int _numLandmarks, _i, _j;
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
	 * - ?? what else ??
	 * 
	 * @param networkFilename the name of the network file
	 * @throws IOException if cannot find the network file, or can't read a line in the file
	 * @throws NumberNotFoundException if cannot format the first number
	 */
	public void read(String networkFilename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(networkFilename));
		String line;
		// on the first line is the number of following lines that describe variables
		int numVariables = -1;
		// after the first line, we either are reading variables, or edges.
		boolean readingVariables = true;
		while ((line = br.readLine()) != null) {
			if(numVariables == -1) { // set the number of variables
				numVariables = new Integer(line); // throws number format exception
			}
			else if(readingVariables) {
				// read variable
				String[] varInfo = line.split("\\s");
				String varName = varInfo[0];
				String[] varValues = varInfo[1].split(",");
				// TODO if it's a position row, take max value for I
				// TODO if it's a position col, take max value for J
				// TODO if it's observe landmark, take max value for N
				numVariables--; // and decrement our number left to read
			}
			else { // reading edges
				
			}
		}
		br.close();
	}

	public void train(String string) {
		// TODO Auto-generated method stub
		
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
	 * @param string the cpd filename (to be created / overwritten)
	 */
	public void writeCPD(String string) {
		// TODO Auto-generated method stub
		
	}

}
