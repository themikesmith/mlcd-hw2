package elanmike.mlcd.hw2;

import java.util.ArrayList;

import com.sun.tools.javac.util.Pair;

public class ObservationModel {
	private final int num_landmarks,num_row,num_col;
	
	
	ObservationModel(int num_row, int num_col, int num_landmarks){
		this.num_row = num_row;
		this.num_col = num_col;
		this.num_landmarks = num_landmarks;
		
		Pair<ArrayList<Integer>,Integer>[][] observationEntries = new Pair[this.num_row][this.num_col];
		
		for(int i=0; i<num_row;i++){
			for(int j=0; j<num_col;j++){
				Pair<ArrayList<Integer>,Integer> newEntry =  new Pair<ArrayList<Integer>,Integer>(new ArrayList<Integer>(4*(1+num_landmarks)), 0);
				for(int k=0; k<4*(1+num_landmarks); k++){newEntry.fst.add(0);}
				
				observationEntries[i][j] = newEntry;
			}
		}
		 
	}
	
	
	
	
	
}
