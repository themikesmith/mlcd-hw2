package elanmike.mlcd.hw2;

import java.io.IOException;

import elanmike.mlcd.hw2.Factor.FactorException;

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
		if(args.length>4 && args[4].equals("-s")) {
			useSumProduct = true;
		}
		else if(args.length>4 && args[4].equals("-m")) {
			useSumProduct = false;
		}
		else if (args.length>4 ){
			usage();
			return;
		}
		// create and init clique tree
		b = new Bump();
		try {
			b.init(args[0],args[2],args[1]);
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		} catch (FactorException e) {
			e.printStackTrace();
			return;
		}
		// make a query processor
		QueryProcessor qp = new QueryProcessor(b);
		try {
			// process the queries
			qp.processQueries(args[3], useSumProduct);
		} catch (IOException e) {
			System.err.println("error processing queries from:"+args[4]);
			e.printStackTrace();
		}
		// done!
	}
	
	/**
	 * Prints an example of proper usage of the program
	 */
	private static void usage() {
		System.err.println("usage: pass 4 arguments.\narg[0] = network_file\n" +
				"arg[1] = cpd_file ; arg[2] = clique_tree_file\n" +
				"arg[3] = query_file\n" +
				"\nOne may specify an optional argument arg[4] = 's|m' (for sum- or max-product");
	}
}
