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
		return (double)observationEntries[row][col].first.get(dir)/(double)observationEntries[row][col].second;
	}
	
	public double getLandmarkObservation(int row, int col, int land_num, int dir){		
		return (double)observationEntries[row][col].first.get(4*land_num+dir)/(double)observationEntries[row][col].second;
	}
	
	/*
	public void addLambdaSmoothing(int lambda){
		for(int i=0; i<num_row;i++){
			for(int j=0; j<num_col;j++){
				
			}
		}
	}*/
	
	public void printWallDebug(){
		
		System.out.println();
		System.out.println("===NorthWallObs===");
		
		for(int i=num_row-1; i>=0;i--){
			System.out.printf("%04d   ", (i+1));
			for(int j=0; j<num_col;j++){
				System.out.printf("%.2f   ", getWallObservation(i,j,Constants.DIR.NORTH.ordinal()));;
			}
			System.out.println();
		}
		System.out.printf("       ");
		for(int i=0; i<num_row;i++)System.out.printf("%04d   ", (i+1));
		System.out.printf("\n");
		
		System.out.println();
		System.out.println("===SouthWallObs===");
		for(int i=num_row-1; i>=0;i--){
			System.out.printf("%04d   ", (i+1));
			for(int j=0; j<num_col;j++){
				System.out.printf("%.2f   ", getWallObservation(i,j,Constants.DIR.SOUTH.ordinal()));;
			}
			System.out.println();
		}
		System.out.printf("       ");
		for(int i=0; i<num_row;i++)System.out.printf("%04d   ", (i+1));
		System.out.printf("\n");
		
		System.out.println();
		System.out.println("===EastWallObs===");
		for(int i=num_row-1; i>=0;i--){
			System.out.printf("%04d   ", (i+1));
			for(int j=0; j<num_col;j++){
				System.out.printf("%.2f   ", getWallObservation(i,j,Constants.DIR.EAST.ordinal()));;
			}
			System.out.println();
		}
		System.out.printf("       ");
		for(int i=0; i<num_row;i++)System.out.printf("%04d   ", (i+1));
		System.out.printf("\n");
		
		System.out.println();
		System.out.println("===WestWallObs===");
		for(int i=num_row-1; i>=0;i--){
			System.out.printf("%04d   ", (i+1));
			for(int j=0; j<num_col;j++){
				System.out.printf("%.2f   ", getWallObservation(i,j,Constants.DIR.WEST.ordinal()));;
			}
			System.out.println();
		}
		System.out.printf("       ");
		for(int i=0; i<num_row;i++)System.out.printf("%04d   ", (i+1));
		System.out.printf("\n");
		
	}
		
	public void printLandmarkDebug(){
			
		for(int lmk = 1; lmk <= num_landmarks; lmk++){
		
			System.out.println();
			System.out.println("===Landmark("+lmk+")North Obs===");
			
			for(int i=num_row-1; i>=0;i--){
				System.out.printf("%04d   ", (i+1));
				for(int j=0; j<num_col;j++){
					System.out.printf("%.2f   ", getLandmarkObservation(i,j,lmk,Constants.DIR.NORTH.ordinal()));;
				}
				System.out.println();
			}
			System.out.printf("       ");
			for(int i=0; i<num_row;i++)System.out.printf("%04d   ", (i+1));
			System.out.printf("\n");
			
			System.out.println();
			System.out.println("===Landmark("+lmk+")South Obs===");
			for(int i=num_row-1; i>=0;i--){
				System.out.printf("%04d   ", (i+1));
				for(int j=0; j<num_col;j++){
					System.out.printf("%.2f   ", getLandmarkObservation(i,j,lmk,Constants.DIR.SOUTH.ordinal()));;
				}
				System.out.println();
			}
			System.out.printf("       ");
			for(int i=0; i<num_row;i++)System.out.printf("%04d   ", (i+1));
			System.out.printf("\n");
			
			System.out.println();
			System.out.println("===Landmark("+lmk+")East Obs===");
			for(int i=num_row-1; i>=0;i--){
				System.out.printf("%04d   ", (i+1));
				for(int j=0; j<num_col;j++){
					System.out.printf("%.2f   ", getLandmarkObservation(i,j,lmk,Constants.DIR.EAST.ordinal()));;
				}
				System.out.println();
			}
			System.out.printf("       ");
			for(int i=0; i<num_row;i++)System.out.printf("%04d   ", (i+1));
			System.out.printf("\n");
			
			System.out.println();
			System.out.println("===Landmark("+lmk+")West Obs===");
			for(int i=num_row-1; i>=0;i--){
				System.out.printf("%04d   ", (i+1));
				for(int j=0; j<num_col;j++){
					System.out.printf("%.2f   ", getLandmarkObservation(i,j,lmk,Constants.DIR.WEST.ordinal()));;
				}
				System.out.println();
			}
			System.out.printf("       ");
			for(int i=0; i<num_row;i++)System.out.printf("%04d   ", (i+1));
			System.out.printf("\n");
		}
			
	}
	
	public String formattedCpdOutput(int timestamp){
		String output = "";
		
		for(int r = 0; r<num_row; r++){
			for(int c = 0; c<num_col; c++){
				//walls
				//e.g. ObserveWall_N_8=Yes PositionRow_8=9,PositionCol_8=2 0.866634
				for(int i = 0; i<4; i++){
					output += String.format("\nObserveWall_%s_%d=Yes PositionRow_%d=%d,PositionCol_%d=%d %.13e",
							Constants.DIR.values()[i].toString(),timestamp,timestamp,r,timestamp,c,getWallObservation(r, c, i));
					
					output += String.format("\nObserveWall_%s_%d=No PositionRow_%d=%d,PositionCol_%d=%d %.13e",
							Constants.DIR.values()[i].toString(),timestamp,timestamp,r,timestamp,c,(1-getWallObservation(r, c, i)));
				}
				
				//landmarks 
				//e.g. ObserveLandmark2_S_8=Yes PositionRow_8=7,PositionCol_8=10 0.152161
				for(int lmk = 1; lmk <= num_landmarks; lmk++){
					for(int i = 0; i<4; i++){
						output += String.format("\nObserveLandmark%d_%s_%d=Yes PositionRow_%d=%d,PositionCol_%d=%d %.13e",
								lmk,Constants.DIR.values()[i].toString(),timestamp,timestamp,r,timestamp,c,getLandmarkObservation(r, c,lmk, i));
						
						output += String.format("\nObserveLandmark%d_%s_%d=No PositionRow_%d=%d,PositionCol_%d=%d %.13e",
								lmk,Constants.DIR.values()[i].toString(),timestamp,timestamp,r,timestamp,c,(1-getLandmarkObservation(r, c, lmk, i)));
					}
				}
			}
		}
		
		return output;
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
