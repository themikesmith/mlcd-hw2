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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Belief Update Message Passing class
 * 
 * @author mcs
 *
 */
public class Bump {
	public static final int NO_EVIDENCE = -1;
	public static final int UNMARKED = 0;
	public static int nextOrderID = UNMARKED;
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
//		void addVariable(int var) {
//			_variables.add(var);
//		}
//		/**
//		 * @param other the other clique
//		 * @return the number of variables this clique has in common with the other
//		 */
//		int getCardinalityOfIntersectionWith(Clique other) {
//			Set<Integer> intersection = new HashSet<Integer>(this._variables);
//			intersection.retainAll(other._variables);
//			return intersection.size();
//		}
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
			return Factor.variableIndicesToNames(_variables).toString();
		}
		public String getLongInfo() {
			StringBuilder sb = new StringBuilder(toString());
			// add factor result
			sb.append("\n").append(super.toString());
			return sb.toString();
		}
	}
	private class Vertex extends Clique {
		private int _orderID;
		private Set<Edge> _outgoingEdges;
		/**
		 * Stores each neighboring edge, initializes with the fact that we
		 * have not yet received an informed message via that edge.
		 * Tracks our 'informed' status - whether or not we've received
		 * informed messages on all edges
		 */
		private Map<Edge, Boolean> _recvdMsgStatus;
		private boolean _isInformed;
		Vertex(String[] varsContained){
			super(varsContained);
			_outgoingEdges = new HashSet<Edge>();
			_recvdMsgStatus = new HashMap<Edge, Boolean>();
			reset();
		}
		/**
		 * Resets this vertex to prepare for the running of the algorithm
		 */
		void reset() {
			_orderID = UNMARKED;
			_isInformed = false;
			for(Edge e : _recvdMsgStatus.keySet()) {
				_recvdMsgStatus.put(e, false);
			}
			_outgoingEdges.clear();
		}
		void setOrderID() {this._orderID = ++nextOrderID;}
		/**
		 * Adds a neighbor edge
		 * checks for duplicates.
		 * initializes our received message status for that edge.
		 * @param e
		 */
		void addNeighborEdge(Edge e) {
			if(!_recvdMsgStatus.containsKey(e)) {
				_recvdMsgStatus.put(e, false);
			}
		}
		/**
		 * Sends a message to its neighbor.
		 * Calls the neighbor's onReceiveMessage method
		 * @param recvingNeighbor
		 */
		void sendMessage(Edge recvingNeighborEdge) {
			// calculate message, and send
			// TODO compose message
			Factor message = null;
			recvingNeighborEdge.getOtherVertex(this).
				onReceiveMessage(recvingNeighborEdge, message);
		}
		/**
		 * When we receive a message...
		 * (called by our 'send message' function when our neighbor sends
		 * @param sendingNeighborEdge
		 * @param message
		 */
		private void onReceiveMessage(Edge sendingNeighborEdge, Factor message) {
			// TODO process message
			Vertex v = sendingNeighborEdge.getOtherVertex(this);
			_recvdMsgStatus.put(sendingNeighborEdge, v._isInformed);
			if(v._isInformed) { // if we've altered our state, recheck
				_isInformed = isInformed();
			} // else don't bother, as we know it's impossible
		}
		/**
		 * Return a list of all outgoing neighbors for the upward pass,
		 * as defined by our vertex ordering.
		 * @return the list of all outgoing neighbors for the upward pass
		 */
		Set<Edge> getUpwardOutgoingNeighborEdges() {
			if(!_bumpOnUpwardPass) {
				System.err.println("calling upward pass neighbor method on downward pass!");
			}
			if(_outgoingEdges.size() == 0) { // only compute if we have to
				Set<Edge> outgoingEdges = new HashSet<Edge>();
				Iterator<Edge> it = _recvdMsgStatus.keySet().iterator();
				while(it.hasNext()) {
					Edge e = it.next();
					Vertex v = e.getOtherVertex(this);
					if(this._orderID < v._orderID) {
						outgoingEdges.add(e);
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
				return v._variables == this._variables;
			}
			return false;
		}
		public String getLongInfo() {
			StringBuilder sb = new StringBuilder("Vertex:\n");
			// add clique string
			sb.append(super.getLongInfo());
			// add edge info
			sb.append("recvdMsgStatus:").append(_recvdMsgStatus);
			return sb.toString();
		}
		
	}
	private class Edge extends Factor{
		public static final String EDGE = " -- ";
		Vertex _one, _two;
		//int _weight;
		Edge(Vertex one, Vertex two) {//, int weight) {
			super(one.intersection(two._variables));
			this._one = one;
			this._two = two;
			//this._weight = weight;
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
			sb.append(Factor.variableIndicesToNames(_one._variables));
			sb.append(EDGE);
			sb.append(Factor.variableIndicesToNames(_two._variables));
			return sb.toString();
		}
		public String getLongInfo() {
			StringBuilder sb = new StringBuilder("edge:\n");
			sb.append(Factor.variableIndicesToNames(_one._variables));
			sb.append(" --").append(Factor.variableIndicesToNames(_variables)).append("-- ");
			sb.append(Factor.variableIndicesToNames(_two._variables));
			sb.append("\nmu:\n").append(super.toString());
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
		void addVertex(Vertex v) {
			_vertices.put(v.makeKey(),v);
			//System.out.println("adding vertice with key: "+ v._variables.toString());
		}
		/**
		 * Adds the edge to the tree.
		 * Checks for duplicates edges
		 * @param e
		 */
		void addEdge(Edge e) {
			if(!_edges.containsKey(e.makeKey())) {
				//_vertices.put(e._one._variables.toString(),e._one);
				//_vertices.put(e._two._variables.toString(),e._two);
				_edges.put(e.makeKey(),e);
				// for each involved vertex V, add the edge to its 'informed' check
				e._one.addNeighborEdge(e);
				e._two.addNeighborEdge(e);
			}
		}
		Tree makeCopy() {
			Tree t = new Tree();
			//TODO make deep copy
			return null;
		}
		
		public String toString(){
			StringBuilder output = new StringBuilder("=== Vertices ===\n");
			Set<String> keys = _vertices.keySet();
			for(String k:keys){
				output.append(_vertices.get(k).toString()).append("\n\n");
			}
			output.append("=== Edges ===\n");
			keys = _edges.keySet();
			for(String k:keys){
				output.append(_edges.get(k).toString()).append("\n");
			}
			return output.toString();
		}
		public String getLongInfo() {
			StringBuilder output = new StringBuilder("=== Vertices ===\n");
			Set<String> keys = _vertices.keySet();
			for(String k:keys){
				output.append(_vertices.get(k).getLongInfo()).append("\n\n");
			}
			output.append("=== Edges ===\n");
			keys = _edges.keySet();
			for(String k:keys){
				output.append(_edges.get(k).getLongInfo()).append("\n");
			}
			return output.toString();
		}
		/**
		 * Initialize beliefs at each vertex of the tree
		 */
		void initCliques() {
			//TODO implement init cliques
			for(Vertex v : _vertices.values()) {
				
			}
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
	private boolean _useSumProduct;
	
	/**
	 * A map from variable name to variable value, transformed into integers
	 */
	Map<Integer, Integer> _queryContexts;
	Bump() {
		_tree = new Tree();
		_bumpOnUpwardPass = false;
		_queryContexts = new HashMap<Integer, Integer>();
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
	 * Resets, inits beliefs, calibrates the tree.
	 * copies this calibrated tree such that we might alter it with query evidence
	 */
	public void runBump() {
		_tree.initCliques();
		upwardPassBeliefUpdate(downwardPassBeliefUpdate(_tree));
		resetTreeForQueries();
	}
	/**
	 * Calibrate the tree with the upward pass of belief-update message passing.
	 * Use the reverse of our ordering that we created in the downward pass.
	 */
	void upwardPassBeliefUpdate(List<Vertex> orderedVertices) {
		_bumpOnUpwardPass = true;
		if(DEBUG) System.out.println("\n\nupward pass!\n\n");
		for(int i = orderedVertices.size() - 1; i >= 0; i--) {
			Vertex v = orderedVertices.get(i);
			// for each edge that is outgoing given our ordering
			for(Edge e : v.getUpwardOutgoingNeighborEdges()) {
				// send our message along that edge
				v.sendMessage(e);
				if(DEBUG) {
					System.out.println("\n******\nmsg:"+i);
					System.out.println(_tree.getLongInfo());
				}
			}
		}
	}
	/**
	 * Conducts one downward pass of belief update message passing
	 * with a designated root.
	 * Conducts breadth-first search of the query tree, 
	 * and sends messages in that order.
	 * 
	 * Chooses an arbitrary root.
	 * 
	 * @param t the tree in question
	 * @return a list of the order of this pass, so we can reverse it in the upward pass
	 */
	List<Vertex> downwardPassBeliefUpdate(Tree t) {
		return downwardPassBeliefUpdate(t, t._vertices.values().iterator().next());
	}
	/**
	 * Conducts one downward pass of belief update message passing
	 * with a designated root.
	 * Conducts depth-first or breadth-first search of the query tree, 
	 * sends messages in that order.
	 * 
	 * @param t the tree in question.
	 * @param root
	 * @return a list of the order of this pass, so the upward pass can reverse it
	 */
	List<Vertex> downwardPassBeliefUpdate(Tree t, Vertex root) {
		_bumpOnUpwardPass = false;
		if(DEBUG) System.out.println("\n\ndownward pass!\n\n");
		nextOrderID = UNMARKED; // begin again at 0
		List<Vertex> ordering = new ArrayList<Vertex>();
		if(t._vertices.size() == 0) return ordering;
		Queue<Vertex> toProcess = new LinkedList<Vertex>();
		toProcess.add(root);
		// giving a number is equivalent to adding to ordering, giving index
		// while all vertices don't have a number
		while(ordering.size() != t._vertices.size()) {
			if(toProcess.size() == 0) {
				// find an unmarked node... expensive
				for(Vertex v : t._vertices.values()) {
					if(v._orderID == UNMARKED) {
						toProcess.add(v); // add to queue when find unmarked
					}
				}
			}
			Vertex curr = toProcess.remove();
			// mark
			curr.setOrderID();
			ordering.add(curr); // and add to our ordered list
			if(curr._orderID != ordering.size()) {// verify
				System.err.println("oops! order id incorrect");
			}
			// and then for each downstream child...
			for(Edge e : curr._recvdMsgStatus.keySet()) {
				Vertex k = e.getOtherVertex(curr);
				if(k._orderID == UNMARKED) {
					// downstream if we haven't marked it yet
					// send belief update message to the child
					curr.sendMessage(e);
					if(DEBUG) {
						System.out.println("\n******\nmsg:");
						System.out.println(_tree.getLongInfo());
					}
					// and add the child to our list to process
					toProcess.add(k);
				}
			}
		}
		return ordering;
	}
	/**
	 * Resets the query tree in preparation for queries.
	 * makes a fresh copy of the tree in our query tree variable,
	 * and clears our stored contexts.
	 */
	void resetTreeForQueries() {
		_queryTree = _tree.makeCopy();
		_queryContexts = new HashMap<Integer, Integer>();
	}
	/**
	 * find a vertex with a clique containing the given set of variables
	 * in the given tree
	 * @param t the tree in question
	 * @param vars the variable integers
	 * @return a suitable vertex, or null otherwise.  should always return a vertex.
	 */
	Vertex findVertexInTree(Tree t, int... vars) {
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
	 * Given that our tree is calibrated, incorporate the evidence
	 * into our QUERY COPY
	 * 
	 * @param pairs list of Pair<Integer, Integer>... pairs pairs of variable=value
	 */
	void incorporateQueryEvidence(int varInt, int varValue) {
		// Find a clique with the variable, C, in the query tree
		Vertex newRoot = findVertexInTree(_queryTree, varInt);
		if(newRoot == null) {
			System.err.println("can't find vertex - whoops!");
		}
		// multiply in a new indicator factor
		// TODO multiply in new indicator
		// conduct one pass of B-U with C as the root
		downwardPassBeliefUpdate(_queryTree, newRoot);
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
		// now process lhs
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
		// now process lhs
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
				if(DEBUG) System.out.println("Adding vertex: " + Factor.variableNamesToIndicies(containedVars) );
				Vertex v = new Vertex(containedVars);
				_tree.addVertex(v);
			}else{
				br.close();
				throw new IOException("inconsistant network file.");
			}
		}
		while ((line = br.readLine()) != null) {
			// edges
			String[] tokenized = line.split(" ");
			
			String[] left_variables = tokenized[0].split(",");
			String[] right_variables = tokenized[2].split(",");
			
			ArrayList<Integer> left = Factor.variableNamesToIndicies(left_variables);
			ArrayList<Integer> right = Factor.variableNamesToIndicies(right_variables);
			
			Collections.sort(left);
			Collections.sort(right);
			
			_tree.addEdge(new Edge(
					_tree._vertices.get(left.toString()), 
					_tree._vertices.get(right.toString()) 
//					,1
					));
		}
		if(DEBUG) System.out.println(_tree.getLongInfo());
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
				throw new IOException("inconsistent network file.");
			}
		}
		br.close();
	}

	public void readCPDFile(String cpdFilename) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(cpdFilename));
		String line;
		
		HashMap<String,Factor> initialFactors = new HashMap<String,Factor>();
		
		while ((line = br.readLine()) != null) {
			String[] tokenized = line.split(" |,");
			ArrayList<String> variables = new ArrayList<String>();
			ArrayList<String> var_value = new ArrayList<String>();
			for(int i = 0; i<tokenized.length-1; i++){
				//System.out.println(tokenized[i]);
				String[] var_pair = tokenized[i].split("=");
				variables.add(var_pair[0]);
				var_value.add(var_pair[1]);
			}
			//System.out.println(tokenized[tokenized.length-1]);
			double prob = Double.valueOf(tokenized[tokenized.length-1]);
			//Put into appropriate clique
			if(DEBUG) System.out.println(variables);
			//System.out.println(variables+" "+var_value+" "+ prob);
			
			
			//System.out.println(Factor.variableNamesToIndicies(variables).toString());
			
			ArrayList<Integer> key = Factor.variableNamesToIndicies(variables);
			ArrayList<Integer> val_indicies = Factor.valueNamesToIndicies(variables, var_value);
			Collections.sort(key);
			
			//System.out.println(variables+" "+var_value+" "+ prob + "  key: "+key.toString()+ "  vals: "+val_indicies.toString());
			//System.out.println(initialFactors.keySet().toString());
			
			if(initialFactors.containsKey(key.toString())){ //already has this factor
				//System.out.println("Adding to previously created factor.");
				initialFactors.get(key.toString()).putProbByValues(val_indicies, prob);
				
			}else{// new factor
				//System.out.println("Creating new factor.");
				Factor newFac = new Factor(key);
				newFac.putProbByValues(val_indicies, prob);
				initialFactors.put(key.toString(), newFac);
			}
		}		
		
		
		if(DEBUG){
			System.out.println("==initialFactors==");
			for(String s:initialFactors.keySet()){
				System.out.println(Factor.variableIndicesToNames(initialFactors.get(s)._variables));
			}
			
			System.out.println("==Cliques==");
			for(String s:_tree._vertices.keySet()){
				System.out.println(Factor.variableIndicesToNames(_tree._vertices.get(s)._variables));
			}
		}
		
		for(String initFactors:initialFactors.keySet()){
			ArrayList<Integer> vars = initialFactors.get(initFactors)._variables;
			
			if(DEBUG) System.out.println("Itinital Factor: " + initialFactors.get(initFactors));
			
			boolean foundSuperset = false;
			for(String cliquesKeys:_tree._vertices.keySet()){
				if(_tree._vertices.get(cliquesKeys).difference(vars).size() ==0 ){//we're a subset
					if(DEBUG) System.out.println(initFactors + " is a subset of "+cliquesKeys );
					_tree._vertices.get(cliquesKeys).setFactorData(_tree._vertices.get(cliquesKeys).product(initialFactors.get(initFactors)));
					foundSuperset = true;
					break;
				}
			}
			if(!foundSuperset)
				if(DEBUG) System.out.println("Hmmm... " + Factor.variableIndicesToNames(initialFactors.get(initFactors)._variables) + " has no supersets");
			//System.out.println("initialFact "+ initialFactors.get(s));
			
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
