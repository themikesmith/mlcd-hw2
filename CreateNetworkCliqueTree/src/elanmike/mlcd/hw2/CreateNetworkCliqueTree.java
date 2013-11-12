package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import elanmike.mlcd.hw2.Constants.DIR;

public class CreateNetworkCliqueTree {
	private static int _biggestRow, _biggestCol, _biggestTimeStep, _numLandmarks;
	private static Clique _lastAction;
	private static boolean _debug = false;
	/**
	 * Small clique class that holds a list of variables
	 * We compare cliques by their number of shared variables
	 * @author mcs
	 *
	 */
	private static class Clique {
		private List<String> _variables;
		Clique() {
			_variables = new ArrayList<String>();
		}
		void addVariable(String var) {
			_variables.add(var);
		}
		/**
		 * @param other the other clique
		 * @return the number of variables this clique has in common with the other
		 */
		int getCardinalityOfIntersectionWith(Clique other) {
			Set<String> intersection = new HashSet<String>(this._variables);
			intersection.retainAll(other._variables);
			return intersection.size();
		}
		/**
		 * A clique is equal to another clique if their sets of variables are equal
		 */
		@Override
		public boolean equals(Object other) {
			if(other instanceof Clique) {
				Clique c = (Clique) other;
				return c._variables.equals(this._variables);
			}
			return false;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(String var : _variables) {
				sb.append(var).append(',');
			}
			if(_variables.size() > 0) sb.deleteCharAt(sb.length()-1); // delete final comma
			return sb.toString();
		}
	}
	private static class Edge {
		public static final String EDGE = " -- ";
		Clique _one, _two;
		int _weight; // number of variables one has in common with two
		Edge(Clique one, Clique two, int weight) {
			this._one = one;
			this._two = two;
			this._weight = weight;
		}
		/**
		 * An edge is equal to another edge if its two vertices match the other's two
		 */
		@Override
		public boolean equals(Object other) {
			if(other instanceof Edge) {
				Edge e = (Edge) other;
				return e._one.equals(this._one) && e._two.equals(this._two)
					|| e._one.equals(this._two) && e._two.equals(this._one);
			}
			return false;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(_one.toString());
			if(_debug) sb.append(" --").append(_weight).append("-- ");
			else sb.append(EDGE);
			sb.append(_two.toString());
			return sb.toString();
		}
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
			return -1 * new Integer(e1._weight).compareTo(new Integer(e2._weight));
		}
	}
	private static class Tree {
		private Set<Clique> _vertices;
		private Set<Edge> _edges;
		Tree() {
			_vertices = new HashSet<Clique>();
			_edges = new HashSet<Edge>();
		}
		/**
		 * Adds the edge to the tree.
		 * Checks for duplicates edges
		 * @param e
		 */
		void addEdge(Edge e) {
			_edges.add(e);
			_vertices.add(e._one);
			_vertices.add(e._two);
		}
		public String toString() {
			StringBuilder sb = new StringBuilder("Vertices:\n");
			for(Clique c : _vertices) {
				sb.append(c).append("\n");
			}
			sb.append("Edges:\n");
			for(Edge e : _edges) {
				sb.append(e).append("\n");
			}
			return sb.toString();
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
		List<Clique> maximalCliques;
		if(_debug) {
			System.out.println("debug with 10.8 example");
			// debug with 10.8 example
			maximalCliques = debugMaximalCliques();	
		}
		else {
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
			maximalCliques = assembleMaximalCliques();
		}
//		if(_debug) System.out.println(maximalCliques);
		// now that we have our list of maximal cliques, assemble cluster graph
		List<Edge> clusterGraphEdges = assembleClusterGraphEdges(maximalCliques);
//		if(_debug) System.out.println(clusterGraphEdges);
		// sort the edges in descending order by weight
		Collections.sort(clusterGraphEdges, new EdgeComparator());
		// create the tree!
		Tree cliqueTree = createMaximalSpanningTree(clusterGraphEdges, maximalCliques.size());
		if(_debug) System.out.println(cliqueTree);
		if(cliqueTree._vertices.size() != maximalCliques.size()) {
			System.err.printf("error when making tree! V:%d N:%d\n", cliqueTree._vertices.size(), maximalCliques.size());
			return;
		}
		// and print to file.
		try {
			printTreeToFile(cliqueTree, args[1]);
		} catch (IOException e) {
			System.err.println("error writing clique file:"+args[1]);
			e.printStackTrace();
			return;
		}
		System.out.println("wrote clique to:"+args[1]);
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
		_numLandmarks = 0;
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
	private static List<Clique> assembleMaximalCliques() {
		List<Clique> maximalCliques = new ArrayList<Clique>();
		// forms of cliques:
		for(int t = 0; t <= _biggestTimeStep; t++) {
			// row n, col n, observation o n
			for(DIR d : DIR.values()) {
				// row t, col t, observe wall d t
				Clique c = new Clique();
				c.addVariable("PositionRow_"+t);
				c.addVariable("PositionCol_"+t);
				c.addVariable("ObserveWall_"+d.toString()+"_"+t);
				maximalCliques.add(c);
				for(int l = 1; l <= _numLandmarks; l++) {
					// row t, col t, observe landmark L d t
					c = new Clique();
					c.addVariable("PositionRow_"+t);
					c.addVariable("PositionCol_"+t);
					c.addVariable("ObserveLandmark"+l+"_"+d.toString()+"_"+t);
					maximalCliques.add(c);
				}
			}
			// motion model:
			// if time step not last one
			if(t != _biggestTimeStep) {
				// use cliques size 5 - too big, but at recommendation of suchi
				Clique c = new Clique();
				c.addVariable("PositionRow_"+t);
				c.addVariable("PositionCol_"+t);
				c.addVariable("PositionRow_"+(t+1));
				c.addVariable("PositionCol_"+(t+1));
				c.addVariable("Action_"+t);
				maximalCliques.add(c);
				
//				// cliques size 4
//				// at each time step we have 
//				// blue:row n, row n+1, col n+1, action n
//				Clique c = new Clique();
//				c.addVariable("PositionRow_"+t);
//				c.addVariable("PositionRow_"+(t+1));
//				c.addVariable("PositionCol_"+(t+1));
//				c.addVariable("Action_"+t);
//				maximalCliques.add(c);
//				// green:row n, col n, action n, col n+1
//				c = new Clique();
//				c.addVariable("PositionRow_"+t);
//				c.addVariable("PositionCol_"+t);
//				c.addVariable("Action_"+t);
//				c.addVariable("PositionCol_"+(t+1));
//				maximalCliques.add(c);
			}
			else { // last time step
				// action t
				_lastAction = new Clique();
				_lastAction.addVariable("Action_"+t);
				maximalCliques.add(_lastAction);
			}
		}
		return maximalCliques;
	}
	/**
	 * Now that we have our list of maximal cliques,
	 * assemble our cluster graph's list of edges.
	 */
	private static List<Edge> assembleClusterGraphEdges(List<Clique> maximalCliques) {
		List<Edge> clusterGraphEdges = new ArrayList<Edge>();
		// for each possible pair of cliques...
		Iterator<Clique> iIter = maximalCliques.iterator();
		int i = 0;
		while(iIter.hasNext()) {
			Clique one = iIter.next();
			i++;
			Iterator<Clique> jIter = maximalCliques.iterator();
			for(int j = 0; j < i; j++) jIter.next();
			while(jIter.hasNext()) {
				Clique two = jIter.next();
				if(!one.equals(two)) { // exclude edges between identical cliques 
					int weight = one.getCardinalityOfIntersectionWith(two);
					Edge e = new Edge(one, two, weight);
					if(weight > 0) {  // add an edge if one has things in common with two
						if(!clusterGraphEdges.contains(e)) { // and if the edge isn't already there
							clusterGraphEdges.add(e);
						}
					}
				}
				else {
					System.err.println("uh oh!"); 
				}
			}
		}
		return clusterGraphEdges;
	}
	/**
	 * Creates the maximal spanning tree given our sorted edge list.
	 * Kruskal's algorithm, slightly modified to return a tree that isn't fully connected
	 * if we run out of edges, instead of returning an error.
	 * 
	 * Assumes our list of edges is sorted in decreasing order by weight.
	 */
	private static Tree createMaximalSpanningTree(List<Edge> _clusterGraphEdges, int numCliques) {
		Tree cliqueTree = new Tree();
		// add every clique to the tree
		// in our special case the only clique that won't get added when adding
		// edges is the last action
		if(!_debug) cliqueTree._vertices.add(_lastAction);
		// add first edge
		cliqueTree.addEdge(_clusterGraphEdges.get(0));
		// try to add n-2 edges without cycles - we know that one vertex isn't connected
		int i = 1;
		// (assuming we will get edges in order of decreasing weight)
		while(cliqueTree._edges.size() < numCliques - 1) {
			if(i >= _clusterGraphEdges.size()) { // we've run out of edges to add
				return cliqueTree; // stop, return, even though the tree might not be fully connected
			}
			else {
				Edge e = _clusterGraphEdges.get(i);
				// if at least one vertex in e is not already in T...
				if(!cliqueTree._vertices.contains(e._one)
						|| !cliqueTree._vertices.contains(e._two)) {
					cliqueTree.addEdge(e); // ... we add!
				} // otherwise the edge would be a duplicate or create a cycle
				i++;
			}
		}
		return cliqueTree;
	}
	/**
	 * Prints our tree to file.
	 * The first line contains the number of cliques |V| in the tree.
	 * Each of the following |V| lines contains a clique specification.
	 * Each of the following lines contains one edge.
	 * 'cluster1 -- cluster2'
	 * @param filename the desired output filename
	 * @throws IOException 
	 */
	private static void printTreeToFile(Tree cliqueTree, String filename) throws IOException {
		// create and open the cpd file
		File outfile = new File(filename);
		if(outfile.exists()) {
			outfile.delete();
		}
		outfile.createNewFile();
		PrintWriter out = new PrintWriter(outfile);
		// the first line contains the number of cliques V in the tree.
		out.println(cliqueTree._vertices.size());
		// each of the following V lines contains a clique specification
		for(Clique c : cliqueTree._vertices) {
			out.println(c);
		}
		// rest of the lines contain edges
		for(Edge e : cliqueTree._edges) {
			out.println(e);
		}
		out.close();
	}
	private static List<Clique> debugMaximalCliques() {
		List<Clique> cliques = new ArrayList<Clique>();
		Clique c = new Clique();
		c.addVariable("C");
		c.addVariable("D");
		cliques.add(c);
		c = new Clique();
		c.addVariable("I");
		c.addVariable("G");
		c.addVariable("D");
		cliques.add(c);
		c = new Clique();
		c.addVariable("G");
		c.addVariable("H");
		cliques.add(c);
		c = new Clique();
		c.addVariable("G");
		c.addVariable("S");
		c.addVariable("L");
		cliques.add(c);
		c = new Clique();
		c.addVariable("G");
		c.addVariable("I");
		c.addVariable("S");
		cliques.add(c);
		c = new Clique();
		c.addVariable("S");
		c.addVariable("L");
		c.addVariable("J");
		cliques.add(c);
		return cliques;
	}
}
