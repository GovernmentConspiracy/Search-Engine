import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
//TODO refactor whole builder class to accept Query

/**
 * A builder class for inverted index and query, where an index stores words and
 * the location (both file location and position in file) of where those words were found
 * and a query...//TODO Write this
 *
 * @author Jason Liang
 * @version v2.0.0
 */
public class SearchBuilder {
	/**
	 * An index to store words and the location (both file location and position in file) of where those words were found.
	 */

	/**
	 * Do not instantiate.
	 */
	private SearchBuilder() {
	}

	/**
	 * Adds a non-directory file into index
	 *
	 * @param input the path to be added into InvertedIndex
	 * @param index
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public static void addFile(Path input, InvertedIndex index) throws IOException {
		index.index(input);
	}

	/**
	 * @param input the path to be added into InvertedIndex
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public static void transverse(Path input, InvertedIndex index) throws IOException {
		List<Path> paths = getFiles(input);
		for (Path in : paths) {
			addFile(in, index);
		}
	}

	/**
	 * Returns all paths of {@code Path input} recursively or an empty list if the files could not be generated.
	 *
	 * @param input The root directory or text tile
	 * @return A list of paths of the entire directory of {@code Path input} or 0-1 text file(s)
	 */
	public static List<Path> getFilesOrEmpty(Path input) {
		try {
			return getFiles(input);
		} catch (IOException e) {
			//TODO logger
			System.err.println(e.getMessage());
		}
		return Collections.emptyList();
	}

	/**
	 * Returns all paths of {@code Path} input recursively.
	 *
	 * @param input The root directory or text tile
	 * @return A list of non-directory files of the entire directory of {@code Path input}
	 * @throws IOException if the stream could not be made or if {@code Path input} does not exist
	 */
	public static List<Path> getFiles(Path input) throws IOException {
		return TextFileFinder.list(input);
	}

	/**
	 * Generates a JSON text file of the inverted index, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @param index
	 * @throws IOException if the output file could not be created or written
	 */
	public static void indexToJSON(Path output, InvertedIndex index) throws IOException {
		index.indexToJSON(output);
	}

	/**
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 */
	public static void countToJSON(Path output, InvertedIndex index) throws IOException {
		index.countToJSON(output);
	}
}
