import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2019
 */
public class Driver {
	/* -------------- Logger -------------- */

	/**
	 * The logger of this class
	 */
	private static final Logger log = LogManager.getLogger();

	/* -------------- Defaults -------------- */

	/**
	 * The default index path if no path is provided through command line.
	 */
	private static final Path INDEX_DEFAULT_PATH = Path.of("index.json");

	/**
	 * The default counts path if no path is provided through command line.
	 */
	private static final Path COUNTS_DEFAULT_PATH = Path.of("counts.json");

	/**
	 * The default results path if no path is provided through command line.
	 */
	private static final Path RESULTS_DEFAULT_PATH = Path.of("results.json");

	/* -------------- Arguments -------------- */

	/**
	 * A string representing the path flag.
	 * This flag must exist in args in order to run.
	 */
	private static final String PATH_FLAG = "-path";

	/**
	 * A string representing the index flag
	 */
	private static final String INDEX_FLAG = "-index";

	/**
	 * A string representing the counts flag
	 */
	private static final String COUNTS_FLAG = "-counts";

	/**
	 * A string representing the query flag
	 */
	private static final String QUERY_FLAG = "-query";

	/**
	 * A string representing the exact flag
	 */
	private static final String EXACT_FLAG = "-exact";

	/**
	 * A string representing the results flag
	 */
	private static final String RESULTS_FLAG = "-results";

	/* -------------- Main -------------- */

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		/* --------------Start -------------- */
		ArgumentParser command = new ArgumentParser(args);
		InvertedIndex index = new InvertedIndex();
		SearchBuilder search = new SearchBuilder(index);
		// TODO Project 3 prep: Create an InvertedIndexBuilder object here

		Path indexPath, queryPath;

		log.debug(command.toString());
		if ((indexPath = command.getPath(PATH_FLAG)) != null) {
			try {
				// TODO Call the non-static traverse method here
				InvertedIndexBuilder.traverse(indexPath, index);
			} catch (IOException e) {
				log.error("Input path for index could not be read. Check if other threads are accessing it.");
			}
		} else {
			log.warn("Program arguments {} is required\n", PATH_FLAG);
			log.info("Ex:\n -path \"project-tests/huckleberry.txt\"\n");
		}

		if (command.hasFlag(INDEX_FLAG)) {
			Path indexOutput = command.getPath(INDEX_FLAG, INDEX_DEFAULT_PATH);
			try {
				index.indexToJSON(indexOutput);
			} catch (IOException e) {
				log.error("Output path for index could not be written.");
				log.info("Check if path ({}) is writable (i.e is not a directory)\n", indexOutput);
			}
		}

		if (command.hasFlag(COUNTS_FLAG)) {
			Path countsOutput = command.getPath(COUNTS_FLAG, COUNTS_DEFAULT_PATH);
			try {
				index.countToJSON(countsOutput);
			} catch (IOException e) {
				log.error("Output path for counts could not be written.");
				log.info("Check if path ({}) is writable (i.e is not a directory)\n", countsOutput);
			}
		}

		if ((queryPath = command.getPath(QUERY_FLAG)) != null) {
			try {
				search.parseQueries(queryPath, command.hasFlag(EXACT_FLAG));
			} catch (IOException e) {
				log.error("Input path for index could not be read.");
				log.info("Check if this is the correct path type.");
			}
		} else {
			log.warn("Program arguments {} is required\n", PATH_FLAG);
			log.info("Ex:\n -path \"project-tests/huckleberry.txt\"\n");
		}

		if (command.hasFlag(RESULTS_FLAG)) {
			Path resultsOutput = command.getPath(RESULTS_FLAG, RESULTS_DEFAULT_PATH);
			try {
				search.queryToJSON(resultsOutput);
			} catch (IOException e) {
				log.warn("Output path for counts could not be written.");
				log.info("Check if path ({}) is writable (i.e is not a directory)\n", resultsOutput);
			}
		}


		/* -------------- End -------------- */

		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		log.info("Elapsed: {} seconds\n", seconds);
	}
}
