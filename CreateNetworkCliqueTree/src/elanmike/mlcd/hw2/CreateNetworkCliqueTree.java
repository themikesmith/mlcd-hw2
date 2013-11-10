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
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import elanmike.mlcd.hw2.Constants.DIR;

public class CreateNetworkCliqueTree {
	private static int _biggestRow, _biggestCol, _biggestTimeStep, _numLandmarks;
	private static ArrayList<Clique> _maximalCliques;
	private static List<Edge> _clusterGraphEdges;
	private static Tree _cliqueTree;
	/**
	 * Small clique class that holds a list of variables
	 * We compare cliques by their number of shared variables
	 * @author mcs
	 *
	 */
	private static class Clique {
		private Set<String> _variables;
		Clique() {
			_variables = new HashSet<String>();
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
			sb.deleteCharAt(sb.length()-1); // delete final comma
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
			sb.append(_one.toString()).append(EDGE).append(_two.toString());
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
			if(!_edges.contains(e)) {
				_vertices.add(e._one);
				_vertices.add(e._two);
				_edges.add(e);
			}
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
		try {
			printTreeToFile(args[1]);
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
				// row t, col t, observe wall d t
				Clique c = new Clique();
				c.addVariable("PositionRow_"+t);
				c.addVariable("PositionCol_"+t);
				c.addVariable("ObserveWall_"+d.toString()+"_"+t);
				_maximalCliques.add(c);
				for(int l = 1; l <= _numLandmarks; l++) {
					// row t, col t, observe landmark L d t
					c = new Clique();
					c.addVariable("PositionRow_"+t);
					c.addVariable("PositionCol_"+t);
					c.addVariable("ObserveLandmark"+l+"_"+d.toString()+"_"+t);
					_maximalCliques.add(c);
				}
			}
			// if time step not last one
			if(t != _biggestTimeStep) {
				// row t, col t, action t
				Clique c = new Clique();
				c.addVariable("PositionRow_"+t);
				c.addVariable("PositionCol_"+t);
				c.addVariable("Action_"+t);
				_maximalCliques.add(c);
				// row t+1, col t+1, action t
				c = new Clique();
				c.addVariable("PositionRow_"+(t+1));
				c.addVariable("PositionCol_"+(t+1));
				c.addVariable("Action_"+t);
				_maximalCliques.add(c);
				// row t, row t+1, action t
				c = new Clique();
				c.addVariable("PositionRow_"+t);
				c.addVariable("PositionRow_"+(t+1));
				c.addVariable("Action_"+t);
				_maximalCliques.add(c);
				// col t, col t+1, action t
				c = new Clique();
				c.addVariable("PositionCol_"+t);
				c.addVariable("PositionCol_"+(t+1));
				c.addVariable("Action_"+t);
				_maximalCliques.add(c);
				// row t, col t, col t+1
				c = new Clique();
				c.addVariable("PositionRow_"+t);
				c.addVariable("PositionCol_"+t);
				c.addVariable("PositionCol_"+(t+1));
				_maximalCliques.add(c);
				// row t, action t, col t+1
				c = new Clique();
				c.addVariable("PositionRow_"+t);
				c.addVariable("Action_"+t);
				c.addVariable("PositionCol_"+(t+1));
				_maximalCliques.add(c);
				// row t, row t+1, col t+1
				c = new Clique();
				c.addVariable("PositionRow_"+t);
				c.addVariable("PositionRow_"+(t+1));
				c.addVariable("PositionCol_"+(t+1));
				_maximalCliques.add(c);
			}
			else { // last time step
				// action t
				Clique c = new Clique();
				c.addVariable("Action_"+t);
				_maximalCliques.add(c);
			}
		}
	}
	/**
	 * Now that we have our list of maximal cliques,
	 * assemble our cluster graph's list of edges.
	 */
	private static void assembleClusterGraphEdges() {
		_clusterGraphEdges = new ArrayList<Edge>();
		int n = _maximalCliques.size();
		// for each possible pair of cliques...
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if(i != j) { // exclude edges between identical cliques 
					Clique one = _maximalCliques.get(i), two = _maximalCliques.get(j);
					int weight = one.getCardinalityOfIntersectionWith(two);
					if(weight > 0) { // add an edge if they have things in common
						_clusterGraphEdges.add(new Edge(one, two, weight));
					}
				}
			}
		}
	}
	/**
	 * Creates the maximal spanning tree given our sorted edge list.
	 * Kruskal's algorithm, slightly modified to return a tree that isn't fully connected
	 * if we run out of edges, instead of returning an error.
	 * 
	 * Assumes our list of edges is sorted in decreasing order by weight.
	 */
	private static void createMaximalSpanningTree() {
		_cliqueTree = new Tree();
		_cliqueTree.addEdge(_clusterGraphEdges.get(0)); // add first edge
		int i = 1;
		// try to add n-1 edges without cycles
		// (assuming we will get edges in order of decreasing weight)
		while(i < _maximalCliques.size() - 1) {
			if(i >= _clusterGraphEdges.size()) { // we've run out of edges to add
				return; // stop, return, even though the tree might not be fully connected
			}
			else {
				Edge e = _clusterGraphEdges.get(i);
				// if at least one vertex in e is not already in T...
				if(!_cliqueTree._vertices.contains(e._one)
						|| !_cliqueTree._vertices.contains(e._two)) {
					_cliqueTree.addEdge(e); // ... we add!
				} // otherwise the edge would be a duplicate or create a cycle
			}
			i++;
		}
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
	private static void printTreeToFile(String filename) throws IOException {
		// create and open the cpd file
		File outfile = new File(filename);
		if(outfile.exists()) {
			outfile.delete();
		}
		outfile.createNewFile();
		PrintWriter out = new PrintWriter(outfile);
		// the first line contains the number of cliques V in the tree.
		out.println(_cliqueTree._vertices.size());
		// each of the following V lines contains a clique specification
		for(Clique c : _cliqueTree._vertices) {
			out.println(c);
		}
		// rest of the lines contain edges
		for(Edge e : _cliqueTree._edges) {
			out.println(e);
		}
		out.close();
	}
}
