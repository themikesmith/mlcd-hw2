package elanmike.mlcd.hw2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BayesQuery {

	private static Bump b;
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
		try {
			b = readTree(args[2]);
		} catch (NumberFormatException | IOException e) {
			System.err.println("error reading tree!");
			e.printStackTrace();
			return;
		}
		// if sum-product:
			// TODO run belief-update message passing, sum-product
			// TODO process queries, print output for each
		// else if max-product
			// TODO run belief-update message passing, max-product
			// TODO process queries, print output for each
		// done!
	}
	/**
	 * Reads in the tree from the clique file.
	 * Builds the tree
	 * @param cliqueTreeFilename
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static Bump readTree(String cliqueTreeFilename) 
			throws IOException, NumberFormatException {
		Bump b = new Bump();
		BufferedReader br = new BufferedReader(new FileReader(cliqueTreeFilename));
		String line;
		// on the first line is the number of following lines that describe vertices
		int numVertices = -1;
		while ((line = br.readLine()) != null) {
			if(numVertices == -1) { // set the number of vertices
				numVertices = new Integer(line); // throws number format exception 
			}
			else if(numVertices > 0) { // reading vertices
				String[] vars = line.split(",");
				// TODO create clique
				// TODO create vertex
				numVertices--; // and decrement our number left to read
			}
			else { // reading edges
				// TODO create edge
			}
		}
		br.close();
		return b;
	}
	private static void processQueries(String queryFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(queryFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] stuff = line.split(" ");
			String[] lhs = stuff[0].split(",");
			String[] rhs = stuff[1].split(",");
			System.out.println(b.query(lhs, rhs));
		}
		br.close();
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
