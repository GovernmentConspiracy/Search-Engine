import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * An index to store words and the location (both file location and position in file) of where those words were found.
 * <p>Auxiliary functions includes word counter and JSON writer
 *
 * @author Jason Liang
 * @version v2.1.0
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
		indexMap.get(word).get(pathString).add(location);

		countMap.put(
				pathString,
				Math.max(location, countMap.getOrDefault(pathString, (long) 0))
		);
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
		Map<String, SearchResult> searchResultMap = new TreeMap<>(); //location - SearchResult pair // TODO HashMap!
		BiPredicate<String, String> condition = exact ? String::equals : String::startsWith;

		for (String search : phrases) {
			/*
			 * TODO We still have a linear search here, through the keys... depending on the type of search
			 * we can optimize this
			 * 
			 * if its exact you only need to do if indexMap.containsKey(search)
			 * 
			 * Streams do not love mutable stuff. We are mutating the map and eventually the list.
			 */
			indexMap.entrySet().stream()
					.filter(e -> condition.test(e.getKey(), search))
					.forEach(
							wordComp -> {
								String word = wordComp.getKey();
								wordComp.getValue().forEach(
										(location, sizeComp) -> {
											if (!searchResultMap.containsKey(location)) {
												searchResultMap.put(
														location, new SearchResult(word, location)
												);
											} else {
												searchResultMap.get(location).update(word);
											}
										}
								);
							}
					);
		}
		
		/*
		 * TODO 

		exact search:
		for each query
			if the query is a key
				do stuff

		partial search:
		for each query
			loop only the keys we need...
			use tailMap(query) and break when no longer starts with
			https://github.com/usf-cs212-fall2019/lectures/blob/master/Data%20Structures/src/FindDemo.java#L146-L163
				do stuff
				
		private void searchHelper that does stuff
		itneracting with the map
		
		for each location... 
			if the location is in our map
				get the result and update
			else
				create a new search result
				add the result to the map
				add the same result to the list
		 */

		// TODO Can avoid this copy step
		ArrayList<SearchResult> results = new ArrayList<>(searchResultMap.values());
		Collections.sort(results);
		return results;
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
		 * A set of existing words already in this SearchResult
		 */
		private final Set<String> existingWords; // TODO Since you are taking in a set to search, can remove the logic associated with this

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
			existingWords = new HashSet<>();
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
			if (contains(word, where)) { // TODO Remove
				if (existingWords.add(word)) {
					this.count += indexMap.get(word).get(where).size();
					this.score = (double) count / countMap.get(where);
				}
			}
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

}
