package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import elanmike.mlcd.hw2.Constants.DIR;

public class CreateNetworkCliqueTree {
	private static int _biggestRow, _biggestCol, _biggestTimeStep, _numLandmarks;
	private static List<Clique> _maximalCliques;
	private static List<Edge> _clusterGraphEdges;
	/**
	 * Small clique class that holds a list of variables
	 * We compare cliques by their number of shared variables
	 * @author mcs
	 *
	 */
	private static class Clique {
		private Set<String> variables;
		Clique() {
			variables = new HashSet<String>();
		}
		Clique(String... variables) {
			this.variables = new HashSet<String>(Arrays.asList(variables));
		}
		void addVariable(String var) {
			variables.add(var);
		}
		Set<String> getVariables() {
			return variables;
		}
		/**
		 * @param other theo ther clique
		 * @return the number of variables this clique has in common with the other
		 */
		int getCardinalityOfIntersectionWith(Clique other) {
			Set<String> intersection = new HashSet<String>(this.variables);
			intersection.retainAll(other.variables);
			return intersection.size();
		}
	}
	private class Edge {
		Clique one, two;
		int weight; // number of variables one has in common with two
		Edge(Clique one, Clique two, int weight) {
			this.one = one;
			this.two = two;
			this.weight = weight;
		}
		Clique getOne() {return one;}
		Clique getTwo() {return two;}
		int getWeight() {return weight;}
	}
	/**
	 * Creating a comparator of edges so can call Collections.sort(list of edges)
	 * The comparison of one edge to another is by their weights,
	 * but in DESCENDING order, so we negate the result of the standard 
	 * integer comparison
	 * @author mcs
	 *
	 */
	private static class EdgeComparator implements Comparator<Edge> {
		@Override
		public int compare(Edge e1, Edge e2) {
			return -1 * new Integer(e1.weight).compareTo(new Integer(e2.weight));
		}
	}
	/**
	 * This script reads a network file,
	 * and uses sufficient statistics and our domain knowledge
	 * to create a clique tree for that network. 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 2) {
			usage();
			return;
		}
		// read network file
		try {
			read(args[0]);
		} catch (IOException e) {
			System.err.println("error reading network file:"+args[0]);
			e.printStackTrace();
			return;
		}
		System.out.println("read network from:"+args[0]);
		// given sufficient statistics, assemble list of maximal cliques
		assembleMaximalCliques();
		// now that we have our list of maximal cliques, assemble cluster graph
		assembleClusterGraphEdges();
		// sort the edges in descending order by weight
		Collections.sort(_clusterGraphEdges, new EdgeComparator());
		// create the tree!
		createMaximalSpanningTree();
		// and print to file.
		printTreeToFile(args[1]);
		// done! yay.
	}
	private static void usage() {
		System.err.println("usage: pass 2 arguments. arg[0] = network_file arg[1] = clique_output_file");
	}
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
	private static void read(String networkFilename) throws IOException {
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
	 * Now that we have our sufficient statistics from the network,
	 * assemble the list of maximal cliques.
	 */
	private static void assembleMaximalCliques() {
		_maximalCliques = new ArrayList<Clique>();
		// forms of cliques:
		for(int t = 0; t <= _biggestTimeStep; t++) {
			for(DIR d : DIR.values()) {
				// row t, col t, observe wall t d
				for(int l = 0; l < _numLandmarks; l++) {
					// row t, col t, observe landmark L t d		
				}
			}
			// row t, col t, action t
			// row t+1, col t+1, action i
			// row t, row t+1, action i
			// col t, col t+1, action i
			// row t, col t, col t+1
			// row t, action t, col t+1
			// row t, row t+1, col t+1
		}
	}
	/**
	 * Now that we have our list of maximal cliques,
	 * assemble our cluster graph's list of edges.
	 */
	private static void assembleClusterGraphEdges() {
		
	}
	/**
	 * Creates the maximal spanning tree given our sorted edge list
	 */
	private static void createMaximalSpanningTree() {
		
	}
	/**
	 * Prints our tree to file
	 * @param filename the desired output filename
	 */
	private static void printTreeToFile(String filename) {
		
	}
}
