package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import elanmike.mlcd.hw2.Factor.FactorException;

public class QueryProcessor {
	public static final int NO_EVIDENCE = -1;
	private Bump _bump;
	private boolean _useSumProduct;
	/**
	 * A map from variable name to variable value, transformed into integers
	 */
	Map<Integer, Integer> _queryContexts;
	public QueryProcessor(Bump b) {
		this._bump = b;
		_queryContexts = new HashMap<Integer, Integer>();
		_useSumProduct = true;
	}
	public void resetTreeForQueries() {
		_queryContexts = new HashMap<Integer, Integer>();
		_bump.resetTreeForQueries();
	}
	public String query(String[] lhs, String[] contexts) {
		if(_useSumProduct != _bump.useSumProduct()) {
			System.err.println("oops! not ready.");
			// set appropriate method, and run bump to calibrate
			_bump.setUseSumProduct(_useSumProduct);
			_bump.runBump();
		}
		if(_useSumProduct) return querySumProduct(lhs, contexts);
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
				try {
					_bump.incorporateQueryEvidence(varInt, varInt);
				} catch (FactorException e) {
					e.printStackTrace();
					return e.getMessage();
				}
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
				// TODO conduct query sum product
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
				try {
					_bump.incorporateQueryEvidence(varInt, varInt);
				} catch (FactorException e) {
					e.printStackTrace();
					return e.getMessage();
				}
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
				// TODO conduct query max product
			}
		}
		return null;
	}
	/**
	 * Process queries in a query file according to a semiring
	 * @param queryFile
	 * @param b
	 * @throws IOException
	 */
	public void processQueries(String queryFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(queryFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] stuff = line.split(" ");
			String[] lhs = stuff[0].split(",");
			String[] rhs = stuff[1].split(",");
			System.out.println(query(lhs, rhs));
		}
		br.close();
	}
}
