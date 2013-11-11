package elanmike.mlcd.hw2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class Factor {
	
	public class Pair<A, B> {
	    public A first;
	    public B second;

	    public Pair(A first, B second) {
	    	super();
	    	this.first = first;
	    	this.second = second;
	    }
	}
	
	
	protected static ArrayList<String> _variableNames;
	protected static ArrayList<ArrayList<String>> _variableValues;
	protected static ArrayList<Integer> _variableCard;
	
	public static void addVariable(String varName, ArrayList<String> varValues){
		if(_variableNames == null) _variableNames = new ArrayList<String>();
		if(_variableValues == null) _variableValues = new ArrayList<ArrayList<String>>();
		if(_variableCard == null) _variableCard = new ArrayList<Integer>();
		
		_variableNames.add(varName);
		_variableValues.add(varValues);
		_variableCard.add(varValues.size());
	}
	
	public static int getVariableIndex(String var){
		if(_variableNames == null)
			return -1;
		return _variableNames.indexOf(var);
	}
	public static String getVariableName(int i){
		if(_variableNames == null)
			return "";
		return _variableNames.get(i);
	}
	public static ArrayList<Integer> variableNamesToIndicies(ArrayList<String> variables){
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for(String s:variables){
			indicies.add(_variableNames.indexOf(s));
		}
		return indicies;
	}
	public static ArrayList<Integer> variableNamesToIndicies(String[] variables){
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		for(String s:variables){
			indicies.add(_variableNames.indexOf(s));
		}
		return indicies;
	}
	public static ArrayList<String> variableIndicesToNames(ArrayList<Integer> variables){
		ArrayList<String> names = new ArrayList<String>();
		for(int s:variables){
			names.add(_variableNames.get(s));
		}
		return names;
	}
	public static ArrayList<String> variableIndicesToNames(int... variables){
		ArrayList<String> names = new ArrayList<String>();
		for(int s:variables){
			names.add(_variableNames.get(s));
		}
		return names;
	}
	
	public static int getVariableValueIndex(int varIdx, String val){
		if(_variableValues == null)
			return -1;
		return _variableValues.indexOf(val);
	}
	public static String getVariableName(int varIdx, int valueIdx){
		if(_variableNames == null)
			return "";
		return _variableValues.get(varIdx).get(valueIdx);
	}
	public static ArrayList<Integer> valueNamesToIndicies(ArrayList<String> variables, ArrayList<String> var_value){
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		
		for(int i = 0; i < variables.size(); i++){
			indicies.add(_variableValues.get(getVariableIndex(variables.get(i))).indexOf(var_value.get(i)));
		}
		return indicies;
	}
	
	public static String variableInfo(){
		String output = "";
		if(_variableNames == null){
			output += "_variableNames is null.\n";
		}
		if(_variableValues == null){
			output += "_variableValues is null.\n";
		}
		if(_variableCard == null){
			output += "_variableCard is null.\n";
		}
		if(_variableNames != null && _variableValues != null && _variableCard != null ){
			output+= "_variableNames size: " + _variableNames.size() +"\n";
			for(int varIdx = 0; varIdx<_variableNames.size(); varIdx++){
				output+= _variableNames.get(varIdx) + " size(" + _variableCard.get(varIdx) +")[";
				for(int valIdx = 0; valIdx < _variableValues.get(varIdx).size(); valIdx++){
					output+=" "+_variableValues.get(varIdx).get(valIdx);
				}
				output+=" ]\n";
			}
		}
		
		return output;
	}
	
	protected ArrayList<Integer> _variables;
	protected ArrayList<Integer> _stride;
	
	protected ArrayList<Double> data;
	
	Factor(String[] varsNames){
		_variables= new ArrayList<Integer>(varsNames.length);
		for(int index=0; index < varsNames.length; index ++) 
			_variables.add(_variableNames.indexOf(varsNames[index]));
		
		Collections.sort(_variables);
		
		this._stride = new ArrayList<Integer>(_variables.size());
		int strideTot = 1;
		for(int index:_variables){
			_stride.add(strideTot);
			strideTot*=_variableCard.get(index);
		}
		
		this.data = new ArrayList<Double>(strideTot);
		for(int i = 0; i<strideTot; i++) data.add(1.0);
	}
	
	protected Factor(ArrayList<Integer> vars){
		this._variables = vars;
		
		this._stride = new ArrayList<Integer>(_variables.size());
		int strideTot = 1;
		for(int index:_variables){
			_stride.add(strideTot);
			strideTot*=_variableCard.get(index);
		}
		
		this.data = new ArrayList<Double>(strideTot);
		for(int i = 0; i<strideTot; i++) data.add(0.0);
	}
	
	private int getInternalIndex(int globalIndex){
		return _variables.indexOf(globalIndex);
	}
	
	interface Callback {
		void iterate(int[] curValue); // n-dimensional point
	}
	void iterate(ArrayList<Integer> heldVariables, ArrayList<Integer> heldValues, int currentDimension, int[] js, Callback c) {
		for (int i = 0; i < _variableCard.get(currentDimension); i++) {
			if(heldVariables.contains(i)){
				js[currentDimension] =  heldValues.get(heldVariables.indexOf(i));
			}else{
				js[currentDimension] =  i;
			}
			
	        if (currentDimension == js.length - 1) c.iterate(js);
	        else iterate(heldVariables,heldValues, currentDimension + 1, js, c);

		}
	}

	private int index(int[] variableValues) throws Exception{
		int searchIndex = 0;
		if(variableValues.length != _variables.size()) 
			throw new Exception("FactorIndexError: indexLength("+variableValues.length+") does not match number of variables for this factor("+_variables.size()+")");
		for(int index=0; index < _variables.size(); index ++)
			if(variableValues[index] >= _variableCard.get(_variables.get(index))|| variableValues[index]< 0)
				throw new Exception("FactorIndexError: variableValue("+variableValues[index]+") was not in the valid range of ( 0 - "+(_variableCard.get(_variables.get(index))-1)+")");
			else
				searchIndex += variableValues[index]*_stride.get(index);
		
		return searchIndex;
	}
	
	private int[] valuesFromIndex(int datum_index) throws Exception{
		if(datum_index >= data.size()|| datum_index< 0)
			throw new Exception("FactorIndexError: index("+datum_index+") was not in the valid range of ( 0 - "+(data.size()-1)+")");
		
		int values[] = new int[_variables.size()];
		
		for(int varIdx = 0; varIdx< _variables.size(); varIdx++){
			values[varIdx] = (datum_index/_stride.get(varIdx))%_variableCard.get(varIdx);
		}
		
		return values;
	}
	
	public void addJointProbByName(String[] varVals, double prob) throws Exception{
		if(varVals.length != _variables.size()) 
			throw new Exception("InputLengthError: indexLength("+varVals.length+") does not match number of variables for this factor("+_variables.size()+")");
		ArrayList<Integer> valVarsIndicies = new ArrayList<Integer>();
		for(int varIdx=0; varIdx < varVals.length; varIdx ++){
			int integerValOfString = _variableValues.get(varIdx).indexOf(varVals[varIdx]);
			if(integerValOfString <0)
				throw new Exception("ValueError: Value("+varVals[varIdx]+") not applicable for "+_variableNames.get(varIdx));
			valVarsIndicies.add(integerValOfString);
		}
		putProbByValues(valVarsIndicies,prob);
		
	}
	
	public void addJointProbByIndex(int datum_index, double prob) throws Exception{
		if(datum_index < 0 || datum_index > data.size()) 
			throw new Exception("InputLengthError: datumIndex("+datum_index+") is not in range ( 0 - "+_variables.size()+")");
		
		data.set(datum_index,prob);
		
	}
	
	public String toString(){
		String output = "Phi( ";
		for(int var_idx=0; var_idx < _variables.size(); var_idx ++)
			output += _variableNames.get(_variables.get(var_idx)) + " ";
		output += ")\n";
		
		for(int var_idx=0; var_idx < _variables.size(); var_idx ++)
			output += _variableNames.get(var_idx) + "\t";
		output += "Value\n";
		
		for(int datum_index = 0; datum_index < data.size(); datum_index++){
			for(int var_idx=0; var_idx < _variables.size(); var_idx ++){
				output += String.format("%01d       ", ((datum_index/_stride.get(var_idx))%_variableCard.get(var_idx)));
			}
			output += data.get(datum_index)+"\n";
		}
		
		return output;
	}
	
	public void putProbByValues(ArrayList<Integer> arrayList,double prob) throws Exception{
		
		int searchIndex = 0;
		if(arrayList.size() != _variables.size()) 
			throw new Exception("FactorIndexError: indexLength("+arrayList.size()+") does not match number of variables for this factor("+_variables.size()+")");
		for(int index=0; index < _variables.size(); index ++)
			if(arrayList.get(index) >= _variableCard.get(_variables.get(index))|| arrayList.get(index)< 0)
				throw new Exception("FactorIndexError: variableValue("+arrayList.get(index)+") was not in the valid range of ( 0 - "+(_variableCard.get(_variables.get(index))-1)+")");
			else
				searchIndex += arrayList.get(index)*_stride.get(index);
		
		data.set(searchIndex,prob);
	}
	
	public double getProbByValues(int[] variableValues) throws Exception{
		return data.get(index(variableValues));
	}
	
	public ArrayList<Integer> intersection(ArrayList<Integer> other){
		Set<Integer> myVars = new TreeSet<Integer>(this._variables);
		Set<Integer> theirVars = new TreeSet<Integer>(other);
		
		myVars.retainAll(theirVars);
		ArrayList<Integer> intersection = new ArrayList<Integer>();
		for(Object o:myVars.toArray())
			intersection.add((Integer) o);
		return intersection;
	}
	
	public ArrayList<Integer> difference(ArrayList<Integer> other){
		Set<Integer> myVars = new TreeSet<Integer>(this._variables);
		Set<Integer> theirVars = new TreeSet<Integer>(other);
		
		myVars.removeAll(theirVars);
		ArrayList<Integer> difference = new ArrayList<Integer>();
		
		for(Object o:myVars.toArray())
			difference.add((Integer) o);
		return difference;
	}
	
	public ArrayList<Integer> union(ArrayList<Integer> other){
		Set<Integer> myVars = new TreeSet<Integer>(this._variables);
		Set<Integer> theirVars = new TreeSet<Integer>(other);
		
		myVars.addAll(theirVars);
		ArrayList<Integer> union = new ArrayList<Integer>();
		
		for(Object o:myVars.toArray())
			union.add((Integer) o);
		return union;
	}
	
	public Factor product(Factor f) throws Exception{
		ArrayList<Integer> unionScope = this.union(f._variables);
		Factor psi = new Factor(unionScope);
		
		
		int j =0;
		int k =0;
		int[] assigment = new int[unionScope.size()];
		
		for(int i = 0; i < psi.data.size(); i++){
			psi.data.set(i, this.data.get(j)*f.data.get(k));
			for(int l =0; l < unionScope.size(); l++){
				
				assigment[l]++;
				
				if(assigment[l]==_variableCard.get(unionScope.get(l))){
					assigment[l]=0;
					if(this._variables.contains(unionScope.get(l)))
						j = j-(_variableCard.get(unionScope.get(l))-1)*this._stride.get(this.getInternalIndex(unionScope.get(l)));
					if(f._variables.contains(unionScope.get(l)))
						k = k-(_variableCard.get(unionScope.get(l))-1)*f._stride.get(f.getInternalIndex(unionScope.get(l)));
				}else{
					if(this._variables.contains(unionScope.get(l)))
						j = j + this._stride.get(this.getInternalIndex(unionScope.get(l)));
					if(f._variables.contains(unionScope.get(l)))
						k = k + f._stride.get(f.getInternalIndex(unionScope.get(l)));
					break;
				}
			}
		}
		
		
		return psi;
	}
	
	public Factor divide(Factor f) throws Exception{
		if(!this._variables.containsAll(f._variables))
			throw new Exception("DivionError: Numerator does not contain Denominator");
		
		Factor result = new Factor(this._variables);
		
		ArrayList<Integer> sepset = this.intersection(f._variables);
		

		for(int datum_idx = 0; datum_idx < this.data.size(); datum_idx++ ){
			int[] values = valuesFromIndex(datum_idx);
			ArrayList<Integer> sharedVarValues = new ArrayList<Integer>();
			
			int[] f_indicies_of_values = new int[sepset.size()];
			for(int s:sepset){
				int this_ind = this._variables.indexOf(s);//index of variable in sepset in this factor
				//sharedVarValues.add(values[this_ind]);//value of that variable
				int that_ind = f._variables.indexOf(s);//index of variable in sepset in this factor
				f_indicies_of_values[that_ind] = values[this_ind];
			}
			
			if(this.data.get(datum_idx) == 0.0 && f.getProbByValues(f_indicies_of_values) == 0.0 ){
				result.data.set(datum_idx, 0.0);
			}else{
				result.data.set(datum_idx, this.data.get(datum_idx)/f.getProbByValues(f_indicies_of_values));
			}
			
		}
		
		return result;
	}
	
	public Factor marginalize(ArrayList<Integer> elimVar) throws Exception{
		ArrayList<Integer> finalVars = difference(elimVar);
		
		Factor result = new Factor(finalVars);
		for(int datum_idx = 0; datum_idx < this.data.size(); datum_idx++ ){
			int[] values = valuesFromIndex(datum_idx);
			
			int[] f_indicies_of_values = new int[finalVars.size()];
			ArrayList<Integer> sharedVarValues = new ArrayList<Integer>();
			
			for(int s:finalVars){
				
				int this_ind = this._variables.indexOf(s);//index of variable in sepset in this factor
				//sharedVarValues.add(values[this_ind]);//value of that variable
				int that_ind = result._variables.indexOf(s);//index of variable in sepset in this factor
				f_indicies_of_values[that_ind] = values[this_ind];
				
			}
			result.data.set(result.index(f_indicies_of_values), result.data.get(result.index(f_indicies_of_values))+this.data.get(datum_idx));
			
		}
		return result;
	}
	/**
	 * Creates and returns a key representation of this factor.
	 * This is a string representation of the variables array list
	 * @return a key representation of this factor
	 */
	protected String makeKey() {
		return _variables.toString();
	}
	
//	public static void main(String args[]) throws Exception{
//		
//		
//		//Division Test
//				ArrayList<String> A_vals = new ArrayList<String>();
//				A_vals.add("1");
//				A_vals.add("2");
//				A_vals.add("3");
//				
//				ArrayList<String> B_vals = new ArrayList<String>();
//				B_vals.add("1");
//				B_vals.add("2");
//				
//				Factor.addVariable("A", A_vals);
//				Factor.addVariable("B", B_vals);
//				Factor.addVariable("C", B_vals);
//				System.out.println(Factor.variableInfo());
//				
//				String[] fac1_vars = {"A","B","C"}; 
//				Factor fac1 = new Factor(fac1_vars);
//				fac1.addJointProbByIndex(0, .25);// 1 1 1
//				fac1.addJointProbByIndex(1, .05);// 2 1 1
//				fac1.addJointProbByIndex(2, .15);// 3 1 1
//				
//				fac1.addJointProbByIndex(3, .08);// 1 2 1 
//				fac1.addJointProbByIndex(4, 0);  // 2 2 1
//				fac1.addJointProbByIndex(5, .09);// 3 2 1
//				
//				fac1.addJointProbByIndex(6, .35);// 1 1 2
//				fac1.addJointProbByIndex(7, .07);// 2 1 2
//				fac1.addJointProbByIndex(8, .21);// 3 1 2
//				
//				fac1.addJointProbByIndex(9, .16); //1 2 2
//				fac1.addJointProbByIndex(10, 0);  //2 2 2
//				fac1.addJointProbByIndex(11, .18);//3 2 2
//				System.out.println(fac1);
//				
//				ArrayList<Integer> elim_vars = new ArrayList<Integer>();
//				elim_vars.add(1);
//				System.out.println(fac1.marginalize(elim_vars));
//		
//		/*
//		 
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
//		Factor.addVariable("A", A_vals);
//		Factor.addVariable("B", B_vals);
//		System.out.println(Factor.variableInfo());
//		
//		String[] fac1_vars = {"A","B"}; 
//		Factor fac1 = new Factor(fac1_vars);
//		fac1.addJointProbByIndex(0, .5);
//		fac1.addJointProbByIndex(1, 0);
//		fac1.addJointProbByIndex(2, .3);
//		fac1.addJointProbByIndex(3, .2);
//		fac1.addJointProbByIndex(4, 0);
//		fac1.addJointProbByIndex(5, .45);
//		System.out.println(fac1);
//		
//		String[] fac2_vars = {"A"}; 
//		Factor fac2 = new Factor(fac2_vars);
//		fac2.addJointProbByIndex(0, .8);
//		fac2.addJointProbByIndex(1, .0);
//		fac2.addJointProbByIndex(2, .6);
//		System.out.println(fac2);
//		
//		
//		System.out.println(fac1.divide(fac2));
//		*/
//		
//		
//		
//		/*//Product Test
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
//		fac1.addJointProbByIndex(0, .5);
//		fac1.addJointProbByIndex(1, .1);
//		fac1.addJointProbByIndex(2, .3);
//		fac1.addJointProbByIndex(3, .8);
//		fac1.addJointProbByIndex(4, 0);
//		fac1.addJointProbByIndex(5, .9);
//		System.out.println(fac1);
//		
//		String[] fac2_vars = {"B","C"}; 
//		Factor fac2 = new Factor(fac2_vars);
//		fac2.addJointProbByIndex(0, .5);
//		fac2.addJointProbByIndex(1, .1);
//		fac2.addJointProbByIndex(2, .7);
//		fac2.addJointProbByIndex(3, .2);
//		System.out.println(fac2);
//		
//		
//		System.out.println(fac1.product(fac2));
//		
//		*/
//	}
	
}
