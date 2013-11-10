package elanmike.mlcd.hw2;

import java.util.ArrayList;

public class Factor {
	private static ArrayList<String> _variableNames;
	private static ArrayList<Integer> _variableCard;
	
	public static void defineVariables(){
		
	}
	
	private int[] _variables;
	private int _stride;
	
	private double[]data;
	
	Factor(String[] varsNames){
		_variables=new int[varsNames.length];
		for(int i=0; i < varsNames.length; i ++) 
			_variables[i] = _variableNames.indexOf(varsNames[i]);
		
		_stride = 1;
		for(int index:_variables) 
			_stride*=_variableCard.get(index); 
	}
	
	
}
