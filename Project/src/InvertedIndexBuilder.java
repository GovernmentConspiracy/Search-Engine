import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * A builder class for inverted index, which is an index to store words and
 * the location (both file location and position in file) of where those words were found.
 *
 * @author Jason Liang
 * @version v2.1.0
 */
public class InvertedIndexBuilder {
	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	/*
	 * TODO One per file
	 */
	/**
	 * Stemmer used in this class.
	 */
	private static final Stemmer STEMMER = new SnowballStemmer(DEFAULT_LANG);

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
		this.traverse(input);
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
	 * Adds a non-directory file into the index
	 *
	 * @param input the path to be added into InvertedIndex
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public InvertedIndexBuilder addFile(Path input) throws IOException {
		InvertedIndexBuilder.addFile(input, index);
		return this;
	}

	/**
	 * Adds a non-directory file into the index
	 *
	 * @param input the path to be added into InvertedIndex
	 * @param index the index to be edited
	 * @throws IOException if the files could not be inserted
	 */
	public static void addFile(Path input, InvertedIndex index) throws IOException {
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			long i = 0;
			String inputString = input.toString();
			while ((line = reader.readLine()) != null) {
				for (String word : TextParser.parse(line)) {
					index.indexPut(STEMMER.stem(word).toString(), inputString, ++i);
				}
			}
		}
	}

	/**
	 * Adds non-directory files into the index from directory input
	 *
	 * @param input the path to be added into InvertedIndex
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public InvertedIndexBuilder traverse(Path input) throws IOException {
		InvertedIndexBuilder.traverse(input, index);
		return this;
	}

	/**
	 * Adds non-directory files into the index from directory input
	 *
	 * @param input the path to be added into InvertedIndex
	 * @param index the index to be edited
	 * @throws IOException if the files could not be inserted
	 */
	public static void traverse(Path input, InvertedIndex index) throws IOException {
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
