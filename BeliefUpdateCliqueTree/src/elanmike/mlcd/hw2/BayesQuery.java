package elanmike.mlcd.hw2;

import java.io.IOException;

public class BayesQuery {

	private static Bump b;
	/**
	 * Runs bayes-query sum product or max product depending on arguments
	 * @param args list of arguments
	 */
	public static void main(String[] args) {
		// check arguments
		if(args.length != 4 && args.length != 5) {
			usage();
			return;
		}
		boolean useSumProduct = true;
		if(args.length>4 && args[0].equals("-s")) {
			useSumProduct = true;
		}
		else if(args.length>4 && args[0].equals("-m")) {
			useSumProduct = false;
		}
		else if (args.length>4 ){
			usage();
			return;
		}
		
		b = new Bump();
		b.setUseSumProduct(useSumProduct);
		try {
			// TODO load cpd, throw error on failure
			// TODO load network file, throw error on failure
			// TODO verify clique tree is valid for the loaded network, throw error on failure
			b.readNetworkFile(args[0]);
			b.readCliqueTreeFile(args[2]);
			b.readCPDFile(args[1]);
			
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// if sum-product:
			b.runBump();
			try {
				b.processQueries(args[4]);
			} catch (IOException e) {
				System.err.println("error processing queries from:"+args[4]);
				e.printStackTrace();
			}
		// else if max-product
			// TODO run belief-update message passing, max-product
			// TODO process queries, print output for each
		// done!
	}
	
	/**
	 * Prints an example of proper usage of the program
	 */
	private static void usage() {
		System.err.println("usage: pass 5 arguments. arg[0] = 's|m' (for sum- or max-product ; " +
				"arg[1] = network_file ; arg[2] = cpd_file ; arg[3] = clique_tree_file ;" +
				"arg[4] = query_file");
	}
}
