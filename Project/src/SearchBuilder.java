import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
//TODO refactor whole builder class to accept Query but not Index

/**
 * A builder class for inverted index and query, where an index stores words and
 * the location (both file location and position in file) of where those words were found
 * and a query...//TODO Write this
 *
 * @author Jason Liang
 * @version v2.0.0
 */
public class SearchBuilder {

	private static final Logger log = LogManager.getLogger();

	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * An query to store words and the location (both file location and position in file) of where those words were found.
	 */
	private final Query query;

	/**
	 * //TODO
	 *
	 * @param query
	 */
	public SearchBuilder(Query query) {
		this.query = query;
	}

	public SearchBuilder() {
		this(new Query());
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
	 * A behemoth of a function
	 * //TODO
	 * @param input
	 * @param query
	 * @param index
	 * @param exact
	 * @throws IOException
	 */
	public static void addQueryPath(Path input, Query query, InvertedIndex index, boolean exact) throws IOException {
		if (Files.isDirectory(input)) {
			throw new IOException("Query Path: Wrong file type");
		}
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			final Map<String, Long> counts = index.getCounts(); //read only
			while ((line = reader.readLine()) != null) {
				Set<String> usedPhrases = new TreeSet<>(); //used to create finalString and stop duplicates
				Map<String, Long> fileCount = new TreeMap<>(); //Used to merge all files
				for (String word : TextParser.parse(line)) {
					word = stemmer.stem(word).toString();

					if (usedPhrases.add(word)) {
						index.getWordFileCount(word, exact)
								.forEach((key, value) ->
										fileCount.put(key, fileCount.getOrDefault(key, (long) 0) + value));
					}
				}
				String lineFinal = String.join(" ", usedPhrases);

				if (!fileCount.isEmpty()) {
					fileCount.forEach((key, value) ->
							query.addQuery(lineFinal, key, value, counts.get(key)));
				} else if (lineFinal.length() > 0) {
					query.addEmptyQuery(lineFinal);
				}
			}
		}
	}

	/**
	 * A behemoth of a function
	 * //TODO
	 *
	 * @param input
	 * @param query
	 * @param index
	 * @param queue
	 * @param exact
	 * @throws IOException
	 */
	public static void addQueryPath(Path input, Query query, InvertedIndex index, WorkQueue queue, boolean exact) throws IOException {
		if (Files.isDirectory(input)) {
			throw new IOException("Query Path: Wrong file type");
		}
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			final Map<String, Long> counts = index.getCounts(); //read only
			while ((line = reader.readLine()) != null) {
				Set<String> usedPhrases = new TreeSet<>(); //used to create finalString and stop duplicates
				Map<String, Long> fileCount = new TreeMap<>(); //Used to merge all files
				for (String word : TextParser.parse(line)) {

					word = stemmer.stem(word).toString();

					if (usedPhrases.add(word)) {
						queue.execute(new CountTask(index, fileCount, word, exact));
					}
				}

				try {
					log.debug("NOTIFICATION: .finish() called");
					queue.finish();
					log.debug("NOTIFICATION: .finish() ended");
				} catch (InterruptedException e) {
					log.error("Work did NOT finish.");
				}

				String lineFinal = String.join(" ", usedPhrases);

				if (!fileCount.isEmpty()) {
					fileCount.forEach((key, value) ->
							query.addQuery(lineFinal, key, value, counts.get(key)));
				} else if (lineFinal.length() > 0) {
					query.addEmptyQuery(lineFinal);
				}
			}
		}
	}

	private static class CountTask implements Runnable {
		private final InvertedIndex index;
		private final Map<String, Long> fileCount;
		private final String word;
		private final boolean exact;


		public CountTask(InvertedIndex index, Map<String, Long> fileCount, String word, boolean exact) {
			this.index = index;
			this.fileCount = fileCount;
			this.word = word;
			this.exact = exact;
		}

		@Override
		public void run() {

			Map<String, Long> tempCount = index.getWordFileCount(word, exact);
			synchronized (fileCount) {
				log.trace("Adding tempCount into fileCount...");
				//expensive in memory
				tempCount.forEach((key, value) ->
						fileCount.put(key, fileCount.getOrDefault(key, (long) 0) + value));
				log.trace("Added tempCount into fileCount!");
			}

		}
	}

	public static void queryTraverse(Path input, Query query, InvertedIndex index) throws IOException {
		List<Path> paths = getFiles(input);
		for (Path in : paths) {
			addQueryPath(in, query, index, true);
		}
	}

	public static void queryToJSON(Path output, Query query) throws IOException {
		query.queryToJSON(output);
	}


}
