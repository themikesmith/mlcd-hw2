package elanmike.mlcd.hw2;


//test
import java.io.IOException;

public class EstimateParams {
	/**
	 * Our main method for estimating parameters, given the HW2 framework.
	 * @param args
	 */
	public static void main(String[] args) {
		// check argument length
		if(args.length != 3) {
			usage();
			return;
		}
		// read in the network
		Network n = new Network();
		try {
			n.read(args[0]);
		} catch (IOException e) {
			System.err.printf("error reading from file: ",args[0]);
			e.printStackTrace();
			return;
		} catch(NumberFormatException e) {
			System.err.printf("error reading from file: ",args[0]);
			e.printStackTrace();
			return;
		}
		System.out.println("Read network from:"+args[0]);
		// train using the MAP estimate, given our training data
		try {
			n.train(args[1]);
		} catch (IOException e) {
			System.err.printf("error training from file: ",args[1]);
			e.printStackTrace();
			return;
		}
		System.out.println("Trained from:"+args[1]);
		// and write our CPD to the designated output file
		try {
			n.writeCPD(args[2]);
		} catch (IOException e) {
			System.err.printf("error writing cpd to file: ",args[2]);
			e.printStackTrace();
			return;
		}
		System.out.println("Wrote CPD to:"+args[2]);
		// done!
	}
	/**
	 * Prints a description of proper usage of the program.
	 */
	private static void usage() {
		System.err.println("Pass 3 parameters, network file, training data, and cpd output file.");
	}
}
