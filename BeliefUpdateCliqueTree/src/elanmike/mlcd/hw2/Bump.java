package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Belief Update Message Passing class
 * 
 * @author mcs
 *
 */
public class Bump {
	public static final int NO_EVIDENCE = -1;
	private static int nextVertexID = 0;
	/**
	 * Small clique class that holds a list of variables
	 * We compare cliques by their number of shared variables
	 * @author mcs
	 *
	 */
	private class Clique extends Factor {
		
		Clique(String[] varNames) {
			super(varNames);
		}
		void addVariable(int var) {
			_variables.add(var);
		}
		/**
		 * @param other the other clique
		 * @return the number of variables this clique has in common with the other
		 */
		int getCardinalityOfIntersectionWith(Clique other) {
			Set<Integer> intersection = new HashSet<Integer>(this._variables);
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
			for(Integer var : _variables) {
				sb.append(var).append(',');
			}
			sb.deleteCharAt(sb.length()-1); // delete final comma
			return sb.toString();
		}
	}
	private class Vertex extends Clique{
		private Integer _orderID;

		private Set<Edge> _neighborEdges, _incomingEdges, _outgoingEdges;
		private Map<Edge, Boolean> _recvdMsgStatus;
		private boolean _isInformed, _onUpwardPass;
		Vertex(String[] varsContained){
			super(varsContained);
			_neighborEdges = new TreeSet<Edge>();
			_incomingEdges = new TreeSet<Edge>();
			_outgoingEdges = new TreeSet<Edge>();
			_orderID = nextVertexID++;
			_recvdMsgStatus = new HashMap<Edge, Boolean>();
			_isInformed = false;
			_onUpwardPass = false;
		}
		int getOrderID() {return _orderID;}
		void setOrderID(int o) {this._orderID = o;}
		Set<Edge> getNeighborEdges() {return _neighborEdges;}
		/**
		 * Adds a neighbor.
		 * checks for duplicates.
		 * @param v
		 */
		void addNeighborEdge(Edge e) {
			if(_recvdMsgStatus.containsKey(e)) {
				_recvdMsgStatus.put(e, false);
				_neighborEdges.add(e);
			}
		}
		/**
		 * Sends a message to its neighbor.
		 * Calls the neighbor's onReceiveMessage method
		 * @param recvingNeighbor
		 */
		void sendMessage(Edge recvingNeighborEdge) {
			// TODO compose message
			Factor message = null;
			recvingNeighborEdge.getOtherVertex(this).
				onReceiveMessage(recvingNeighborEdge, message);
		}
		/**
		 * When we receives a message...
		 * @param sendingNeighborEdge
		 * @param message
		 */
		void onReceiveMessage(Edge sendingNeighborEdge, Factor message) {
			// TODO process message
			Vertex v = sendingNeighborEdge.getOtherVertex(this);
			_recvdMsgStatus.put(sendingNeighborEdge, v._isInformed);
			if(v._isInformed) { // if we've altered our state, recheck
				_isInformed = isInformed();
			} // else don't bother, as we know it's impossible
		}
		/**
		 * @return a list of all incoming neighbors, as defined by our stream direction.
		 */
		Set<Edge> getIncomingNeighborEdges() {
			// only compute if we have to
			if(_incomingEdges.size() == 0 || this._onUpwardPass != _bumpOnUpwardPass) {
				Set<Edge> incomingEdges = new TreeSet<Edge>();
				Iterator<Edge> it = _neighborEdges.iterator();
				while(it.hasNext()) {
					Edge e = it.next();
					Vertex v = e.getOtherVertex(this);
					if(_onUpwardPass) { // check order id is less than this order id
						if(this._orderID < v._orderID) {
							incomingEdges.add(e);
						}
					}
					else { // check order id is greater than this order id
						if(this._orderID > v._orderID) {
							incomingEdges.add(e);
						}
					}
				}
				_incomingEdges = incomingEdges;
			}
			return _incomingEdges;
		}
		/**
		 * @return a list of all outgoing neighbors, as defined by our stream direction.
		 */
		Set<Edge> getOutgoingNeighborEdges() {
			// only compute if we have to
			if(_outgoingEdges.size() == 0 || this._onUpwardPass != _bumpOnUpwardPass) {
				Set<Edge> outgoingEdges = new TreeSet<Edge>();
				Iterator<Edge> it = _neighborEdges.iterator();
				while(it.hasNext()) {
					Edge e = it.next();
					Vertex v = e.getOtherVertex(this);
					if(_onUpwardPass) { // check order id is less than this order id
						if(this._orderID < v._orderID) {
							outgoingEdges.add(e);
						}
					}
					else { // check order id is greater than this order id
						if(this._orderID > v._orderID) {
							outgoingEdges.add(e);
						}
					}
				}
				_outgoingEdges = outgoingEdges;
			}
			return _outgoingEdges;
		}
		/**
		 * Checks if this vertex is informed.
		 * @return true if has received informed messages from all neighbors
		 */
		boolean isInformed() {
			// default to true, to account for the case where we have no neighbors
			for(boolean b : _recvdMsgStatus.values()) {
				// if any value is false, we return false
				if(!b) return false; 
			}
			return true;
		}
		@Override
		public boolean equals(Object o) {
			if(o instanceof Vertex) {
				Vertex v = (Vertex) o;
				return v._orderID == this._orderID;
			}
			return false;
		}
	}
	private class Edge extends Factor{
		public static final String EDGE = " -- ";
		Vertex _one, _two;
		//Factor _mu;
		//Set<Integer> _sepset; 
		int _weight; // number of variables one has in common with two
		
		Edge(Vertex one, Vertex two, int weight) {
			super(one.intersection(two._variables));
			this._one = one;
			this._two = two;
			this._weight = weight;
		}
		
		/*
		Edge(Vertex one, Vertex two, int weight) {
			this._one = one;
			this._two = two;
			this._weight = weight;
			this._sepset = new HashSet<Integer>(one._variables);
			_sepset.retainAll(two._variables);
		}
		void setBelief(Factor f) {this._mu = f;}
		Factor getBelief() {return _mu;}
		*/
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
			if(DEBUG) sb.append(" --").append(_weight).append("-- ");
			else sb.append(EDGE);
			sb.append(_two.toString());
			return sb.toString();
		}
		/**
		 * Given a vertex v, if this edge contains v, get the other vertex in the edge.
		 * else return null, if this edge doesn't contain v
		 * @param v
		 * @return
		 */
		Vertex getOtherVertex(Vertex v) {
			if(v.equals(_one)) return _two;
			else if(v.equals(_two)) return _one;
			else return null;
		}
	}
	private class Tree {
		private HashMap<String,Vertex> _vertices;
		private HashMap<String,Edge> _edges;
		Tree() {
			_vertices = new HashMap<String,Vertex>();
			_edges = new HashMap<String,Edge>();
		}
		/*
		Tree(Set<Vertex> vs, Set<Edge> es) {
			this._vertices = vs;
			this._edges = es;
		}*/
		void addVertex(Vertex v) {
			_vertices.put(v._variables.toString(),v);
			//System.out.println("adding vertice with key: "+ v._variables.toString());
		}
		/**
		 * Adds the edge to the tree.
		 * Checks for duplicates edges
		 * @param e
		 */
		void addEdge(Edge e) {
			if(!_edges.containsKey(e._variables.toString())) {
				//_vertices.put(e._one._variables.toString(),e._one);
				//_vertices.put(e._two._variables.toString(),e._two);
				_edges.put(e._variables.toString(),e);
			}
		}
		Tree makeCopy() {
			Tree t = new Tree();
			//TODO make deep copy
			return null;
		}
		
		public String toString(){
			String output = "=== Verticies ===\n";
			Set<String> keys = _vertices.keySet();
			for(String k:keys){
				output += _vertices.get(k)._variables.toString()+"\n";
			}
			output += "=== Edges ===\n";
			keys = _edges.keySet();
			for(String k:keys){
				output += _edges.get(k)._variables.toString()+"\n";
			}
			return output;
		}
	}
	private static final boolean DEBUG = true;
	/**
	 * true if we're on the upward pass, 
	 * if we're going in increasing order id. 
	 * false otherwise
	 */
	private boolean _bumpOnUpwardPass;
	private Tree _tree, _queryTree;
	private ArrayList<Vertex> _orderedVertices;
	private boolean _useSumProduct;
	
	/**
	 * A map from variable name to variable value, transformed into integers
	 */
	Map<Integer, Integer> _queryContexts;
	Bump() {
		_tree = new Tree();
		_bumpOnUpwardPass = false;
		_queryContexts = new HashMap<Integer, Integer>();
		_orderedVertices = new ArrayList<Vertex>();
		_useSumProduct = true;
	}
	/**
	 * Sets the value of use sum product.
	 * Note that after changing this, one needs to call runBump again
	 * to recalibrate the tree.
	 * @param useSumProduct true if use sum product, false if use max product
	 */
	void setUseSumProduct(boolean useSumProduct) {
		_useSumProduct = useSumProduct;
	}
	/**
	 * Runs belief update message passing to calibrate the tree.
	 * Resets, assigns an ordering, inits beliefs, calibrates the tree.
	 */
	public void runBump() {
		assignOrderingAndInitBeliefs();
		calibrateTree();
	}
	/**
	 * Run DFS to init ordering.
	 * Initialize beliefs at each vertex of the tree
	 */
	void assignOrderingAndInitBeliefs() {
		_orderedVertices.clear();
		//TODO implement assign ordering
		// giving a number is equivalent to adding to ordering, giving index
		// choose root
		// while all vertices don't have a number
		// 		depth first search from root, assigning numbers and init'ing beliefs
		// 		if DFS ends before all vertices have numbers,
		// 		choose another root, repeat
	}
	/**
	 * Calibrate the tree with two passes of belief-update message passing.
	 * Begin with our ordering.
	 */
	void calibrateTree() {
		_bumpOnUpwardPass = false;
		for(int i = 0; i < _orderedVertices.size(); i++) {
			Vertex v = _orderedVertices.get(i);
			for(Edge e : v.getOutgoingNeighborEdges()) {
				v.sendMessage(e);
			}
		}
		_bumpOnUpwardPass = true;
		for(int i = _orderedVertices.size() - 1; i >= 0; i--) {
			Vertex v = _orderedVertices.get(i);
			for(Edge e : v.getOutgoingNeighborEdges()) {
				v.sendMessage(e);
			}
		}
	}
	/**
	 * Conducts one downward pass of belief update message passing
	 * with a designated root.
	 * Conducts depth-first search of the query tree, sends messages in that order.
	 * 
	 * @param root
	 * @return a stack of the reverse order of this pass, for the upward pass.
	 */
	Stack<Integer> downwardPassBeliefUpdateQuery(Vertex root) {
		_bumpOnUpwardPass = false;
		Tree t = _queryTree;
		//TODO implement DFS in QUERY COPY
		return null;
	}
	/**
	 * Resets the query tree in preparation for queries
	 */
	void resetTreeForQueries() {
		_queryTree = _tree.makeCopy();
		_queryContexts = new HashMap<Integer, Integer>();
	}
	/**
	 * find a vertex with a clique containing the given set of variables
	 * in the QUERY TREE
	 * @param vars
	 * @return vertex if found, null otherwise.  should always return a vertex.
	 */
	Vertex findVertexQuery(int... vars) {
		Tree t = _queryTree;
		for(Vertex v : t._vertices.values()) {
			boolean containsAll = true;
			for(int i : vars) {
				if(!v._variables.contains(i)) {
					containsAll = false;
					//continue; // skip.
				}
			}
			if(containsAll) return v;
		}
		return null;
	}
	/**
	 * find a vertex with a clique containing the given variable
	 * in the QUERY TREE
	 * @param var if found, null if not found
	 * @return vertex if found, null otherwise.  should always return a vertex.
	 */
	Vertex findVertexQuery(String var) {
		Tree t = _queryTree;
		for(Vertex v : t._vertices.values()) {
			if(v._variables.contains(var)) {
				return v;
			}
		}
		return null;
	}
	/**
	 * Given that our tree is calibrated, incorporate the evidence
	 * into our QUERY COPY
	 * 
	 * @param pairs list of Pair<Integer, Integer>... pairs pairs of variable=value
	 */
	void incorporateQueryEvidence(int varInt, int varValue) {
		// Find a clique with the variable, C
		Vertex newRoot = findVertexQuery(varInt);
		if(newRoot == null) {
			System.err.println("can't find vertex - whoops!");
		}
		// multiply in a new indicator factor
		// TODO multiply in new indicator
		// conduct one pass of B-U with C as the root
		downwardPassBeliefUpdateQuery(newRoot);
	}
	String query(String[] lhs, String[] contexts, boolean useSumProduct) {
		if(useSumProduct != _useSumProduct) {
			System.err.println("oops! not ready.");
			// set appropriate method, and run bump to calibrate
			setUseSumProduct(useSumProduct);
			runBump();
		}
		if(useSumProduct) return querySumProduct(lhs, contexts);
		else return queryMaxProduct(lhs, contexts);
	}
	/**
	 * queries the structure for p(lhs|contexts)
	 */
	String querySumProduct(String[] lhs, String[] contexts) {
		// check if evidence is incremental or retractive
		boolean retractive = false;
		// then take action
		// check number of variables
		if(contexts.length < _queryContexts.size()) {
			// retractive -- less evidence than before. reset and treat as incremental
			retractive = true;
		}
		if(!retractive) {
			for(String s : contexts) {
				String[] varValue = s.split("=");
				String var = varValue[0], value = varValue[1];
				int varInt = Factor.getVariableIndex(var),
					valueInt = Factor.getVariableValueIndex(varInt, value);
				if(_queryContexts.containsKey(varInt) && _queryContexts.get(varInt) != valueInt) {
					// query context variable has other value. reset.
					retractive = true;
					break;
				}
			}
		}
		if(retractive) resetTreeForQueries();
		for(String s : contexts) {
			String[] varValue = s.split("=");
			String var = varValue[0], value = varValue[1];
			int varInt = Factor.getVariableIndex(var),
				valueInt = Factor.getVariableValueIndex(varInt, value);
			if(!_queryContexts.containsKey(varInt)) {
				// additional evidence - we've never seen it before
				incorporateQueryEvidence(varInt, varInt);
			}
			else if(_queryContexts.get(varInt) == valueInt) {
				// already have this context variable = value pair, do nothing
			}
			else { // query context variable has other value. reset.
				// we've already reset so this should never occur
				System.err.println("this should never occur. investigate handling of retractive evidence.");
			}
		}
		for(String s : lhs) {
			String[] varValue = s.split("=");
			String var = varValue[0], value = varValue[1];
			int varInt = Factor.getVariableIndex(var),
				valueInt = Factor.getVariableValueIndex(varInt, value);
			if(valueInt != NO_EVIDENCE) {
				// TODO conduct query
			}
		}
		return null;
	}
	/**
	 * queries the structure for p(lhs|contexts)
	 */
	String queryMaxProduct(String[] lhs, String[] contexts) {
		// TODO implement query max product = maximum likelihood
		// check if evidence is incremental or retractive
		boolean retractive = false;
		// then take action
		// check number of variables
		if(contexts.length < _queryContexts.size()) {
			// retractive -- less evidence than before. reset and treat as incremental
			retractive = true;
		}
		if(!retractive) {
			for(String s : contexts) {
				String[] varValue = s.split("=");
				String var = varValue[0], value = varValue[1];
				int varInt = Factor.getVariableIndex(var),
					valueInt = Factor.getVariableValueIndex(varInt, value);
				if(_queryContexts.containsKey(varInt) && _queryContexts.get(varInt) != valueInt) {
					// query context variable has other value. reset.
					retractive = true;
					break;
				}
			}
		}
		if(retractive) resetTreeForQueries();
		for(String s : contexts) {
			String[] varValue = s.split("=");
			String var = varValue[0], value = varValue[1];
			int varInt = Factor.getVariableIndex(var),
					valueInt = Factor.getVariableValueIndex(varInt, value);
			if(!_queryContexts.containsKey(varInt)) {
				// additional evidence - we've never seen it before
				incorporateQueryEvidence(varInt, varInt);
			}
			else if(_queryContexts.get(varInt) == valueInt) {
				// already have this context variable = value pair, do nothing
			}
			else { // query context variable has other value. reset.
				// we've already reset so this should never occur
			}
		}
		for(String s : lhs) {
			String[] varValue = s.split("=");
			String var = varValue[0], value = varValue[1];
			int varInt = Factor.getVariableIndex(var),
				valueInt = Factor.getVariableValueIndex(varInt, value);
			if(valueInt != NO_EVIDENCE) {
				// TODO conduct query
			}
		}
		return null;
	}

	/**
	 * Reads in the tree from the clique file.
	 * Builds the tree
	 * @param cliqueTreeFilename
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public void readCliqueTreeFile(String cliqueTreeFilename) 
			throws IOException, NumberFormatException {
		BufferedReader br = new BufferedReader(new FileReader(cliqueTreeFilename));
		String line;
		
		// on the first line is the number of following lines that describe vertices
		int numCliques = -1;
		if((line = br.readLine()) != null){
			numCliques = Integer.valueOf(line);
		}else{
			br.close();
			throw new IOException();
		}
		//num Cliques
		if(numCliques<0) {
			br.close();
			throw new NumberFormatException();
		}
		//cliques
		for(int i = 0;i<numCliques;i++){
			if((line = br.readLine()) != null){
				String[] containedVars = line.split(",");
				//System.out.println("Adding verticie: " + Factor.variableNamesToIndicies(containedVars) );
				Vertex v = new Vertex(containedVars);
				_tree.addVertex(v);
				
			}else{
				br.close();
				throw new IOException("inconsistant network file.");
			}
		}
		//edges
		while ((line = br.readLine()) != null) {
			// edges
			String[] tokenized = line.split(" ");
			
			String[] left_variables = tokenized[0].split(",");
			String[] right_variables = tokenized[2].split(",");
			
			
			ArrayList<Integer> left = Factor.variableNamesToIndicies(new ArrayList<String>(Arrays.asList(left_variables)));
			ArrayList<Integer> right = Factor.variableNamesToIndicies(new ArrayList<String>(Arrays.asList(right_variables)));
			
			Collections.sort(left);
			Collections.sort(right);
			
			_tree.addEdge(new Edge(
					_tree._vertices.get(left.toString()), 
					_tree._vertices.get(right.toString()), 
					1));
		}
		
		
		System.out.println(_tree.toString());
		
		br.close();
	}

	public void readNetworkFile(String networkFilename)
		throws IOException, NumberFormatException {
		BufferedReader br = new BufferedReader(new FileReader(networkFilename));
		String line;
		// on the first line is the number of following lines that describe vertices
		int numVariables = -1;
		if((line = br.readLine()) != null){
			numVariables = Integer.valueOf(line);
		}else{
			br.close();
			throw new IOException();
		}
		if(numVariables<0) {
			br.close();
			throw new NumberFormatException();
		}
		for(int i = 0;i<numVariables;i++){
			if((line = br.readLine()) != null){
				String[] tokenized = line.split(" ");
				String variableName = tokenized[0];
				tokenized = tokenized[1].split(",");//values
				
				Factor.addVariable(variableName, new ArrayList<String>(Arrays.asList(tokenized)));
			}else{
				br.close();
				throw new IOException("inconsistant network file.");
			}
		}
		br.close();
	}

	public void readCPDFile(String cpdFilename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(cpdFilename));
		String line;
		
		while ((line = br.readLine()) != null) {
			String[] tokenized = line.split(" |,");
			ArrayList<String> variables = new ArrayList<String>();
			ArrayList<String> var_value = new ArrayList<String>();
			for(int i = 0; i<tokenized.length-1; i++){
				System.out.println(tokenized[i]);
				String[] var_pair = tokenized[i].split("=");
				variables.add(var_pair[0]);
				var_value.add(var_pair[1]);
			}
			System.out.println(tokenized[tokenized.length-1]);
			double prob = Double.valueOf(tokenized[tokenized.length-1]);
			//Put into appropriate clique
			//System.out.println(variables);
			System.out.println(variables+" "+var_value+" "+ prob);
			
			System.out.println(Factor.variableNamesToIndicies(variables).toString());
			
			ArrayList<Integer> key = Factor.variableNamesToIndicies(variables);
			
			Collections.sort(key);
			
			if(_tree._vertices.containsKey(key.toString())){// CDP is covers an entire clique
				System.out.println("fail");
			}else{// Cpd is a subfactor of one of our cliques.
				
			}
		}		
		br.close();
	}

	public void processQueries(String queryFile, boolean useSumProduct) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(queryFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] stuff = line.split(" ");
			String[] lhs = stuff[0].split(",");
			String[] rhs = stuff[1].split(",");
			System.out.println(query(lhs, rhs, useSumProduct));
		}
		br.close();
	}
}
