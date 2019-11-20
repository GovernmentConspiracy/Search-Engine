import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/*
 * TODO

Since the SearchResult data depends on a specific instance of a specific inverted index,
it should be an inner class inside of inverted index. (Not a static nested class.)

Need to make the SearchResult class mutable. So where you used to store Map<String, Long>
you are going to store a Map<String (location?), SearchResult> instead and update the count within that object.

SearchResult will still have a where, count, score and get methods. It needs a new method:

private void update(String word) {
	this.count += indexMap.get(word).get(where).size();
	this.score = (double) this.count / countMap.get(where);
}

Combine Query INTO SearchBuilder.

This is SearchBuilder: //non-static

private final Map<String, List<InvertedIndex.SearchResult>> queryEntries; //Does expensive sort at the end
private final InvertedIndex index;

public void queryToJSON(Path output) throws IOException

public void parseQueries(Path input, boolean exact) throws IOException {
		open the file, read line by line, and on each line call the other parseQueries
}

public void parseQueries(String line, boolean exact) {
	Set<String> usedPhrases = ... filled up by parse and stemming the line
	String lineFinal = String.join(" ", usedPhrases);
	queryEntries.put(lineFinal, index.search(usedPhrases, exact));
}
 */

/**
 * A query to store phrases and each word count of the phrase of where those words were found.
 *
 * @author Jason Liang
 * @version v2.0.1
 */
public class Query {

	/**
	 * Nested data structure used to store count of where a word was found.
	 */
	private final Map<String, List<InvertedIndex.SearchResult>> queryEntries; //before: TreeMap<String, TreeSet<SearchResult>>

	/**
	 * Constructs a new empty query.
	 */
	public Query() {
		queryEntries = new TreeMap<>();
	}

	/** //TODO REMOVE
	 * Adds a search result to the set mapped to the provided phrase.
	 *
	 * @param phrase   The search phrase
	 * @param pathName The path of where a specified word of the search phrase was found
	 * @param partial  The word count of the specified word of the search phrase in the path
	 * @param total    The total word count of the path
	 */
//	public void addQuery(String phrase, String pathName, Long partial, Long total) {
//		addEmptyQuery(phrase);
//		queryEntries.get(phrase).add(InvertedIndex.new SearchResult(pathName, partial, (double) partial / total));
//	}

	/** //TODO REMOVE
	 * Adds an empty set of search result into the query.
	 * (Not Collections.emptySet(), so more expensive);
	 *
	 * @param phrase The search phrase
	 */
	public void addEmptyQuery(String phrase) {
		queryEntries.putIfAbsent(phrase, new ArrayList<>());
	}

	/**
	 * Generates a JSON text file of the search result of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 */
	public void queryToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(queryEntries, output);
	}


}
