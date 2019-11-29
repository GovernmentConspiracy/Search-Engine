import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

	/**
	 * The default thread count if no count is specified in the thread flag.
	 */
	private static final int DEFAULT_THREADS = 5;

	/**
	 * The default maximum of URLs to search if no count is specified in the url flag.
	 */
	private static final int DEFAULT_URL_LIMIT = 50;

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

	/**
	 * A string representing the threads flag
	 */
	private static final String THREAD_FLAG = "-threads";

	/**
	 * A string representing the url flag
	 */
	private static final String URL_FLAG = "-url";

	/**
	 * A string representing the threads flag
	 */
	private static final String LIMIT_FLAG = "-limit";

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

		/* -------------- Start -------------- */
		ArgumentParser command = new ArgumentParser(args);
		InvertedIndex index;
		WorkQueue queue = null;
		boolean isMultiThreaded;

		log.info(command);

		if (isMultiThreaded = (command.hasFlag(THREAD_FLAG) || command.hasFlag(URL_FLAG)) ) {
			int threadCount;
			index = new ConcurrentInvertedIndex();

			try {
				threadCount = Integer.parseInt(command.getString(THREAD_FLAG));
			} catch (NumberFormatException e) {
				log.warn("Incorrect parameters in threadCount. Changed to {}", DEFAULT_THREADS);
				log.catching(Level.DEBUG, e);
				threadCount = DEFAULT_THREADS;
			}
			if (threadCount <= 0) {
				threadCount = DEFAULT_THREADS;
			}
			queue = new WorkQueue(threadCount);
			log.debug("Thread count = {}", threadCount);
		} else {
			index = new InvertedIndex();
		}

		/* ---- Build ---- */
		log.debug(command.toString());
		log.info("Started");

		/* Builds InvertedIndex in WebCrawler */
		String urlStringSeed;
		if ((urlStringSeed = command.getString(URL_FLAG)) != null) {
			assert index instanceof ConcurrentInvertedIndex;

			/* Set urlLimit */
			int urlLimit = DEFAULT_URL_LIMIT;
			if (command.hasFlag(LIMIT_FLAG)) {
				try {
					urlLimit = Integer.parseInt(command.getString(LIMIT_FLAG));
				} catch (NumberFormatException e) {
					log.warn("Incorrect parameters in threadCount. Changed to {}", DEFAULT_URL_LIMIT);
					log.catching(Level.DEBUG, e);
				}
				if (urlLimit <= 0) {
					urlLimit = DEFAULT_URL_LIMIT;
				}
				log.debug("Url limit = {}", urlLimit);
			}

			WebCrawler crawler = new WebCrawler((ConcurrentInvertedIndex) index, queue, urlLimit);
			try {
				crawler.traverse(urlStringSeed);
			} catch (MalformedURLException mue) {
				log.error("Input path for index could not be read. Check if other threads are accessing it.");
			}
		}

		/* Builds InvertedIndex in InvertedIndexBuilder */
		Path indexPath;
		if ((indexPath = command.getPath(PATH_FLAG)) != null) {
			InvertedIndexBuilder indexBuilder = new InvertedIndexBuilder(index, queue);
			try {
				indexBuilder.traverse(indexPath);
			} catch (IOException ioe) {
				log.error("Input path for index could not be read. Check if other threads are accessing it.");
			}
		}

		/* Builds Queries with SearchBuilder */
		SearchBuilder search = new SearchBuilder(index, queue);
		Path queryPath;
		if ((queryPath = command.getPath(QUERY_FLAG)) != null) {
			try {
				search.parseQueries(queryPath, command.hasFlag(EXACT_FLAG));
			} catch (IOException ioe) {
				log.error("Input path for index could not be read.");
				log.info("Check if this is the correct path type.");
			}
		}

		if (isMultiThreaded) {
			queue.shutdown();
		}

		/* Outputs Index as pretty JSON */
		if (command.hasFlag(INDEX_FLAG)) {
			Path indexOutput = command.getPath(INDEX_FLAG, INDEX_DEFAULT_PATH);
			try {
				index.indexToJSON(indexOutput);
			} catch (IOException ioe) {
				log.error("Output path for index could not be written.");
				log.info("Check if path ({}) is writable (i.e is not a directory)\n", indexOutput);
			}
		}

		/* Outputs Counts as pretty JSON */
		if (command.hasFlag(COUNTS_FLAG)) {
			Path countsOutput = command.getPath(COUNTS_FLAG, COUNTS_DEFAULT_PATH);
			try {
				index.countToJSON(countsOutput);
			} catch (IOException ioe) {
				log.error("Output path for counts could not be written.");
				log.info("Check if path ({}) is writable (i.e is not a directory)\n", countsOutput);
			}
		}

		/* Outputs Search Results as pretty JSON */
		if (command.hasFlag(RESULTS_FLAG)) {
			Path resultsOutput = command.getPath(RESULTS_FLAG, RESULTS_DEFAULT_PATH);
			try {
				search.queryToJSON(resultsOutput);
			} catch (IOException ioe) {
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
