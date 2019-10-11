import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Do not instantiate.
	 */
	private SearchBuilder() {
	}

	/**
	 * Adds a non-directory file input into index
	 *
	 * @param input the path to be added into InvertedIndex
	 * @param index the index to be filled
	 * @return the reference of this object
	 * @throws IOException if the files could not be inserted
	 */
	public static void addIndexPath(Path input, InvertedIndex index) throws IOException {
		index.index(input);
	}

	/**
	 * Adds all files and sub-files of directory input into index
	 *
	 * @param input the path to be added into InvertedIndex
	 * @param index the index to be filled
	 * @throws IOException if the files could not be inserted
	 */
	public static void indexTraverse(Path input, InvertedIndex index) throws IOException {
		List<Path> paths = getFiles(input);
		for (Path in : paths) {
			addIndexPath(in, index);
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
	 * @param index the index to be filled
	 * @throws IOException if the output file could not be created or written
	 */
	public static void indexToJSON(Path output, InvertedIndex index) throws IOException {
		index.indexToJSON(output);
	}

	/**
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @param index the index to be filled
	 * @throws IOException if the output file could not be created or written
	 */
	public static void countToJSON(Path output, InvertedIndex index) throws IOException {
		index.countToJSON(output);
	}

	public static void addQueryPath(Path input, Query query, InvertedIndex index, boolean partial) throws IOException {
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			Map<String, Long> counts = index.getCounts();
			Map<String, Long> fileCount;
			while ((line = reader.readLine()) != null) {
				for (String word : TextParser.parse(line)) {
					String phrase = stemmer.stem(word).toString();
					if (partial) {
						fileCount = index.getPartialWordFileCount(phrase);
					} else {
						fileCount = index.getExactWordFileCount(phrase);
					}
					fileCount.entrySet()
							.forEach(e -> query.addQuery(
									phrase, e.getKey(), e.getValue(), counts.get(e.getKey())));
				}
			}
		}
	}

//	public static void queryTraverse(Path input, Query query, InvertedIndex index) throws IOException {
//		List<Path> paths = getFiles(input);
//		for (Path in : paths) {
//			addQueryPath(in, query, index);
//		}
//	}

	public void queryToJSON(Path output, Query query) throws IOException {
		query.queryToJSON(output);
	}
}
