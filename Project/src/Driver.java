import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();
		System.out.println(Arrays.toString(args));

        ArgumentParser command = new ArgumentParser(args);

		System.out.println(command);

        String[] expectedFlags = {"-path", "-index", "-counts"};
		InvertedIndex invertedIndex = new InvertedIndex();
		Path indexOutput = Path.of("index.json");
		Path countOutput = Path.of("counts.json");
		Path input = null;
        if (command.hasValue(expectedFlags[0])) {
			input = command.getPath(expectedFlags[0]);
		} else {
			System.out.printf("Program arguments %s is required\n", expectedFlags[0]);
			System.out.println("Ex:\n -path \"project-tests/huckleberry.txt\"\n");
		}

		if (command.hasFlag(expectedFlags[1])) {
            indexOutput = command.getPath(expectedFlags[1], indexOutput);
			if (input != null)
				invertedIndex.index(input);
			invertedIndex.indexToJSON(indexOutput);
		}

		if (command.hasFlag(expectedFlags[2])) {
            countOutput = command.getPath(expectedFlags[2], countOutput);
			if (input != null)
				invertedIndex.count(input);
			invertedIndex.countToJSON(countOutput);
		}

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
