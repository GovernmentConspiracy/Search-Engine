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

/**
 * A builder class for inverted index and query, where an index stores words and
 * the location (both file location and position in file) of where those words were found
 * and a query stores word count.
 *
 * @author Jason Liang
 * @version v3.0.1
 */
public class SearchBuilder {
	/**
	 * The logger of this class.
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * A nested data structure which stores search queries mapped to the search results.
	 */
	private final Map<String, List<InvertedIndex.SearchResult>> queryEntries;

	/**
	 * The index to be used to search for queries
	 */
	private final InvertedIndex index;

	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * A work queue for parsing each line into queryEntries.
	 */
	private final WorkQueue queue;

	/**
	 * Constructs a Search builder of an existing Index.
	 *
	 * @param index the query to be set
	 * @param queue the work queue executing the code.
	 */
	public SearchBuilder(InvertedIndex index, WorkQueue queue) {
		this.index = index;
		queryEntries = new TreeMap<>();
		this.queue = queue;
	}

	/**
	 * Constructs a Search builder of an existing Index.
	 *
	 * @param index the query to be set
	 */
	public SearchBuilder(InvertedIndex index) {
		this(index, null);
	}

	/**
	 * Populates queryEntries map with each query in the input file.
	 *
	 * @param input the input path
	 * @param exact a flag to turn on exact matches
	 * @throws IOException if the input file is a directory or could not be opened
	 */
	public void parseQueries(Path input, boolean exact) throws IOException {
		if (Files.isDirectory(input)) {
			throw new IOException("Query Path: Wrong file type");
		}
		if (queue != null) {
			parseQueries(input, exact, queue);
		} else {
			try (
					BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
			) {
				String line;
				while ((line = reader.readLine()) != null) {
					parseQuery(line, exact);
				}
			}
		}
	}

	/**
	 * Populates queryEntries map with each query in the input file using
	 * a WorkQueue.
	 *
	 * @param input the input path
	 * @param exact a flag to turn on exact matches
	 * @param queue the work queue executing the code.
	 * @throws IOException if the input file is a directory or could not be opened
	 */
	private void parseQueries(Path input, boolean exact, WorkQueue queue) throws IOException {
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				queue.execute(new ParseQueryTask(line, exact));
			}

			try {
				log.debug("NOTIFICATION: .finish() called");
				queue.finish();
				log.debug("NOTIFICATION: .finish() ended");
			} catch (InterruptedException e) {
				log.error("Work did NOT finish.");
			}
		}
	}

	/**
	 * Adds a query into queryEntries and maps it to a list of search results.
	 * The query is cleaned and parsed before added into queryEntries.
	 *
	 * @param query a String of search phrases
	 * @param exact a flag to turn on exact matches
	 */
	public void parseQuery(String query, boolean exact) {
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		Set<String> usedPhrases = new TreeSet<>();
		for (String s : TextParser.parse(query)) {
			usedPhrases.add(stemmer.stem(s).toString());
		}

		if (usedPhrases.isEmpty()) {
			return;
		}
		String lineFinal = String.join(" ", usedPhrases);
		if (queryEntries.containsKey(lineFinal)) {
			return;
		}
		var local = index.search(usedPhrases, exact);
		queryEntries.put(lineFinal, local);
	}

	/**
	 * Generates a JSON text file of the search result of words, stored at Path output.
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 */
	public void queryToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(queryEntries, output);
	}

	/**
	 * A Runnable for adding to queryEntries.
	 */
	private class ParseQueryTask implements Runnable {
		/**
		 * A String of search phrases separated by a space
		 */
		private final String query;

		/**
		 * A boolean flag which determines whether to turn on exact matches
		 */
		private final boolean exact;

		/**
		 * Constructs a new ParseQueryTask runnable to add
		 * a query string and its SearchResults into queryEntries.
		 *
		 * @param query a String of search phrases
		 * @param exact a flag to turn on exact matches
		 */
		public ParseQueryTask(String query, boolean exact) {
			this.query = query;
			this.exact = exact;
		}

		@Override
		public void run() {
			Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
			Set<String> usedPhrases = new TreeSet<>();
			for (String s : TextParser.parse(query)) {
				usedPhrases.add(stemmer.stem(s).toString());
			}

			if (usedPhrases.isEmpty()) {
				return;
			}

			String lineFinal = String.join(" ", usedPhrases);
			boolean run;

			/* Version 2: Purpose:
			 *  To circumvent two synchronized blocks.
			 */
			List<InvertedIndex.SearchResult> entryPoint = null;
			synchronized (queryEntries) {
				if (run = !queryEntries.containsKey(lineFinal)) {
					queryEntries.put(lineFinal, entryPoint = new ArrayList<>()); //Reserves so no need to overwrite
				}
			}

			if (run) {
				entryPoint.addAll(index.search(usedPhrases, exact));
				log.debug("Added {}. to queryEntries", lineFinal);
			}
		}
	}
}