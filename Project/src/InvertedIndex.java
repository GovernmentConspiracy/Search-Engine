import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An index to store words and the location (both file location and position in file) of where those words were found.
 * <p>Auxiliary functions includes word counter and JSON writer
 *
 * @author Jason Liang
 * @version v1.3.0
 */
public class InvertedIndex {
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
	 * Constructs a new empty inverted index and can pass in acceptable file extensions
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
			return false;
		}
		return true;
	}

	/**
	 * Gives the source to Query through SearchBuilder
	 * //TODO
	 * @param word
	 * @param exact
	 * @return
	 */
	public Map<String, Long> getWordFileCount(String word, boolean exact) {
		if (exact) {
			return getExactWordFileCount(word);
		}
		return getPartialWordFileCount(word);
	}

	/**
	 * //TODO
	 *
	 * @param word
	 * @return
	 */
	private Map<String, Long> getExactWordFileCount(String word) {
		if (contains(word)) {
			return indexMap.get(word).entrySet()
					.stream()
					.collect(
							Collectors.toUnmodifiableMap(
									Map.Entry::getKey, e -> (long) e.getValue().size()
							)
					);
		}
		return Collections.emptyMap();
	}

	/**
	 * //TODO
	 * @param word
	 * @return
	 */
	private Map<String, Long> getPartialWordFileCount(String word) {
		if (!indexMap.isEmpty()) {
			return indexMap.entrySet().stream()
					.filter(e -> e.getKey().startsWith(word))
					.map(Map.Entry::getValue)
					.flatMap(e -> e.entrySet().stream())
					.collect(
							Collectors.toUnmodifiableMap(
									Map.Entry::getKey, e -> (long) e.getValue().size(), Long::sum //resolve duplicates with addition
							)
					);
		}
		return Collections.emptyMap();
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
			//TODO logger
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
	 * @param word the String key to be tested on indexMap
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
	 * @param word the String key to be tested on indexMap
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
	 * @param word the String key to retrieve an element of indexMap
	 * @param location the String location to retrieve an element of indexMap.get(word)
	 * @return an unmodifiable set of indexMap.get(word).get(location)
	 */
	public Set<Long> getPositions(String word, String location) {
		if (contains(word, location)) {
			return Collections.unmodifiableSet(indexMap.get(word).get(location));
		}
		return Collections.emptySet();
	}

	public void addAll(InvertedIndex other) {
		other.indexMap.forEach((word, wordValue) -> wordValue.forEach((path, pathValue) -> pathValue.forEach(
				location -> this.indexPut(word, path, location)
		)));
	}
}
