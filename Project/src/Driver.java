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


	/*
	 * TODO Exception handling
	 * Handle exceptions in the code that interacts with the user.
	 * Which means Driver.main. All output should be user friendly (no stack traces)
	 * and informative so what when an error occurs users know exactly what argument
	 * they provided that caused the problem.
	 */

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
		InvertedIndex invertedIndex = new InvertedIndex();
		Path input;

		if ((input = command.getPath(PATH_FLAG)) == null) {
			System.out.printf("Program arguments %s is required\n", PATH_FLAG);
			System.out.println("Ex:\n -path \"project-tests/huckleberry.txt\"\n");
		}

		if (command.hasFlag(INDEX_FLAG)) {
			Path indexOutput = command.getPath(INDEX_FLAG, INDEX_DEFAULT_PATH);
			if (input != null)
				invertedIndex.index(input);
			invertedIndex.indexToJSONSafe(indexOutput);
		}

		if (command.hasFlag(COUNTS_FLAG)) {
			Path countsOutput = command.getPath(COUNTS_FLAG, COUNTS_DEFAULT_PATH);
			if (input != null)
				invertedIndex.count(input);
			invertedIndex.countToJSONSafe(countsOutput);
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
