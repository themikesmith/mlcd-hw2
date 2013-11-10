package elanmike.mlcd.hw2;

import java.util.ArrayList;
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
	
	
	private static ArrayList<String> _variableNames;
	private static ArrayList<ArrayList<String>> _variableValues;
	private static ArrayList<Integer> _variableCard;
	
	public static void addVariable(String varName, ArrayList<String> varValues){
		if(_variableNames == null) _variableNames = new ArrayList<String>();
		if(_variableValues == null) _variableValues = new ArrayList<ArrayList<String>>();
		if(_variableCard == null) _variableCard = new ArrayList<Integer>();
		
		_variableNames.add(varName);
		_variableValues.add(varValues);
		_variableCard.add(varValues.size());
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
	
	
	private ArrayList<Integer> _variables;
	private ArrayList<Integer> _stride;
	
	private ArrayList<Double> data;
	
	Factor(String[] varsNames){
		_variables= new ArrayList<Integer>(varsNames.length);
		for(int index=0; index < varsNames.length; index ++) 
			_variables.add(_variableNames.indexOf(varsNames[index]));
		
		this._stride = new ArrayList<Integer>(_variables.size());
		int strideTot = 1;
		for(int index:_variables){
			_stride.add(strideTot);
			strideTot*=_variableCard.get(index);
		}
		
		this.data = new ArrayList<Double>(strideTot);
		for(int i = 0; i<strideTot; i++) data.add(0.0);
	}
	
	private Factor(ArrayList<Integer> vars){
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
	
	public void addJointProbByName(String[] varVals, double prob) throws Exception{
		if(varVals.length != _variables.size()) 
			throw new Exception("InputLengthError: indexLength("+varVals.length+") does not match number of variables for this factor("+_variables.size()+")");
		int[] valVarsIndicies = new int[varVals.length];
		for(int varIdx=0; varIdx < varVals.length; varIdx ++){
			int integerValOfString = _variableValues.get(varIdx).indexOf(varVals[varIdx]);
			if(integerValOfString <0)
				throw new Exception("ValueError: Value("+varVals[varIdx]+") not applicable for "+_variableNames.get(varIdx));
			valVarsIndicies[varIdx] = integerValOfString;
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
			output += _variableNames.get(var_idx) + " ";
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
	
	public void putProbByValues(int[] variableValues,double prob) throws Exception{
		
		int searchIndex = 0;
		if(variableValues.length != _variables.size()) 
			throw new Exception("FactorIndexError: indexLength("+variableValues.length+") does not match number of variables for this factor("+_variables.size()+")");
		for(int index=0; index < _variables.size(); index ++)
			if(variableValues[index] >= _variableCard.get(_variables.get(index))|| variableValues[index]< 0)
				throw new Exception("FactorIndexError: variableValue("+variableValues[index]+") was not in the valid range of ( 0 - "+(_variableCard.get(_variables.get(index))-1)+")");
			else
				searchIndex += variableValues[index]*_stride.get(index);
		
		data.set(searchIndex,prob);
	}
	
	public double getProbByValues(int[] variableValues) throws Exception{
		return data.get(index(variableValues));
	}
	
	public ArrayList<Integer> intersection(Factor f){
		Set<Integer> myVars = new TreeSet<Integer>(this._variables);
		Set<Integer> theirVars = new TreeSet<Integer>(f._variables);
		
		myVars.retainAll(theirVars);
		ArrayList<Integer> intersection = new ArrayList<Integer>();
		for(Object o:myVars.toArray())
			intersection.add((Integer) o);
		return intersection;
	}
	
	public ArrayList<Integer> difference(Factor f){
		Set<Integer> myVars = new TreeSet<Integer>(this._variables);
		Set<Integer> theirVars = new TreeSet<Integer>(f._variables);
		
		myVars.removeAll(theirVars);
		ArrayList<Integer> difference = new ArrayList<Integer>();
		
		for(Object o:myVars.toArray())
			difference.add((Integer) o);
		return difference;
	}
	
	public ArrayList<Integer> union(Factor f){
		Set<Integer> myVars = new TreeSet<Integer>(this._variables);
		Set<Integer> theirVars = new TreeSet<Integer>(f._variables);
		
		myVars.addAll(theirVars);
		ArrayList<Integer> union = new ArrayList<Integer>();
		
		for(Object o:myVars.toArray())
			union.add((Integer) o);
		return union;
	}
	

	public Factor product(Factor f) throws Exception{
		ArrayList<Integer> unionScope = this.union(f);
		Factor psi = new Factor(unionScope);
		
		
		
		int j =0;
		int k =0;
		int[] assigment = new int[unionScope.size()];
		
		for(int i = 0; i < psi.data.size(); i++){
			psi.data.set(i, this.data.get(j)*f.data.get(k));
			for(int l =0; l < unionScope.size(); l++){
				assigment[l]++;
				if(assigment[l]==_variableCard.get(l)){
					assigment[l]=0;
					j = j-(_variableCard.get(l)-1)*this._stride.get(l);
					k = k-(_variableCard.get(l)-1)*f._stride.get(l);
				}else{
					j = j + this._stride.get(l);
					k = k + f._stride.get(l);
					break;
				}
			}
		}
		
		
		return psi;
	}
	
	
	public Factor divided(Factor f) throws Exception{
		if(!this._variables.containsAll(f._variables))
			throw new Exception("DivionError: Numerator does not contain Denominator");
		
		Factor result = new Factor(this._variables);
		
		ArrayList<Integer> sepset = this.intersection(f);
		ArrayList<Integer> cliqueMinusSepset = this.difference(f);
		
		//new ArrayList<Integer>()
		
		iterate(new ArrayList<Integer>(),new ArrayList<Integer>(), 0, new int[_variables.size()], new Callback() {
			   public void iterate(int[] curValue) {
			        try {
						System.out.println(data.get(index(curValue)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			   }
			});
	
		for(int common_var_idx = 0; common_var_idx<sepset.size(); common_var_idx++){
			
		}
		
		return result;
	}
	
	public static void main(String args[]) throws Exception{
		
		
		ArrayList<String> A_vals = new ArrayList<String>();
		A_vals.add("1");
		A_vals.add("2");
		A_vals.add("3");
		
		ArrayList<String> B_vals = new ArrayList<String>();
		B_vals.add("1");
		B_vals.add("2");
		
		
		Factor.addVariable("A", A_vals);
		Factor.addVariable("B", B_vals);
		System.out.println(Factor.variableInfo());
		
		String[] fac1_vars = {"A","B"}; 
		Factor fac1 = new Factor(fac1_vars);
		fac1.addJointProbByIndex(0, .5);
		fac1.addJointProbByIndex(1, 0);
		fac1.addJointProbByIndex(2, .3);
		fac1.addJointProbByIndex(3, .2);
		fac1.addJointProbByIndex(4, 0);
		fac1.addJointProbByIndex(5, .45);
		System.out.println(fac1);
		
		String[] fac2_vars = {"A"}; 
		Factor fac2 = new Factor(fac2_vars);
		fac2.addJointProbByIndex(0, .8);
		fac2.addJointProbByIndex(1, 0);
		fac2.addJointProbByIndex(2, .6);
		System.out.println(fac2);
		
		for(int i:fac1.intersection(fac2))
			System.out.println(_variableNames.get(i));
		
		
		//fac1.divided(fac2);
		
		System.out.println(fac1.product(fac2));
		
		
		/*
		ArrayList<String> A_vals = new ArrayList<String>();
		A_vals.add("Yes");
		A_vals.add("No");
		Factor.addVariable("A", A_vals);
		Factor.addVariable("B", A_vals);
		System.out.println(Factor.variableInfo());
		
		String[] fac1_vars = {"A","B"}; 
		String[] values = {"Yes","No"};
		Factor fac1 = new Factor(fac1_vars);
		fac1.addJointProbByName(values, .5);
		fac1.addJointProbByIndex(0, .5);
		System.out.println(fac1);
		*/
		
		
	}
	
}
