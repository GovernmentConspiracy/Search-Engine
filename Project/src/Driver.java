import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/*
 * TODO Remove old TODO comments.
 */

/*
 * TODO Address the warnings.
Collection is a raw type. References to generic type Collection<E> should be parameterized	SimpleJsonWriter.java	/Project/src	line 221	Java Problem
The import java.util.Arrays is never used	TextParser.java	/Project/src	line 2	Java Problem
The serializable class  does not declare a static final serialVersionUID field of type long	ArgumentParser.java	/Project/src	line 24	Java Problem
Unsupported @SuppressWarnings("WeakerAccess")	ArgumentParser.java	/Project/src	line 12	Java Problem
Unsupported @SuppressWarnings("WeakerAccess")	InvertedIndex.java	/Project/src	line 20	Java Problem
Unsupported @SuppressWarnings("WeakerAccess")	SimpleJsonWriter.java	/Project/src	line 21	Java Problem
 */

/*
 * TODO Try to avoid using a mix of tabs and spaces. Configure your IDE to convert between the two for you.
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

    private static final Path DEFAULT_INDEX_PATH = Path.of("index.json"); //try this instead
    private static final Path DEFAULT_COUNTS_PATH = Path.of("counts.json");

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
        ArgumentParser command = new ArgumentParser(args);

        /*
         * TODO Don't check for expected flags anymore.
         */

        String[] expectedFlags = {"-path", "-index", "-counts"};
        InvertedIndex invertedIndex = new InvertedIndex();

        Path input = null;

        if (command.hasValue(expectedFlags[0])) {
            input = command.getPath(expectedFlags[0]);
        } else {
            System.out.printf("Program arguments %s is required\n", expectedFlags[0]);
            System.out.println("Ex:\n -path \"project-tests/huckleberry.txt\"\n");
        }

        if (command.hasFlag(expectedFlags[1])) {
            Path indexOutput = DEFAULT_INDEX_PATH;
            indexOutput = command.getPath(expectedFlags[1], indexOutput);
            if (input != null)
                invertedIndex.index(input);
            invertedIndex.indexToJSON(indexOutput);
        }

        if (command.hasFlag(expectedFlags[2])) {
            Path countsOutput = DEFAULT_COUNTS_PATH;
            countsOutput = command.getPath(expectedFlags[2], countsOutput);
            if (input != null)
                invertedIndex.count(input);
            invertedIndex.countToJSON(countsOutput);
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
