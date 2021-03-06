import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

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
 * @version v3.1.0
 */
public class SearchBuilder {
	
	/*
	 * TODO Breaking encapsulation
	 * 
	 * 1) Create methods to read/write safely from the private data
	 * 
	 * 2) Create a common parent (interface) that SearchBuilder and ConcurrentSearchBuilder implement,
	 * but both have their own data and implementations.
	 */

	/**
	 * A nested data structure which stores search queries mapped to the search results.
	 */
	protected final Map<String, List<InvertedIndex.SearchResult>> queryEntries;

	/**
	 * The index to be used to search for queries
	 */
	protected final InvertedIndex index;

	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	protected static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Constructs a Search builder of an existing Index.
	 *
	 * @param index the index used to set the query
	 */
	public SearchBuilder(InvertedIndex index) {
		this.index = index;
		queryEntries = new TreeMap<>();
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
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				parseQuery(line, exact);
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
		queryEntries.put(lineFinal, index.search(usedPhrases, exact));
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
}