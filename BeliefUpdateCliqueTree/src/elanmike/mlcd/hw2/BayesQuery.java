package elanmike.mlcd.hw2;

public class BayesQuery {

	/**
	 * Runs bayes-query sum product or max product depending on arguments
	 * @param args list of arguments
	 */
	public static void main(String[] args) {
		// check arguments
		if(args.length != 4) {
			usage();
			return;
		}
		boolean useSumProduct = true;
		if(args[0].equals("-s")) {
			useSumProduct = true;
		}
		else if(args[0].equals("-m")) {
			useSumProduct = false;
		}
		else {
			usage();
			return;
		}
		// TODO load cpd, throw error on failure
		// TODO load network file, throw error on failure
		// TODO verify clique tree is valid for the loaded network, throw error on failure
		// if sum-product:
			// TODO run belief-update message passing, sum-product
			// TODO process queries, print output for each
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
