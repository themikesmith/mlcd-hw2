package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import elanmike.mlcd.hw2.Factor.FactorException;
import elanmike.mlcd.hw2.Factor.FactorIndexException;

public class QueryProcessor {
	public static final int NO_EVIDENCE = -1;
	private Bump _bump;
	private boolean _calibrated;
	/**
	 * A map from variable name to variable value, transformed into integers
	 */
	Map<Integer, Integer> _queryContexts;
	public QueryProcessor(Bump b) {
		this._bump = b;
		_queryContexts = new HashMap<Integer, Integer>();
		_calibrated = false;
	}
	public void resetTreeForQueries() {
		_queryContexts = new HashMap<Integer, Integer>();
		_bump.resetTreeForQueries();
	}
	public String query(String[] lhs, String[] contexts, boolean useSumProduct) {
		if(!_calibrated || (useSumProduct != _bump.useSumProduct())) {
			// set appropriate method, and run bump to calibrate
			_bump.setUseSumProduct(useSumProduct);
			_bump.runBump();
			_calibrated = true;
			resetTreeForQueries();
		}
		return query(lhs, contexts);
	}
	/**
	 * queries the structure for p(lhs|contexts)
	 */
	String query(String[] lhs, String[] contexts) {
		// check if evidence is incremental or retractive
		boolean retractive = false;
		// then take action
		// check number of variables in rhs
		if(contexts.length < _queryContexts.size()) {
			// retractive -- less evidence than before. reset and treat as incremental
			retractive = true;
		}
		// if it's the same number in rhs, check each variable
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
		} // and reset if we have retractive evidence
		if(retractive) resetTreeForQueries(); // clears our evidence
		// treat all new evidence as incremental
		// note if we cleared, all evidence is new
		ArrayList<Integer> vars = new ArrayList<Integer>(), 
				values = new ArrayList<Integer>();
		int numberNewEvidence = 0;
		for(int i = 0; i < contexts.length; i++) {
			String[] varValue = contexts[i].split("=");
			String var = varValue[0], value = varValue[1];
			int varInt = Factor.getVariableIndex(var),
				valueInt = Factor.getVariableValueIndex(varInt, value);
			if(!_queryContexts.containsKey(varInt)) {
				// additional evidence - we've never seen it before
				if(Bump.DEBUG) {
					System.out.printf("\ni:%d add'l evidence:%s=%s\n", i, var, value);
				}
				vars.add(varInt);
				values.add(valueInt);
				_queryContexts.put(varInt,valueInt);
				numberNewEvidence++;
			}
			else { // do nothing with repeat evidence
				if(Bump.DEBUG) {
					System.out.printf("\ni:%d repeat evidence:%s=%s\n", i, var, value);
				}
			}
		}
		if(numberNewEvidence > 0) {
			try {
				_bump.incorporateQueryEvidence(vars, values, numberNewEvidence);
			} catch (FactorException e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		// now process lhs
		vars = new ArrayList<Integer>();
		values = new ArrayList<Integer>();
		for(String s : lhs) {
			String[] varValue = s.split("=");
			String var = varValue[0], value = "";
			int varInt = Factor.getVariableIndex(var), valueInt = NO_EVIDENCE;
			if(varValue.length > 1) {
				value = varValue[1];
				valueInt = Factor.getVariableValueIndex(varInt, value);
			}
			vars.add(varInt);
			values.add(valueInt);
		}
		Factor result;
		try {
			result = _bump.getQueryResult(vars, values);
			if(Bump.DEBUG) {
				System.out.println("\n******result!!!******\n");
			}
			if(result != null) return result.toString();
			else return "out of clique inference";
		} catch (FactorIndexException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	/**
	 * Process queries in a query file according to a semiring
	 * @param queryFile
	 * @param useSumProduct true if sum product semiring, false if max product
	 * @throws IOException
	 */
	public void processQueries(String queryFile, boolean useSumProduct) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(queryFile));
		String line;
		while ((line = br.readLine()) != null) {
			if(Bump.DEBUG) System.out.println("\n### query:\n\n"+line);
			String[] stuff = line.split(" ");
			String[] lhs = stuff[0].split(",");
			String[] rhs = stuff[1].split(",");
			System.out.println(query(lhs, rhs, useSumProduct));
		}
		br.close();
	}

	public static void main(String[] args) {
		
	}
}
