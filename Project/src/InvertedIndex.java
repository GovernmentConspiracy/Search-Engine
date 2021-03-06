import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;

/**
 * An index to store words and the location (both file location and position in file) of where those words were found.
 * <p>Auxiliary functions includes word counter and JSON writer
 *
 * @author Jason Liang
 * @version v3.1.0
 */
public class InvertedIndex {

	/**
	 * The logger of this class.
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * Nested data structure used to store location of where a word was found.
	 * Outer map stores (key - value) as (word - file location)
	 * Inner map stores (key - value) as (file location - position in file)
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Long>>> indexMap; //String1 = word, String2 = Path, Set1 = Location

	/**
	 * Nested data structure used to store word count of a file
	 * Map stores (key - value) as (file location - number count)
	 */
	private final TreeMap<String, Long> countMap;

	/**
	 * Constructs a new empty inverted index.
	 */
	public InvertedIndex() {
		this.indexMap = new TreeMap<>();
		this.countMap = new TreeMap<>();
	}

	/* -------- Write Operations -------- */

	/**
	 * Helper method used to store word, pathString, and location into indexMap. See usages below
	 *
	 * @param word       A word made from a stemmed string. Used as the outer map key
	 * @param pathString A string representing the path where {@code word} in string form. Used as inner map key
	 * @param location   An long representing the location of where {@code word} was found in {@code pathString}
	 */
	public void indexPut(String word, String pathString, long location) {
		indexMap.putIfAbsent(word, new TreeMap<>());
		indexMap.get(word).putIfAbsent(pathString, new TreeSet<>());
		if (indexMap.get(word).get(pathString).add(location)) {
			countMap.put(
					pathString,
					Math.max(location, countMap.getOrDefault(pathString, (long) 0))
			);
		}
	}

	/**
	 * Generates a JSON text file of the inverted index, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 */
	public void indexToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(indexMap, output);
	}

	/**
	 * Generates a JSON text file of the inverted index, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @return {@code true} if successful in creating a JSON file
	 * @see #indexToJSON(Path)
	 */
	public boolean indexToJSONSafe(Path output) {
		try {
			indexToJSON(output);
		} catch (IOException e) {
			log.warn("Could not write JSON object of {}. Reason: {}", this.getClass().getSimpleName(), e.getMessage());
			return false;
		}
		return true;
	}

	/* -------- Read Operations -------- */

	/**
	 * Returns a sorted list of SearchResults which match the inverted index.
	 * If exact is {@code true}, the list will contain only exact matches instead
	 * of partial.
	 *
	 * @param phrases a unique cleaned and stemmed set of search phrases
	 * @param exact   a flag to turn on exact matches
	 * @return a sorted list of SearchResults
	 */
	public List<SearchResult> search(Set<String> phrases, boolean exact) {
		Map<String, SearchResult> searchResultMap = new HashMap<>();
		ArrayList<SearchResult> results = new ArrayList<>();
		if (exact) {
			searchExactHelper(phrases, searchResultMap, results);
		} else {
			searchPartialHelper(phrases, searchResultMap, results);
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * Populates a map of SearchResults of searchPhrases which gets exact matches
	 * from the inverted index.
	 *
	 * @param phrases         a unique cleaned and stemmed set of search phrases
	 * @param searchResultMap the location-SearchResult map to be populated
	 * @param results         a list of results to return
	 */
	private void searchExactHelper(Set<String> phrases, Map<String, SearchResult> searchResultMap, ArrayList<SearchResult> results) {
		for (String searchPhrase : phrases) {
			if (contains(searchPhrase)) {
				searchInputHelper(searchPhrase, searchResultMap, results);
			}
		}
	}

	/**
	 * Populates a map of SearchResults of searchPhrases which gets partial matches
	 * from the inverted index.
	 *
	 * @param phrases         a unique cleaned and stemmed set of search phrases
	 * @param searchResultMap the location-SearchResult map to be populated
	 * @param results         a list of results to return
	 */
	private void searchPartialHelper(Set<String> phrases, Map<String, SearchResult> searchResultMap, ArrayList<SearchResult> results) {
		for (String searchPhrase : phrases) {
			for (String matchedPhrase : indexMap.tailMap(searchPhrase).keySet()) {
				if (!matchedPhrase.startsWith(searchPhrase)) {
					break;
				}
				searchInputHelper(matchedPhrase, searchResultMap, results);
			}
		}
	}

	/**
	 * Populates a map of SearchResults with a word match
	 * from the inverted index, either partial or exact.
	 *
	 * @param match           a unique cleaned and stemmed search phrase
	 * @param searchResultMap the location-SearchResult map to be populated
	 * @param results         a list of results to return
	 */
	private void searchInputHelper(String match, Map<String, SearchResult> searchResultMap, ArrayList<SearchResult> results) {
		for (String location : indexMap.get(match).keySet()) {
			if (!searchResultMap.containsKey(location)) {
				SearchResult res = new SearchResult(match, location);
				results.add(res);
				searchResultMap.put(location, res);
			} else {
				searchResultMap.get(location).update(match);
			}
		}
	}

	/**
	 * Returns an unmodifiable map of the countMap
	 *
	 * @return an unmodifiable map of the countMap
	 */
	public Map<String, Long> getCounts() {
		return Collections.unmodifiableMap(countMap);
	}

	/**
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 */
	public void countToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(countMap, output);
	}

	/**
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @return {@code true} if successful in creating a JSON file
	 * @see #indexToJSON(Path)
	 */
	public boolean countToJSONSafe(Path output) {
		try {
			countToJSON(output);
		} catch (IOException e) {
			log.warn("Could not write JSON object of {}. Reason: {}", this.getClass().getSimpleName(), e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Returns {@code true} if the indexMap contains the word key.
	 *
	 * @param word the String key to be tested
	 * @return {@code true} if the indexMap contains the specified word
	 */
	public boolean contains(String word) {
		return indexMap.containsKey(word);
	}

	/**
	 * Returns {@code true} if a map paired with key word in
	 * indexMap contains the string location.
	 *
	 * @param word     the String key to be tested on indexMap
	 * @param location the String location to be tested an element in indexMap
	 * @return {@code true} if the indexMap.get(word) contains the specified location
	 */
	public boolean contains(String word, String location) {
		return contains(word) && indexMap.get(word).containsKey(location);
	}

	/**
	 * Returns {@code true} if a set paired with key location in
	 * a map paired with key word in
	 * indexMap contains the position.
	 *
	 * @param word     the String key to be tested on indexMap
	 * @param location the String location to be tested an element in indexMap
	 * @param position the long position to be tested on a an element of indexMap.get(location)
	 * @return {@code true} if the indexMap.get(word).get(location) contains the specified position
	 */
	public boolean contains(String word, String location, long position) {
		return contains(word, location) && indexMap.get(word).get(location).contains(position);
	}

	/**
	 * Returns an unmodifiable key set of indexMap, or specifically
	 * all words stored in Index.
	 *
	 * @return an unmodifiable set of indexMap.ketSet()
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(indexMap.keySet());
	}

	/**
	 * Returns an unmodifiable set of all file locations of a word.
	 *
	 * @param word the String key to retrieve an element of indexMap
	 * @return an unmodifiable set of indexMap.get(word).ketSet()
	 */
	public Set<String> getLocations(String word) {
		if (contains(word)) {
			return Collections.unmodifiableSet(indexMap.get(word).keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable set of all positions of a word found in a file location
	 *
	 * @param word     the String key to retrieve an element of indexMap
	 * @param location the String location to retrieve an element of indexMap.get(word)
	 * @return an unmodifiable set of indexMap.get(word).get(location)
	 */
	public Set<Long> getPositions(String word, String location) {
		if (contains(word, location)) {
			return Collections.unmodifiableSet(indexMap.get(word).get(location));
		}
		return Collections.emptySet();
	}

	/**
	 * A search result to store file location, word count, and word occurrence ratio.
	 * It is unique to only one location.
	 */
	public class SearchResult implements Comparable<SearchResult>, JSONObject {

		/**
		 * The String representation of a file location of where the word was found.
		 */
		private final String where;

		/**
		 * The number of occurrence of a certain word in a file.
		 */
		private long count;

		/**
		 * Word occurrence ratio to total word count of a file.
		 */
		private double score;

		/**
		 * Constructs a new SearchResult,
		 * storing the file location, word count, and score.
		 *
		 * @param where String representing the file location
		 * @param word  Search result phrase
		 */
		public SearchResult(String word, String where) {
			this.where = where;
			this.count = 0;
			this.score = 0.0;
			update(word);
		}

		/**
		 * Returns the String representation of a file location of where the word was found.
		 *
		 * @return String representation of a path
		 */
		public String getWhere() {
			return where;
		}

		/**
		 * Returns the number of occurrence of a certain word in a file.
		 *
		 * @return count of a particular String
		 */
		public long getCount() {
			return count;
		}

		/**
		 * Returns the ratio between word count of a particular String and total word count
		 *
		 * @return (word count) / (total word count)
		 */
		public double getScore() {
			return score;
		}

		/**
		 * Updates this SearchResult if the word never existed before.
		 *
		 * @param word the word to be added to this search result.
		 */
		private void update(String word) {
			this.count += indexMap.get(word).get(where).size();
			this.score = (double) count / countMap.get(where);
		}

		@Override
		public String toString() {
			return this.toJSONObjectString(0);
		}

		@Override
		public void toJSONObject(Writer writer, int level) throws IOException {
			String scoreFormat = "%.8f";

			SimpleJsonWriter.indent(writer, level);
			writer.write("{\n");
			SimpleJsonWriter.indent(writer, level + 1);
			writer.write("\"where\": ");
			SimpleJsonWriter.quote(where, writer);
			writer.write(",\n");
			SimpleJsonWriter.indent(writer, level + 1);
			writer.write("\"count\": ");
			writer.write(Long.toString(count));
			writer.write(",\n");
			SimpleJsonWriter.indent(writer, level + 1);
			writer.write("\"score\": ");
			writer.write(String.format(scoreFormat, score));
			writer.write('\n');
			SimpleJsonWriter.indent(writer, level);
			writer.write('}');
		}

		/* Required if using sets */
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other instanceof SearchResult) {
				return compareTo((SearchResult) other) == 0;
			}
			return false;
		}

		@Override
		public int compareTo(SearchResult other) {
			int temp;
			if ((temp = -Double.compare(this.score, other.score)) == 0) {
				if ((temp = -Long.compare(this.count, other.count)) == 0) {
					return this.where.compareToIgnoreCase(other.where);
				}
			}
			return temp;
		}
	}

	/* -------- Bulk Operations -------- */

	/**
	 * Adds all of the elements in the specified InvertedIndex
	 * onto this InvertedIndex.
	 *
	 * @param other the other InvertedIndex
	 */
	public void addAll(InvertedIndex other) {
		for (String word : other.indexMap.keySet()) {
			if (!indexMap.containsKey(word)) {
				indexMap.put(word, other.indexMap.get(word));
			} else {
				TreeMap<String, TreeSet<Long>> pathMap = indexMap.get(word);
				TreeMap<String, TreeSet<Long>> otherPathMap = other.indexMap.get(word);

				for (String path : otherPathMap.keySet()) {
					if (!pathMap.containsKey(path)) { //contains(path, word) gets blocked by read lock
						pathMap.put(path, otherPathMap.get(path));
					} else {
						pathMap.get(path).addAll(otherPathMap.get(path));
					}
				}
			}
		}

		for (String path : other.countMap.keySet()) {
			countMap.put(path,
					Math.max(other.countMap.get(path), countMap.getOrDefault(path, (long) 0))
			);
		}
	}
}