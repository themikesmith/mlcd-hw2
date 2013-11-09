package elanmike.mlcd.hw2;

import java.util.ArrayList;

public class ObservationModel {
	public class Pair<A, B> {
	    public A first;
	    public B second;

	    public Pair(A first, B second) {
	    	super();
	    	this.first = first;
	    	this.second = second;
	    }
	}
	
	private final int num_landmarks,num_row,num_col;
	private Pair<ArrayList<Integer>,Integer>[][] observationEntries;
	
	ObservationModel(int num_row, int num_col, int num_landmarks){
		this.num_row = num_row;
		this.num_col = num_col;
		this.num_landmarks = num_landmarks;
		
		observationEntries = new Pair[this.num_row][this.num_col];
		
		for(int i=0; i<num_row;i++){
			for(int j=0; j<num_col;j++){
				Pair<ArrayList<Integer>,Integer> newEntry =  new Pair<ArrayList<Integer>,Integer>(new ArrayList<Integer>(4*(1+num_landmarks)), 0);
				for(int k=0; k<4*(1+num_landmarks); k++){newEntry.first.add(0);}
				
				observationEntries[i][j] = newEntry;
			}
		}
	}
	
	public void addObservation(int row, int col, boolean[] observations){
		if(observations.length != observationEntries[row][col].first.size())
			System.err.println("Error length of observation array != " + observationEntries[row][col].first.size() );
		else{
			
			for(int i=0; i<observations.length; i++){
				if(observations[i])
					observationEntries[row][col].first.set(i, observationEntries[row][col].first.get(i)+1); 
			}
			observationEntries[row][col].second++;
			
		}
	}	
	
	public double getWallObservation(int row, int col, int dir){		
		return observationEntries[row][col].first.get(dir)/observationEntries[row][col].second;
	}
	
	public double getLandmarkObservation(int row, int col, int land_num, int dir){		
		return observationEntries[row][col].first.get(4*land_num+dir)/observationEntries[row][col].second;
	}
	
}
