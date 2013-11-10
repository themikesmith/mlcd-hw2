package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Belief Update Message Passing class
 * 
 * @author mcs
 *
 */
public class Bump {
	private boolean _debug = true;
	/**
	 * true if we're on the upward pass, 
	 * if we're going in increasing order id. 
	 * false otherwise
	 */
	private boolean _bumpOnUpwardPass = true;
	/**
	 * Small clique class that holds a list of variables
	 * We compare cliques by their number of shared variables
	 * @author mcs
	 *
	 */
	private class Clique {
		private Set<Integer> _variables;
		Clique() {
			_variables = new HashSet<Integer>();
		}
		Clique(Integer... variables) {
			this._variables = new HashSet<Integer>(Arrays.asList(variables));
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
	private class Vertex {
		private Integer _orderID;
		private Factor _beta;
		private Clique _data;
		private Set<Edge> _neighborEdges, _incomingEdges, _outgoingEdges;
		private Map<Edge, Boolean> _recvdMsgStatus;
		private boolean _isInformed, _onUpwardPass;
		Vertex() {
			_neighborEdges = new TreeSet<Edge>();
			_incomingEdges = new TreeSet<Edge>();
			_outgoingEdges = new TreeSet<Edge>();
			_orderID = null;
			_beta = null;
			_data = null;
			_recvdMsgStatus = new HashMap<Edge, Boolean>();
			_isInformed = false;
			_onUpwardPass = true;
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
		Clique getClique() {return _data;}
		Factor getBelief() {return _beta;}
		void setFactor(Factor f) {this._beta = f;}
		/**
		 * Sends a message to its neighbor.
		 * Calls the neighbor's onReceiveMessage method
		 * @param recvingNeighbor
		 * @param sentMsgInformed
		 */
		void sendMessage(Edge recvingNeighborEdge, boolean sentMsgInformed) {
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
	private class Edge {
		public static final String EDGE = " -- ";
		Vertex _one, _two;
		Factor _mu;
		Set<Integer> _sepset; 
		int _weight; // number of variables one has in common with two
		Edge(Vertex one, Vertex two, int weight) {
			this._one = one;
			this._two = two;
			this._weight = weight;
			this._sepset = one._data._variables;
			_sepset.retainAll(two._data._variables);
		}
		void setBelief(Factor f) {this._mu = f;}
		Factor getBelief() {return _mu;}
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
		private Set<Vertex> _vertices;
		private Set<Edge> _edges;
		Tree() {
			_vertices = new HashSet<Vertex>();
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
	 * Reads in the tree from the clique file.
	 * @param cliqueTreeFilename
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public void readTree(String cliqueTreeFilename) 
			throws IOException, NumberFormatException {
		BufferedReader br = new BufferedReader(new FileReader(cliqueTreeFilename));
		String line;
		// on the first line is the number of following lines that describe vertices
		int numVertices = -1;
		while ((line = br.readLine()) != null) {
			if(numVertices == -1) { // set the number of vertices
				numVertices = new Integer(line); // throws number format exception 
			}
			else if(numVertices > 0) { // reading vertices
				String[] vars = line.split(",");
				
			}
			else { // reading edges
				
			}
		}
		br.close();
	}
}
