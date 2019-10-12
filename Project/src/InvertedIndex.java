import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An index to store words and the location (both file location and position in file) of where those words were found.
 * <p>Auxiliary functions includes word counter and JSON writer
 *
 * @author Jason Liang
 * @version v1.1.0
 */
public class InvertedIndex {
	/**
	 * Default SnowballStemmer algorithm from OpenNLP.
	 */
	private static final SnowballStemmer.ALGORITHM DEFAULT_LANG = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * Nested data structure used to store location of where a word was found.
	 * Outer map stores (key - value) as (word - file location)
	 * Inner map stores (key - value) as (file location - position in file)
	 */
	private final Map<String, Map<String, Set<Long>>> indexMap; //String1 = word, String2 = Path, Set1 = Location

	/**
	 * Nested data structure used to store word count of a file
	 * Map stores (key - value) as (file location - number count)
	 */
	private final Map<String, Long> countMap;

	/*
	 * TODO indexMap and countMap must always have the same information in it
	 * 
	 * Every time you add anything to indexMap, update countMap
	 * 
	 * 1) If this is a non-duplicate add, increase the count by 1
	 * 2) Use the position as a proxy for the word count
	 * 
	 * add(hello, hello.txt, 5) <-- know there is at least 5 words in hello.txt
	 * add(hello, hello.txt, 2) <-- ignore, know we have at least 5 words
	 * add(world, hello.txt, 12) <--- update the count to 12
	 */
	
	/**
	 * Constructs a new empty inverted index and can pass in acceptable file extensions
	 */
	public InvertedIndex() {
		this.indexMap = new TreeMap<>();
		this.countMap = new TreeMap<>();
	}

	// TODO Move this to InvertedIndexBuilder
	/**
	 * Generates word - path - location pairs onto a nested map structure,
	 * storing where a stemmed word was found in a file and position
	 *
	 * @param input The file path which populates {@code indexMap}
	 * @throws IOException if path input could not be read
	 * @see #indexMap
	 */
	public void index(Path input) throws IOException {
		Stemmer stemmer = new SnowballStemmer(DEFAULT_LANG);
		try (
				BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)
		) {
			String line;
			long i = 0;
			while ((line = reader.readLine()) != null) {
				for (String word : TextParser.parse(line)) {
					indexPut(stemmer.stem(word).toString(), input.toString(), ++i);
				}
			}
			// TODO Remove (see other comments)
			if (i > 0)
				countMap.put(input.toString(), i); //writes into count map
		}

	}

	/**
	 * Helper method used to store word, pathString, and location into indexMap. See usages below
	 *
	 * @param word       A word made from a stemmed string. Used as the outer map key
	 * @param pathString A string representing the path where {@code word} in string form. Used as inner map key
	 * @param location   An long representing the location of where {@code word} was found in {@code pathString}
	 * @see #index(Path)
	 */
	public void indexPut(String word, String pathString, long location) {
		indexMap.putIfAbsent(word, new TreeMap<>());
		indexMap.get(word).putIfAbsent(pathString, new TreeSet<>());
		indexMap.get(word).get(pathString).add(location);
	}

	/**
	 * Generates a JSON text file of the inverted index, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 * @see #index(Path)
	 */
	public void indexToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(indexMap, output);
	}

	/**
	 * Generates a JSON text file of the inverted index, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @return {@code true} if successful in creating a JSON file
	 * @see #index(Path)
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
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @throws IOException if the output file could not be created or written
	 * @see #index(Path)
	 */
	public void countToJSON(Path output) throws IOException {
		SimpleJsonWriter.asObject(countMap, output);
	}

	/**
	 * Generates a JSON text file of the count of words, stored at Path output
	 *
	 * @param output The output path to store the JSON object
	 * @return {@code true} if successful in creating a JSON file
	 * @see #index(Path)
	 * @see #indexToJSON(Path)
	 */
	public boolean countToJSONSafe(Path output) {
		try {
			countToJSON(output);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean contains(String word) {
		return indexMap.containsKey(word);
	}

	public boolean contains(String word, String location) {
		return contains(word) && indexMap.get(word).containsKey(location);
	}

	public boolean contains(String word, String location, long position) {
		return contains(word, location) && indexMap.get(word).get(location).contains(position);
	}

	public Set<String> getWords() {
		return Collections.unmodifiableSet(indexMap.keySet());
	}

	public Set<String> getLocations(String word) {
		if (contains(word))
			return Collections.unmodifiableSet(indexMap.get(word).keySet());
		return Collections.emptySet();
	}

	public Set<Long> getPositions(String word, String location) {
		if (contains(word, location))
			return Collections.unmodifiableSet(indexMap.get(word).get(location));
		return Collections.emptySet();
	}

	public Map<String, Long> getCounts() {
		return Collections.unmodifiableMap(countMap);
	}

}
