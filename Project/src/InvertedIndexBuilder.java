import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * A builder class for inverted index, which is an index to store words and
 * the location (both file location and position in file) of where those words were found.
 *
 * @author Jason Liang
 * @version v1.1.0
 */
public class InvertedIndexBuilder {
	/**
	 * An index to store words and the location (both file location and position in file) of where those words were found.
	 */
	private final InvertedIndex index;

	/**
	 * Constructs a InvertedIndex builder of an existing index
	 *
	 * @param index the initial contents of the InvertedIndex
	 */
	public InvertedIndexBuilder(InvertedIndex index) {
		this.index = index;
	}

	/**
	 * Constructs an InvertedIndex builder with a new index
	 */
	public InvertedIndexBuilder() {
		this(new InvertedIndex());
	}

	/**
	 * Constructs an InvertedIndex builder of an existing index, then loads it with files.
	 *
	 * @param index the initial contents of the InvertedIndex
	 * @param input the path to be added into InvertedIndex
	 * @throws IOException if the files could not be inserted
	 */
	public InvertedIndexBuilder(InvertedIndex index, Path input) throws IOException {
		this(index);
		transverse(input);
	}

	/**
	 * Constructs an InvertedIndex builder of a new index, then loads it with files.
	 *
	 * @param input the path to be added into InvertedIndex
	 * @throws IOException if the files could not be inserted
	 */
	public InvertedIndexBuilder(Path input) throws IOException {
		this(new InvertedIndex(), input);
	}

	/**
	 * Adds a non-directory file into
	 *
	 * @param input the path to be added into InvertedIndex
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public InvertedIndexBuilder addFile(Path input) throws IOException {
		index.index(input);
		return this;
	}

	/**
	 * @param input the path to be added into InvertedIndex
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public InvertedIndexBuilder transverse(Path input) throws IOException {
		List<Path> paths = getFiles(input);
		for (Path in : paths) {
			index.index(in);
		}
		return this;
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
	 * Returns the InvertedIndex of which this index builder is wrapping.
	 *
	 * @return this InvertedIndex
	 */
	public InvertedIndex getIndex() {
		return index;
	}
}
