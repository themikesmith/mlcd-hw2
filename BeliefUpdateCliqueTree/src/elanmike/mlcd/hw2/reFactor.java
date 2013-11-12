package elanmike.mlcd.hw2;
//package elanmike.mlcd.hw2;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Set;
//import java.util.TreeSet;
//
//public class Factor {
//	public static interface Variable{
//		
//		public String getName();
//		public int getCard();
//		public ArrayList<String> getValues();
//		
//		public boolean equals(Variable v);
//	}
//	
//	public static class GlobalVariable implements Variable, Comparable<Variable>{
//		public final String name;
//		public final int card;
//		public final ArrayList<String> values;
//		
//		GlobalVariable(String name, ArrayList<String> values){
//			this.name = name;
//			this.card = values.size();
//			this.values = values;
//		}
//	
//		public GlobalVariable(Variable var) {
//			this.name = var.getName();
//			this.card = var.getCard();
//			this.values = var.getValues();
//		}
//
//		public String getName() {return name;}
//		public int getCard() {return card;}
//		public ArrayList<String> getValues() {return values;}
//		
//		public String toString(){return this.name;}
//		
//		public boolean equals(Variable v){
//			System.out.println("equals");
//			return this.name.equals(v.getName());
//		}
//		
//		public int hashcode(){
//			System.out.println("hashcode");
//			return name.hashCode();
//		}
//		public int compareTo(Variable arg0) {
//			System.out.println("compareTo");
//			return this.name.compareTo(arg0.getName());
//		}
//	}
//	
//	public static class HeldVariable extends GlobalVariable{
//		HeldVariable(Variable var, int valueHeld) {
//			super(var);
//			valueIndex = valueHeld;
//		}
//
//		public int valueIndex;
//	}
//	
//	public static class LocalVariable extends GlobalVariable{
//		public LocalVariable(Variable var, int stride) {
//			super(var);
//			this.stride = stride;
//		}
//
//		public int stride;
//	}
//	
//	public class FactorException extends Exception
//    {
//		
//		
//		
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//		public FactorException () {super();}
//    	public FactorException (String message) {
//        	super (message);
//        }
//    	public FactorException (Throwable cause) {
//        	super (cause);
//        }
//    	public FactorException (String message, Throwable cause) {
//    		super (message, cause);
//        }
//    }
//	public class FactorIndexException extends FactorException {
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//		public FactorIndexException () {super();}
//    	public FactorIndexException (String message) {
//        	super (message);
//        }
//    	public FactorIndexException (Throwable cause) {
//        	super (cause);
//        }
//    	public FactorIndexException (String message, Throwable cause) {
//    		super (message, cause);
//        }
//	}
//	public class FactorScopeException extends FactorException {
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//		public FactorScopeException () {super();}
//    	public FactorScopeException (String message) {
//        	super (message);
//        }
//    	public FactorScopeException (Throwable cause) {
//        	super (cause);
//        }
//    	public FactorScopeException (String message, Throwable cause) {
//    		super (message, cause);
//        }
//	}
//	
//	protected static HashMap<String,GlobalVariable> GlobalVariables;
//	
//
//	public static Variable addVariable(String varName, ArrayList<String> varValues){
//		
//		if(GlobalVariables == null) GlobalVariables = new HashMap<String,GlobalVariable>();
//		
//		GlobalVariable newVar = new GlobalVariable(varName,varValues);
//		
//		if(!GlobalVariables.containsKey(newVar)){
//			GlobalVariables.put(varName,newVar);
//			return newVar;
//		}else{ 
//			System.err.println("Error: Already added variable "+newVar);
//			return null;
//		}
//	}
//	
//	public static String variableInfo(){
//		String output = "";
//		
//		if(GlobalVariables == null){
//			output += "GlobalVariables is null.\n";
//		}else{
//			output+= "GlobalVariables size: " + GlobalVariables.size() +"\n";
//			for(String vs: GlobalVariables.keySet()){
//				output+= GlobalVariables.get(vs).name + " size(" + GlobalVariables.get(vs).card +")[";
//				for(int valIdx = 0; valIdx < GlobalVariables.get(vs).card; valIdx++){
//					output+=" "+GlobalVariables.get(vs).values.get(valIdx);
//				}
//				output+=" ]\n";
//			}
//		}
//		
//		return output;
//	}
//	
//	protected ArrayList<LocalVariable> _variables;
//	protected ArrayList<Double> data; //log probabilities
//	
//	Factor(String[] varsNames){
//		_variables= new ArrayList<LocalVariable>(varsNames.length);
//		for(int index=0; index < varsNames.length; index ++) 
//			_variables.add(new LocalVariable(GlobalVariables.get(varsNames[index]),-1));
//		
//		//Collections.sort(_variables);
//
//		int strideTot = 1;
//		for(int i = _variables.size()-1 ; i >=0 ; i--){
//			_variables.get(i).stride = strideTot;
//			strideTot*=_variables.get(i).card;
//		}
//		
//		
//		this.data = new ArrayList<Double>(strideTot);
//		for(int i = 0; i<strideTot; i++) data.add(Math.log(1.0));
//	}
//	protected Factor(ArrayList<? extends Variable> vars){
//		_variables= new ArrayList<LocalVariable>(vars.size());
//		for(int index=0; index < vars.size(); index ++) 
//			_variables.add(new LocalVariable(vars.get(index),-1));
//		
//		//Collections.sort(_variables);
//		
//		int strideTot = 1;
//		for(int i = _variables.size()-1 ; i >=0 ; i--){
//			_variables.get(i).stride = strideTot;
//			strideTot*=_variables.get(i).card;
//		}
//		
//		this.data = new ArrayList<Double>(strideTot);
//		for(int i = 0; i<strideTot; i++) data.add(Math.log(1.0));
//	}
//	protected Factor(ArrayList<? extends Variable> vars, double d) {
//		_variables= new ArrayList<LocalVariable>(vars.size());
//		for(int index=0; index < vars.size(); index ++) 
//			_variables.add(new LocalVariable(vars.get(index),-1));
//		
//		//Collections.sort(_variables);
//		
//		int strideTot = 1;
//		for(int i = _variables.size()-1 ; i >=0 ; i--){
//			_variables.get(i).stride = strideTot;
//			strideTot*=_variables.get(i).card;
//		}
//		
//		this.data = new ArrayList<Double>(strideTot);
//		for(int i = 0; i<strideTot; i++) data.add(Math.log(d));
//	}
//	protected Factor(Factor factToCopy){
//		this._variables = factToCopy._variables;
//		
//		this.data = new ArrayList<Double>(factToCopy.data.size());
//		for(int i = 0; i<factToCopy.data.size(); i++) data.add(factToCopy.data.get(i));
//	}
//	
//	public void setFactorData(Factor f) throws FactorScopeException {
//		if(this._variables.equals(f._variables)){
//			this.data = f.data;
//		}else{
//			throw new FactorScopeException("can't see this factor, does not contain the same variables");
//		}
//	}
//	
//	private int getInternalIndex(int globalIndex){
//		return _variables.indexOf(globalIndex);
//	}
//
//	private int index(ArrayList<Variable> variables, ArrayList<Integer> values) throws FactorIndexException {
//		int[] temp = new int[variables.size()];
//		for(int i = 0; i< variables.size();i++){
//			if(_variables.indexOf(variables.get(i)) == -1) {
//				throw new FactorIndexException("index error: factor does not contain desired variable");
//			}
//			temp[_variables.indexOf(variables.get(i))] = values.get(i);
//		}
//		return index(temp);
//	}
//	private int index(int[] variableValues) throws FactorIndexException{
//		int searchIndex = 0;
//		
//		if(variableValues.length != _variables.size()) 
//			throw new FactorIndexException("FactorIndexError: indexLength("+variableValues.length+") does not match number of variables for this factor("+_variables.size()+")");
//		
//		for(int index=0; index < _variables.size(); index ++){
//			if(variableValues[index] >= _variables.get(index).card|| variableValues[index]< 0)
//				throw new FactorIndexException("FactorIndexError: variableValue("+variableValues[index]+") was not in the valid range of ( 0 - "+(_variables.get(index).card-1)+")");
//			else
//				searchIndex += variableValues[index]*_variables.get(index).stride;
//		}
//		
//		return searchIndex;
//	}
//	
//	private ArrayList<Integer> valuesFromIndex(int datum_index) throws FactorIndexException {
//		
//		
//		if(datum_index >= data.size()|| datum_index< 0)
//			throw new FactorIndexException("FactorIndexError: index("+datum_index+") was not in the valid range of ( 0 - "+(data.size()-1)+")");
//		
//		ArrayList<Integer> values = new ArrayList<Integer>();
//		
//		/*
//		System.out.println("valuesFromIndex");
//		System.out.println("index:" + datum_index);
//		System.out.println("_variables"+_variables );
//		System.out.println("_stride"+_stride );
//		System.out.println("_variableCard"+_variableCard );
//		System.out.println();
//		*/
//		for(int varIdx = 0; varIdx< _variables.size(); varIdx++){
//			values.add((datum_index/_variables.get(varIdx).stride)%_variables.get(varIdx).card);
//		}
//		
//		return values;
//	}
//	
//	public void putProbByName(String[] varVals, double prob) 
//			throws FactorIndexException, FactorScopeException {
//		if(varVals.length != _variables.size()) 
//			throw new FactorIndexException("InputLengthError: indexLength("+varVals.length+") does not match number of variables for this factor("+_variables.size()+")");
//		ArrayList<Integer> valVarsIndicies = new ArrayList<Integer>();
//		for(int varIdx=0; varIdx < varVals.length; varIdx ++){
//			int integerValOfString = _variables.indexOf(varVals[varIdx]);//_variableValues.get(varIdx).indexOf(varVals[varIdx]);
//			if(integerValOfString <0)
//				throw new FactorScopeException("ValueError: Value("+varVals[varIdx]+") not applicable for "+_variables.get(varIdx).name);
//			valVarsIndicies.add(integerValOfString);
//		}
//		putProbByValues(valVarsIndicies,prob);
//		
//	}
//	
//	public void putProbByIndex(int datum_index, double prob) throws FactorIndexException{
//		if(datum_index < 0 || datum_index > data.size()) 
//			throw new FactorIndexException("InputLengthError: datumIndex("+datum_index+") is not in range ( 0 - "+_variables.size()+")");
//		
//		data.set(datum_index,Math.log(prob));
//		
//	}
//	
//	public String toString(){
//		String output = "";
//		if(_variables.size() != 0) {
//			// print out a description of the factor
//			output += "Phi( ";
//			for(int var_idx=0; var_idx < _variables.size(); var_idx ++)
//				output += _variables.get(var_idx).name + " ";
//			output += ")\n";
//			
////			for(int var_idx=0; var_idx < _variables.size(); var_idx ++)
////				output += _stride.get(var_idx) + "\t";
////			output += "Stride\n";
//			
//			for(int var_idx=0; var_idx < _variables.size(); var_idx ++)
//				output += _variables.get(var_idx).name + "\t";
//			output += "Value\n";
//		}
//		for(int datum_index = 0; datum_index < data.size(); datum_index++){
//			
//			ArrayList<Integer> values = null;
//			try {
//				values = valuesFromIndex(datum_index);
//			} catch (FactorIndexException e) {e.printStackTrace();}
//			
//			/*
//			System.out.println(_variables);
//			System.out.println(values);
//			ArrayList<String> valueNames = valueIndiciesToNames(_variables,values);
//			System.out.println(valueNames);
//			*/
//			for(int var_idx=0; var_idx < _variables.size(); var_idx ++){
////				output += String.format("%01d\t", values.get(var_idx));
//				output += String.format("%s\t", _variables.get(var_idx).values.get(values.get(var_idx)));//Factor.valueIndexToName(var_idx, values.get(var_idx)));
//				//output += valueNames.get(var_idx) + "\t";
//					//	String.format("%s\t",
//					//	.get(var_idx),);
//					//Factor.getVariableName(var_idx,
//					//		((datum_index/_stride.get(var_idx))%_variableCard.get(var_idx))));
//						
//			}
//			output += Math.exp(data.get(datum_index))+"\n";
//		}
//		return output.substring(0, output.length()-1);
//	}
//	
//	public void putProbByValues(double prob,int... values )
//			throws FactorIndexException {
//		
//		int searchIndex = 0;
//		if(values.length != _variables.size()) 
//			throw new FactorIndexException("FactorIndexError: indexLength("+values.length+") does not match number of variables for this factor("+_variables.size()+")");
//		for(int index=0; index < _variables.size(); index ++)
//			if(values[index] >= _variables.get(index).card|| values[index]< 0)
//				throw new FactorIndexException("FactorIndexError: variableValue("+values[index]+") was not in the valid range of ( 0 - "+(_variables.get(index).card-1)+")");
//			else
//				searchIndex += values[index]*_variables.get(index).stride;
//		
//		data.set(searchIndex,Math.log(prob));
//	}
//	
//	public void putProbByValues(ArrayList<Integer> arrayList,double prob) 
//			throws FactorIndexException {
//		
//		int searchIndex = 0;
//		if(arrayList.size() != _variables.size()) 
//			throw new FactorIndexException("FactorIndexError: indexLength("+arrayList.size()+") does not match number of variables for this factor("+_variables.size()+")");
//		for(int index=0; index < _variables.size(); index ++)
//			if(arrayList.get(index) >= _variables.get(index).card || arrayList.get(index)< 0) {
//				System.err.printf("for index:%d variable:%s\nthought card:%d\nsupplied index:%d\n", 
//					index, _variables.get(index).name, _variables.get(index), arrayList.get(index));
//				throw new FactorIndexException("FactorIndexError: variableValue("+arrayList.get(index)+") was not in the valid range of ( 0 - "+(_variables.get(index).card-1)+")");
//			}
//			else
//				searchIndex += arrayList.get(index)*_variables.get(index).stride;
//		
//		data.set(searchIndex,Math.log(prob));
//	}
//	
//	public double getProbByValues(int[] variableValues) throws FactorIndexException {	
//		return data.get(index(variableValues));
//	}
//	
//	public ArrayList<Variable> intersection(ArrayList<? extends Variable> _variables2){
//		Set<Variable> myVars = new TreeSet<Variable>(this._variables);
//		Set<Variable> theirVars = new TreeSet<Variable>(_variables2);
//		
//		myVars.retainAll(theirVars);
//		ArrayList<Variable> intersection = new ArrayList<Variable>();
//		for(Variable curVar:myVars)
//			intersection.add(curVar);
//		return intersection;
//	}
//	
//	public ArrayList<Variable> difference(ArrayList<? extends Variable> other){
//		Set<Variable> myVars = new TreeSet<Variable>(this._variables);
//		Set<Variable> theirVars = new TreeSet<Variable>(other);
//		
//		myVars.removeAll(theirVars);
//		ArrayList<Variable> difference = new ArrayList<Variable>();
//		
//		for(Variable curVar:myVars)
//			difference.add(curVar);
//		return difference;
//	}
//	
//	public ArrayList<Variable> union(ArrayList<? extends Variable> _variables2){
//		Set<Variable> myVars = new TreeSet<Variable>(this._variables);
//		Set<Variable> theirVars = new TreeSet<Variable>(_variables2);
//		
//		myVars.addAll(theirVars);
//		ArrayList<Variable> union = new ArrayList<Variable>();
//		
//		for(Variable curVar:myVars)
//			union.add(curVar);
//		return union;
//	}
//
//	public boolean contains(ArrayList<? extends Variable> other){
//			Set<Variable> myVars = new TreeSet<Variable>(this._variables);
//			Set<Variable> theirVars = new TreeSet<Variable>(other);
//			//System.out.println(myVars + " ?= "+ theirVars);
//			return myVars.containsAll(theirVars);
//		}
//	
//	//p359 example problem p107
//	public Factor product(Factor f) throws ArrayIndexOutOfBoundsException {
//		ArrayList<Variable> unionScope = this.union(f._variables);
//		Factor psi = new Factor(unionScope);
//		
//		int j =0;
//		int k =0;
//		int[] assigment = new int[unionScope.size()];
//		
//		for(int i = 0; i < psi.data.size(); i++){
//			psi.data.set(i, this.data.get(j)+f.data.get(k)); //adding log probabilies
//			//System.out.printf("Multiplying value at (%d) by (%d) to get value at (%d)\n",j,k,i);
//			
//			//for(int l =0; l < unionScope.size(); l++){
//			 for(int l =unionScope.size()-1; l >=0 ; l--){
//				assigment[l]++;
//				//System.out.printf("l = %d\n",l);
//				if(assigment[l]==unionScope.get(l).getCard()){
//					//System.out.printf("assignment[%d] == card(%d)\n",unionScope.get(l),unionScope.get(l));
//					assigment[l]=0;
//					if(this._variables.contains(unionScope.get(l)))
//						j = j-(unionScope.get(l).getCard()-1)*this._variables.get(this._variables.indexOf(unionScope.get(l))).stride;//this.getInternalIndex(unionScope.get(l)));
//					if(f._variables.contains(unionScope.get(l)))
//						k = k-(unionScope.get(l).getCard()-1)*f._variables.get(f._variables.indexOf(unionScope.get(l))).stride;//f.getInternalIndex(unionScope.get(l)));
//				}else{
//					//System.out.printf("Else\n");
//					if(this._variables.contains(unionScope.get(l)))
//						j = j + this._variables.get(this._variables.indexOf(unionScope.get(l))).stride;//this.getInternalIndex(unionScope.get(l)));
//					if(f._variables.contains(unionScope.get(l)))
//						k = k + f._variables.get(f._variables.indexOf(unionScope.get(l))).stride;//f.getInternalIndex(unionScope.get(l)));
//					break;
//				}
//			}
//		}
//		
//		
//		return psi;
//	}
//	
//	public Factor divide(Factor f) throws FactorScopeException, FactorIndexException {
//		if(!this._variables.containsAll(f._variables))
//			throw new FactorScopeException("DivionError: Numerator does not contain Denominator");
//		
//		Factor result = new Factor(this._variables);
//		
//		ArrayList<Variable> sepset = this.intersection(f._variables);
//		
//		//System.out.println("Dividing: "+ this._variables + " by "+ f._variables + ", intersection of: " + sepset  +"("+sepset.size()+")");
//		//System.out.println("LHS datasize: "+ this.data.size() + " RHS datasize: " + f.data.size() );
//		for(int datum_idx = 0; datum_idx < this.data.size(); datum_idx++ ){
//			ArrayList<Integer> values = valuesFromIndex(datum_idx);
//			//ArrayList<Integer> sharedVarValues = new ArrayList<Integer>();
//			
//			int[] f_indicies_of_values = new int[sepset.size()];
//			for(Variable s:sepset){
//				int this_ind = this._variables.indexOf(s);//index of variable in sepset in this factor
//				//sharedVarValues.add(values[this_ind]);//value of that variable
//				int that_ind = f._variables.indexOf(s);//index of variable in sepset in this factor
//				f_indicies_of_values[that_ind] = values.get(this_ind);
//			}
//			if(this.data.get(datum_idx).equals(Double.NEGATIVE_INFINITY) && f.getProbByValues(f_indicies_of_values) == Double.NEGATIVE_INFINITY ){
//				result.data.set(datum_idx, Double.NEGATIVE_INFINITY);
//			}else{
//				result.data.set(datum_idx, this.data.get(datum_idx) - f.getProbByValues(f_indicies_of_values));
//			}
//			
//		}
//		
//		return result;
//	}
//	
//	//example p297
//	public Factor marginalize(ArrayList<Variable> elimVar) throws FactorIndexException{
//		ArrayList<Variable> finalVars = difference(elimVar);
//		
//		Factor result = new Factor(finalVars, 0);
////		System.out.println("inital factor of 0's:");
////		System.out.println(result);
//		
//		for(int datum_idx = 0; datum_idx < this.data.size(); datum_idx++ ){
//			ArrayList<Integer> values = valuesFromIndex(datum_idx);
//			
//			int[] f_indicies_of_values = new int[finalVars.size()];
//			//ArrayList<Integer> sharedVarValues = new ArrayList<Integer>();
//			
//			for(Variable s:finalVars){
//				
//				int this_ind = this._variables.indexOf(s);//index of variable in sepset in this factor
//				//sharedVarValues.add(values[this_ind]);//value of that variable
//				int that_ind = result._variables.indexOf(s);//index of variable in sepset in this factor
//				System.out.println("Looking for "+s+" in: "+result._variables);
//				for(Variable v: result._variables)
//					System.out.println(v +" to "+s + " = "+ v.equals(s));
//				f_indicies_of_values[that_ind] = values.get(this_ind);
//				
//			}
////			System.out.printf("this:%e result:%f\n", Math.exp(this.data.get(datum_idx)), 
////					Math.exp(result.data.get(result.index(f_indicies_of_values))));
//			result.data.set(result.index(f_indicies_of_values), 
//					Math.log(
//							Math.exp(result.data.get(result.index(f_indicies_of_values)))
//							+Math.exp(this.data.get(datum_idx))));
//			
//			
//		}
//		return result;
//	}
//
//	public void normalize(){
//		double Z = 0;
//		for(int i = 0; i<data.size(); i++){
//			Z += Math.exp(data.get(i));
//		}
//		
//		double logZ = Math.log(Z);
//		for(int i = 0; i<data.size(); i++){
//			data.set(i,data.get(i)-logZ);
//		}
//		
//	}
//	
//	public Factor reduce(ArrayList<Variable> heldVars, ArrayList<Integer> heldValues){
//		Factor result = new Factor(this.difference(heldVars));
//		
//		for(int i = 0; i<result.data.size(); i++){
//			try {
//				ArrayList<Variable> variablesOfLarger = (ArrayList<Variable>) result._variables.clone();
//				variablesOfLarger.addAll(heldVars);
//				ArrayList<Integer> varValues = result.valuesFromIndex(i);
//				varValues.addAll(heldValues);
//				try {
//					int indexOfLarger = this.index(variablesOfLarger,varValues);
//					result.data.set(i, this.data.get(indexOfLarger));
//				}
//				catch(FactorIndexException ex) {
//					// if we don't find the index of our desired variable
//					// (if the original doesn't have it), simply print the original
//					result.data.set(i, this.data.get(i));
//				}
//			} catch (FactorIndexException e) {
//				// this is from the 'valuesFromIndex' call,
//				// though it will never fail because of the for loop constraint
//				e.printStackTrace();
//			}
//		}
//		return result;
//	}
//	
//	/**
//	 * Creates and returns a key representation of this factor.
//	 * This is a string representation of the variables array list
//	 * @return a key representation of this factor
//	 */
//	protected String makeKey() {
//		return _variables.toString();
//
//	}
//	
//	public static Factor indicatorFunctionForNames(ArrayList<String> varNames, ArrayList<String> valueNames){
//		
//		ArrayList<HeldVariable> heldVars = new ArrayList<HeldVariable>();
//		for(int i = 0; i<varNames.size(); i++){
//			GlobalVariable temp = GlobalVariables.get(varNames.get(i));
//			heldVars.add(new HeldVariable(temp,temp.values.indexOf(valueNames.get(i))));//variableNamesToIndicies(varNames);
//		}
//		
//		return indicatorFunction(heldVars);
//	}
//	
//	public static Factor indicatorFunction(ArrayList<HeldVariable> vars){
//		Factor result = new Factor(vars,Math.log(0.0));
//		
//		for(int i = 0; i<result.data.size(); i++){
//			boolean match = true;
//			for(int varIdx = 0; varIdx < vars.size(); varIdx++){
//				int valueOfVarAtIndex = (i/result._variables.get(varIdx).stride)%vars.get(varIdx).card;
//				if(valueOfVarAtIndex != vars.get(varIdx).valueIndex){
//					match = false;
//					break;
//				}
//			}
//			if(match) result.data.set(i, Math.log(1.0));
//			else result.data.set(i, Math.log(0.0));
//		}
//		
//		return result;
//	}
//	
//	private void printData() {
//		for(int i = 0; i < data.size(); i++) {
//			System.out.print(Math.exp(data.get(i)));
//			System.out.print(", ");
//		}
//		System.out.println();
//	}
//	
//	public static void main(String args[]) throws Exception{
//		
//		if(true){
//		//Margin Test
//		ArrayList<String> A_vals = new ArrayList<String>();
//		A_vals.add("1");
//		A_vals.add("2");
//		A_vals.add("3");
//		
//		ArrayList<String> B_vals = new ArrayList<String>();
//		B_vals.add("1");
//		B_vals.add("2");
//		
//		Variable A = Factor.addVariable("A", A_vals);
//		Variable B = Factor.addVariable("B", B_vals);
//		Variable C = Factor.addVariable("C", B_vals);
//		System.out.println(Factor.variableInfo());
//		
//		String[] fac1_vars = {"A","B","C"}; 
//		Factor fac1 = new Factor(fac1_vars);
//		
//		fac1.putProbByValues(0.25, 0, 0, 0);
//		fac1.putProbByValues(0.35, 0, 0, 1);
//		fac1.putProbByValues(0.08, 0, 1, 0);
//		fac1.putProbByValues(0.16, 0, 1, 1);
//		fac1.putProbByValues(0.05, 1, 0, 0);
//		fac1.putProbByValues(0.07, 1, 0, 1);
//		fac1.putProbByValues(0.00, 1, 1, 0);
//		fac1.putProbByValues(0.00, 1, 1, 1);
//		fac1.putProbByValues(0.15, 2, 0, 0);
//		fac1.putProbByValues(0.21, 2, 0, 1);
//		fac1.putProbByValues(0.09, 2, 1, 0);
//		fac1.putProbByValues(0.18, 2, 1, 1);
//
//		System.out.println(fac1);
//		
//		ArrayList<Variable> elim_vars = new ArrayList<Variable>();
//		elim_vars.add(B);
//		Factor marginialzied = fac1.marginalize(elim_vars);
//		System.out.println(marginialzied);
//		System.out.println("== normalized ==");
//		marginialzied.normalize();
//		System.out.println(marginialzied);
//
//		}
//		
//		if(false){
//		//Division Test
//		ArrayList<String> A_vals = new ArrayList<String>();
//		A_vals.add("1");
//		A_vals.add("2");
//		A_vals.add("3");
//		
//		ArrayList<String> B_vals = new ArrayList<String>();
//		B_vals.add("1");
//		B_vals.add("2");
//		
//		ArrayList<String> C_vals = new ArrayList<String>();
//		C_vals.add("1");
//		C_vals.add("2");
//		
//		Factor.addVariable("A", A_vals);
//		Factor.addVariable("B", B_vals);
//		Factor.addVariable("C", C_vals);
//		System.out.println(Factor.variableInfo());
//		
//		String[] fac1_vars = {"A","B"}; 
//		Factor fac1 = new Factor(fac1_vars);
//		fac1.putProbByValues(.5, 0, 0);
//		fac1.putProbByValues(.2, 0, 1);
//		fac1.putProbByValues(0, 1, 0);
//		fac1.putProbByValues(1, 1, 1);
//		fac1.putProbByValues(.3, 2, 0);
//		fac1.putProbByValues(.45, 2, 1);
//		System.out.println(fac1);
//		
//		String[] fac2_vars = {"A"}; 
//		Factor fac2 = new Factor(fac2_vars);
//		fac2.putProbByValues(.8, 0);
//		fac2.putProbByValues(.0, 1);
//		fac2.putProbByValues(.6, 2);
//		System.out.println(fac2);
//		
//		String[] fac3_vars = {"A"}; 
//		Factor fac3 = new Factor(fac3_vars);
//		fac3.putProbByValues(.8, 0);
//		fac3.putProbByValues(.0, 1);
//		fac3.putProbByValues(.6, 2);
//		System.out.println(fac3);
//		
//		System.out.println("f1 / f2 = ");
//		System.out.println(fac1.divide(fac2));
//		}
//		
//		
//		if(false){
//		//Product Test
//		ArrayList<String> A_vals = new ArrayList<String>();
//		A_vals.add("1");
//		A_vals.add("2");
//		A_vals.add("3");
//		
//
//		ArrayList<String> B_vals = new ArrayList<String>();
//		B_vals.add("1");
//		B_vals.add("2");
//		
//		ArrayList<String> C_vals = new ArrayList<String>();
//		C_vals.add("1");
//		C_vals.add("2");
//		
//		Factor.addVariable("A", A_vals);
//		Factor.addVariable("B", B_vals);
//		Factor.addVariable("C", C_vals);
//		System.out.println(Factor.variableInfo());
//		
//		String[] fac1_vars = {"A","B"};
//		Factor fac1 = new Factor(fac1_vars);
//		fac1.putProbByValues(.5,0,0);
//		fac1.putProbByValues(.8,0,1);
//		fac1.putProbByValues(.1,1,0);
//		fac1.putProbByValues(.0,1,1);
//		fac1.putProbByValues(.3,2,0);
//		fac1.putProbByValues(.9,2,1);
//	
//		System.out.println(fac1);
//		
//		String[] fac2_vars = {"B","C"}; 
//		Factor fac2 = new Factor(fac2_vars);
//		fac2.putProbByValues(.5, 0,0);
//		fac2.putProbByValues(.7, 0,1);
//		fac2.putProbByValues(.1, 1,0);
//		fac2.putProbByValues(.2, 1,1);
//
//		System.out.println(fac2);
//		System.out.println(fac2.data.toString());
//		System.out.println("A x B = ");
//		System.out.println(fac1.product(fac2));
//		}
//		
////		if(false){
////		//Reduction test p 107 and 111
////		ArrayList<String> A_vals = new ArrayList<String>();
////		A_vals.add("1");
////		A_vals.add("2");
////		A_vals.add("3");
////		
////		ArrayList<String> B_vals = new ArrayList<String>();
////		B_vals.add("1");
////		B_vals.add("2");
////		
////		ArrayList<String> C_vals = new ArrayList<String>();
////		C_vals.add("1");
////		C_vals.add("2");
////		
////		ArrayList<String> D_vals = new ArrayList<String>();
////		D_vals.add("1");
////		D_vals.add("2");
////		
////		Factor.addVariable("A", A_vals);
////		Factor.addVariable("B", B_vals);
////		Factor.addVariable("C", C_vals);
////		Factor.addVariable("D", D_vals);
////		System.out.println(Factor.variableInfo());
////		
////		String[] fac1_vars = {"A","B","C"}; 
////		Factor fac1 = new Factor(fac1_vars);
////		fac1.putProbByValues(.25, 0,0,0);// 1 1 1
////		fac1.putProbByValues(.05, 1,0,0);// 2 1 1
////		fac1.putProbByValues(.15, 2,0,0);// 3 1 1
////		
////		fac1.putProbByValues(.08, 0,1,0);// 1 2 1 
////		fac1.putProbByValues(0, 1,1,0);  // 2 2 1
////		fac1.putProbByValues(.09, 2,1,0);// 3 2 1
////		
////		fac1.putProbByValues(.35, 0,0,1);// 1 1 2
////		fac1.putProbByValues(.07, 1,0,1);// 2 1 2
////		fac1.putProbByValues(.21, 2,0,1);// 3 1 2
////		
////		fac1.putProbByValues(.16, 0,1,1); //1 2 2
////		fac1.putProbByValues(0, 1,1,1);  //2 2 2
////		fac1.putProbByValues(.18, 2,1,1);//3 2 2
////		System.out.println(fac1);
////		{
////		System.out.println("reduce f1 by C=1");
////		ArrayList<String> heldVarStrs = new ArrayList<String>();
////		heldVarStrs.add("C");
////		ArrayList<String> heldVarValStrs = new ArrayList<String>();
////		heldVarValStrs.add("1");
////		ArrayList<Integer> heldVars = Factor.variableNamesToIndicies(heldVarStrs);
////		ArrayList<Integer> heldValues = Factor.valueNamesToIndicies(heldVarStrs, heldVarValStrs);
////		System.out.println(fac1.reduce(heldVars, heldValues));
////		}
////		
////			if(false){
////				System.out.println("reduce f1 by D=1");
////				ArrayList<String> heldVarStrs = new ArrayList<String>();
////				heldVarStrs.add("D");
////				ArrayList<String> heldVarValStrs = new ArrayList<String>();
////				heldVarValStrs.add("1");
////				ArrayList<Integer> heldVars = Factor.variableNamesToIndicies(heldVarStrs);
////				ArrayList<Integer> heldValues = Factor.valueNamesToIndicies(heldVarStrs, heldVarValStrs);
////				System.out.println(fac1.reduce(heldVars, heldValues));
////			}
////		}
//		if(true) {
//			System.out.println("normalize test");
//			ArrayList<String> X_vals = new ArrayList<String>();
//			X_vals.add("1");
//			X_vals.add("2");
////			X_vals.add("3");
//			
//			ArrayList<String> Y_vals = new ArrayList<String>();
//			Y_vals.add("1");
//			Y_vals.add("2");
//			
//			ArrayList<String> Z_vals = new ArrayList<String>();
//			Z_vals.add("1");
//			Z_vals.add("2");
//			
//			Factor.addVariable("X", X_vals);
//			Factor.addVariable("Y", Y_vals);
//			Factor.addVariable("Z", Z_vals);
//			System.out.println(Factor.variableInfo());
//			
//			String[] fac1_vars = {"X","Y","Z"}; 
//			Factor fac1 = new Factor(fac1_vars);
//			fac1.putProbByValues(65, 0,0,0);
//			fac1.putProbByValues(2, 0,0,1);
//			fac1.putProbByValues(3, 0,1,0);
//			fac1.putProbByValues(4, 0,1,1);
//			fac1.putProbByValues(5, 1,0,0);
//			fac1.putProbByValues(6, 1,0,1);
//			fac1.putProbByValues(7, 1,1,0);
//			fac1.putProbByValues(8, 1,1,1);
////			fac1.putProbByValues(9, 2,0,0);
////			fac1.putProbByValues(10, 2,0,1);
////			fac1.putProbByValues(11, 2,1,0);
////			fac1.putProbByValues(12, 2,1,1);
//			System.out.println("before normalize:");
//			System.out.println(fac1);
//			System.out.println("normalize:");
//			fac1.normalize();
//			System.out.println(fac1);
//		}
//	}
//	
//}
