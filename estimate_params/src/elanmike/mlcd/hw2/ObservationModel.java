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
				Pair<ArrayList<Integer>,Integer> newEntry =  new Pair<ArrayList<Integer>,Integer>(new ArrayList<Integer>(4*(1+num_landmarks)), 2);
				for(int k=0; k<4*(1+num_landmarks); k++){newEntry.first.add(1);}
				
				observationEntries[i][j] = newEntry;
			}
		}
	}
	
	public void addObservation(int row, int col, boolean[] observations){
		int r = row-1;
		int c = col-1;
		if(observations.length != observationEntries[r][c].first.size())
			System.err.println("Error length of observation array != " + observationEntries[r][c].first.size() );
		else{
			
			for(int i=0; i<observations.length; i++){
				if(observations[i])
					observationEntries[r][c].first.set(i, observationEntries[r][c].first.get(i)+1); 
			}
			observationEntries[r][c].second++;
			
		}
	}	
	
	public double getWallObservation(int row, int col, int dir){
		int r = row-1;
		int c = col-1;
		return (double)observationEntries[r][c].first.get(dir)/(double)observationEntries[r][c].second;
	}
	
	public double getLandmarkObservation(int row, int col, int land_num, int dir){	
		int r = row-1;
		int c = col-1;
		return (double)observationEntries[r][c].first.get(4*land_num+dir)/(double)observationEntries[r][c].second;
	}
	
	/*
	public void addLambdaSmoothing(int lambda){
		for(int i=0; i<num_row;i++){
			for(int j=0; j<num_col;j++){
				
			}
		}
	}*/
	
	public void printWallDebug(){
		
		System.out.println("===NorthWallObs===");
		for(int i=1; i<num_row+1;i++){
			for(int j=1; j<num_col+1;j++){
				System.out.printf("%.2f   ", getWallObservation(i,j,Constants.DIR.NORTH.ordinal()));;
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("===SouthWallObs===");
		for(int i=1; i<num_row+1;i++){
			for(int j=1; j<num_col+1;j++){
				System.out.printf("%.2f   ", getWallObservation(i,j,Constants.DIR.SOUTH.ordinal()));;
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("===EastWallObs===");
		for(int i=1; i<num_row+1;i++){
			for(int j=1; j<num_col+1;j++){
				System.out.printf("%.2f   ", getWallObservation(i,j,Constants.DIR.EAST.ordinal()));;
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("===WestWallObs===");
		for(int i=1; i<num_row+1;i++){
			for(int j=1; j<num_col+1;j++){
				System.out.printf("%.2f   ", getWallObservation(i,j,Constants.DIR.WEST.ordinal()));;
			}
			System.out.println();
		}
	}
	
	/*
	public static void main(String[] args){
		ObservationModel obsmod = new ObservationModel(5,5,2);
		obsmod.printWallDebug();
		
		boolean[] obs = new boolean[4*(1+2)];
		obs[1] = true;
		obsmod.addObservation(0, 0, obs);
		
		obsmod.printWallDebug();
	}
	*/
	
}
