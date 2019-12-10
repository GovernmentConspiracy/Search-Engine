import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A thread-safe builder class for inverted index and query, where an index stores words and
 * the location (both file location and position in file) of where those words were found
 * and a query stores word count.
 *
 * @author Jason Liang
 * @version v4.0.1
 */
public class ConcurrentSearchBuilder extends SearchBuilder {

	/**
	 * The logger of this class.
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * A work queue for parsing each line into queryEntries.
	 */
	private final WorkQueue queue;

	/**
	 * Constructs a Search builder of an existing Index.
	 *
	 * @param index the index used to set the query
	 * @param queue the work queue executing the code
	 */
	public ConcurrentSearchBuilder(ConcurrentInvertedIndex index, WorkQueue queue) {
		super(index);
		this.queue = queue;
	}

	@Override
	public void parseQueries(Path input, boolean exact) throws IOException {
		if (Files.isDirectory(input)) {
			throw new IOException("Query Path: Wrong file type");
		}
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
			} catch (InterruptedException e) {
				log.error("Work did NOT finish.");
			}
		}
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
		ParseQueryTask(String query, boolean exact) {
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
