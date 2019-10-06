import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/*
 * TODO Remove NEW* TODO comments after asking
 */

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Fall 2019
 */
public class Driver {

	/**
	 * The default index path if no path is provided through command line.
	 */
	private static final Path INDEX_DEFAULT_PATH = Path.of("index.json");

	/**
	 * The default counts path if no path is provided through command line.
	 */
	private static final Path COUNTS_DEFAULT_PATH = Path.of("counts.json");

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
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		/*-----------------Start-----------------*/
		ArgumentParser command = new ArgumentParser(args);
		InvertedIndexBuilder index = new InvertedIndexBuilder();
		Path input;

		if ((input = command.getPath(PATH_FLAG)) != null) {
			try {
				index.transverse(input);
			} catch (IOException e) {
				System.err.println("Input path for index could not be read. Check if other threads are accessing it.");
				System.err.println(e.getMessage());
			}
		} else {
			System.out.printf("Program arguments %s is required\n", PATH_FLAG);
			System.out.println("Ex:\n -path \"project-tests/huckleberry.txt\"\n");
		}

		if (command.hasFlag(INDEX_FLAG)) {
			Path indexOutput = command.getPath(INDEX_FLAG, INDEX_DEFAULT_PATH);
			try {
				index.indexToJSON(indexOutput);
			} catch (IOException e) {
				System.err.println("Output path for index could not be written.");
				System.err.printf("Check if path (%s) is writable (i.e is not a directory)\n", indexOutput);
				System.err.println(e.getMessage());
				System.err.println();
			}

		}

		if (command.hasFlag(COUNTS_FLAG)) {
			Path countsOutput = command.getPath(COUNTS_FLAG, COUNTS_DEFAULT_PATH);
			try {
				index.countToJSON(countsOutput);
			} catch (IOException e) {
				System.err.println("Output path for counts could not be written.");
				System.err.printf("Check if path (%s) is writable (i.e is not a directory)\n", countsOutput);
				System.err.println(e.getMessage());
			}
		}
		/*-----------------End-----------------*/

		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

	/*
	 * Generally, "driver" classes are responsible for setting up and calling other
	 * classes, usually from a main() method that parses command-line parameters. If
	 * the driver were only responsible for a single class, we use that class name.
	 * For example, "PizzaDriver" is what we would name a driver class that just
	 * sets up and calls the "Pizza" class.
	 */
}
